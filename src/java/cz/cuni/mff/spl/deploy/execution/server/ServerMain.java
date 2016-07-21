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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * This class starts the execution server. At first it tries to lock the spllock
 * file to check no other server is currently running. If it succeeds successful
 * status is reported back to client. If it does not succeed failure is reported
 * back.
 * 
 * In case of any troubles error exit status is reported and error details are
 * written to standard error.
 * 
 * @author Frantisek Haas
 * 
 */
public class ServerMain {

    /**
     * Start status reported in case of success to standard out.
     */
    public static final int   START_STATUS_SUCCESS = 42;

    /**
     * Start status reported in case of failure to standard out.
     */
    public static final int   START_STATUS_FAILURE = 24;

    /**
     * Original output stream to console where this instance of program was
     * started.
     */
    private final PrintStream stdout               = System.out;

    /**
     * Original error stream to console where this instance of program was
     * started.
     */
    private final PrintStream stderr               = System.err;

    /**
     * File lock of this instance. This lock should be acquired by
     * {@link #lock()} function.
     * 
     * If the lock could not be acquired the SPL data file should be checked for
     * connection information to already running server.
     * 
     */
    private FileLock          lock                 = null;

    /**
     * Name of the shared lock file.
     */
    private final String      lockFileName         = ".spl-server-lock";

    /**
     * Server identification.
     */
    private final String      identification;

    /**
     * Initializes the scheduler.
     * 
     * @param identification
     */
    public ServerMain(String identification) {
        this.identification = identification;
    }

    /**
     * Starts the scheduler.
     * 
     */
    public void start() {
        redirectStdToLogs();

        if (lock()) {
            reportSuccessAndClose();
            runBatch();
        } else {
            reportFailureAndClose();
        }

        if (lock != null) {
            try {
                lock.release();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Writes exception stack trace to standard error, closes standard output
     * and error streams and exits with error exit status.
     * 
     * @param e
     */
    private void reportErrAndExit(Throwable e) {
        if (lock != null) {
            try {
                lock.release();
            } catch (IOException ignore) {
            }
        }

        stdout.close();
        e.printStackTrace(stderr);
        stderr.close();

        System.exit(1);
    }

    /**
     * 
     */
    private void reportSuccessAndClose() {
        stdout.write(START_STATUS_SUCCESS);
        stdout.close();
    }

    /**
     * 
     */
    private void reportFailureAndClose() {
        stdout.write(START_STATUS_FAILURE);
        stdout.close();
    }

    /**
     * Tries to lock the shared scheduler lock file. Returns true if lock was
     * successfully acquired, otherwise return false.
     * 
     * The spl file lock should be released by the system after the JVM shuts
     * down.
     * 
     * @return
     */
    @SuppressWarnings("resource")
    private boolean lock() {
        final String lockType = "rw";

        try {
            File lockFile = new File(lockFileName);

            RandomAccessFile lockFileAccess = new RandomAccessFile(lockFile, lockType);
            FileChannel channel = lockFileAccess.getChannel();
            lock = channel.tryLock();

            if (lock != null) {
                return true;
            } else {
                channel.close();
                lockFileAccess.close();
                lockFileAccess = null;
                return false;
            }

        } catch (IOException e) {
            reportErrAndExit(e);
        }

        return false;
    }

    /**
     * Redirects standard output and error streams to log files.
     * 
     * @throws IOException
     */
    private void redirectStdToLogs() {
        File out = new File(identification, Server.outFileName);
        File err = new File(identification, Server.errFileName);

        try {
            out.createNewFile();
            err.createNewFile();

            PrintStream outputStream = new PrintStream(new FileOutputStream(out));
            PrintStream errorStream = new PrintStream(new FileOutputStream(err));
            System.setOut(outputStream);
            System.setErr(errorStream);

        } catch (IOException e) {
            reportErrAndExit(e);
        }
    }

    /**
     * Run batch.
     * 
     * @param name
     * @param port
     */
    private void runBatch() {
        Server server = new Server(identification);
        server.run();
    }

    public static void main(String args[]) {
        if (args.length == 1) {
            String identification = args[0];
            try {
                ServerMain server = new ServerMain(identification);
                server.start();
            } catch (Throwable e) {
                // this should print to console which started this program
                e.printStackTrace();
                System.exit(1);
            }
        } else {
            new IllegalArgumentException("Wrong command line arguments.").printStackTrace();
            System.exit(1);
        }
    }
}
