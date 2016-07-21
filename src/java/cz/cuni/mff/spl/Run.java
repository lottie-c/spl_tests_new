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
package cz.cuni.mff.spl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.configuration.SplAccessConfiguration;
import cz.cuni.mff.spl.configuration.SplDeploymentConfiguration;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.IniConversion;
import cz.cuni.mff.spl.deploy.build.Builder;
import cz.cuni.mff.spl.deploy.build.Sampler;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.exception.DeployException;
import cz.cuni.mff.spl.deploy.execution.run.IExecution;
import cz.cuni.mff.spl.deploy.execution.run.IExecution.Trace;
import cz.cuni.mff.spl.deploy.execution.run.LocalExecution;
import cz.cuni.mff.spl.deploy.execution.run.RemoteExecution;
import cz.cuni.mff.spl.deploy.store.IStore;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.Evaluator;
import cz.cuni.mff.spl.evaluator.input.CachingMeasurementSampleProvider;
import cz.cuni.mff.spl.evaluator.input.MeasurementSampleProvider;
import cz.cuni.mff.spl.evaluator.input.StoreMeasurementDataProvider;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.Pair;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplDynamicTargetLoggerFactory;
import cz.cuni.mff.spl.utils.logging.SplDynamicTargetLoggerFactory.LEVELS;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;
import cz.cuni.mff.spl.utils.ssh.SshDetails;
import cz.cuni.mff.spl.utils.ssh.SshReconnectingSession;

/**
 * Entry point of spl command line utility. Requires path to spl configuration
 * xml and spl working directory. Prepares sampler code for measuring and
 * executes it on the local machine. All data and files produced are stored in
 * the spl working directory.
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 * 
 */
public class Run {

    /** The Constant logger. */
    private static final SplLog logger = SplLogger.getLogger(Run.class);

    /**
     * Checks validity of parameters passed to.
     * 
     * @param xml
     *            The xml.
     * @param wd
     *            The wd.
     * @param ini
     *            The ini.
     * @throws SplRunError
     *             The spl run error.
     *             {@link #run(File, File, File, String, InteractiveInterface)}
     *             function.
     */
    private static void checkRunParameters(File xml, File wd, File ini)
            throws SplRunError {
        if (xml == null) {
            throw new SplRunError("SPL xml is not specified.");
        }

        if (!xml.exists()) {
            throw new SplRunError(String.format("SPL xml does not exist [%s].", xml));
        }

        if (wd == null) {
            throw new SplRunError(String.format("SPL working directory is not specified [%s].", wd));
        }

        if (ini != null && !ini.exists()) {
            logger.error("SPL ini specified but does not exist [%s].", ini);
        }
    }

