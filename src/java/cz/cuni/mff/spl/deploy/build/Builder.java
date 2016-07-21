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
package cz.cuni.mff.spl.deploy.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.InfoConverter;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * This class loads user's prepared SPL XML configuration. Obtains HEAD
 * revision in THIS project to scan its classes. Checks which measurements
 * are not already present in the store and needs measuring. Checks out all
 * necessary code for building these samplers and generates them.
 * 
 * @author Frantisek Haas
 * 
 */
public class Builder {

    public static final SplLog         logger                  = SplLogger.getLogger("Build");

    public static final String         OUTPUT_DIR_NAME         = "build";
    public static final String         SOURCE_CODE_DIR_NAME    = "sourceCode";
    public static final String         GENERATED_CODE_DIR_NAME = "generatedCode";

    /**
     * Store to check for already measured data. Store used for temporary build
     * and execution directories if needed.
     */
    private final LocalStore           localStore;
    /** Where to execution code. Filled into loaded Info. */
    private final Machine              targetMachine;
    /** XML configuration file. */
    private final File                 xml;

    /** If interaction is required */
    private final InteractiveInterface interactive;
    /** Various configuration settings */
    private final ConfigurationBundle  config;

    /** Root of output directories. */
    private File                       outputDirectory;
    /** Where to check out source code. */
    private File                       sourceCodeDirectory;
    /** Where to generate sampling code. */
    private File                       generatedCodeDirectory;

    /** Loaded Info from XML. */
    private Info                       info;

    /** Generated samplers. */
    private final List<Sampler>        samplers;

    /**
     * Initializes builder object. To perform build call {@link #call()}.
     * 
     * @param localStore
     *            Store to check for already measured data. Store used for
     *            temporary build and execution directories if
     *            needed.
     * @param targetMachine
     *            Where to execution code. Filled into loaded Info.
     * @param xml
     *            XML configuration file. Source of Info.
     * @param interactive
     *            If interaction is required
     * @param config
     *            Various configuration settings.
     * 
     * @throws StoreException
     * @throws BuildException
     */
    public Builder(LocalStore localStore, Machine targetMachine, File xml, InteractiveInterface interactive, ConfigurationBundle config) {
        this.localStore = localStore;
        this.targetMachine = targetMachine;
        this.xml = xml.getAbsoluteFile();

        this.interactive = interactive;
        this.config = config;

        this.info = null;
        this.samplers = new LinkedList<>();
    }

    /**
     * Creates list of all paths combined from directory where revision was
     * checked out and relative class paths specified in the project info.
     * 
     * @param context
     * @param revision
     * @return
     * @throws BuildException
     */
    private File[] createAndExpandClassPaths(BuilderContext context, Revision revision)
            throws BuildException {
        LinkedList<String> classPaths = new LinkedList<>();

        String base = context.getRevisionMap().get(revision).getPath();
        for (String relativeClassPath : revision.getProject().getClasspaths()) {
            File classPathFile = new File(base, relativeClassPath);
            classPaths.add(classPathFile.getPath());
        }

        File[] expandedClassPathFiles = null;
        try {
            expandedClassPathFiles = ClassPathExpander.expandClassPathsToFiles(classPaths.toArray(new String[classPaths.size()]));
        } catch (FileNotFoundException e) {
            throw new BuildException("Failed to create or expand revision's class paths.", e);
        }

        logger.trace("Expanded class path count [%s].", expandedClassPathFiles.length);
        for (File expandedClassPathFile : expandedClassPathFiles) {
            logger.trace("Expanded  class path [%s].", expandedClassPathFile.getPath());
        }

        return expandedClassPathFiles;
    }

