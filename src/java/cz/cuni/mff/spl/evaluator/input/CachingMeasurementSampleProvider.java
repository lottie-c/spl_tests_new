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

import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.output.AnnotationPrettyPrinter;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;

/**
 * The measurement sample provider. Provided measurement samples are cached in
 * memory.
 * 
 * @author Martin Lacina
 * 
 * @see MeasurementDataProvider
 */
public class CachingMeasurementSampleProvider implements MeasurementSampleProvider {

    /** The measurement data provider. */
    private final MeasurementDataProvider             measurementDataProvider;

    /** The cached samples. */
    private final Map<Measurement, MeasurementSample> cachedSamples = new HashMap<>();

    /**
     * Instantiates a new caching measurement sample provider.
     * 
     * @param measurementDataProvider
     *            The measurement data provider.
     */
    public CachingMeasurementSampleProvider(MeasurementDataProvider measurementDataProvider) {
        this.measurementDataProvider = measurementDataProvider;
    }

    @Override
    public MeasurementSample getMeasurementSample(Measurement measurement) throws MeasurementDataNotFoundException {
        MeasurementSample sample = cachedSamples.get(measurement);
        if (sample == null) {
            MeasurementSampleDataProvider sampleDataProvider = measurementDataProvider.getMeasurementData(new SampleIdentification(measurement));
            sample = new MeasurementSample(measurement, sampleDataProvider);
            cachedSamples.put(measurement, sample);
        }
        return sample;
    }

    @Override
    public MeasurementSample getInvalidMeasurementSample(Measurement measurement) {
        return MeasurementSample.createInvalidMeasurementSample(measurement, AnnotationPrettyPrinter.createMeasurementOutput(measurement));
    }
}
