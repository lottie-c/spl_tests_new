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

import java.awt.Color;
import java.io.IOException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;

/**
 * The utilities to manipulate with graphs - output to PNG, point series
 * generation from arrays.
 * 
 * @author Martin Lacina
 */
public class GraphUtils {

    /** The configuration to use. */
    private final SplEvaluatorConfiguration configuration;

    static {
        init();
    }

    /**
     * Initializes the graph utilities.
     * 
     * Sets chart theme.
     */
    static void init() {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        // ChartFactory.setChartTheme(StandardChartTheme.createJFreeTheme());
        // ChartFactory.setChartTheme(StandardChartTheme.createDarknessTheme());
    }

    /** The default value for chart width in pixels is 800. */
    private int CHART_WIDTH  = 800;

    /** The default value for chart height in pixels is 600. */
    private int CHART_HEIGHT = 600;

    /**
     * Instantiates a new graph utilities.
     * 
     * @param configuration
     *            The evaluation configuration configuration.
     */
    public GraphUtils(SplEvaluatorConfiguration configuration) {
        CHART_WIDTH = configuration.getGraphImageWidth();
        CHART_HEIGHT = configuration.getGraphImageHeight();
        this.configuration = configuration;
    }

    /**
     * Creates the series of points.
     * 
     * @param xValues
     *            The x axis values.
     * @param yValues
     *            The y axis values.
     * @param seriesName
     *            The series name.
     * @return The XY point series.
     */
    public static XYSeries createSeriesOfPoints(double[] xValues, double[] yValues,
            String seriesName) {
        if (xValues.length != yValues.length) {
            throw new IllegalArgumentException(
                    "Number of x axis values and y axis values does not match.");
        }

        XYSeries series = new XYSeries(seriesName);
        for (int index = 0; index < xValues.length; index++) {
            series.add(xValues[index], yValues[index]);
        }
        return series;
    }

    /**
     * Configures chart with defaults.
     * 
     * @param chart
     *            The chart to configure.
     */
    public void configureChartWithDefaults(JFreeChart chart) {

        XYItemRenderer renderer = chart.getXYPlot().getRenderer();

        if (renderer instanceof XYBarRenderer) {
            ((XYBarRenderer) renderer).setDrawBarOutline(true);
            ((XYBarRenderer) renderer).setShadowVisible(false);
        }

        Color background = configuration.getGraphBackgroundColor();
        if (configuration.isGraphBackgroundTransparent()) {
            chart.setBackgroundPaint(background);
            chart.setBackgroundImageAlpha(configuration.getGraphBackgroundColor().getRGBComponents(null)[3]);
        } else {
            chart.setBackgroundPaint(configuration.getGraphBackgroundColor());
        }

        chart.getXYPlot().getRenderer().setBaseItemLabelPaint(Color.orange);

        int colorIndex = 0;
        for (Color c : configuration.getGraphSampleColors()) {
            renderer.setSeriesPaint(colorIndex, c);
            renderer.setSeriesOutlinePaint(colorIndex, c);
            ++colorIndex;
        }

        // configure text color
        Color textColor = configuration.getGraphTextColor();

        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setPaint(textColor);
        }

        for (Object subtitleObject : chart.getSubtitles()) {
            if (subtitleObject instanceof TextTitle) {
                ((TextTitle) subtitleObject).setPaint(textColor);
            }
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemPaint(textColor);
            if (textColor.equals(legend.getBackgroundPaint())) {
                legend.setBackgroundPaint(background);
            }
        }

        configureAxisWithDefaults(chart.getXYPlot().getDomainAxis(), textColor);
        configureAxisWithDefaults(chart.getXYPlot().getRangeAxis(), textColor);

    }

    /**
     * Configures axis with defaults.
     * 
     * @param axis
     *            The axis.
     * @param textColor
     *            The text color.
     */
    private void configureAxisWithDefaults(Axis axis, Color textColor) {
        axis.setAxisLinePaint(textColor);
        axis.setLabelPaint(textColor);
        axis.setTickLabelPaint(textColor);
        axis.setTickMarkPaint(textColor);
    }

    /**
     * Converts chart to PNG.
     * 
     * @param chart
     *            The chart.
     * @return PNG encoded chart image as byte array, or {@code null} when error
     *         occurs.
     */
    public byte[] chartToPNG(JFreeChart chart) {
        try {
            if (configuration.isGraphBackgroundTransparent()) {
                return ChartUtilities.encodeAsPNG(chart.createBufferedImage(
                        CHART_WIDTH, CHART_HEIGHT), true, 6);
            } else {
                return ChartUtilities.encodeAsPNG(chart.createBufferedImage(
                        CHART_WIDTH, CHART_HEIGHT));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Load sample data with specified data clipping.
     * 
     * @param graphType
     *            The graph type defining data clip.
     * @param sample
     *            The sample.
     * @return The double[].
     * @throws MeasurementDataNotFoundException
     *             The measurement data not found exception.
     */
    public double[] loadSampleData(GraphDefinition graphType, MeasurementSampleDescriptor sampleDescriptor) throws MeasurementDataNotFoundException {
        if (sampleDescriptor == null) {
            throw new MeasurementDataNotFoundException(null);
        }
        MeasurementSample sample = sampleDescriptor.getMeasurementSample();

        if (sample.getSampleDataProvider() == null) {
            throw new MeasurementDataNotFoundException(new SampleIdentification(sample.getMeasurement()));
        }

        double lambdaMultiplier = sampleDescriptor.getLambdaMultiplier();
        switch (graphType.getDataClipType()) {
            case Quantile:
                return sample.loadQuantileClippedData(lambdaMultiplier,
                        graphType.getQuantileLowerClip(), graphType.getQuantileUpperClip()
                        );

            case Sigma:
                return sample.loadSigmaClippedData(lambdaMultiplier,
                        graphType.getSigmaMultiplier(), graphType.getSigmaMaxIteration()
                        );

            case None:
                return sample.loadRawData(lambdaMultiplier);
            default:
                throw new UnsupportedOperationException(String.format("Unknown data clip type '%s'", graphType.getDataClipType()));
        }
    }

    /**
     * Gets the SPL evaluator configuration.
     * 
     * @return The SPL evaluator configuration.
     */
    SplEvaluatorConfiguration getConfiguration() {
        return configuration;
    }
}
