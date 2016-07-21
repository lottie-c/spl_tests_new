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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.utils.ssh.UnixFile;

/**
 * <p>
 * This class provides store directory index via HTTP.
 * </p>
 * 
 * <p>
 * Check {@link LocalStoreIndexDirectory} for index format.
 * </p>
 * 
 * @author Frantisek Haas
 * 
 */
public class HttpStoreIndexDirectory extends HttpStoreIndex {

    private final Map<String, URL> map = new HashMap<>();

    public HttpStoreIndexDirectory(URL indexedDirectory)
            throws StoreException {
        super(indexedDirectory);

        load();
    }

    @Override
    protected void processLine(String line, int lineNumber)
            throws StoreException {
        String[] split = line.split(StoreIndexUtils.SEPARATOR);
        if (split.length != 2) {
            throw new StoreException(String.format("Corrupted index entry on line %d.", lineNumber));
        }

        String file = StoreIndexUtils.decodeFile(split[0]);
        String end = split[1];

        if (!end.equals(lineEnd)) {
            throw new StoreException(String.format("Corrupted index entry on line %d.", lineNumber));
        }

        try {
            URL url = new URL(index.getProtocol(), index.getHost(), index.getPort(), new UnixFile(indexedDirectory.getPath(), file).getPath());
            map.put(file, url);
        } catch (MalformedURLException e) {
            throw new StoreException(e);
        }
    }

    /**
     * @return
     *         Loaded data from remote index. Key is file name. Value is file's
     *         URL.
     */
    public Map<String, URL> getMap() {
        return map;
    }
}
