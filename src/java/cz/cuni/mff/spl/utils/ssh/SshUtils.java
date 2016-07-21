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
package cz.cuni.mff.spl.utils.ssh;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Logger;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.AuthenticationException;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;
import cz.cuni.mff.spl.utils.ssh.exception.UnknownHostException;

/**
 * <p>
 * Basic functions to work with files accessible via secure shell protocol.
 * Supports functions such as creating and deleting of files and whole directory
 * trees. Reading and writing of files via streams and checking whether files or
 * directories exist at all.
 * 
 * <p>
 * Comments about Unix path.
 * 
 * <p>
 * Unix path may contain multiple following slashes which are equivalent to a
 * single slash and should be accepted.
 * 
 * <p>
 * However path should not start with multiple slashes though it may have
 * different meaning.
 * 
 * <p>
 * Path should end with a trailing slash when a directory is meant as a target
 * of such path. In case target of such path is directory represented with a
 * symbolic link it may be pretty important. If the slash is missing then the
 * target is the symbolic link itself and not the linked directory.
 * 
 * <p>
 * Based on The Open Group Base Specifications Issue 6; Unix Specification.
 * 
 * <p>
 * <a href="http://pubs.opengroup.org/onlinepubs/009695399/">http://pubs.
 * opengroup.org/onlinepubs/009695399/</a>
 * 
 * <p>
 * <a href=
 * "http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap04.html#tag_04_11"
 * >http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap04.html#
 * tag_04_11</a>
 * 
 * <p>
 * <a href=
 * "http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap03.html#tag_03_266"
 * >http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap03.html#
 * tag_03_266</a>
 * 
 * <p>
 * Windows should accept '/' in case '\\?\' is not used.
 * 
 * <p>
 * <a href="http://msdn.microsoft.com/en-us/library/aa365247%28VS.85%29.aspx">
 * http://msdn.microsoft.com/en-us/library/aa365247%28VS.85%29.aspx</a>
 * 
 * @author Frantisek Haas
 * 
 */
public class SshUtils {

    private static final SplLog logger = SplLogger.getLogger(SshUtils.class);

    /**
     * Checks if specified file exists on the host accessible using SSH and
     * login data.
     * 
     * @param channel
     *            Channel to access the file.
     * @param path
     *            Path of file to be checked.
     * @return
     *         True if path represents existing file.
     *         False if path does not exists or does not represent a file.
     * @throws SshException
     */
    public static boolean fileExists(ChannelSftp channel, String path)
            throws SshException {
        try {
            // this way of checking for file existence is based on Jsch's
            // docs on sourceforge
            channel.lstat(path);
            return true;

        } catch (SftpException e) {
            if (e.getMessage().equals("No such file")) {
                return false;
            }

            throw new SshException(String.format("Failed to check if file exists [%s].", path), e);
        }
    }

