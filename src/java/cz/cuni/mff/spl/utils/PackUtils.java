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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Class finds resources (class, java, other...) using standard JVM or custom
 * class loader and packs the resources to the executable jar.
 * 
 * @author Frantisek Haas
 * 
 */
public class PackUtils {

    private static final SplLog logger = SplLogger.getLogger(PackUtils.class);

    private enum Kind {
        RESOURCE, DIRECTORY, RECURSIVE;
    }

    private class Entry {

        private final String      path;
        private final String      newPath;
        private final Kind        kind;
        private final ClassLoader classLoader;

        public Entry(String path, Kind kind, ClassLoader classLoader) {
            this.path = path;
            this.newPath = path;
            this.kind = kind;
            this.classLoader = classLoader;
        }

        /**
         * This constructor enables renaming resource. This constructor should
         * be used only directory be the user code to add a single entry. Expand
         * functions should not use it yet (renaming whole trees not yet).
         * 
         * @param path
         * @param newPath
         * @param kind
         * @param classLoader
         */
        public Entry(String path, String newPath, Kind kind, ClassLoader classLoader) {
            this.path = path;
            this.newPath = newPath;
            this.kind = kind;
            this.classLoader = classLoader;
        }

        public String getPath() {
            return path;
        }

        public String getNewPath() {
            return newPath;
        }

        public Kind getKind() {
            return kind;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @Override
        public String toString() {
            if (path.equals(newPath)) {
                return String.format("%s", path);
            } else {
                return String.format("===>>>%s===>>>%s", path, newPath);
            }
        }
    }

    private static final String URL_PROTOCOL_JAR         = "jar";
    private static final String URL_PROTOCOL_FILE        = "file";
    private static final String JAR_TO_PACKAGE_SEPARATOR = "!";
    private static final String MANIFEST_VERSION         = "1.0";
    private static final String SEPARATOR                = "/";

    private String              mainClass;
    private final Set<Entry>    entrySet;

    private ClassLoader         classLoader;

