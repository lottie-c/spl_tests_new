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
package cz.cuni.mff.spl;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * The wrapper for execution invoked from another tool.
 * <p>
 * Invoked execution is expected to be run in separate thread.
 * <p>
 * Invoked execution can be aborted in two ways. First way is call
 * {@link #setForceAbort(boolean)} with true value. Second is calling
 * {@link Thread#interrupt()} on thread with called SPL Tools Framework
 * Execution.
 * <p>
 * Abort is not instant, as SPL Tools Framework checks if abort is requested.
 * <p>
 * Invoking execution using this method is not thread safe as all used values
 * and flags are static.
 * <p>
 * If you need to run more invoked executions at the same time, use separate
 * class loaders.
 * 
 * @author Martin Lacina
 */
public class InvokedExecutionConfiguration {

    /** The logger. */
    private static final SplLog     logger              = SplLogger.getLogger(InvokedExecutionConfiguration.class);

    /** The flag indicating whether an invoked execution is in progress. */
    private static volatile boolean executionInProgress = false;

    /** The flag indicating whether to abort on interrupt. */
    private static volatile boolean abortOnInterrupt    = false;

    /** The execution in progress ID. */
    private static volatile long    executionInProgressID;

    /** The flag indicating whether to force abort. */
    private static volatile boolean forceAbort          = false;

    /**
     * Checks if execution using
     * {@link cz.cuni.mff.spl.InvokedExecution#run(boolean, java.io.File, java.io.File, java.io.File, String, cz.cuni.mff.spl.utils.interactive.InteractiveInterface)}
     * is in progress and if so, then it checks if it was aborted.
     */
    public static synchronized void checkIfExecutionAborted() {
        if (executionInProgress) {
            if (InvokedExecutionConfiguration.forceAbort || (InvokedExecutionConfiguration.abortOnInterrupt && Thread.currentThread().isInterrupted())) {
                logger.fatal("Received request for execution abort.");
                throw new SplRunInterrupted();
            }
        }
    }

    /**
     * Start execution.
     * 
     * @param executionID
     *            The execution id.
     * @param abortOnThreadInterrupt
     *            The abort on thread interrupt flag.
     */
    static synchronized void startExecution(long executionID, boolean abortOnThreadInterrupt) {
        if (executionInProgress) {
            throw new IllegalStateException("Not properly synchronized call to InvokedExecutionConfiguration as other execution is in progress.");
        }
        executionInProgress = true;
        abortOnInterrupt = abortOnThreadInterrupt;
        executionInProgressID = executionID;
        forceAbort = false;
    }

    /**
     * Stops execution with specified ID. ID is provided only to check if called
     * properly.
     * 
     * @param executionID
     *            The execution ID.
     * @throws IllegalStateException
     *             Thrown when current execution ID is not same as provided one.
     */
    static synchronized void stopExecution(long executionID) {
        if (executionInProgressID == executionID) {
            executionInProgress = false;
        } else {
            throw new IllegalStateException("Not properly synchronized call to InvokedExecutionConfiguration as other execution is in progress.");
        }
    }

    /**
     * Cancels execution.
     * 
     * @param executionID
     *            The execution id.
     */
    static synchronized void cancelExecution(long executionID) {
        if (executionInProgress && executionInProgressID == executionID) {
            forceAbort = true;
        }
    }

    /**
     * Sets the flag indicating whether an invoked execution is in progress.
     * 
     * @param executionInProgress
     *            The new flag indicating whether an invoked execution is in
     *            progress.
     */
    static void setExecutionInProgress(boolean executionInProgress) {
        InvokedExecutionConfiguration.executionInProgress = executionInProgress;
    }

    /**
     * SPL Execution was interrupted.
     * 
     * @author Martin Lacina
     */
    public static class SplRunInterrupted extends RuntimeException {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new spl run interrupted.
         */
        public SplRunInterrupted() {

        }

        /**
         * Instantiates a new spl run interrupted.
         * 
         * @param message
         *            The message.
         */
        public SplRunInterrupted(String message) {
            super(message);
        }

        /**
         * Instantiates a new spl run interrupted.
         * 
         * @param cause
         *            The cause.
         */
        public SplRunInterrupted(Throwable cause) {
            super(cause);
        }

        /**
         * Instantiates a new spl run interrupted.
         * 
         * @param message
         *            The message.
         * @param cause
         *            The cause.
         */
        public SplRunInterrupted(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
