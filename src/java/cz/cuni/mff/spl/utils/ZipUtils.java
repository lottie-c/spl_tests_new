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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class provider utilites to pack and unpack directory structures into and
 * from zip archive.
 * 
 * Instance of this class serves as a configuration for zip archive - from
 * source path to relative archuve path mapping and also as packer itself.
 * 
 * @author Frantisek Haas
 * 
 */
@SuppressWarnings("unused")
public class ZipUtils {

    /**
     * Use only Unix separator, Windows can handle it and Linux will be ok if
     * windows separator is used. Unzip on Linux fails to create directories if
     * Windows separator was used, respectively if {@link File#separator} was
     * used while creating zip on Windows.
     */
    private static final String UNIX_SEPARATOR = "/";

    /**
     * This class serves as an configuration entry for zip archive packing.
     * 
     * @author Frantisek Haas
     * 
     */
    private class Entry {

        /**
         * This class servers as a result of {@link ZipUtils.Entry} expanding.
         * 
         * @author Frantisek Haas
         * 
         */
        private class Pair {

            private final String path;
            private final String relativePath;

            public Pair(String path, String relativePath) {
                this.path = path;
                this.relativePath = relativePath;
            }

            public String getPath() {
                return path;
            }

            public String getRelativePath() {
                return relativePath;
            }
        }

        private final String path;
        private final String relativePath;

        public Entry(String path, String relativePath) {
            this.path = path;
            this.relativePath = relativePath;
        }

        public String getPath() {
            return path;
        }

        public String getRelativePath() {
            return relativePath;
        }

        /**
         * This function expands the {@link ZipUtils.Entry} configuration to
         * single files or directories represented by
         * {@link ZipUtils.Entry.Pair} which are then directly written.
         * 
         * @return
         *         Collection of files or directories expanded from this
         *         {@link ZipUtils.Entry}.
         */
        public Collection<? extends Pair> getPairs() {
            return getPairsRecursive(path, relativePath, 0);
        }

        /**
         * This function expands all files on the current path level to files on
         * relative path level.
         * 
         * That means entry expanding works directory by directory down the
         * absolute path.
         * 
         * @param path
         *            The absolute path whose files are going the be expanded to
         *            relative files.
         * @param relativePath
         *            The relative path the files are going to be expanded on
         *            to.
         * @param recursion
         *            The level of recursion.
         * @return
         *         Collection of files or directories expanded on the specified
         *         absolute path to the specified relative path.
         */
        public Collection<? extends Pair> getPairsRecursive(String path, String relativePath, int recursion) {
            LinkedList<Pair> pairs = new LinkedList<>();

            File file = new File(path);

            // create relative path down the recursion
            String relativeFilePath;
            if (recursion == 0) {
                relativeFilePath = relativePath;
            } else if (relativePath.equals("")) {
                relativeFilePath = file.getName();
            } else {
                relativeFilePath = relativePath + UNIX_SEPARATOR + file.getName();
            }

            // if it is just and ordinary file, just add it
            if (file.exists()) {
                if (!relativeFilePath.equals("")) {
                    pairs.add(
                            new Pair(file.getPath(), relativeFilePath));
                }
            }

            // if file is directory expand everythin inside
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    pairs.addAll(
                            getPairsRecursive(f.getPath(), relativeFilePath, recursion + 1));
                }
            }

