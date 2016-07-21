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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * The {@link IStoreFile} implementation based on {@link java.io.File}.
 * 
 * @author Frantisek Haas
 * 
 */
public class LocalStoreFile implements IStore.IStoreDirectory.IStoreFile {

    private final File file;

    public LocalStoreFile(File file)
            throws StoreException {
        this.file = file;
    }

    @Override
    public OutputStream getOutputStream() throws StoreException {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new StoreException(String.format("Failed to create data stream from file [%s].", file), e);
        }
    }

    @Override
    public InputStream getInputStream()
            throws StoreException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new StoreException(String.format("Failed create data strom from file [%s].", file), e);
        }
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public boolean delete() throws StoreException {
        try {
            return file.delete();
        } catch (SecurityException e) {
            throw new StoreException(String.format("Failed delete file [%s].", file), e);
        }

    }
}
