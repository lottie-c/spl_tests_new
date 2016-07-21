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
 * The parsed declaration implementation for methods.
 * 
 * @author Martin Lacina
 * @author Jaroslav Kotrc
 */
public class MethodAliasDeclaration extends ParsedAliasDeclaration<Method> {

    /**
     * Gets the method.
     * 
     * @return The method.
     */
    public Method getMethod() {
        return getParsedDeclaration();
    }

    /**
     * Sets the method.
     * 
     * @param method
     *            The new method.
     */
    public void setMethod(Method method) {
        setParsedDeclaration(method);
    }

    /**
     * Instantiates a new method alias declaration.
     * 
     * @param alias
     *            method alias.
     * @param method
     *            method object.
     * @param image
     *            string image of method.
     * @param errors
     *            errors occurred during method parsing.
     * @param warnings
     *            warnings occurred during method parsing.
     */
    public MethodAliasDeclaration(String alias, Method method, String image, Set<Problem> errors, Set<Problem> warnings) {
        super(alias, method, image, errors, warnings);
    }

    /**
     * Instantiates a new empty method alias declaration.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public MethodAliasDeclaration() {
        super();
    }

    /**
     * Creates the failed declaration.
     * 
     * @param image
     *            The image.
     * @param errors
     *            The errors.
     * @param warnings
     *            The warnings.
     * @return The method alias declaration.
     */
    public static MethodAliasDeclaration createFailedDeclaration(String image, Set<Problem> errors, Set<Problem> warnings) {
        return new MethodAliasDeclaration(null, null, image, errors, warnings);
    }
}
