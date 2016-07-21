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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Standard Logger implementation of SPL Logger.
 * 
 * @author Jiri Daniel
 */
public class SplStdLogger implements SplLog {

    private static Logger logger = Logger.getLogger("SPL");

    private final String  className;

    protected SplStdLogger(Class<?> clazz) {
        className = clazz.getName();
    }

    protected SplStdLogger(String name)
    {
        className = name;
    }

    @Override
    public void fatal(String format, Object... args) {
        if (isFatalEnabled()) {
            logger.logp(Level.SEVERE, className, "", String.format(format, args));
        }
    }

    @Override
    public void fatal(Throwable t, String format, Object... args) {
        if (isFatalEnabled()) {
            logger.logp(Level.SEVERE, className, "", String.format(format, args), t);
        }
    }

    @Override
    public void error(String format, Object... args) {
        if (isErrorEnabled()) {
            logger.logp(Level.SEVERE, className, "", String.format(format, args));
        }
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        if (isErrorEnabled()) {
            logger.logp(Level.SEVERE, className, "", String.format(format, args), t);
        }
    }

    @Override
    public void warn(String format, Object... args) {
        if (isWarnEnabled()) {
            logger.logp(Level.WARNING, className, "", String.format(format, args));
        }
    }

    @Override
    public void warn(Throwable t, String format, Object... args) {
        if (isWarnEnabled()) {
            logger.logp(Level.WARNING, className, "", String.format(format, args), t);
        }
    }

    @Override
    public void info(String format, Object... args) {
        if (isInfoEnabled()) {
            logger.logp(Level.INFO, className, "", String.format(format, args));
        }
    }

    @Override
    public void info(Throwable t, String format, Object... args) {
        if (isInfoEnabled()) {
            logger.logp(Level.INFO, className, "", String.format(format, args), t);
        }
    }

    @Override
    public void debug(String format, Object... args) {
        if (isDebugEnabled()) {
            logger.logp(Level.FINE, className, "", String.format(format, args));
        }
    }

    @Override
    public void debug(Throwable t, String format, Object... args) {
        if (isDebugEnabled()) {
            logger.logp(Level.FINE, className, "", String.format(format, args), t);
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (isTraceEnabled()) {
            logger.logp(Level.FINEST, className, "", String.format(format, args));
        }
    }

    @Override
    public void trace(Throwable t, String format, Object... args) {
        if (isTraceEnabled()) {
            logger.logp(Level.FINEST, className, "", String.format(format, args), t);
        }
    }

    @Override
    public boolean isFatalEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }
}
