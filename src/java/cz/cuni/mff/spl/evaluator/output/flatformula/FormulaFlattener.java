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

package cz.cuni.mff.spl.evaluator.output.flatformula;

import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.EvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.LogicalOperationEvaluationResult;

/**
 * This class provided method to make formula binary tree more flat if possible.
 * 
 * @author Martin Lacina
 * 
 */
public abstract class FormulaFlattener {
    /**
     * Makes evaluation result flat.
     * 
     * @param evaluationResult
     *            The evaluation result.
     * @return The flat evaluation result.
     */
    public static FlatEvaluationResult makeFlat(EvaluationResult evaluationResult) {
        if (evaluationResult.isFormulaResult()) {
            return makeFlat(evaluationResult.asFormulaResult().getFormulaEvaluationResultRoot());
        }

        if (evaluationResult.isComparisonEvaluationResult()) {
            ComparisonEvaluationResult comparisonResult = evaluationResult.asComparisonEvaluationResult();
            return new FlatComparisonEvaluationResultImpl(comparisonResult);
        }

        if (evaluationResult.isLogicalOperationResult()) {

            LogicalOperationEvaluationResult evalResult = evaluationResult.asLogicalOperationResult();
            FlatLogicalOperationEvaluationResult result = new FlatLogicalOperationEvaluationResultImpl(evalResult);

            fillFlatEvaluationResult(result, evalResult);
            return result;
        }

        throw new IllegalStateException("Unexpected type of EvaluationResult");
    }

    /**
     * Fill flat evaluation result.
     * 
     * @param result
     *            The result.
     * @param evaluationResult
     *            The evaluation result.
     */
    private static void fillFlatEvaluationResult(FlatLogicalOperationEvaluationResult result, EvaluationResult evaluationResult) {
        if (evaluationResult.isComparisonEvaluationResult()) {
            ComparisonEvaluationResult comparisonResult = evaluationResult.asComparisonEvaluationResult();
            result.getOperands().add(new FlatComparisonEvaluationResultImpl(comparisonResult));
            return;
        }

        if (!evaluationResult.isLogicalOperationResult()) {
            throw new IllegalStateException("Unexpected type of EvaluationResult");
        }

        LogicalOperationEvaluationResult evalResult = evaluationResult.asLogicalOperationResult();

        if (evalResult.getLogicalOperator() == Operator.IMPL) {
            // implication can not be aggregated
            FlatLogicalOperationEvaluationResult newFlatEvaluationResult = new FlatLogicalOperationEvaluationResultImpl(evalResult);
            result.getOperands().add(newFlatEvaluationResult);
            fillFlatEvaluationResult(newFlatEvaluationResult, evalResult.getLeftOperandResult());
            fillFlatEvaluationResult(newFlatEvaluationResult, evalResult.getRightOperandResult());
        } else if (evalResult.getLogicalOperator() == result.getLogicalOperator()) {
            // we can aggregate
            EvaluationResult leftResult = evalResult.getLeftOperandResult();
            EvaluationResult rightResult = evalResult.getRightOperandResult();
            fillFlatEvaluationResult(result, leftResult);
            fillFlatEvaluationResult(result, rightResult);
        } else {
            // operators differ, we need to make new item to fill
            FlatLogicalOperationEvaluationResult newFlatEvaluationResult = new FlatLogicalOperationEvaluationResultImpl(evalResult);
            result.getOperands().add(newFlatEvaluationResult);
            fillFlatEvaluationResult(newFlatEvaluationResult, evalResult);
        }
    }
}
