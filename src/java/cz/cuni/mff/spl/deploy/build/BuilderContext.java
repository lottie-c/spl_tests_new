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
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.build.vcs.IRepository;
import cz.cuni.mff.spl.deploy.build.vcs.RepositoryFactory;
import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsCheckoutException;
import cz.cuni.mff.spl.deploy.build.vcs.exception.VcsParseException;
import cz.cuni.mff.spl.deploy.store.IStore;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.Pair;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.collection.StrictHashMap;
import cz.cuni.mff.spl.utils.collection.StrictMap;
import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.logging.SplLog;

/**
 * Based on scanned {@link Info} and prepared HEAD revision goes through all
 * {@link Measurement}s and marks those which are not yet measured and prepares
 * and builds revisions for code generation.
 * 
 * @author Frantisek Haas
 * @author Martin Lacina - repository checkout dependencies
 */
public class BuilderContext {

    private static final SplLog                  logger = Builder.logger;

    /** List of measurements that cannot be loaded and must be performed. */
    private final List<Measurement>              notMeasured;
    /** List of already performed measurements that will be just loaded. */
    private final List<Measurement>              alreadyMeasured;
    /**
     * List of measurements that cannot be performed due troubles with obtaining
     * the source revisions.
     */
    private final List<Measurement>              unableToMeasure;
    /** Mapping of {@link Revision} to local directories. */
    private final StrictMap<Revision, File>      revisionMap;
    /** Mapping of source URLs to {@link Repository}ies. */
    private final StrictMap<String, IRepository> repositoryMap;

    /** Object whose measurements are to be performed. */
    private final Info                           info;
    /** Data storage. */
    private final IStore                         store;
    /** XML configuration file. */
    private final File                           xml;
    /** Where to check out new revisions. */
    private final File                           sourceCodeDirectory;

    /** Object to interaction with user. */
    private final InteractiveInterface           interactive;
    /** Configuration. */
    private final ConfigurationBundle            config;
    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File                           localStoreRootDirectory;

    public BuilderContext(Info info, IStore store, File xml, File sourceCodeDirectory, Pair<Repository, IRepository> scannerRepositoryPair,
            Pair<Revision, File> scannerRevisionPair, InteractiveInterface interactive, ConfigurationBundle config, File localStoreRootDirectory) {

        this.notMeasured = new LinkedList<>();
        this.alreadyMeasured = new LinkedList<>();
        this.unableToMeasure = new LinkedList<>();
        this.revisionMap = new StrictHashMap<>();
        this.revisionMap.put(scannerRevisionPair.getLeft(), scannerRevisionPair.getRight());

        this.repositoryMap = new StrictHashMap<>();
        this.repositoryMap.put(scannerRepositoryPair.getLeft().getUrl(), scannerRepositoryPair.getRight());

        this.info = info;
        this.store = store;
        this.xml = xml;
        this.sourceCodeDirectory = sourceCodeDirectory;

        this.interactive = interactive;
        this.config = config;
        this.localStoreRootDirectory = localStoreRootDirectory;
    }

    /**
     * Returns not yet processed measurements without data in the store.
     * 
     * @return
     */
    public List<Measurement> getNotMeasured() {
        return notMeasured;
    }

    /**
     * Returns already processed measurements with data present in the store.
     * 
     * @return
     */
    public List<Measurement> getAlreadyMeasured() {
        return alreadyMeasured;
    }

    /**
     * Returns measurements witch can't be measured as generator or method code
     * is missing.
     * 
     * @return The unable to measure.
     */
    public List<Measurement> getUnableToMeasure() {
        return unableToMeasure;
    }

    /**
     * Returns map of checked of revisions. In case revision was not
     * successfully checked out, {@link null} is present. In case revision was
     * not needed because no measurement needs it.
     * 
     * @return
     */
    public StrictMap<Revision, File> getRevisionMap() {
        return revisionMap;
    }

    /**
     * Checks if measurement is already processed and stored or not.
     * 
     * @param measurement
     * @return
     */
    private boolean isMeasured(Measurement measurement) {
        try {
            if (store.measurementExists(new SampleIdentification(measurement))) {
                return true;
            } else {
                return false;
            }
        } catch (StoreException e) {
            logger.error(e, "Failed to check measurement existence.");
            return false;
        }
    }

    /**
     * Checks out specified revision. Uses already parsed repositories. Checks
     * out revision only if was not already checked out before. Stores revision
     * directory in the revision map.
     * 
     * @param repositoryMap
     * @param revision
     */
    private void checkout(Revision revision) {

        if (!repositoryMap.containsKey(revision.getRepository().getUrl())) {
            try {
                Repository repository = revision.getRepository();
                File cache = FileUtils.createUniqueDirectory(sourceCodeDirectory, "repository-cache", false);

                String type = repository.getType();
                String url = repository.getUrl();
                Map<String, String> values = config.getAccessConfig().getVcsValues(revision.getRepository().getRepositoryConfigurationSectionName());

                IRepository iRepository = RepositoryFactory.parse(type, url, values, xml, cache, interactive, localStoreRootDirectory);
                repositoryMap.put(repository.getUrl(), iRepository);

            } catch (VcsParseException e) {
                logger.error(e, "Failed to parse repository.");
            } catch (IOException e) {
                logger.error(e, "Failed to create cache directory for repository.");
            }
        }
        InvokedExecutionConfiguration.checkIfExecutionAborted();
        if (!revisionMap.containsKey(revision)) {
            try {
                IRepository vcs = repositoryMap.get(revision.getRepository().getUrl());
                File directory = FileUtils.createUniqueDirectory(sourceCodeDirectory, "revision", false);
                String id = vcs.checkout(revision.getValue(), directory);
                revision.setRevisionIdentification(id);

                logger.debug("Checked out revision [%s][%s][%s] into [%s].",
                        revision.getRepository().getType(), revision.getRepository().getUrl(), revision.getValue(), directory);

                try {
                    BuilderUtils.buildRevision(revision, directory, config);
                    revisionMap.put(revision, directory);
                } catch (BuildException e) {
                    logger.error(e, "Failed to build revision.");
                    revisionMap.put(revision, null);
                }

            } catch (VcsCheckoutException e) {
                logger.error(e, "Failed to checkout revision [%s].", revision.getDeclarationString());
                revisionMap.put(revision, null);
            } catch (IOException e) {
                logger.error(e, "Failed to create directory for revision.");
                revisionMap.put(revision, null);
            }
        }
    }

