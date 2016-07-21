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
import java.net.URL;

import cz.cuni.mff.spl.deploy.store.exception.StoreException;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class HttpStoreFile implements IStoreReadonly.IStoreReadonlyDirectory.IStoreReadonlyFile {

    private final String name;
    private final URL    url;

    public HttpStoreFile(String name, URL url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public InputStream getInputStream()
            throws StoreException {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Checks existence of target URL.
     * 
     * @return True, if target URL really exists.
     */
    public boolean exists() {
        try (InputStream stream = this.url.openStream()) {
            // dummy call to solve compilation warning
            // resource servers its purpose just by opening
            stream.getClass();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
