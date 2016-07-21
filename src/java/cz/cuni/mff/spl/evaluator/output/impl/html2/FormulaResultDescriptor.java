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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.util.ArrayList;

import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.flatformula.FlatEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;
import cz.cuni.mff.spl.evaluator.output.results.FormulaEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The formula evaluation result descriptor for XSLT transformation.
 * 
 * @author Martin Lacina
 */
public class FormulaResultDescriptor extends OutputResultDescriptor {

    /** The flat formula evaluation result. */
    private final FlatEvaluationResult flatFormulaEvaluationResult;

    /**
     * Gets the flat formula evaluation result.
     * 
     * @return The flat formula evaluation result.
     */
    public FlatEvaluationResult getFlatFormulaEvaluationResult() {
        return flatFormulaEvaluationResult;
    }

    /** The formula declaration. */
    private final FormulaDeclaration formulaDeclaration;

    /**
     * Gets the formula declaration.
     * 
     * @return the formula declaration
     */
    public FormulaDeclaration getFormulaDeclaration() {
        return formulaDeclaration;
    }

    /** The comparison validation flags. */
    private final FormulaValidationFlags formulaValidationFlags = new FormulaValidationFlags();

    /**
     * Gets the comparison validation flags.
     * 
     * @return the comparison validation flags
     */
    public FormulaValidationFlags getFormulaValidationFlags() {
        return formulaValidationFlags;
    }

    /**
     * Instantiates a new measurement result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param formulaEvaluationResult
     *            The formula evaluation result.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public FormulaResultDescriptor(Info info, ConfigurationBundle configuration, FormulaEvaluationResult formulaEvaluationResult,
            StatisticValueChecker checker, BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks, AnnotationValidationFlags globalAliasesSummary) {
        super(info, configuration, outputLinks, globalAliasesSummary);

        this.flatFormulaEvaluationResult = formulaEvaluationResult.getFlatFormula();

        this.formulaDeclaration = formulaEvaluationResult.getFormulaDeclaration();

        this.formulaValidationFlags.setFlags(formulaEvaluationResult, checker);
    }

    /**
     * The formula validation flags.
     * 
     * @author Martin Lacina
     */
    public static class FormulaValidationFlags {

        /**
         * Instantiates a new formula validation flags.
         */
        public FormulaValidationFlags() {

        }

        /**
         * Sets the flags.
         * 
         * @param result
         *            The formula evaluation result.
         * @param checker
         *            The checker.
         */
        public void setFlags(FormulaEvaluationResult result, StatisticValueChecker checker) {

        }

    }
}
