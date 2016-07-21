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
package cz.cuni.mff.spl.configuration;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cz.cuni.mff.spl.conversion.IniConversionSplDeploymentConfiguration;

/**
 * <p>
 * Configuration of various deployment settings. Project building, code
 * generation, code execution or store management may be configured.
 * 
 * @author Frantisek Haas
 * 
 */
public class SplDeploymentConfiguration {

    /**
     * @return
     *         <p>
     *         Lists factories for dynamic configuration editor and value
     *         validation.
     */
    public static List<ISectionFactory> getSectionFactories() {
        LinkedList<ISectionFactory> list = new LinkedList<>();
        list.add(new SplDeploymentSectionFactory());
        return list;
    }

    /**
     * @return<p>
     *            Instantiates a New default deployment configuration.
     */
    public static SplDeploymentConfiguration createDefaultConfiguration() {
        return new SplDeploymentConfiguration();
    }

    /** Whether to use system shell for build commands. */
    private boolean              useSystemShell;
    private static final boolean defaultUseSystemShell    = true;
    /** Whether to clear temporary directory before any real work. */
    private boolean              clearTmpBefore;
    private static final boolean defaultClearTmpBefore    = true;
    /** Whether to clear temporary directory after all work. */
    private boolean              clearTmpAfter;
    private static final boolean defaultClearTmpAfter     = false;
    /** Path to Java binary on execution machine. */
    private String               javaPath;
    private static final String  defaultJavaPath          = "java";
    /** JVM arguments passed to sampler. */
    private String               samplerArguments;
    private static final String  defaultSamplerArguments  = "";
    /** How many warmup cycles should be maximally performed. */
    private int                  warmupCycles;
    private static final int     defaultWarmupCycles      = 1000;
    /** How much time at max should be spend warming up in seconds. */
    private int                  warmupTime;
    private static final int     defaultWarmupTime        = 5;
    /** How many measurement cycles should be maximally performed. */
    private int                  measurementCycles;
    private static final int     defaultMeasurementCycles = 2000;
    /** How much time at max should be spend measuring in seconds. */
    private int                  measurementTime;
    private static final int     defaultMeasurementTime   = 10;
    /** How much time can sampler take running at max in seconds. */
    private int                  timeout;
    private static final int     defaultTimeout           = 300;
    /** What time source should be used */
    private String                  timeSource;
    private static final String     defaultTimeSource        = "threadtime";

    /**
     * <p>
     * Instantiates a New default deployment configuration.
     */
    public SplDeploymentConfiguration() {
        this(defaultUseSystemShell,
                defaultClearTmpBefore, defaultClearTmpAfter,
                defaultJavaPath, defaultSamplerArguments,
                defaultWarmupCycles, defaultWarmupTime,
                defaultMeasurementCycles, defaultMeasurementTime,
                defaultTimeout, defaultTimeSource);
    }

    /**
     * <p>
     * Instantiates a New deployment configuration with values specified.
     * 
     * @param useSystemShell
     *            Whether to use system shell for build commands.
     * @param clearTmpBefore
     *            Whether to clear temporary directory before any real work.
     * @param clearTmpAfter
     *            Whether to clear temporary directory after all work.
     * @param javaPath
     *            Path to Java binary on execution machine.
     * @param samplerArguments
     *            JVM arguments passed to sampler.
     * @param warmupCycles
     *            How many warmup cycles should be maximally performed.
     * @param warmupTime
     *            How much time at max should be spend warming up in seconds.
     * @param measurementCycles
     *            How many measurement cycles should be maximally performed.
     * @param measurementTime
     *            How much time at max should be spend measuring in seconds.
     * @param timeout
     *            How much time can sampler take running at max in seconds.
     * @param timeSource
     *            What time source should be used.
     */
    public SplDeploymentConfiguration(
            boolean useSystemShell, boolean clearTmpBefore, boolean clearTmpAfter, String javaPath, String samplerArguments, int warmupCycles, int warmupTime,
            int measurementCycles, int measurementTime, int timeout, String timeSource) {
        this.useSystemShell = useSystemShell;
        this.clearTmpBefore = clearTmpBefore;
        this.clearTmpAfter = clearTmpAfter;
        this.setJavaPath(javaPath);
        this.setSamplerArguments(samplerArguments);
        this.warmupCycles = warmupCycles;
        this.warmupTime = warmupTime;
        this.measurementCycles = measurementCycles;
        this.measurementTime = measurementTime;
        this.timeout = timeout;
        this.timeSource = timeSource;
    }

