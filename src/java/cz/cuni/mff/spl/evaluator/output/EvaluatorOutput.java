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

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The interface for SPL Evaluator Output.
 * 
 * @author Martin Lacina
 */
public interface EvaluatorOutput {

    /**
     * Initializes the evaluator output.
     * 
     * @param configuration
     *            The configuration.
     * @param context
     *            The evaluation context.
     * @param statisticValueChecker
     *            The statistic value checker.
     * @param outputStoreDirectory
     *            The output store directory.
     * @throws OutputNotInitializedException
     *             The output not initialized exception indicates, that output
     *             will not work.
     */
    void init(ConfigurationBundle configuration, Info context, StatisticValueChecker statisticValueChecker, IStoreDirectory outputStoreDirectory)
            throws OutputNotInitializedException;

    /**
     * Generate measurement output.
     * 
     * @param measurementSample
     *            The measurement sample.
     */
    void generateMeasurementOutput(MeasurementSample measurementSample);

    /**
     * Generate comparison output.
     * 
     * @param result
     *            The comparison evaluation result.
     */
    void generateComparisonOutput(ComparisonEvaluationResult result);

    /**
     * Generate formula output.
     * 
     * @param formulaEvaluationResult
     *            The formula evaluation result.
     */
    void generateFormulaOutput(FormulaEvaluationResult formulaEvaluationResult);

    /**
     * Generate annotation output.
     * 
     * @param annotationEvaluationResult
     *            The annotation evaluation result.
     */
    void generateAnnotationOutput(AnnotationEvaluationResult annotationEvaluationResult);

    /**
     * Close evaluator output. To be called when output ends.
     */
    void close();

    /**
     * The evaluator output not initialized exception.
     * 
     * @author Martin Lacina
     */
    public static class OutputNotInitializedException extends Exception {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -5667352883333814471L;

        /**
         * Instantiates a new output not initialized exception.
         */
        public OutputNotInitializedException() {

        }

        /**
         * Instantiates a new output not initialized exception.
         * 
         * @param message
         *            The message.
         */
        public OutputNotInitializedException(String message) {
            super(message);
        }

        /**
         * Instantiates a new output not initialized exception.
         * 
         * @param cause
         *            The cause.
         */
        public OutputNotInitializedException(Throwable cause) {
            super(cause);
        }

        /**
         * Instantiates a new output not initialized exception.
         * 
         * @param message
         *            The message.
         * @param cause
         *            The cause.
         */
        public OutputNotInitializedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
