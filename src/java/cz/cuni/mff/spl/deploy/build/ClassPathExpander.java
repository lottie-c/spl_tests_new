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
package cz.cuni.mff.spl.deploy.build;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * This class expands class paths in the project's built revisions. Expansion is
 * based on very simple wild cards. Important thing is expansion works only on
 * already built revision where files and directories to expanded on already
 * exist.
 * 
 * @author Frantisek Haas
 * 
 */
public class ClassPathExpander {

    public static final String EXPAND_JAR_RECURSIVELY = "**.jar";
    public static final String EXPAND_JAR_DIRECTORY   = "*.jar";
    public static final String JAR_SUFFIX             = ".jar";

    /**
     * Do not expands class path. Just check if file exists.
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    private static File noExpand(File file)
            throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("File [%s] does not exist.", file));
        }
        return file;
    }

    /**
     * Finds all jars in the directory and all its subdirectories.
     * 
     * @param directory
     *            The directory where to start expanding.
     * @return
     * @throws FileNotFoundException
     * 
     */
    private static List<File> expandJarRecursively(File directory)
            throws FileNotFoundException {
        LinkedList<File> jars = new LinkedList<>();

        if (!directory.exists()) {
            throw new FileNotFoundException(String.format("Directory [%s] does not exist.", directory));
        }

        jars.addAll(expandJarDirectory(directory));

        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                jars.addAll(expandJarRecursively(f));
            }
        }

        return jars;
    }

    /**
     * Finds all jars in the directory.
     * 
     * @param directory
     *            The directory to expand in.
     * @return
     * @throws FileNotFoundException
     */
    private static List<File> expandJarDirectory(File directory)
            throws FileNotFoundException {
        LinkedList<File> jars = new LinkedList<>();

        if (!directory.exists()) {
            throw new FileNotFoundException(String.format("Directory [%s] does not exist.", directory));
        }

        for (File f : directory.listFiles()) {
            if (!f.isDirectory() && f.getName().endsWith(JAR_SUFFIX)) {
                jars.add(f);
            }
        }

        return jars;
    }

    /**
     * <p>
     * Expand class paths. This function expands those class paths that contains
     * known wildcards.
     * </p>
     * 
     * <ul>
     * <li>{@link ClassPathExpander#EXPAND_JAR_RECURSIVELY}</li>
     * <li>{@link ClassPathExpander#EXPAND_JAR_DIRECTORY}</li>
     * </ul>
     * 
     * @param classPaths
     * @return
     * @throws FileNotFoundException
     */
    public static String[] expandClassPaths(String[] classPaths)
            throws FileNotFoundException {

        List<File> files = new LinkedList<>();

        for (String classPath : classPaths) {

            if (classPath.endsWith(EXPAND_JAR_RECURSIVELY)) {
                String path = classPath.substring(0, classPath.length() - EXPAND_JAR_RECURSIVELY.length());
                files.addAll(expandJarRecursively(new File(path)));

            } else if (classPath.endsWith(EXPAND_JAR_DIRECTORY)) {
                String path = classPath.substring(0, classPath.length() - EXPAND_JAR_DIRECTORY.length());
                files.addAll(expandJarDirectory(new File(path)));

            } else {
                files.add(noExpand(new File(classPath)));
            }
        }

        List<String> result = new LinkedList<>();
        for (File f : files) {
            result.add(f.getPath());
        }

        return result.toArray(new String[result.size()]);
    }

    /**
     * Does the same as {@link #expandClassPaths(String[])} but converts paths
     * to actual {@link File}s.
     * 
     * @param classPaths
     * @return
     * @throws FileNotFoundException
     */
    public static File[] expandClassPathsToFiles(String[] classPaths)
            throws FileNotFoundException {

        List<File> files = new LinkedList<>();

        for (String classPath : classPaths) {

            if (classPath.endsWith(EXPAND_JAR_RECURSIVELY)) {
                String path = classPath.substring(0, classPath.length() - EXPAND_JAR_RECURSIVELY.length());
                files.addAll(expandJarRecursively(new File(path)));

            } else if (classPath.endsWith(EXPAND_JAR_DIRECTORY)) {
                String path = classPath.substring(0, classPath.length() - EXPAND_JAR_DIRECTORY.length());
                files.addAll(expandJarDirectory(new File(path)));

            } else {
                files.add(noExpand(new File(classPath)));
            }
        }

        return files.toArray(new File[files.size()]);
    }
}