    /**
     * @return
     *         Whether to use system shell for build commands
     */
    public boolean getUseSystemShell() {
        return useSystemShell;
    }

    /**
     * @param useSystemShell
     *            Whether to use system shell for build commands
     */
    public void setUseSystemShell(boolean useSystemShell) {
        this.useSystemShell = useSystemShell;
    }

    /**
     * @return
     *         Whether to clear temporary directory before any real work.
     */
    public boolean getClearTmpBefore() {
        return clearTmpBefore;
    }

    /**
     * @param clearTmpBefore
     *            Whether to clear temporary directory before any real work.
     */
    public void setClearTmpBefore(boolean clearTmpBefore) {
        this.clearTmpBefore = clearTmpBefore;
    }

    /**
     * @return
     *         Whether to clear temporary directory after all work.
     */
    public boolean getClearTmpAfter() {
        return clearTmpAfter;
    }

    /**
     * @param clearTmpAfter
     *            Whether to clear temporary directory after all work.
     */
    public void setClearTmpAfter(boolean clearTmpAfter) {
        this.clearTmpAfter = clearTmpAfter;
    }

    /**
     * @return
     *         Path to Java binary on execution machine.
     */
    public String getJavaPath() {
        return javaPath;
    }

    /**
     * @param javaPath
     *            Path to Java binary on execution machine.
     */
    public void setJavaPath(String javaPath) {
        this.javaPath = javaPath.trim();
    }

    /**
     * @return
     *         JVM arguments passed to sampler.
     */
    public String getSamplerArguments() {
        return samplerArguments;
    }

    /**
     * @param samplerArguments
     *            JVM arguments passed to sampler.
     */
    public void setSamplerArguments(String samplerArguments) {
        this.samplerArguments = samplerArguments.trim();
    }

    /**
     * @return
     *         What time source should be used.
     */
    public String getTimeSource() {
        return timeSource;
    }

    /**
     * @param timeSource
     *            What time source should be used.
     */
    public void setTimeSource(String timeSource) {

        this.timeSource = timeSource;
    }

    /**
     * @return
     *         How many warmup cycles should be maximally performed.
     */
    public int getWarmupCycles() {
        return warmupCycles;
    }

    /**
     * @param warmupCycles
     *            How many warmup cycles should be maximally performed.
     */
    public void setWarmupCycles(int warmupCycles) {
        this.warmupCycles = warmupCycles;
    }

    /**
     * @return
     *         How much time at max should be spend warming up in seconds.
     */
    public int getWarmupTime() {
        return warmupTime;
    }

    /**
     * @param warmupTime
     *            How much time at max should be spend warming up in seconds.
     */
    public void setWarmupTime(int warmupTime) {
        this.warmupTime = warmupTime;
    }

    /**
     * @return
     *         How many measurement cycles should be maximally performed.
     */
    public int getMeasurementCycles() {
        return measurementCycles;
    }

    /**
     * @param measurementCycles
     *            How many measurement cycles should be maximally performed.
     */
    public void setMeasurementCycles(int measurementCycles) {
        this.measurementCycles = measurementCycles;
    }

    /**
     * @return
     *         How much time at max should be spend measuring in seconds.
     */
    public int getMeasurementTime() {
        return measurementTime;
    }

    /**
     * @param measurementTime
     *            How much time at max should be spend measuring in seconds.
     */
    public void setMeasurementTime(int measurementTime) {
        this.measurementTime = measurementTime;
    }

    /**
     * @return
     *         How much time can sampler take running at max in seconds.
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout
     *            How much time can sampler take running at max in seconds.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * <p>
     * This class implements interface for dynamic editor creation and value
     * validation.
     * 
     * @author Frantisek Haas
     * 
     */
    public static class SplDeploymentSectionFactory implements ISectionFactory {

        /** List of properties description. */
        private static final Map<String, EntryInformation> descriptions = new LinkedHashMap<String, EntryInformation>();

