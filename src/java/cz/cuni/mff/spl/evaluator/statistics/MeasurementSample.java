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
package cz.cuni.mff.spl.evaluator.statistics;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.input.MeasurementSampleDataProvider;
import cz.cuni.mff.spl.evaluator.output.AnnotationPrettyPrinter;

/**
 * <p>
 * Measurement sample data container with basic statistic information - mean,
 * standard deviation, median, minimum and maximum.
 * <p>
 * Measurement sample has state "missing" (i.e. has no valid statistical data)
 * when {@link #getSampleCount()} returns value less than or equal to
 * {@code zero}.
 * 
 * @author Martin Lacina
 */
public class MeasurementSample extends AbstractXmlTransformationReference {

    /**
     * The measurement.
     */
    private Measurement                      measurement;

    /**
     * The measurement sample data provider.
     * 
     * Null value means that no sample data provider is available.
     */
    private MeasurementSampleDataProvider    sampleDataProvider;

    /** The specification. */
    private String                           specification;

    /** The statistical data of sample. */
    private MeasurementSampleStatisticalData statisticalData = new MeasurementSampleStatisticalData();

    /** The statistical summary to use for T-test. */
    private StatisticalSummary               summary;

    /**
     * Instantiates a new measurement statistics.
     * 
     * @param measurement
     *            The measurement.
     * @param specification
     *            The specification.
     * @param dataProvider
     *            The sample data provider for access to data during evaluation.
     * @param data
     *            The sample data.
     */
    public MeasurementSample(Measurement measurement, MeasurementSampleDataProvider dataProvider) {
        this.measurement = measurement;

        this.sampleDataProvider = dataProvider;

        try {
            this.summary = dataProvider.loadStatisticalSummaryForRawData();
            this.statisticalData = dataProvider.loadMeasurementDescriptionSummaryForRawData();
        } catch (MeasurementDataNotFoundException e) {
            this.summary = new DescriptiveStatistics();
            this.statisticalData = new MeasurementSampleStatisticalData();
        }
    }

    /**
     * Instantiates a new measurement sample.
     */
    @Deprecated
    public MeasurementSample() {

    }

    /**
     * Gets the measurement.
     * 
     * @return The measurement.
     */
    public Measurement getMeasurement() {
        return measurement;
    }

    /**
     * Sets the measurement.
     * 
     * @param measurement
     *            The new measurement.
     */
    public void setMeasurement(Measurement measurement) {
        this.measurement = measurement;
    }

    /**
     * Gets the measurement sample data provider.
     * 
     * @return The measurement sample data provider.
     */
    public MeasurementSampleDataProvider getSampleDataProvider() {
        return sampleDataProvider;
    }

    /**
     * Sets the measurement sample data provider.
     * 
     * @param sampleDataProvider
     *            The new sample data provider.
     */
    public void setSampleDataProvider(MeasurementSampleDataProvider sampleDataProvider) {
        this.sampleDataProvider = sampleDataProvider;
    }

    /**
     * Gets the sample count. When returned value is less than or equal to zero,
     * than measurement data are missing (i.e. they are not measured).
     * 
     * @return The sample count.
     */
    public long getSampleCount() {
        return this.statisticalData.sampleCount;
    }

    /**
     * Sets the sample count.
     * 
     * @param sampleCount
     *            The new sample count.
     */
    @Deprecated
    public void setSampleCount(int sampleCount) {
        this.statisticalData.sampleCount = sampleCount;
    }

    /**
     * Gets the specification.
     * 
     * @return The specification.
     */
    public String getSpecification() {
        if (specification == null) {
            specification = AnnotationPrettyPrinter.createMeasurementOutput(measurement);
        }
        return specification;
    }

    /**
     * Sets the specification.
     * 
     * @param specification
     *            The new specification.
     */
    @Deprecated
    public void setSpecification(String specification) {
        this.specification = specification;
    }

    /**
     * Gets the standard deviation.
     * 
     * @return statisticalData.The standard deviation.
     */
    public double getStandardDeviation() {
        return statisticalData.standardDeviation;
    }

    /**
     * Sets the standard deviation.
     * 
     * @param standardDeviation
     *            The new standard deviation.
     */
    @Deprecated
    public void setStandardDeviation(double standardDeviation) {
        this.statisticalData.standardDeviation = standardDeviation;
    }

    /**
     * Gets the sample variance.
     * 
     * @return The sample variance.
     */
    public double getVariance() {
        return statisticalData.variance;
    }

    /**
     * Sets the sample variance.
     * 
     * @param variance
     *            The new sample variance.
     */
    @Deprecated
    public void setVariance(double variance) {
        this.statisticalData.variance = variance;
    }

    /**
     * Gets the mean.
     * 
     * @return statisticalData.The mean.
     */
    public double getMean() {
        return statisticalData.mean;
    }

    /**
     * Sets the mean.
     * 
     * @param mean
     *            The new mean.
     */
    @Deprecated
    public void setMean(double mean) {
        this.statisticalData.mean = mean;
    }

