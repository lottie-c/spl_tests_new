/*
 * Copyright (c) 2012, František Haas, Martin Lacina, Jaroslav Kotrč, Jiří Daniel
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING 
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package cz.cuni.mff.spl.deploy.build.vcs;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.errors.NoRemoteRepositoryException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.SshDetails;
import cz.cuni.mff.spl.utils.ssh.SshUtils.SshUserInfo;

/**
 * <p>
 * Implementation of Git repository that allows check out of specified
 * revisions. There's also support for private authentication via password,
 * private key or passphrase protected private key.
 * 
 * <p>
 * The code is based on JGit library. In some cases when JGit fails to
 * successfully check out the revision probably due to differences in
 * implementation and semantics between JGit and main stream Git a fall back to
 * Git on system path is present and seamlesslly used.
 * 
 * @author Frantisek Haas
 * 
 */
public class Git extends IRepository {

    private static final SplLog        logger       = SplLogger.getLogger(Git.class);

    /** Directory to use as a cache for cloning repository. */
    private File                       cache;
    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File                 localStoreRootDirectory;
    /** Flag whether the repository is already cached or not. */
    private boolean                    cached       = false;

    /** URL of the repository. */
    private final String               url;
    /** Username to login with. */
    private final String               username;
    /** Path to private key to authenticate with. */
    private final String               keyPath;

    /** Whether to trust to all remote public keys. */
    private final boolean              trustAll;
    /** Fingeprint of remote public key to trust. */
    private final String               fingerprint;
    /** File with known remote public keys. */
    private final String               knownHostsPath;

    /** Object to interact with user. */
    private final InteractiveInterface interactive;

    /** Whether to use system's git. If not, JGit is used by default. */
    private boolean                    useSystemGit = false;

    /**
     * <p>
     * Initializes repository without cache file. Every time
     * 
     * @param url
     *            The repository url.
     * @param values
     *            Optional values to access the repository.
     * @param localStoreRootDirectory
     *            The {@link LocalStore} root directory or {@code null} when not
     *            available.
     * @param interactive
     *            Object to interact with user. Null if no interaction.
     *            {@link #checkout(String, File)} is called the repository is
     *            cloned again.
     */
    public Git(String url, Map<String, String> values, InteractiveInterface interactive, File localStoreRootDirectory) {
        this(url, values, interactive, localStoreRootDirectory, null);
    }

    /**
     * <p>
     * Initializes repository with cache file. Only the first time the
     * 
     * @param url
     *            The repository url.
     * @param values
     *            Optional values to access the repository.
     * @param localStoreRootDirectory
     *            The {@link LocalStore} root directory or {@code null} when not
     *            available.
     * @param interactive
     *            Object to interact with user. Null if no interaction.
     * @param cache
     *            Directory to clone repository to.
     *            {@link #checkout(String, File)} is called the repository is
     *            cloned. All
     *            other calls use locally cloned repository.
     */
    public Git(String url, Map<String, String> values, InteractiveInterface interactive, File cache, File localStoreRootDirectory) {

        this.url = url;
        this.localStoreRootDirectory = localStoreRootDirectory;
        this.cache = cache;
        this.interactive = interactive;

        if (values.containsKey("username")) {
            this.username = values.get("username");
        } else {
            this.username = null;
        }

        if (values.containsKey("keyPath")) {
            this.keyPath = values.get("keyPath");
        } else {
            this.keyPath = null;
        }

        if (values.containsKey("trustAll")) {
            this.trustAll = Boolean.valueOf(values.get("trustAll"));
        } else {
            this.trustAll = false;
        }

        if (values.containsKey("fingerprint")) {
            this.fingerprint = values.get("fingerprint");
        } else {
            this.fingerprint = null;
        }

        if (values.containsKey("knownHostsPath")) {
            this.knownHostsPath = values.get("knownHostsPath");
        } else {
            this.knownHostsPath = null;
        }

    }

    /**
     * <p>
     * Debug function to switch to system git only.
     */
    void setUseSystemGit() {
        useSystemGit = true;
    }

    /**
     * This method sets the correct authentication provider based on the type of
     * transfer JGit is going to use.
     * 
     * @param cloneCommand
     */
    private void setAuthentication(CloneCommand cloneCommand) {
        if (username != null && keyPath != null) {
            cloneCommand.setTransportConfigCallback(new MyTransportConfigCallback());

        } else if (username != null) {
            cloneCommand.setCredentialsProvider(new MyAuthentication());
        }
    }

