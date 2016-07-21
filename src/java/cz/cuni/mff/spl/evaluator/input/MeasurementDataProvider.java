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
package cz.cuni.mff.spl.evaluator.input;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;

/**
 * <p>
 * Provider of measurement sample data.
 * <p>
 * Allows abstraction of data location and hides the loading mechanism.
 * <p>
 * Data can be placed in folder, on FTP server or obtained in other way.
 * <p>
 * This interface allows to get instances of
 * {@link MeasurementSampleDataProvider} for loading measurement data for
 * specific measurement identified with the {@link SampleIdentification}.
 * 
 * @author Martin Lacina
 * 
 */
public interface MeasurementDataProvider {

    /**
     * Gets the measurement specific sample data.
     * 
     * 
     * @param measurement
     *            The measurement to get data for.
     * @return The measurement data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement data were not found.
     */
    MeasurementSampleDataProvider getMeasurementData(SampleIdentification measurement) throws MeasurementDataNotFoundException;

    /**
     * Checks if measurement exists.
     * 
     * @param identification
     *            The measurement identification.
     * @return True, if measurement exists.
     */
    boolean measurementExists(SampleIdentification identification);

    /**
     * The exception which is thrown by {@link MeasurementDataProvider} when no
     * data for specified measurement were not found.
     * 
     * @author Martin Lacina
     */
    public static class MeasurementDataNotFoundException extends Exception {

        /**
         * Instantiates a new measurement data not found exception.
         */
        public MeasurementDataNotFoundException(SampleIdentification identification) {
            super(identification != null ? identification.getIdentification() : "Sample identification is null");
        }

        /**
         * Instantiates a new measurement data not found exception.
         * 
         * @param cause
         *            The exception which caused this.
         */
        public MeasurementDataNotFoundException(SampleIdentification identification, Throwable cause) {
            super(identification != null ? identification.getIdentification() : "Sample identification is null", cause);
        }

        /**
         * Instantiates a new measurement data not found exception.
         */
        public MeasurementDataNotFoundException() {
        }

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 2924429955212754408L;
    }

    /**
     * The exception which is thrown by.
     * 
     * {@link MeasurementDataProvider#createMeasurementDataStream(SampleIdentification, double[])}
     * when data for specified measurement were already stored.
     * 
     * @author Martin Lacina
     */
    public static class MeasurementDataFoundException extends Exception {

        /**
         * Serialization ID.
         */
        private static final long serialVersionUID = 2924429955212754490L;
    }
}
