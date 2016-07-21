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

import java.util.LinkedHashSet;
import java.util.Set;

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * Abstract class for common declaration information.
 * 
 * @param <T>
 *            The type of parsed declaration
 * @author Martin Lacina
 */
public abstract class ParsedDeclaration<T> extends AbstractXmlTransformationReference {

    /** The declaration image which was passed to parser. */
    private String             image;

    /**
     * The errors found during parsing.
     * 
     * This value can be {@code null} when no errors found.
     * 
     * Use {@link #hasParserErrors()} to check if errors are present.
     */
    private final Set<Problem> parserErrors   = new LinkedHashSet<>();

    /**
     * The warnings found during parsing.
     * 
     * This value can be {@code null} when no warnings found.
     * 
     * Use {@link #hasParserWarnings()} to check if warnings are present.
     */
    private final Set<Problem> parserWarnings = new LinkedHashSet<>();

    /** The parsed declaration. */
    private T                  parsedDeclaration;

    /**
     * Gets the declaration image which was passed to parser.
     * 
     * @return The declaration image which was passed to parser.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the declaration image which was passed to parser.
     * 
     * @param image
     *            The new declaration image which was passed to parser.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the parser errors.
     * 
     * @return The parser errors.
     */
    public Set<Problem> getParserErrors() {
        return parserErrors;
    }

    /**
     * Sets the parser errors.
     * 
     * @param errors
     *            The new parser errors. Can be {@code null}.
     */
    public void setParserErrors(Set<Problem> errors) {
        this.parserErrors.clear();
        if (errors != null) {
            this.parserErrors.addAll(errors);
        }
    }

    /**
     * Adds the parser errors.
     * 
     * @param errors
     *            The errors. Can be {@code null}.
     */
    public void addParserErrors(Set<Problem> errors) {
        if (errors != null) {
            this.parserErrors.addAll(errors);
        }
    }

    /**
     * Adds the parser error.
     * 
     * @param error
     *            The error. Can be {@code null}.
     */
    public void addParserError(Problem error) {
        if (error != null) {
            this.parserErrors.add(error);
        }
    }

    /**
     * Gets the parser warnings.
     * 
     * @return The parser warnings.
     */
    public Set<Problem> getParserWarnings() {
        return parserWarnings;
    }

    /**
     * Sets the parser warnings.
     * 
     * @param warnings
     *            The new parser warnings. Can be {@code null}.
     */
    public void setParserWarnings(Set<Problem> warnings) {
        this.parserWarnings.clear();
        if (warnings != null) {
            this.parserWarnings.addAll(warnings);
        }
    }

    /**
     * Adds the parser warnings.
     * 
     * @param warnings
     *            The warnings. Can be {@code null}.
     */
    public void addParserWarnings(Set<Problem> warnings) {
        if (warnings != null) {
            this.parserWarnings.addAll(warnings);
        }
    }

    /**
     * Adds the parser warning.
     * 
     * @param warning
     *            The warning. Can be {@code null}.
     */
    public void addParserWarning(Problem warning) {
        if (warning != null) {
            this.parserWarnings.add(warning);
        }
    }

    /**
     * Checks if parser errors are present.
     * 
     * When returns {@code true}, than call to {@link #getParserErrors()} does
     * not return {@code null} but valid set of parser errors.
     * 
     * @return True, if successful.
     */
    public boolean hasParserErrors() {
        return this.parserErrors != null && !this.parserErrors.isEmpty();
    }

    /**
     * Checks if parser warnings are present.
     * 
     * When returns {@code true}, than call to {@link #getParserWarnings()} does
     * not return {@code null} but valid set of parser warnings.
     * 
     * @return True, when parser warnings are present.
     */
    public boolean hasParserWarnings() {
        return this.parserWarnings != null && !this.parserWarnings.isEmpty();
    }

    /**
     * Checks if declaration has been parsed successfully.
     * 
     * @return True, if successful.
     */
    public boolean hasDeclarationBeenParsedSuccessfully() {
        return parsedDeclaration != null && !hasParserErrors();
    }

    /**
     * Gets the parsed declaration.
     * 
     * @return The parsed declaration.
     */
    public T getParsedDeclaration() {
        return parsedDeclaration;
    }

    /**
     * Sets the parsed declaration.
     * 
     * @param parsedDeclaration
     *            The new parsed declaration.
     */
    protected void setParsedDeclaration(T parsedDeclaration) {
        this.parsedDeclaration = parsedDeclaration;
    }

    /**
     * Instantiates a new parsed declaration.
     * 
     * @param parsedDeclaration
     *            The parsed declaration.
     * @param image
     *            The image.
     * @param errors
     *            The errors.
     * @param warnings
     *            The warnings.
     */
    protected ParsedDeclaration(T parsedDeclaration, String image, Set<Problem> errors, Set<Problem> warnings) {
        this.parsedDeclaration = parsedDeclaration;
        this.image = image;
        setParserErrors(errors);
        setParserWarnings(warnings);
    }

    /**
     * Instantiates a new empty parsed declaration.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public ParsedDeclaration() {

    }

    /**
     * Hash code.
     * 
     * @return The int.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parsedDeclaration == null) ? 0 : parsedDeclaration.hashCode());
        result = prime * result + ((image == null) ? 0 : image.hashCode());
        result = prime * result + ((parserErrors == null) ? 0 : parserErrors.hashCode());
        result = prime * result + ((parserWarnings == null) ? 0 : parserWarnings.hashCode());
        return result;
    }

    /**
     * Equals.
     * 
     * @param obj
     *            The obj.
     * @return True, if successful.
     */
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
        @SuppressWarnings("unchecked")
        ParsedDeclaration<T> other = (ParsedDeclaration<T>) obj;
        return EqualsUtils.safeEquals(this.parsedDeclaration, other.parsedDeclaration)
                && EqualsUtils.safeEquals(this.image, other.image)
                && EqualsUtils.safeEquals(this.parserErrors, other.parserErrors)
                && EqualsUtils.safeEquals(this.parserWarnings, other.parserWarnings);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [image=" + image + ", parserErrors=" + parserErrors + ", parserWarnings=" + parserWarnings
                + ", parsedDeclaration="
                + parsedDeclaration + "]";
    }
}
