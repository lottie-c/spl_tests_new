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
package cz.cuni.mff.spl.reflection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import cz.cuni.mff.spl.StringFilter;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/*
 * The implementation was greatly inspired by posts on StackOverflow:
 * http://stackoverflow.com/questions/205573/at-runtime-find-all-classes-in-a-java-application-that-extend-a-base-class
 * http://stackoverflow.com/questions/176527/how-can-i-enumerate-all-classes-in-a-package-and-add-them-to-a-list
 */

/** Listing of all loaded classes. */
public class ClassListing {
    private static SplLog LOGGER = SplLogger.getLogger(ClassListing.class);

    private StringFilter  filter;

    /**
     * Create new instance with given filtering.
     * 
     * @param f
     *            Filter for class names.
     */
    public ClassListing(StringFilter f) {
        if (f != null) {
            filter = f;
        } else {
            filter = new NullFilter();
        }
    }

    /** Create new instance listing all classes. */
    public ClassListing() {
        filter = new NullFilter();
    }

    /**
     * Get list of all known classes wrt filter.
     * 
     * @return List of classes (fully qualified names).
     * @throws URISyntaxException
     */
    public List<String> getAll(URL[] classPath) {
        List<String> result = new LinkedList<>();

        String[] javacp = System.getProperty("java.class.path", "").split(System.getProperty("path.separator", ":"));
        List<String> cp = new ArrayList<String>(javacp.length + classPath.length);

        for (URL url : classPath) {
            URI uri;
            try {
                uri = url.toURI();
                cp.add(uri.getPath());
            } catch (URISyntaxException e) {
                LOGGER.warn(e, "The url '%s' could not be converted to URI. Skipping this URL", url);
            }
        }

        for (String s : javacp) {
            cp.add(s);
        }

        getAllFromClasspath(cp, result);
        return result;
    }

    /**
     * Get list of all known classes wrt filter residing on given classpath.
     * 
     * @param classpath
     *            List of classpath to scan.
     * @param list
     *            List to which add found classes.
     */
    public void getAllFromClasspath(List<String> classpath, List<String> list) {
        for (String cp : classpath) {
            try {
                if (cp.endsWith(".jar")) {
                    getAllFromJar(cp, list);
                } else if (!cp.isEmpty()) {
                    getAllFromDirectory(cp, list);
                }
            } catch (Exception e) {
                /*
                 * Silently ignore. User probably do not even know how to
                 * set-up the $CLASSPATH to not point to nonexistent JARs
                 * and directories.
                 */
            }
        }
    }

    protected void getAllFromJar(String jarname, List<String> list) throws FileNotFoundException, IOException {
        File jar = new File(jarname);
        try (JarInputStream is = new JarInputStream(new FileInputStream(jar))) {
            while (true) {
                JarEntry entry = is.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                addToListIfClass(entry.getName(), list);
            }
        }
    }

    protected void getAllFromDirectory(String dirname, List<String> list) {
        getAllFromDirectory(new File(dirname), "", list);
    }

    protected void getAllFromDirectory(File dir, String prefix, List<String> list) {
        if (!dir.isDirectory()) {
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                getAllFromDirectory(f, prefix + f.getName() + "/", list);
            } else {
                addToListIfClass(prefix + f.getName(), list);
            }
        }
    }

    protected void addToListIfClass(String name, List<String> list) {
        if (!name.endsWith(".class")) {
            return;
        }
        name = name.substring(0, name.length() - ".class".length()).replace("/", ".");
        if (filter.match(name)) {
            list.add(name);
        }
    }

    private class NullFilter implements StringFilter {
        @Override
        public boolean match(String s) {
            return true;
        }
    }
}
