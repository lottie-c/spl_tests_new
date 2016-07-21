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

import org.apache.commons.math3.random.RandomDataGenerator;

/**
 * Abstract class for primitive types generators.
 * 
 * @author Frantisek Haas
 * 
 */
public abstract class AbstractGenerator {

    /** Separator of arguments in constructor. */
    protected final static String       SEPARATOR    = ";";

    /** Data generator. */
    protected final RandomDataGenerator generator    = new RandomDataGenerator();

    /** Type of collection around data to return them in. */
    private ArgumentType                argumentType = ArgumentType.arrayPrimitive;

    /** Sets the collection around the data. */
    protected void setArgumentType(ArgumentType argumentType) {
        this.argumentType = argumentType;
    }

    /**
     * <p>
     * Implement to generate random numbers to array of primitives in size of
     * {@code args}.
     * 
     * @param args
     *            Number of random numbers to generate into an array.
     * @return
     *         <p>
     *         Array of random numbers as a first member in the {@link Object[]}
     *         array. For e.g. 'return new Object[] { new double[] {-1.0,
     *         1.0}}'.
     */
    protected abstract Object[] generateArrayPrimitive(int args);

    /**
     * <p>
     * Implement to generate random numbers to array of wrappers in size of
     * {@code args}.
     * 
     * @param args
     *            Number of random numbers to generate into an array.
     * @return
     *         <p>
     *         Array of random numbers as a first member in the {@link Object[]}
     *         array. For e.g. 'return new Object[] { new Double[] {-1.0,
     *         1.0}}'.
     */
    protected abstract Object[] generateArray(int args);

    /**
     * <p>
     * Implement to generate wrapped list using {@link #generateArray(int)}.
     * 
     * @return
     */
    protected abstract Object[] newList(int args);

    @SuppressWarnings("unchecked")
    private Object[] generateList(int args) {
        Object[] data = generateArray(args);
        Object[] obj = newList(args);

        for (int i = 0; i < args; i++) {
            ((List<Object>) obj[0]).add((((Object[]) data[0])[i]));
        }

        return obj;
    }

    /**
     * <p>
     * Generates data for specified number of {@code calls}. Each call's
     * arguments consists of a single array of generated numbers. The size of
     * array is {@code args}.
     * 
     * @param calls
     *            The number of calls to generate data for.
     * @param args
     *            The count of randomly generated numbers for a single call.
     * @return
     */
    public ArrayList<Object[]> generate(int calls, int args) {
        ArrayList<Object[]> data = new ArrayList<>();

        for (int i = 0; i < calls; i++) {
            switch (argumentType) {
                case arrayPrimitive:
                    data.add(generateArrayPrimitive(args));
                    break;
                case array:
                    data.add(generateArray(args));
                    break;
                case list:
                    data.add(generateList(args));
                    break;
            }
        }

        return data;
    }
}