    /**
     * Gets the median.
     * 
     * @return statisticalData.The median.
     */
    public double getMedian() {
        return statisticalData.median;
    }

    /**
     * Sets the median.
     * 
     * @param median
     *            The new median.
     */
    @Deprecated
    public void setMedian(double median) {
        this.statisticalData.median = median;
    }

    /**
     * Gets the minimum.
     * 
     * @return statisticalData.The minimum.
     */
    public double getMinimum() {
        return statisticalData.minimum;
    }

    /**
     * Sets the minimum.
     * 
     * @param minimum
     *            The new minimum.
     */
    @Deprecated
    public void setMinimum(double minimum) {
        this.statisticalData.minimum = minimum;
    }

    /**
     * Gets the maximum.
     * 
     * @return statisticalData.The maximum.
     */
    public double getMaximum() {
        return statisticalData.maximum;
    }

    /**
     * Sets the maximum.
     * 
     * @param maximum
     *            The new maximum.
     */
    @Deprecated
    public void setMaximum(double maximum) {
        statisticalData.maximum = maximum;
    }

    /**
     * Gets the statistical data.
     * 
     * @return The statistical data.
     */
    public MeasurementSampleStatisticalData getStatisticalData() {
        return statisticalData;
    }

    /**
     * Sets the statistical data and creates statistical summary with provided
     * data.
     * 
     * @param statisticalData
     *            The new statistical data.
     */
    @Deprecated
    public void setStatisticalData(MeasurementSampleStatisticalData statisticalData) {
        this.statisticalData = statisticalData;
        this.summary =
                new StatisticalSummaryValues(
                        statisticalData.mean, statisticalData.variance, statisticalData.sampleCount, statisticalData.maximum,
                        statisticalData.minimum, statisticalData.mean * statisticalData.sampleCount);
    }

    /**
     * Gets the statistical summary.
     * 
     * @return The statistical summary.
     */
    public StatisticalSummary getStatisticalSummary() {
        return this.summary;
    }

    /**
     * Percentile.
     * 
     * @param p
     *            The percentile value to get. In percent, i. e. value in
     *            interval (0.0,
     *            100.0].
     * @return Percentile estimation.
     * @see StatUtils#percentile(double[], double)
     * 
     * @throws IllegalArgumentException
     *             Throws IllegalArgumentException if values is null or p is not
     *             a valid percentile value (p must be greater than 0 and less
     *             than or equal to 100).
     * 
     * @throws IllegalStateException
     *             Thrown when measurement sample data provider is null.
     */
    public double computeSampleDataPercentile(double lambdaMultiplier, double p) throws MeasurementDataNotFoundException {
        if (this.sampleDataProvider == null) {
            throw new IllegalStateException("No measurement sample data provider set.");
        }
        return StatUtils.percentile(this.sampleDataProvider.loadRawData(lambdaMultiplier), p);
    }

    /**
     * Loads measurement sample data and clips them based on provided percentile
     * values.
     * 
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @param lowerClip
     *            The lower clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @param upperClip
     *            The upper clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @return Clipped sample data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     * @see DataClipper#quantileClip(MeasurementSample, double, double)
     * @see Percentile
     */
    public double[] loadQuantileClippedData(double lambdaMultiplier, double lowerClip, double upperClip) throws MeasurementDataNotFoundException {
        if (this.sampleDataProvider == null) {
            throw new IllegalStateException("No measurement sample data provider set.");
        }
        return this.sampleDataProvider.loadQuantileClippedData(lambdaMultiplier, lowerClip, upperClip);
    }

    /**
     * Returns sigma clipped data.
     * 
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @param sigmaMultiplier
     *            The sigma multiplier.
     * @param maxIterations
     *            The max iterations.
     * @return The sigma clipped data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     */
    public double[] loadSigmaClippedData(double lambdaMultiplier, double sigmaMultiplier, int maxIterations) throws MeasurementDataNotFoundException {
        if (this.sampleDataProvider == null) {
            throw new IllegalStateException("No measurement sample data provider set.");
        }
        return sampleDataProvider.loadSigmaClippedData(lambdaMultiplier, sigmaMultiplier, maxIterations);
    }

    /**
     * Loads raw sample data.
     * 
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @return The raw sample data.
     * @throws MeasurementDataNotFoundException
     *             The measurement data not found exception.
     */
    public double[] loadRawData(double lambdaMultiplier) throws MeasurementDataNotFoundException {
        if (this.sampleDataProvider == null) {
            throw new IllegalStateException("No measurement sample data provider set.");
        }
        return this.sampleDataProvider.loadRawData(lambdaMultiplier);
    }

    /**
     * Creates the invalid measurement sample which does not represent
     * sample data.
     * 
     * @param measurement
     *            The measurement.
     * @param specification
     *            The specification.
     * @return The measurement sample.
     */
    public static MeasurementSample createInvalidMeasurementSample(Measurement measurement, String specification) {
        MeasurementSample measurementSample = new MeasurementSample();
        measurementSample.setMeasurement(measurement);
        measurementSample.setSpecification(specification);
        return measurementSample;
    }

}
