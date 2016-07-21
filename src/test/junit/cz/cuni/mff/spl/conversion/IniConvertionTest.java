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

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;

/**
 * The Class IniConvertionTest.
 * 
 * @author Martin Lacina
 */
public class IniConvertionTest {

    /**
     * Inits the test.
     */
    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    /** The Constant INI_BASE. */
    public static final String                     INI_BASE    = "[evaluator.output]\n"
                                                                       + "generate-html-output = 1\n"
                                                                       + "generate-xml-output = 1\n"
                                                                       + "generate-graph-output = 0\n"
                                                                       + "\n"
                                                                       + "[evaluator.statistics]\n"
                                                                       + "minimum-sample-count-warning-limit = 22\n"
                                                                       + "t-test-limit-p-value = 10.0\n"
                                                                       + "default-equality-interval = 0.05\n"
                                                                       + "maximum-standard-deviation-vs-mean-difference-warning-limit = 555.0\n"
                                                                       + "maximum-median-vs-mean-difference-warning-limit = 333.0\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs]\n"
                                                                       + "graph-image-width = 321\n"
                                                                       + "graph-image-height = 123\n"
                                                                       + "rscript-command = rscript.exe\n"
                                                                       + "histogram-minimum-bin-count = 100\n"
                                                                       + "histogram-maximum-bin-count = 10000\n"
                                                                       + "graph-maximum-normal-density-y-axis-limit = 1.0E-4\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.measurement]\n"
                                                                       + "graph1 = Histogram(Sigma, 3.0, 1.0)\n"
                                                                       + "graph2 = DensityComparison(Sigma, 3.0, 1.0)\n"
                                                                       + "graph3 = Histogram(Quantile, 0.1, 99.1)\n"
                                                                       + "graph4 = Histogram(Quantile, 0.0, 99.0)\n"
                                                                       + "graph5 = Histogram(Quantile, 0.0, 95.0)\n"
                                                                       + "graph6 = TimeDiagram(Sigma, 3.0, 1.0)\n"
                                                                       + "graph7 = TimeDiagram(Quantile, 1.0, 99.0)\n"
                                                                       + "graph8 = Histogram(None)\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.comparison]\n"
                                                                       + "graph1 = DensityComparison(Sigma, 3.0, 1.0)\n"
                                                                       + "graph2 = Histogram(Quantile, 0.1, 99.9)\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.colors]\n"
                                                                       + "text = black\n"
                                                                       + "background = lightgray\n"
                                                                       + "background-transparent = 0\n"
                                                                       + "sample1 = rgb(10, 20, 30)\n"
                                                                       + "sample2 = red\n"
                                                                       + "sample3 = green\n"
                                                                       + "sample4 = blue\n";

    /** The Constant INI_DECODE. */
    public static final String                     INI_DECODE  = INI_BASE + "\n";

    /** The Constant INI_ENCODE. */
    public static final String                     INI_ENCODE  = INI_BASE
                                                                       + "sample5 = yellow\n"
                                                                       + "sample6 = orange\n"
                                                                       + "sample7 = cyan\n"
                                                                       + "sample8 = magenta\n"
                                                                       + "\n";

    /**
     * The contains default configuration, but declarations are messed a little.
     */
    public static final String                     INI_SKIPPED = "[evaluator.graphs]\n"
                                                                       + "graph-maximum-normal-density-y-axis-limit = 0.0001\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.comparison]\n"
                                                                       + "graph3 = Histogram(Quantile, 0.1, 99.9)\n"
                                                                       + "graph2 = DensityComparison(Sigma, 3.0, 1.0)\n"
                                                                       + "graph = will issue erorr to log as no int value is defined\n"
                                                                       + "ignored = will be ignored\n"
                                                                       + "graph100 = will issue erorr to log as no color value is defined\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.colors]\n"
                                                                       + "text = black\n"
                                                                       + "background = lightgray\n"
                                                                       + "sample11= cyan\n"
                                                                       + "sample13= magenta\n"
                                                                       + "sample-1 = red #note that negative indexes are supported too\n"
                                                                       + "sample7 = yellow\n"
                                                                       + "sample5 = green\n"
                                                                       + "sample3 = blue\n"
                                                                       + "sample9 = orange\n"
                                                                       + "sample = will issue erorr to log as no int value is defined\n"
                                                                       + "sample100 = will issue erorr to log as no color value is defined\n"
                                                                       + "ignored = will be ignored\n"
                                                                       + "\n";

