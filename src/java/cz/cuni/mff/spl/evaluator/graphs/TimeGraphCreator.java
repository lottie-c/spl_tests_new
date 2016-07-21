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
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;

/**
 * The implementation for time graph creation.
 * 
 * @author Martin Lacina
 */
public class TimeGraphCreator {

    /** The graph utilities to use. */
    private final GraphUtils graphUtils;

    /**
     * Instantiates a new time graph creator.
     * 
     * @param graphUtils
     *            The graph utilities to use.
     */
    public TimeGraphCreator(GraphUtils graphUtils) {
        this.graphUtils = graphUtils;
    }

    /**
     * Creates the time dataset. On X axis starting with provided value and
     * adding one point for each provided Y axis value.
     * 
     * @param startXFrom
     *            The X axis point to start from.
     * @param yValues
     *            The y values.
     * @return The dataset with points.
     */
    private XYSeries createTimeSeries(String seriesName, int startXFrom, double[] yValues) {
        XYSeries series = new XYSeries(seriesName);
        for (int x = 0; x < yValues.length; x++) {
            series.add(startXFrom + x, yValues[x]);
        }
        return series;
    }

    /**
     * Creates the time diagram for provided measurement samples.
     * <p>
     * Graph is generated only when sample data are found for at least one of
     * provided samples. When no sample data are found, than
     * <p>
     * The sample data will be clipped by provided graph definition.
     * 
     * @param graphDefinition
     *            The graph definition.
     * @param samples
     *            The measurement samples.
     * @return The time diagram graph.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample data were not found.
     *             {@link MeasurementDataNotFoundException} exception is thrown.
     * 
     */
    public JFreeChart createTimeDiagramGraph(GraphDefinition graphDefinition, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {

        XYSeriesCollection dataset = new XYSeriesCollection();

        Set<MeasurementSampleDescriptor> processedSamples = new HashSet<>();

        int failed = 0;

        for (MeasurementSampleDescriptor sample : samples) {
            if (!processedSamples.contains(sample)) {
                processedSamples.add(sample);
                try {
                    double[] data = graphUtils.loadSampleData(graphDefinition, sample);
                    XYSeries timeSeries = createTimeSeries(sample.getSpecification(), 1, data);
                    dataset.addSeries(timeSeries);
                } catch (MeasurementDataNotFoundException e) {
                    dataset.addSeries(new XYSeries("Measurement sample data not found: " + sample.getSpecification()));
                    ++failed;
                }

            }
        }

        if (failed == dataset.getSeriesCount()) {
            throw new MeasurementDataNotFoundException();
        }

        final JFreeChart chart =
                ChartFactory.createScatterPlot(
                        String.format("Time diagram (%s)", graphDefinition.getDataClipTypePrettyString()),
                        "Measurement number", "Execution time [ns]", dataset,
                        PlotOrientation.VERTICAL, samples.length != 1, false, false);

        if (samples.length == 1) {
            chart.addSubtitle(new TextTitle(samples[0].getSpecification()));
        }

        graphUtils.configureChartWithDefaults(chart);

        return chart;
    }

    /**
     * Creates the time diagram as PNG image for provided measurement samples.
     * <p>
     * Graph is generated only when sample data are found for at least one of
     * provided samples. When no sample data are found, than
     * <p>
     * The sample data will be clipped by provided graph definition.
     * 
     * @param graphDefinition
     *            The graph definition.
     * @param samples
     *            The measurement samples.
     * @return PNG encoded image as byte array, or {@code null} when encoding
     *         error occurs.
     * @throws MeasurementDataNotFoundException
     *             Thrown when measurement sample data were not found.
     *             {@link MeasurementDataNotFoundException} exception is thrown.
     * 
     */
    public byte[] createTimeDiagramGraphPNG(GraphDefinition graphDeclaration, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {
        return graphUtils.chartToPNG(createTimeDiagramGraph(graphDeclaration, samples));
    }
}
