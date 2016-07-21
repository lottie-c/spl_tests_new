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
import java.util.Map;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import com.trilead.ssh2.KnownHosts;

import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;

/**
 * <p>
 * Implementation of Subversion repository that allows check out of specified
 * revisions. There's also support for private authentication via password,
 * private key or passphrase protected private key.
 * 
 * <p>
 * The code is based on SVNKit library.
 * 
 * @author Frantisek Haas
 * 
 */
public class Subversion extends IRepository {

    /** URL of the repository. */
    private final String               url;
    /** Username to login with. */
    private String                     username;
    /** Private key to authenticate with. */
    private String                     keyPath;

    /** Class that handles remote public key. */
    private final HostVerifier         hostVerifier;

    /** Object to interact with user. */
    private final InteractiveInterface interactive;

    public Subversion(String url, Map<String, String> values, InteractiveInterface interactive) {
        this.url = url;
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

        boolean trustAll = false;
        String fingerprint = null;
        String knownHostsPath = null;

        if (values.containsKey("trustAll")) {
            trustAll = Boolean.valueOf(values.get("trustAll"));
        } else {
            trustAll = false;
        }

        if (values.containsKey("fingerprint")) {
            fingerprint = values.get("fingerprint");
        } else {
            fingerprint = null;
        }

        if (values.containsKey("knownHostsPath")) {
            knownHostsPath = values.get("knownHostsPath");
        } else {
            knownHostsPath = null;
        }

        this.hostVerifier = new HostVerifier(trustAll, fingerprint, knownHostsPath);

    }

    /**
     * This method sets the correct authentication provider based on the type of
     * transfer SvnKit is going to use.
     * 
     * @param cloneCommand
     */
    private void setNonInteractiveAuthentication(SVNClientManager clientManager) {
        if (username != null && keyPath != null) {
            clientManager.setAuthenticationManager(new KeyAuthentication(username, keyPath, "", 22, hostVerifier));

        } else if (username != null) {
            clientManager.setAuthenticationManager(new PasswordAuthentication(username, "", hostVerifier));

        } else {
            clientManager.setAuthenticationManager(new PublicAuthentication());
        }
    }

    /**
     * This method sets the correct authentication provider based on the type of
     * transfer SvnKit is going to use.
     * 
     * @param cloneCommand
     */
    private void setInteractiveAuthentication(SVNClientManager clientManager) {
        if (interactive == null) {
            return;
        }

        if (username != null && keyPath != null) {
            String passphrase = interactive.getMaskedString(String.format("Passphrase for %s:", url));
            clientManager.setAuthenticationManager(new KeyAuthentication(username, keyPath, passphrase, 22, hostVerifier));

        } else if (username != null) {
            String password = interactive.getMaskedString(String.format("Password for %s:", url));
            clientManager.setAuthenticationManager(new PasswordAuthentication(username, password, hostVerifier));

        } else {
            clientManager.setAuthenticationManager(new PublicAuthentication());
        }
    }

    /**
     * Splits 'what' parameter into revision identification part and repository
     * structure part. That's important in case repository URL was supplied
     * without (for example) '/trunk' or '/branches/name' specified and is
     * passed together with revision number.
     * 
     * Example: what="/trunk;HEAD", "/trunk" is added to the end of URL.
     * 
     * @param url
     * @param what
     * @return
     */
    private String parseUrl(String url, String what) {
        if (what.contains(";")) {
            what = what.split(";")[0];
            return url + what;
        } else {
            return url;
        }
    }

    /**
     * Returns resolved SVN revision. Splits 'what' parameter if repository
     * structure was specified at the beginning of 'what'.
     * 
     * @param what
     * @return
     * @throws VcsCheckoutException
     */
    private SVNRevision parseWhat(String what)
            throws VcsCheckoutException {
        if (what.contains(";")) {
            what = what.split(";")[1];
        }

        switch (what) {
            case "HEAD":
                return SVNRevision.HEAD;

            default:
                try {
                    return SVNRevision.parse(what);
                } catch (Throwable e) {
                    throw new VcsCheckoutException("Failed to parse revision [" + what + "].", e);
                }
        }
    }

