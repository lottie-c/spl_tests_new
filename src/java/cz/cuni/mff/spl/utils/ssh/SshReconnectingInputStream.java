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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * <p>
 * Class extends {@link InputStream} for reading files via SSH. This is reliable
 * stream which on first call of {@link #read()}, {@link #read(byte[])} or
 * {@link #read(byte[], int, int)} opens the connection and reads the whole
 * file. Further calls to these functions are served from buffer.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshReconnectingInputStream extends InputStream {

    private static final SplLog          logger = SplLogger.getLogger(SshReconnectingInputStream.class);
    private static final int             SLEEP  = 1000;

    private final SshReconnectingSession session;

    /** Path of the file to read. */
    private final String                 path;

    /** Buffer to server read calls. */
    private ByteArrayInputStream         buffer;

    /** Shows whether the stream is closed. Cannot read from closed stream. */
    private boolean                      closed = false;

    /**
     * <p>
     * Opens custom {@link InputStream} of remote file for reading.
     * 
     * @param session
     *            Session to remote machine via SSH.
     * @param path
     *            Path of file to be written.
     * @return
     *         OutputStream to write the file.
     */
    public SshReconnectingInputStream(SshReconnectingSession session, String path) {
        this.session = session;
        this.path = path;
        this.buffer = null;
    }

    /**
     * Reads the whole file to the buffer.
     */
    private void receive() {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        SshInputStream input = null;

        while (true) {
            try {
                if (input != null) {
                    input.close();
                }

            } catch (IOException e) {
                logger.trace(e, "Troubles closing malfunctioning stream.");
            }

            try {
                ChannelSftp channel = session.getSftpChannel();
                if (channel != null) {
                    input = new SshInputStream(channel, path);
                    FileUtils.copy(input, data);
                    buffer = new ByteArrayInputStream(data.toByteArray());
                    break;
                }

            } catch (SshException e) {
                logger.trace(e, "Troubles initializing new ssh input stream. Never mind, will keep trying.");
            } catch (IOException e) {
                logger.trace(e, "Troubles receiving data from new ssh input stream. Never mind, will keep trying.");
            }

            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }

    }

    @Override
    public int read()
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        if (buffer == null) {
            receive();
        }

        return buffer.read();
    }

    @Override
    public int read(byte[] b)
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        if (buffer == null) {
            receive();
        }

        return buffer.read(b);
    };

    @Override
    public int read(byte[] b, int off, int len)
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        if (buffer == null) {
            receive();
        }

        return buffer.read(b, off, len);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        } else {
            closed = true;
        }

        if (buffer != null) {
            try {
                buffer.close();
            } catch (IOException e) {
                logger.trace("Troubles closing buffered input stream.");
            }
        }

        buffer = null;
    }
}
