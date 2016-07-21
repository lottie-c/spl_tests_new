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
import java.util.LinkedHashSet;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.AnnotationPrettyPrinter;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * <p>
 * The descriptor for overview of suspicious measurements (measurements with
 * warnings when checked using {@link StatisticValueChecker}).
 * 
 * @author Martin Lacina
 */
public class SuspiciousMeasurementsResultDescriptor extends OutputResultDescriptor {

    /** The checker for checked values. */
    private final StatisticValueChecker      checker;

    /** The suspicious measurements. */
    private final Set<SuspiciousMeasurement> suspiciousMeasurements = new LinkedHashSet<>();

    /**
     * Gets the suspicious measurements.
     * 
     * @return The suspicious measurements.
     */
    public Set<SuspiciousMeasurement> getSuspiciousMeasurements() {
        return suspiciousMeasurements;
    }

    /**
     * Overview result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param annotationEvaluationResults
     *            The annotation evaluation results.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public SuspiciousMeasurementsResultDescriptor(Info info, ConfigurationBundle configuration,
            StatisticValueChecker checker, BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks) {
        super(info, configuration, outputLinks, null);
        this.checker = checker;
    }

    public boolean addMeasurementIfSuspicious(MeasurementSample measurementSample) {
        long sampleCount = measurementSample.getSampleCount();
        if (sampleCount > 0) {
            double mean = measurementSample.getMean();
            double standardDeviation = measurementSample.getStandardDeviation();
            double stdVsMean = standardDeviation / mean;
            double median = measurementSample.getMedian();
            double medianVsMean = median / mean;

            if (!(checker.isSampleCountAcceptable(sampleCount)
                    && checker.isStandardDeviationVsMeanAcceptable(stdVsMean)
                    && checker.isMedianVsMeanAcceptable(medianVsMean))) {

                String name = AnnotationPrettyPrinter.createMeasurementOutput(measurementSample.getMeasurement());
                @SuppressWarnings("deprecation")
                String id = measurementSample.getMeasurement().getId();
                suspiciousMeasurements.add(new SuspiciousMeasurement(
                        id, name,
                        sampleCount, mean, standardDeviation,
                        median, stdVsMean, medianVsMean,
                        !checker.isSampleCountAcceptable(sampleCount),
                        !checker.isStandardDeviationVsMeanAcceptable(stdVsMean),
                        !checker.isMedianVsMeanAcceptable(medianVsMean)
                        ));

                return true;
            }

        }
        return false;
    }

    /**
     * The suspicious measurement descriptor.
     */
    public static class SuspiciousMeasurement {

        /** The reference id. */
        private final String  referenceId;
        /** The name. */
        private final String  name;
        /** The sample count. */
        private final long    sampleCount;
        /** The mean. */
        private final double  mean;
        /** The standard deviation. */
        private final double  standardDeviation;
        /** The median. */
        private final double  median;
        /** The std vs mean. */
        private final double  stdVsMean;
        /** The median vs mean. */
        private final double  medianVsMean;
        /** The is sample count suspicious flag. */
        private final boolean isSampleCountSuspicious;
        /** The is std vs mean suspicious flag. */
        private final boolean isStdVsMeanSuspicious;
        /** The is median vs mean suspicious flag. */
        private final boolean isMedianVsMeanSuspicious;

        /**
         * Gets the reference id.
         * 
         * @return The reference id.
         */
        public String getReferenceId() {
            return referenceId;
        }

        /**
         * Gets the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the sample count.
         * 
         * @return The sample count.
         */
        public long getSampleCount() {
            return sampleCount;
        }

        /**
         * Gets the mean.
         * 
         * @return The mean.
         */
        public double getMean() {
            return mean;
        }

        /**
         * Gets the standard deviation.
         * 
         * @return The standard deviation.
         */
        public double getStandardDeviation() {
            return standardDeviation;
        }

        /**
         * Gets the median.
         * 
         * @return The median.
         */
        public double getMedian() {
            return median;
        }

        /**
         * Gets the std vs mean.
         * 
         * @return The std vs mean.
         */
        public double getStdVsMean() {
            return stdVsMean;
        }

        /**
         * Gets the median vs mean.
         * 
         * @return The median vs mean.
         */
        public double getMedianVsMean() {
            return medianVsMean;
        }

        /**
         * Checks if sample count is suspicious.
         * 
         * @return True, if sample count is suspicious.
         */
        public boolean isSampleCountSuspicious() {
            return isSampleCountSuspicious;
        }

        /**
         * Checks if std vs mean is suspicious.
         * 
         * @return True, if std vs mean is suspicious.
         */
        public boolean isStdVsMeanSuspicious() {
            return isStdVsMeanSuspicious;
        }

        /**
         * Checks if median vs mean is suspicious.
         * 
         * @return True, if median vs mean is suspicious.
         */
        public boolean isMedianVsMeanSuspicious() {
            return isMedianVsMeanSuspicious;
        }

        /**
         * Instantiates a new overview node.
         * 
         * @param referenceId
         *            The reference id.
         * @param name
         *            The name.
         * @param sampleCount
         *            The sample count.
         * @param mean
         *            The mean.
         * @param standardDeviation
         *            The standard deviation.
         * @param median
         *            The median.
         * @param stdVsMean
         *            The std vs mean.
         * @param medianVsMean
         *            The median vs mean.
         * @param isSampleCountSuspicious
         *            The is sample count suspicious flag.
         * @param isStdVsMeanSuspicious
         *            The is std vs mean suspicious flag.
         * @param isMedianVsMeanSuspicious
         *            The is median vs mean suspicious flag.
         */
        public SuspiciousMeasurement(String referenceId, String name, long sampleCount, double mean, double standardDeviation, double median, double stdVsMean,
                double medianVsMean, boolean isSampleCountSuspicious, boolean isStdVsMeanSuspicious, boolean isMedianVsMeanSuspicious) {
            this.referenceId = referenceId;
            this.name = name;
            this.sampleCount = sampleCount;
            this.mean = mean;
            this.standardDeviation = standardDeviation;
            this.median = median;
            this.stdVsMean = stdVsMean;
            this.medianVsMean = medianVsMean;
            this.isSampleCountSuspicious = isSampleCountSuspicious;
            this.isStdVsMeanSuspicious = isStdVsMeanSuspicious;
            this.isMedianVsMeanSuspicious = isMedianVsMeanSuspicious;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(medianVsMean);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((referenceId == null) ? 0 : referenceId.hashCode());
            result = prime * result + (int) (sampleCount ^ (sampleCount >>> 32));
            temp = Double.doubleToLongBits(stdVsMean);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SuspiciousMeasurement other = (SuspiciousMeasurement) obj;
            if (Double.doubleToLongBits(medianVsMean) != Double.doubleToLongBits(other.medianVsMean)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (referenceId == null) {
                if (other.referenceId != null) {
                    return false;
                }
            } else if (!referenceId.equals(other.referenceId)) {
                return false;
            }
            if (sampleCount != other.sampleCount) {
                return false;
            }
            if (Double.doubleToLongBits(stdVsMean) != Double.doubleToLongBits(other.stdVsMean)) {
                return false;
            }
            return true;
        }

    }

}
