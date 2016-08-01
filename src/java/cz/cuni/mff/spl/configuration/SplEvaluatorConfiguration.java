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
package cz.cuni.mff.spl.configuration;

import static cz.cuni.mff.spl.evaluator.graphs.GraphDefinition.createGraphDefinition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;

import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.IniConversion;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition.DataClipType;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition.GraphType;
import cz.cuni.mff.spl.evaluator.graphs.HistogramCreator;
import cz.cuni.mff.spl.evaluator.graphs.ProbabilityDensityGraphCreator;
import cz.cuni.mff.spl.evaluator.r.RProjectCaller;
import cz.cuni.mff.spl.utils.parsers.ColorParser;

/**
 * Class with configuration for SPL formula Evaluator.
 * 
 * This class should contain only simple data types to allow XML conversion
 * without specifying mapping manually.
 * 
 * Use factory methods to create new instances.
 * 
 * @author Martin Lacina
 * 
 */
public class SplEvaluatorConfiguration {

    /**
     * Instantiates a new SPL evaluator configuration without graph types and
     * sample colors.
     * <p>
     * Use factory method {@link #createDefaultConfiguration()} or
     * {@link #createDefaultConfigurationWithoutGraphs()} to create new
     * instances.
     * 
     * @see #createDefaultConfiguration()
     * @see #createDefaultConfigurationWithoutGraphs()
     */
    @Deprecated
    public SplEvaluatorConfiguration() {
    }

    /**
     * The limit p-value for statistical t-test.
     * 
     * Comparison of measurement is satisfied when t-test p-value is higher than
     * limit.
     * 
     * Default value is {@code 0.05}.
     */
    private double                      TTestLimitPValue;

    /**
     * The equality interval for {@link Sign#EQI}.
     * <p>
     * Default value is 5% (0.05).
     */
    private double                      equalityInterval;

    /**
     * The minimum sample count warning limit.
     * 
     * Warning will be issued, when sample count is below this limit.
     */
    private long                        minimumSampleCountWarningLimit;

    /**
     * The maximum standard deviation compared to mean difference warning limit
     * in percents.
     * <p>
     * Default value is 75 %.
     * <p>
     * Warning will be issued, when difference is more than specified value.
     */
    private double                      maximumStandardDeviationVsMeanDifferenceWarningLimit;

    /**
     * The maximum median compared to mean difference warning limit in percents.
     * 
     * Default value is 20 %.
     * 
     * Warning will be issued, when difference is more than specified value.
     */
    private double                      maximumMedianVsMeanDifferenceWarningLimit;

    /**
     * The boolean value indicating whether to generate HTML output.
     * 
     * Default value is {@code true}.
     */
    private boolean                     generateHtmlOutput;

    /**
     * The boolean value indicating whether to generate XML output.
     * 
     * Default value is {@code true}.
     */
    private boolean                     generateXmlOutput;

    /**
     * The boolean value indicating whether to generate graph output.
     * 
     * Default value is {@code true}.
     */
    private boolean                     generateGraphOutput;

    /**
     * The command to run R script. Default value is {@code Rscript}, i. e. it
     * is expected on PATH.
     */
    private String                      RScriptCommand;

    /**
     * The graph image width in pixels.
     * 
     * Default value is 800px.
     */
    private int                         graphImageWidth;

    /**
     * The graph image height in pixels.
     * 
     * Default value is 600px.
     */
    private int                         graphImageHeight;

    /** Types of graphs to generate for every measurement. */
    private final List<GraphDefinition> measurementGraphTypes = new ArrayList<>(10);

    /** Types of graphs to generate for every comparison. */
    private final List<GraphDefinition> comparisonGraphTypes  = new ArrayList<>(10);

    /** The colors for samples. */
    private final List<Color>           graphSampleColors     = new ArrayList<>(10);

    /** The color for text. */
    private Color                       graphTextColor;

    /** The color for background. */
    private Color                       graphBackgroundColor;

    /** The flag indicating if background transparent. */
    private boolean                     graphBackgroundTransparent;

    /** The maximum Y axis limit for comparison with normal density. */
    private double                      graphMaximumNormalDensityYAxisLimit;

    /** The histogram minimum bin count. */
    private int                         histogramMinimumBinCount;

