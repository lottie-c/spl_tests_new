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
import java.util.Collections;
import java.util.ArrayList;
import java.util.Arrays;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.plot.XYPlot; 

import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.statistics.DataClipper;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.SimpleStatisticalFunctions;

/**
 * The implementation for Empirical Distristribution graph creation.
 * 
 * @author Lottie Carruthers
 */
public class EdfCreator {

    /** The graph utilities to use. */
    private final GraphUtils graphUtils;


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
    public EdfCreator(GraphUtils graphUtils) {
        this.graphUtils = graphUtils;
    }

    /**
     * Creates the EdfG chart.
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
     * @param showLegend
     *            The show legend flag.
     * @return The EDF chart.
     */
    public JFreeChart createEdfGraph(String plotTitle, String plotSubtitle, List<EdfSeries> data, String xaxisTitle, String yaxisTitle,
            boolean showLegend) {

        XYDataset dataset = this.createDataset(data);

        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean toolTips = false;
        boolean urls = false;
        final JFreeChart chart = ChartFactory.createXYStepChart(plotTitle, xaxisTitle,
                yaxisTitle, dataset,false, orientation, showLegend, toolTips, urls);

        if (plotSubtitle != null && !plotSubtitle.isEmpty()) {
            chart.addSubtitle(new TextTitle(plotSubtitle));
        }

        graphUtils.configureChartWithDefaults(chart);

        return chart;
    }


     private XYDataset createDataset( List<EdfSeries> data){
        final XYSeriesCollection dataset = new XYSeriesCollection( );  
        for (EdfSeries oneData : data) {
            XYSeries dataSeries = new XYSeries(oneData.title);
            
            for (int i = 0; i < oneData.data.size(); i++){
                // x axis is i + min - 1, -1 is so you start at 0
                dataSeries.add( oneData.data.get(i).get(1), oneData.data.get(i).get(0));
            }
            dataset.addSeries(dataSeries);
        }

        
        return dataset;
     }

    /**
     * The Class EdfSeries.
     */
    public static class EdfSeries {

        /** The series data. */
        public final ArrayList<ArrayList<Double>> data;

        /** The series title. */
        public final String   title;

        /*Min value of the series*/
        public final double min;

        /**
         * Instantiates a new histogram series.
         * 
         * @param title
         *            The series title.
         * @param data
         *            The series data.
         */
        public EdfSeries(String title, ArrayList<ArrayList<Double>> data, double min) {
            this.data = data;
            this.title = title;
            this.min = min;
        }
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
    public JFreeChart createEdfGraph(GraphDefinition definition, MeasurementSampleDescriptor... samples) throws MeasurementDataNotFoundException {

        List<EdfSeries> data = new LinkedList<>();
        Set<MeasurementSampleDescriptor> processedSamples = new HashSet<>();

        int failed = 0;
        int binCount = 0;
        for (MeasurementSampleDescriptor sample : samples) {
            if (!processedSamples.contains(sample)) {
                processedSamples.add(sample);
                try {
                    double[] clippedData = graphUtils.loadSampleData(definition, sample);
                    double min = clippedData[0];
                    double max = clippedData[0];

                    for (int i = 1; i < clippedData.length; i++){
                        if(clippedData[i] < min){
                            min = clippedData[i];
                        }else if(clippedData[i] > max){
                            max = clippedData[i];
                        }
                    }
                    EmpiricalDistribution dist = new EmpiricalDistribution();
                    ArrayList<ArrayList<Double>> output = dist.load(clippedData);
                    data.add(new EdfSeries(sample.getSpecification(), output, min));
                } catch (MeasurementDataNotFoundException e) {
                    ArrayList<Double> zero = new ArrayList<Double>(Arrays.asList(0D,0D));
                    ArrayList<ArrayList<Double>> empty = new ArrayList<ArrayList<Double>>();
                    empty.add(zero);

                    data.add(new EdfSeries("Measurement sample data not found: " + sample.getSpecification(), empty, 0));
                    ++failed;
                }
            }
        }

        if (failed == data.size()) {
            throw new MeasurementDataNotFoundException();
        }

        String subtitle = samples.length == 1 ? samples[0].getSpecification() : "";

        final JFreeChart chart = createEdfGraph(String.format("Empirical Distribution (%s)", definition.getDataClipTypePrettyString()),
                subtitle, data, "Execution time [ns]", "Cumulative Probability", samples.length != 1);
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
    public byte[] createEdfPNG(GraphDefinition definition, MeasurementSampleDescriptor... sampleData) throws MeasurementDataNotFoundException {
        return graphUtils.chartToPNG(createEdfGraph(definition, sampleData));
    }

}