    /**
     * Counts all revision across all {@link Project}s in the info.
     * 
     * Counts also the integrated generator's project.
     * 
     * @return
     */
    private int revisionCount() {
        int revisionCount = 0;
        Iterator<Project> it = info.getProjects().iterator();
        while (it.hasNext()) {
            revisionCount += it.next().getRepository().getRevisions().size();
        }
        return revisionCount;
    }

    /**
     * Walks through all measurements and checks what needs to be measured and
     * checks out required code.
     */
    public void call() {
        logger.info("Obtaining code for resolving ids to check if to load or perform measurements [%d].", info.getMeasurements().size());
        logger.info("Repositories to possibly access [%d].", info.getProjects().size());
        logger.info("Revisions to possibly checkout [%d].", revisionCount());

        int measurementCount = 1;
        for (Measurement measurement : info.getMeasurements()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();

            // some measurements may have full identification already present
            if (isMeasured(measurement)) {
                alreadyMeasured.add(measurement);
                logger.info("[%" + Utils.magnitude(info.getMeasurements().size()) + "d/%d] - Already measured.", measurementCount, info.getMeasurements()
                        .size());
            } else {

                Revision gRevision = measurement.getGenerator().getRevision();
                Revision mRevision = measurement.getMethod().getRevision();

                String checkoutNotAvailableError = "";
                if (canBeCodeAvailable(gRevision, mRevision)) {
                    checkout(gRevision);
                    if (canBeCodeAvailable(gRevision, mRevision)) {
                        checkout(mRevision);
                        if (!canBeCodeAvailable(gRevision, mRevision)) {
                            checkoutNotAvailableError = "Method code not available.";
                        }
                    } else {
                        checkoutNotAvailableError = "Generator code not available.";
                    }
                } else {
                    checkoutNotAvailableError = "Generator or method code not available.";
                }

                // measurement can be already measured if revisions just needed
                // to have identifications resolved (such as git branch name to
                // revision hash)
                if (isMeasured(measurement)) {
                    alreadyMeasured.add(measurement);
                    measurement.getMeasurementState().setLastPhase(LastPhase.BUILD);
                    logger.info("[%" + Utils.magnitude(info.getMeasurements().size()) + "d/%d] - Already measured.", measurementCount, info.getMeasurements()
                            .size());
                } else if (canBeCodeAvailable(gRevision, mRevision)) {
                    notMeasured.add(measurement);
                    logger.info("[%" + Utils.magnitude(info.getMeasurements().size()) + "d/%d] - Will measure.", measurementCount, info.getMeasurements()
                            .size());
                } else {
                    unableToMeasure.add(measurement);
                    measurement.getMeasurementState().setLastPhase(LastPhase.BUILD);
                    measurement.getMeasurementState().setOk(false);
                    measurement.getMeasurementState().setMessage(checkoutNotAvailableError);
                    logger.info("[%" + Utils.magnitude(info.getMeasurements().size()) + "d/%d] - Unable to measure, missing code.", measurementCount, info
                            .getMeasurements()
                            .size());
                }
            }

            measurementCount++;
        }
    }

    /**
     * Checks if any of passed revisions have already failed.
     * <p>
     * Returns {@code true} when following holds for every passed revision:
     * Revision was checked out successfully, or revision was not checked out
     * yet. Otherwise returns {@code false}.
     * 
     * @param relatedRevisions
     *            The related revisions to check.
     * @return True, if code can be available.
     */
    private boolean canBeCodeAvailable(Revision... relatedRevisions) {
        for (Revision revision : relatedRevisions) {
            if (revisionMap.containsKey(revision) && revisionMap.get(revision) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * This class serves for recognizing repositories which can be cached and
     * reused.
     * 
     * @author Frantisek Haas
     * 
     */
    @SuppressWarnings("unused")
    private static class RepositoryEquals {

        private final Repository repository;

        public RepositoryEquals(Repository repository) {
            this.repository = repository;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            if (repository == null) {
                return result;
            }

            result = prime * result + ((repository.getType() == null) ? 0 : repository.getType().hashCode());
            result = prime * result + ((repository.getUrl() == null) ? 0 : repository.getUrl().hashCode());

            return result;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }

            if (object == null) {
                return false;
            }

            if (getClass() != object.getClass()) {
                return false;
            }

            RepositoryEquals o = (RepositoryEquals) object;

            if (!o.repository.getType().equals(this.repository.getType())) {
                return false;
            }

            if (!o.repository.getUrl().equals(this.repository.getUrl())) {
                return false;
            }

            return true;
        }
    }
}