    /** The histogram maximum bin count. */
    private int                         histogramMaximumBinCount;

    {
        this.TTestLimitPValue = 0.05d;
        this.equalityInterval = 0.05d;
        this.minimumSampleCountWarningLimit = 10;
        this.maximumStandardDeviationVsMeanDifferenceWarningLimit = 75;
        this.maximumMedianVsMeanDifferenceWarningLimit = 20;

        this.generateHtmlOutput = true;
        this.generateXmlOutput = true;
        this.generateGraphOutput = true;

        this.RScriptCommand = RProjectCaller.R_RUNTIME_DEFAULT;

        this.graphImageWidth = 800;
        this.graphImageHeight = 600;

        this.histogramMinimumBinCount = HistogramCreator.DEFAULT_MINIMUM_HISTOGRAM_BIN_COUNT;
        this.histogramMaximumBinCount = HistogramCreator.DEFAULT_MAXIMUM_HISTOGRAM_BIN_COUNT;

        this.graphTextColor = Color.BLACK;
        this.graphBackgroundColor = ColorParser.LIGHT_GRAY;
        this.graphBackgroundTransparent = false;
        this.graphMaximumNormalDensityYAxisLimit = ProbabilityDensityGraphCreator.DEFAULT_MAXIMUM_DENSITY_Y_AXIS_VALUE;

        // default colors are not in basic object
        // default graph types are not in basic object
    }

    /**
     * Gets the default measurement graph definitions.
     * 
     * @return The default measurement graph definitions.
     */
    public static Collection<GraphDefinition> getDefaultMeasurementGraphs() {
        return Arrays.asList(

                // imported static method
                // GraphDefinition.createGraphDefinition(...)

                // GraphTypes.CompleteHistogram,
                createGraphDefinition(GraphType.Histogram, DataClipType.None),

                // GraphTypes.SigmaClippedCompleteHistogram_3,
                createGraphDefinition(GraphType.Histogram, DataClipType.Sigma, 3.0, 1.0),

                // GraphTypes.NormalDistributionComparisonGraph,
                createGraphDefinition(GraphType.DensityComparison, DataClipType.Sigma, 3.0, 1.0),

                // GraphTypes.QuantileClippedCompleteHistogram_001_999,
                createGraphDefinition(GraphType.Histogram, DataClipType.Quantile, 0.1, 99.1),
                // GraphTypes.QuantileClippedCompleteHistogram_000_990,
                createGraphDefinition(GraphType.Histogram, DataClipType.Quantile, 0.0, 99.0),

                // GraphTypes.QuantileClippedCompleteHistogram_000_950,
                createGraphDefinition(GraphType.Histogram, DataClipType.Quantile, 0.0, 95.0),

                // GraphTypes.SigmaClippedTimeDiagram_3,
                createGraphDefinition(GraphType.TimeDiagram, DataClipType.Sigma, 3.0, 1.0),

                // GraphTypes.QuantileClippedTimeDiagram_010_990
                createGraphDefinition(GraphType.TimeDiagram, DataClipType.Quantile, 1.0, 99.0)
                );
    }

    /**
     * Gets the default comparison graph definitions.
     * 
     * @return The default comparison graph definitions.
     */
    public static Collection<GraphDefinition> getDefaultComparisonGraphs() {
        return Arrays.asList(
                // GraphTypes.TwoSamples_DensityComparisonGraph,
                createGraphDefinition(GraphType.DensityComparison, DataClipType.Sigma, 3.0, 1.0),

                // GraphTypes.TwoSamples_QuantileClippedCompleteHistogram_001_999
                createGraphDefinition(GraphType.Histogram, DataClipType.Quantile, 0.1, 99.9),
                
                /*createGraphDefinition(HERE INSERT EDF COMPARISON GRAPH)*/
                createGraphDefinition(GraphType.Edf, DataClipType.Quantile, 0.1, 99.9)
                
                );
    }

    /**
     * Gets the default colors for measurement samples.
     * 
     * @return The default colors for measurement samples.
     */
    public static Collection<Color> getDefaultSampleColors() {
        return Arrays.asList(
                Color.red,
                Color.blue,
                Color.green,
                Color.yellow,
                Color.orange,
                Color.cyan,
                Color.magenta
                );
    }

