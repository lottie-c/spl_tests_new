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

import java.io.File;

import cz.cuni.mff.spl.InvokedExecutionConfiguration.SplRunInterrupted;
import cz.cuni.mff.spl.Run.SplRunError;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplDynamicTargetLoggerFactory;

/**
 * <p>
 * This class is API for invoking SPL Tools Framework.
 * 
 * <p>
 * Create instance and call
 * {@link #run(boolean, File, File, File, String, InteractiveInterface, Appendable, int, boolean)}
 * in separate thread.
 * 
 * <p>
 * If you need to cancel running execution, then call {@link #cancelExecution()}.
 * 
 * @author Martin Lacina
 */
public class InvokedExecution {

    /** The execution ID. */
    private final long       executionID;

    /** The cancelled flag. */
    private volatile boolean cancelled;

    /**
     * Instantiates a new invoked execution.
     */
    public InvokedExecution() {
        this.executionID = System.nanoTime();
    }

    /**
     * Runs SPL Tools Framework. The execution is placed to queue of executions
     * (implemented using static synchronized method run on this class).
     * <p>
     * This method is not synchronized to allow calling
     * 
     * @param abortOnThreadInterrupt
     *            The value for {@link #setAbortOnInterrupt(boolean)}. True
     *            value indicates that execution should be aborted, if executing
     *            thread is interrupted.
     * @param xml
     *            SPL XML file. Required
     * @param wd
     *            SPL working directory. Required.
     * @param ini
     *            SPL INI configuration file. Not required.
     * @param machine
     *            Machine where to perform measuring. Not required, defaults to
     *            local machine.
     * @param interactive
     *            Object to interact with user. Not required, defaults to no
     *            interaction.
     * @param outputTarget
     *            The output target.
     * @param logDetailLevel
     *            The log detail level.
     * @param acceptExceptions
     *            <p>
     *            The accept exceptions flag, value {@code true} means, that
     *            exceptions will be passed to output, {@code false} that they
     *            won't be passed to output. Fatal error exceptions are always
     *            passed to output.
     * @return The string with path to evaluation result.
     * @throws SplRunError
     *             The SPL run error. {@link #cancelExecution()} at any time.
     * @see Run#run(File, File, File, String, InteractiveInterface)
     */
    public String run(boolean abortOnThreadInterrupt, File xml, File wd, File ini, String machine, InteractiveInterface interactive,
            Appendable outputTarget, int logDetailLevel, boolean acceptExceptions) throws SplRunError {
        return run(this, abortOnThreadInterrupt, xml, wd, ini, machine, interactive, outputTarget, logDetailLevel, acceptExceptions);
    }

    /**
     * Call this method to cancel execution represented by this instance.
     */
    public synchronized void cancelExecution() {
        this.cancelled = true;
        InvokedExecutionConfiguration.cancelExecution(executionID);
    }

    /**
     * Runs SPL framework.
     * 
     * @param execution
     *            The execution.
     * @param abortOnThreadInterrupt
     *            The value for {@link #setAbortOnInterrupt(boolean)}. True
     *            value indicates that execution should be aborted, if executing
     *            thread is interrupted.
     * @param xml
     *            SPL XML file. Required
     * @param wd
     *            SPL working directory. Required.
     * @param ini
     *            SPL INI configuration file. Not required.
     * @param machine
     *            Machine where to perform measuring. Not required, defaults to
     *            local machine.
     * @param interactive
     *            Object to interact with user. Not required, defaults to no
     *            interaction.
     * @param outputTarget
     *            The output target.
     * @param logDetailLevel
     *            The log detail level.
     * @param acceptExceptions
     *            <p>
     *            The accept exceptions flag, value {@code true} means, that
     *            exceptions will be passed to output, {@code false} that they
     *            won't be passed to output. Fatal error exceptions are always
     *            passed to output.
     * @return The string with path to evaluation result.
     * @throws SplRunError
     *             The SPL run error.
     * @see Run#run(File, File, File, String, InteractiveInterface)
     */
    private static synchronized String run(InvokedExecution execution, boolean abortOnThreadInterrupt, File xml, File wd, File ini, String machine,
            InteractiveInterface interactive,
            Appendable outputTarget, int logDetailLevel, boolean acceptExceptions)
            throws SplRunError {
        if (execution.startSession(abortOnThreadInterrupt)) {
            try {
                SplDynamicTargetLoggerFactory.addLogOutput(outputTarget, logDetailLevel, acceptExceptions);
                return Run.run(xml, wd, ini, machine, interactive);
            } finally {
                SplDynamicTargetLoggerFactory.removeLogOutput(outputTarget);
                execution.stopSession();
            }
        } else {
            throw new SplRunInterrupted("Execution cancelled before it has started.");
        }
    }

    /**
     * Start session if not cancelled already.
     * 
     * @param abortOnThreadInterrupt
     *            The abort on thread interrupt.
     * @return True, if successful.
     */
    private synchronized boolean startSession(boolean abortOnThreadInterrupt) {
        if (this.cancelled) {
            return false;
        } else {
            InvokedExecutionConfiguration.startExecution(executionID, abortOnThreadInterrupt);
            return true;
        }
    }

    /**
     * Stops session.
     */
    private synchronized void stopSession() {
        InvokedExecutionConfiguration.stopExecution(executionID);
    }
}
