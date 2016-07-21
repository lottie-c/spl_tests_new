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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.NormalDistributionFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.r.RProjectCaller;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * The implementation for probability density graph creation.
 * 
 * There are two implementation for obtaining probability density function -
 * RCaller to user R-project runtime and simple estimation {@link LambdaVoronoi}
 * . RCaller is preferred, but if it fails, than the other is used for all
 * following calls.
 * 
 * @author Martin Lacina
 * 
 * @see "http://people.sc.fsu.edu/~hnguyen/Presentation/hoa_Auburn_modified_0327.pdf"
 */
public class ProbabilityDensityGraphCreator {

    /** The logger. */
    private static final SplLog  logger              = SplLogger.getLogger(ProbabilityDensityGraphCreator.class);

    /** The flag indicating if {@link RProjectCaller} works. */
    private boolean              RProjectCallerWorks = true;

    /** The r project caller. */
    private final RProjectCaller rCaller;

    /** The graph utilities to use. */
    private final GraphUtils     graphUtils;

    /**
     * Instantiates a new probability density graph creator.
     * 
     * @param graphUtils
     *            The graph utilities to use.
     */
    public ProbabilityDensityGraphCreator(GraphUtils graphUtils, File workingDirectory) {
        this.graphUtils = graphUtils;
        this.rCaller = new RProjectCaller(this.graphUtils.getConfiguration().getRScriptCommand(), workingDirectory);
    }

    /** The samples count for sampling function. */
    private static final int   SAMPLES_COUNT                        = 100000;

    /**
     * The maximum density to show on y axis (when comparing to normal
     * distribution).
     */
    public static final double DEFAULT_MAXIMUM_DENSITY_Y_AXIS_VALUE = 10e-5d;

