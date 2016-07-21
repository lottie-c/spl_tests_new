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
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Info.ProjectNotFoundException;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Repository.RevisionNotFoundException;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.InfoConverter;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.build.vcs.IRepository;
import cz.cuni.mff.spl.deploy.build.vcs.RepositoryFactory;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.scanner.Scanner;
import cz.cuni.mff.spl.scanner.ScannerException;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.Pair;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplLog;

/**
 * Loads XML {@link Info} and prepares and builds HEAD revision. Scans the
 * revision and serves scanned {@link Info}.
 * 
 * @author Frantisek Haas
 * 
 */
public class BuilderScanner {

    private static final SplLog           logger = Builder.logger;

    /** Loaded {@link Info} combined with scanned information. */
    private Info                          resultInfo;
    /** THIS project's checked out repository. */
    private Pair<Repository, IRepository> repositoryPair;
    /** HEAD revision's checked out code. */
    private Pair<Revision, File>          revisionPair;

    /** XML configuration file to load. */
    private final File                    xml;
    /** Where measurement will be performed. Important due identification. */
    private final Machine                 targetMachine;
    /** Where to place files. */
    private final File                    outputDirectory;
    /** Where to checkout HEAD revision. */
    private final File                    sourceCodeDirectory;

    /** Object for interaction with user. */
    private final InteractiveInterface    interactive;
    /** Configuration. */
    private final ConfigurationBundle     config;

    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File                    localStoreRootDirectory;

    /**
     * Instantiates a new builder scanner.
     * 
     * @param xml
     *            The project XML configuration file.
     * @param targetMachine
     *            The target machine.
     * @param outputDirectory
     *            The output directory.
     * @param sourceCodeDirectory
     *            The source code directory.
     * @param interactive
     *            The interactive interface instance.
     * @param config
     *            The configuration bundle.
     * @param localStoreRootDirectory
     *            The {@link LocalStore} root directory or {@code null} when not
     *            available.
     */
    public BuilderScanner(File xml, Machine targetMachine, File outputDirectory, File sourceCodeDirectory, InteractiveInterface interactive,
            ConfigurationBundle config, File localStoreRootDirectory) {

        this.repositoryPair = null;
        this.revisionPair = null;

        this.xml = xml;
        this.targetMachine = targetMachine;
        this.outputDirectory = outputDirectory;
        this.sourceCodeDirectory = sourceCodeDirectory;

        this.interactive = interactive;
        this.config = config;
        this.localStoreRootDirectory = localStoreRootDirectory;
    }

    /**
     * Loads base info from XML file.
     * 
     * @return
     * @throws BuildException
     */
    private Info loadInfo()
            throws BuildException {
        try {
            Info loadedInfo = InfoConverter.loadInfoFromFile(xml);
            loadedInfo.setMachine(targetMachine);
            return loadedInfo;
        } catch (ConversionException e) {
            throw new BuildException(String.format("Failed to load Info from file [%s].", xml), e);
        }
    }

    /**
     * Resolves HEAD revision from THIS project to be scanned.
     * 
     * @param info
     * @return
     * @throws BuildException
     */
    private Revision getRevisionToScan(Info info)
            throws BuildException {
        try {
            return info.getThisProject().getRepository().getHeadRevision();
        } catch (RevisionNotFoundException e) {
            throw new BuildException("Failed to find HEAD revision to scan.", e);
        } catch (ProjectNotFoundException e) {
            throw new BuildException("Failed to find THIS project to scan.", e);
        }
    }

    /**
     * Checks out revision to specified directory and stores parsed
     * {@link IRepository} and revision's directory for later use.
     * 
     * @param revision
     * @param directory
     * @throws BuildException
     */
    private void checkoutRevision(Revision revision, File directory)
            throws BuildException {
        try {
            File cache = FileUtils.createUniqueDirectory(sourceCodeDirectory, "repository-cache", false);

            String type = revision.getRepository().getType();
            String url = revision.getRepository().getUrl();
            Map<String, String> values = config.getAccessConfig().getVcsValues(revision.getRepository().getRepositoryConfigurationSectionName());

            IRepository repository = RepositoryFactory.parse(type, url, values, xml, cache, interactive, localStoreRootDirectory);

            String id = repository.checkout(revision.getValue(), directory);
            revision.setRevisionIdentification(id);

            logger.debug("Checked out revision [%s][%s][%s] into [%s].",
                    revision.getRepository().getType(), revision.getRepository().getUrl(), revision.getValue(), directory);

            repositoryPair = new Pair<Repository, IRepository>(revision.getRepository(), repository);
            revisionPair = new Pair<Revision, File>(revision, directory);

        } catch (IOException e) {
            throw new BuildException("Failed to create cache directory.", e);
        }
    }

