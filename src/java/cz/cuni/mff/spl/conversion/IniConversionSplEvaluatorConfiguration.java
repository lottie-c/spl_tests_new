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
package cz.cuni.mff.spl.conversion;

import java.awt.Color;
import java.util.Collection;

import org.ini4j.Ini;

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * The conversion of {@link SplEvaluatorConfiguration} to and from INI format.
 * 
 * @author Martin Lacina
 * 
 * @see IniConversion
 * @see SplEvaluatorConfiguration
 */
class IniConversionSplEvaluatorConfiguration {

    /** The logger. */
    static final SplLog         logger                                               = SplLogger.getLogger(IniConversionSplEvaluatorConfiguration.class);

    /** The Constant EVALUATOR_OUTPUT. */
    private static final String EVALUATOR_OUTPUT                                     = "evaluator.output";

    /** The Constant generateHtmlOutput. */
    private static final String generateHtmlOutput                                   = "generate-html-output";

    /** The Constant generateXmlOutput. */
    private static final String generateXmlOutput                                    = "generate-xml-output";

    /** The Constant generateGraphOutput. */
    private static final String generateGraphOutput                                  = "generate-graph-output";

    /** The Constant rscriptCommand. */
    private static final String rscriptCommand                                       = "rscript-command";

    /** The Constant EVALUATOR_STATISTICS. */
    private static final String EVALUATOR_STATISTICS                                 = "evaluator.statistics";

    /** The Constant TTestLimitPValue. */
    private static final String TTestLimitPValue                                     = "t-test-limit-p-value";

    /** The Constant defaultEqualityInterval. */
    private static final String defaultEqualityInterval                              = "default-equality-interval";

    /** The Constant minimumSampleCountWarningLimit. */
    private static final String minimumSampleCountWarningLimit                       = "minimum-sample-count-warning-limit";

    /** The Constant maximumStandardDeviationVsMeanDifferenceWarningLimit. */
    private static final String maximumStandardDeviationVsMeanDifferenceWarningLimit = "maximum-standard-deviation-vs-mean-difference-warning-limit";

    /** The Constant maximumMedianVsMeanDifferenceWarningLimit. */
    private static final String maximumMedianVsMeanDifferenceWarningLimit            = "maximum-median-vs-mean-difference-warning-limit";

    /** The Constant EVALUATOR_GRAPHS. */
    private static final String EVALUATOR_GRAPHS                                     = "evaluator.graphs";

    /** The Constant graphMaxYAxisLimitForNormalDistribution. */
    private static final String graphMaxYAxisLimitForNormalDistribution              = "graph-maximum-normal-density-y-axis-limit";

    /** The Constant graphImageWidth. */
    private static final String graphImageWidth                                      = "graph-image-width";

    /** The Constant graphImageHeight. */
    private static final String graphImageHeight                                     = "graph-image-height";

    /** The Constant graphHistogramMinimumBinCount. */
    private static final String graphHistogramMinimumBinCount                        = "histogram-minimum-bin-count";

    /** The Constant graphHistogramMaximumBinCount. */
    private static final String graphHistogramMaximumBinCount                        = "histogram-maximum-bin-count";

    /** The Constant EVALUATOR_GRAPHS_MEASUREMENT. */
    private static final String EVALUATOR_GRAPHS_MEASUREMENT                         = "evaluator.graphs.measurement";

    /** The Constant EVALUATOR_GRAPHS_COMPARISON. */
    private static final String EVALUATOR_GRAPHS_COMPARISON                          = "evaluator.graphs.comparison";

    /** The Constant EVALUATOR_GRAPH_MEASUREMENT. */
    private static final String graphDeclaration                                     = "graph";

    /** The Constant EVALUATOR_GRAPHS_COLORS. */
    private static final String EVALUATOR_GRAPHS_COLORS                              = "evaluator.graphs.colors";

    /** The Constant graphBackgroundColor. */
    private static final String graphBackgroundColor                                 = "background";

    /** The Constant graphTextColor. */
    private static final String graphTextColor                                       = "text";

    /** The Constant EVALUATOR_GRAPH_BACKGROUND_COLOR. */
    private static final String graphIsBackgroundTransparent                         = "background-transparent";

    /** The Constant EVALUATOR_GRAPHS_COLORS. */
    private static final String graphSampleColor                                     = "sample";

    /**
     * Saves SPL evaluator configuration to provided INI instance.
     * 
     * @param config
     *            The SPL evaluator configuration.
     * @param output
     *            The output.
     * 
     */
    static void saveSplEvaluatorConfiguration(SplEvaluatorConfiguration config, Ini output) {
        encodeConfiguration(config, output);
    }

