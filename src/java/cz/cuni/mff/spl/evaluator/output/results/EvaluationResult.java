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

import cz.cuni.mff.spl.conversion.XmlTransformationReference;

/**
 * The main interface for evaluation result for formula.
 * 
 * @author Martin Lacina
 */
public interface EvaluationResult extends XmlTransformationReference {

    /**
     * <p>
     * Gets the statistical evaluation result value, one of
     * {@link StatisticalResult#OK}, {@link StatisticalResult#FAILED} and
     * {@link StatisticalResult#NOT_COMPUTED}.
     * 
     * @return The statistical evaluation result.
     */
    StatisticalResult getStatisticalResult();

    /**
     * Checks if is comparison.
     * 
     * @return True, if is comparison.
     */
    boolean isComparisonEvaluationResult();

    /**
     * Gets this instance as comparison result. Can be used only if
     * this is instance of {@link ComparisonEvaluationResult}, i. e.
     * {@link #isComparisonEvaluationResult()} return {@code true}.
     * 
     * @return This instance as comparison result.
     * 
     * @throws IllegalStateException
     *             Thrown when this instance is not instance of
     *             {@link ComparisonEvaluationResult}.
     */
    ComparisonEvaluationResult asComparisonEvaluationResult();

    /**
     * Checks if is logical operation.
     * 
     * @return True, if is logical operation.
     */
    boolean isLogicalOperationResult();

    /**
     * Gets this instance as logical operation result. Can be used only if
     * this is instance of {@link LogicalOperationEvaluationResult}, i. e.
     * {@link #isLogicalOperationResult()} return {@code true}.
     * 
     * @return This instance as logical operation result.
     * 
     * @throws IllegalStateException
     *             Thrown when this instance is not instance of
     *             {@link LogicalOperationEvaluationResult}.
     */
    LogicalOperationEvaluationResult asLogicalOperationResult();

    /**
     * Checks if is formula result.
     * 
     * @return True, if is formula result.
     */
    boolean isFormulaResult();

    /**
     * Gets this instance as formula evaluation result. Can be used only if
     * this is instance of {@link FormulaEvaluationResult}, i. e.
     * {@link #isFormulaResult()} return {@code true}.
     * 
     * @return This instance as formula evaluation result.
     * 
     * @throws IllegalStateException
     *             Thrown when this instance is not instance of
     *             {@link FormulaEvaluationResult}.
     */
    FormulaEvaluationResult asFormulaResult();

    /**
     * Gets the result type.
     * 
     * @return The result type.
     */
    EvaluationResultType getResultType();

    /**
     * The enumeration values for evaluation result type.
     * Three types defined - for result of comparison, logical operation and
     * formula.
     */
    public enum EvaluationResultType {

        /** The result type is comparison. */
        COMPARISON,

        /** The result type is logical operation. */
        LOGICAL_OPERATION,

        /** The result type is formula. */
        FORMULA
    }

}
