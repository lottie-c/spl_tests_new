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
package cz.cuni.mff.spl.configuration;

import java.util.List;

import cz.cuni.mff.spl.conversion.IniValueConvertor;
import cz.cuni.mff.spl.conversion.IniValueConvertor.InvalidValueException;

/**
 * <p>
 * This class helps checking if supplied values can be decoded to certain types.
 * </p>
 * 
 * @see ISectionFactory
 * 
 * @author Frantisek Haas
 * 
 */
public class ConversionChecker {

    /**
     * <p>
     * Tries to decode {@code value} to {@link Boolean} type. If conversion
     * fails error is filled into {@code errors} list.
     * </p>
     * 
     * @param errors
     *            Where to add error if occurs.
     * @param key
     *            For debugging purpose into errors only.
     * @param value
     *            The value tried for decoding.
     */
    public static void tryDecodeBoolean(List<String> errors, String key, String value) {
        try {
            IniValueConvertor.decodeBoolean(value);
        } catch (InvalidValueException e) {
            errors.add(String.format("Configuration value [%s] is not supported for key [%s].", value, key));
        }
    }

    /**
     * <p>
     * Tries to decode {@code value} to {@link Integer} type. If conversion
     * fails error is filled into {@code errors} list.
     * </p>
     * 
     * @param errors
     *            Where to add error if occurs.
     * @param key
     *            For debugging purpose into errors only.
     * @param value
     *            The value tried for decoding.
     */
    public static void tryDecodeInteger(List<String> errors, String key, String value) {
        try {
            IniValueConvertor.decodeInteger(value);
        } catch (InvalidValueException e) {
            errors.add(String.format("Configuration value [%s] is not supported for key [%s].", value, key));
        }
    }
}
