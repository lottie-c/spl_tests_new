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

import org.jfree.chart.JFreeChart;

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;

public class GraphProvider {

    /** The graph creation utilities. */
    private final GraphUtils               graphUtils;

    /** The histogram creator. */
    private HistogramCreator               histogramCreator;

    /** The time graph creator. */
    private TimeGraphCreator               timeGraphCreator;

    /** The density graph creator. */
    private ProbabilityDensityGraphCreator densityGraphCreator;

    /**The Empirical Distribution graph creator*/
    private EdfCreator                      edfCreator;

    /** The working directory. */
    private final File                     workingDirectory;

    /**
     * Creates the chart for specified graph definition.
     * 
     * @param graphDefinition
     *            The graph definition.
     * @param samples
     *            The samples.
     * @return The chart implementation.
     * @throws MeasurementDataNotFoundException
     *             The measurement data not found exception.
     * @throws IllegalStateException
     *             Thrown when graph definition is not supported (i.e. not
     *             implemented yet).
     */
    public JFreeChart createChartFor(GraphDefinition graphDefinition, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {

        switch (graphDefinition.getBasicGraphType()) {
            case DensityComparison:
                return getDensityGraphCreator().createDensityComparisonGraph(graphDefinition, samples);
            case Histogram:
                return getHistogramCreator().createHistogram(graphDefinition, samples);
            case NotDefined:
                break;
            case TimeDiagram:
                return getTimeGraphCreator().createTimeDiagramGraph(graphDefinition, samples);
            case Edf:
                return getEdfCreator().createEdfGraph(graphDefinition, samples);
            default:
                break;
        }

        throw new IllegalStateException(String.format("Unsupported graph type [%s]", graphDefinition.getBasicGraphType()));
    }

    /**
     * Creates the chart PNG encoded in byte array for specified graph
     * definition.
     * 
     * @param graphDefinition
     *            The graph definition.
     * @param samples
     *            The samples.
     * @return The chart PNG encoded in byte array.
     * @throws MeasurementDataNotFoundException
     *             The measurement data not found exception.
     * @throws IllegalStateException
     *             Thrown when graph definition is not supported (i.e. not
     *             implemented yet).
     */
    public byte[] createChartPNGFor(GraphDefinition graphDefinition, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {
        return graphUtils.chartToPNG(createChartFor(graphDefinition, samples));
    }

    /**
     * Instantiates a new graph provider.
     * 
     * @param configuration
     *            The configuration to use.
     * @param workingDirectory
     *            The working directory.
     */
    public GraphProvider(SplEvaluatorConfiguration configuration, File workingDirectory) {
        graphUtils = new GraphUtils(configuration);
        this.workingDirectory = workingDirectory;
    }

    /**
     * Gets the histogram creator.
     * 
     * @return The histogram creator.
     */
    private HistogramCreator getHistogramCreator() {
        if (histogramCreator == null) {
            histogramCreator = new HistogramCreator(graphUtils);
        }
        return histogramCreator;
    }

    /**
     * Gets the time graph creator.
     * 
     * @return The time graph creator.
     */
    private TimeGraphCreator getTimeGraphCreator() {
        if (timeGraphCreator == null) {
            timeGraphCreator = new TimeGraphCreator(graphUtils);
        }
        return timeGraphCreator;
    }

    /**
     * Gets the Empirical Distribution graph creator.
     * 
     * @return The Empirical Distribution graph creator.
     */
    private EdfCreator getEdfCreator() {
        if (edfCreator == null) {
            edfCreator = new EdfCreator(graphUtils);
        }
        return edfCreator;
    }


    /**
     * Gets the density graph creator.
     * 
     * @return The density graph creator.
     */
    private ProbabilityDensityGraphCreator getDensityGraphCreator() {
        if (densityGraphCreator == null) {
            densityGraphCreator = new ProbabilityDensityGraphCreator(graphUtils, workingDirectory);
        }
        return densityGraphCreator;
    }
}