    /**
     * Creates the density comparison graph for provided measurement samples.
     * <p>
     * Graph is generated only when sample data are found for at least one of
     * provided samples. When no sample data are found, than
     * {@link MeasurementDataNotFoundException} exception is thrown.
     * <p>
     * The sample data will not be clipped.
     * 
     * @param samples
     *            The measurement samples.
     * @return Density comparison graph.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample data were not found.
     */
    public JFreeChart createDensityComparisonGraph(GraphDefinition definition,
            MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {
        double scanFrom = Double.NEGATIVE_INFINITY;
        double scanTo = Double.POSITIVE_INFINITY;

        XYSeriesCollection dataset = new XYSeriesCollection();

        Set<MeasurementSampleDescriptor> processedSamples = new HashSet<>();

        int failed = 0;

        for (MeasurementSampleDescriptor sample : samples) {
            if (!processedSamples.contains(sample)) {
                processedSamples.add(sample);
                try {
                    XYSeries densitySeries = createDensitySeriesFor(definition, sample, scanFrom, scanTo);
                    dataset.addSeries(densitySeries);
                } catch (MeasurementDataNotFoundException e) {
                    dataset.addSeries(new XYSeries("Measurement sample data not found: "
                            + (sample.isApplyLambda() ? Double.toString(sample.getLambdaMultiplier()) + " * " : "")
                            + sample.getMeasurementSample().getSpecification()));
                    ++failed;
                }

            }
        }

        if (failed == dataset.getSeriesCount()) {
            throw new MeasurementDataNotFoundException();
        }

        boolean cutYaxis = false;
        double cutYaxisValue = 0;
        // add normal distribution comparison when only one sample present
        if (samples.length == 1) {
            cutYaxis = true;
            StatisticalSummary data = samples[0].getStatisticalSummary();

            if (data.getStandardDeviation() > 0) {
                Function2D normal = new NormalDistributionFunction2D(data.getMean(),
                        data.getStandardDeviation());
                XYSeries normalDensitySeries = DatasetUtilities.sampleFunction2DToSeries(normal,
                        dataset.getSeries(0).getMinX(), dataset.getSeries(0).getMaxX(), SAMPLES_COUNT,
                        String.format("Normal distribution (%.3f, %.3f)", data.getMean(), data.getStandardDeviation())
                        );
                cutYaxisValue = normalDensitySeries.getMaxY();
                dataset.addSeries(normalDensitySeries);
            }
        }

        final JFreeChart chart = ChartFactory.createXYAreaChart(
                String.format("Probability density comparison (%s)", definition.getDataClipTypePrettyString()),
                "Execution time [ns]", "Probability density",
                dataset, PlotOrientation.VERTICAL, true, false, false);

        return finishChart(chart, cutYaxis, cutYaxisValue);
    }

    /**
     * Creates the normal distribution comparison graph as PNG image.
     * 
     * The sample data will be first 3-sigma clipped to filter some of the
     * outliers.
     * 
     * @param samples
     *            The sample data.
     * @return PNG encoded image as byte array, or {@code null} when error
     *         occurs.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample data were not found.
     */
    public byte[] createDensityComparisonGraphPNG(GraphDefinition definition,
            MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {
        return graphUtils.chartToPNG(createDensityComparisonGraph(definition, samples));
    }

    /**
     * <p>
     * Finishes density comparison chart. Limits Y axis range to maximum value
     * specified in {@link #DEFAULT_MAXIMUM_DENSITY_Y_AXIS_VALUE}.
     * <p>
     * When normal distribution function is not {@code null}), than it should
     * will be made visible.
     * 
     * @param chart
     *            The chart.
     * @param cutYAxis
     *            The flag indicating whether to cut y axis.
     * @param cutYseriesMinimumValue
     *            The Y axis minimum range (to show full normal distribution).
     * @return PNG encoded image as byte array.
     */
    private JFreeChart finishChart(JFreeChart chart, boolean cutYAxis, double cutYseriesMinimumValue) {
        // force Y axis to start with 0.
        chart.getXYPlot().getRangeAxis().setRange(0, chart.getXYPlot().getRangeAxis().getRange().getUpperBound());
        // cut Y axis
        if (cutYAxis) {
            Range yRange = chart.getXYPlot().getRangeAxis().getRange();
            double limit = graphUtils.getConfiguration().getGraphMaximumNormalDensityYAxisLimit();
            if (yRange.getUpperBound() > limit) {
                // increase limit by 50% to prevent normal distribution to be
                // from bottom to top
                cutYseriesMinimumValue = 1.5 * cutYseriesMinimumValue;
                chart.getXYPlot()
                        .getRangeAxis()
                        .setRange(yRange.getLowerBound(),
                                Math.max(DEFAULT_MAXIMUM_DENSITY_Y_AXIS_VALUE, cutYseriesMinimumValue));
            }
        }
        graphUtils.configureChartWithDefaults(chart);

        return chart;
    }

    /**
     * Creates the density series for specified sample data.
     * <p>
     * The sample data will be first clipped to filter some of the outliers by
     * provided definition.
     * 
     * @param definition
     * 
     * @param sample
     *            The measurement sample.
     * @param scanFrom
     *            The scan from. Used for PDF estimation.
     * @param scanTo
     *            The scan to. Used for PDF estimation.
     * @return The XY series of PDF points.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample data were not found.
     */
    private XYSeries createDensitySeriesFor(GraphDefinition graphType, MeasurementSampleDescriptor sample, double scanFrom, double scanTo)
            throws MeasurementDataNotFoundException {

        XYSeries densitySeries = null;

        double[] sampleData = graphUtils.loadSampleData(graphType, sample);

        if (sampleData.length < 2) {
            XYSeries series = new XYSeries("To few samples: " + sample.getSpecification());
            return series;
        }

        if (RProjectCallerWorks) {
            densitySeries = rCaller.getDensitySeries(sampleData,
                    "Density (R): " + sample.getSpecification());
            RProjectCallerWorks = densitySeries.getItemCount() > 0;

            if (!RProjectCallerWorks) {
                logger.info("R project call did not work, disabling R project calling.");
            }
        }

        if (!RProjectCallerWorks) {
            RProjectCallerWorks = false;
            LambdaVoronoi density2 = new LambdaVoronoi(sampleData,
                    "Density Estimation (R not available): " + sample.getSpecification());

            densitySeries = density2.getSeries(scanFrom, scanTo);

        }

        return densitySeries;
    }

    /**
     * The probability density function approximation.
     * 
     * @see "http://people.sc.fsu.edu/~hnguyen/Presentation/hoa_Auburn_modified_0327.pdf"
     */
    private static class LambdaVoronoi implements Function2D {

        /** The sample data. */
        private final double[] sampleData;

        /** The "Voronoi" borders. */
        private final double[] voronoiBorders;

        /** The minimum value of computation. Used value is 0. */
        private final double   min;

        /**
         * The maximum value of computation. Used value is
         * {@link Double#POSITIVE_INFINITY}.
         */
        private final double   max;

        /**
         * The name for series.
         * 
         * @see #getSeries(double, double)
         */
        private final String   seriesName;

        /**
         * Instantiates a new lambda voronoi estimation.
         * 
         * @param inputData
         *            The input data.
         * @param seriesName
         *            The series name.
         */
        public LambdaVoronoi(final double[] inputData, String seriesName) {

            this.seriesName = seriesName;

            this.sampleData = Arrays.copyOf(inputData, inputData.length);

            Arrays.sort(this.sampleData);

            min = 0;
            max = Double.POSITIVE_INFINITY;

            voronoiBorders = new double[this.sampleData.length + 1];

            voronoiBorders[0] = min;
            for (int i = 0; i < this.sampleData.length - 1; ++i) {
                voronoiBorders[i + 1] = (this.sampleData[i] + this.sampleData[i + 1]) / 2;
            }
            voronoiBorders[this.sampleData.length] = max;
        }

        @Override
        public double getValue(double x) {
            if (x < 0) {
                return 0;
            }

            for (int i = 0; i < voronoiBorders.length - 1; ++i) {
                if (voronoiBorders[i] <= x && x < voronoiBorders[i + 1]) {
                    return 1.0d / (voronoiBorders.length * (voronoiBorders[i + 1] - voronoiBorders[i]));
                }
            }
            return -1;
        }

        /**
         * Gets the series of points.
         * 
         * @param min
         *            The minimum X axis value to include.
         * @param max
         *            The maximum X axis value to include.
         * @return the series
         */
        XYSeries getSeries(double min, double max) {

            XYSeries series = new XYSeries(seriesName);

            for (double x : this.sampleData) {
                if (min <= x && x <= max) {
                    series.add(x, getValue(x));
                }
            }

            return series;
        }

    }

}
