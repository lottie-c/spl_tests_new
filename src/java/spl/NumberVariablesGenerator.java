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
package spl;

import java.util.ArrayList;
import java.util.List;

/**
 * The SPL generator implementation for handling passing variables directly to
 * the method.
 * 
 * @author Martin Lacina
 */
public class NumberVariablesGenerator {

    /**
     * Creates generator with all provided variables as one item in one Object
     * array.
     * 
     * @param variables
     *            The variables.
     * @return The iterable.
     */
    public static Iterable<Object[]> createOne(int... variables) {
        List<Object[]> result = new ArrayList<>();
        result.add(new Object[] { variables });
        return result;
    }

    /**
     * Creates generator with where every provided variable is in its own in one
     * Object array.
     * 
     * @param variables
     *            The variables.
     * @return The iterable.
     */
    public static Iterable<Object[]> createMany(int... variables) {
        List<Object[]> result = new ArrayList<>(variables.length);
        for (int i = 0; i < variables.length; ++i) {
            result.add(new Object[] { variables[i] });
        }
        return result;
    }
}
