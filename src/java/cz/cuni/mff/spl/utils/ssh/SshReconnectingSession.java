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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * <p>
 * This class provides auto reconnecting session for JSch library.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshReconnectingSession implements ISshSession {

    private static final SplLog logger                 = SplLogger.getLogger(SshReconnectingSession.class);
    private static final int    KEEP_ALIVE_INTERVAL_MS = 10000;

    private final SshDetails    details;

    /** If session is initialized connecting and auto reconnecting is enabled. */
    private boolean             initialized            = false;

    private JSch                jsch                   = null;
    private Session             session                = null;
    private ChannelSftp         sftpChannel            = null;

    /** To log reconnecting only once per lost connection. */
    private boolean             logReconnecting        = true;

    /**
     * <p>
     * Creates the session. Does not open any connections. To start the session
     * working call {@link #connect}. Even if it fails further calls to
     * {@link #connect()}, {@link #getSession()} and {@link #getSftpChannel()}
     * will try to open the connection. The {@link #close()} closes the
     * connection.
     * 
     * @param details
     */
    public SshReconnectingSession(SshDetails details) {
        this.details = details;
    }

    /**
     * @return
     *         Returns connection details.
     */
    public SshDetails getDetails() {
        return details;
    }

    /**
     * <p>
     * Initializes the session and tries to open the connection.
     * 
     * @throws SshException
     *             <p>
     *             If connection fails. But later calls to {@link #connect()},
     *             {@link #getSession()} and {@link #getSftpChannel()} will try
     *             to open the connection again.
     */
    @Override
    public void connect()
            throws SshException {
        if (initialized && logReconnecting) {
            logger.info("Connection lost ... reconnecting.");
        }

        if (!initialized) {
            jsch = SshUtils.createJsch(details);
            initialized = true;
        }

        try {
            if (!isConnected()) {
                innerClose();

                try {
                    session = SshUtils.createSession(jsch, details);
                    session.setServerAliveInterval(KEEP_ALIVE_INTERVAL_MS);
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect();
                    logger.trace("Successfully connected session.");

                } catch (SshException e) {
                    innerClose();
                    logger.trace("Failed to connect session.");
                    throw e;
                } catch (JSchException e) {
                    innerClose();
                    logger.trace("Failed to connect session.");
                    throw new SshException(e);
                }
            }
        } finally {
            logReconnecting = isConnected();
        }
    }

    /**
     * <p>
     * Initializes the session and tries to open the connection.
     * 
     * @return
     *         <p>
     *         True if connected. False If connection fails. But later calls to
     *         {@link #connect()}, {@link #getSession()} and
     *         {@link #getSftpChannel()} will try to open the connection again.
     * 
     */
    @Override
    public boolean silentConnect() {
        try {
            connect();
            return true;
        } catch (SshException e) {
            return false;
        }
    }

    /**
     * Checks if connection is established. Tries to send keep alive message.
     * 
     * @return
     */
    @Override
    public boolean isConnected() {
        if (session == null || !session.isConnected() || sftpChannel == null || !sftpChannel.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * <p>
     * If there's a valid connected session it's returned. If there's not the
     * function tries to connect one and return it.
     * 
     * @return
     *         Session if it's valid and connected. Otherwise {@code null}.
     */
    @Override
    public Session getSession() {
        if (!initialized) {
            return null;
        }

        if (!isConnected()) {
            silentConnect();
        }

        return session;
    }

    /**
     * <p>
     * If there's a valid connected session it's returned. If there's not the
     * function tries to connect one and return it.
     * 
     * @return
     *         Session if it's valid and connected. Otherwise {@code null}.
     */
    @Override
    public ChannelSftp getSftpChannel() {
        if (!initialized) {
            return null;
        }

        if (!isConnected()) {
            silentConnect();
        }

        return sftpChannel;
    }

    public void innerClose() {
        if (sftpChannel != null) {
            sftpChannel.disconnect();
            sftpChannel = null;
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    @Override
    public void close() {
        initialized = false;
        innerClose();
    }
}
