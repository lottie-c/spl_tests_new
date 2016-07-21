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
package cz.cuni.mff.spl.conversion;

import java.awt.Color;

import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.parsers.ColorParser;
import cz.cuni.mff.spl.utils.parsers.GraphDefinitionParser;
import cz.cuni.mff.spl.utils.parsers.ParseException;

/**
 * Class with implementation of string to type and type to string conversion for
 * values used in IniConversion.
 * <p>
 * There are methods to decode values - two for every type, one throwing
 * {@link InvalidValueException} on errors, and one returning default value -
 * and one for encoding value to string.
 * <p>
 * Implementation for types:
 * <ul>
 * <li>{@code boolean}
 * <li>{@code int}
 * <li>{@code long}
 * <li>{@code double}
 * <li>{@link GraphDefinition}
 * <li>{@link java.awt.Color}
 * </ul>
 * 
 * @author Martin Lacina
 * 
 */
public class IniValueConvertor {

    /** The logger. */
    static final SplLog logger = SplLogger.getLogger(IniManipulator.class);

    /**
     * Decodes boolean.
     * 
     * @param value
     *            The value.
     * @return True, if successful.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static boolean decodeBoolean(String value) throws InvalidValueException {
        if (value != null) {
            switch (value.toLowerCase()) {
                case "true":
                case "yes":
                case "1":
                    return true;
                case "false":
                case "no":
                case "0":
                    return false;
            }
        }
        throw new InvalidValueException(String.format("Boolean value [%s] is not one of true, yes, 1, false, no, 0.", value));
    }

    /**
     * Decodes boolean.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return True, if successful.
     */
    public static boolean decodeBoolean(String value, boolean defaultValue) {
        if (value != null) {
            switch (value.toLowerCase()) {
                case "true":
                case "yes":
                case "1":
                    return true;
                case "false":
                case "no":
                case "0":
                    return false;
                default:
                    return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Encodes boolean.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeBoolean(boolean value) {
        return value ? "1" : "0";
    }

    /**
     * Decodes integer.
     * 
     * @param value
     *            The value.
     * @return The int.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static int decodeInteger(String value) throws InvalidValueException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String message = String.format("Invalid integer value: [%s]", value);
            logger.error(e, message);
            throw new InvalidValueException(message, e);
        }
    }

    /**
     * Decodes integer.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return The int.
     */
    public static int decodeInteger(String value, int defaultValue) {
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.error(e, "Invalid integer value: [%s]", value);
            }
        }
        return defaultValue;
    }

    /**
     * Encodes integer.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeInteger(int value) {
        return Integer.toString(value);
    }

    /**
     * Decodes long.
     * 
     * @param value
     *            The value.
     * @return The long.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static long decodeLong(String value) throws InvalidValueException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            String message = String.format("Invalid long value: [%s]", value);
            logger.error(e, message);
            throw new InvalidValueException(message, e);
        }
    }

    /**
     * Decodes long.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return The long.
     */
    public static long decodeLong(String value, long defaultValue) {
        if (value != null) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.error(e, "Invalid long value: [%s]", value);
            }
        }
        return defaultValue;
    }

    /**
     * Encodes long.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeLong(long value) {
        return Long.toString(value);
    }

    /**
     * Decodes double.
     * 
     * @param value
     *            The value.
     * @return The double.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static double decodeDouble(String value) throws InvalidValueException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            String message = String.format("Invalid double value: [%s]", value);
            logger.error(e, message);
            throw new InvalidValueException(message, e);
        }
    }

    /**
     * Decodes double.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return The double.
     */
    public static double decodeDouble(String value, double defaultValue) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                logger.error(e, "Invalid double value: [%s]", value);
            }
        }
        return defaultValue;
    }

    /**
     * Encodes double.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeDouble(double value) {
        return Double.toString(value);
    }

    /**
     * Decodes graph type.
     * 
     * @param value
     *            The value.
     * @return The graph definition.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static GraphDefinition decodeGraphType(String value) throws InvalidValueException {
        try {
            return GraphDefinitionParser.ParseGraphDefinition(value);
        } catch (ParseException e) {
            String message = String.format("Invalid graph type value: [%s]", value);
            logger.error(e, message);
            throw new InvalidValueException(message, e);
        }
    }

    /**
     * Decodes graph type.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return The graph definition.
     */
    public static GraphDefinition decodeGraphType(String value, GraphDefinition defaultValue) {
        if (value != null) {
            try {
                return GraphDefinitionParser.ParseGraphDefinition(value);
            } catch (ParseException e) {
                logger.error(e, "Invalid graph type value: [%s]", value);
            }
        }
        return defaultValue;
    }

    /**
     * Encodes graph type.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeGraphType(GraphDefinition value) {
        return GraphDefinitionParser.createGraphDeclarationString(value);
    }

    /**
     * Decodes color.
     * 
     * @param value
     *            The value.
     * @return The color.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static Color decodeColor(String value) throws InvalidValueException {
        try {
            return ColorParser.ParseColorDefinition(value);
        } catch (ParseException e) {
            String message = String.format("Invalid graph type value: [%s]", value);
            logger.error(e, message);
            throw new InvalidValueException(message, e);
        }
    }

    /**
     * Decodes color.
     * 
     * @param value
     *            The value.
     * @param defaultValue
     *            The default value.
     * @return The color.
     */
    public static Color decodeColor(String value, Color defaultValue) {
        if (value != null) {
            try {
                return ColorParser.ParseColorDefinition(value);
            } catch (ParseException e) {
                logger.error(e, "Invalid color value: [%s]", value);
            }
        }
        return defaultValue;
    }

    /**
     * Encodes color.
     * 
     * @param value
     *            The value.
     * @return The string.
     */
    public static String encodeColor(Color value) {
        return ColorParser.createColorDeclarationString(value);
    }

    /**
     * Validates timeSource.
     * 
     * @param value
     *            The value.
     * @return The validated timeSource.
     * @throws InvalidValueException
     *             The invalid value exception.
     */
    public static String validateTimeSource(String value) throws InvalidValueException {
        if (value.equals("nanotime") || value.equals("threadtime")) {
            return value;
        } else {
            String message = String.format("Invalid timeSource value: [%s]", value);
            throw new InvalidValueException(message);
        }
    }

    /**
     * The exception to signal that value is not valid.
     */
    public static class InvalidValueException extends Exception {

        /** Serialization ID. */
        private static final long serialVersionUID = 418475919624943988L;

        /**
         * Instantiates a new invalid value exception.
         * 
         * @param message
         *            The message.
         */
        public InvalidValueException(String message) {
            super(message);
        }

        /**
         * Instantiates a new invalid value exception.
         * 
         * @param cause
         *            The cause.
         */
        public InvalidValueException(Throwable cause) {
            super(cause);
        }

        /**
         * Instantiates a new invalid value exception.
         * 
         * @param message
         *            The message.
         * @param cause
         *            The cause.
         */
        public InvalidValueException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