    /**
     * <p>
     * Verify that current JVM is JDK with javac present.
     * 
     */
    private static void checkSystem() {
        if (!cz.cuni.mff.spl.deploy.build.Compiler.isCompilerPresent()) {
            logger.error("Compiler is not present. Probably running on JRE not JDK. If measuring will be needed it will fail.");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                InvokedExecutionConfiguration.checkIfExecutionAborted();
            }
        }
    }

    /**
     * Initializes local store in the specified working directory.
     * 
     * @param wd
     *            The wd.
     * @return The local store.
     * @throws SplRunError
     *             The spl run error.
     */
    private static LocalStore initializeLocalStore(File wd)
            throws SplRunError {
        try {
            return new LocalStore(wd);
        } catch (StoreException e) {
            throw new SplRunError("Failed to initialize local data store.", e);
        }
    }

    /**
     * Initializes logger in local store's temporary directory. This log is
     * later copied into evaluation directory and is used by Eclipse or Hudson
     * plug-in.
     * 
     * @param localStore
     *            The local store.
     * @return The logger.
     */
    private static Logger initializeLog(LocalStore localStore) {
        try {
            File logFile = localStore.createTemporaryFile("log", "");
            logFile.deleteOnExit();
            Logger log = new Logger(logFile, false);
            SplDynamicTargetLoggerFactory.addLogOutput(log, LEVELS.DEBUG);
            return log;

        } catch (StoreException | FileNotFoundException e) {
            logger.error(e, "Failed to initialize log.");
        }

        return null;
    }

    /**
     * Copies content of logger to the store directory.
     * 
     * @param log
     *            The log.
     * @param directory
     *            The directory.
     */
    private static void copyLog(Logger log, IStoreDirectory directory) {
        if (log == null || directory == null) {
            return;
        }

        try {
            IStoreFile file = directory.createFile("spl.log");
            FileUtils.copy(log.getInputStream(), file.getOutputStream());
        } catch (Exception e) {
            logger.error(e, "Failed to copy log to store directory.");
        }
    }

    /**
     * Loads access configuration if it's possible or returns default
     * configuration.
     * 
     * @param ini
     *            The ini.
     * @return The spl access configuration.
     */
    private static SplAccessConfiguration loadAccessConfiguration(File ini) {
        SplAccessConfiguration accessConfig = SplAccessConfiguration.createDefaultConfiguration();

        if (ini != null) {
            try {
                accessConfig = IniConversion.loadSplAccessConfiguration(new FileInputStream(ini));
            } catch (FileNotFoundException e) {
                logger.error(e, "Failed to load access configuration, using default configuration.");
            } catch (ConversionException e) {
                logger.error(e, "Failed to load access configuration, using default configuration.");
            }
        }

        return accessConfig;
    }

    /**
     * Loads evaluator configuration if it's possible or returns default
     * configuration.
     * 
     * @param ini
     *            The ini.
     * @return The spl evaluator configuration.
     */
    private static SplEvaluatorConfiguration loadEvaluatorConfiguration(File ini) {
        SplEvaluatorConfiguration evaluatorConfig = SplEvaluatorConfiguration.createDefaultConfiguration();

        if (ini != null) {
            try {
                evaluatorConfig = IniConversion.loadSplEvaluatorConfiguration(new FileInputStream(ini));
            } catch (FileNotFoundException e) {
                logger.error(e, "Failed to load evaluator configuration, using default configuration.");
            } catch (ConversionException e) {
                logger.error(e, "Failed to load evaluator configuration, using default configuration.");
            }
        }

        return evaluatorConfig;
    }

    /**
     * Loads measurement configuration if it's possible or returns default
     * configuration.
     * 
     * @param ini
     *            The ini.
     * @return The spl deployment configuration.
     */
    private static SplDeploymentConfiguration loadMeasurementConfiguration(File ini) {
        SplDeploymentConfiguration measurementConfig = SplDeploymentConfiguration.createDefaultConfiguration();

        if (ini != null) {
            try {
                measurementConfig = IniConversion.loadSplMeasurementConfiguration(new FileInputStream(ini));
            } catch (FileNotFoundException e) {
                logger.error(e, "Failed to load measurement configuration, using default configuration.");
            } catch (ConversionException e) {
                logger.error(e, "Failed to load measurement configuration, using default configuration.");
            }
        }

        return measurementConfig;
    }

    /**
     * <p>
     * Loads all configurations into a bundle.
     * 
     * @param ini
     *            The INI file.
     * @return
     */
    private static ConfigurationBundle loadConfiguration(File ini) {
        SplAccessConfiguration accessConfig = loadAccessConfiguration(ini);
        SplEvaluatorConfiguration evaluatorConfig = loadEvaluatorConfiguration(ini);
        SplDeploymentConfiguration deploymentConfig = loadMeasurementConfiguration(ini);

        ConfigurationBundle config = new ConfigurationBundle(accessConfig, evaluatorConfig, deploymentConfig);

        return config;
    }

    /**
     * <p>
     * Clears store temporary directory if so is specified in the configuration.
     * 
     * @param localStore
     * @param config
     */
    private static void clearTmpBefore(LocalStore localStore, ConfigurationBundle config) {
        if (config.getDeploymentConfig().getClearTmpBefore()) {
            logger.info("Deleting temporary store, may take a while.");
            localStore.clearTemporary();
        }
    }

    /**
     * <p>
     * Clears store temporary directory if so is specified in the configuration.
     * 
     * @param localStore
     * @param config
     */
    private static void clearTmpAfter(LocalStore localStore, ConfigurationBundle config) {
        if (config.getDeploymentConfig().getClearTmpAfter()) {
            logger.info("Deleting temporary store, may take a while.");
            localStore.clearTemporary();
        }
    }

    /**
     * Runs SPL framework.
     * 
     * @param xml
     *            SPL XML file. Required
     * @param wd
     *            SPL working directory. Required.
     * @param ini
     *            SPL INI configuration file. Not required.
     * @param machine
     *            Machine where to perform measuring. Not required, defaults to
     *            local machine.
     * @param interactive
     *            Object to interact with user. Not required, defaults to no
     *            interaction.
     * @return The string with path to evaluation result.
     * @throws SplRunError
     *             The spl run error.
     */
    public static String run(File xml, File wd, File ini, String machine, InteractiveInterface interactive)
            throws SplRunError {
        checkRunParameters(xml, wd, ini);

        checkSystem();

        try (LocalStore localStore = initializeLocalStore(wd)) {

            ConfigurationBundle config = loadConfiguration(ini);

            clearTmpBefore(localStore, config);

            /**
             * <p>
             * Opens log so the output can be captured and saved together with
             * evaluation results.
             */
            Logger log = initializeLog(localStore);

            try (
                    /**
                     * <p>
                     * Opens connection to the remote machine specified. In case
                     * there are some malformed credentials and the connection
                     * is impossible to open. Therefore first connect must
                     * always succeed to verify configuration is valid. Further
                     * problems with connection can be potentially resolved.
                     */
                    SshReconnectingSession sshSession = initializeSshSession(machine, config, interactive)) {

                Pair<List<Sampler>, Info> buildResult = build(localStore, machine, xml, interactive, config);
                Info info = buildResult.getRight();
                List<Sampler> samplers = buildResult.getLeft();

                execute(localStore, info, samplers, sshSession, machine, interactive, config);

                IStoreDirectory evaluationResult;
                try {
                    ConfigurationBundle evalutorConfiguration = new ConfigurationBundle(null, config.getEvaluatorConfig(), config.getDeploymentConfig());
                    evaluationResult = evaluate(localStore, info, evalutorConfiguration, localStore.createTemporaryDirectory("evaluator"));
                } catch (StoreException e) {
                    logger.error(e, "Unable to create temporary directory.");
                    throw new SplRunError(e);
                }

                /**
                 * Output capturing is stopped and log is copied to evaluation
                 * directory.
                 */
                log.close();
                copyLog(log, evaluationResult);

                clearTmpAfter(localStore, config);

                return evaluationResult.getName();

            } catch (InterruptedException e) {
                logger.info(e, "SPL has been interrupted.");
                throw new SplRunError("SPL has been interrupted.");

            } finally {
                if (log != null) {
                    log.close();
                }
            }
        }
    }

    /**
     * <p>
     * Checks if machine identified with the string is meant to be this one the
     * framework is running on. Thus local.
     * </p>
     * 
     * @param machine
     *            May be null or valid identifier.
     * @return
     *         Returns true if {@code machine} is either null, empty string ''
     *         or has value of 'local'.
     */
    private static boolean isMachineLocal(String machine) {
        return (machine == null || machine.equals("") || machine.equals("local"));
    }

    /**
     * Instantiates {@link Machine} object to identify measuring machine.
     * 
     * @param machine
     *            The machine.
     * @param accessConfig
     *            The access config.
     * @return The machine.
     */
    private static Machine getMachine(String machine, SplAccessConfiguration accessConfig) {
        if (isMachineLocal(machine)) {
            try {
                return new Machine("local", InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                return new Machine("local", "<unknown>");
            }

        } else {
            if (accessConfig.getSshValues(machine).containsKey("url")) {
                return new Machine(machine, accessConfig.getSshValues(machine).get("url"));
            } else {
                return new Machine(machine, "<unknown>");
            }
        }
    }

    /**
     * Loads SPL XML information and scans annotations. Builds all sampling code
     * needed.
     * 
     * @param localStore
     *            The local store.
     * @param machine
     *            The machine.
     * @param xml
     *            The xml.
     * @param interactive
     *            The interactive.
     * @param config
     *            The config.
     * @return The pair.
     * @throws SplRunError
     *             The spl run error.
     * @throws InterruptedException
     *             The interrupted exception.
     */
    public static Pair<List<Sampler>, Info> build(LocalStore localStore, String machine, File xml, InteractiveInterface interactive,
            ConfigurationBundle config)
            throws SplRunError, InterruptedException {
        try {
            logger.info("Scanning project and building code ... ");
            Builder builder = new Builder(localStore, getMachine(machine, config.getAccessConfig()), xml, interactive, config);
            builder.call();
            logger.info("Scanned project and builded code.");

            Pair<List<Sampler>, Info> result = new Pair<>(builder.getSamplers(), builder.getInfo());

            return result;

        } catch (BuildException | StoreException e) {
            throw new SplRunError(String.format(
                    "Failed to scan the project and build any sampling code (%s).",
                    e.getMessage()), e);
        }
    }

    /**
     * Based on values loaded from INI file and machine name specified
     * instantiates {@link IExecution} object.
     * 
     * @param values
     *            The values.
     * @param interactive
     *            The interactive.
     * @return
     *         [SshDetails, Path]
     * 
     * 
     * @throws DeployException
     *             The deploy exception.
     */
    private static Pair<SshDetails, String> getExecutionDetails(Map<String, String> values, InteractiveInterface interactive)
            throws DeployException {
        SshDetails details = SshDetails.create(values, interactive);
        String path = null;

        if (values.containsKey("path")) {
            path = values.get("path");
        } else {
            throw new DeployException("Path on machine not set correctly.");
        }

        return new Pair<SshDetails, String>(details, path);
    }

    /**
     * Initializes connection to the machine if it's remote one. Otherwise
     * returns null.
     * 
     * @param machine
     *            Machine to connect to.
     * @param config
     *            Configuration with further details.
     * @param interactive
     *            Interface for user interaction.
     * @return
     * @throws SplRunError
     * @throws DeployException
     */
    private static SshReconnectingSession initializeSshSession(String machine, ConfigurationBundle config, InteractiveInterface interactive)
            throws SplRunError {
        if (isMachineLocal(machine)) {
            return null;
        }

        if (config.getAccessConfig().containsSshValues(machine)) {
            SshReconnectingSession session = null;
            try {
                Pair<SshDetails, String> details = getExecutionDetails(config.getAccessConfig().getSshValues(machine), interactive);
                session = new SshReconnectingSession(details.getLeft());
                // first connect must succeed
                session.connect();
                return session;

            } catch (DeployException e) {
                if (session != null) {
                    session.close();
                }

                throw new SplRunError("Failed to initialize connection to remote host.", e);
            }

        } else {
            throw new SplRunError("Failed to initialize connection to remote host. Machine access information not found.");
        }

    }

    /**
     * Initializes object to control execution based on machine id and access
     * configuration.
     * 
     * @param localStore
     *            Store for creating temporary directory.
     * @param info
     *            The info.
     * @param samplers
     *            Sampler to execute.
     * @param machine
     *            The machine where to measure.
     * @param session
     *            Already opened session to the machine.
     * @param interactive
     *            The interactive.
     * @param config
     *            The config.
     * @return The i execution.
     * @throws DeployException
     *             The deploy exception.
     */
    private static IExecution initializeExecution(LocalStore localStore, Info info, List<Sampler> samplers, SshReconnectingSession session, String machine,
            InteractiveInterface interactive,
            ConfigurationBundle config)
            throws DeployException {
        IExecution execution;
        if (isMachineLocal(machine)) {
            File executionDirectory = localStore.createTemporaryDirectory("execution");
            execution = new LocalExecution(info, samplers, executionDirectory, config);

        } else {
            if (config.getAccessConfig().containsSshValues(machine)) {
                // only to get path
                Pair<SshDetails, String> details = getExecutionDetails(config.getAccessConfig().getSshValues(machine), interactive);
                // initialize with already opened session
                execution = new RemoteExecution(info, samplers, session, details.getRight(), config);
            } else {
                throw new DeployException("Failed to initialize execution. Machine access information not found.");
            }
        }

        return execution;
    }

    /**
     * Executes all samplers via execution object and stores result in the
     * store.
     * 
     * @param localStore
     *            The local store.
     * @param info
     *            The info.
     * @param samplers
     *            Sampler to execute.
     * @param machine
     *            The machine where to measure.
     * @param session
     *            Already opened session to the machine.
     * @param interactive
     *            The interactive.
     * @param config
     *            The config.
     * @throws InterruptedException
     *             The interrupted exception.
     */
    public static void execute(LocalStore localStore, Info info, List<Sampler> samplers, SshReconnectingSession session, String machine,
            InteractiveInterface interactive,
            ConfigurationBundle config)
            throws InterruptedException {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        if (samplers.isEmpty()) {
            logger.info("Nothing to measure, skipping measurement.");
            return;
        }

        try (IExecution execution = initializeExecution(localStore, info, samplers, session, machine, interactive, config)) {
            logger.info("Starting measuring ... ");
            execution.start();
            logger.info("Measuring in progress.");

            logger.info("Waiting for measurements to finish ... ");
            execution.waitForFinished();

            if (execution.isSuccessful()) {
                logger.info("Measurements finished successfully.");
            } else {
                logger.error("Some measurements failed.");
            }

            logger.info("Transfering measured data ... ");
            int samplerId = 1;
            for (Sampler sp : samplers) {
                if (execution.getTrace(sp.getIdentification()).getStatus() != Trace.Status.Successful) {
                    logger.error("Not transferring [%s]: measuring has failed.", sp.getIdentification());

                } else {
                    try (InputStream stream = execution.getResult(sp.getIdentification())) {
                        localStore.saveMeasurement(stream, sp.getIdentification());
                        logger.debug("Transferred measured data for [%s].", sp.getIdentification());
                        logger.info("[%" + Utils.magnitude(samplers.size()) + "d/%d].", samplerId, samplers.size());
                    } catch (IOException | StoreException e) {
                        logger.error("Failed to transfer measured data for [%s] due to [%s].", sp.getIdentification(), e.getMessage());
                    } catch (DeployException e) {
                        logger.error("Failed to transfer measured data for [%s] due to [%s].", sp.getIdentification(), e.getMessage());
                        throw e;
                    }
                }

                samplerId++;
            }
            logger.info("Measured data transferred.");

        } catch (DeployException e) {
            logger.error(e, "Failed to measure all samples successfully.");
        }
    }

    /**
     * Runs evaluation on provided context information with data from specified
     * store.
     * 
     * @param store
     *            The store.
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param temporaryDirectory
     *            The temporary directory.
     * @return The store directory with evaluation result.
     * @throws SplRunError
     *             Throws when evaluation is not performed correctly.
     */
    public static IStoreDirectory evaluate(IStore store, Info info, ConfigurationBundle configuration, File temporaryDirectory) throws SplRunError {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        checkEvaluationPreconditions(store, info, configuration.getEvaluatorConfig());

        logger.info("Processing evaluation (generating graphs takes a lot of time) ... ");

        long nanos = System.nanoTime();

        StoreMeasurementDataProvider measurementDataProvider = new StoreMeasurementDataProvider(store);
        MeasurementSampleProvider sampleProvider = new CachingMeasurementSampleProvider(measurementDataProvider);

        IStoreDirectory outputStoreDirectory;
        try {
            outputStoreDirectory = store.createEvaluationDirectory("run-evaluate");
            logger.info("Evaluation output directory is [%s]", outputStoreDirectory.getName());
        } catch (StoreException e) {
            logger.fatal(e, "Unable to obtain evaluation output directory from store.");
            throw new SplRunError("Unable to obtain evaluation output directory from store.", e);
        }

        Evaluator.evaluate(configuration, info, sampleProvider, outputStoreDirectory, temporaryDirectory);

        logger.info("Evaluation took %s s", (System.nanoTime() - nanos) / 1e9);

        logger.info("Evaluation finished.");

        return outputStoreDirectory;
    }

    /**
     * <p>
     * Checks evaluation preconditions. Preconditions are met when this method
     * does not throw {@link SplRunError} exception.
     * <p>
     * Preconditions that have to be met:
     * <ul>
     * <li>Provided context information object {@code info} can't be
     * 
     * @param store
     *            The store.
     * @param info
     *            The context info.
     * @param configuration
     *            The configuration.
     * @throws SplRunError
     *             The SPL run error is thrown when evaluation preconditions are
     *             not met. {@code null}.</li> <li>Provided store object
     *             {@code store} can't be {@code null}.</li>
     *             </ul>
     *             <p>
     *             Number of annotations is additionally checked and when no
     *             annotations to evaluate are found, than warning to log is
     *             issued. This does not break preconditions.
     */
    private static void checkEvaluationPreconditions(IStore store, Info info, SplEvaluatorConfiguration configuration) throws SplRunError {
        if (configuration == null) {
            logger.fatal("configuration == null");
            throw new SplRunError("SplEvaluatorConfiguration can not be null.");
        }
        if (info == null) {
            throw new SplRunError("Info with project information can not be null.");
        }
        if (store == null) {
            throw new SplRunError("Output store can not be null.");
        }
        if (info.getAnnotationLocations().isEmpty()) {
            logger.warn("Evaluation context information has no annotations.");
        }
    }

    /**
     * Unrecoverable error.
     * 
     * @author Frantisek Haas
     * 
     */
    @SuppressWarnings("serial")
    public static class SplRunError extends Exception {

        /**
         * Instantiates a new spl run error.
         */
        public SplRunError() {

        }

        /**
         * Instantiates a new spl run error.
         * 
         * @param message
         *            The message.
         */
        public SplRunError(String message) {
            super(message);
        }

        /**
         * Instantiates a new spl run error.
         * 
         * @param cause
         *            The cause.
         */
        public SplRunError(Throwable cause) {
            super(cause);
        }

        /**
         * Instantiates a new spl run error.
         * 
         * @param message
         *            The message.
         * @param cause
         *            The cause.
         */
        public SplRunError(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
