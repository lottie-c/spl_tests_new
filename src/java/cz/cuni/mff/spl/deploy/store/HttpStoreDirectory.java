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

import java.net.URL;
import java.util.LinkedList;
import java.util.Map.Entry;

import cz.cuni.mff.spl.deploy.store.IStoreReadonly.IStoreReadonlyDirectory;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.ssh.UnixFile;

/**
 * 
 * @see HttpStoreIndexDirectory
 * 
 * @author Frantisek Haas
 * 
 */
public class HttpStoreDirectory implements IStoreReadonly.IStoreReadonlyDirectory {

    private final String                  name;
    private final HttpStoreIndexDirectory index;

    public HttpStoreDirectory(String name, URL url)
            throws StoreException {
        this.name = name;
        this.index = new HttpStoreIndexDirectory(url);
    }

    @Override
    public IStoreReadonlyFile[] listFiles()
            throws StoreException {
        LinkedList<HttpStoreFile> files = new LinkedList<>();
        for (Entry<String, URL> f : index.getMap().entrySet()) {
            files.add(new HttpStoreFile(f.getKey(), f.getValue()));
        }
        return files.toArray(new HttpStoreFile[files.size()]);
    }

    @Override
    public IStoreReadonlyFile getFile(String name)
            throws StoreException {
        if (index.getMap().containsKey(name)) {
            return new HttpStoreFile(name, index.getMap().get(name));
        } else {
            throw new StoreException(String.format("File [%s] not found.", name));
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Finds HTTP store directory for any URL in it. Uses
     * {@link IStoreReadonly#STORE_ROOT_HELPER_FILE_NAME} to recognize store
     * root and
     * {@link IStoreReadonlyDirectory#EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME}
     * to recognize evaluation result directory.
     * 
     * @param anyUrlInStore
     *            The any URL in evaluation result directory.
     * @return The local store.
     * @throws StoreException
     *             The store exception is thrown when HTTP store location
     *             found, but its creation fails, or when HTTP store was not
     *             found at all.
     */
    public static HttpStoreDirectory findHttpEvaluationDirectory(URL anyUrlInStore) throws StoreException {
        UnixFile file = new UnixFile(anyUrlInStore.getPath());

        HttpStoreDirectory result = findHttpEvaluationDirectory(anyUrlInStore, file);
        if (result != null) {
            return result;
        } else {
            throw new StoreException(String.format("Unable to find http store for file [%s].", anyUrlInStore));
        }
    }

    /**
     * Implementation of {@link #findHttpStore(URL)} which does all the search
     * work.
     * 
     * @param anyUrlInStore
     *            The any URL in store.
     * @param directory
     *            The directory to search in.
     * @return The HTTP store when found, or {@code null} when not found.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails.
     */
    private static HttpStoreDirectory findHttpEvaluationDirectory(URL anyUrlInStore, UnixFile directory) throws StoreException {
        if (directory == null) {
            return null;
        }

        URL targetUrl = HttpStore.buildUrl(anyUrlInStore, new UnixFile(directory, EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME));
        HttpStoreFile targetFile = new HttpStoreFile(EVALUATION_RESULT_FOLDER_HELPER_FILE_NAME, targetUrl);
        if (targetFile.exists()) {
            URL directoryUrl = HttpStore.buildUrl(anyUrlInStore, directory);
            return new HttpStoreDirectory(targetUrl.getPath(), directoryUrl);
        } else {
            URL targetStoreUrl = HttpStore.buildUrl(anyUrlInStore, new UnixFile(directory, IStoreReadonly.STORE_ROOT_HELPER_FILE_NAME));
            HttpStoreFile targetStoreFile = new HttpStoreFile(IStoreReadonly.STORE_ROOT_HELPER_FILE_NAME, targetStoreUrl);
            if (targetStoreFile.exists()) {
                // stop search
                return null;
            } else {
                return findHttpEvaluationDirectory(anyUrlInStore, directory.getParent());
            }
        }
    }

}
