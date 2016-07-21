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

import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.Operator;

/**
 * Flat evaluation result allows representing SPL formula (which is binary tree)
 * as tree, where every inner node can have more than two children (this applies
 * to nodes representing {@link Operator#AND} and {@link Operator#OR}
 * operators).
 * 
 * @author Martin Lacina
 */
public abstract class AbstractFlatEvaluationResultImpl implements FlatEvaluationResult {

    /** The formula root. */
    private final Formula formulaRoot;

    /**
     * Gets the formula root.
     * 
     * @return The formula root.
     */
    @Override
    public Formula getFormulaRoot() {
        return formulaRoot;
    }

    /**
     * Instantiates a new abstract flat evaluation result implementation.
     * <p>
     * For XML transformation only.
     */
    @Deprecated
    public AbstractFlatEvaluationResultImpl() {
        formulaRoot = null;
    }

    /**
     * Instantiates a new flat evaluation result.
     * 
     * @param formulaRoot
     *            The formula root.
     * @param logicalOperator
     *            The logical operator.
     * @param isSatisfied
     *            The is satisfied.
     */
    public AbstractFlatEvaluationResultImpl(Formula formulaRoot) {
        this.formulaRoot = formulaRoot;
    }

    @Override
    public boolean isFlatComparisonEvaluationResult() {
        return false;
    }

    @Override
    public FlatComparisonEvaluationResult asFlatComparisonEvaluationResult() {
        throw new IllegalStateException(
                this.getClass().getCanonicalName() + " is not instance of " + FlatComparisonEvaluationResult.class.getCanonicalName());
    }

    @Override
    public boolean isFlatLogicalOperationEvaluationResult() {
        return false;
    }

    @Override
    public FlatLogicalOperationEvaluationResult asFlatLogicalOperationEvaluationResult() {
        throw new IllegalStateException(
                this.getClass().getCanonicalName() + " is not instance of " + FlatLogicalOperationEvaluationResult.class.getCanonicalName());
    }

}
