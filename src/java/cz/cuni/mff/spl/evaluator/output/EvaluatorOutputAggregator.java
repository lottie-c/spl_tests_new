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
package cz.cuni.mff.spl.evaluator.output;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Stores multiple outputs and sends all request to all stored evaluator
 * outputs.
 * 
 * Evaluator outputs are called in order, in which they were added.
 * 
 * @author Martin Lacina
 * 
 */
public class EvaluatorOutputAggregator implements EvaluatorOutput {

    /** The logger. */
    private static final SplLog         logger           = SplLogger.getLogger(EvaluatorOutputAggregator.class);

    /** Stored evaluator outputs. */
    private final List<EvaluatorOutput> evaluatorOutputs = new LinkedList<>();

    /**
     * Adds the evaluator output.
     * 
     * When added evaluation output is {@code null}, than nothing happens.
     * 
     * @param evaluatorOutput
     *            The evaluator output to add.
     */
    public void addEvaluatorOutput(EvaluatorOutput evaluatorOutput) {
        if (evaluatorOutput != null) {
            this.evaluatorOutputs.add(evaluatorOutput);
        }
    }

    @Override
    public void init(ConfigurationBundle configurationBundle, Info context, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory) {

        for (Iterator<EvaluatorOutput> iterator = evaluatorOutputs.iterator(); iterator.hasNext();) {
            EvaluatorOutput output = iterator.next();
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            try {
                output.init(configurationBundle, context, statisticValueChecker, outputStoreDirectory);
            } catch (Throwable e) {
                logger.error(e, "Error during initialization of output [%s], disabling.", output.getClass());
                iterator.remove();
            }
        }
        InvokedExecutionConfiguration.checkIfExecutionAborted();
    }

    @Override
    public void generateMeasurementOutput(MeasurementSample measurementSample) {
        for (EvaluatorOutput output : evaluatorOutputs) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            try {
                output.generateMeasurementOutput(measurementSample);
            } catch (Exception e) {
                logger.error(e, "Error during generating measurement output in %s", output.getClass());
            }
        }
    }

    @Override
    public void generateComparisonOutput(ComparisonEvaluationResult result) {
        for (EvaluatorOutput output : evaluatorOutputs) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            try {
                output.generateComparisonOutput(result);
            } catch (Exception e) {
                logger.error(e, "Error during generating comparison output in %s", output.getClass());
            }
        }
    }

    @Override
    public void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationResult) {
        for (EvaluatorOutput output : evaluatorOutputs) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            try {
                output.generateFormulaOutput(formulaEvaluationResult);
            } catch (Exception e) {
                logger.error(e, "Error during generating formula output in %s", output.getClass());
            }
        }
    }

    @Override
    public void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult) {
        InvokedExecutionConfiguration.checkIfExecutionAborted();
        for (EvaluatorOutput output : evaluatorOutputs) {
            try {
                output.generateAnnotationOutput(annotationEvaluationResult);
            } catch (Exception e) {
                logger.error(e, "Error during generating annotation output in %s", output.getClass());
            }
        }
    }

    @Override
    public void close() {
        for (EvaluatorOutput output : evaluatorOutputs) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            try {
                output.close();
            } catch (Exception e) {
                logger.error(e, "Error during output closing in %s", output.getClass());
            }
        }
    }

}