    /**
     * Load SPL evaluator configuration from INI instance.
     * 
     * @param input
     *            The input.
     * @return The SPL evaluator configuration.
     * @throws ConversionException
     *             The conversion exception.
     */
    static SplEvaluatorConfiguration loadSplEvaluatorConfiguration(Ini input) throws ConversionException {
        try {
            return decodeSplEvaluatorConfiguration(input);
        } catch (ConversionException e) {
            throw new ConversionException(e);
        }
    }

    /**
     * Encodes configuration to provided INI instance.
     * 
     * @param config
     *            The SPL evaluator configuration to endode.
     * @param ini
     *            The INI to add configuration values to.
     * @return Same instance as {@code ini} argument.
     */
    private static Ini encodeConfiguration(SplEvaluatorConfiguration config, Ini ini) {

        IniManipulator.writeBoolean(ini, EVALUATOR_OUTPUT, generateHtmlOutput, config.isGenerateHtmlOutput());
        IniManipulator.writeBoolean(ini, EVALUATOR_OUTPUT, generateXmlOutput, config.isGenerateXmlOutput());
        IniManipulator.writeBoolean(ini, EVALUATOR_OUTPUT, generateGraphOutput, config.isGenerateGraphOutput());

        IniManipulator.writeLong(ini, EVALUATOR_STATISTICS, minimumSampleCountWarningLimit, config.getMinimumSampleCountWarningLimit());

        IniManipulator.writeDouble(ini, EVALUATOR_STATISTICS, TTestLimitPValue, config.getTTestLimitPValue());
        IniManipulator.writeDouble(ini, EVALUATOR_STATISTICS, defaultEqualityInterval, config.getEqualityInterval());
        IniManipulator.writeDouble(ini, EVALUATOR_STATISTICS, maximumStandardDeviationVsMeanDifferenceWarningLimit,
                config.getMaximumStandardDeviationVsMeanDifferenceWarningLimit());
        IniManipulator.writeDouble(ini, EVALUATOR_STATISTICS, maximumMedianVsMeanDifferenceWarningLimit, config.getMaximumMedianVsMeanDifferenceWarningLimit());

        IniManipulator.writeInteger(ini, EVALUATOR_GRAPHS, graphImageWidth, config.getGraphImageWidth());
        IniManipulator.writeInteger(ini, EVALUATOR_GRAPHS, graphImageHeight, config.getGraphImageHeight());
        IniManipulator.writeString(ini, EVALUATOR_GRAPHS, rscriptCommand, config.getRScriptCommand());
        IniManipulator.writeInteger(ini, EVALUATOR_GRAPHS, graphHistogramMinimumBinCount, config.getHistogramMinimumBinCount());
        IniManipulator.writeInteger(ini, EVALUATOR_GRAPHS, graphHistogramMaximumBinCount, config.getHistogramMaximumBinCount());

        int itemIndex;

        IniManipulator.writeGraphTypes(ini, EVALUATOR_GRAPHS_MEASUREMENT, graphDeclaration, config.getMeasurementGraphTypes());
        IniManipulator.writeGraphTypes(ini, EVALUATOR_GRAPHS_COMPARISON, graphDeclaration, config.getComparisonGraphTypes());

        IniManipulator.writeColor(ini, EVALUATOR_GRAPHS_COLORS, graphTextColor, config.getGraphTextColor());
        IniManipulator.writeColor(ini, EVALUATOR_GRAPHS_COLORS, graphBackgroundColor, config.getGraphBackgroundColor());
        IniManipulator.writeBoolean(ini, EVALUATOR_GRAPHS_COLORS, graphIsBackgroundTransparent, config.isGraphBackgroundTransparent());

        itemIndex = 1;
        for (Color c : config.getGraphSampleColors()) {
            IniManipulator.writeColor(ini, EVALUATOR_GRAPHS_COLORS, graphSampleColor + itemIndex, c);
            ++itemIndex;
        }

        IniManipulator.writeDouble(ini, EVALUATOR_GRAPHS, graphMaxYAxisLimitForNormalDistribution, config.getGraphMaximumNormalDensityYAxisLimit());

        return ini;
    }

