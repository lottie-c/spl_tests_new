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
package cz.cuni.mff.spl.utils.ssh;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cz.cuni.mff.spl.utils.interactive.InteractiveInterface;
import cz.cuni.mff.spl.utils.interactive.InteractiveSilentConsole;

/**
 * 
 * @author Frantisek Haas
 * 
 */
public class Utils {

    public static Map<String, String> loadValuesAndSetInput(String file)
            throws IOException, URISyntaxException {
        Properties properties = new Properties();
        try (InputStream in = Utils.class.getResourceAsStream(file)) {
            if (in == null) {
                throw new NotFoundPropertiesException(String.format("Properties %s not found.", file));
            }

            properties.load(in);
        }

        Map<String, String> values = new HashMap<>();
        for (java.util.Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();

            if (key.equals("keyPath")) {
                values.put(key, getAbsolutePath(properties.getProperty("keyPath")));
            } else if (key.equals("knownHostsPath")) {
                values.put(key, getAbsolutePath(properties.getProperty("knownHostsPath")));
            } else {
                values.put(key, value);
            }
        }

        StringBuilder input = new StringBuilder();

        for (java.util.Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();

            if (key.equals("test-input")) {
                input.append(value);
                break;
            }
        }

        ByteArrayInputStream in = new ByteArrayInputStream(input.toString().getBytes());
        System.setIn(in);

        return values;
    }

    private static String getAbsolutePath(String path) throws URISyntaxException {
        URL url = Utils.class.getResource(path);
        if (url == null) {
            return path;
        } else {
            File asFile = new File(url.toURI());
            return asFile.getAbsolutePath();
        }
    }

    public static SshDetails createSshDetails(Map<String, String> values) {
        SshDetails details = null;
        InteractiveInterface console = new InteractiveSilentConsole();

        if (values.containsKey("url") && values.containsKey("username") && values.containsKey("keyPath")) {

            details = new SshDetails(
                    values.get("url"),
                    values.get("username"),
                    values.get("keyPath"),
                    Boolean.valueOf(values.get("trustAll")),
                    values.get("fingerprint"),
                    values.get("knownHostsPath"),
                    console);

        } else if (values.containsKey("url") && values.containsKey("username")) {
            details = new SshDetails(
                    values.get("url"),
                    values.get("username"),
                    Boolean.valueOf(values.get("trustAll")),
                    values.get("fingerprint"),
                    values.get("knownHostsPath"),
                    console);
        } else {
            throw new NotFoundPropertiesException("Machine information not set correctly.");
        }

        return details;
    }

    @SuppressWarnings("serial")
    public static class NotFoundPropertiesException extends RuntimeException {

        public NotFoundPropertiesException() {
            super();
        }

        public NotFoundPropertiesException(String message) {
            super(message);
        }

        public NotFoundPropertiesException(Throwable cause) {
            super(cause);
        }

        public NotFoundPropertiesException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
