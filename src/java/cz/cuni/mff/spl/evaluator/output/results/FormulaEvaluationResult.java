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

import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.evaluator.output.flatformula.FlatEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.flatformula.FormulaFlattener;

/**
 * Result of comparison evaluation in SPL formula.
 * 
 * @author Martin Lacina
 * 
 */
public class FormulaEvaluationResult extends AbstractEvaluationResult {

    /** The formula. */
    private FormulaDeclaration   formulaDeclaration;

    /** The flat formula. */
    private FlatEvaluationResult flatFormula;

    /** The comparison result. */
    private EvaluationResult     formulaEvaluationResultRoot;

    /**
     * Instantiates a new comparison evaluation result implementation.
     * 
     * @param comparison
     *            The comparison.
     * @param comparisonResult
     *            The comparison result.
     */
    public FormulaEvaluationResult(FormulaDeclaration formula, EvaluationResult formulaEaluationResultRoot) {
        this.formulaDeclaration = formula;
        this.formulaEvaluationResultRoot = formulaEaluationResultRoot;
    }

    public FormulaDeclaration getFormulaDeclaration() {
        return formulaDeclaration;
    }

    public EvaluationResult getFormulaEvaluationResultRoot() {
        return formulaEvaluationResultRoot;
    }

    @Override
    public StatisticalResult getStatisticalResult() {
        return formulaEvaluationResultRoot.getStatisticalResult();
    }

    /**
     * Does nothing as satisfied value is computed dynamically.
     * <p>
     * Method to satisfy Castor XML Mapping.
     * 
     * @param value
     *            Not used.
     */
    @Deprecated
    public void setSatisfied(boolean value) {

    }

    @Override
    public boolean isFormulaResult() {
        return true;
    }

    @Override
    public FormulaEvaluationResult asFormulaResult() {
        return this;
    }

    @Override
    public EvaluationResultType getResultType() {
        return EvaluationResultType.FORMULA;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formulaDeclaration == null) ? 0 : formulaDeclaration.hashCode());
        result = prime * result + ((formulaEvaluationResultRoot == null) ? 0 : formulaEvaluationResultRoot.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FormulaEvaluationResult other = (FormulaEvaluationResult) obj;
        if (formulaDeclaration == null) {
            if (other.formulaDeclaration != null) {
                return false;
            }
        } else if (!formulaDeclaration.equals(other.formulaDeclaration)) {
            return false;
        }
        if (formulaEvaluationResultRoot == null) {
            if (other.formulaEvaluationResultRoot != null) {
                return false;
            }
        } else if (!formulaEvaluationResultRoot.equals(other.formulaEvaluationResultRoot)) {
            return false;
        }
        return true;
    }

    /**
     * Instantiates a new comparison evaluation result instance.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public FormulaEvaluationResult() {
    }

    /**
     * Sets the formula.
     * 
     * For XML transformation only.
     * 
     * @param formula
     *            The new formula.
     */
    @Deprecated
    public void setFormulaDeclaration(FormulaDeclaration formula) {
        this.formulaDeclaration = formula;
    }

    /**
     * Sets the formula evaluation result root.
     * 
     * For XML transformation only.
     * 
     * @param formulaEvaluationResultRoot
     *            The new formula evaluation result root result.
     */
    @Deprecated
    public void setFormulaEvaluationResultRoot(EvaluationResult formulaEvaluationResultRoot) {
        this.formulaEvaluationResultRoot = formulaEvaluationResultRoot;
    }

    /**
     * Gets the flat formula.
     * 
     * @return The flat formula.
     */
    public FlatEvaluationResult getFlatFormula() {
        if (this.flatFormula == null) {
            this.flatFormula = FormulaFlattener.makeFlat(this);
        }
        return this.flatFormula;
    }
}
