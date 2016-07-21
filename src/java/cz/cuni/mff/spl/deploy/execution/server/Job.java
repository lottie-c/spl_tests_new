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
package cz.cuni.mff.spl.deploy.execution.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;

/**
 * Class wraps standard java Process class and adds some more features. Prior to
 * the execution itself it saves binaries to the specified location and then
 * executes specified command.
 * 
 * It is possible to retrieve the exit value of the process or the state of the
 * process. Normal output stream and error stream can be accessed represented as
 * a String it is not intended for heavy use but just for debugging purposes.
 * 
 * @author Frantisek Haas
 * 
 */
public class Job {

    private Process              process        = null;
    /** Process build to create the child process. */
    private final ProcessBuilder processBuilder = new ProcessBuilder();
    /** Whether to close all streams to the child process right after start. */
    private boolean              closeStreams   = false;
    /** Process output. */
    private String               output         = new String();
    /** Process error. */
    private String               error          = new String();

    @Deprecated
    public Job(String command) {
        processBuilder.command(command.split(" "));
    }

    @Deprecated
    public Job(String command, boolean closeStreams) {
        processBuilder.command(command.split(" "));
        this.closeStreams = closeStreams;
    }

    @Deprecated
    public Job(String command, File directory) {
        processBuilder.command(command.split(" "));
        processBuilder.directory(directory);
    }

    @Deprecated
    public Job(String command, File directory, boolean closeStreams) {
        processBuilder.command(command.split(" "));
        processBuilder.directory(directory);
        this.closeStreams = closeStreams;
    }

    public Job(String[] command, File directory) {
        processBuilder.command(command);
        processBuilder.directory(directory);
    }

    public Job(String[] command) {
        processBuilder.command(command);
    }

    /**
     * Executes the command in the directory.
     * 
     */
    public void execute()
            throws IOException {
        process = processBuilder.start();
        /**
         * <p>
         * It seams that reading streams from created process is a bit lot
         * tricky and hangs the reader. Therefore it's better to redirect stdout
         * and stderr to files in the forked application.
         */
        if (closeStreams) {
            process.getOutputStream().close();
            process.getInputStream().close();
            process.getErrorStream().close();
        }
    }

    /**
     * Kills the process.
     * 
     */
    public void destroy() {
        if (process != null) {
            process.destroy();
        }
    }

    /**
     * Returns stream to process' input stream.
     * 
     * @return
     * @throws IOException
     */
    public OutputStream getInput()
            throws IOException {
        if (process == null) {
            throw new IOException("Process not started.");
        }

        return process.getOutputStream();
    }

    /**
     * Reads entire process output stream into string.
     * 
     * May block if process is still running.
     * 
     * @return
     * @throws IOException
     */
    public String getOutput()
            throws IOException {
        if (process == null || closeStreams) {
            return output;
        }

        final int BUFFER_SIZE = 4096;
        final int EOF = -1;

        InputStream outputStream = process.getInputStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int length = outputStream.read(buffer);

        while (length != EOF) {
            output += new String(buffer, 0, length);
            length = outputStream.read(buffer);
        }

        return output;
    }

    /**
     * Reads entire process error stream into string.
     * 
     * May block if process is still running.
     * 
     * @return
     * @throws IOException
     */
    public String getError()
            throws IOException {
        if (process == null || closeStreams) {
            return error;
        }

        final int BUFFER_SIZE = 4096;
        final int EOF = -1;

        InputStream errorStream = process.getErrorStream();

        byte[] buffer = new byte[BUFFER_SIZE];
        int length = errorStream.read(buffer);

        while (length != EOF) {
            error += new String(buffer, 0, length);
            length = errorStream.read(buffer);
        }

        return error;
    }

    /**
     * Returns job exit status. If job has not yet finished returns -1 by
     * default.
     * 
     * @return
     */
    public int getExitStatus() {
        if (process == null) {
            return 0;
        }

        try {
            return process.exitValue();
        } catch (IllegalThreadStateException e) {
            // thrown if process is still running
            return -1;
        }
    }

    /**
     * Waits maximum of specified time. Returns true if job is not running.
     * Returns false otherwise.
     * 
     * @param milliseconds
     * @return
     */
    public boolean waitFor(long milliseconds) {
        if (!isRunning()) {
            return true;
        }

        final long millisecondsWaitInterval = 100;
        while (true) {
            if (milliseconds < millisecondsWaitInterval) {
                try {
                    Thread.sleep(milliseconds);
                } catch (InterruptedException e) {
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    // if execution was not aborted, clear the interrupted flag
                    Thread.interrupted();
                }
                return isRunning();

            } else {
                milliseconds -= millisecondsWaitInterval;
                try {
                    Thread.sleep(millisecondsWaitInterval);
                } catch (InterruptedException e) {
                    InvokedExecutionConfiguration.checkIfExecutionAborted();
                    // if execution was not aborted, clear the interrupted flag
                    Thread.interrupted();
                }

                if (!isRunning()) {
                    return true;
                }
            }
        }
    }

    /**
     * Indicates job status.
     * 
     * @return
     *         True - If job is still running.
     *         False - If job is not running yet or not anymore.
     */
    public boolean isRunning() {
        if (process == null) {
            return false;
        }

        try {
            process.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            // thrown if process is still running
            return true;
        }
    }

    /**
     * Indicates job status using process exit value.
     * 
     * @return
     *         True - If job has successfully finished returning zero.
     *         False - If job has not finished yet or not successfully.
     */
    public boolean isFinished() {
        if (process == null) {
            return false;
        }

        try {
            return (process.exitValue() == 0);
        } catch (IllegalThreadStateException e) {
            // thrown if process is still running
            return false;
        }
    }
}
