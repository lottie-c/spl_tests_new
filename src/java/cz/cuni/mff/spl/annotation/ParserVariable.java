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

import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Contains list of variables which have to be expanded for final structure
 * after parsing.
 * 
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class ParserVariable extends Variable {
    private final List<String> variables = new ArrayList<String>();

    /**
     * Creates ParserVariable and adds passed variable to the beginning of the
     * list of variables.
     * 
     * @param variable
     *            first variable to add to the list of variables
     */
    public ParserVariable(String variable) {
        variables.add(variable);
    }

    /** Creates ParserVariable with empty list of variables. */
    public ParserVariable() {
    }

    @Override
    public List<Integer> getVariables() {
        throw new IllegalStateException("Parser variable has no value");
    }

    public List<String> getVariableNames() {
        return variables;
    }

    /**
     * Add variable with given name.
     * 
     * @param variable
     *            Name of the variable
     */
    public void addVariable(String variable) {
        variables.add(variable);
    }

    @Override
    public String toString() {
        return getVariables().toString();
    }

    @Override
    public String getDeclarationString() {
        return StringUtils.createOneString(variables, ", ", "(", ")");
    }

    @Override
    public String getIdentificationString() {
        List<String> base64variables = new ArrayList<>(variables.size());
        for (String variable : variables) {
            base64variables.add(StringUtils.encodeToBase64(variable));
        }
        return StringUtils.createOneString(base64variables, ", ", "(", ")");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
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
        ParserVariable other = (ParserVariable) obj;
        return EqualsUtils.safeEquals(this.variables, other.variables);
    }

}
