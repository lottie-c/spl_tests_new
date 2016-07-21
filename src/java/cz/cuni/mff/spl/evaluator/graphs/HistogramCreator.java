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
package cz.cuni.mff.spl.evaluator.graphs;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.statistics.DataClipper;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.SimpleStatisticalFunctions;

/**
 * The implementation for histogram graph creation.
 * 
 * @author Martin Lacina
 */
public class HistogramCreator {

    /** The graph utilities to use. */
    private final GraphUtils graphUtils;

    /** The default minimum histogram bin count. */
    public static final int  DEFAULT_MINIMUM_HISTOGRAM_BIN_COUNT = 100;

    /** The default minimum histogram bin count. */
    public static final int  DEFAULT_MAXIMUM_HISTOGRAM_BIN_COUNT = 10000;

    /** The minimum histogram bin count. */
    private final int        minimumHistogramBinCount;
    /** The minimum histogram bin count. */
    private final int        maximumHistogramBinCount;

    /**
     * Instantiates a new histogram graph creator.
     * <p>
     * Provided minimum and maximum values can be swapped, i. e. higher will be
     * used for maximum and lower for minimum.
     * 
     * @param graphUtils
     *            The graph utilities to use.
     * @param minHistogramBinCount
     *            The minimum histogram bin count.
     * @param maxHistogramBinCount
     *            The maximum histogram bin count.
     */
    public HistogramCreator(GraphUtils graphUtils) {
        int minHistogramBinCount = graphUtils.getConfiguration().getHistogramMaximumBinCount();
        int maxHistogramBinCount = graphUtils.getConfiguration().getHistogramMinimumBinCount();
        this.graphUtils = graphUtils;
        this.minimumHistogramBinCount = Math.max(DEFAULT_MINIMUM_HISTOGRAM_BIN_COUNT, Math.min(minHistogramBinCount, maxHistogramBinCount));
        this.maximumHistogramBinCount = Math.min(DEFAULT_MAXIMUM_HISTOGRAM_BIN_COUNT, Math.max(minHistogramBinCount, maxHistogramBinCount));
    }

