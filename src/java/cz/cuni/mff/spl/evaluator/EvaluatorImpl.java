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

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.input.MeasurementSampleDataProvider;
import cz.cuni.mff.spl.evaluator.input.MeasurementSampleProvider;
import cz.cuni.mff.spl.evaluator.output.EvaluatorOutput;
import cz.cuni.mff.spl.evaluator.output.results.AnnotationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonResult;
import cz.cuni.mff.spl.evaluator.output.results.EvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.LogicalOperationEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluator;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluatorT;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluatorMWW;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluatorKS;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * SPL annotation evaluator implementation.
 * 
 * @author Martin Lacina
 * 
 */
class EvaluatorImpl {

    /** The sample provider. */
    private final MeasurementSampleProvider     measurementSampleProvider;

    /** The comparison evaluator for the t test. */
    private final ComparisonEvaluatorT           comparisonEvaluatorT;

    /** The comparison evaluator for the mann whitney u test*/
    private final ComparisonEvaluatorMWW        comparisonEvaluatorMWW;

   /*
     The comparison evaluator for the Kolmogorov Smirnov test*/
     private final ComparisonEvaluatorKS        comparisonEvaluatorKS;

   

    /**
     * Instantiates a new evaluator.
     * 
     * @param measurementSampleProvider
     *            The measurement sample provider.
     * @param checker
     *            The confidence to be used for p-value comparison.
     * @param configuration
     *            The configuration.
     */
    EvaluatorImpl(MeasurementSampleProvider measurementSampleProvider, StatisticValueChecker checker, SplEvaluatorConfiguration configuration) {
        this.measurementSampleProvider = measurementSampleProvider;
        this.comparisonEvaluatorT = new ComparisonEvaluatorT(configuration, checker);
        this.comparisonEvaluatorMWW = new ComparisonEvaluatorMWW(configuration, checker);
        this.comparisonEvaluatorKS = new ComparisonEvaluatorKS(configuration, checker);
    }