    /**
     * Reads configuration values for SplEvaluatorConfiguration from provided
     * INI instance.
     * 
     * @param ini
     *            The INI instance to read values from.
     * @return The SPL evaluator configuration.
     * @throws ConversionException
     *             The conversion exception.
     */
    private static SplEvaluatorConfiguration decodeSplEvaluatorConfiguration(Ini ini) throws ConversionException {
        SplEvaluatorConfiguration config = SplEvaluatorConfiguration.createDefaultConfigurationWithoutGraphs();

        config.setGenerateHtmlOutput(IniManipulator.readBoolean(ini, EVALUATOR_OUTPUT, generateHtmlOutput, config.isGenerateHtmlOutput()));

        config.setGenerateXmlOutput(IniManipulator.readBoolean(ini, EVALUATOR_OUTPUT, generateXmlOutput, config.isGenerateXmlOutput()));

        config.setGenerateGraphOutput(IniManipulator.readBoolean(ini, EVALUATOR_OUTPUT, generateGraphOutput, config.isGenerateGraphOutput()));

        config.setMinimumSampleCountWarningLimit(IniManipulator.readLong(ini, EVALUATOR_STATISTICS, minimumSampleCountWarningLimit,
                config.getMinimumSampleCountWarningLimit()));

        config.setTTestLimitPValue(IniManipulator.readDouble(ini, EVALUATOR_STATISTICS, TTestLimitPValue, config.getTTestLimitPValue()));
        config.setEqualityInterval(IniManipulator.readDouble(ini, EVALUATOR_STATISTICS, defaultEqualityInterval, config.getEqualityInterval()));

        config.setMaximumStandardDeviationVsMeanDifferenceWarningLimit(IniManipulator.readDouble(ini, EVALUATOR_STATISTICS,
                maximumStandardDeviationVsMeanDifferenceWarningLimit, config.getMaximumStandardDeviationVsMeanDifferenceWarningLimit()));

        config.setMaximumMedianVsMeanDifferenceWarningLimit(IniManipulator.readDouble(ini, EVALUATOR_STATISTICS, maximumMedianVsMeanDifferenceWarningLimit,
                config.getMaximumMedianVsMeanDifferenceWarningLimit()));

        config.setGraphMaximumNormalDensityYAxisLimit(IniManipulator.readDouble(ini, EVALUATOR_GRAPHS, graphMaxYAxisLimitForNormalDistribution,
                config.getGraphMaximumNormalDensityYAxisLimit()));

        config.setGraphImageWidth(IniManipulator.readInteger(ini, EVALUATOR_GRAPHS, graphImageWidth, config.getGraphImageWidth()));

        config.setGraphImageHeight(IniManipulator.readInteger(ini, EVALUATOR_GRAPHS, graphImageHeight, config.getGraphImageHeight()));

        config.setRScriptCommand(IniManipulator.readString(ini, EVALUATOR_GRAPHS, rscriptCommand, config.getRScriptCommand()));

        config.setHistogramMinimumBinCount(IniManipulator.readInteger(ini, EVALUATOR_GRAPHS, graphHistogramMinimumBinCount,
                config.getHistogramMinimumBinCount()));

        config.setHistogramMaximumBinCount(IniManipulator.readInteger(ini, EVALUATOR_GRAPHS, graphHistogramMaximumBinCount,
                config.getHistogramMaximumBinCount()));

        config.setGraphBackgroundColor(IniManipulator.readColor(ini, EVALUATOR_GRAPHS_COLORS, graphBackgroundColor, config.getGraphBackgroundColor()));

        config.setGraphBackgroundTransparent(IniManipulator.readBoolean(ini, EVALUATOR_GRAPHS_COLORS, graphIsBackgroundTransparent,
                config.isGraphBackgroundTransparent()));

        {
            Collection<GraphDefinition> loadedGraphTypes;

            loadedGraphTypes = IniManipulator.readGraphTypes(ini, EVALUATOR_GRAPHS_MEASUREMENT, graphDeclaration);
            if (loadedGraphTypes != null) {
                config.getMeasurementGraphTypes().addAll(loadedGraphTypes);
            } else {
                config.getMeasurementGraphTypes().addAll(SplEvaluatorConfiguration.getDefaultMeasurementGraphs());
            }

            loadedGraphTypes = IniManipulator.readGraphTypes(ini, EVALUATOR_GRAPHS_COMPARISON, graphDeclaration);
            if (loadedGraphTypes != null) {
                config.getComparisonGraphTypes().addAll(loadedGraphTypes);
            } else {
                config.getComparisonGraphTypes().addAll(SplEvaluatorConfiguration.getDefaultComparisonGraphs());
            }

        }

        {

            Collection<Color> loadedColors = IniManipulator.readColors(ini, EVALUATOR_GRAPHS_COLORS, graphSampleColor);
            if (loadedColors != null) {
                config.setGraphSampleColors(loadedColors);
            }

        }

        return config;
    }
}
