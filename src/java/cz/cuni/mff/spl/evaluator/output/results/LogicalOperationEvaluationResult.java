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
package cz.cuni.mff.spl.evaluator.output.results;

import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Operator;

/**
 * Result of logical operation in SPL formula.
 * 
 * @author Martin Lacina
 * 
 */
public class LogicalOperationEvaluationResult extends AbstractEvaluationResult {

    /**
     * The logical operator.
     * This field is meant to be read only.
     */
    private Expression       evaluatedExpression;

    /**
     * The left operand result.
     * This field is meant to be read only.
     */
    private EvaluationResult leftOperandResult;

    /**
     * The right operand result.
     * This field is meant to be read only.
     */
    private EvaluationResult rightOperandResult;

    /**
     * Instantiates a new logical operation result implementation.
     * 
     * @param evaluatedExpression
     *            The evaluated expression.
     * @param leftOperandResult
     *            The left operand result.
     * @param rightOperandResult
     *            The right operand result.
     */
    public LogicalOperationEvaluationResult(Expression evaluatedExpression, EvaluationResult leftOperandResult, EvaluationResult rightOperandResult) {
        this.evaluatedExpression = evaluatedExpression;
        this.leftOperandResult = leftOperandResult;
        this.rightOperandResult = rightOperandResult;
    }

    @Override
    public boolean isLogicalOperationResult() {
        return true;
    }

    @Override
    public LogicalOperationEvaluationResult asLogicalOperationResult() {
        return this;
    }

    @Override
    public EvaluationResultType getResultType() {
        return EvaluationResultType.LOGICAL_OPERATION;
    }

    /**
     * Gets the left operand result.
     * 
     * @return The left operand result.
     */
    public EvaluationResult getLeftOperandResult() {
        return this.leftOperandResult;
    }

    /**
     * Gets the right operand result.
     * 
     * @return The right operand result.
     */
    public EvaluationResult getRightOperandResult() {
        return this.rightOperandResult;
    }

    /**
     * Gets the logical operator.
     * 
     * @return The logical operator.
     */
    public Operator getLogicalOperator() {
        return this.evaluatedExpression.getOperator();
    }

    /**
     * Gets the evaluated logical operation.
     * 
     * @return The evaluated logical operation.
     */
    public Expression getEvaluatedLogicalOperation() {
        return evaluatedExpression;
    }

    @Override
    public StatisticalResult getStatisticalResult() {
        StatisticalResult leftOperand = leftOperandResult.getStatisticalResult();

        StatisticalResult rightOperand = rightOperandResult.getStatisticalResult();

        return StatisticalResult.combine(getLogicalOperator(), leftOperand, rightOperand);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((evaluatedExpression == null) ? 0 : evaluatedExpression.hashCode());
        result = prime * result + ((leftOperandResult == null) ? 0 : leftOperandResult.hashCode());
        result = prime * result + ((rightOperandResult == null) ? 0 : rightOperandResult.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof LogicalOperationEvaluationResult) {
            LogicalOperationEvaluationResult otherResult = (LogicalOperationEvaluationResult) other;

            return otherResult.isLogicalOperationResult()
                    && otherResult.getLogicalOperator().equals(evaluatedExpression.getOperator())
                    && otherResult.getLeftOperandResult().equals(leftOperandResult)
                    && otherResult.getRightOperandResult().equals(rightOperandResult);
        } else {
            return false;
        }
    }

    /**
     * Instantiates a new logical operation result instance.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public LogicalOperationEvaluationResult() {
    }

    /**
     * Sets the evaluated expression.
     * 
     * For XML transformation only.
     * 
     * @param evaluatedExpression
     *            The new evaluated expression operator.
     */
    @Deprecated
    public void setEvaluatedLogicalOperation(Expression evaluatedExpression) {
        this.evaluatedExpression = evaluatedExpression;
    }

    /**
     * Sets the left operand result.
     * 
     * For XML transformation only.
     * 
     * @param leftOperandResult
     *            The new left operand result.
     */
    @Deprecated
    public void setLeftOperandResult(EvaluationResult leftOperandResult) {
        this.leftOperandResult = leftOperandResult;
    }

    /**
     * Sets the right operand result.
     * 
     * For XML transformation only.
     * 
     * @param rightOperandResult
     *            The new right operand result.
     */
    @Deprecated
    public void setRightOperandResult(EvaluationResult rightOperandResult) {
        this.rightOperandResult = rightOperandResult;
    }

}
