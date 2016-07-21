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
package cz.cuni.mff.spl.annotation;

import java.util.Set;

import cz.cuni.mff.spl.formula.context.ParserContext.Problem;

/**
 * The parsed declaration implementation for formulas.
 * 
 * @author Martin Lacina
 */
public class FormulaDeclaration extends ParsedDeclaration<Formula> {

    /** The annotation location. */
    private AnnotationLocation annotationLocation;

    /**
     * Gets the formula.
     * 
     * @return The formula.
     */
    public Formula getFormula() {
        return getParsedDeclaration();
    }

    /**
     * Sets the formula.
     * 
     * @param formula
     *            The new formula.
     */
    public void setFormula(Formula formula) {
        setParsedDeclaration(formula);
    }

    /**
     * Instantiates a new formula declaration.
     * 
     * @param annotationLocation
     *            The annotation location.
     * @param formula
     *            The formula.
     * @param image
     *            The image. Can be {@link null}.
     * @param errors
     *            The errors. Can be {@link null}.
     * @param warnings
     *            The warnings. Can be {@link null}.
     */
    public FormulaDeclaration(AnnotationLocation annotationLocation, Formula formula, String image, Set<Problem> errors, Set<Problem> warnings) {
        super(formula, image, errors, warnings);
        this.annotationLocation = annotationLocation;
    }

    /**
     * Instantiates a new empty formula declaration.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public FormulaDeclaration() {
        super();
    }

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
     * For XML transformation only.
     * 
     * @param annotationLocation
     *            The new annotation location.
     */
    @Deprecated
    public void setAnnotationLocation(AnnotationLocation annotationLocation) {
        this.annotationLocation = annotationLocation;
    }

    /**
     * Creates the failed declaration.
     * 
     * @param annotationLocation
     *            The annotation location.
     * @param image
     *            The image.
     * @param errors
     *            The errors.
     * @param warnings
     *            The warnings.
     * @return The formula declaration.
     */
    public static FormulaDeclaration createFailedDeclaration(AnnotationLocation annotationLocation, String image, Set<Problem> errors, Set<Problem> warnings) {
        return new FormulaDeclaration(annotationLocation, null, image, errors, warnings);
    }

}
