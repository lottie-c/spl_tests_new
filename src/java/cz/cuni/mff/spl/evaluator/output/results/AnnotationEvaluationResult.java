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

import java.util.LinkedHashSet;
import java.util.Set;

import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;

/**
 * The annotation evaluation representation.
 * 
 * @author Martin Lacina
 */
public class AnnotationEvaluationResult extends AbstractXmlTransformationReference {

    /** The annotation location. */
    private AnnotationLocation           annotationLocation;

    /** The formula evaluation results. */
    private Set<FormulaEvaluationResult> formulaEvaluationResults = new LinkedHashSet<>();

    /**
     * Gets the annotation location.
     * 
     * @return The annotation location.
     */
    public AnnotationLocation getAnnotationLocation() {
        return annotationLocation;
    }

    /**
     * Sets the annotation location.
     * 
     * @param annotationLocation
     *            The new annotation location.
     */
    public void setAnnotationLocation(AnnotationLocation annotationLocation) {
        this.annotationLocation = annotationLocation;
    }

    /**
     * Gets the formula evaluation results.
     * 
     * @return The formula evaluation results.
     */
    public Set<FormulaEvaluationResult> getFormulaEvaluationResults() {
        return formulaEvaluationResults;
    }

    public void addFormulaEvaluationResult(FormulaEvaluationResult formulaEvaluationResult) {
        if (this.formulaEvaluationResults == null) {
            this.formulaEvaluationResults = new LinkedHashSet<>();
        }
        this.formulaEvaluationResults.add(formulaEvaluationResult);
    }

    /**
     * Instantiates a new annotation evaluation result.
     */
    public AnnotationEvaluationResult() {

    }

    /**
     * Instantiates a new annotation evaluation result.
     * 
     * @param annotationLocation
     *            The annotation location.
     */
    public AnnotationEvaluationResult(AnnotationLocation annotationLocation) {
        this.annotationLocation = annotationLocation;
    }

    @Override
    public String toString() {
        return String.format("%s %s", annotationLocation, formulaEvaluationResults);
    }
}
