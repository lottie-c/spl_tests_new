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

/**
 * <p>
 * Encapsulates specific configurations of different parts of the SPL framework.
 * This class makes it easier to pass all configurations across the framework so
 * each module can access it's own configuration and make easy future
 * extensions.
 * </p>
 * 
 * @see SplAccessConfiguration
 * @see SplEvaluatorConfiguration
 * @see SplDeploymentConfiguration
 * 
 * @author Frantisek Haas
 * 
 */
public class ConfigurationBundle {

    /** The access config. */
    private SplAccessConfiguration     accessConfig;

    /** The evaluator config. */
    private SplEvaluatorConfiguration  evaluatorConfig;

    /** The deployment config. */
    private SplDeploymentConfiguration deploymentConfig;

    /**
     * Creates the default.
     * 
     * @return The configuration bundle.
     */
    public static ConfigurationBundle createDefault() {
        return new ConfigurationBundle(
                SplAccessConfiguration.createDefaultConfiguration(),
                SplEvaluatorConfiguration.createDefaultConfiguration(),
                SplDeploymentConfiguration.createDefaultConfiguration());
    }

    /**
     * Instantiates a new empty configuration bundle.
     */
    public ConfigurationBundle() {

    }

    /**
     * Instantiates a new configuration bundle.
     * 
     * @param accessConfig
     *            The access config.
     * @param evaluatorConfig
     *            The evaluator config.
     * @param deploymentConfig
     *            The deployment config.
     */
    public ConfigurationBundle(
            SplAccessConfiguration accessConfig,
            SplEvaluatorConfiguration evaluatorConfig,
            SplDeploymentConfiguration deploymentConfig) {
        this.accessConfig = accessConfig;
        this.evaluatorConfig = evaluatorConfig;
        this.deploymentConfig = deploymentConfig;
    }

    /**
     * Gets the access config.
     * 
     * @return The access config.
     */
    public SplAccessConfiguration getAccessConfig() {
        return accessConfig;
    }

    public void setAccessConfig(SplAccessConfiguration accessConfig) {
        this.accessConfig = accessConfig;
    }

    /**
     * Gets the evaluator config.
     * 
     * @return The evaluator config.
     */
    public SplEvaluatorConfiguration getEvaluatorConfig() {
        return evaluatorConfig;
    }

    /**
     * Sets the evaluator config.
     * 
     * @param evaluatorConfig
     *            The new evaluator config.
     */
    public void setEvaluatorConfig(SplEvaluatorConfiguration evaluatorConfig) {
        this.evaluatorConfig = evaluatorConfig;
    }

    /**
     * Gets the deployment config.
     * 
     * @return The deployment config.
     */
    public SplDeploymentConfiguration getDeploymentConfig() {
        return deploymentConfig;
    }

    /**
     * Sets the deployment config.
     * 
     * @param deploymentConfig
     *            The new deployment config.
     */
    public void setDeploymentConfig(SplDeploymentConfiguration deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

}
