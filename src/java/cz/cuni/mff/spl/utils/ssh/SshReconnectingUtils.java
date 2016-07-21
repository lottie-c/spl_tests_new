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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * <p>
 * Basic functions to operate with files and commands across secure shell. These
 * function are reconnecting. That means whenever function fails due to a
 * connection reason it keeps trying and trying.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshReconnectingUtils {

    private static final SplLog logger = SplLogger.getLogger(SshReconnectingSession.class);

    private static int          SLEEP  = 1000;

    /**
     * Checks if specified file exists on the host accessible using SSH and
     * login data.
     * 
     * @param session
     *            Already opened session to the host.
     * @param path
     *            Path of file to be checked.
     * @return
     *         True if path represents existing file.
     *         False if path does not exists or does not represent a file.
     * @throws SshException
     */
    public static boolean fileExists(SshReconnectingSession session, String path)
            throws SshException {
        while (true) {
            try {
                ChannelSftp channel = session.getSftpChannel();
                if (channel != null) {
                    /**
                     * <p>
                     * this way of checking for file existence is based on
                     * Jsch's docs on sourceforge
                     */
                    channel.lstat(path);
                    return true;
                }

            } catch (SftpException e) {
                if (e.getMessage().equals("No such file")) {
                    return false;
                }
                logger.trace("Troubles checking file existence.");
            }

            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }
    }

    /**
     * Creates the file on specified path using SSH and login data.
     * 
     * @param session
     *            Already opened session to the host.
     * @param path
     *            Path of file to be created.
     * @throws SshException
     */
    public static void createFile(SshReconnectingSession session, String path)
            throws SshException {
        while (true) {
            try {
                ChannelSftp channel = session.getSftpChannel();
                if (channel != null) {
                    SshUtils.createFile(channel, path);
                    return;
                }

            } catch (SshException e) {
                logger.trace("Troubles creating file.");
            }

            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
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
    public static void createDirectory(SshReconnectingSession session, String path)
            throws SshException {
        while (true) {
            try {
                Session s = session.getSession();
                if (s != null) {
                    SshUtils.createDirectory(s, path);
                    return;
                }

            } catch (SshException e) {
                logger.trace("Troubles creating directory.");
            }

            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }
    }
}
