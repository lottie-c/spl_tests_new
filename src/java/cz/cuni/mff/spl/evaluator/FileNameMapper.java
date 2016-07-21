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
package cz.cuni.mff.spl.evaluator;

import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.Measurement;

/**
 * Class for measurement to file mapping.
 * 
 * Idea is that we hash measurement and use this has as prefix for stored file.
 * 
 * Hash collisions will be resolved by measuring tool in a way that unique
 * suffix will be added to generated prefix (
 * {@link FileCreator#createUniqueFile(java.io.File, String, String)} ).
 * 
 * When loading, first file with correct prefix, that contains specified
 * measurement data will be used.
 * 
 * This implies, that output of measurement tool has to provide a way to
 * precisely identify measurement in stored file.
 * 
 * @author Martin Lacina
 * 
 * @see FileCreator#createUniqueFile(java.io.File, String, String)
 */
public class FileNameMapper {

    /**
     * Gets the measurement file name extension ('.dat').
     * 
     * @return The measurement data file name extension ('.dat').
     */
    public static String getMeasurementDataFileNameExtension() {
        return ".dat";
    }

    /**
     * Gets the measurement file name prefix.
     * 
     * @param measurement
     *            The measurement.
     * @return The measurement file name prefix. Never {@code null}.
     */
    public static String getMeasurementFileNamePrefix(Measurement measurement) {
        return "m-" + createPrefixProposal(measurement);
    }

    /**
     * Gets the comparison file name prefix.
     * 
     * @param comparison
     *            The comparison.
     * @return The comparison file name prefix. Never {@code null}.
     */
    public static String getComparisonFileNamePrefix(Comparison comparison) {
        return "c-" + createPrefixProposal(comparison);
    }

    /**
     * Gets the formula file name prefix.
     * 
     * @param formula
     *            The formula.
     * @return The formula file name prefix. Never {@code null}.
     */
    public static String getFormulaFileNamePrefix(Formula formula) {
        return "f-" + createPrefixProposal(formula);
    }

    /**
     * @param annotationLocation
     * @return
     */
    public static String getAnnotationFileNamePrefix(AnnotationLocation annotationLocation) {
        return "a-" + createPrefixProposal(annotationLocation);
    }

    /**
     * Creates the prefix proposal based on hash code of provided object.
     * 
     * @param object
     *            The object.
     * @return The prefix proposal. When object is {@code null}, than empty
     *         string is returned.
     */
    public static String createPrefixProposal(Object object) {
        if (object != null) {
            StringBuilder buffer = new StringBuilder(8);
            buffer.append(Integer.toHexString(object.hashCode()));
            while (buffer.length() < 8) {
                buffer.insert(0, '0');
            }
            return buffer.toString();
        } else {
            return "";
        }
    }

}
