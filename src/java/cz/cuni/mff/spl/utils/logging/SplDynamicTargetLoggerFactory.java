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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A factory class for creating SplOutputStreamLogger objects.
 * <p>
 * Log output targets (files, streams, ...) can be added dynamically to items
 * produced with this factory.
 * <p>
 * When new output target is added, it is stored in internal list of output
 * targets and all created output loggers (even those created in past) will use
 * this target.
 * <p>
 * Only method {@link Appendable#append(CharSequence)} will be used for passing
 * data and it will be called with entire log message as parameter of type
 * {@link java.lang.String}.
 * <p>
 * Every method in output target can return {@code null} value as output is not
 * used in any way.
 * 
 * @author Martin Lacina
 */
public class SplDynamicTargetLoggerFactory {

    /** The Constant outputWriters. */
    private static final List<OutputWriter> outputTargets     = new LinkedList<>();

    /** The max log detail level covered by added log outputs. */
    private static int                      maxLogDetailLevel = 0;

    /**
     * Configures SPL logging utility to use provided stream for all log output.
     * <p>
     * Only method {@link Appendable#append(CharSequence)} will be used for
     * passing data and it will be called with entire log message as parameter
     * of type {@link java.lang.String}.
     * <p>
     * Every method in output target can return {@code null} value as output is
     * not used in any way.
     * 
     * @param outputTarget
     *            The output target.
     * @param logDetailLevel
     *            The log detail level.
     */
    public static synchronized void addLogOutput(Appendable outputTarget, int logDetailLevel) {
        addLogOutput(outputTarget, logDetailLevel, true);
    };

    /**
     * Configures SPL logging utility to use provided stream for all log output.
     * <p>
     * Only method {@link Appendable#append(CharSequence)} will be used for
     * passing data and it will be called with entire log message as parameter
     * of type {@link java.lang.String}.
     * <p>
     * Every method in output target can return {@code null} value as output is
     * not used in any way.
     * 
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
     */
    public static synchronized void addLogOutput(Appendable outputTarget, int logDetailLevel, boolean acceptExceptions) {
        if (outputTarget != null) {
            outputTargets.add(new OutputWriter(outputTarget, logDetailLevel, acceptExceptions));
            maxLogDetailLevel = Math.max(maxLogDetailLevel, logDetailLevel);
        }
    };

    /**
     * Configures SPL logging utility to use provided stream for all log output.
     * <p>
     * Only method {@link Appendable#append(CharSequence)} will be used for
     * passing data and it will be called with entire log message as parameter
     * of type {@link java.lang.String}.
     * <p>
     * Every method in output target can return {@code null} value as output is
     * not used in any way.
     * 
     * @param outputWriter
     *            The output writer.
     * @param logDetailLevel
     *            The log detail level.
     */
    public static synchronized void addLogOutput(Appendable outputWriter, LEVELS logDetailLevel) {
        addLogOutput(outputWriter, logDetailLevel.ordinal(), true);
    }

    /**
     * Configures SPL logging utility to use provided stream for all log output.
     * <p>
     * Only method {@link Appendable#append(CharSequence)} will be used for
     * passing data and it will be called with entire log message as parameter
     * of type {@link java.lang.String}.
     * <p>
     * Every method in output target can return {@code null} value as output is
     * not used in any way.
     * 
     * @param outputWriter
     *            The output writer.
     * @param logDetailLevel
     *            The log detail level.
     * @param acceptExceptions
     *            <p>
     *            The accept exceptions flag, value {@code true} means, that
     *            exceptions will be passed to output, {@code false} that they
     *            won't be passed to output. Fatal error exceptions are always
     *            passed to output.
     */
    public static synchronized void addLogOutput(Appendable outputWriter, LEVELS logDetailLevel, boolean acceptExceptions) {
        addLogOutput(outputWriter, logDetailLevel.ordinal(), acceptExceptions);
    }

    /**
     * Removes the log output.
     * 
     * @param outputWriter
     *            The output writer.
     */
    public static synchronized void removeLogOutput(Appendable outputWriter) {

        if (outputWriter != null) {
            Iterator<OutputWriter> iterator = outputTargets.iterator();
            int newMaxlLogDetailLevel = LEVELS.FATAL.ordinal();

            while (iterator.hasNext()) {
                OutputWriter writer = iterator.next();
                if (writer.output == outputWriter) {
                    iterator.remove();
                } else {
                    newMaxlLogDetailLevel = Math.max(newMaxlLogDetailLevel, writer.logDetailLevel);
                }
            }
            maxLogDetailLevel = newMaxlLogDetailLevel;
        }
    }

    /**
     * The supported log detail levels.
     */
    public enum LEVELS {

        /** The fatal. */
        FATAL,
        /** The error. */
        ERROR,
        /** The warn. */
        WARN,
        /** The info. */
        INFO,
        /** The debug. */
        DEBUG,
        /** The trace. */
        TRACE;

        /** The default log level. */
        public static final LEVELS DEFAULT_LOG_LEVEL = LEVELS.INFO;
    };

    /**
     * Gets a logger instance by class.
     * 
     * @param clazz
     *            class
     * @return logger
     */
    public static SplLog getLogger(Class<?> clazz) {
        return new SplDynamicTargetLogger(clazz.getName());
    }

    /**
     * Gets a logger instance by name.
     * 
     * @param name
     *            name
     * @return logger
     */
    public static SplLog getLogger(String name) {
        return new SplDynamicTargetLogger(name);
    }

    /**
     * Checks if is fatal enabled.
     * 
     * @return True, if is fatal enabled.
     */
    public static boolean isFatalEnabled() {
        return maxLogDetailLevel >= LEVELS.FATAL.ordinal();
    }

    /**
     * Checks if is error enabled.
     * 
     * @return True, if is error enabled.
     */
    public static boolean isErrorEnabled() {
        return maxLogDetailLevel >= LEVELS.ERROR.ordinal();
    }

    /**
     * Checks if is warn enabled.
     * 
     * @return True, if is warn enabled.
     */
    public static boolean isWarnEnabled() {
        return maxLogDetailLevel >= LEVELS.WARN.ordinal();
    }

    /**
     * Checks if is info enabled.
     * 
     * @return True, if is info enabled.
     */
    public static boolean isInfoEnabled() {
        return maxLogDetailLevel >= LEVELS.INFO.ordinal();
    }

    /**
     * Checks if is debug enabled.
     * 
     * @return True, if is debug enabled.
     */
    public static boolean isDebugEnabled() {
        return maxLogDetailLevel >= LEVELS.DEBUG.ordinal();
    }

    /**
     * Checks if is trace enabled.
     * 
     * @return True, if is trace enabled.
     */
    public static boolean isTraceEnabled() {
        return maxLogDetailLevel >= LEVELS.TRACE.ordinal();
    }

    /**
     * Checks if is enabled.
     * 
     * @param level
     *            The level.
     * @return True, if is enabled.
     */
    public static boolean isEnabled(LEVELS level) {
        return maxLogDetailLevel >= level.ordinal();
    }

    /** The Constant dateFormat. */
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss,SSS");

    /**
     * Writes message to all output targets.
     * 
     * @param name
     *            The name.
     * @param level
     *            The level.
     * @param t
     *            The t.
     * @param format
     *            The format.
     * @param args
     *            The arguments.
     */
    static synchronized void write(String name, LEVELS level, Throwable t, String format, Object[] args) {
        for (OutputWriter writer : outputTargets) {
            writer.writeToLog(name, level, t, format, args);
        }
    }

    /**
     * The Class OutputWriter.
     */
    private static class OutputWriter {

        /** The log detail level. */
        private final int        logDetailLevel;

        /** The accept exceptions flag. */
        private final boolean    acceptExceptions;

        /** The output. */
        private final Appendable output;

        /**
         * Instantiates a new output writer.
         * 
         * @param outputWriter
         *            The output writer.
         * @param logDetailLevel
         *            The log detail level.
         * @param acceptExceptions
         *            <p>
         *            The accept exceptions flag, value {@code true} means, that
         *            exceptions will be passed to output, {@code false} that
         *            they won't be passed to output. Fatal error exceptions are
         *            always passed to output.
         */
        public OutputWriter(Appendable outputWriter, int logDetailLevel, boolean acceptExceptions) {
            this.logDetailLevel = logDetailLevel;
            this.output = outputWriter;
            this.acceptExceptions = acceptExceptions;
        }

        /**
         * Checks if log detail level is enabled.
         * 
         * @param level
         *            The log detail level.
         * @return True, if log detail level is enabled.
         */
        public boolean isLogLevelEnabled(LEVELS level) {
            return logDetailLevel >= level.ordinal();
        }

        /**
         * Writes log message to log output.
         * <p>
         * Only method @link {@link Appendable#append(CharSequence)} will be
         * used and it will be called with entire log message.
         * 
         * @param name
         *            The name.
         * @param level
         *            The level.
         * @param t
         *            The exception which caused this.
         * @param format
         *            The string format.
         * @param args
         *            The string format arguments.
         */
        void writeToLog(String name, LEVELS level, Throwable t, String format, Object[] args) {
            if (isLogLevelEnabled(level)) {
                StringWriter buffer = new StringWriter();
                PrintWriter writer = new PrintWriter(buffer);
                writer.append(dateFormat.format(Calendar.getInstance().getTime()));
                writer.append(" ");
                writer.append(level.name());
                writer.append(" ");
                writer.append(name);
                writer.append(" - ");
                writer.append(String.format(format, args));
                if (t != null) {
                    if (acceptExceptions == true || level.equals(LEVELS.FATAL)) {
                        writer.append('\n');
                        t.printStackTrace(writer);
                    }
                }
                String fullMessage = buffer.getBuffer().toString();
                try {
                    output.append(fullMessage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
