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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Various helper functions that would be tedious to have separate classes for. */
public class Utils {

    /**
     * Get list of all (public, private, protected) methods in a class.
     * 
     * The private methods are automatically marked as accessible from
     * the outside.
     * 
     * @param c
     *            Class to scan.
     * @return Set of all methods.
     */
    public static Set<Method> getAllClassMethods(Class<?> c) {
        Method[] publicMethods = c.getMethods();
        Method[] declaredMethods = c.getDeclaredMethods();

        Set<Method> allMethods = new HashSet<>();

        for (Method m : publicMethods) {
            allMethods.add(m);
        }
        for (Method m : declaredMethods) {
            m.setAccessible(true);
            allMethods.add(m);
        }

        return allMethods;
    }

    /**
     * Create map from string description.
     * 
     * The input mapping describes single mapping in a single string, separated
     * by the provided separator.
     * 
     * On input <code>[ "a:alpha", "c:charlie" ]</code> the output would be
     * <code>{ "a" => "alpha", "c" => "charlie" }</code>.
     * 
     * @param mappings
     *            List of mappings.
     * @param sep
     *            Separator used in each mapping.
     * @return Map corresponding to the provided mapping.
     */
    public static Map<String, String> createMap(String[] mappings, String sep) {
        Map<String, String> dict = new HashMap<>();
        for (String m : mappings) {
            String[] parts = m.split(sep, 2);
            if (parts.length == 1) {
                dict.put(m, m);
            } else {
                dict.put(parts[0], parts[1]);
            }
        }
        return dict;
    }

    /**
     * Adds the items to the classPath of the system classLoader
     * 
     * @param cpItems
     *            classPath items to be added
     * @return extended URLClassLoader
     * 
     * @throws MalformedURLException
     */
    public static URLClassLoader addClassPathItems(String[] cpItems)
            throws MalformedURLException {
        return addClassPathItems(ClassLoader.getSystemClassLoader(), cpItems);
    }

    /**
     * Adds to the give classLoader the class path items
     * 
     * @param classLoader
     *            any class loader, you can use
     *            ClassLoader.getSystemClassLoader()
     * @param cpItems
     *            paths to items to be added
     * @return the extended URLClassLoader
     * 
     * @throws MalformedURLException
     */
    public static URLClassLoader addClassPathItems(ClassLoader classLoader, String[] cpItems)
            throws MalformedURLException {
        URL[] urls = new URL[cpItems.length];
        for (int i = 0; i < cpItems.length; ++i) {
            File f = new File(cpItems[i]);
            urls[i] = f.toURI().toURL();

        }
        return new URLClassLoader(urls, classLoader);
    }

    /**
     * <p>
     * Returns magnitude of the value to determine the reserved space for
     * logging progress to the value.
     * 
     * @param value
     * @return
     */
    public static int magnitude(long value) {
        return String.valueOf(value).length();
    }
}