            return pairs;
        }
    }

    /**
     * Entries to be packed inside archive.
     */
    private final List<Entry> entries      = new LinkedList<>();

    /**
     * Path that should be excluded from packing. This allows the result archive
     * to be written inside just being packed directory.
     */
    private Set<String>       excludePaths = new TreeSet<>();

    public ZipUtils() {
    }

    /**
     * This function adds entry to the list of those being packed.
     * 
     * @param path
     *            The absolute path to be packed into archive.
     * @param relativePath
     *            The relative location of all files on the absolute path in the
     *            future archive.
     * @throws Exception
     */
    public void add(String path, String relativePath)
            throws Exception {
        File pathFile = new File(path);

        try {
            entries.add(new Entry(pathFile.getCanonicalPath(), relativePath));
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    @Deprecated
    public void remove(String path, String relativePath) {
        throw new RuntimeException("Not implemented.");
    }

    @Deprecated
    public void removeByPath(String path) {
        throw new RuntimeException("Not implemented.");
    }

    @Deprecated
    public void removeByRelativePath(String relativePath) {
        throw new RuntimeException("Not implemented.");
    }

    /**
     * This function checks if files are not going to be packed more than once.
     * This could happen if user adds a path and then sub-path of the path
     * previously added.
     * 
     * @throws Exception
     */
    private void checkSubpaths()
            throws Exception {
        try {
            for (int i = 0; i < entries.size(); i++) {
                String icp = new File(entries.get(i).getPath()).getCanonicalPath();

                for (int j = 0; j < entries.size(); j++) {
                    String jcp = new File(entries.get(j).getPath()).getCanonicalPath();

                    if (i != j) {
                        if (icp.indexOf(jcp) != -1 || jcp.indexOf(icp) != -1) {
                            throw new Exception("Will not zip subpaths.");
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    /**
     * This function expands all added entries to path:relativePath pairs which
     * can be directly written into the archive.
     * 
     * @return
     * @throws Exception
     */
    private List<Entry.Pair> createEntryPairs()
            throws Exception {
        LinkedList<Entry.Pair> pairs = new LinkedList<>();

        for (Entry entry : entries) {
            pairs.addAll(entry.getPairs());
        }

        return pairs;
    }

    public void write(OutputStream outputStream)
            throws Exception {
        final int EOF = -1;
        final int BUFFER_SIZE = 4096;

        checkSubpaths();
        List<Entry.Pair> pairs = createEntryPairs();

        try {
            ZipOutputStream zipOut = new ZipOutputStream(outputStream);
            FileInputStream fileIn = null;
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            File file = null;

            for (Entry.Pair pair : pairs) {
                if (this.excludePaths.contains(pair.getRelativePath())) {
                    continue;
                }

                file = new File(pair.getPath());

                if (file.isDirectory()) {
                    zipOut.putNextEntry(new ZipEntry(pair.getRelativePath() + UNIX_SEPARATOR));
                    zipOut.closeEntry();
                }

                else {
                    zipOut.putNextEntry(new ZipEntry(pair.getRelativePath()));
                    fileIn = new FileInputStream(pair.getPath());

                    length = fileIn.read(buffer);
                    while (length != EOF) {
                        zipOut.write(buffer, 0, length);
                        length = fileIn.read(buffer);
                    }

                    fileIn.close();
                    zipOut.closeEntry();
                }
            }

            zipOut.close();

        } catch (IOException e) {
            throw new Exception(e);
        }
    }

    public static void zip(String path, String relativePath, OutputStream zipOutputStream, Set<String> excludePaths)
            throws Exception {
        ZipUtils zipUtils = new ZipUtils();
        zipUtils.add(path, relativePath);
        zipUtils.setExcludePaths(excludePaths);
        zipUtils.write(zipOutputStream);
    }

    private void setExcludePaths(Set<String> excludePaths) {
        if (excludePaths != null) {
            this.excludePaths = excludePaths;
        } else {
            this.excludePaths = new TreeSet<>();
        }
    }

    public static void unzip(InputStream zipInputStream, File directory)
            throws IOException {
        final int EOF = -1;
        final int BUFFER_SIZE = 4096;

        ZipInputStream zipIn = new ZipInputStream(zipInputStream);

        ZipEntry zipEntry = zipIn.getNextEntry();
        while (zipEntry != null) {
            if (zipEntry.getName().endsWith(UNIX_SEPARATOR)) {
                FileUtils.createDirectory(new File(directory, zipEntry.getName()));
            } else {

                FileOutputStream fileOut = new FileOutputStream(new File(directory, zipEntry.getName()));

                byte[] buffer = new byte[BUFFER_SIZE];
                int lenght = zipIn.read(buffer);

                while (lenght != EOF) {
                    fileOut.write(buffer, 0, lenght);
                    lenght = zipIn.read(buffer);
                }

                fileOut.close();
            }

            zipEntry = zipIn.getNextEntry();
        }
    }

    public static void main(String args[])
            throws Exception {
        ZipUtils z = new ZipUtils();
        z.add("C:\\Users\\fusr\\_Projects\\SPL\\workspace\\spl-tools-code\\test", "a");
        z.write(new FileOutputStream(new File("my.zip")));
    }
}
