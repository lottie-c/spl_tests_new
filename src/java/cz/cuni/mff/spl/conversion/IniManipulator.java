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
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cz.cuni.mff.spl.conversion.IniValueConvertor.InvalidValueException;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.parsers.ColorParser;
import cz.cuni.mff.spl.utils.parsers.GraphDefinitionParser;

/**
 * The INI manipulation functions for used data types.
 * 
 * @author Martin Lacina
 */
class IniManipulator {

    /** The logger. */
    static final SplLog logger = SplLogger.getLogger(IniManipulator.class);

    /**
     * Reads boolean value from INI.
     * 
     * Boolean value is represented as {@code '1'} for {@code true} and
     * {@code '0'} for {@code false}. When not set, than {@code null} is
     * returned.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The boolean value if option is set to valid value ('0','1')
     *         or {@code null} when option is not set or value is invalid.
     * 
     * @see #writeBoolean(Ini, String, String, boolean)
     */
    static boolean readBoolean(Ini ini, String sectionName, String optionName, boolean defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);

        if (loaded != null) {
            try {
                return IniValueConvertor.decodeBoolean(loaded);
            } catch (InvalidValueException e) {
                logger.error("Invalid boolean configuration value: [%s] %s=%s", sectionName, optionName,
                        loaded);
            }
        } else {
            logger.trace("Boolean configuration value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Writes boolean value to INI.
     * 
     * Valid boolean value is represented as {@code '1'} for {@code true} and
     * {@code '0'} for {@code false}.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param value
     *            The value to write.
     * 
     * @see #readBoolean(Ini, String, String)
     */
    static void writeBoolean(Ini ini, String sectionName, String optionName, boolean value) {
        String valueStr = IniValueConvertor.encodeBoolean(value);
        ini.put(sectionName, optionName, valueStr);
        logger.trace("Boolean configuration value set: [%s] %s=%s", sectionName, optionName, valueStr);
    }

    /**
     * Reads double value from INI.
     * 
     * Value is not valid when it cannot be parsed using
     * {@link Double#parseDouble(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The double value if option is set to valid value or {@code null}
     *         when option is not set or value is invalid.
     * 
     * @see #writeInteger(Ini, String, String, int)
     */
    static double readDouble(Ini ini, String sectionName, String optionName, double defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.decodeDouble(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid double configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        } else {
            logger.trace("Double configuration value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Writes double value to INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param value
     *            The value to write.
     * 
     * @see #readDouble(Ini, String, String)
     */
    static void writeDouble(Ini ini, String sectionName, String optionName, double value) {
        String valueStr = IniValueConvertor.encodeDouble(value);
        ini.put(sectionName, optionName, valueStr);
        logger.trace("Double configuration value set: [%s] %s=%s", sectionName, optionName, valueStr);
    }

    /**
     * Reads integer value from INI.
     * 
     * Value is not valid when it cannot be parsed using
     * {@link Integer#parseInt(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The integer value if option is set to valid value or {@code null}
     *         when option is not set or value is invalid.
     * 
     * @see #writeInteger(Ini, String, String, int)
     */
    static int readInteger(Ini ini, String sectionName, String optionName, int defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.decodeInteger(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid integer configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        } else {
            logger.trace("Integer configuration value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Writes integer value to INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param value
     *            The value to write.
     * 
     * @see #readInteger(Ini, String, String)
     */
    static void writeInteger(Ini ini, String sectionName, String optionName, int value) {
        String valueStr = IniValueConvertor.encodeInteger(value);
        ini.put(sectionName, optionName, valueStr);
        logger.trace("Integer configuration value set: [%s] %s=%s", sectionName, optionName, valueStr);
    }

    /**
     * Reads long value from INI.
     * 
     * Value is not valid when it cannot be parsed using
     * {@link Long#parseLong(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The long value if option is set to valid or {@code null} when
     *         option is not set or value is invalid.
     * 
     * @see #writeLong(Ini, String, String, long)
     */
    static long readLong(Ini ini, String sectionName, String optionName, long devaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.decodeLong(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid long configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        } else {
            logger.trace("Long configuration value not set: [%s] %s", sectionName, optionName);
        }
        return devaultValue;
    }

    /**
     * Writes long value to INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param value
     *            The value to write.
     * 
     * @see #readLong(Ini, String, String)
     */
    static void writeLong(Ini ini, String sectionName, String optionName, long value) {
        String valueStr = IniValueConvertor.encodeLong(value);
        ini.put(sectionName, optionName, valueStr);
        logger.trace("Integer configuration value set: [%s] %s=%s", sectionName, optionName, valueStr);
    }

    /**
     * Reads graph type value from INI.
     * <p>
     * Value is notvaluehen it cannot be mapped to instance using
     * {@link GraphDefinitionParser#ParseGraphDefinition(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The graph type if option is set to valid value or {@code null}
     *         when option is not set or value is invalid.
     * 
     * @see GraphDefinitionParser#ParseGraphDefinition(String)
     */
    static GraphDefinition readGraphType(Ini ini, String sectionName, String optionName, GraphDefinition defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.decodeGraphType(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid graph type configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        } else {
            logger.trace("Graph type configuration value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Reads list graph type of values from INI.
     * <p>
     * Value is not valid when it cannot be mapped to instance using
     * {@link GraphDefinitionParser#ParseGraphDefinition(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The graph type list if at least one option is set to valid value
     *         or {@code null} when option is not set or value is invalid.
     * @see GraphDefinitionParser#ParseGraphDefinition(String)
     */
    static Collection<GraphDefinition> readGraphTypes(Ini ini, String sectionName, String optionName) {
        Section s = ini.get(sectionName);

        SortedMap<Integer, GraphDefinition> result = new TreeMap<>();

        if (s != null) {
            Set<Entry<String, String>> loaded = s.entrySet();
            if (loaded != null) {

                for (Entry<String, String> loadedValue : loaded) {
                    String optionKey = loadedValue.getKey();
                    try {
                        if (optionKey.startsWith(optionName)) {
                            Integer order = new Integer(optionKey.substring(optionName.length()));
                            result.put(order, IniValueConvertor.decodeGraphType(loadedValue.getValue()));
                        } else {
                            logger.trace("Ignored value for graphs configuration: [%s] %s=%s", sectionName, optionKey, loadedValue.getValue());
                        }
                    } catch (NumberFormatException e) {
                        logger.error(e, "Invalid graph type configuration key name: [%s] %s", sectionName, optionKey);
                    } catch (InvalidValueException e) {
                        logger.error(e, "Invalid graph type configuration value: [%s] %s=%s", sectionName, optionKey, loadedValue.getValue());
                    }
                }
                return result.values();
            }
        }
        logger.trace("No graph type configuration values set: [%s] %s", sectionName, optionName);
        return null;
    }

    /**
     * Writes graph type value to INI using
     * {@link GraphDefinitionParser#createGraphDeclarationString(GraphDefinition)}
     * .
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param graphDefinition
     *            The graph definition to write.
     * 
     * @see GraphDefinitionParser#createGraphDeclarationString(GraphDefinition)
     */
    static void writeGraphType(Ini ini, String sectionName, String optionName, GraphDefinition graphDefinition) {
        String graphDefinitionString = GraphDefinitionParser.createGraphDeclarationString(graphDefinition);
        ini.put(sectionName, optionName, graphDefinitionString);
        logger.trace("Graph type configuration value set: [%s] %s=%s", sectionName, optionName, graphDefinitionString);
    }

    /**
     * Writes lit of graph type values to INI using
     * {@link GraphDefinitionParser#createGraphDeclarationString(GraphDefinition)}
     * .
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param graphDefinitions
     *            The graph definitions.
     * @see GraphDefinitionParser#createGraphDeclarationString(GraphDefinition)
     */
    static void writeGraphTypes(Ini ini, String sectionName, String optionName, List<GraphDefinition> graphDefinitions) {
        int itemIndex = 0;
        for (GraphDefinition definition : graphDefinitions) {
            writeGraphType(ini, sectionName, optionName + ++itemIndex, definition);
        }
    }

    /**
     * Reads color value from INI.
     * 
     * Value is not valid when it cannot be mapped to instance using
     * {@link ColorParser#ParseColorDefinition(String)}.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The color if option is set to valid value or {@code null} when
     *         option is not set or value is invalid.
     * 
     * @see ColorParser#ParseColorDefinition(String)
     */
    static Color readColor(Ini ini, String sectionName, String optionName, Color defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.decodeColor(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid color configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        } else {
            logger.trace("Color configuration value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Reads color values from INI.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The color type list if at least one option is set to valid value
     *         or {@code null} when option is not set or value is invalid.
     * 
     * @see ColorParser#ParseColorDefinition(String)
     */
    static Collection<Color> readColors(Ini ini, String sectionName, String optionName) {
        Section s = ini.get(sectionName);

        SortedMap<Integer, Color> result = new TreeMap<>();

        if (s != null) {
            Set<Entry<String, String>> loaded = s.entrySet();
            if (loaded != null) {

                for (Entry<String, String> loadedValue : loaded) {
                    String optionKey = loadedValue.getKey();
                    try {
                        if (optionKey.startsWith(optionName)) {
                            Integer order = new Integer(optionKey.substring(optionName.length()));
                            result.put(order, IniValueConvertor.decodeColor(loadedValue.getValue()));
                        } else {
                            logger.trace("Ignored value for colors configuration: [%s] %s=%s", sectionName, optionKey, loadedValue.getValue());
                        }
                    } catch (NumberFormatException e) {
                        logger.error(e, "Invalid color configuration key name: [%s] %s", sectionName, optionKey);
                    } catch (InvalidValueException e) {
                        logger.error(e, "Invalid color configuration value: [%s] %s=%s", sectionName, optionKey, loadedValue.getValue());
                    }
                }
                return result.values();
            }
        }
        logger.trace("No color configuration values set: [%s] %s", sectionName, optionName);
        return null;
    }

    /**
     * Writes color value to INI using
     * {@link ColorParser#createColorDeclarationString(Color)}.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param graphDefinition
     *            The graph definition to write.
     * 
     * @see GraphTypes
     * @see #readGraphType(Ini, String, String)
     */
    static void writeColor(Ini ini, String sectionName, String optionName, Color colorDefinition) {
        String graphDefinitionString = IniValueConvertor.encodeColor(colorDefinition);
        ini.put(sectionName, optionName, graphDefinitionString);
        logger.trace("Graph type configuration value set: [%s] %s=%s", sectionName, optionName, graphDefinitionString);
    }

    /**
     * Read string from INI.
     * 
     * @param ini
     *            The INI to read from.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @return The string value if option set or {@code null} when option is not
     *         set.
     */
    public static String readString(Ini ini, String sectionName, String optionName, String defaultValue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            return loaded;
        } else {
            logger.trace("String value not set: [%s] %s", sectionName, optionName);
        }
        return defaultValue;
    }

    /**
     * Writes string to INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param stringValue
     *            The string value.
     */
    public static void writeString(Ini ini, String sectionName, String optionName, String stringValue) {
        ini.put(sectionName, optionName, stringValue);
        logger.trace("String value set: [%s] %s=%s", sectionName, optionName, stringValue);
    }

    /**
     * Reads timeSource from INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param stringValue
     *            The string value.
     */
    public static String readTimeSource(Ini ini, String sectionName, String optionName, String defaultvalue) {
        String loaded = ini.fetch(sectionName, optionName);
        if (loaded != null) {
            try {
                return IniValueConvertor.validateTimeSource(loaded);
            } catch (InvalidValueException e) {
                logger.error(e, "Invalid timeSource configuration value: [%s] %s=%s", sectionName, optionName, loaded);
            }
        }
        return defaultvalue;
    }

    /**
     * Writes timeSource to INI.
     * 
     * @param ini
     *            The INI to write to.
     * @param sectionName
     *            The section name.
     * @param optionName
     *            The option name.
     * @param stringValue
     *            The timeSource value.
     */
    public static void writeTimeSource(Ini ini, String sectionName, String optionName, String stringValue) {
        ini.put(sectionName, optionName, stringValue);
        logger.trace("timeSource value set: [%s] %s=%s", sectionName, optionName, stringValue);
    }

}
