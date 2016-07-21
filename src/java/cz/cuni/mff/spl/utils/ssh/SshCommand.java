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

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.cuni.mff.spl.utils.ssh.exception.SshException;

/**
 * Class serves to access execution progress and execution result information.
 * 
 * @author Frantisek Haas
 * 
 */
public class SshCommand implements AutoCloseable {

    private final boolean closeSession;

    private final Session session;
    private ChannelExec   channel;
    private InputStream   outputStream;
    private InputStream   errorStream;
    private String        output = new String();
    private String        error  = new String();

    public SshCommand(SshDetails details, String command)
            throws SshException {
        closeSession = true;
        session = SshUtils.createSession(details);

        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            outputStream = channel.getInputStream();
            errorStream = channel.getErrStream();

        } catch (JSchException e) {
            close();
            throw new SshException(e);
        } catch (Exception e) {
            close();
            throw new SshException(e);
        }
    }

    public SshCommand(Session session, String command)
            throws SshException {
        closeSession = false;
        this.session = session;

        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            outputStream = channel.getInputStream();
            errorStream = channel.getErrStream();

        } catch (JSchException e) {
            close();
            throw new SshException(e);
        } catch (Exception e) {
            close();
            throw new SshException(e);
        }
    }

    public void execute()
            throws SshException {
        try {
            channel.connect();
        } catch (JSchException e) {
            close();
            throw new SshException(e);
        }
    }

    /**
     * Releases all resources used, connections opened and stops all threads
     * serving request.
     * 
     */
    @Override
    public void close() {
        if (closeSession) {
            if (channel != null) {
                channel.disconnect();
            }

            if (session != null) {
                session.disconnect();
            }
        }
    }

    /**
     * Checks if command execution is still in progress.
     * 
     * @return
     */
    public boolean isRunning() {
        // -1 is JSch specific way to tell command is still running
        return channel.getExitStatus() == -1;
    }

    /**
     * Returns command exit status.
     * 
     * Value -1 indicates command execution is still in progress.
     * 
     * @return
     */
    public int getExitStatus() {
        return channel.getExitStatus();
    }

    /**
     * Returns command output. If command execution is still running it may
     * not return the whole output. However following calls will return more
     * and more complete data. Usually returns either nothing or complete
     * data.
     * 
     * It is not recommended to call both {@link #getOutput()} and
     * {@link #getOutputStream()} on the same instance.
     * 
     * @return
     * @throws SshException
     */
    public String getOutput()
            throws SshException {
        final int EOF = -1;

        try {
            while (outputStream.available() > 0) {
                int c = outputStream.read();
                if (c != EOF) {
                    output += (char) c;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new SshException(e);
        }
        return output;
    }

    /**
     * Returns command output stream. All data should be available when
     * command execution is not running anymore.
     * 
     * It is not recommended to call both {@link #getOutput()} and
     * {@link #getOutputStream()} on the same instance.
     * 
     * @return
     */
    public InputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Returns command output stream. All data should be available when
     * command execution is not running anymore.
     * 
     * It is not recommended to call both {@link #getOutput()} and
     * {@link #getOutputStream()} on the same instance.
     * 
     * This function returns stream which when closed closes also the whole SSH
     * session.
     * 
     * @return
     */
    public InputStream getSmartOutputStream() {
        return new CommandInputStream(outputStream);
    }

    /**
     * Returns command error. If command execution is still running it may
     * not return the whole output. However following calls will return more
     * and more complete data. Usually returns either nothing or complete
     * data.
     * 
     * It is not recommended to call both {@link #getError()} and
     * {@link #getErrorStream()} on the same instance.
     * 
     * @return
     * @throws SshException
     */
    public String getError()
            throws SshException {
        final int EOF = -1;

        try {
            while (errorStream.available() > 0) {
                int c = errorStream.read();
                if (c != EOF) {
                    error += (char) c;
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new SshException(e);
        }
        return error;
    }

    /**
     * Returns command error stream. All data should be available when
     * command execution is not running anymore.
     * 
     * It is not recommended to call both {@link #getError()} and
     * {@link #getErrorStream()} on the same instance.
     * 
     * @return
     */
    public InputStream getErrorStream() {
        return errorStream;
    }

    /**
     * Returns command error stream. All data should be available when
     * command execution is not running anymore.
     * 
     * It is not recommended to call both {@link #getError()} and
     * {@link #getErrorStream()} on the same instance.
     * 
     * This function returns stream which when closed closes also the whole SSH
     * session.
     * 
     * @return
     */
    public InputStream getSmartErrorStream() {
        return new CommandInputStream(outputStream);
    }

    /**
     * This stream enables reading from SSH session stream and when closed also
     * closes the whole SSH session.
     * 
     * @author Frantisek Haas
     * 
     */
    public class CommandInputStream extends InputStream {

        private final InputStream stream;

        public CommandInputStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public void close() {
            SshCommand.this.close();
        }
    }
}
