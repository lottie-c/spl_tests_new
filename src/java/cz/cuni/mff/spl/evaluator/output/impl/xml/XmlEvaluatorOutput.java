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
package cz.cuni.mff.spl.evaluator.output.impl.xml;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.XmlConversion;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.impl.graphs.GraphKeyFactory;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ResultData;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * XML output support for SPL evaluator.
 * 
 * <br />
 * 
 * Generated result file is ({@link #RESULT_XML_FILENAME}) is placed to
 * directory provided to {@code init} method.
 * 
 * <br />
 * 
 * Graphs mapping ({@link #setGraphsMapping(BasicOutputFileMapping)}) has to be
 * used to add generated graphs to output.
 * 
 * @author Martin Lacina
 */
public class XmlEvaluatorOutput implements EvaluatorOutput {

    /** The logger. */
    private static final SplLog    logger              = SplLogger.getLogger(XmlEvaluatorOutput.class);

    /** The file name for {@code spl-result.xml}. */
    public static final String     RESULT_XML_FILENAME = "spl-result.xml";

    /** The name of evaluated {@link Info} instance file. */
    public static final String     INFO_XML_FILENAME   = "spl-evaluated-info.xml";

    /** The evaluator configuration. */
    private ConfigurationBundle    configuration;

    /** The store directory. */
    private IStoreDirectory        outputStoreDirectory;

    /** The created result data. */
    private ResultData             resultData;

    /**
     * The output graph file mapping.
     * Should be set to shared graph file mapping.
     */
    private BasicOutputFileMapping graphsMapping;

    /**
     * Gets the graph mapping.
     * 
     * @return The graph mapping.
     */
    public BasicOutputFileMapping getGraphsMapping() {
        return graphsMapping;
    }

    /**
     * Sets the graph mapping.
     * 
     * @param graphFileMapping
     *            The new graph mapping.
     */
    public void setGraphsMapping(BasicOutputFileMapping graphFileMapping) {
        this.graphsMapping = graphFileMapping;
    }

    /**
     * Instantiates a new XML evaluator output.
     */
    public XmlEvaluatorOutput() {
    }

    @Override
    public void init(ConfigurationBundle configuration, Info context, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory) {
        this.configuration = configuration;
        this.resultData = new ResultData(context);
        this.resultData.setConfiguration(configuration);
        this.outputStoreDirectory = outputStoreDirectory;

        try {
            IStoreFile targetFile = outputStoreDirectory.createFile(INFO_XML_FILENAME);
            XmlConversion.ConvertClassToXml(context, targetFile.getOutputStream());
        } catch (ConversionException e) {
            logger.error(e, "Unable to convert evaluated info to XML.");
        } catch (StoreException e) {
            logger.error(e, "Unable to save evaluated info to XML.");
        }
    }

    /**
     * Generate measurement output.
     * 
     * @param measurementSample
     *            The measurement sample.
     */
    @Override
    public void generateMeasurementOutput(MeasurementSample measurementSample) {
        if (graphsMapping != null) {
            for (GraphDefinition graphType : configuration.getEvaluatorConfig().getMeasurementGraphTypes()) {
                Object graphKey = GraphKeyFactory.createGraphKey(measurementSample.getMeasurement(), graphType);
                IStoreFile graphFile = this.graphsMapping.getIStoreFile(graphKey);
                if (graphFile != null) {
                    String filename = graphFile.getName();
                    MeasurementGraphReference reference = new MeasurementGraphReference(graphType, measurementSample.getMeasurement(), filename);
                    this.resultData.addMeasurementGraphReference(reference);
                }
            }
        }
        this.resultData.getMeasurementSamples().add(measurementSample);
    }

    /**
     * Generate comparison output.
     * 
     * @param result
     *            The result.
     */
    @Override
    public void generateComparisonOutput(ComparisonEvaluationResult result) {
        if (graphsMapping != null) {
            for (GraphDefinition graphType : configuration.getEvaluatorConfig().getComparisonGraphTypes()) {
                Object graphKey = GraphKeyFactory.createGraphKey(result.getComparison(), graphType);
                IStoreFile graphFile = this.graphsMapping.getIStoreFile(graphKey);
                if (graphFile != null) {
                    String filename = graphFile.getName();
                    ComparisonGraphReference reference = new ComparisonGraphReference(graphType, result, filename);
                    this.resultData.addComparisonGraphReference(reference);
                }
            }
        }
    }

    /**
     * Generate formula output.
     * 
     * @param formulaEvaluationiResult
     *            The formula evaluationi result.
     */
    @Override
    public void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationiResult) {
        // formula results are stored within annotation result
    }

    /**
     * Generate annotation output.
     * 
     * @param annotationEvaluationResult
     *            The annotation evaluation result.
     */
    @Override
    public void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult) {
        this.resultData.getAnnotationEvaluationResults().add(annotationEvaluationResult);
    }

    /**
     * Closes XML output and writes accumulated data to XML file.
     */
    @Override
    public void close() {
        try {
            IStoreFile targetFile = outputStoreDirectory.createFile(RESULT_XML_FILENAME);
            XmlConversion.ConvertClassToXml(resultData, targetFile.getOutputStream());
        } catch (ConversionException e) {
            logger.error(e, "Unable to convert evaluator result to XML.");
        } catch (StoreException e) {
            logger.error(e, "Unable to save evaluator result to XML.");
        }
    }

}
