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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import cz.cuni.mff.spl.utils.StreamUtils;

/**
 * <p>
 * This class implements index support for measurement directory. Every
 * measurement stored in the store is noted in the index file together with its
 * full identification.
 * </p>
 * 
 * <p>
 * On initialization this class reads the index file in its indexed directory
 * and checks that all measurement files are present and none is missing or
 * redundant.
 * </p>
 * 
 * <p>
 * Format of measurement index is following:
 * </p>
 * 
 * <pre>
 * identification#1 fileName#1 lineEnd
 * identification#2 fileName#2 lineEnd
 * identification#3 fileName#3 lineEnd
 * ...
 * </pre>
 * 
 * <p>
 * Based on {@link StoreIndexUtils#ENCODE_ID} identification might or might not
 * be encoded in Base64.
 * </p>
 * 
 * <p>
 * Based on {@link StoreIndexUtils#ENCODE_DAT} file name might or might not be
 * encoded in Base64.
 * </p>
 * 
 * @see LocalStoreIndex
 * 
 * @author Frantisek Haas
 * 
 */
public class LocalStoreIndexMeasurement extends LocalStoreIndex {

    public LocalStoreIndexMeasurement(File indexedDirectory) throws IOException {
        super(indexedDirectory);
    }

    @Override
    protected boolean checkLine(String line, int lineNumber) {
        String[] split = line.split(StoreIndexUtils.SEPARATOR);
        if (split.length != 3) {
            return false;
        }

        // String id = StoreIndexUtils.decodeId(split[0]);
        String file = StoreIndexUtils.decodeDat(split[1]);
        String end = split[2];

        if (!end.equals(StoreIndexUtils.LINE_END)) {
            return false;
        }

        File indexedFile = new File(indexedDirectory, file);
        if (!indexedFile.exists()) {
            return false;
        }

        // check id or not? this check might miss corrupted entry but checking
        // every first line of every measurement file might be pretty long and
        // it might be faster to rebuild index every time it's initialized

        return true;
    }

    @Override
    protected void processFile(PrintStream writer, File file)
            throws IOException {
        if (file.isFile()) {
            try (FileInputStream input = new FileInputStream(file)) {
                String identification = StreamUtils.readFirstStreamLineToString(input);

                writer.printf("%s %s %s", StoreIndexUtils.encodeId(identification), StoreIndexUtils.encodeDat(file.getName()), StoreIndexUtils.LINE_END);
                writer.println();
            }
        }
    }

    /**
     * Directly writes measurement entry into index file.
     * 
     * @param fileName
     *            The file name of measurement.
     * @param identification
     *            Measurement identification.
     * @throws IOException
     */
    public void put(String fileName, String identification)
            throws IOException {
        boolean append = true;
        try (PrintStream writer = new PrintStream(new FileOutputStream(index, append))) {

            writer.printf("%s %s %s", StoreIndexUtils.encodeId(identification), StoreIndexUtils.encodeDat(fileName), StoreIndexUtils.LINE_END);
            writer.println();
        }
    }
}
