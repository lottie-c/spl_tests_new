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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.util.ArrayList;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The measurement evaluation result descriptor for XSLT transformation.
 * 
 * @author Martin Lacina
 */
public class MeasurementResultDescriptor extends OutputResultDescriptor {

    /** The measurement sample. */
    private final MeasurementSample measurementSample;

    /**
     * Gets the measurement sample.
     * 
     * @return The measurement sample.
     */
    public MeasurementSample getMeasurementSample() {
        return measurementSample;
    }

    /** The validation flags. */
    private final MeasurementValidationFlags validationFlags = new MeasurementValidationFlags();

    public MeasurementValidationFlags getValidationFlags() {
        return validationFlags;
    }

    /**
     * Instantiates a new measurement result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param measurementSample
     *            The measurement sample.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public MeasurementResultDescriptor(Info info, ConfigurationBundle configuration, MeasurementSample measurementSample, StatisticValueChecker checker,
            BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks, AnnotationValidationFlags globalAliasesSummary) {
        super(info, configuration, outputLinks, globalAliasesSummary);
        this.measurementSample = measurementSample;

        this.validationFlags.setFlags(measurementSample, checker);

        fillGraphReferences(graphsMapping, configuration.getEvaluatorConfig().getMeasurementGraphTypes(), measurementSample.getMeasurement());
    }

    /**
     * The measurement validation flags.
     * 
     * @author Martin Lacina
     */
    public static class MeasurementValidationFlags {

        /** The std vs mean ok. */
        public boolean stdVsMeanOk;

        /** The std vs mean. */
        public double  stdVsMean;

        /** The median vs mean ok. */
        public boolean medianVsMeanOk;

        /** The median vs mean. */
        public double  medianVsMean;

        /** The sample count ok. */
        public boolean sampleCountOk;

        /**
         * Sets the flags.
         * 
         * @param measurementSample
         *            The measurement sample.
         * @param checker
         *            The checker.
         */
        public void setFlags(MeasurementSample measurementSample, StatisticValueChecker checker) {
            this.sampleCountOk = checker.isSampleCountAcceptable(measurementSample.getSampleCount());
            this.stdVsMean = measurementSample.getStandardDeviation() / measurementSample.getMean();
            this.stdVsMeanOk = checker.isStandardDeviationVsMeanAcceptable(this.stdVsMean);
            this.medianVsMean = measurementSample.getMedian() / measurementSample.getMean();
            this.medianVsMeanOk = checker.isMedianVsMeanAcceptable(this.medianVsMean);
        }

    }
}