    /**
     * <p>
     * Clones repository to specified folder.
     * 
     * @param url
     *            What repository to clone.
     * @param where
     *            Where to checkout.
     * @throws Throwable
     */
    private void innerClone(String url, File where)
            throws Throwable {
        CloneCommand cloneCommand = org.eclipse.jgit.api.Git.cloneRepository();
        cloneCommand.setBare(false);
        cloneCommand.setCloneAllBranches(true);
        cloneCommand.setURI(url);
        cloneCommand.setDirectory(where);
        cloneCommand.setNoCheckout(false);
        setAuthentication(cloneCommand);
        cloneCommand.call();
    }

    /**
     * <p>
     * Checks out revision using JGit.
     * 
     * @param what
     *            What revision to checkout.
     * @param where
     *            Where to checkout.
     * @return
     *         Revision's hash.
     * 
     * @throws Throwable
     */
    private String innerCheckout(String what, File where)
            throws Throwable {
        org.eclipse.jgit.api.Git git =
                org.eclipse.jgit.api.Git.open(where);

        CheckoutCommand command = git.checkout();
        command.setName(what);
        command.setForce(true);
        command.call();

        String revisionHash = git.getRepository().resolve(what).getName();
        return revisionHash;
    }

    /**
     * <p>
     * If JGit fails, fall back to system git if such is present.
     * 
     * @param what
     *            What revision to checkout.
     * @param where
     *            Where to checkout.
     * @return
     *         Revision's hash.
     * 
     * @throws Throwable
     */
    private String handleSystem(String what, File where)
            throws Throwable {
        if (!cached && cache != null) {
            GitSystem.clone(url, cache);
            cached = true;
        }

        if (cached) {
            String hash = GitSystem.checkout(what, cache);
            FileUtils.copyDirectory(cache, where, localStoreRootDirectory, new File(cache, ".git"));
            return hash;

        } else {
            GitSystem.clone(url, where);
            return GitSystem.checkout(what, where);
        }
    }

    /**
     * <p>
     * Handles git operations. Clones the remote repository and checks out
     * specified revision. If cache file is present clones the remote repository
     * only once.
     * 
     * @param what
     *            What revision to checkout.
     * @param where
     *            Where to checkout.
     * @return
     *         Revision's hash.
     * 
     * @throws Throwable
     */
    private String handle(String what, File where)
            throws Throwable {
        if (useSystemGit) {
            return handleSystem(what, where);
        }

        try {
            if (!cached && cache != null) {
                innerClone(url, cache);
                cached = true;
            }

            if (cached) {
                String hash = innerCheckout(what, cache);
                FileUtils.copyDirectory(cache, where, localStoreRootDirectory, new File(cache, ".git"));
                return hash;

            } else {
                innerClone(url, where);
                return innerCheckout(what, where);
            }

        } catch (CheckoutConflictException e) {
            /**
             * <p>
             * Fall back especially in case of this exception. This exception
             * has occurred for unknown reasons and this hack should solve it if
             * it happens again.
             */

            if (!GitSystem.isPresent()) {
                throw e;
            }

            logger.debug(e, "Troubles using JGit. Falling back to system's git.");

            if (cache != null) {
                /**
                 * <p>
                 * Create a new cache inside the old one because crashed JGit
                 * keeps some files open and it's not possible to delete them
                 * right now.
                 */
                cache = new File(cache, ".git-spl-cache");
                FileUtils.makeClearDirectory(cache);
            }
            cached = false;
            FileUtils.makeClearDirectory(where);
            useSystemGit = true;
            return handleSystem(what, where);
        }
    }

