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
import java.io.IOException;
import java.util.LinkedList;

import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStoreReadonly.IStoreReadonlyDirectory;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.FileUtils;

/**
 * The {@link IStoreDirectory} implementation based on {@link java.io.File}.
 * 
 * @author Frantisek Haas
 * 
 */
public class LocalStoreDirectory implements IStore.IStoreDirectory {

    private final File                     directory;
    private final LocalStoreIndexDirectory index;

    public LocalStoreDirectory(File directory)
            throws StoreException {
        this.directory = directory;
        try {
            this.index = new LocalStoreIndexDirectory(directory);
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public IStoreFile createFile(String name)
            throws StoreException {
        File file = new File(directory, name);
        try {
            if (!file.createNewFile()) {
                throw new StoreException("File already exists.");
            }

        } catch (IOException e) {
            throw new StoreException("Could not create file.", e);
        }

        try {
            index.put(file.getName());
        } catch (IOException e) {
            throw new StoreException(e);
        }
        return new LocalStoreFile(file);
    }

    @Override
    public IStoreFile createUniqueFile(String prefix, String suffix)
            throws StoreException {
        File file = FileUtils.createUniqueFile(directory, prefix, suffix);
        try {
            index.put(file.getName());
        } catch (IOException e) {
            throw new StoreException(e);
        }
        return new LocalStoreFile(file);
    }

    @Override
    public IStoreFile[] listFiles()
            throws StoreException {
        LinkedList<IStoreFile> files = new LinkedList<>();
        for (File f : directory.listFiles()) {
            files.add(new LocalStoreFile(f));
        }
        return files.toArray(new IStoreFile[files.size()]);
    }

    @Override
    public IStoreFile getFile(String name)
            throws StoreException {
        File targetFile = new File(directory, name);
        if (targetFile.exists()) {
            return new LocalStoreFile(targetFile);
        } else {
            throw new StoreException(String.format("File [%s] does not exist .", targetFile.getAbsolutePath()));
        }
    }

    @Override
    public String getName() {
        return directory.getName();
    }

    /**
     * Finds local store directory for any file in it. Uses
     * {@link IStoreReadonly#STORE_ROOT_HELPER_FILE_NAME} to recognize store
     * root and
     * {@link IStoreReadonlyDirectory#EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME}
     * to recognize evaluation result directory.
     * 
     * @param anyFileInStore
     *            The any file in store.
     * @return The local store.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails, or when local store was not
     *             found at all.
     */
    public static LocalStoreDirectory findLocalEvaluationDirectory(File anyFileInStore) throws StoreException {
        anyFileInStore = anyFileInStore.getAbsoluteFile();
        LocalStoreDirectory result;
        if (anyFileInStore.isFile()) {
            result = findLocalEvaluationDirectoryImpl(anyFileInStore.getParentFile());
        } else {
            result = findLocalEvaluationDirectoryImpl(anyFileInStore);
        }
        if (result != null) {
            return result;
        } else {
            throw new StoreException(String.format("Unable to find local store for file [%s].", anyFileInStore));
        }
    }

    /**
     * Implementation of {@link #findLocalStoreDirectory(File)} which does all
     * the search work.
     * 
     * @param directory
     *            The directory to search in.
     * @return The local store when found, or {@code null} when not found.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails.
     */
    private static LocalStoreDirectory findLocalEvaluationDirectoryImpl(File directory) throws StoreException {
        if (directory == null) {
            return null;
        }
        if (new File(directory, IStoreReadonly.STORE_ROOT_HELPER_FILE_NAME).exists()) {
            return null;
        } else if (new File(directory, EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME).exists()) {
            return new LocalStoreDirectory(directory);
        } else {
            return findLocalEvaluationDirectoryImpl(directory.getParentFile());
        }
    }
}