    /**
     * Checks if specified file exists on the host accessible using SSH and
     * login data.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Path of file to be checked.
     * @return
     *         True if path represents existing file.
     *         False if path does not exists or does not represent a file.
     * @throws SshException
     */
    public static boolean fileExists(SshDetails details, String path)
            throws SshException {
        Session session = createSession(details);
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            return fileExists(channel, path);

        } catch (JSchException e) {
            throw new SshException(e);
        } finally {
            session.disconnect();
        }
    }

    /**
     * Creates the file on specified path using SSH and login data.
     * 
     * @param channel
     *            Already opened session to the host.
     * @param path
     *            Path of file to be created.
     * @throws SshException
     */
    public static void createFile(ChannelSftp channel, String path)
            throws SshException {

        try (OutputStream stream = new SshOutputStream(channel, path)) {
            // flush only to disable warning on unused stream or multiple close
            // calls
            stream.flush();
            // auto close creates empty file
        } catch (IOException e) {
            throw new SshException("Failed to create file specified.", e);
        }
    }

    /**
     * Creates the file on specified path using SSH and login data.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Path of file to be created.
     * @throws SshException
     */
    public static void createFile(SshDetails details, String path)
            throws SshException {

        try (OutputStream stream = new SshOutputStream(details, path)) {
            // flush only to disable warning on unused stream or multiple close
            // calls
            stream.flush();
            // auto close creates empty file
        } catch (IOException e) {
            throw new SshException("Failed to create file specified.", e);
        }
    }

    /**
     * Removes the file on specified path using SSH.
     * 
     * @param channel
     *            Already opened session to the host.
     * @param path
     *            Path of file to be remove.
     * @throws SshException
     */
    public static void removeFile(ChannelSftp channel, String path)
            throws SshException {
        try {
            channel.rm(path);
        } catch (SftpException e) {
            throw new SshException("Failed to create file specified.", e);
        }
    }

    /**
     * Removes the file on specified path using SSH.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Path of file to be remove.
     * @throws SshException
     */
    public static void removeFile(SshDetails details, String path)
            throws SshException {
        Session session = createSession(details);
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            removeFile(channel, path);

        } catch (JSchException e) {
            throw new SshException(e);
        } finally {
            session.disconnect();
        }
    }

    /**
     * Internal function to {@link #directoryExists(ConnectionData, String)}
     * using
     * already opened session to the host for simple coupling of functions
     * without need to reconnect.
     * 
     * @param session
     *            Already opened session to the host.
     * @param path
     *            Path of directory to be checked.
     * @return
     * @throws SshException
     */
    private static boolean directoryExists(Session session, String path)
            throws SshException {
        try {
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            SftpATTRS attributes = channel.lstat(path);

            if (attributes != null && attributes.isDir()) {
                return true;
            } else {
                return false;
            }

        } catch (JSchException e) {
            throw new SshException(e);
        } catch (SftpException e) {
            throw new SshException(e);
        }
    }

    /**
     * Checks if specified file exists on the host accessible using SSH and
     * login data.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Path of directory to be checked.
     * @return
     *         True if path represents existing directory.
     *         False if path does not exists or does not represent a directory.
     * @throws SshException
     */
    public static boolean directoryExists(SshDetails details, String path)
            throws SshException {
        Session session = SshUtils.createSession(details);
        try {
            return directoryExists(session, path);
        } catch (Throwable e) {
            throw e;
        } finally {
            session.disconnect();
        }
    }

    /**
     * Parses Unix path into separated directory names.
     * 
     * @param path
     *            Unix slash separated path. Absolute or relative.
     * @param trailingSlash
     *            True if trailing slashes should be left at the end of
     *            directory name.
     * @param lastTrailingSlash
     *            True if trailing slash should be added at the end of last
     *            directory name. It is only compatible with 'trailingSlash' set
     *            true.
     * @return
     */
    private static String[] parseUnixPath(String path, boolean trailingSlash, boolean lastTrailingSlash) {
        final char slash = '/';
        final String Slash = "/";

        LinkedList<String> directories = new LinkedList<>();
        char[] chars = path.toCharArray();

        String buffer = new String();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // add root directory
            if (i == 0 && c == slash) {
                directories.add(Slash);
                continue;
            }

            if (c == slash) {
                if (buffer.endsWith(Slash) && trailingSlash || buffer.isEmpty()) {
                    // skip slash or slashes at the end of file name
                    continue;
                } else if (!trailingSlash) {
                    // add file without trailing slash to list
                    directories.add(buffer);
                    buffer = new String();
                } else {
                    // add file with trailing slash to list
                    buffer += c;
                    directories.add(buffer);
                    buffer = new String();
                }
            } else {
                buffer += c;
            }

        }

        if (!buffer.isEmpty()) {
            if (trailingSlash && lastTrailingSlash) {
                buffer += slash;
                directories.add(buffer);
            } else {
                directories.add(buffer);
            }
        }

        return directories.toArray(new String[directories.size()]);
    }

    /**
     * Creates all directories required to create the whole path.
     * 
     * @param session
     *            Already opened session to the host.
     * @param path
     *            Directory path to be created.
     * @throws SshException
     */
    public static void createDirectory(Session session, String path)
            throws SshException {
        ChannelSftp channel = null;
        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String[] directories = parseUnixPath(path, false, false);

            // it is pretty dirty but SftpExceptiom is used for detecting of
            // directory non existence after change directory call
            for (String directory : directories) {
                try {
                    // change directory and try this was if directory exists
                    channel.cd(directory);

                } catch (SftpException codeFlow) {
                    // directory apparently does not exist lets try make it
                    try {
                        channel.mkdir(directory);
                        channel.cd(directory);
                    } catch (SftpException e) {
                        // in this case we either failed to create the directory
                        // directly or failed to cd to the new directory
                        throw new SshException(e);
                    }
                }
            }

        } catch (JSchException e) {
            throw new SshException(e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    /**
     * Creates all directories required to create the whole path.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Directory path to be created.
     * @throws SshException
     */
    public static void createDirectory(SshDetails details, String path)
            throws SshException {
        Session session = SshUtils.createSession(details);
        try {
            createDirectory(session, path);
        } catch (Throwable e) {
            throw e;
        } finally {
            session.disconnect();
        }
    }

    @Deprecated
    public static void clearDirectory(SshDetails details, String path)
            throws SshException {
        throw new RuntimeException("Not implemented.");
    }

    @Deprecated
    public static void makeClearDirectory(SshDetails details, String path)
            throws SshException {
        throw new RuntimeException("Not implemented.");
    }

    @Deprecated
    public static void removeAll(SshDetails details, String path)
            throws SshException {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * Creates and connects session to specified destination with specified
     * login information.
     * 
     * Disconnect session on exit.
     * 
     * @param details
     *            Connection and login data.
     * @return
     *         Secure shell session object.
     * 
     * @throws SshException
     */
    public static Session createSession(SshDetails details)
            throws SshException {
        Session session = null;

        try {
            JSch jsch = createJsch(details);

            session = jsch.getSession(
                    details.getUsername(),
                    details.getUrl());

            session.setUserInfo(new SshUserInfo(details));
            session.connect();

            return session;

        } catch (JSchException e) {
            if (e.getCause() instanceof java.net.UnknownHostException) {
                throw new UnknownHostException(e);

            } else if (e.getMessage().equals("Auth fail")) {
                throw new AuthenticationException(e);

            } else {
                throw new SshException(e);
            }
        }
    }

    /**
     * Creates session on the specified JSch object.
     * 
     * @param jsch
     *            Already initialized JSch
     * @param details
     *            Connection and login data.
     * @return
     * @throws SshException
     */
    public static Session createSession(JSch jsch, SshDetails details)
            throws SshException {
        Session session = null;

        try {
            session = jsch.getSession(
                    details.getUsername(),
                    details.getUrl());

            session.setUserInfo(new SshUserInfo(details));
            session.connect();

            return session;

        } catch (JSchException e) {
            if (e.getCause() instanceof java.net.UnknownHostException) {
                throw new UnknownHostException(e);

            } else if (e.getMessage().equals("Auth fail")) {
                throw new AuthenticationException(e);

            } else {
                throw new SshException(e);
            }
        }
    }

    /**
     * Creates JSch object.
     * 
     * @param details
     *            Connection and login data.
     * @return
     * @throws SshException
     */
    public static JSch createJsch(SshDetails details)
            throws SshException {
        try {
            JSch jsch = new JSch();

            if (details.getKnownHostsPath() != null && details.getKnownHostsPath().length() > 0) {
                try {
                    // known hosts are copied so JSch does not change user's
                    // settings
                    Path path = Files.createTempFile("knownHosts", null);
                    FileUtils.copy(new File(details.getKnownHostsPath()), path.toFile());
                    jsch.setKnownHosts(path.toFile().getAbsolutePath());
                } catch (IOException e) {
                    logger.error("Failed to set known hosts file.", e);
                }
            }

            if (details.getKeyPath() != null && details.getKeyPath().length() > 0) {
                jsch.addIdentity(details.getKeyPath());
            }

            JSch.setLogger(new SshLogger());

            return jsch;

        } catch (JSchException e) {
            if (e.getCause() instanceof java.net.UnknownHostException) {
                throw new UnknownHostException(e);

            } else if (e.getMessage().equals("Auth fail")) {
                throw new AuthenticationException(e);

            } else {
                throw new SshException(e);
            }
        }
    }

    /**
     * <p>
     * Logger that forwards JSch messages to SPL logger.
     * 
     * @author Frantisek Haas
     * 
     */
    private static class SshLogger implements Logger {

        @Override
        public boolean isEnabled(int level) {
            return true;
        }

        @Override
        public void log(int level, String message) {
            logger.trace("JSchLevel-%s: %s", level, message);
        }
    }

    /**
     * <p>
     * This class is a holder object for JSch credentials. It's bound to
     * {@link SshDetails} and {@link InteractiveInterface}.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class SshUserInfo implements UserInfo, UIKeyboardInteractive {

        private static final SplLog logger     = SplLogger.getLogger(SshUserInfo.class);

        private final SshDetails    details;

        private String              password   = null;
        private String              passphrase = null;

        public SshUserInfo(SshDetails details) {
            this.details = details;
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
            LinkedList<String> prompts = new LinkedList<>();

            for (int i = 0; i < prompt.length; i++) {
                String myPrompt = String.format("[%s] [%s] [%s]\n%s", destination, name, instruction, prompt[i]);

                if (echo[i]) {
                    prompts.add(details.getString(myPrompt));
                } else {
                    prompts.add(details.getMaskedString(myPrompt));
                }
            }

            return prompts.toArray(new String[prompts.size()]);
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public boolean promptPassword(String message) {
            password = details.getMaskedString(message);
            if (password != null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean promptPassphrase(String message) {
            passphrase = details.getMaskedString(message);
            if (passphrase != null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean promptYesNo(String message) {
            if (message.startsWith("The authenticity of host") && message.contains("key fingerprint")) {
                if (details.getTrustAll()) {
                    return true;
                }

                if (message.contains("fingerprint is " + details.getFingerprint())) {
                    return true;
                }
            }

            return details.getBoolean(message);
        }

        @Override
        public void showMessage(String message) {
            logger.info(message);
        }
    }
}