    /**
     * Creates the default configuration with all graphs in default order.
     * 
     * @return The SPL Evaluator configuration.
     */
    public static SplEvaluatorConfiguration createDefaultConfiguration() {
        SplEvaluatorConfiguration defaultConfiguration = new SplEvaluatorConfiguration();
        defaultConfiguration.measurementGraphTypes.addAll(
                getDefaultMeasurementGraphs()
                );

        defaultConfiguration.comparisonGraphTypes
                .addAll(
                getDefaultComparisonGraphs()
                );

        defaultConfiguration.setGraphSampleColors(null);

        return defaultConfiguration;
    }

    /**
     * Creates the default configuration without graphs definitions.
     * <p>
     * Note than generating graf flag is still set.
     * 
     * @return The SPL evaluator configuration.
     */
    public static SplEvaluatorConfiguration createDefaultConfigurationWithoutGraphs() {
        SplEvaluatorConfiguration result = createDefaultConfiguration();
        result.getMeasurementGraphTypes().clear();
        result.getComparisonGraphTypes().clear();
        return result;
    }

    // Getter and setters.

    /**
     * Gets the t test limit p value.
     * 
     * @return The t test limit p value.
     */
    public double getTTestLimitPValue() {
        return TTestLimitPValue;
    }

    /**
     * Sets the t test limit p value.
     * 
     * @param tTestLimitPValue
     *            The new t test limit p value.
     */
    public void setTTestLimitPValue(double tTestLimitPValue) {
        this.TTestLimitPValue = tTestLimitPValue;
    }

    /**
     * Gets the default equality interval to be used when no interval is
     * specified in comparison.
     * <p>
     * The equality interval is used for {@link Sign#EQI} as tolerance interval
     * for equality (+- x % interval to check p-value).
     * <p>
     * <code>
     * C1 =(interval) C2
     * <br>
     *   iff
     * <br>
     * [C1 <=(1-interval, 1+interval)] C2 AND [C1 >=(1+interval, 1-interval) C2]
     * </code>
     * <p>
     * Where C1 and C2 are comparisons and interval is specified interval value.
     * <p>
     * Interval value has to be non negative double value lower or equal to 1.
     * 
     * @return The default equality interval to be used when no interval is
     *         specified in comparison.
     */
    public double getEqualityInterval() {
        return equalityInterval;
    }

    /**
     * Sets the equality interval.
     * 
     * @param equalityInterval
     *            The new equality interval.
     * 
     * @see #getEqualityInterval() for details about meaning and properties.
     */
    public void setEqualityInterval(double equalityInterval) {
        if (equalityInterval < 0) {
            this.equalityInterval = 0;
        } else if (equalityInterval > 1) {
            this.equalityInterval = 1;
        } else {
            this.equalityInterval = equalityInterval;
        }
    }

    /**
     * Gets the minimum sample count warning limit.
     * 
     * @return the minimum sample count warning limit
     */
    public long getMinimumSampleCountWarningLimit() {
        return minimumSampleCountWarningLimit;
    }

    /**
     * Sets the minimum sample count warning limit.
     * 
     * @param minimumSampleCountWarningLimit
     *            the new minimum sample count warning limit
     */
    public void setMinimumSampleCountWarningLimit(long minimumSampleCountWarningLimit) {
        this.minimumSampleCountWarningLimit = minimumSampleCountWarningLimit;
    }

    /**
     * Gets the maximum standard deviation compared to mean difference warning
     * limit in percents.
     * 
     * @return the maximum standard deviation compared to mean difference
     *         warning limit in percents
     */
    public double getMaximumStandardDeviationVsMeanDifferenceWarningLimit() {
        return maximumStandardDeviationVsMeanDifferenceWarningLimit;
    }

    /**
     * Sets the maximum standard deviation compared to mean difference warning
     * limit in percents.
     * 
     * @param maximumStandardDeviationVsMeanDifferenceWarningLimit
     *            the new maximum standard deviation compared to mean difference
     *            warning limit in percents
     */
    public void setMaximumStandardDeviationVsMeanDifferenceWarningLimit(double maximumStandardDeviationVsMeanDifferenceWarningLimit) {
        this.maximumStandardDeviationVsMeanDifferenceWarningLimit = maximumStandardDeviationVsMeanDifferenceWarningLimit;
    }

