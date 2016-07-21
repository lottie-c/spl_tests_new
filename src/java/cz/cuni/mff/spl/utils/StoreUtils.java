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
package cz.cuni.mff.spl.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * <p>
 * Helper functions to work with {@link IStore} files.
 * 
 * @author Frantisek Haas
 * @author Martin Lacine
 * 
 */
public class StoreUtils {

    /**
     * Save to store file.
     * 
     * @param targetFile
     *            The target file.
     * @param string
     *            The string.
     * @throws StoreException
     *             The store exception.
     */
    public static void saveToStoreFile(IStoreFile targetFile, String string)
            throws StoreException {
        byte[] bytes = StringUtils.getStringBytes(string);
        StoreUtils.saveToStoreFile(targetFile, bytes);
    }

    /**
     * Save to store file.
     * 
     * @param targetFile
     *            The target file.
     * @param bytes
     *            The bytes.
     * @throws StoreException
     *             The store exception.
     */
    public static void saveToStoreFile(IStoreFile targetFile, byte[] bytes)
            throws StoreException {
        try {
            FileUtils.copy(new ByteArrayInputStream(bytes), targetFile.getOutputStream());
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

}