    /**
     * <p>
     * Parameter 'what' might specify either only revision number if full
     * revision structure was specified earlier in the repository URL. Or might
     * specify also the structure.
     * </p>
     * 
     * <p>
     * Example:
     * <ul>
     * <li>url="://splvcstester@svn.code.sf.net/p/spl-tools/svn-testing"</li>
     * <li>what="/trunk;HEAD"</li>
     * <li>what="/branches/testing;42"</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Example:
     * <ul>
     * <li>url="://splvcstester@svn.code.sf.net/p/spl-tools/svn-testing/trunk"</li>
     * <li>what="HEAD"</li>
     * <li>what="42"</li>
     * </ul>
     * </p>
     * 
     */
    @Override
    public String checkout(String what, File where)
            throws VcsCheckoutException {

        checkWhere(where);

        try {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            SVNUpdateClient updateClient = clientManager.getUpdateClient();

            setNonInteractiveAuthentication(clientManager);

            long revisionNumber = updateClient.doCheckout(
                    SVNURL.parseURIDecoded(parseUrl(url, what)),
                    where,
                    SVNRevision.HEAD,
                    parseWhat(what),
                    SVNDepth.INFINITY,
                    true);

            return String.valueOf(revisionNumber);
        } catch (Throwable cause) {
            // ignore and try login with password or passphrase
        }

        try {
            SVNClientManager clientManager = SVNClientManager.newInstance();
            SVNUpdateClient updateClient = clientManager.getUpdateClient();

            setInteractiveAuthentication(clientManager);

            long revisionNumber = updateClient.doCheckout(
                    SVNURL.parseURIDecoded(parseUrl(url, what)),
                    where,
                    SVNRevision.HEAD,
                    parseWhat(what),
                    SVNDepth.INFINITY,
                    true);

            return String.valueOf(revisionNumber);

        } catch (Throwable e) {
            if (e.getMessage().contains("E175002: unknown host")) {
                throw new VcsCheckoutException("Unknown host [" + e.getCause().getCause().getMessage() + "].", e);

            } else if (e.getMessage().contains("E125002: URL protocol is not supported")) {
                throw new VcsCheckoutException("URL [" + url + "] protocol could not be resolved.", e);

            } else if (e.getMessage().contains("E170001: Authentication required for")) {
                throw new VcsCheckoutException("Repository authentication failed.", e);

            } else {
                throw new VcsCheckoutException(e);
            }
        }
    }

    /**
     * 
     * @author Frantisek Haas
     * 
     */
    public static class PublicAuthentication extends BasicAuthenticationManager {

        public PublicAuthentication() {
            super(null);
        }
    }

    /**
     * 
     * @author Frantisek Haas
     * 
     */
    public static class PasswordAuthentication extends BasicAuthenticationManager {

        private final HostVerifier hostVerifier;

        public PasswordAuthentication(String username, String password, HostVerifier hostVerifier) {
            super(username, password);
            this.hostVerifier = hostVerifier;
        }

        @Override
        public void verifyHostKey(String hostName, int port, String keyAlgorithm, byte[] hostKey)
                throws SVNException {
            if (!hostVerifier.verify(hostName, keyAlgorithm, hostKey)) {
                throw new SVNException(null);
            }
        }
    }

    /**
     * 
     * @author Frantisek Haas
     * 
     */
    public static class KeyAuthentication extends BasicAuthenticationManager {

        private final HostVerifier hostVerifier;

        public KeyAuthentication(String username, String keyPath, String passphrase, int port, HostVerifier hostVerifier) {
            super(username, new File(keyPath), passphrase, port);
            this.hostVerifier = hostVerifier;
        }

        @Override
        public void verifyHostKey(String hostName, int port, String keyAlgorithm, byte[] hostKey)
                throws SVNException {
            if (!hostVerifier.verify(hostName, keyAlgorithm, hostKey)) {
                throw new SVNException(null);
            }
        }
    }

    /**
     * <p>
     * SVNKit does not enable sufficient host public key verification. Therefore
     * this implementation based on Trilead SSH is used.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class HostVerifier {

        private final boolean trustAll;
        private final String  fingerprint;
        private final File    knownHosts;

        public HostVerifier(boolean trustAll, String fingerprint, String knownHostsPath) {
            this.trustAll = trustAll;
            this.fingerprint = fingerprint;

            if (knownHostsPath != null) {
                this.knownHosts = new File(knownHostsPath);
            } else {
                knownHosts = null;
            }
        }

        public String createFingerPrint(String keyAlgorithm, byte[] key) {
            return KnownHosts.createHexFingerprint(keyAlgorithm, key);
        }

        private boolean verifyFingerPrint(String hostName, String keyAlgorithm, byte[] hostKey) {
            try {
                String keyFingerPrint = KnownHosts.createHexFingerprint(keyAlgorithm, hostKey);
                return keyFingerPrint.startsWith(fingerprint);

            } catch (IllegalArgumentException e) {
                return false;
            }
        }

        private boolean verifyKnownHosts(String hostName, String keyAlgorithm, byte[] hostKey) {
            try {
                KnownHosts hosts = new KnownHosts();
                hosts.addHostkeys(knownHosts);

                return hosts.verifyHostkey(hostName, keyAlgorithm, hostKey) == KnownHosts.HOSTKEY_IS_OK;

            } catch (IllegalArgumentException e) {
                return false;

            } catch (IOException e) {
                return false;
            }
        }

        public boolean verify(String hostName, String keyAlgorithm, byte[] hostKey) {
            if (trustAll) {
                return true;

            } else if (fingerprint != null) {
                return verifyFingerPrint(hostName, keyAlgorithm, hostKey);

            } else if (knownHosts != null) {
                return verifyKnownHosts(hostName, keyAlgorithm, hostKey);

            } else {
                return false;
            }
        }
    }
}
