package cz.cuni.mff.spl.conversion;

import org.ini4j.Ini;

import cz.cuni.mff.spl.configuration.SplDeploymentConfiguration;

public class IniConversionSplDeploymentConfiguration {

    public static final String DEPLOYMENT = "deployment";

    /**
     * Saves SPL deployment configuration to provided INI instance.
     * 
     * @param config
     *            The SPL deployment configuration.
     * @param output
     *            The output.
     * 
     */
    static void saveSplDeploymentConfiguration(SplDeploymentConfiguration config, Ini output) {
        encodeConfiguration(config, output);
    }

    /**
     * Load SPL deployment configuration from INI instance.
     * 
     * @param input
     *            The input.
     * @return The SPL deployment configuration.
     * @throws ConversionException
     *             The conversion exception.
     */
    static SplDeploymentConfiguration loadSplDeploymentConfiguration(Ini input) throws ConversionException {
        try {
            return decodeSplMeasurementConfiguration(input);
        } catch (ConversionException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Encodes configuration to provided INI instance.
     * 
     * @param config
     *            The SPL deployment configuration to encode.
     * @param ini
     *            The INI to add configuration values to.
     * @return Same instance as {@code ini} argument.
     */
    private static Ini encodeConfiguration(SplDeploymentConfiguration config, Ini ini) {

        IniManipulator.writeBoolean(ini, DEPLOYMENT, "useSystemShell", config.getUseSystemShell());
        IniManipulator.writeBoolean(ini, DEPLOYMENT, "clearTmpBefore", config.getClearTmpBefore());
        IniManipulator.writeBoolean(ini, DEPLOYMENT, "clearTmpAfter", config.getClearTmpAfter());
        IniManipulator.writeString(ini, DEPLOYMENT, "javaPath", config.getJavaPath());
        IniManipulator.writeString(ini, DEPLOYMENT, "samplerArguments", config.getSamplerArguments());
        IniManipulator.writeInteger(ini, DEPLOYMENT, "warmupCycles", config.getWarmupCycles());
        IniManipulator.writeInteger(ini, DEPLOYMENT, "warmupTime", config.getWarmupTime());
        IniManipulator.writeInteger(ini, DEPLOYMENT, "measurementCycles", config.getMeasurementCycles());
        IniManipulator.writeInteger(ini, DEPLOYMENT, "measurementTime", config.getMeasurementTime());
        IniManipulator.writeInteger(ini, DEPLOYMENT, "timeout", config.getTimeout());
        IniManipulator.writeTimeSource(ini, DEPLOYMENT, "timeSource", config.getTimeSource());

        return ini;
    }

    /**
     * <p>
     * Reads configuration values for {@link SplDeploymentConfiguration} from
     * provided INI instance.
     * </p>
     * 
     * @param ini
     *            The INI instance to read values from.
     * @return The SPL deployment configuration.
     * @throws ConversionException
     *             The conversion exception.
     */
    private static SplDeploymentConfiguration decodeSplMeasurementConfiguration(Ini ini) throws ConversionException {
        SplDeploymentConfiguration defaultConfig = SplDeploymentConfiguration.createDefaultConfiguration();

        return new SplDeploymentConfiguration(
                IniManipulator.readBoolean(ini, DEPLOYMENT, "useSystemShell", defaultConfig.getUseSystemShell()),
                IniManipulator.readBoolean(ini, DEPLOYMENT, "clearTmpBefore", defaultConfig.getClearTmpBefore()),
                IniManipulator.readBoolean(ini, DEPLOYMENT, "clearTmpAfter", defaultConfig.getClearTmpAfter()),
                IniManipulator.readString(ini, DEPLOYMENT, "javaPath", defaultConfig.getJavaPath()),
                IniManipulator.readString(ini, DEPLOYMENT, "samplerArguments", defaultConfig.getSamplerArguments()),
                IniManipulator.readInteger(ini, DEPLOYMENT, "warmupCycles", defaultConfig.getWarmupCycles()),
                IniManipulator.readInteger(ini, DEPLOYMENT, "warmupTime", defaultConfig.getWarmupTime()),
                IniManipulator.readInteger(ini, DEPLOYMENT, "measurementCycles", defaultConfig.getMeasurementCycles()),
                IniManipulator.readInteger(ini, DEPLOYMENT, "measurementTime", defaultConfig.getMeasurementTime()),
                IniManipulator.readInteger(ini, DEPLOYMENT, "timeout", defaultConfig.getTimeout()),
                IniManipulator.readTimeSource(ini, DEPLOYMENT, "timeSource", defaultConfig.getTimeSource()));
        
    }
}