    /**
     * Actually builds sampling code for every measurement specified with
     * annotations.
     * 
     * @param context
     * @param measurement
     * @throws BuildException
     */
    private void buildSampler(BuilderContext context, Measurement measurement)
            throws BuildException {
        SampleIdentification sampleIdentification = new SampleIdentification(measurement);
        Generator generator = measurement.getGenerator();
        Method method = measurement.getMethod();

        File generatorRevisionDirectory = context.getRevisionMap().get(generator.getRevision());

        if (generatorRevisionDirectory == null) {
            throw new BuildException("Failed to build sampler its generator code not prepared.");
        }

        File methodRevisionDirectory = context.getRevisionMap().get(method.getRevision());

        if (methodRevisionDirectory == null) {
            throw new BuildException("Failed to build sampler its method code not prepared.");
        }

        String generatorDirectory = generatorRevisionDirectory.getPath();
        String methodDirectory = methodRevisionDirectory.getPath();
        logger.trace("Generating sampler for generator from [%s] and method from [%s].", generatorDirectory, methodDirectory);

        File[] generatorPaths = createAndExpandClassPaths(context, generator.getRevision());
        File[] methodPaths = createAndExpandClassPaths(context, method.getRevision());

        try {
            File samplerDirectory = FileUtils.createUniqueDirectory(generatedCodeDirectory, "sampler", false);

            Assembler assembler = new Assembler(
                    sampleIdentification,
                    generator,
                    generatorPaths,
                    method,
                    methodPaths,
                    measurement.getVariable().getVariables(),
                    samplerDirectory.getPath(),
                    config,
                    localStore.getLocalStoreRootDirectory());

            Sampler sampler = assembler.call();
            samplers.add(sampler);

        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Saves {@link Info} before building takes place.
     * 
     * @param info
     */
    private void savePreInfo(Info info) {
        try {
            InfoConverter.saveInfoToFile(info, new File(outputDirectory, "pre-build-info.xml"));
        } catch (ConversionException e) {
            logger.error(e, "Unable to save pre-build-info.xml");
        }
    }

    /**
     * Saves {@link Info} after building takes place.
     * 
     * @param info
     */
    private void savePostInfo(Info info) {
        try {
            InfoConverter.saveInfoToFile(info, new File(outputDirectory, "post-build-info.xml"));
        } catch (ConversionException e) {
            logger.error(e, "Unable to save post-build-info.xml");
        }
    }

    /**
     * Sets details about measurement status to {@link Info}.
     * 
     */
    private void setMeasurementStatus(Measurement measurement, Throwable cause) {
        if (measurement.getMeasurementState().isOk()) {
            measurement.getMeasurementState().setLastPhase(LastPhase.BUILD);
            if (cause != null) {
                measurement.getMeasurementState().setOk(false);
                measurement.getMeasurementState().setMessage(String.format("Measurement sampler failed to build, ended with: %s.", cause.getMessage()));
            }
        }

    }

    /**
     * This function loads user's prepared SPL XML configuration. Obtains HEAD
     * revision in THIS project to scan its classes. Checks which measurements
     * are not already present in the store and needs measuring. Checks out all
     * necessary code for building these samplers and generates them.
     * 
     * @return
     * @throws BuildException
     * @throws StoreException
     */
    public void call()
            throws BuildException, StoreException {
        outputDirectory = localStore.createTemporaryDirectory(OUTPUT_DIR_NAME);
        sourceCodeDirectory = new File(outputDirectory, SOURCE_CODE_DIR_NAME);
        generatedCodeDirectory = new File(outputDirectory, GENERATED_CODE_DIR_NAME);

        if (!this.sourceCodeDirectory.mkdir() || !this.generatedCodeDirectory.mkdir()) {
            throw new BuildException("Failed to create source code and generated code directories.");
        }

        // load info XML and obtain classes to scan, perform scan
        BuilderScanner scanner = new BuilderScanner(xml, targetMachine, outputDirectory, sourceCodeDirectory, interactive, config,
                localStore.getLocalStoreRootDirectory());
        scanner.call();

        info = scanner.getInfo();
        savePreInfo(info);
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        // based on scanned info checks measurements if they were build
        // check out all revisions needed
        BuilderContext context = new BuilderContext(info, localStore, xml, sourceCodeDirectory, scanner.getRepository(), scanner.getRevision(), interactive,
                config, localStore.getLocalStoreRootDirectory());
        context.call();
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        // report already measured data in the store
        logger.info("Data will be loaded for measurements [%s].", context.getAlreadyMeasured().size());
        for (Measurement measurement : context.getAlreadyMeasured()) {
            logger.debug("Skipped creating sampler, already measured [%s].", SampleIdentification.createIdentification(measurement));
        }

        // report measurements which can't be measured
        logger.info("Skipping measurements due to missing code [%s].", context.getUnableToMeasure().size());
        for (Measurement measurement : context.getUnableToMeasure()) {
            logger.debug("Skipped creating sampler, missing code [%s].", SampleIdentification.createIdentification(measurement));
        }

        logger.info("Creating samplers for measurements [%s].", context.getNotMeasured().size());

        int measurementCount = 1;
        // build all samplers needed
        for (Measurement measurement : context.getNotMeasured()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();

            try {
                buildSampler(context, measurement);
                setMeasurementStatus(measurement, null);
                logger.debug("Created sampler for measurement [%s].", SampleIdentification.createIdentification(measurement));

            } catch (Throwable cause) {
                setMeasurementStatus(measurement, cause);
                logger.error(cause, "Failed to create runtime for measurement [%s].", SampleIdentification.createIdentification(measurement));
            }

            logger.info("[%" + Utils.magnitude(context.getNotMeasured().size()) + "d/%d].", measurementCount, context.getNotMeasured().size());
            measurementCount++;
        }

        savePostInfo(info);
        InvokedExecutionConfiguration.checkIfExecutionAborted();

    }

    /**
     * Returns Info after all samples were created and all missing fields
     * filled.
     * 
     * @return
     */
    public Info getInfo() {
        return info;
    }

    /**
     * Returns prepared samplers for measuring.
     * 
     * @return
     */
    public List<Sampler> getSamplers() {
        return samplers;
    }
}
