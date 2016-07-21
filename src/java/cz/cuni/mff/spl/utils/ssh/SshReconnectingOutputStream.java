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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelSftp;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * <p>
 * Class extends {@link OutputStream} for writing files via SSH. This is
 * reliable stream which whenever connection is lost tries to reconnects.
 * Therefore #write(...) functions and {@link #close()} may block until
 * connection is established.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshReconnectingOutputStream extends OutputStream {

    private static final SplLog          logger = SplLogger.getLogger(SshReconnectingOutputStream.class);
    private static final int             SLEEP  = 1000;

    private final SshReconnectingSession session;

    /** Path of the file to write. */
    private final String                 path;

    /**
     * <p>
     * Buffer to store write calls. If connection is lost the whole buffer is
     * re-send to the file.
     */
    private ByteArrayOutputStream        buffer;

    /** Stream to the remote file. */
    private SshOutputStream              output;

    /** Shows whether the stream is closed. Cannot write to closed stream. */
    private boolean                      closed = false;

    /**
     * <p>
     * Opens custom {@link OutputStream} of remote file for writing. File needs
     * not to exist before writing and is created as is written. If file does
     * exist it is overwritten.
     * 
     * <p>
     * All data is transferred to the stream end and also buffered locally.
     * Whenever transfer fails the data is re-send.
     * 
     * @param session
     *            Session to remote machine via SSH.
     * @param path
     *            Path of file to be written.
     * @return
     *         OutputStream to write the file.
     */
    public SshReconnectingOutputStream(SshReconnectingSession session, String path) {
        this.session = session;
        this.path = path;
        this.buffer = new ByteArrayOutputStream();
        this.output = null;
    }

    /**
     * <p>
     * Opens new connection to the remote file and resends all buffered data.
     */
    private void resend() {
        while (true) {
            try {
                if (output != null) {
                    output.close();
                }

            } catch (IOException e) {
                logger.trace(e, "Troubles closing malfunctioning stream.");
            }

            try {
                ChannelSftp channel = session.getSftpChannel();
                if (channel != null) {
                    output = new SshOutputStream(channel, path);
                    output.write(buffer.toByteArray());
                    break;
                }

            } catch (SshException e) {
                logger.trace(e, "Troubles initializing new ssh output stream. Never mind, will keep trying.");
            } catch (IOException e) {
                logger.trace(e, "Troubles resending data to new ssh output stream. Never mind, will keep trying.");
            }

            try {
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }
    }

    @Override
    public void write(int b)
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        buffer.write(b);

        try {
            output.write(b);
        } catch (NullPointerException | IOException e) {
            resend();
        }
    }

    @Override
    public void write(byte[] b)
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        buffer.write(b);

        try {
            output.write(b);
        } catch (NullPointerException | IOException e) {
            resend();
        }
    }

    @Override
    public void write(byte[] b, int off, int len)
            throws IOException {
        if (closed) {
            throw new IOException("Stream already closed.");
        }

        buffer.write(b, off, len);

        try {
            output.write(b, off, len);
        } catch (NullPointerException | IOException e) {
            resend();
        }
    }

    @Override
    public void close()
            throws IOException {
        if (closed) {
            return;
        } else {
            closed = true;
        }

        if (output != null) {
            while (true) {
                try {
                    output.close();
                    break;

                } catch (IOException e) {
                    resend();
                }

                try {
                    Thread.sleep(SLEEP);
                } catch (InterruptedException e) {
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                }
            }

            output = null;
        }

        if (buffer != null) {
            buffer.close();
            buffer = null;
        }
    }
}
