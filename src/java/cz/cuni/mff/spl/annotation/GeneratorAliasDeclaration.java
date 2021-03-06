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
 * The parsed declaration implementation for generators.
 * 
 * @author Martin Lacina
 * @author Jaroslav Kotrc
 */
public class GeneratorAliasDeclaration extends ParsedAliasDeclaration<Generator> {

    /**
     * Gets the generator.
     * 
     * @return The generator.
     */
    public Generator getGenerator() {
        return getParsedDeclaration();
    }

    /**
     * Sets the generator.
     * 
     * @param formula
     *            The new generator.
     */
    public void setGenerator(Generator formula) {
        setParsedDeclaration(formula);
    }

    /**
     * Instantiates a new generator alias declaration.
     * 
     * @param alias
     *            alias of the declared generator
     * @param generator
     *            The generator.
     * @param image
     *            The image.
     * @param errors
     *            The errors.
     * @param warnings
     *            The warnings.
     */
    public GeneratorAliasDeclaration(String alias, Generator generator, String image, Set<Problem> errors, Set<Problem> warnings) {
        super(alias, generator, image, errors, warnings);
    }

    /**
     * Instantiates a new empty generator alias declaration.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public GeneratorAliasDeclaration() {
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
     * @return The generator alias declaration.
     */
    public static GeneratorAliasDeclaration createFailedDeclaration(String image, Set<Problem> errors, Set<Problem> warnings) {
        return new GeneratorAliasDeclaration(null, null, image, errors, warnings);
    }

}
