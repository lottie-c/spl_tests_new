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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * <p>
 * Abstract local index file with generic code for index's validation and
 * building.
 * </p>
 * 
 * @see LocalStoreIndexDirectory
 * @see LocalStoreIndexMeasurement
 * 
 * @author Frantisek Haas
 * 
 */
public abstract class LocalStoreIndex {

    protected final File index;
    protected final File indexedDirectory;

    public LocalStoreIndex(File indexedDirectory)
            throws IOException {
        this.indexedDirectory = indexedDirectory.getAbsoluteFile();
        this.index = new File(indexedDirectory, StoreIndexUtils.INDEX_FILE_NAME).getAbsoluteFile();

        // if (!validate()) {
        // logger.info("Building index in [%s/%s].", indexedDirectory.getName(),
        // index.getName());
        build();
        // }
    }

    /**
     * This method checks single line of index and checks details of mentioned
     * file.
     * 
     * @param line
     *            The line entry to check.
     * @param lineNumber
     *            For debugging purpose.
     * @return
     * @throws IOException
     */
    protected abstract boolean checkLine(String line, int lineNumber)
            throws IOException;

    /**
     * <p>
     * Reads the index file line by line and processes it via
     * {@link #checkLine(String, int)}. If {@code false} is returned the index
     * is corrupted and must be rebuilt.
     * </p>
     * 
     * <p>
     * Also checks if number of lines matches number of files inside indexed
     * directory. If it doesn't index must be rebuilt.
     * </p>
     * 
     * @return
     */
    /** No validation currently used, index rebuilt every time. */
    @SuppressWarnings("unused")
    private boolean validate() {
        try (BufferedReader reader = new BufferedReader(new FileReader(index))) {
            String line = reader.readLine();
            int lineNumber = 0;

            while (line != null) {
                lineNumber++;

                if (!checkLine(line, lineNumber)) {
                    return false;
                }

                line = reader.readLine();
            }

            int indexFile = 1;
            if ((indexedDirectory.listFiles().length - indexFile) != lineNumber) {
                return false;
            }

        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * <p>
     * In case of building index this function is used to process a file in the
     * indexed directory. It should produce a single line entry to the writer
     * which indexes the file.
     * </p>
     * 
     * @param writer
     * @param file
     * @throws IOException
     */
    protected abstract void processFile(PrintStream writer, File file)
            throws IOException;

    /**
     * <p>
     * This method iterates over all files inside the indexed directory (except
     * index file itself) and calls {@link #processFile(PrintStream, File)} to
     * create entry in the index file via print stream.
     * </p>
     * 
     * @throws IOException
     */
    private void build()
            throws IOException {
        if (index.exists()) {
            if (!index.delete()) {
                throw new IOException("Failed to delete index.");
            }
        }

        if (!index.createNewFile()) {
            throw new IOException("Failed to create index.");
        }

        try (PrintStream writer = new PrintStream(new FileOutputStream(index))) {
            for (File file : indexedDirectory.listFiles()) {
                if (!file.getName().startsWith(".")) {
                    processFile(writer, file);
                }
            }
        }
    }
}
