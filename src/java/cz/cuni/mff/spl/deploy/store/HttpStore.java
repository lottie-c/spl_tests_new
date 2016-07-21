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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.ssh.UnixFile;

/**
 * <p>
 * This class enabled access to measurement and evaluation files via HTTP
 * protocol.
 * </p>
 * 
 * <p>
 * HTTP does not offer directory listing by default and simple directory listing
 * at all. That's why this class depends on directory index created in a
 * well-known file inside every remotely accessible directory.
 * </p>
 * 
 * @see HttpStoreIndex
 * @see HttpStoreIndexDirectory
 * @see HttpStoreIndexMeasurement
 * 
 * @author Frantisek Haas
 * 
 */
public class HttpStore implements IStoreReadonly {

    private final HttpStoreIndexMeasurement measurementIndex;
    private final HttpStoreIndexDirectory   evaluationIndex;

    public HttpStore(URL root) throws StoreException {
        UnixFile measurementDirectory = new UnixFile(root.getPath(), StoreUtils.MEASUREMENT);
        UnixFile evaluationDirectory = new UnixFile(root.getPath(), StoreUtils.EVALUATION);
        URL measurement = buildUrl(root, measurementDirectory);
        URL evaluation = buildUrl(root, evaluationDirectory);

        this.measurementIndex = new HttpStoreIndexMeasurement(measurement);
        this.evaluationIndex = new HttpStoreIndexDirectory(evaluation);
    }

    public HttpStore(URL measurement, URL evaluation)
            throws IOException, StoreException {
        this.measurementIndex = new HttpStoreIndexMeasurement(measurement);
        this.evaluationIndex = new HttpStoreIndexDirectory(evaluation);
    }

    @Override
    public boolean measurementExists(SampleIdentification sid)
            throws StoreException {
        return measurementIndex.containsMeasurement(sid.getIdentification());
    }

    @Override
    public InputStream loadMeasurement(SampleIdentification sid)
            throws StoreException {
        try {
            return measurementIndex.getMeasurement(sid.getIdentification()).openStream();
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public IStoreReadonlyDirectory[] listEvaluationDirectories()
            throws StoreException {
        List<HttpStoreDirectory> directories = new LinkedList<>();
        for (Entry<String, URL> d : evaluationIndex.getMap().entrySet()) {
            directories.add(new HttpStoreDirectory(d.getKey(), d.getValue()));
        }
        return directories.toArray(new HttpStoreDirectory[directories.size()]);
    }

    /**
     * Finds local store for any file in it. Uses
     * {@link IStoreReadonly#STORE_ROOT_HELPER_FILE_NAME} to recognize store
     * root.
     * 
     * @param anyUrlInStore
     *            The any URL in store.
     * @return The local store.
     * @throws StoreException
     *             The store exception is thrown when local store location
     *             found, but its creation fails, or when local store was not
     *             found at all.
     */
    public static HttpStore findHttpStore(URL anyUrlInStore) throws StoreException {
        UnixFile file = new UnixFile(anyUrlInStore.getPath());

        HttpStore result = findHttpStoreImpl(anyUrlInStore, file);
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
    private static HttpStore findHttpStoreImpl(URL anyUrlInStore, UnixFile directory) throws StoreException {
        if (directory == null) {
            return null;
        }
        URL targetUrl = buildUrl(anyUrlInStore, new UnixFile(directory, STORE_ROOT_HELPER_FILE_NAME));
        HttpStoreFile targetFile = new HttpStoreFile(STORE_ROOT_HELPER_FILE_NAME, targetUrl);

        if (targetFile.exists()) {
            URL storeUrl = buildUrl(anyUrlInStore, directory);
            return new HttpStore(storeUrl);
        } else {
            return findHttpStoreImpl(anyUrlInStore, directory.getParent());
        }
    }

    /**
     * Builds the URL for same server as provider root URL and path specified by
     * provided Unix file instance.
     * 
     * @param root
     *            The root.
     * @param path
     *            The path.
     * @return The URL.
     * @throws StoreException
     *             The store exception.
     */
    static URL buildUrl(URL root, UnixFile path) throws StoreException {
        try {
            return new URL(root.getProtocol(), root.getHost(), root.getPort(), path.getPath());
        } catch (MalformedURLException e) {
            throw new StoreException(e);
        }
    }
}
