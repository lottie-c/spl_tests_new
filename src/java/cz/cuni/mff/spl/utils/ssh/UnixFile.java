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

import java.util.LinkedList;

import cz.cuni.mff.spl.utils.Pair;

/**
 * This class does not do any operations on Unix file system itself. It
 * serves as a tool for manipulation of paths in Unix environment.
 * 
 * @author Frantisek Haas
 * 
 */
public class UnixFile {

    private static final char   SLASH_CHAR = '/';
    private static final String SLASH      = "/";
    private static final String HOME       = "~";

    private final String        path;

    public UnixFile(String path) {
        this.path = normalize(path);
    }

    public UnixFile(UnixFile parent, String child) {
        this.path = concatenate(parent.path, child);
    }

    public UnixFile(String parent, UnixFile child) {
        this.path = concatenate(parent, child.path);
    }

    public UnixFile(String parent, String child) {
        this.path = concatenate(parent, child);
    }

    public String getName() {
        return split(path).getRight();
    }

    public String getPath() {
        return path;
    }

    public UnixFile getParent() {
        return new UnixFile(split(path).getLeft());
    }

    public UnixFile[] getParentList() {
        LinkedList<UnixFile> parents = new LinkedList<>();

        UnixFile parent = getParent();
        while (parent != null) {
            parents.add(parent);
            parent = parent.getParent();
        }

        return parents.toArray(new UnixFile[parents.size()]);
    }

    public boolean isAbsolute() {
        return (path.startsWith(SLASH));
    }

    public boolean isHomeRelative() {
        return (path.startsWith(HOME));
    }

    public boolean isRelative() {
        return (!isAbsolute() && !isHomeRelative());
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * This function deletes doubled slashes and trims the trailing slash if
     * present.
     * 
     * @param path
     *            The path to normalize.
     * @return
     */
    public static String normalize(String path) {
        String newPath = new String();

        char[] pa = path.toCharArray();

        // last path valid character except trailing slashes
        int lastValidChar = pa.length - 1;
        for (; lastValidChar > 1; lastValidChar--) {
            if (pa[lastValidChar] != SLASH_CHAR) {
                break;
            }
        }

        // remove doubled slashes
        boolean lastSlash = false;
        for (int i = 0; i <= lastValidChar; i++) {
            if (pa[i] == SLASH_CHAR) {
                // not double slash or trailing slash
                if (!lastSlash) {
                    newPath += pa[i];
                }
                lastSlash = true;
            } else {
                lastSlash = false;
                newPath += pa[i];
            }
        }

        return newPath;
    }

    /**
     * Connects to paths into a single normalized one.
     * 
     * @param parentPath
     * @param childPath
     * @return
     */
    public static String concatenate(String parentPath, String childPath) {
        return normalize(normalize(parentPath) + SLASH + normalize(childPath));
    }

    /**
     * Splits path into the top right directory name and parent path.
     * 
     * @param path
     * @return
     */
    public static Pair<String, String> split(String path) {
        path = normalize(path);

        int split = path.length() - 1;
        for (; split >= 0; split--) {
            if (path.charAt(split) == SLASH_CHAR) {
                split++;
                break;
            }
        }

        String parentPath = normalize(path.substring(0, split));
        String childName = normalize(path.substring(split));

        return new Pair<String, String>(parentPath, childName);
    }
}