    /**
     * Gets the maximum median compared to mean difference warning limit in
     * percents.
     * 
     * @return the maximum median compared to mean difference warning limit in
     *         percents
     */
    public double getMaximumMedianVsMeanDifferenceWarningLimit() {
        return maximumMedianVsMeanDifferenceWarningLimit;
    }

    /**
     * Sets the maximum median compared to mean difference warning limit in
     * percents.
     * 
     * @param maximumMedianVsMeanDifferenceWarningLimit
     *            the new maximum median compared to mean difference warning
     *            limit in percents
     */
    public void setMaximumMedianVsMeanDifferenceWarningLimit(double maximumMedianVsMeanDifferenceWarningLimit) {
        this.maximumMedianVsMeanDifferenceWarningLimit = maximumMedianVsMeanDifferenceWarningLimit;
    }

    /**
     * Checks if is the boolean value indicating whether to generate HTML
     * output.
     * 
     * @return the boolean value indicating whether to generate HTML output
     */
    public boolean isGenerateHtmlOutput() {
        return generateHtmlOutput;
    }

    /**
     * Sets the boolean value indicating whether to generate HTML output.
     * 
     * @param generateHtmlOutput
     *            the new boolean value indicating whether to generate HTML
     *            output
     */
    public void setGenerateHtmlOutput(boolean generateHtmlOutput) {
        this.generateHtmlOutput = generateHtmlOutput;
    }

    /**
     * Checks if is the boolean value indicating whether to generate XML output.
     * 
     * @return the boolean value indicating whether to generate XML output
     */
    public boolean isGenerateXmlOutput() {
        return generateXmlOutput;
    }

    /**
     * Sets the boolean value indicating whether to generate XML output.
     * 
     * @param generateXmlOutput
     *            the new boolean value indicating whether to generate XML
     *            output
     */
    public void setGenerateXmlOutput(boolean generateXmlOutput) {
        this.generateXmlOutput = generateXmlOutput;
    }

    /**
     * Checks if is the boolean value indicating whether to generate graph
     * output.
     * 
     * @return the boolean value indicating whether to generate graph output
     */
    public boolean isGenerateGraphOutput() {
        return generateGraphOutput;
    }

    /**
     * Sets the boolean value indicating whether to generate graph output.
     * 
     * @param generateGraphOutput
     *            the new boolean value indicating whether to generate graph
     *            output
     */
    public void setGenerateGraphOutput(boolean generateGraphOutput) {
        this.generateGraphOutput = generateGraphOutput;
    }

    /**
     * Gets the r script command.
     * 
     * @return The r script command.
     */
    public String getRScriptCommand() {
        return RScriptCommand;
    }

    /**
     * Sets the r script command.
     * 
     * @param rScriptCommand
     *            The new r script command.
     */
    public void setRScriptCommand(String rScriptCommand) {
        RScriptCommand = rScriptCommand;
    }

    /**
     * Gets the graph image width in pixels.
     * 
     * @return the graph image width in pixels
     */
    public int getGraphImageWidth() {
        return graphImageWidth;
    }

    /**
     * Sets the graph image width in pixels.
     * 
     * @param graphImageWidth
     *            the new graph image width in pixels
     */
    public void setGraphImageWidth(int graphImageWidth) {
        this.graphImageWidth = graphImageWidth;
    }

    /**
     * Gets the graph image height in pixels.
     * 
     * @return the graph image height in pixels
     */
    public int getGraphImageHeight() {
        return graphImageHeight;
    }

    /**
     * Sets the graph image height in pixels.
     * 
     * @param graphImageHeight
     *            the new graph image height in pixels
     */
    public void setGraphImageHeight(int graphImageHeight) {
        this.graphImageHeight = graphImageHeight;
    }

    /**
     * Gets the histogram minimum bin count.
     * 
     * @return The histogram minimum bin count.
     */
    public int getHistogramMinimumBinCount() {
        return histogramMinimumBinCount;
    }

    /**
     * Sets the histogram minimum bin count.
     * 
     * @param histogramMinimumBinCount
     *            The new histogram minimum bin count.
     */
    public void setHistogramMinimumBinCount(int histogramMinimumBinCount) {
        this.histogramMinimumBinCount = histogramMinimumBinCount;
    }

