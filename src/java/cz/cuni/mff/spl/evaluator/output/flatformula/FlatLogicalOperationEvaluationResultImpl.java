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

import java.util.ArrayList;
import java.util.List;

import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.evaluator.output.results.LogicalOperationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.StatisticalResult;

/**
 * Flat logical operation evaluation result allows representing SPL formula node
 * with operators with more than two children, i. e. aggregates subtree of SPL
 * formula with one operator type to one node (this applies to nodes
 * representing {@link Operator#AND} and {@link Operator#OR} operators).
 * 
 * @author Martin Lacina
 * 
 * @see FlatEvaluationResult
 */
public class FlatLogicalOperationEvaluationResultImpl extends AbstractFlatEvaluationResultImpl implements FlatLogicalOperationEvaluationResult {

    /**
     * The logical operation evaluation result root as flat logical operation
     * result can represent whole subtree of original evaluation result.
     */
    private final LogicalOperationEvaluationResult logicalOperationEvaluationResultRoot;

    /** The operands. */
    public final List<FlatEvaluationResult>        operands = new ArrayList<>();

    /**
     * Instantiates a new flat logical operation evaluation result impl.
     * <p>
     * For XML transformation only.
     */
    @Deprecated
    public FlatLogicalOperationEvaluationResultImpl() {
        logicalOperationEvaluationResultRoot = null;
    }

    /**
     * Instantiates a new flat logical operation evaluation result.
     * 
     * @param result
     *            The logical operation result to represent.
     */
    public FlatLogicalOperationEvaluationResultImpl(LogicalOperationEvaluationResult result) {
        super(result.getEvaluatedLogicalOperation());
        logicalOperationEvaluationResultRoot = result;
    }

    /**
     * Checks if is logical operation evaluation result.
     * 
     * @return True, if is logical operation evaluation result.
     */
    @Override
    public boolean isFlatLogicalOperationEvaluationResult() {
        return true;
    }

    @Override
    public FlatLogicalOperationEvaluationResult asFlatLogicalOperationEvaluationResult() {
        return this;
    }

    /**
     * Gets the logical operation.
     * 
     * @return The logical operation.
     */
    @Override
    public Expression getLogicalOperation() {
        return logicalOperationEvaluationResultRoot.getEvaluatedLogicalOperation();
    }

    @Override
    public List<FlatEvaluationResult> getOperands() {
        return this.operands;
    }

    @Override
    public Operator getLogicalOperator() {
        return logicalOperationEvaluationResultRoot.getLogicalOperator();
    }

    @Override
    public LogicalOperationEvaluationResult getLogicalOperationEvaluationResultRoot() {
        return logicalOperationEvaluationResultRoot;
    }

    @Override
    public StatisticalResult getStatisticalResult() {
        return logicalOperationEvaluationResultRoot.getStatisticalResult();
    }
}
