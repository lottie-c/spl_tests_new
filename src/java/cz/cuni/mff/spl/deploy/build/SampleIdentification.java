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
package cz.cuni.mff.spl.deploy.build;

import cz.cuni.mff.spl.annotation.AnnotationToString;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.evaluator.FileNameMapper;

/**
 * 
 * Represents identification of measurement sample allowing to identify its file
 * with data.
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 * 
 */
public class SampleIdentification {

    /**
     * Creates the file name prefix for measurement sample file.
     * 
     * Hash code of measurement is encoded to hexadecimal string as it not cover
     * entire project of method/generator, but only specific revision.
     * 
     * @param measurement
     *            The measurement.
     * @return The file name prefix for measurement sample file.
     */
    public static String createFileNamePrefix(Measurement measurement) {
        return FileNameMapper.getMeasurementFileNamePrefix(measurement);
    }

    /**
     * Creates the identification of measurement.
     * 
     * Uses {@link AnnotationToString#getIdentificationString()}, which is
     * expected to contain no new lines.
     * 
     * @param measurement
     *            The measurement.
     * @return The identification of measurement as one line string.
     */
    public static String createIdentification(Measurement measurement) {
        return "#" + measurement.getIdentificationString().replace('\\', '/');
    }

    /**
     * The identification of measurement.
     * To be used for identification of measurement.
     */
    private final String identification;

    /** The file name prefix for measurement sample. */
    private final String fileNamePrefix;

    /**
     * Instantiates a new sample identification.
     * 
     * @param measurement
     *            The measurement to identify.
     */
    public SampleIdentification(Measurement measurement) {
        fileNamePrefix = createFileNamePrefix(measurement);
        identification = createIdentification(measurement);
    }

    /**
     * For purpose of serialization.
     */
    public SampleIdentification(String fileNamePrefix, String identification) {
        this.fileNamePrefix = fileNamePrefix;
        this.identification = identification;
    }

    /**
     * Gets the identification of measurement.
     * 
     * @return The identification of measurement.
     * 
     * @see AnnotationToString#getIdentificationString()
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * Gets the file name prefix for measurement sample.
     * 
     * @return The file name prefix for measurement sample.
     */
    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public boolean equals(SampleIdentification identification) {
        return this.identification.equals(identification.identification);
    }

    public boolean equals(String identification) {
        return this.identification.equals(identification);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileNamePrefix == null) ? 0 : fileNamePrefix.hashCode());
        result = prime * result + ((identification == null) ? 0 : identification.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SampleIdentification) {
            return ((SampleIdentification) object).equals(identification);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return identification;
    }

    public static String serialize(SampleIdentification identification) {
        return identification.fileNamePrefix + "<>" + identification.identification;
    }

    public static SampleIdentification deserialize(String serializedIdentification) {
        String[] split = serializedIdentification.split("<>");
        return new SampleIdentification(split[0], split[1]);
    }
}
