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
package cz.cuni.mff.spl.evaluator.output;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * The implementation for mapping objects to generated files.
 * 
 * One file per one object is stored.
 * One file per one instance of {@link Measurement} / {@link Comparison} /
 * {@link Formula} is stored.
 * 
 * File extension and prefix are same for all generated files (each of them can
 * be empty string).
 * 
 * All added mapping entries are stored for the whole lifetime of this object.
 * 
 * @author Martin Lacina
 */
public class StoreBasicOutputFileMappingImpl implements BasicOutputFileMapping {

    /**
     * The file mapping for processed objects.
     * Allows to create links to measurements and comparisons generated files.
     */
    private final Map<Object, IStoreFile> streamMapping = new LinkedHashMap<>(); // new
                                                                                 // TreeMap<>();

    /** The directory to create new files in. */
    private final IStoreDirectory         directory;

    /**
     * Instantiates a new file mapping implementation.
     * 
     * @param directory
     *            The directory to create new files in.
     */
    public StoreBasicOutputFileMappingImpl(IStoreDirectory directory) {
        this.directory = directory;
    }

    @Override
    public IStoreFile getOutputFile(Object key, String prefix, String suffix) throws StoreException {
        IStoreFile result = this.streamMapping.get(key);

        if (result == null) {
            prefix = prefix != null ? prefix : "";
            suffix = suffix != null ? suffix : "";

            result = this.directory.createUniqueFile(prefix, suffix);

            this.streamMapping.put(key, result);
        }
        return result;
    }

    @Override
    public IStoreFile getIStoreFile(Object key) {
        return this.streamMapping.get(key);
    }

    @Override
    public Set<Object> getMappedObjects() {
        return Collections.unmodifiableSet(this.streamMapping.keySet());
    }

    @Override
    public boolean releaseIStoreFile(Object key) {
        return this.streamMapping.remove(key) != null;
    }

    @Override
    public void clearMapping() {
        this.streamMapping.clear();
    }

}