    /**
     * Gets the histogram maximum bin count.
     * 
     * @return The histogram maximum bin count.
     */
    public int getHistogramMaximumBinCount() {
        return histogramMaximumBinCount;
    }

    /**
     * Sets the histogram maximum bin count.
     * 
     * @param histogramMaximumBinCount
     *            The new histogram maximum bin count.
     */
    public void setHistogramMaximumBinCount(int histogramMaximumBinCount) {
        this.histogramMaximumBinCount = histogramMaximumBinCount;
    }

    /**
     * Gets the types of graphs to generate for every measurement.
     * 
     * @return the types of graphs to generate for every measurement
     */
    public List<GraphDefinition> getMeasurementGraphTypes() {
        return measurementGraphTypes;
    }

    /**
     * Gets the types of graphs to generate for every comparison.
     * 
     * @return the types of graphs to generate for every comparison
     */
    public List<GraphDefinition> getComparisonGraphTypes() {
        return comparisonGraphTypes;
    }

    /**
     * Gets the colors for samples.
     * 
     * @return the colors for samples
     */
    public List<Color> getGraphSampleColors() {
        return graphSampleColors;
    }

    /**
     * Sets the graph color samples and retains default colors in the end of the
     * list.
     * 
     * @param graphColorSamples
     *            The new graph color samples.
     */
    public void setGraphSampleColors(Collection<Color> graphColorSamples) {
        this.graphSampleColors.clear();
        this.graphSampleColors.addAll(getDefaultSampleColors());
        if (graphColorSamples != null) {
            this.graphSampleColors.removeAll(graphColorSamples);
            this.graphSampleColors.addAll(0, graphColorSamples);
        }
    }

    /**
     * Gets the color for text.
     * 
     * @return the color for text
     */
    public Color getGraphTextColor() {
        return graphTextColor;
    }

    /**
     * Sets the color for text.
     * 
     * @param colorText
     *            the new color for text
     */
    public void setGraphTextColor(Color colorText) {
        this.graphTextColor = colorText;
    }

    /**
     * Gets the color for background.
     * 
     * @return the color for background
     */
    public Color getGraphBackgroundColor() {
        return graphBackgroundColor;
    }

    /**
     * Sets the color for background.
     * 
     * @param colorBackground
     *            the new color for background
     */
    public void setGraphBackgroundColor(Color colorBackground) {
        this.graphBackgroundColor = colorBackground;
    }

    /**
     * Checks if is the flag indicating if background transparent.
     * 
     * @return the flag indicating if background transparent
     */
    public boolean isGraphBackgroundTransparent() {
        return graphBackgroundTransparent;
    }

    /**
     * Sets the flag indicating if background transparent.
     * 
     * @param backgroundTransparent
     *            the new flag indicating if background transparent
     */
    public void setGraphBackgroundTransparent(boolean backgroundTransparent) {
        this.graphBackgroundTransparent = backgroundTransparent;
    }

    /**
     * Gets the maximum Y axis limit for comparison with normal density.
     * 
     * @return the maximum Y axis limit for comparison with normal density
     */
    public double getGraphMaximumNormalDensityYAxisLimit() {
        return graphMaximumNormalDensityYAxisLimit;
    }

    /**
     * Sets the maximum Y axis limit for comparison with normal density.
     * 
     * @param graphMaximumNormalDensityYAxisLimit
     *            the new maximum Y axis limit for comparison with normal
     *            density
     */
    public void setGraphMaximumNormalDensityYAxisLimit(double graphMaximumNormalDensityYAxisLimit) {
        this.graphMaximumNormalDensityYAxisLimit = graphMaximumNormalDensityYAxisLimit;
    }

