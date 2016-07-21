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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * The basic functions to operate with streams such as reading entire file to
 * string, saving data to steam.
 * 
 * @author Martin Lacina
 */
public class StreamUtils {

    /**
     * Saves string to file.
     * 
     * @param stream
     *            The file to save string to.
     * @param string
     *            The string.
     * @return True, if save successful.
     */
    public static boolean saveToStream(OutputStream stream, String string) {
        return saveToStream(stream, string.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Saves bytes to file.
     * 
     * @param file
     *            The file to save bytes to.
     * @param data
     *            The data to save.
     * @return True, if successful.
     */
    public static boolean saveToStream(OutputStream stream, byte[] data) {
        try {
            stream.write(data, 0, data.length);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Reads entire stream to string.
     * 
     * @param stream
     *            The stream to read.
     * @return
     *         The stream content as string, or {@code null} when reading fails.
     */
    public static String readEntireStreamToString(InputStream stream) {
        BufferedInputStream reader = null;
        try {
            StringBuilder buffer = new StringBuilder();
            reader = new BufferedInputStream(stream);
            int character;
            while ((character = reader.read()) >= 0) {
                buffer.append((char) character);
            }
            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * Reads entire stream to string.
     * 
     * @param stream
     *            The stream to read.
     * @return
     * @throws IOException
     */
    public static String readEntireStreamToStringSafe(InputStream stream)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        while (stream.available() > 0) {
            int c = stream.read();
            if (c != -1) {
                builder.append((char) c);
            } else {
                break;
            }

        }
        stream.close();
        return builder.toString();
    }

    /**
     * Reads first line of stream to string.
     * 
     * @param stream
     *            The stream to read.
     * @return
     */
    public static String readFirstStreamLineToString(InputStream stream)
            throws IOException {
        InputStreamReader streamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        String line = bufferedReader.readLine();
        bufferedReader.close();

        if (line == null) {
            line = new String();
        }

        return line;
    }
}
