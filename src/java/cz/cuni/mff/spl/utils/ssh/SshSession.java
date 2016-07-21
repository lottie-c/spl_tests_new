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
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

public class SshSession implements ISshSession {

    private static final SplLog logger                 = SplLogger.getLogger(SshSession.class);
    private static final int    KEEP_ALIVE_INTERVAL_MS = 10000;

    private final SshDetails    details;
    private Session             session                = null;
    private ChannelSftp         sftpChannel            = null;

    public SshSession(SshDetails details)
            throws SshException {
        this.details = details;
    }

    @Override
    public void connect()
            throws SshException {
        try {
            session = SshUtils.createSession(details);
            session.setServerAliveInterval(KEEP_ALIVE_INTERVAL_MS);
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

        } catch (JSchException e) {
            throw new SshException("Failed to connect session.", e);
        }
    }

    @Override
    public boolean silentConnect() {
        try {
            connect();
            return true;

        } catch (SshException e) {
            logger.error(e, "Failed to connect session.");
            return false;
        }
    }

    @Override
    public boolean isConnected() {
        return (session != null && session.isConnected() && sftpChannel != null && sftpChannel.isConnected());
    }

    @Override
    public Session getSession()
            throws SshException {
        if (session == null) {
            connect();
        }
        return session;
    }

    @Override
    public ChannelSftp getSftpChannel()
            throws SshException {
        if (session == null) {
            connect();
        }

        return sftpChannel;
    }

    @Override
    public void close() {
        if (sftpChannel != null) {
            sftpChannel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
}
