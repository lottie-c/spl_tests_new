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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;

/**
 * The basic functions to operate with files such as reading entire file to
 * string, saving data to file and creating new files in a directory with
 * specified prefix and extension.
 * 
 * @author Martin Lacina
 * @author Frantisek Haas
 */
public class FileUtils {

    /**
     * Saves string to file.
     * 
     * @param file
     *            The file to save string to.
     * @param string
     *            The string.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void saveToFile(File file, String string) throws IOException {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            out.write(string);
        }
    }

    /**
     * Saves bytes to file.
     * 
     * @param file
     *            The file to save bytes to.
     * @param data
     *            The data to save.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void saveToFile(File file, byte[] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data);
        }
    }

    /**
     * Reads entire file to string.
     * 
     * @param file
     *            The file to read.
     * @return The file content as string, or {@code null} when reading fails.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static String readEntireFileToString(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            StringBuilder buffer = new StringBuilder((int) file.length());
            int character;
            while ((character = reader.read()) >= 0) {
                buffer.append((char) character);
            }
            return buffer.toString();
        }
    }

    /**
     * Creates new unique file.
     * 
     * First check file name is prefix.extension
     * 
     * @param directory
     *            The directory to create file in.
     * @param prefix
     *            The file name prefix.
     * @param extension
     *            The extension (with starting dot, for example {@code .html}).
     * @return The created file.
     */
    public static File createUniqueFile(File directory, String prefix, String suffix) {
        final int maxAttemptCount = Integer.MAX_VALUE;

        // make sure that we have usable values
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;

        File result;
        for (int i = 0; i < maxAttemptCount; ++i) {
            result = tryToCreateFile(new File(directory, String.format("%s.%d%s", prefix, i, suffix)));
            if (result != null) {
                return result;
            }
        }
        throw new IllegalStateException(String.format("Unable to create new file with prefix '%s' and extension '%s'", prefix, suffix));
    }

    /**
     * Creates new unique file atomically.
     * 
     * @param directory
     *            The directory to create file in.
     * @param prefix
     *            The file name prefix.
     * @param extension
     *            The extension (with starting dot, for example {@code .html}).
     * @return The created file.
     * @throws IOException
     */
    public static File createUniqueAtomicFile(File directory, String prefix, String suffix) throws IOException {
        final int maxAttemptCount = Integer.MAX_VALUE;

        // make sure that we have usable values
        prefix = prefix == null ? "" : prefix;
        suffix = suffix == null ? "" : suffix;

        for (int i = 0; i < maxAttemptCount; i++) {
            File file = new File(directory, String.format("%s.%d%s", prefix, i, suffix));
            if (!file.exists()) {
                try {
                    return Files.createFile(file.toPath()).toFile();

                } catch (FileAlreadyExistsException ignoreAndTryAgain) {
                }
            }
        }

        return Files.createTempFile(directory.toPath(), prefix, suffix).toFile();
    }

    /**
     * Creates new unique directory.
     * 
     * @param directory
     *            The directory to create unique directory in. Null if current
     *            directory should be used.
     * @param prefix
     *            The prefix directory starts with. Null if non is required.
     * @param date
     *            If set true, current date in format 'yyyyMMddHHmmss' is added
     *            after prefix. Otherwise no date is added to directory name.
     * @return
     *         The created directory.
     * @throws IOException
     */
    public static File createUniqueDirectory(File directory, String prefix, boolean date)
            throws IOException {
        final int maxAttemptCount = Integer.MAX_VALUE;

        // make sure that we have usable values
        prefix = prefix == null ? "" : prefix + "-";

        // add date if required
        if (date) {
            DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            Date now = Calendar.getInstance().getTime();
            prefix += formatter.format(now) + "-";
        }

        // check if current directory should be used
        if (directory == null) {
            directory = new File(".");
        }

        // new File(directory, prefix +
        // UUID.randomUUID().toString().substring(0, 8));

        for (int i = 0; i < maxAttemptCount; i++) {
            File file = new File(directory, String.format("%s%d", prefix, i));
            if (!file.exists()) {
                createDirectory(file);
                return file;
            }
        }

        throw new IllegalStateException(String.format("Unable to create new directory with prefix '%s'.", prefix));
    }