    @Override
    public String toString() {
        return "SplEvaluatorConfiguration [TTestLimitPValue=" + TTestLimitPValue + ", minimumSampleCountWarningLimit=" + minimumSampleCountWarningLimit
                + ", maximumStandardDeviationVsMeanDifferenceWarningLimit=" + maximumStandardDeviationVsMeanDifferenceWarningLimit
                + ", maximumMedianVsMeanDifferenceWarningLimit=" + maximumMedianVsMeanDifferenceWarningLimit + ", generateHtmlOutput=" + generateHtmlOutput
                + ", generateXmlOutput=" + generateXmlOutput + ", generateGraphOutput=" + generateGraphOutput + ", RScriptCommand=" + RScriptCommand
                + ", graphImageWidth=" + graphImageWidth + ", graphImageHeight=" + graphImageHeight + ", measurementGraphTypes=" + measurementGraphTypes
                + ", comparisonGraphTypes=" + comparisonGraphTypes + ", graphSampleColors=" + graphSampleColors + ", graphTextColor=" + graphTextColor
                + ", graphBackgroundColor=" + graphBackgroundColor + ", graphBackgroundTransparent=" + graphBackgroundTransparent
                + ", graphMaximumNormalDensityYAxisLimit=" + graphMaximumNormalDensityYAxisLimit + ", histogramMinimumBinCount=" + histogramMinimumBinCount
                + ", histogramMaximumBinCount=" + histogramMaximumBinCount + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((RScriptCommand == null) ? 0 : RScriptCommand.hashCode());
        long temp;
        temp = Double.doubleToLongBits(TTestLimitPValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((comparisonGraphTypes == null) ? 0 : comparisonGraphTypes.hashCode());
        result = prime * result + (generateGraphOutput ? 1231 : 1237);
        result = prime * result + (generateHtmlOutput ? 1231 : 1237);
        result = prime * result + (generateXmlOutput ? 1231 : 1237);
        result = prime * result + ((graphBackgroundColor == null) ? 0 : graphBackgroundColor.hashCode());
        result = prime * result + (graphBackgroundTransparent ? 1231 : 1237);
        result = prime * result + graphImageHeight;
        result = prime * result + graphImageWidth;
        temp = Double.doubleToLongBits(graphMaximumNormalDensityYAxisLimit);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((graphSampleColors == null) ? 0 : graphSampleColors.hashCode());
        result = prime * result + ((graphTextColor == null) ? 0 : graphTextColor.hashCode());
        result = prime * result + histogramMaximumBinCount;
        result = prime * result + histogramMinimumBinCount;
        temp = Double.doubleToLongBits(maximumMedianVsMeanDifferenceWarningLimit);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maximumStandardDeviationVsMeanDifferenceWarningLimit);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((measurementGraphTypes == null) ? 0 : measurementGraphTypes.hashCode());
        result = prime * result + (int) (minimumSampleCountWarningLimit ^ (minimumSampleCountWarningLimit >>> 32));
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
        SplEvaluatorConfiguration other = (SplEvaluatorConfiguration) obj;
        if (RScriptCommand == null) {
            if (other.RScriptCommand != null) {
                return false;
            }
        } else if (!RScriptCommand.equals(other.RScriptCommand)) {
            return false;
        }
        if (Double.doubleToLongBits(TTestLimitPValue) != Double.doubleToLongBits(other.TTestLimitPValue)) {
            return false;
        }
        if (comparisonGraphTypes == null) {
            if (other.comparisonGraphTypes != null) {
                return false;
            }
        } else if (!comparisonGraphTypes.equals(other.comparisonGraphTypes)) {
            return false;
        }
        if (generateGraphOutput != other.generateGraphOutput) {
            return false;
        }
        if (generateHtmlOutput != other.generateHtmlOutput) {
            return false;
        }
        if (generateXmlOutput != other.generateXmlOutput) {
            return false;
        }
        if (graphBackgroundColor == null) {
            if (other.graphBackgroundColor != null) {
                return false;
            }
        } else if (!graphBackgroundColor.equals(other.graphBackgroundColor)) {
            return false;
        }
        if (graphBackgroundTransparent != other.graphBackgroundTransparent) {
            return false;
        }
        if (graphImageHeight != other.graphImageHeight) {
            return false;
        }
        if (graphImageWidth != other.graphImageWidth) {
            return false;
        }
        if (Double.doubleToLongBits(graphMaximumNormalDensityYAxisLimit) != Double.doubleToLongBits(other.graphMaximumNormalDensityYAxisLimit)) {
            return false;
        }
        if (graphSampleColors == null) {
            if (other.graphSampleColors != null) {
                return false;
            }
        } else if (!graphSampleColors.equals(other.graphSampleColors)) {
            return false;
        }
        if (graphTextColor == null) {
            if (other.graphTextColor != null) {
                return false;
            }
        } else if (!graphTextColor.equals(other.graphTextColor)) {
            return false;
        }
        if (histogramMaximumBinCount != other.histogramMaximumBinCount) {
            return false;
        }
        if (histogramMinimumBinCount != other.histogramMinimumBinCount) {
            return false;
        }
        if (Double.doubleToLongBits(maximumMedianVsMeanDifferenceWarningLimit) != Double.doubleToLongBits(other.maximumMedianVsMeanDifferenceWarningLimit)) {
            return false;
        }
        if (Double.doubleToLongBits(maximumStandardDeviationVsMeanDifferenceWarningLimit) != Double
                .doubleToLongBits(other.maximumStandardDeviationVsMeanDifferenceWarningLimit)) {
            return false;
        }
        if (measurementGraphTypes == null) {
            if (other.measurementGraphTypes != null) {
                return false;
            }
        } else if (!measurementGraphTypes.equals(other.measurementGraphTypes)) {
            return false;
        }
        if (minimumSampleCountWarningLimit != other.minimumSampleCountWarningLimit) {
            return false;
        }
        return true;
    }