        /** Initialization of descriptions. */
        static {
            SplDeploymentConfiguration config = SplDeploymentConfiguration.createDefaultConfiguration();

            descriptions.put("useSystemShell", new EntryInformation(
                    "useSystemShell",
                    String.valueOf(defaultUseSystemShell),
                    "" +
                            "If true is set framework uses supposed system shell to execute project's build command. " +
                            "If false is set no shell is used and command is executed as is on path."));

            descriptions.put("clearTmpBefore", new EntryInformation(
                    "clearTmpBefore",
                    String.valueOf(defaultClearTmpBefore),
                    ""));

            descriptions.put("clearTmpAfter", new EntryInformation(
                    "clearTmpAfter",
                    String.valueOf(defaultClearTmpAfter),
                    ""));

            descriptions.put("javaPath", new EntryInformation(
                    "javaPath",
                    defaultJavaPath,
                    "Path to java binary on execution machine."));

            descriptions.put("samplerArguments", new EntryInformation(
                    "samplerArguments",
                    defaultSamplerArguments,
                    "Arguments passed to java on command line when starting sampler."));

            descriptions.put("warmupCycles", new EntryInformation(
                    "warmupCycles",
                    String.valueOf(config.getWarmupCycles()),
                    "Maximum number of warmup cycles to perform."));

            descriptions.put("warmupTime", new EntryInformation(
                    "warmupTime",
                    String.valueOf(config.getWarmupTime()),
                    "Maximum amount of time in seconds to spend warming up."));

            descriptions.put("measurementCycles", new EntryInformation(
                    "measurementCycles",
                    String.valueOf(config.getMeasurementCycles()),
                    "Maximum number of measurement cycles to perform."));

            descriptions.put("measurementTime", new EntryInformation(
                    "measurementTime",
                    String.valueOf(config.getMeasurementTime()),
                    "Maximum amount of time in seconds to spend sampling."));

            descriptions.put("timeout", new EntryInformation(
                    "timeout",
                    String.valueOf(config.getTimeout()),
                    "Maximum amount of time to spend on a single sampler. If sampler exceeds this limit it gets killed."));

            descriptions.put("timeSource", new EntryInformation(
                    "timeSource",
                    String.valueOf(config.getTimeSource()),
                    "Time source to be used."));
        }

        /**
         * @return
         *         <p>
         *         Returns overall section description.
         */
        @Override
        public String getDescription() {
            return "Configuration of measurement code generation and execution.";
        }

        /**
         * @return
         *         <p>
         *         Returns properties description.
         */
        @Override
        public Map<String, EntryInformation> getEntriesDescription() {
            return descriptions;
        }

        @Override
        public ISection createFromSectionName(String sectionName, Map<String, String> values) {
            if (sectionName.equals(IniConversionSplDeploymentConfiguration.DEPLOYMENT)) {
                return new SplMeasurementSection(this, sectionName, values);
            } else {
                return null;
            }
        }

        /**
         * <p>
         * Deployment section validator.
         * 
         * @author Frantisek Haas
         * 
         */
        public static class SplMeasurementSection implements ISection {

            private final ISectionFactory     factory;
            private final String              sectionName;
            private final Map<String, String> values;

            private final LinkedList<String>  errors;

            public SplMeasurementSection(ISectionFactory factory, String sectionName, Map<String, String> values) {
                this.factory = factory;
                this.sectionName = sectionName;
                this.values = values;
                this.errors = new LinkedList<>();
            }

            @Override
            public ISectionFactory getFactory() {
                return factory;
            }

            @Override
            public String getName() {
                return sectionName;
            }

            @Override
            public boolean isValid() {
                errors.clear();

                for (Entry<String, String> entry : values.entrySet()) {
                    if (!descriptions.containsKey(entry.getKey())) {
                        errors.add(String.format("Configuration key [%s] is not supported.", entry.getKey()));
                    }

                    switch (entry.getKey()) {
                        case "useSystemShell":
                        case "clearTmpBefore":
                        case "clearTmpAfter":
                            ConversionChecker.tryDecodeBoolean(errors, entry.getKey(), entry.getValue());
                            break;

                        case "javaPath":
                        case "samplerArguments":
                            // nothing to check for String
                            break;

                        default:
                            ConversionChecker.tryDecodeInteger(errors, entry.getKey(), entry.getValue());
                            break;
                    }
                }

                return errors.isEmpty();
            }

            @Override
            public List<String> getErrors() {
                return errors;
            }
        }

        @Override
        public String getSectionPrefix() {
            return IniConversionSplDeploymentConfiguration.DEPLOYMENT;
        }
    }
}
