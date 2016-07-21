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

/**
 * Simple log output agregator. Just passes every request to all loggers.
 * 
 * @author Martin Lacina
 * 
 */
public class MultiOutputLogger implements SplLog {

    /** The loggers. */
    private final SplLog[] loggers;

    /**
     * Instantiates a new multi output logger.
     * 
     * @param loggers
     *            The loggers.
     */
    public MultiOutputLogger(SplLog... loggers) {
        if (loggers == null) {
            loggers = new SplLog[0];
        }
        this.loggers = loggers;
    }

    @Override
    public void fatal(String format, Object... args) {
        for (SplLog log : loggers) {
            log.fatal(format, args);
        }
    }

    @Override
    public void fatal(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.fatal(t, format, args);
        }

    }

    @Override
    public void error(String format, Object... args) {
        for (SplLog log : loggers) {
            log.error(format, args);
        }

    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.error(t, format, args);
        }

    }

    @Override
    public void warn(String format, Object... args) {
        for (SplLog log : loggers) {
            log.warn(format, args);
        }

    }

    @Override
    public void warn(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.warn(t, format, args);
        }

    }

    @Override
    public void info(String format, Object... args) {
        for (SplLog log : loggers) {
            log.info(format, args);
        }

    }

    @Override
    public void info(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.info(t, format, args);
        }

    }

    @Override
    public void debug(String format, Object... args) {
        for (SplLog log : loggers) {
            log.debug(format, args);
        }

    }

    @Override
    public void debug(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.debug(t, format, args);
        }

    }

    @Override
    public void trace(String format, Object... args) {
        for (SplLog log : loggers) {
            log.trace(format, args);
        }

    }

    @Override
    public void trace(Throwable t, String format, Object... args) {
        for (SplLog log : loggers) {
            log.trace(t, format, args);
        }

    }

    @Override
    public boolean isFatalEnabled() {
        for (SplLog log : loggers) {
            if (log.isFatalEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isErrorEnabled() {
        for (SplLog log : loggers) {
            if (log.isErrorEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        for (SplLog log : loggers) {
            if (log.isWarnEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInfoEnabled() {
        for (SplLog log : loggers) {
            if (log.isInfoEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDebugEnabled() {
        for (SplLog log : loggers) {
            if (log.isDebugEnabled()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isTraceEnabled() {
        for (SplLog log : loggers) {
            if (log.isTraceEnabled()) {
                return true;
            }
        }
        return false;
    }
}