    /**
     * This function checks out the revision for scanning. If build commands is
     * specified it builds the code.
     * 
     * @param revision
     *            {@link Revision} to check out, build and retrieve scan
     *            information.
     * @return
     *         Pair - [classPathsToScan, patternsToScan].
     * @throws BuildException
     * 
     */
    private Pair<List<String>, List<String>> getScanConfiguration(Revision revision)
            throws BuildException {
        revision.getRepository().setUrl(revision.getRepository().getUrl());

        try {
            File revisionDirectory = FileUtils.createUniqueDirectory(sourceCodeDirectory, "revision", false);

            checkoutRevision(revision, revisionDirectory);
            BuilderUtils.buildRevision(revision, revisionDirectory, config);

            Project project = revision.getProject();
            LinkedList<String> classPaths = new LinkedList<>();
            for (String classPath : project.getClasspaths()) {
                classPaths.add(new File(revisionDirectory, classPath).getPath());
            }

            return new Pair<List<String>, List<String>>(classPaths, project.getScanPatterns());

        } catch (IOException e) {
            throw new BuildException("Failed to create directory for revision.", e);
        }
    }

    /**
     * Creates scanner instance from specified configuration.
     * 
     * @param configuration
     * @return
     * @throws BuildException
     */
    private Scanner getScanner(Pair<List<String>, List<String>> configuration)
            throws BuildException {

        String[] expandedScanClassPaths = null;
        {
            String[] scanClassPaths = configuration.getLeft().toArray(new String[configuration.getLeft().size()]);

            logger.trace("Scan class path count [%s].", scanClassPaths.length);
            for (String classPath : scanClassPaths) {
                logger.trace("Scan class path [%s].", classPath);
            }

            try {
                expandedScanClassPaths = ClassPathExpander.expandClassPaths(scanClassPaths);
            } catch (FileNotFoundException e) {
                String msg = String.format("Failed to expand scan class paths: %s", e.getMessage());
                logger.warn(msg);
                throw new BuildException(msg, e);
            }

            logger.trace("Expanded scan class path count [%s].", expandedScanClassPaths.length);
            for (String expandedClassPath : expandedScanClassPaths) {
                logger.trace("Expanded scan class path [%s].", expandedClassPath);
            }
        }

        String[] scanPatterns = null;
        {
            scanPatterns = configuration.getRight().toArray(new String[configuration.getRight().size()]);

            logger.trace("Scan pattern count [%s].", scanPatterns.length);
            for (String pattern : scanPatterns) {
                logger.trace("Scan pattern [%s].", pattern);
            }
        }

        try {
            URLClassLoader myClassLoader = Utils.addClassPathItems(expandedScanClassPaths);
            return new Scanner(xml.getPath(), new File(outputDirectory, "info.xml").getPath(), scanPatterns, myClassLoader);

        } catch (IOException | ConversionException e) {
            throw new BuildException("Failed initialize scanner.", e);
        }
    }

    /**
     * Scans XML and classes.
     * 
     * @param scanner
     * @return
     * @throws BuildException
     */
    private Info scan(Scanner scanner)
            throws BuildException {
        try {
            return scanner.scan();
        } catch (ScannerException e) {
            throw new BuildException("Failed to scan project.", e);
        }
    }

    /**
     * Scans the project based on spl configuration xml. If it's needed it
     * builds the source of the project and runs the scanner on binaries.
     * 
     * @throws BuildException
     */
    public void call()
            throws BuildException {

        logger.info("Obtaining and scanning main revision of the main project.");
        Info scanInfo = loadInfo();
        Revision scanRevision = getRevisionToScan(scanInfo);
        Pair<List<String>, List<String>> scanConfiguration = getScanConfiguration(scanRevision);
        logger.info("Project obtained.");

        Scanner scanner = getScanner(scanConfiguration);
        Info scannedInfo = scan(scanner);
        scannedInfo.setMachine(targetMachine);
        logger.info("Project scanned.");

        // so the scan revision will not be checked twice
        getRevisionToScan(scannedInfo).setRevisionIdentification(scanRevision.getRevisionIdentification());

        resultInfo = scannedInfo;

    }

    /**
     * Returns scanned {@link Info}.
     * 
     * @return
     */
    public Info getInfo() {
        return resultInfo;
    }

    /**
     * Returns {@link IRepository} used to check out HEAD revision of THIS
     * project to scan.
     * 
     * @return
     */
    public Pair<Repository, IRepository> getRepository() {
        return repositoryPair;
    }

    /**
     * Returns directory of HEAD revision of THIS project which was scanned.
     * 
     * @return
     */
    public Pair<Revision, File> getRevision() {
        return revisionPair;
    }
}
