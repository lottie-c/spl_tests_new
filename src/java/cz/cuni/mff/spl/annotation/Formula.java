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

import java.util.Map;

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.formula.context.ParserContext;

/**
 * Abstract ancestor for objects that can be used in formula tree representing
 * structure of SPL formula.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public abstract class Formula extends AbstractXmlTransformationReference {

    /**
     * Instantiates a new formula.
     */
    protected Formula() {
    }

    /**
     * Expanding formula with given set of values and mapping parameter names to
     * index in array.
     * 
     * @param context
     *            Parser context for storing measurements.
     * @param valuesArr
     *            Values of parameters.
     * @param position
     *            Mapping name of parameter to index in the value array.
     * @return New formula with expanded parameters.
     */
    public abstract Formula expand(ParserContext context, int[] valuesArr, Map<String, Integer> position);

}
