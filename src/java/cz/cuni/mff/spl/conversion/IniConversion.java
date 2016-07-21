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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ini4j.Ini;

import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.configuration.SplAccessConfiguration;
import cz.cuni.mff.spl.configuration.SplDeploymentConfiguration;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;

/**
 * The specialized INI format conversion for specified classes.
 * 
 * Currently supports:
 * <ul>
 * <li>{@link SplEvaluatorConfiguration}</li>
 * </ul>
 * 
 * @author Martin Lacina
 * 
 * @see SplEvaluatorConfiguration
 * @see SplAccessConfiguration
 * @see SplDeploymentConfiguration
 */
public class IniConversion {

    /**
     * Saves SPL evaluator configuration to INI file.
     * 
     * @param config
     *            The SPL evaluator configuration.
     * @param outputStream
     *            The output stream.
     * @return True, if successful.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplEvaluatorConfiguration
     */
    public static Ini saveSplEvaluatorConfiguration(SplEvaluatorConfiguration config, OutputStream outputStream) throws ConversionException {
        Ini result = new Ini();
        IniConversionSplEvaluatorConfiguration.saveSplEvaluatorConfiguration(config, result);
        try {
            result.store(outputStream);
        } catch (IOException e) {
            throw new ConversionException(e);
        }
        return result;
    }

    /**
     * Saves SPL evaluator configuration to INI file.
     * 
     * @param config
     *            The SPL evaluator configuration.
     * @param output
     *            The INI instance to write data to.
     * @return Modified INI instance.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplEvaluatorConfiguration
     */
    public static Ini saveSplEvaluatorConfiguration(SplEvaluatorConfiguration config, Ini output) throws ConversionException {
        IniConversionSplEvaluatorConfiguration.saveSplEvaluatorConfiguration(config, output);
        return output;
    }

    /**
     * Load SPL evaluator configuration from INI provided as stream.
     * 
     * @param input
     *            The input.
     * @return The SPL evaluator configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplEvaluatorConfiguration
     */
    public static SplEvaluatorConfiguration loadSplEvaluatorConfiguration(InputStream input) throws ConversionException {
        try {
            return IniConversionSplEvaluatorConfiguration.loadSplEvaluatorConfiguration(new Ini(input));
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Loads SPL evaluator configuration from INI instance.
     * 
     * @param input
     *            The input.
     * @return The SPL evaluator configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplEvaluatorConfiguration
     */
    public static SplEvaluatorConfiguration loadSplEvaluatorConfiguration(Ini input) throws ConversionException {
        return IniConversionSplEvaluatorConfiguration.loadSplEvaluatorConfiguration(input);
    }

    /**
     * Load SPL access configuration from INI provided as stream.
     * 
     * @param input
     *            The input.
     * @return The SPL access configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplAccessConfiguration
     */
    public static SplAccessConfiguration loadSplAccessConfiguration(InputStream input) throws ConversionException {
        try {
            return IniConversionSplAccessConfiguration.loadSplAccessConfiguration(new Ini(input));
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Loads SPL access configuration from INI instance.
     * 
     * @param input
     *            The input.
     * @return The SPL access configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplAccessConfiguration
     */
    public static SplAccessConfiguration loadSplAccessConfiguration(Ini input) throws ConversionException {
        return IniConversionSplAccessConfiguration.loadSplAccessConfiguration(input);
    }

    /**
     * Saves SPL measurement configuration to INI file.
     * 
     * @param config
     *            The SPL measurement configuration.
     * @param outputStream
     *            The output stream.
     * @return True, if successful.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplDeploymentConfiguration
     */
    public static Ini saveSplMeasurementConfiguration(SplDeploymentConfiguration config, OutputStream outputStream) throws ConversionException {
        Ini result = new Ini();
        IniConversionSplDeploymentConfiguration.saveSplDeploymentConfiguration(config, result);
        try {
            result.store(outputStream);
        } catch (IOException e) {
            throw new ConversionException(e);
        }
        return result;
    }

    /**
     * Saves SPL measurement configuration to INI file.
     * 
     * @param config
     *            The SPL measurement configuration.
     * @param output
     *            The INI instance to write data to.
     * @return Modified INI instance.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplDeploymentConfiguration
     */
    public static Ini saveSplMeasurementConfiguration(SplDeploymentConfiguration config, Ini output) throws ConversionException {
        IniConversionSplDeploymentConfiguration.saveSplDeploymentConfiguration(config, output);
        return output;
    }

    /**
     * Load SPL measurement configuration from INI provided as stream.
     * 
     * @param input
     *            The input.
     * @return The SPL measurement configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplDeploymentConfiguration
     */
    public static SplDeploymentConfiguration loadSplMeasurementConfiguration(InputStream input) throws ConversionException {
        try {
            return IniConversionSplDeploymentConfiguration.loadSplDeploymentConfiguration(new Ini(input));
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Loads SPL measurement configuration from INI instance.
     * 
     * @param input
     *            The input.
     * @return The SPL measurement configuration.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplDeploymentConfiguration
     */
    public static SplDeploymentConfiguration loadSplMeasurementConfiguration(Ini input) throws ConversionException {
        return IniConversionSplDeploymentConfiguration.loadSplDeploymentConfiguration(input);
    }

    /**
     * Creates default SPL evaluator configuration to INI file.
     * 
     * @param config
     *            The SPL evaluator configuration.
     * @param output
     *            The INI instance to write data to.
     * @return Modified INI instance.
     * @throws ConversionException
     *             The conversion exception.
     * 
     * @see SplEvaluatorConfiguration
     */
    public static Ini defaultConfigurationBundle() throws ConversionException {
        ConfigurationBundle bundle = ConfigurationBundle.createDefault();
        Ini ini = new Ini();
        // no machine entries in default configuration
        saveSplMeasurementConfiguration(bundle.getDeploymentConfig(), ini);
        IniConversionSplEvaluatorConfiguration.saveSplEvaluatorConfiguration(bundle.getEvaluatorConfig(), ini);
        return ini;
    }
}