    /**
     * Creates the histogram chart.
     * 
     * @param plotTitle
     *            The plot title.
     * @param plotSubtitle
     *            The plot subtitle.
     * @param data
     *            The data.
     * @param xaxisTitle
     *            The X axis title.
     * @param yaxisTitle
     *            The Y axis title.
     * @param binCount
     *            The histogram bin count.
     * @param showLegend
     *            The show legend flag.
     * @return The histogram chart.
     */
    public JFreeChart createHistogram(String plotTitle, String plotSubtitle, List<HistogramSeries> data, String xaxisTitle, String yaxisTitle,
            int binCount,
            boolean showLegend) {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.FREQUENCY);
        for (HistogramSeries oneData : data) {
            dataset.addSeries(oneData.title, oneData.data,
                    binCount);
        }
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean toolTips = false;
        boolean urls = false;
        final JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxisTitle,
                yaxisTitle, dataset, orientation, showLegend, toolTips, urls);

        if (plotSubtitle != null && !plotSubtitle.isEmpty()) {
            chart.addSubtitle(new TextTitle(plotSubtitle));
        }

        graphUtils.configureChartWithDefaults(chart);

        return chart;
    }

    /**
     * The Class HistogramSeries.
     */
    public static class HistogramSeries {

        /** The series data. */
        public final double[] data;

        /** The series title. */
        public final String   title;

        /**
         * Instantiates a new histogram series.
         * 
         * @param title
         *            The series title.
         * @param data
         *            The series data.
         */
        public HistogramSeries(String title, double[] data) {
            this.data = data;
            this.title = title;
        }
    }

    /**
     * Calculates estimated bin count for histogram.
     * 
     * @param data
     *            The series data.
     * @param minimumHistogramBinCount
     *            The minimum histogram bin count.
     * @param maximumHistogramBinCount
     *            The maximum histogram bin count.
     * @return Estimated bin count.
     * @see http://www.ehow.com/how_8485512_determine-bin-width-histogram.html
     * 
     *      <p>
     *      1 Calculate the value of the cube root of the number of data points
     *      that will make up your histogram. For example, if you are making a
     *      histogram of the height of 200 people, you would take the cube root
     *      of 200, which is 5.848. Most scientific calculators will have a cube
     *      root function that you can use to perform this calculation.
     *      <p>
     *      2 Take the inverse of the value you just calculated. To do this, you
     *      can divide the value into 1 or use the "1/x" key on a scientific
     *      calculator. The inverse of 5.848 is 1/5.848 = 0.171.
     *      <p>
     *      3 Multiply your new value by the standard deviation (s) of your data
     *      set. The standard deviation is a measure of the amount of variation
     *      in a series of numbers. You can use a calculator with statistical
     *      functions to calculate s for your data or use the "STDEV" function
     *      in Microsoft Excel. If the standard deviation of your height data
     *      was 2.8 inches, you would calculate (2.8)(0.171) = 0.479.
     *      <p>
     *      4 Multiply the number you just derived by 3.49. The value 3.49 is a
     *      constant derived from statistical theory and the result of this
     *      calculation is the bin width you should use to construct a histogram
     *      of your data. In the case of the height example, you would calculate
     *      (3.49)(0.479) = 1.7 inches. This means that, if your lowest height
     *      was (for example) 5 feet, your first bin would span 5 feet to 5 feet
     *      1.7 inches. The height of the column for this bin would depend on
     *      how many of your 200 measured heights were within this range. The
     *      next bin would be from 5 feet 1.7 inches to 5 feet 3.4 inches, and
     *      so on.
     */
    public static int calculateBinCount(double[] data, int minimumHistogramBinCount, int maximumHistogramBinCount) {

        double cubeRoot = Math.cbrt(data.length);

        double inverse = 1 / cubeRoot;

        double multipliedByStandardDeviation = inverse * (SimpleStatisticalFunctions.sd(data) / 10000);

        double magicResult = 3.49 * multipliedByStandardDeviation;

        int result = (int) Math.ceil(magicResult);

        int finalResult = Math.min(maximumHistogramBinCount, Math.max(minimumHistogramBinCount, result));

        return finalResult;
    }

    /**
     * Calculates histogram bin count. Just shortcut to
     * {@link #calculateBinCount(double[], int, int)}.
     * 
     * @param data
     *            The data.
     * @return The estimated bin count.
     * @see #calculateBinCount(double[], int, int)
     */
    public int calculateBinCount(double[] data) {
        return calculateBinCount(data, minimumHistogramBinCount, maximumHistogramBinCount);
    }

    /**
     * Creates the histogram.
     * 
     * @param definition
     *            The definition.
     * @param samples
     *            The sample data.
     * @return The histogram chart.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample sample data were not found.
     */
    public JFreeChart createHistogram(GraphDefinition definition, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {

        List<HistogramSeries> data = new LinkedList<>();
        Set<MeasurementSampleDescriptor> processedSamples = new HashSet<>();

        int failed = 0;
        int binCount = 0;
        for (MeasurementSampleDescriptor sample : samples) {
            if (!processedSamples.contains(sample)) {
                processedSamples.add(sample);
                try {
                    double[] clippedData = graphUtils.loadSampleData(definition, sample);
                    data.add(new HistogramSeries(sample.getSpecification(), clippedData));
                    binCount = Math.max(binCount, calculateBinCount(clippedData));
                } catch (MeasurementDataNotFoundException e) {
                    data.add(new HistogramSeries("Measurement sample data not found: " + sample.getSpecification(), new double[] { 0 }));
                    ++failed;
                }
            }
        }

        if (failed == data.size()) {
            throw new MeasurementDataNotFoundException();
        }

        String subtitle = samples.length == 1 ? samples[0].getSpecification() : "";

        final JFreeChart chart = createHistogram(String.format("Histogram (%s)", definition.getDataClipTypePrettyString()),
                subtitle, data, "Execution time [ns]", "Frequency", binCount, samples.length != 1);
        return chart;
    }

    /**
     * Creates the quantile clipped histogram.
     * 
     * @param sampleData
     *            The sample data.
     * @param lowerClip
     *            The lower clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @param upperClip
     *            The upper clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @return PNG encoded image as byte array, or {@code null} when encoding
     *         error occurs.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample sample data were not found.
     * @throws IllegalArgumentException
     *             When provided clip values are outside of range [0.0, 100.0]
     *             or when lower clip is higher than upper clip.
     * 
     * @see DataClipper#quantileClip(MeasurementSample, double, double)
     */
    public byte[] createHistogramPNG(GraphDefinition definition, MeasurementSampleDescriptor... sampleData) throws MeasurementDataNotFoundException {
        return graphUtils.chartToPNG(createHistogram(definition, sampleData));
    }

}
