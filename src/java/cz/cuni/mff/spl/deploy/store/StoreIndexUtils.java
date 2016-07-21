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

import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Utilities for {@link IStore} and {@link IStoreReadonly} index
 * manipulation and configuration.
 * 
 * @see LocalStoreIndex
 * @see HttpStoreIndex
 * 
 * @author Frantisek Haas
 * 
 */
public class StoreIndexUtils {

    /** File name of index files. */
    public static final String  INDEX_FILE_NAME = ".spl-index";

    /** Line end of single index entry. */
    public static final String  LINE_END        = "~";

    /** Separator between parts of single index entry. */
    public static final String  SEPARATOR       = " ";

    /** If sample identification in measurement index should be encoded. */
    public static final boolean ENCODE_ID       = true;

    /** If sample data file name in measurement index should be encoded. */
    public static final boolean ENCODE_DAT      = false;

    /** If file name in directory index should be encoded. */
    public static final boolean ENCODE_FILE     = true;

    /**
     * Conditionally encodes measurement identification if encoding is set.
     * 
     * @param decoded
     * @return
     */
    public static String encodeId(String decoded) {
        if (ENCODE_ID) {
            return StringUtils.encodeToBase64(decoded);
        } else {
            return decoded;
        }
    }

    /**
     * Conditionally decodes measurement identification if encoding is set.
     * 
     * @param encoded
     * @return
     */
    public static String decodeId(String encoded) {
        if (ENCODE_ID) {
            return StringUtils.decodeFromBase64(encoded);
        } else {
            return encoded;
        }
    }

    /**
     * Conditionally encodes measurement data file name if encoding is set.
     * 
     * @param decoded
     * @return
     */
    public static String encodeDat(String decoded) {
        if (ENCODE_DAT) {
            return StringUtils.encodeToBase64(decoded);
        } else {
            return decoded;
        }
    }

    /**
     * Conditionally decodes measurement data file name if encoding is set.
     * 
     * @param encoded
     * @return
     */
    public static String decodeDat(String encoded) {
        if (ENCODE_DAT) {
            return StringUtils.decodeFromBase64(encoded);
        } else {
            return encoded;
        }
    }

    /**
     * Conditionally encodes generic directory file name if encoding is set.
     * 
     * @param decoded
     * @return
     */
    public static String encodeFile(String decoded) {
        if (ENCODE_FILE) {
            return StringUtils.encodeToBase64(decoded);
        } else {
            return decoded;
        }
    }

    /**
     * Conditionally decodes generic directory file name if encoding is set.
     * 
     * @param encoded
     * @return
     */
    public static String decodeFile(String encoded) {
        if (ENCODE_FILE) {
            return StringUtils.decodeFromBase64(encoded);
        } else {
            return encoded;
        }
    }
}
