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

import java.util.Set;

import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * The Interface for mapping objects to generated output streams.
 * 
 * @author Martin Lacina
 */
public interface BasicOutputFileMapping {

    /**
     * Gets the output stream for specified key.
     * For specified key returns same file in every call based on
     * 
     * @param key
     *            The key to access generated file.
     * @param prefix
     *            The file name prefix.
     * @param suffix
     *            The file name suffix.
     * @return The mapped output stream.
     * @throws StoreException
     *             The store exception. {@code equals()} method.
     *             When no output stream has been mapped, than new output stream
     *             will be
     *             mapped for specified key using provided prefix and suffix.
     */
    IStoreFile getOutputFile(Object key, String prefix, String suffix) throws StoreException;

    /**
     * Gets the mapped stream for specified key.
     * For specified key returns same file in every call based on
     * {@code equals()} method.
     * When no output stream was mapped, than {@code null} is returned.
     * 
     * @param key
     *            The key to get file for.
     * @return The mapped output file.
     */
    IStoreFile getIStoreFile(Object key);

    /**
     * Releases mapped output stream for specified key.
     * 
     * @param key
     *            The key to release mapping for.
     * @return True, if key was mapped to output stream.
     */
    boolean releaseIStoreFile(Object key);

    /**
     * Clears all created mappings.
     */
    void clearMapping();

    /**
     * Gets the mapped objects. Returned set is not to be modified.
     * 
     * @return The mapped objects.
     */
    Set<Object> getMappedObjects();

}
