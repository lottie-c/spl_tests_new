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
package cz.cuni.mff.spl.evaluator.output.impl;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.evaluator.output.AnnotationPrettyPrinter;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Simple notification output implementation.
 * 
 * @author Martin Lacina
 */
public class EvaluationProgressMonitor implements EvaluatorOutput {

    /** The logger for output. */
    private static final SplLog logger = SplLogger.getLogger(EvaluationProgressMonitor.class);

    /** The default evaluation interval. */
    private double              defaultEvaluationInterval;

    @Override
    public void init(ConfigurationBundle configuration, Info context, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory) {
        defaultEvaluationInterval = configuration.getEvaluatorConfig().getEqualityInterval();
        logger.info("Evaluator output initialized.");
    }

    @Override
    public void generateMeasurementOutput(MeasurementSample measurementSample) {
        logger.trace("Processed measurement: " +
                AnnotationPrettyPrinter.createMeasurementOutput(measurementSample.getMeasurement()));
    }

    @Override
    public void generateComparisonOutput(ComparisonEvaluationResult result) {
        logger.trace("Processed comparison:  " +
                AnnotationPrettyPrinter.createComparisonOutput(result.getComparison(), defaultEvaluationInterval));
    }

    @Override
    public void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationResult) {
        logger.trace("Processed formula:     " +
                formulaEvaluationResult.getFormulaDeclaration().getImage().replace("\n", "").trim());
    }

    @Override
    public void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult) {
        logger.trace("Processed annotation:\t "
                + annotationEvaluationResult.getAnnotationLocation());
        logger.info("Processed annotation at location:\t "
                + annotationEvaluationResult.getAnnotationLocation().getBasicSignature());
    }

    @Override
    public void close() {
        logger.info("Evaluator output closed.");
    }

}