    public PackUtils() {
        entrySet = new HashSet<>();
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    public PackUtils setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    @Deprecated
    public PackUtils setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public PackUtils addResource(String resourcePath) {
        entrySet.add(new Entry(resourcePath, Kind.RESOURCE, classLoader));
        return this;
    }

    /**
     * This function allows user to save a specific resource under a different
     * name in the new jar file.
     * 
     * @param resourcePath
     *            The current path of the resource.
     * @param newResourcePath
     *            The new path the resource will be written to.
     * @return
     */
    public PackUtils addResourceAs(String resourcePath, String newResourcePath) {
        entrySet.add(new Entry(resourcePath, newResourcePath, Kind.RESOURCE, classLoader));
        return this;
    }

    public PackUtils addAllInDirectory(String directoryPath)
            throws IOException {
        if (!directoryPath.endsWith(SEPARATOR)) {
            throw new IOException("Directory path must end with separator.");
        }

        entrySet.add(new Entry(directoryPath, Kind.DIRECTORY, classLoader));
        return this;
    }

    public PackUtils addAllInDirectoryRecursively(String directoryPath)
            throws IOException {
        if (!directoryPath.endsWith(SEPARATOR)) {
            throw new IOException("Directory path must end with separator.");
        }

        entrySet.add(new Entry(directoryPath, Kind.RECURSIVE, classLoader));
        return this;
    }

    @Deprecated
    private void expandRootEntry(Entry search, Set<Entry> searchSet, Map<String, Entry> resourceMap) {
        // don't know if it's even possible to expand root entry
    }

    private void expandJarProtocol(URL url, Entry search, Set<Entry> searchSet, Map<String, Entry> resourceMap)
            throws IOException, URISyntaxException {
        // based on the resource found this line extracts the path of the jar
        // containing the resource
        String jarPath = url.getPath().split(JAR_TO_PACKAGE_SEPARATOR)[0];
        URI jarUri = new URI(jarPath);
        @SuppressWarnings("resource")
        JarFile jarFile = new JarFile(jarUri.getPath());

        Enumeration<JarEntry> entries = jarFile.entries();

        // jar lists all files, up and down the directory tree
        while (entries.hasMoreElements()) {
            JarEntry resourceFile = entries.nextElement();

            switch (search.getKind()) {
                case RESOURCE:
                    throw new IOException("Kind not supported.");

                case DIRECTORY:
                    // check if the resource path starts with the specified
                    // directory path but does not contain any more directory
                    // separators
                    String directoryName = resourceFile.getName();
                    String restOfPath = resourceFile.getName().substring(directoryName.length());

                    if (directoryName.startsWith(search.getPath()) && !restOfPath.contains(SEPARATOR)) {
                        Entry newResource = new Entry(resourceFile.getName(), Kind.RESOURCE, search.getClassLoader());
                        resourceMap.put(newResource.getPath(), newResource);
                    }
                    break;

                case RECURSIVE:
                    // check if resource path starts with directory path
                    // specified
                    if (resourceFile.getName().startsWith(search.getPath())) {
                        Entry newResource = new Entry(resourceFile.getName(), Kind.RESOURCE, search.getClassLoader());
                        resourceMap.put(newResource.getPath(), newResource);
                    }
                    break;

                default:
                    throw new IOException("Kind unknown.");
            }
        }
    }

    private void expandFileProtocol(URL url, Entry search, Set<Entry> searchSet, Map<String, Entry> resourceMap)
            throws IOException {
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        if (file.isDirectory()) {

            File[] files = file.listFiles();
            for (File resourceFile : files) {

                if (!resourceFile.isDirectory()) {
                    Entry newResource = new Entry(search.getPath() + resourceFile.getName(), Kind.RESOURCE, search.getClassLoader());
                    resourceMap.put(newResource.getPath(), newResource);
                }

                if (resourceFile.isDirectory() && search.getKind() == Kind.RECURSIVE) {
                    Entry newSearch = new Entry(search.getPath() + resourceFile.getName() + SEPARATOR, Kind.RECURSIVE, search.getClassLoader());
                    searchSet.add(newSearch);
                }
            }
        }
    }

    private void expandEntry(Entry search, Set<Entry> searchSet, Map<String, Entry> resourceMap)
            throws IOException, URISyntaxException {

        logger.trace("Expand resource [%s].", search.getPath());

        if (search.getPath().equals(SEPARATOR)) {
            expandRootEntry(search, searchSet, resourceMap);
            return;
        }

        URL url = search.getClassLoader().getResource(search.getPath());
        if (url == null) {
            throw new IOException("Could not expand resource " + search.getPath() + ".");
        }

        // handle case when resource is packed in jar
        if (url.getProtocol().equals(URL_PROTOCOL_JAR)) {
            expandJarProtocol(url, search, searchSet, resourceMap);
        }

        // handle case when resource is in directory structure
        else if (url.getProtocol().equals(URL_PROTOCOL_FILE)) {
            expandFileProtocol(url, search, searchSet, resourceMap);
        }

        else {
            throw new IOException("Packer does not support following URL protocol " + url.getProtocol() + ".");
        }

    }

    private InputStream getResource(Entry entry) {
        InputStream inputStream = entry.getClassLoader().getResourceAsStream(entry.getPath());
        return inputStream;
    }

    /**
     * Writes a single resource file into jar output stream.
     * 
     * @param resource
     *            The resource to write to the stream.
     * @param jarOutputStream
     *            The jar's output stream to write to.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private void writeResourceToJar(Entry resource, JarOutputStream jarOutputStream)
            throws IOException {

        logger.trace("Write resource [%s].", resource);

        JarEntry jarEntry = new JarEntry(resource.getNewPath());
        jarOutputStream.putNextEntry(jarEntry);

        InputStream resourceInputStream = getResource(resource);
        if (resourceInputStream == null) {
            throw new IOException("Resource " + resource.getPath() + " not found.");
        }

        final int BUFFER_SIZE = 4096;
        final int EOF = -1;

        byte[] buffer = new byte[BUFFER_SIZE];
        int length = resourceInputStream.read(buffer);
        while (length != EOF) {
            jarOutputStream.write(buffer, 0, length);
            length = resourceInputStream.read(buffer);
        }

        jarOutputStream.closeEntry();
    }

    /**
     * Expands specified paths based on configuration and writes all directly
     * specified or expanded resources to the stream.
     * 
     * @param outputStream
     *            The output stream to write to.
     * @throws IOException
     * @throws URISyntaxException
     */
    public void write(OutputStream outputStream)
            throws IOException, URISyntaxException {

        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION);
        attributes.put(Attributes.Name.MAIN_CLASS, mainClass);

        try (JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
            Set<Entry> searchSet = new HashSet<>();
            Map<String, Entry> resourceMap = new TreeMap<>();

            // resource type entry are ready for writing but directory kind a
            // recursive kind needs expansion
            for (Entry entry : entrySet) {
                switch (entry.getKind()) {
                    case RESOURCE:
                        resourceMap.put(entry.getPath(), entry);
                        break;
                    default:
                        searchSet.add(entry);
                }
            }

            // expand resources that need to be expanded
            while (searchSet.iterator().hasNext()) {
                Entry search = searchSet.iterator().next();
                searchSet.remove(search);
                expandEntry(search, searchSet, resourceMap);
            }

            for (Entry resource : resourceMap.values()) {
                writeResourceToJar(resource, jarOutputStream);
            }
        }
    }

    public void write(File file)
            throws IOException, URISyntaxException {
        try (FileOutputStream jarStream = new FileOutputStream(file)) {
            write(jarStream);
        }
    }
}
