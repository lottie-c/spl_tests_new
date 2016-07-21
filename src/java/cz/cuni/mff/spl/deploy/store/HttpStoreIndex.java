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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.ssh.UnixFile;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public abstract class HttpStoreIndex {

    protected final String   lineEnd = "~";
    protected final URL      index;
    protected final UnixFile indexedDirectory;

    public HttpStoreIndex(URL indexedDirectory)
            throws StoreException {
        this.indexedDirectory = new UnixFile(indexedDirectory.getPath());

        try {
            this.index = new URL(
                    indexedDirectory.getProtocol(), indexedDirectory.getHost(), indexedDirectory.getPort(),
                    new UnixFile(indexedDirectory.getPath(), StoreIndexUtils.INDEX_FILE_NAME).getPath());
        } catch (MalformedURLException e) {
            throw new StoreException(e);
        }
    }

    protected abstract void processLine(String line, int lineNumber)
            throws StoreException;

    protected void load()
            throws StoreException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(index.openStream()))) {
            String line = in.readLine();
            int lineNumber = 0;

            while (line != null) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();

                lineNumber++;

                processLine(line, lineNumber);

                line = in.readLine();

            }
        } catch (IOException e) {
            throw new StoreException(e);
        }
    }
}
