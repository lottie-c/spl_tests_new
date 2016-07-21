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

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;

/**
 * Abstract base class with {@link EvaluationResult} implementation, which
 * returns {@code false} in all checks for type (
 * {@link #isComparisonEvaluationResult()}, {@link #isLogicalOperationResult()},
 * {@link #isFormulaResult()} ) and for all conversions (
 * {@link #asComparisonEvaluationResult()}, {@link #asLogicalOperationResult()},
 * {@link #asFormulaResult()} ) throws {@link IllegalStateException} as defined
 * in {@link EvaluationResult}.
 * 
 * @author Martin Lacina
 * 
 * @see EvaluationResult
 */
public abstract class AbstractEvaluationResult extends AbstractXmlTransformationReference implements EvaluationResult {

    @Override
    public boolean isComparisonEvaluationResult() {
        return false;
    }

    @Override
    public ComparisonEvaluationResult asComparisonEvaluationResult() {
        throw new IllegalStateException(
                this.getClass().getCanonicalName() + " is not instance of " + ComparisonEvaluationResult.class.getCanonicalName());
    }

    @Override
    public boolean isLogicalOperationResult() {
        return false;
    }

    @Override
    public LogicalOperationEvaluationResult asLogicalOperationResult() {
        throw new IllegalStateException(
                this.getClass().getCanonicalName() + " is not instance of " + LogicalOperationEvaluationResult.class.getCanonicalName());
    }

    @Override
    public boolean isFormulaResult() {
        return false;
    }

    @Override
    public FormulaEvaluationResult asFormulaResult() {
        throw new IllegalStateException(
                this.getClass().getCanonicalName() + " is not instance of " + FormulaEvaluationResult.class.getCanonicalName());
    }

}