    /**
     * Creates new unique directory atomically.
     * 
     * @param directory
     *            The directory to create unique directory in. Null if current
     *            directory should be used.
     * @param prefix
     *            The prefix directory starts with. Null if non is required.
     * @return
     *         The created directory.
     * @throws IOException
     */
    public static File createUniqueAtomicDirectory(File directory, String prefix)
            throws IOException {
        final int maxAttemptCount = Integer.MAX_VALUE;

        // make sure that we have usable values
        prefix = prefix == null ? "" : prefix + "-";

        for (int i = 0; i < maxAttemptCount; i++) {
            File file = new File(directory, String.format("%s%d", prefix, i));
            if (!file.exists()) {
                try {
                    return Files.createDirectory(file.toPath()).toFile();

                } catch (FileAlreadyExistsException ignoreAndTryAgain) {
                }
            }
        }

        return Files.createTempDirectory(directory.toPath(), prefix).toFile();
    }

    /**
     * Tries to create file. When successful, provided file is returned.
     * When new file was not created, than {@code null} returned.
     * 
     * @param file
     *            The file.
     * @return Provided file is returned, when successful.
     *         When new file was not created, than {@code null} returned.
     */
    private static File tryToCreateFile(File file) {
        try {
            if (file.createNewFile()) {
                return file;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates the directory and all necessary parent directories.
     * 
     * @param directory
     * @throws IOException
     */
    public static void createDirectory(File directory)
            throws IOException {
        directory.mkdirs();

        if (!directory.exists()) {
            throw new IOException("Failed to create directory [" + directory.getAbsolutePath() + "].");
        }
    }

    /**
     * If directory does not exist it creates one and all necessary parent
     * directories. If directory exists it clears it.
     * 
     * @param directory
     * @throws IOException
     */
    public static void makeClearDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            createDirectory(directory);
        } else {
            clearDirectory(directory);
        }
    }

    /**
     * Clears existing directory. Fails with exception thrown when directory
     * does not exist or failed to clear it.
     * 
     * @param directory
     * @throws IOException
     */
    public static void clearDirectory(File directory)
            throws IOException {
        if (!directory.exists()) {
            return;
        }

        for (File file : directory.listFiles()) {
            deleteAll(file);
        }
    }

    /**
     * Deletes all files and directory specified recursively including passed
     * file itself.
     * 
     * @param file
     * @throws IOException
     */
    public static void deleteAll(File file)
            throws IOException {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteAll(f);
            }
        }

