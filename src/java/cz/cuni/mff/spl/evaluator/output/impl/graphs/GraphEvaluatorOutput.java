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
package cz.cuni.mff.spl.evaluator.output.impl.graphs;

import java.io.File;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.evaluator.FileNameMapper;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.graphs.GraphProvider;
import cz.cuni.mff.spl.evaluator.graphs.MeasurementSampleDescriptor;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.StoreSplOutputFileMappingImpl;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluator;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.utils.StoreUtils;
import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * Generates graph files for measurements and comparisons.
 * <p>
 * Provides method {@link #getGraphFileMapping()} for sharing graph file
 * mapping. See {@link GraphKeyFactory} how to obtain proper keys for mapping.
 * <p>
 * Generated images are stored in PNG format.
 * <p>
 * Types of generated graphs are defined in {@link GraphTypes}.
 * 
 * @author Martin Lacina
 * 
 * @see GraphKeyFactory
 * @see GraphTypes
 */
public class GraphEvaluatorOutput implements EvaluatorOutput {

    /** The logger. */
    private static final SplLog       logger        = SplLogger.getLogger(GraphEvaluatorOutput.class);

    /** The file extension for PNG image files. */
    private static final String       PNG_EXTENSION = ".png";
    /** The file prefix for graphs. */
    private static final String       GRAPH_PREFIX  = "g-";

    /** The evaluation configuration. */
    private SplEvaluatorConfiguration evaluationConfiguration;

    /** The graph files mapping. */
    private BasicOutputFileMapping    graphFileMapping;

    /** The Histogram creator. */
    private GraphProvider             graphProvider;

    /**
     * Gets the graph file mapping.
     * 
     * @return The graph file mapping.
     */
    public BasicOutputFileMapping getGraphFileMapping() {
        return graphFileMapping;
    }

    /**
     * Instantiates a new graph file generator.
     */
    public GraphEvaluatorOutput() {
    }

    @Override
    public void init(ConfigurationBundle configuration, Info info, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory) {
        this.evaluationConfiguration = configuration.getEvaluatorConfig();
        this.graphFileMapping = new StoreSplOutputFileMappingImpl(outputStoreDirectory, GRAPH_PREFIX, PNG_EXTENSION);
        graphProvider = new GraphProvider(evaluationConfiguration, null);
    }

    /**
     * Sets the temporary directory which is used to generate Rscript files.
     * 
     * @param workingDirectory
     *            The new temporary directory.
     */
    public void setTemporaryDirectory(File workingDirectory) {
        workingDirectory.mkdirs();
        graphProvider = new GraphProvider(evaluationConfiguration, workingDirectory);
    }

    /**
     * Saves graph and adds mapping with composite key consisting of related
     * object and graph type.
     * 
     * @param relatedTo
     *            The object graph is related to.
     * @param graphDefinition
     *            The graph definition.
     * @param prefix
     *            The file name prefix.
     * @param pngImage
     *            The PNG image bytes to save.
     */
    private void saveGraph(Object relatedTo, GraphDefinition graphDefinition, String prefix, byte[] pngImage) {
        Object key = GraphKeyFactory.createGraphKey(relatedTo, graphDefinition);
        try {
            IStoreFile targetFile = this.graphFileMapping.getOutputFile(key, prefix, PNG_EXTENSION);
            StoreUtils.saveToStoreFile(targetFile, pngImage);
        } catch (StoreException e) {
            this.graphFileMapping.releaseIStoreFile(key);
            logger.error(e, "Unable to save graph file to store for '%s' with graph type '%s'", relatedTo, graphDefinition);
        }
    }

    @Override
    public void generateMeasurementOutput(MeasurementSample measurementSample) {
        Measurement m = measurementSample.getMeasurement();

        if (measurementSample.getSampleCount() == 0) {
            return;
        }

        String filePrefix = GRAPH_PREFIX + FileNameMapper.getMeasurementFileNamePrefix(m);

        for (GraphDefinition graphType : evaluationConfiguration.getMeasurementGraphTypes()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            generateGraph(graphType, m, filePrefix + "-" + graphType.getBasicGraphType().name(), new MeasurementSampleDescriptor(measurementSample));
        }

    }

    private void generateGraph(GraphDefinition graphType, Object relatedTo, String filePrefix, MeasurementSampleDescriptor... measurementSample) {
        if (checkIfAlreadyGenerated(GraphKeyFactory.createGraphKey(measurementSample, graphType))) {
            return;
        }
        try {
            byte[] image = graphProvider.createChartPNGFor(graphType, measurementSample);
            if (image != null) {
                saveGraph(relatedTo, graphType, filePrefix, image);
            }
        } catch (MeasurementDataNotFoundException e) {
            logger.error(e, "Data not found, unable to generate graph (%s) for %s.",
                    graphType.getBasicGraphType(),
                    StringUtils.createOneString(measurementSample, ", "));
        }
    }

    @Override
    public void generateComparisonOutput(ComparisonEvaluationResult comparisonResult) {
        Comparison c = comparisonResult.getComparison();

        String filePrefix = GRAPH_PREFIX + FileNameMapper.getComparisonFileNamePrefix(c);

        MeasurementSampleDescriptor leftSample = new MeasurementSampleDescriptor(c.getLeftLambda() != null,
                ComparisonEvaluator.getLambdaMultiplier(c.getLeftLambda()), comparisonResult.getLeftMeasurementSample());
        MeasurementSampleDescriptor rightSample = new MeasurementSampleDescriptor(c.getRightLambda() != null,
                ComparisonEvaluator.getLambdaMultiplier(c.getRightLambda()),
                comparisonResult.getRightMeasurementSample());

        for (GraphDefinition graphType : evaluationConfiguration.getComparisonGraphTypes()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            generateGraph(graphType, c, filePrefix + "-" + graphType.getBasicGraphType().name(), leftSample, rightSample);
        }

    }

    @Override
    public void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationiResult) {
        // no graphs for formula
    }

    @Override
    public void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult) {
        // no graphs for annotation
    }

    @Override
    public void close() {
        // nothing to be done
    }

    /**
     * Checks if files for specified key have already been generated.
     * Files are considered generated, when HTML file mapping hold mapping for
     * supplied key.
     * 
     * @param key
     *            The key to check.
     * @return True, if files are considered to have been generated.
     */
    private boolean checkIfAlreadyGenerated(Object key) {
        return this.graphFileMapping.getMappedObjects().contains(key);
    }
}
