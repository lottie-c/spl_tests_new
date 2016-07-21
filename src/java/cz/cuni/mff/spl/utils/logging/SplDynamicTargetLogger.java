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
package cz.cuni.mff.spl.utils.logging;

import cz.cuni.mff.spl.utils.logging.SplDynamicTargetLoggerFactory.LEVELS;

/**
 * Logger implementation which uses output stream only.
 * <p>
 * Log detail level can be set
 * 
 * @author Martin Lacina
 * 
 */
public class SplDynamicTargetLogger implements SplLog {

    /** The name of logger. */
    private final String loggerName;

    /**
     * <p>
     * Instantiates a new SPL output stream logger which will pass all
     * exceptions to output.
     * 
     * @param name
     *            The name of logger.
     */
    public SplDynamicTargetLogger(String name) {
        this.loggerName = name;
    }

    @Override
    public void fatal(String format, Object... args) {
        write(LEVELS.FATAL, format, args);
    }

    @Override
    public void fatal(Throwable t, String format, Object... args) {
        write(LEVELS.FATAL, t, format, args);
    }

    @Override
    public void error(String format, Object... args) {
        write(LEVELS.ERROR, format, args);
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        write(LEVELS.ERROR, t, format, args);
    }

    @Override
    public void warn(String format, Object... args) {
        write(LEVELS.WARN, format, args);
    }

    @Override
    public void warn(Throwable t, String format, Object... args) {
        write(LEVELS.WARN, t, format, args);
    }

    @Override
    public void info(String format, Object... args) {
        write(LEVELS.INFO, format, args);
    }

    @Override
    public void info(Throwable t, String format, Object... args) {
        write(LEVELS.INFO, t, format, args);
    }

    @Override
    public void debug(String format, Object... args) {
        write(LEVELS.DEBUG, format, args);
    }

    @Override
    public void debug(Throwable t, String format, Object... args) {
        write(LEVELS.DEBUG, t, format, args);
    }

    @Override
    public void trace(String format, Object... args) {
        write(LEVELS.TRACE, format, args);
    }

    @Override
    public void trace(Throwable t, String format, Object... args) {
        write(LEVELS.TRACE, t, format, args);
    }

    @Override
    public boolean isFatalEnabled() {
        return SplDynamicTargetLoggerFactory.isFatalEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return SplDynamicTargetLoggerFactory.isErrorEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return SplDynamicTargetLoggerFactory.isWarnEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return SplDynamicTargetLoggerFactory.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return SplDynamicTargetLoggerFactory.isDebugEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return SplDynamicTargetLoggerFactory.isTraceEnabled();
    }

    /**
     * Writes message to output.
     * 
     * @param level
     *            The level.
     * @param format
     *            The format.
     * @param args
     *            The arguments.
     */
    private void write(LEVELS level, String format, Object... args) {
        write(level, null, format, args);
    }

    /**
     * Writes message to output.
     * 
     * @param level
     *            The level.
     * @param format
     *            The format.
     * @param args
     *            The arguments.
     */
    private void write(LEVELS level, Throwable t, String format, Object[] args) {
        SplDynamicTargetLoggerFactory.write(loggerName, level, t, format, args);
    }
}