        /**
         * <p>
         * If file is read-only delete may fail. This might happen for example
         * for git's object, pack and idx files. Therefore it's needed to
         * firstly set them writeable.
         */
        if (!file.isDirectory()) {
            if (!file.setWritable(true, false)) {
                throw new IOException(String.format("Failed to change file to writeable [%s].", file));
            }
        }
        Files.delete(file.toPath());
    }

    /**
     * Checks if the sub-directory is really a sub-directory of the directory.
     * 
     * @param directory
     *            The suspected enclosing parent directory.
     * @param subdirectory
     *            The suspected sub-directory.
     * @throws IOException
     */
    public static boolean isSubdirectory(File directory, File subdirectory)
            throws IOException {
        directory = directory.getCanonicalFile();
        subdirectory = subdirectory.getCanonicalFile();

        File parent = subdirectory;
        while (parent != null) {
            if (directory.equals(parent)) {
                return true;
            }
            parent = parent.getParentFile();
        }
        return false;
    }

    /**
     * Copies content of the source directory to the destination directory or
     * the source file to the destination directory if it's a file.
     * 
     * Checks if the source directory is not a sub-directory of the destination
     * directory.
     * 
     * @param source
     *            All files in the source directory or the file itself is copied
     *            to the destination directory.
     * @param destination
     *            Destination directory.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void copyDirectory(File source, File destination, File destinationRoot)
            throws IOException {
        if (destinationRoot == null) {
            destinationRoot = destination;
        }
        copyDirectoryImplementation(
                source.getAbsoluteFile().getCanonicalFile(),
                destination.getAbsoluteFile().getCanonicalFile(),
                destinationRoot.getAbsoluteFile().getCanonicalFile());
    }

    /**
     * Copies content of the source directory to the destination directory or
     * the source file to the destination directory if it's a file.
     * 
     * Checks if the source directory is not a sub-directory of the destination
     * directory.
     * 
     * @param source
     *            All files in the source directory or the file itself is copied
     *            to the destination directory.
     * @param destination
     *            Destination directory.
     * @param omit
     *            File in the source directory to omit from copying.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void copyDirectory(File source, File destination, File destinationRoot, File omit)
            throws IOException {
        if (destinationRoot == null) {
            destinationRoot = destination;
        }
        copyDirectoryImplementation(
                source.getAbsoluteFile().getCanonicalFile(),
                destination.getAbsoluteFile().getCanonicalFile(),
                destinationRoot.getAbsoluteFile().getCanonicalFile(),
                omit.getAbsoluteFile().getCanonicalFile());
    }

    /**
     * Copy directory implementation.
     * 
     * @param source
     *            The source.
     * @param destination
     *            The destination.
     * @param destinationRoot
     *            The destination root.
     * @param omit
     *            File in the source directory to omit from copying.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void copyDirectoryImplementation(File source, File destination, File destinationRoot)
            throws IOException {
        copyDirectoryImplementation(source, destination, destinationRoot, null);
    }

    /**
     * Copy directory implementation.
     * 
     * @param source
     *            The source.
     * @param destination
     *            The destination.
     * @param destinationRoot
     *            The destination root.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void copyDirectoryImplementation(File source, File destination, File destinationRoot, File omit)
            throws IOException {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        if (source.equals(destinationRoot)) {
            return;
        }

        if (source.isFile()) {
            destination.mkdirs();
            copyFile(source, new File(destination, source.getName()));
        } else {
            File list[] = source.listFiles();
            if (list == null) {
                /* Maybe a broken link? */
                return;
            }
            for (File f : source.listFiles()) {
                if (omit != null && f.equals(omit)) {
                    continue;
                }

                InvokedExecutionConfiguration.checkIfExecutionAborted();
                if (f.isFile()) {
                    destination.mkdirs();
                    copyFile(f, new File(destination, f.getName()));
                } else {
                    copyDirectoryImplementation(f, new File(destination, f.getName()), destinationRoot);
                }
            }
        }
    }

    /**
     * Copies file using {@link FileChannel}.
     * 
     * @param sourceFile
     *            The source file.
     * @param destFile
     *            The destination file.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static void copyFile(File sourceFile, File destFile)
            throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileInputStream source = new FileInputStream(sourceFile);
                FileOutputStream destination = new FileOutputStream(destFile)) {
            FileChannel sourceChannel = source.getChannel();
            destination.getChannel().transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (java.nio.channels.ClosedByInterruptException e) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
        } catch (IOException e) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            throw e;
        }
    }

    /**
     * Checks if a file with specified name exists in the directory.
     * 
     * @param directory
     *            The directory to search in.
     * @param fileName
     *            The file name to look for.
     * @return
     */
    public static boolean fileExists(File directory, String fileName) {
        return new File(directory, fileName).exists();
    }

    /**
     * Checks if within directory exists one or more files with prefix
     * specified.
     * 
     * @param directory
     *            The directory to search in.
     * @param prefix
     *            Prefix of files to be searched for.
     * @return
     */
    public static boolean fileExists(File directory, String prefix, String suffix) {
        final String f_prefix = prefix != null ? prefix : "";
        final String f_suffix = suffix != null ? suffix : "";

        for (File f : directory.listFiles()) {
            if (f.getName().startsWith(f_prefix) && f.getName().endsWith(f_suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lists all files starting with prefix specified located in the directory.
     * 
     * @param directory
     *            The directory to search in.
     * @param prefix
     *            Prefix of files to be listed.
     * @param suffix
     *            Suffix of files to be listed.
     * @return
     */
    public static File[] listFiles(File directory, String prefix, String suffix) {
        final String f_prefix = prefix != null ? prefix : "";
        final String f_suffix = suffix != null ? suffix : "";

        LinkedList<File> files = new LinkedList<>();
        for (File f : directory.listFiles()) {
            if (f.getName().startsWith(f_prefix) && f.getName().endsWith(f_suffix)) {
                files.add(f);
            }
        }

        return files.toArray(new File[files.size()]);
    }

    /**
     * Copy data from one stream to another. Closes both streams.
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copy(InputStream source, OutputStream destination)
            throws IOException {
        byte[] buffer = new byte[4096];
        int length = source.read(buffer);

        while (length != -1) {
            destination.write(buffer, 0, length);
            length = source.read(buffer);
        }

        source.close();
        destination.close();
    }

    /**
     * Copy data from stream to file. Closes both streams.
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copy(InputStream source, File destination)
            throws IOException {
        try (FileOutputStream stream = new FileOutputStream(destination)) {
            copy(source, stream);
        } finally {
            source.close();
        }
    }

    /**
     * Copy data from file to stream. Closes both streams.
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copy(File source, OutputStream destination)
            throws IOException {
        try (FileInputStream stream = new FileInputStream(source)) {
            copy(stream, destination);
        } finally {
            destination.close();
        }
    }

    /**
     * Copy from file to file
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copy(File source, File destination)
            throws IOException {
        try (
                FileInputStream in = new FileInputStream(source);
                FileOutputStream out = new FileOutputStream(destination)) {
            copy(in, out);
        }
    }
}
