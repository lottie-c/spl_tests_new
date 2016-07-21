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

import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * Class extends InputStream for reading files via SSH.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshInputStream extends InputStream {

    private final boolean     closeSession;

    private final Session     session;
    private final ChannelSftp channel;
    private final InputStream stream;

    /**
     * Opens custom InputStream of remote file for reading.
     * 
     * @param details
     *            Login to the machine accessed via SSH.
     * @param path
     *            Path of file to be read.
     * @return
     *         InputStream to read the file.
     * @throws SshException
     */
    public SshInputStream(SshDetails details, String path)
            throws SshException {
        closeSession = true;
        session = SshUtils.createSession(details);

        try {
            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
        } catch (JSchException e) {
            throw new SshException(e);
        }

        try {
            stream = channel.get(path);
        } catch (SftpException e) {
            throw new SshException(e);
        }
    }

    /**
     * Opens custom InputStream of remote file for reading.
     * 
     * Object constructed via this constructor does not closes the session
     * and channel when closed.
     * 
     * @param channel
     *            Already opened sftp channel for transfer.
     * @param path
     *            Path of file to be read.
     * @return
     *         InputStream to read the file.
     * @throws SshException
     * @throws JSchException
     */
    public SshInputStream(ChannelSftp sftpChannel, String path)
            throws SshException {
        closeSession = false;
        channel = sftpChannel;

        try {
            session = channel.getSession();
        } catch (JSchException e) {
            throw new SshException(e);
        }

        try {
            stream = channel.get(path);
        } catch (SftpException e) {
            throw new SshException(e);
        }
    }

    @Override
    public int read()
            throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b)
            throws IOException {
        return stream.read(b);
    };

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException {
        return stream.read(b, off, len);
    }

    @Override
    public void close()
            throws IOException {
        if (stream != null) {
            stream.close();
        }
        if (closeSession) {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

}