    /**
     * Gets the clone.
     * 
     * @return The clone.
     */
    public SplEvaluatorConfiguration getClone() {
        Ini ini = new Ini();
        SplEvaluatorConfiguration result;
        try {
            IniConversion.saveSplEvaluatorConfiguration(this, ini);
            result = IniConversion.loadSplEvaluatorConfiguration(ini);
        } catch (ConversionException e) {
            throw new IllegalStateException("Unable to convert SPL evaluator configuration to or from Ini.", e);
        }
        return result;
    }

    /**
     * Gets the section factories.
     * 
     * @return The section factories.
     */
    public static List<ISectionFactory> getSectionFactories() {
        // basic version of configuration help
        Ini ini = new Ini();
        try {
            IniConversion.saveSplEvaluatorConfiguration(createDefaultConfiguration(), ini);
        } catch (ConversionException e) {
            throw new IllegalStateException("Unable to convert SPL Evaluator Configuration to Ini.", e);
        }

        List<ISectionFactory> result = new ArrayList<>(ini.keySet().size());

        for (Entry<String, Section> section : ini.entrySet()) {
            result.add(new EvaluatorIniSectionFactory(section.getValue()));
        }

        return result;
    }

    /**
     * A factory for creating {@link ISection} objects for evaluator.
     */
    private static final class EvaluatorIniSectionFactory implements ISectionFactory {

        /** The section name. */
        private final String          sectionName;

        /** The descriptions. */
        Map<String, EntryInformation> descriptions = new LinkedHashMap<>();

        /**
         * Instantiates a new evaluator Ini section factory.
         * 
         * @param section
         *            The section.
         */
        public EvaluatorIniSectionFactory(Section section) {
            this.sectionName = section.getName();
            for (Entry<String, String> entry : section.entrySet()) {
                descriptions.put(entry.getKey(), new EntryInformation(entry.getKey(), entry.getValue(), ""));
            }
        }

        /**
         * Gets the description.
         * 
         * @return The description.
         */
        @Override
        public String getDescription() {
            return "";
        }

        /**
         * Gets the entries description.
         * 
         * @return The entries description.
         */
        @Override
        public Map<String, EntryInformation> getEntriesDescription() {
            return descriptions;
        }

        /**
         * Creates a new EvaluatorIniSection object.
         * 
         * @param sectionName
         *            The section name.
         * @param values
         *            The values.
         * @return the i section
         */
        @Override
        public ISection createFromSectionName(String sectionName, Map<String, String> values) {
            if (this.sectionName.equals(sectionName)) {
                return new ISection() {

                    @Override
                    public boolean isValid() {
                        return true;
                    }

                    @Override
                    public String getName() {
                        return EvaluatorIniSectionFactory.this.sectionName;
                    }

                    @Override
                    public ISectionFactory getFactory() {
                        return EvaluatorIniSectionFactory.this;
                    }

                    @Override
                    public List<String> getErrors() {
                        return Collections.emptyList();
                    }
                };
            }
            return null;
        }

        @Override
        public String getSectionPrefix() {
            return sectionName;
        }

    }

}
