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
package cz.cuni.mff.spl.evaluator;

import java.io.File;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider;
import cz.cuni.mff.spl.evaluator.input.MeasurementSampleProvider;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutputAggregator;
import cz.cuni.mff.spl.evaluator.output.impl.EvaluationProgressMonitor;
import cz.cuni.mff.spl.evaluator.output.impl.graphs.GraphEvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.impl.html2.Html2EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.impl.xml.XmlEvaluatorOutput;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueCheckerImpl;

/**
 * The main class of evaluator.
 * 
 * @author Martin Lacina
 */
public class Evaluator {

    /**
     * Evaluates formulas in provided {@link Info} instance.
     * 
     * Access to measurement data is provided by {@link MeasurementDataProvider}
     * .
     * 
     * @param configuration
     *            The configuration.
     * @param evaluationContext
     *            The SPL context with formulas to evaluate.
     * @param measurementSampleProvider
     *            The measurement sample provider.
     * @param outputStoreDirectory
     *            The output store directory.
     * @param temporaryDirectory
     *            The temporary directory.
     */
    public static void evaluate(ConfigurationBundle configuration, Info evaluationContext, MeasurementSampleProvider measurementSampleProvider,
            IStoreDirectory outputStoreDirectory, File temporaryDirectory) {

        InvokedExecutionConfiguration.checkIfExecutionAborted();

        StatisticValueChecker checker = new StatisticValueCheckerImpl(configuration.getEvaluatorConfig());

        EvaluatorOutput evaluatorOutput = createEvaluatorOutput(configuration, checker, evaluationContext, outputStoreDirectory, temporaryDirectory);

        new EvaluatorImpl(measurementSampleProvider, checker, configuration.getEvaluatorConfig()).evaluateAllFormulas(evaluationContext, evaluatorOutput);

    }

    /**
     * Creates combined evaluator output with graph, HTML and XML support.
     * 
     * Note that graph output creator has to be added first, as graphs have to
     * be created before they can be used.
     * 
     * Usage of graphs in HTML is done by passing graph file mapping from graph
     * creator to HTML creator.
     * 
     * @param configuration
     *            The configuration.
     * @param statisticValueChecker
     *            The statistic value checker.
     * @param info
     *            The info.
     * @param outputStoreDirectory
     *            The output store directory.
     * @param temporaryDirectory
     *            The temporary directory.
     * @return The evaluator output.
     */
    private static EvaluatorOutput createEvaluatorOutput(ConfigurationBundle configurationBundle, StatisticValueChecker statisticValueChecker, Info info,
            IStoreDirectory outputStoreDirectory, File temporaryDirectory) {

        SplEvaluatorConfiguration configuration = configurationBundle.getEvaluatorConfig();

        EvaluatorOutputAggregator output = new EvaluatorOutputAggregator();

        GraphEvaluatorOutput graphs = null;
        Html2EvaluatorOutput html = null;
        XmlEvaluatorOutput xml = null;

        if (configuration.isGenerateGraphOutput()) {
            graphs = new GraphEvaluatorOutput();
            output.addEvaluatorOutput(graphs);
        }
        if (configuration.isGenerateXmlOutput()) {
            xml = new XmlEvaluatorOutput();
            output.addEvaluatorOutput(xml);
        }
        if (configuration.isGenerateHtmlOutput()) {
            html = new Html2EvaluatorOutput();
            output.addEvaluatorOutput(html);
        }
        EvaluationProgressMonitor progress = new EvaluationProgressMonitor();
        output.addEvaluatorOutput(progress);

        output.init(configurationBundle, info, statisticValueChecker, outputStoreDirectory);

        // link Graph output to HTML and XML outputs
        if (graphs != null) {
            BasicOutputFileMapping graphsMapping = graphs.getGraphFileMapping();
            if (html != null) {
                html.setGraphsMapping(graphsMapping);
            }
            if (xml != null) {
                xml.setGraphsMapping(graphsMapping);
            }
        }

        // set temporary directory
        if (html != null) {
            html.setTemporaryDirectory(new File(temporaryDirectory, "html"));
        }

        if (graphs != null) {
            graphs.setTemporaryDirectory(new File(temporaryDirectory, "graphs"));
        }
        return output;
    }

}
