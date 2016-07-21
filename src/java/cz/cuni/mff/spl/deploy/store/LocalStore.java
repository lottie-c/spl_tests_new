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
package cz.cuni.mff.spl.deploy.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.FileNameMapper;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.StreamUtils;
import cz.cuni.mff.spl.utils.lock.Lock;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * Implementation of {@link IStore} based on Java's {@link File}.
 * 
 * <p>
 * To enable remote access of stored files via HTTP protocol this class keeps
 * index files right next to stored measurement and evaluation files.
 * 
 * <p>
 * Local store can be accessed concurrently.
 * 
 * <p>
 * Evaluation directories are created atomically. Files inside them should be
 * written by a single process. Evaluation index is protected by the lock file.
 * 
 * <p>
 * Measurement directory writes are protected by the lock file. Every instance
 * wanting to write a measurement must acquire the lock. After the write is
 * finished the lock must be released.
 * 
 * <p>
 * Temporary directories are created atomically. However they are lock protected
 * so another won't clear them while they're used by other instance. On exit
 * functions as {@link #close()} or {@link #releaseTemporary()} should be
 * called.
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 * 
 */
public class LocalStore implements IStore, AutoCloseable {

    private static final SplLog              logger         = SplLogger.getLogger(LocalStore.class);

    /** The local store root directory or {@code null} when not available. */
    private File                             localStoreRootDirectory;
    private final File                       measurement;
    private final File                       measurementLock;
    private final File                       evaluation;
    private final File                       evaluationLock;
    private final File                       temporary;

    private final List<Lock>                 temporaryLocks = new LinkedList<>();

    private final LocalStoreIndexMeasurement measurementIndex;
    private final LocalStoreIndexDirectory   evaluationIndex;

    public LocalStore(File root)
            throws StoreException {
        this(new File(root, StoreUtils.MEASUREMENT), new File(root, StoreUtils.EVALUATION), new File(root, StoreUtils.TEMPORARY));

        localStoreRootDirectory = root;

        File storeRootHelperFile = new File(root, STORE_ROOT_HELPER_FILE_NAME);
        if (!storeRootHelperFile.exists()) {
            try {
                storeRootHelperFile.createNewFile();
            } catch (IOException e) {
                logger.error(e, "Unable to create store root helper file [%s]", storeRootHelperFile);
            }
        }
    }

    /**
     * Instantiates a new local store.
     * 
     * @param measurementFolder
     *            The measurement folder.
     * @param evaluationFolder
     *            The evaluation folder.
     * @param temporaryFolder
     *            The temporary folder.
     * @throws StoreException
     *             The store exception.
     */
    public LocalStore(File measurementFolder, File evaluationFolder, File temporaryFolder)
            throws StoreException {
        /**
         * <p>
         * since locks and waiting was added create some init function so
         * constructor cannot take long time to finish
         */

        this.measurement = measurementFolder;
        this.measurementLock = new File(this.measurement, StoreUtils.LOCK_FILE_NAME);
        this.evaluation = evaluationFolder;
        this.evaluationLock = new File(this.evaluation, StoreUtils.LOCK_FILE_NAME);
        this.temporary = temporaryFolder;

        try {
            if (!measurement.exists()) {
                FileUtils.createDirectory(measurement);
            }

            if (!evaluation.exists()) {
                FileUtils.createDirectory(evaluation);
            }

            if (!temporary.exists()) {
                FileUtils.createDirectory(temporary);
            }

        } catch (IOException e) {
            throw new StoreException(e);
        }

        // first call creates the measurementLock file itself
        try (Lock lock = Lock.waitForLock(measurementLock)) {
            lock.dummy();
            try {
                this.measurementIndex = new LocalStoreIndexMeasurement(measurement);

            } catch (IOException e) {
                throw new StoreException(e);
            }
        }

        // first call creates the evaluationLock file itself
        try (Lock lock = Lock.waitForLock(evaluationLock)) {
            lock.dummy();
            try {
                this.evaluationIndex = new LocalStoreIndexDirectory(evaluation);

            } catch (IOException e) {
                throw new StoreException(e);
            }

            // list to validate or build indices if validation fails
            // listEvaluationDirectories();
        }
    }

    /**
     * <p>
     * Releases all locks on temporary directories so they can be later deleted.
     * 
     */
    @Override
    public void close() {
        releaseTemporary();
    }

    /**
     * Inner locking free implementation.
     * 
     * @param sid
     * @return
     * @throws StoreException
     */
    private boolean measurementExistsInner(SampleIdentification sid)
            throws StoreException {
        logger.trace("Checking measurement existence [%s].", sid);
        File[] files = FileUtils.listFiles(measurement, sid.getFileNamePrefix(), FileNameMapper.getMeasurementDataFileNameExtension());

        for (File f : files) {
            try {
                String line = StreamUtils.readFirstStreamLineToString(new FileInputStream(f));
                if (sid.getIdentification().equals(line)) {
                    logger.trace("Found measurement in [%s].", f);
                    logger.trace("Checking measurement existence succeeded [%s].", sid);
                    return true;
                } else {
                    logger.trace("Not found measurement in [%s].", f);
                }
            } catch (IOException e) {
                throw new StoreException(e);
            }

        }

        logger.trace("Checking measurement existence failed [%s].", sid);
        return false;
    }

    @Override
    public boolean measurementExists(SampleIdentification sid)
            throws StoreException {
        try (Lock lock = Lock.waitForLock(measurementLock)) {
            lock.dummy();
            return measurementExistsInner(sid);
        }
    }

    @Override
    public void saveMeasurement(InputStream stream, SampleIdentification sid)
            throws StoreException {
        try (Lock lock = Lock.waitForLock(measurementLock)) {
            lock.dummy();

            if (measurementExistsInner(sid)) {
                throw new StoreException("Measurement already exists: " + sid.getIdentification());
            }

            try {
                File file = FileUtils.createUniqueFile(measurement, sid.getFileNamePrefix(), FileNameMapper.getMeasurementDataFileNameExtension());
                FileUtils.copy(stream, file);
                measurementIndex.put(file.getName(), sid.getIdentification());
            } catch (IOException e) {
                throw new StoreException(e);
            }
        }
    }

    @Override
    public InputStream loadMeasurement(SampleIdentification sid)
            throws StoreException {
        try (Lock lock = Lock.waitForLock(measurementLock)) {
            lock.dummy();

            File[] files = FileUtils.listFiles(measurement, sid.getFileNamePrefix(), FileNameMapper.getMeasurementDataFileNameExtension());
            for (File f : files) {
                try {
                    String line = StreamUtils.readFirstStreamLineToString(new FileInputStream(f));
                    if (sid.getIdentification().equals(line)) {
                        return new FileInputStream(f);
                    }
                } catch (IOException e) {
                    throw new StoreException(e);
                }
            }

            throw new StoreException("Measurement does not exist.");
        }
    }

    @Override
    public IStoreDirectory createEvaluationDirectory(String prefix)
            throws StoreException {
        try (Lock lock = Lock.waitForLock(evaluationLock)) {
            lock.dummy();

            try {
                File directory = FileUtils.createUniqueAtomicDirectory(evaluation, prefix);

                evaluationIndex.put(directory.getName());
                File storeHelperFile = new File(directory, IStoreReadonlyDirectory.EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME);
                if (!storeHelperFile.exists()) {
                    try {
                        storeHelperFile.createNewFile();
                    } catch (IOException e) {
                        logger.error(e, "Unable to create evaluation result helper file [%s]", storeHelperFile);
                    }
                }
                return new LocalStoreDirectory(directory);

            } catch (IOException e) {
                throw new StoreException(e);
            }
        }
    }

    @Override
    public IStoreDirectory[] listEvaluationDirectories()
            throws StoreException {
        try (Lock lock = Lock.waitForLock(evaluationLock)) {
            lock.dummy();

            LinkedList<IStoreDirectory> directories = new LinkedList<>();

            for (File f : evaluation.listFiles()) {
                if (f.isDirectory()) {
                    directories.add(new LocalStoreDirectory(f));
                }
            }

            return directories.toArray(new IStoreDirectory[directories.size()]);
        }
    }

    /**
     * Creates local unique temporary directory with specified prefix.
     * 
     * @param prefix
     * @return
     */
    public File createTemporaryDirectory(String prefix)
            throws StoreException {
        try {
            File tmpDir = FileUtils.createUniqueAtomicDirectory(temporary, prefix);
            File lockFile = new File(tmpDir, StoreUtils.LOCK_FILE_NAME);
            Lock lock = Lock.tryLock(lockFile);

            if (!lock.isLocked()) {
                throw new StoreException("Failed to create the temporary directory. Failed to lock it.");
            }

            temporaryLocks.add(lock);
            return tmpDir;

        } catch (IOException e) {
            throw new StoreException("Failed to create temporary directory.", e);
        }
    }

    /**
     * Creates local unique temporary file with specified prefix and suffix.
     * 
     * @param prefix
     * @return
     * @throws StoreException
     */
    public File createTemporaryFile(String prefix, String suffix)
            throws StoreException {
        try {
            File tmpDir = createTemporaryDirectory(prefix);
            return FileUtils.createUniqueAtomicFile(tmpDir, prefix, suffix);
        } catch (IOException e) {
            throw new StoreException("Failed to create temporary file.", e);
        }
    }

    /**
     * Releases lock on all temporary directories and files.
     */
    public void releaseTemporary() {
        for (Lock lock : temporaryLocks) {
            lock.close();
        }
        temporaryLocks.clear();
    }

    /**
     * Clears all unused temporary directories.
     */
    public void clearTemporary() {
        releaseTemporary();

        for (File tmpDir : temporary.listFiles()) {
            if (!Lock.isLocked(new File(tmpDir, StoreUtils.LOCK_FILE_NAME))) {
                try {
                    FileUtils.deleteAll(tmpDir);
                } catch (IOException e) {
                    logger.error(e, "Failed to clear temporary directory [%s].", tmpDir);
                }
            }
        }
    }

    /**
     * Finds local store for any file in it. Uses
     * {@link IStoreReadonly#STORE_ROOT_HELPER_FILE_NAME} to recognize store
     * root.
     * 
     * @param anyFileInStore
     *            The any file in store.
     * @return The local store.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails, or when local store was not
     *             found at all.
     */
    public static LocalStore findLocalStore(File anyFileInStore) throws StoreException {
        anyFileInStore = anyFileInStore.getAbsoluteFile();
        LocalStore result;
        if (anyFileInStore.isFile()) {
            result = findLocalStoreImpl(anyFileInStore.getParentFile());
        } else {
            result = findLocalStoreImpl(anyFileInStore);
        }
        if (result != null) {
            return result;
        } else {
            throw new StoreException(String.format("Unable to find local store for file [%s].", anyFileInStore));
        }
    }

    /**
     * Gets the local store root directory or {@code null} when it is not
     * available (local store was not created using
     * {@link LocalStore#LocalStore(File)} constructor).
     * 
     * @return The local store root directory or {@code null}.
     */
    public File getLocalStoreRootDirectory() {
        return localStoreRootDirectory;
    }

    /**
     * Implementation of {@link #findLocalStore(File)} which does all the search
     * work.
     * 
     * @param directory
     *            The directory to search in.
     * @return The local store when found, or {@code null} when not found.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails.
     */
    private static LocalStore findLocalStoreImpl(File directory) throws StoreException {
        if (directory == null) {
            return null;
        }
        if (new File(directory, STORE_ROOT_HELPER_FILE_NAME).exists()) {
            return new LocalStore(directory);
        } else {
            return findLocalStoreImpl(directory.getParentFile());
        }
    }
}
