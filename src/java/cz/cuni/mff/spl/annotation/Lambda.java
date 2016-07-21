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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Class for storing information about one lambda expression. Expression is
 * multiplication of constants and variables.
 * 
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Lambda {

    private final List<Double> constants  = new ArrayList<Double>();
    private final List<String> parameters = new ArrayList<String>();

    public void add(double d) {
        constants.add(d);
    }

    public void add(String str) {
        parameters.add(str);
    }

    public List<Double> getConstants() {
        return constants;
    }

    public List<String> getParameters() {
        return parameters;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constants == null) ? 0 : constants.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
        Lambda other = (Lambda) obj;

        return EqualsUtils.safeEquals(this.constants, other.constants)
                && EqualsUtils.safeEquals(this.parameters, other.parameters);
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();

        if (constants.size() > 0) {
            buffer.append(StringUtils.createOneString(constants, " * ", "", " * "));
        }

        buffer.append("x");

        return buffer.toString();
    }

    /**
     * Expanding variables created by parser with concrete values used in
     * comparison and returning expanded Lambda.
     * 
     * @param context
     *            Parser context for error handling
     * @param valuesArr
     *            Array of concrete variable values
     * @param position
     *            Mapping variable name to index of variable value in
     *            valuesArr
     * @return Expanded lambda
     */
    public Lambda expand(ParserContext context, int[] valuesArr, Map<String, Integer> position) {
        Lambda lambda = new Lambda();
        for (Double value : constants) {
            lambda.add(value);
        }

        for (String name : parameters) {
            Integer positionIdx = position.get(name);
            double value = Double.NaN;
            if (positionIdx == null) {
                context.addError(new Problem("Variable " + name + " was not declared"));
            } else {
                value = valuesArr[positionIdx];
            }
            lambda.add(value);
        }
        return lambda;
    }

    /**
     * Gets the clone of current instance.
     * 
     * @return The clone of current instance.
     */
    public Lambda getClone() {
        Lambda result = new Lambda();
        result.constants.addAll(constants);
        result.parameters.addAll(parameters);
        return result;
    }
}
