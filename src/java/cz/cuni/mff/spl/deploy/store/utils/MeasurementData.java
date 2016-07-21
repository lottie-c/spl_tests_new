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
package cz.cuni.mff.spl.deploy.store.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.deploy.build.Code;
import cz.cuni.mff.spl.utils.ConvertUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class makes reading measurement data easier.
 * 
 * <p>
 * Allows to read both measurement samples and stored meatadata (date and time
 * of measurement, sample count, warmup samples).
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 */
public class MeasurementData implements AutoCloseable {

    private static final SplLog       logger               = SplLogger.getLogger(MeasurementData.class);

    /** Measurement data stream. */
    private final BufferedReader      input;

    /** Properties loaded from measurement data file. */
    private final Map<String, String> properties           = new HashMap<>();

    /** Identification of measurement. */
    private String                    identification       = "<unknown>";

    /** Sample value returned in case of stream eof. */
    private final double              eofSample            = -1;
    private boolean                   hasSample            = false;
    private double                    sample               = eofSample;

    /**
     * The constant for unknown warm-up sample count returned by
     * {@link #getWarmupCount()}.
     */
    public static final int           UNKNOWN_WARMUP_COUNT = -1;

    /**
     * The constant for expected measurement sample count returned by
     * {@link #getExpectedSampleCount()}.
     */
    public static final int           UNKNOWN_SAMPLE_COUNT = -1;

    /**
     * <p>
     * Opens the stream to data and reads properties stored at the beginning of
     * the file.
     * 
     * @param input
     *            The stream to measurement data.
     * @throws IOException
     */
    public MeasurementData(InputStream input)
            throws IOException {
        this.input = new BufferedReader(new InputStreamReader(input));
        open();
    }

    /**
     * <p>
     * Reads the beginning of the file and obtains all properties. Stops reading
     * at sample values.'
     * 
     * @throws IOException
     */
    private void open()
            throws IOException {
        String line = input.readLine();

        /**
         * <p>
         * Read identification. Kind of hack for compatibility reasons.
         */
        if (line != null && line.startsWith(Code.COMMENT)) {
            identification = line.substring(Code.COMMENT.length());
            line = input.readLine();
        }

        /**
         * <p>
         * Read all other properties.
         */
        while (line != null) {
            if (line.startsWith(Code.MARK_SAMPLES_BEGIN)) {
                line = input.readLine();
                break;
            }

            if (line.startsWith(Code.COMMENT)) {
                properties.put(
                        line.substring(0, line.indexOf(Code.SIGN) + Code.SIGN.length()),
                        line.substring(line.indexOf(Code.SIGN) + Code.SIGN.length()));
            }

            line = input.readLine();
        }

        /**
         * <p>
         * Read first sample.
         */
        try {

            if (line != null) {
                sample = eofSample;
                sample = Double.parseDouble(line);
                hasSample = true;
            }

        } catch (NumberFormatException e) {
            throw new IOException(String.format("Failed to read data for measurement [%s]", identification), e);
        }
    }

    /**
     * <p>
     * Returns measurement identification or 'unknown' if failed even to read
     * the identification.
     * 
     * @return
     */
    public String getIdentification() {
        return identification;
    }

    /**
     * <p>
     * Returns the date when the measurement was performed or {@code null}, when
     * measurement date is not known.
     * 
     * @return The date when the measurement was performed or {@code null}, when
     *         measurement date is not known.
     */
    public String getDate() {
        if (properties.containsKey(Code.PROPERTY_DATE)) {
            return properties.get(Code.PROPERTY_DATE);
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Returns how many warmup cycles were actually performed, or
     * {@link UNKNOWN_WARMUP_COUNT} when this value was not specified.
     * 
     * @return How many warmup cycles were actually performed, or
     *         {@link UNKNOWN_WARMUP_COUNT} when this value was not specified.
     * @throws IOException
     */
    public Integer getWarmupCount() {
        try {
            return Integer.valueOf(properties.get(Code.PROPERTY_WARMUP_COUNT));
        } catch (NullPointerException | NumberFormatException e) {
            logger.debug(e, "Failed to get warmup count.");
            return UNKNOWN_WARMUP_COUNT;
        }
    }

    /**
     * <p>
     * Returns how many samples are expected to be present, or
     * {@link UNKNOWN_SAMPLE_COUNT} when this value was not specified.
     * 
     * @return how many samples are expected to be present, or
     *         {@link UNKNOWN_SAMPLE_COUNT} when this value was not specified.
     * @throws IOException
     */
    public int getExpectedSampleCount()
            throws IOException {
        try {
            return Integer.valueOf(properties.get(Code.PROPERTY_SAMPLE_COUNT));
        } catch (NullPointerException | NumberFormatException e) {
            logger.debug(e, "Failed to get sample count.");
            return UNKNOWN_SAMPLE_COUNT;
        }
    }

    /**
     * <p>
     * Returns true if more samples can be read.
     * 
     * @return
     * 
     * @see MeasurementData#readSample()
     */
    public boolean hasSample() {
        return hasSample;
    }

    /**
     * <p>
     * Reads the next sample if such is present. If it's not present '-1' is
     * returned.
     * 
     * @return
     * @throws IOException
     * 
     * @see MeasurementData#hasSample()
     * @see MeasurementData#readSamples()
     */
    public double readSample()
            throws IOException {

        double lastSample = sample;
        sample = eofSample;
        hasSample = false;
        try {
            String line = input.readLine();
            if (line != null && !line.startsWith(Code.MARK_SAMPLES_END)) {
                sample = Double.parseDouble(line);
                hasSample = true;
            }

        } catch (NumberFormatException | IOException e) {
            throw new IOException(String.format("Failed to read data for measurement [%s]", identification), e);
        }

        return lastSample;
    }

    /**
     * <p>
     * Reads all the samples into one big array.
     * 
     * @return
     * @throws IOException
     */
    public double[] readSamples(double lambdaMultiplier)
            throws IOException {

        int expectedSampleCount = getExpectedSampleCount();
        List<Double> samples;
        if (expectedSampleCount == UNKNOWN_SAMPLE_COUNT) {
            samples = new ArrayList<>();
        } else {
            samples = new ArrayList<>(expectedSampleCount);
        }

        while (hasSample) {
            samples.add(lambdaMultiplier * readSample());
        }

        sample = eofSample;
        hasSample = false;

        return ConvertUtils.convertDoublesToArray(samples, new double[samples.size()]);
    }

    @Override
    public void close()
            throws IOException {
        input.close();
    }
}