    /**
     * Evaluates all SPL formulas in provided SPL context.
     * 
     * @param context
     *            The context.
     * @param evaluatorOutput
     *            The evaluator output.
     */
    void evaluateAllFormulas(Info context, EvaluatorOutput evaluatorOutput) {
        for (AnnotationLocation annotationLocation : context.getAnnotationLocations()) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();

            AnnotationEvaluationResult annotationResult = new AnnotationEvaluationResult(annotationLocation);

            for (FormulaDeclaration formula : annotationLocation.getFormulas()) {
                InvokedExecutionConfiguration.checkIfExecutionAborted();
                if (formula.hasDeclarationBeenParsedSuccessfully()) {
                    FormulaEvaluationResult result = this.evaluateFormula(formula, evaluatorOutput);
                    annotationResult.addFormulaEvaluationResult(result);
                    evaluatorOutput.generateFormulaOutput(result);
                }
            }
            evaluatorOutput.generateAnnotationOutput(annotationResult);
        }
        evaluatorOutput.close();
    }

    /**
     * Evaluates formula declaration.
     * 
     * Evaluated formula declaration has to represent valid formula declaration,
     * i. e. {@link FormulaDeclaration#getFormula()} has to return formula.
     * 
     * @param formulaDeclaration
     *            The formula declaration.
     * @param outputProvider
     *            The output provider.
     * @return Formula evaluation result.
     */
    private FormulaEvaluationResult evaluateFormula(FormulaDeclaration formulaDeclaration, EvaluatorOutput outputProvider) {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        EvaluationResult formulaResult = evaluateFormulaPart(formulaDeclaration.getFormula(), outputProvider);

        return new FormulaEvaluationResult(formulaDeclaration, formulaResult);

    }

    /**
     * Evaluates formula part.
     * 
     * @param formula
     *            The formula.
     * @param outputProvider
     *            The output provider.
     * @return The evaluation result.
     */
    private EvaluationResult evaluateFormulaPart(Formula formula, EvaluatorOutput outputProvider) {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        if (formula instanceof Expression) {
            return evaluateExpression((Expression) formula, outputProvider);
        }

        if (formula instanceof Comparison) {
            return evaluateComparison((Comparison) formula, outputProvider);
        }

        throw new IllegalStateException("Unexpected formula data type: " + formula.getClass());
    }

    /**
     * Evaluates expression in formula tree.
     * 
     * @param formula
     *            The formula.
     * @param outputProvider
     *            The output provider.
     * @return True, if formula holds.
     */
    private EvaluationResult evaluateExpression(Expression formula, EvaluatorOutput outputProvider) {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        EvaluationResult leftOperandResult = evaluateFormulaPart(formula.getLeft(), outputProvider);

        EvaluationResult rightOperandResult = evaluateFormulaPart(formula.getRight(), outputProvider);

        LogicalOperationEvaluationResult result = new LogicalOperationEvaluationResult(formula, leftOperandResult, rightOperandResult);

        return result;
    }

    /**
     * Evaluates comparison in formula tree.
     * 
     * @param formula
     *            The formula.
     * @param outputProvider
     *            The output provider.
     * @return True, if formula holds.
     */
    private ComparisonEvaluationResult evaluateComparison(Comparison formula, EvaluatorOutput outputProvider) {
        InvokedExecutionConfiguration.checkIfExecutionAborted();

        Measurement leftMeasurement = formula.getLeftMeasurement();
        Measurement rightMeasurement = formula.getRightMeasurement();

        MeasurementSample leftSample;
        MeasurementSample rightSample;

        ComparisonEvaluationResult result;

        try {
            leftSample = measurementSampleProvider.getMeasurementSample(leftMeasurement);
        } catch (MeasurementDataNotFoundException e) {
            leftSample = measurementSampleProvider.getInvalidMeasurementSample(leftMeasurement);
        }
        try {
            rightSample = measurementSampleProvider.getMeasurementSample(rightMeasurement);
        } catch (MeasurementDataNotFoundException e) {
            rightSample = measurementSampleProvider.getInvalidMeasurementSample(rightMeasurement);
        }

        preloadDataOnSamples(leftSample, rightSample);

        outputProvider.generateMeasurementOutput(leftSample);
        outputProvider.generateMeasurementOutput(rightSample);

        ComparisonResult comparisonResultT = comparisonEvaluatorT.evaluate(formula, leftSample, rightSample);
        ComparisonResult comparisonResultMWW = comparisonEvaluatorMWW.evaluate(formula, leftSample, rightSample);
        ComparisonResult comparisonResultKS = comparisonEvaluatorKS.evaluate(formula, leftSample, rightSample);
        result = new ComparisonEvaluationResult(formula, comparisonResultT, comparisonResultMWW , comparisonResultKS, 
            leftSample, rightSample);
        outputProvider.generateComparisonOutput(result);
        releaseDataOnSamples(leftSample, rightSample);

        return result;
    }

    /**
     * Tries to pre-load data on samples.
     * 
     * @param measurementSamples
     *            The measurement samples.
     */
    private void preloadDataOnSamples(MeasurementSample... measurementSamples) {
        for (MeasurementSample measurementSample : measurementSamples) {
            MeasurementSampleDataProvider provider = measurementSample.getSampleDataProvider();
            if (provider != null) {
                try {
                    provider.acquireDataToCache();
                } catch (MeasurementDataNotFoundException e) {
                }
            }
        }
    }

    /**
     * Releases data from cache on measurement samples.
     * 
     * @param measurementSamples
     *            The measurement samples.
     */
    private void releaseDataOnSamples(MeasurementSample... measurementSamples) {
        for (MeasurementSample measurementSample : measurementSamples) {
            MeasurementSampleDataProvider provider = measurementSample.getSampleDataProvider();
            if (provider != null) {
                provider.releaseDataFromCache();
            }
        }
    }
}
