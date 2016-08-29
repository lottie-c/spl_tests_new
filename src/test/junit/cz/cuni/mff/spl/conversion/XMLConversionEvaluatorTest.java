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
import java.util.Arrays;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonResult;
import cz.cuni.mff.spl.evaluator.output.results.StatisticalResult;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSampleStatisticalData;

/**
 * @author Martin Lacina
 * 
 */
public class XMLConversionEvaluatorTest {

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Test
    public void convertComparisonResult() throws ConversionException {

        for (StatisticalResult crt : StatisticalResult.values()) {

            ComparisonResult comparisonResult = new ComparisonResult(1, crt, "My funny error message");

            String value = XmlConversion.ConvertClassToXml(comparisonResult);

            Object decoded = XmlConversion.ConvertClassFromXml(value);

            assertEquals(decoded, comparisonResult);
        }
    }

    @Test
    public void convertMeasurementSample() throws ConversionException {

        for (StatisticalResult crt : StatisticalResult.values()) {

            ComparisonResult comparisonResult = new ComparisonResult(1, crt, "My funny error message");

            String value = XmlConversion.ConvertClassToXml(comparisonResult);

            Object decoded = XmlConversion.ConvertClassFromXml(value);

            assertEquals(decoded, comparisonResult);
        }
    }

    @Test
    public void ensureStatisticalSummaryIsAssigned() throws ConversionException {
        MeasurementSampleStatisticalData data = new MeasurementSampleStatisticalData();
        data.setMaximum(10);
        data.setMean(11);
        data.setMedian(12);
        data.setSampleCount(13);
        data.setStandardDeviation(4);
        data.setVariance(16);

        MeasurementSample sample = new MeasurementSample();
        sample.setStatisticalData(data);

        String encoded = XmlConversion.ConvertClassToXml(sample);

        // System.out.println(value);

        MeasurementSample sampleDecoded = (MeasurementSample) XmlConversion.ConvertClassFromXml(encoded);

        assertEquals(data, sampleDecoded.getStatisticalData());

        assertEquals(sample.getStatisticalSummary(), sampleDecoded.getStatisticalSummary());
    }

    @Test
    public void testEvaluatorConfiguration() throws ConversionException {
        SplEvaluatorConfiguration configuration = SplEvaluatorConfiguration.createDefaultConfiguration();

        configuration.setGenerateGraphOutput(!configuration.isGenerateGraphOutput());
        configuration.setGenerateHtmlOutput(!configuration.isGenerateHtmlOutput());
        configuration.setGenerateXmlOutput(!configuration.isGenerateXmlOutput());
        configuration.setGraphBackgroundColor(new Color(123));
        configuration.setGraphBackgroundTransparent(!configuration.isGraphBackgroundTransparent());
        configuration.setGraphImageHeight(123);
        configuration.setGraphImageWidth(321);
        configuration.setGraphMaximumNormalDensityYAxisLimit(555);
        configuration.setGraphSampleColors(Arrays.asList(new Color(4546)));
        configuration.setGraphTextColor(new Color(323));
        configuration.setHistogramMaximumBinCount(456);
        configuration.setHistogramMinimumBinCount(321);
        configuration.setMaximumMedianVsMeanDifferenceWarningLimit(500);
        configuration.setMaximumStandardDeviationVsMeanDifferenceWarningLimit(600);
        configuration.setMinimumSampleCountWarningLimit(5000);
        configuration.setRScriptCommand("abc");
        configuration.setTTestLimitPValue(10);

        String value = XmlConversion.ConvertClassToXml(configuration);

        SplEvaluatorConfiguration configurationDecoded = (SplEvaluatorConfiguration) XmlConversion.ConvertClassFromXml(value);

        assertEquals(configuration, configurationDecoded);

    }

}