    /**
     * The contains default configuration, but declarations are messed a little.
     */
    public static final String                     INI_DEFAULT = "[evaluator.output]\n"
                                                                       + "generate-html-output = 1\n"
                                                                       + "generate-xml-output = 1\n"
                                                                       + "generate-graph-output = 1\n"
                                                                       + "\n"
                                                                       + "[evaluator.statistics]\n"
                                                                       + "minimum-sample-count-warning-limit = 10\n"
                                                                       + "t-test-limit-p-value = 0.05\n"
                                                                       + "default-equality-interval = 0.05\n"
                                                                       + "maximum-standard-deviation-vs-mean-difference-warning-limit = 75.0\n"
                                                                       + "maximum-median-vs-mean-difference-warning-limit = 20.0\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs]\n"
                                                                       + "graph-image-width = 800\n"
                                                                       + "graph-image-height = 600\n"
                                                                       + "rscript-command = Rscript\n"
                                                                       + "histogram-minimum-bin-count = 100\n"
                                                                       + "histogram-maximum-bin-count = 10000\n"
                                                                       + "graph-maximum-normal-density-y-axis-limit = 1.0E-4\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.measurement]\n"
                                                                       + "graph1 = Histogram(None)\n"
                                                                       + "graph2 = Histogram(Sigma, 3.0, 1.0)\n"
                                                                       + "graph3 = DensityComparison(Sigma, 3.0, 1.0)\n"
                                                                       + "graph4 = Histogram(Quantile, 0.1, 99.1)\n"
                                                                       + "graph5 = Histogram(Quantile, 0.0, 99.0)\n"
                                                                       + "graph6 = Histogram(Quantile, 0.0, 95.0)\n"
                                                                       + "graph7 = TimeDiagram(Sigma, 3.0, 1.0)\n"
                                                                       + "graph8 = TimeDiagram(Quantile, 1.0, 99.0)\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.comparison]\n"
                                                                       + "graph1 = DensityComparison(Sigma, 3.0, 1.0)\n"
                                                                       + "graph2 = Histogram(Quantile, 0.1, 99.9)\n"
                                                                       + "\n"
                                                                       + "[evaluator.graphs.colors]\n"
                                                                       + "text = black\n"
                                                                       + "background = lightgray\n"
                                                                       + "background-transparent = 0\n"
                                                                       + "sample1 = red\n"
                                                                       + "sample2 = blue\n"
                                                                       + "sample3 = green\n"
                                                                       + "sample4 = yellow\n"
                                                                       + "sample5 = orange\n"
                                                                       + "sample6 = cyan\n"
                                                                       + "sample7 = magenta\n"
                                                                       + "\n";

    /** The Constant testedConfiguration. */
    private static final SplEvaluatorConfiguration testedConfiguration;

    static {
        testedConfiguration = SplEvaluatorConfiguration.createDefaultConfiguration();

        testedConfiguration.setTTestLimitPValue(10);
        testedConfiguration.setGenerateGraphOutput(false);
        testedConfiguration.setRScriptCommand("rscript.exe");
        testedConfiguration.setGraphImageHeight(123);
        testedConfiguration.setGraphImageWidth(321);
        testedConfiguration.setMaximumMedianVsMeanDifferenceWarningLimit(333);
        testedConfiguration.setMaximumStandardDeviationVsMeanDifferenceWarningLimit(555);
        testedConfiguration.setMinimumSampleCountWarningLimit(22);

        // move fist graph for measurement to the end
        GraphDefinition definition = testedConfiguration.getMeasurementGraphTypes().iterator().next();
        testedConfiguration.getMeasurementGraphTypes().remove(definition);
        testedConfiguration.getMeasurementGraphTypes().add(definition);

        // set colors - defined colors first
        List<Color> definedColors = Arrays.asList(
                new Color(10, 20, 30),
                Color.red,
                Color.green,
                Color.blue
                );
        testedConfiguration.getGraphSampleColors().removeAll(definedColors);
        testedConfiguration.getGraphSampleColors().addAll(0, definedColors);

    }

    /**
     * Test spl evaluator default configuration encoding.
     * 
     * @throws ConversionException
     *             The conversion exception.
     */
    @Test
    public void testSplEvaluatorDefaultConfigurationEncoding() throws ConversionException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        IniConversion.saveSplEvaluatorConfiguration(SplEvaluatorConfiguration.createDefaultConfiguration(), output);

        String result = output.toString().replace("\r", "");

        assertEquals(INI_DEFAULT, result);
    }

    /**
     * Test that SPL evaluator configuration is written to INI output.
     * 
     * @throws ConversionException
     *             The conversion exception.
     */
    @Test
    public void testSplEvaluatorConfigurationEncoding() throws ConversionException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        IniConversion.saveSplEvaluatorConfiguration(testedConfiguration, output);

        String result = output.toString().replace("\r", "");

        assertEquals(INI_ENCODE, result);
    }

    /**
     * Test that SPL evaluator configuration is loaded correctly from INI input.
     * 
     * @throws ConversionException
     *             The conversion exception.
     */
    @Test
    public void testSplEvaluatorConfigurationDecoding() throws ConversionException {
        ByteArrayInputStream input = new ByteArrayInputStream(INI_DECODE.getBytes());

        SplEvaluatorConfiguration decoded = IniConversion.loadSplEvaluatorConfiguration(input);

        assertEquals(testedConfiguration, decoded);
    }

    /**
     * Tests that default evaluator configuration is decoded on empty input.
     * 
     * @throws ConversionException
     *             The conversion exception.
     */
    @Test
    public void testDefaultEvaluatorConfigurationDecodedOnEmptyInput() throws ConversionException {
        SplEvaluatorConfiguration decoded = IniConversion.loadSplEvaluatorConfiguration(new ByteArrayInputStream(new byte[0]));

        assertEquals(SplEvaluatorConfiguration.createDefaultConfiguration(), decoded);
    }

    /**
     * Test default that evaluator configuration is decoded even when graph and
     * color definitions are in weird order.
     * 
     * @throws ConversionException
     *             The conversion exception.
     */
    @Test
    public void testDefaultEvaluatorConfigurationDecodingWithGraphAndColorDefinitionsInWeirdOrder() throws ConversionException {
        SplEvaluatorConfiguration decoded = IniConversion.loadSplEvaluatorConfiguration(new ByteArrayInputStream(INI_SKIPPED.getBytes()));
        assertEquals(SplEvaluatorConfiguration.createDefaultConfiguration(), decoded);
    }
}
