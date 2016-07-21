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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Formatter;

import org.apache.xerces.impl.dv.util.Base64;

/**
 * The basic functions to operate with string such as conversion of byte array
 * to hexadecimal string, concatenation of strings to one and encoding string to
 * Base 64.
 * 
 * @author Martin Lacina
 * @author Frantisek Haas
 */
public class StringUtils {

    /**
     * The charset to be used for {@link #getStringBytes(String)}.
     * Preferred charset is UTF-8, but if it is not for some reason available,
     * than we fall back to JVM default charset.
     */
    public static final Charset BYTE_CHARSET;

    static {
        Charset byteCharset;
        try {
            byteCharset = Charset.forName("UTF-8");
        } catch (UnsupportedCharsetException e) {
            byteCharset = Charset.defaultCharset();
        }
        BYTE_CHARSET = byteCharset;
    }

    /**
     * Creates the hexadecimal string encoding specified data.
     * 
     * @param data
     *            The data to encode to hexadecimal string.
     * @return The hexadecimal string representing data.
     */
    public static String createHexadecimalString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : data) {
            formatter.format("%02x", b);
        }
        formatter.close();

        return sb.toString();
    }

    /**
     * Creates one string for specified iterable collection.
     * String representation of element is calculated using their
     * {@code toString()} implementation.
     * String prefix and suffix can be specified.
     * 
     * @param elements
     *            The elements.
     * @param connectWith
     *            The string to connect elements with.
     * @param prefix
     *            The prefix for resulting string.
     * @param suffix
     *            The suffix for resulting string.
     * @return The calculated string.
     */
    public static String createOneString(Iterable<?> elements, String connectWith, String prefix, String suffix) {
        prefix = prefix != null ? prefix : "";
        suffix = suffix != null ? suffix : "";

        StringBuilder buffer = new StringBuilder(prefix);
        boolean isFirst = true;

        for (Object parameter : elements) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(connectWith);
            }
            buffer.append(parameter);
        }

        buffer.append(suffix);
        return buffer.toString();
    }

    /**
     * Creates one string for specified iterable collection.
     * String representation of element is calculated using their
     * {@code toString()} implementation.
     * 
     * @param elements
     *            The elements.
     * @param connectWith
     *            The string to connect elements with.
     * @return The calculated string.
     *         Same as calling
     *         {@link #createOneString(Iterable, String, String, String)} with
     *         empty string as prefix and suffix.
     */
    public static String createOneString(Iterable<?> elements, String connectWith) {
        return createOneString(elements, connectWith, "", "");
    }

    /**
     * Creates one string for specified array.
     * String representation of element is calculated using their
     * {@code toString()} implementation.
     * 
     * @param <T>
     *            Any type derived from Object.
     * @param elements
     *            The elements.
     * @param connectWith
     *            The string to connect elements with.
     * @return The calculated string.
     *         Same as calling
     *         {@link #createOneString(T[], String, String, String)} with
     *         empty string as prefix and suffix.
     */
    public static <T extends Object> String createOneString(T[] elements, String connectWith) {
        ArrayList<T> e = new ArrayList<>(elements.length);
        for (T element : elements) {
            e.add(element);
        }
        return createOneString(e, connectWith, "", "");
    }

    /**
     * Creates one string for specified array.
     * String representation of element is calculated using their
     * 
     * @param <T>
     *            Any type derived from Object.
     * @param elements
     *            The elements.
     * @param connectWith
     *            The string to connect elements with.
     * @param prefix
     *            The prefix for resulting string.
     * @param suffix
     *            The suffix for resulting string.
     * @return The calculated string. {@code toString()} implementation.
     *         Prefix and suffix can be specified.
     */
    public static <T extends Object> String createOneString(T[] elements, String connectWith, String prefix, String suffix) {
        ArrayList<T> e = new ArrayList<>(elements.length);
        for (T element : elements) {
            e.add(element);
        }
        return createOneString(e, connectWith, prefix, suffix);
    }

    /**
     * Encode to base64.
     * 
     * @param string
     *            The string to encode to Base 64.
     * @return The Base 64 encoded string.
     */
    public static String encodeToBase64(String string) {
        return encodeToBase64(getStringBytes(string));
    }

    /**
     * Encode to base64.
     * 
     * @param data
     *            The data to encode to Base 64.
     * @return The Base 64 encoded string representing the data.
     */
    public static String encodeToBase64(byte[] data) {
        return Base64.encode(data);
    }

    /**
     * Decode from base64.
     * 
     * @param data
     *            String encoded in Base 64
     * @return The string decoded from Base64.
     */
    public static String decodeFromBase64(String string) {
        return getBytesString(Base64.decode(string));
    }

    /**
     * Reads raw data from stream and converts them to text characters.
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    public static String convertStreamDataToString(InputStream stream)
            throws IOException {
        StringBuilder builder = new StringBuilder();

        int c = stream.read();
        while (c != -1) {
            builder.append((char) c);
            c = stream.read();
        }

        stream.close();

        return builder.toString();
    }

    /**
     * Gets the string bytes with {@link #BYTE_CHARSET} charset (UTF-8
     * preferred).
     * 
     * @param string
     *            The string to get bytes for.
     * @return The string bytes.
     */
    public static byte[] getStringBytes(String string) {
        return string.getBytes(BYTE_CHARSET);
    }

    /**
     * Gets the string from bytes with {@link #BYTE_CHARSET} charset (UTF-8
     * preferred).
     * 
     * @param data
     * @return
     */
    public static String getBytesString(byte[] data) {
        return new String(data, BYTE_CHARSET);
    }

    /**
     * Checks if stirng is null or empty.
     * 
     * @param string
     *            The string.
     * @return True, if string is null or empty.
     */
    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Converts string array to single string separating fields with specified
     * separator.
     * 
     * @param array
     * @param separator
     * @return
     */
    public static String arrayToString(String[] array, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                builder.append(separator);
            }
            builder.append(array[i]);
        }

        return builder.toString();
    }

}