    @Override
    public String checkout(String what, File where)
            throws VcsCheckoutException {

        what = what.trim();

        if ((what == null) || what.isEmpty()) {
            logger.error("Empty revision name in %s.", url);
            throw new VcsCheckoutException("Revision name cannot be empty.");
        }

        checkWhere(where);

        try {
            return handle(what, where);

        } catch (JGitInternalException | IllegalArgumentException e) {
            if (e.getCause() != null
                    && e.getCause().getCause() != null
                    && e.getCause().getCause() instanceof ConfigInvalidException) {
                logger.error("Git system or repository config may contain BOM at the beginning of the file which JGit cannot handle right now. Try to remove the BOM or edit the config with 'git config -e'.");
                throw new VcsCheckoutException("Git config is missing, corrupted or may contain unexpected (for JGit) BOM at the beginning of file.", e);
            } else {
                throw new VcsCheckoutException(e);
            }

        } catch (Throwable e) {
            if (e.getCause() != null
                    && e.getCause().getCause() != null
                    && e.getCause().getCause() instanceof UnknownHostException) {
                throw new VcsCheckoutException("Unknown host [" + e.getCause().getCause().getMessage() + "].", e);

            } else if (e.getCause() != null
                    && e.getCause().getCause() != null
                    && e.getCause().getCause() instanceof NoRemoteRepositoryException
                    && e.getCause().getCause().getMessage().contains("not found")) {
                throw new VcsCheckoutException("Repository not found [" + url + "].", e);

            } else if (e.getCause() != null
                    && e.getCause() instanceof NotSupportedException
                    && e.getCause().getMessage().contains("URI not supported")) {
                throw new VcsCheckoutException("URL [" + url + "] protocol could not be resolved.", e);

            } else if (e.getCause() != null
                    && e.getCause() instanceof TransportException
                    && e.getCause().getMessage().contains("not authorized")) {
                throw new VcsCheckoutException("Repository authentication failed.", e);

            } else {
                throw new VcsCheckoutException(e);
            }
        }
    }

    /**
     * This class is used for credentials resolving when https password or ssh
     * password authentication is used.
     * 
     * @author Frantisek Haas
     * 
     */
    public class MyAuthentication extends CredentialsProvider {

        public MyAuthentication() {

        }

        @Override
        public boolean get(URIish arg0, CredentialItem... items)
                throws UnsupportedCredentialItem {
            boolean result = true;

            for (CredentialItem item : items) {
                if (item.getPromptText().equals("Username")
                        && item instanceof CredentialItem.StringType) {
                    // https request
                    // username must be set for further credentials prompts
                    if (username != null) {
                        ((CredentialItem.StringType) item).setValue(username);
                    } else {
                        result = false;
                    }

                } else if (item instanceof CredentialItem.Password) {
                    // https request
                    // note the credential type
                    if (interactive != null) {
                        ((CredentialItem.Password) item).setValue(interactive.getMaskedString(item.getPromptText() + " for " + arg0).toCharArray());
                    } else {
                        result = false;
                    }

                } else if (item instanceof CredentialItem.StringType) {
                    // ssh request
                    // note the credential type
                    if (interactive != null) {
                        ((CredentialItem.StringType) item).setValue(interactive.getString(item.getPromptText() + " for " + arg0));
                    } else {
                        result = false;
                    }

                } else {
                    result = false;
                }
            }

            return result;
        }

        @Override
        public boolean isInteractive() {
            return interactive != null;
        }

        @Override
        public boolean supports(CredentialItem... items) {
            return true;
        }
    }

    /**
     * This class is used to create session when JGit is using SSH connection
     * (either key or password authentication). This class then sets the key
     * path to the SSH session and sets user login provider.
     * 
     * @author Frantisek Haas
     * 
     */
    public class MySessionFactory extends JschConfigSessionFactory {

        public MySessionFactory() {

        }

        @Override
        protected void configure(Host hc, Session session) {
            session.setUserInfo(new SshUserInfo(new SshDetails(url, username, keyPath, trustAll, fingerprint, knownHostsPath, interactive)));
        }

        @Override
        protected Session createSession(Host hc, String user, String host, int port, FS fs)
                throws JSchException {
            getJSch(hc, fs).addIdentity(keyPath);

            // JGit tries to find default user specific known_hosts file
            // we do not want to change default known_hosts file
            getJSch(hc, fs).setHostKeyRepository(null);

            if (knownHostsPath != null) {
                try {
                    // create temporary known hosts so JSch won't modify the
                    // user specific known_hosts
                    File tmpKnownHosts = File.createTempFile("known_hosts", null);
                    tmpKnownHosts.deleteOnExit();

                    FileUtils.copy(new File(knownHostsPath), tmpKnownHosts);

                    getJSch(hc, fs).setKnownHosts(tmpKnownHosts.getAbsolutePath());
                } catch (IOException e) {
                    // ignore and keep host key repository to null
                }
            }

            return super.createSession(hc, user, host, port, fs);
        }
    }

    /**
     * This class is used to set the {@link MySessionFactory} as a factory for
     * SSH session if JGit is using SSH for transfer.
     * 
     * @author Frantisek Haas
     * 
     */
    public class MyTransportConfigCallback implements TransportConfigCallback {

        public MyTransportConfigCallback() {

        }

        @Override
        public void configure(Transport transport) {
            if (transport instanceof SshTransport) {
                ((SshTransport) transport).setSshSessionFactory(new MySessionFactory());
            }
        }
    }
}
