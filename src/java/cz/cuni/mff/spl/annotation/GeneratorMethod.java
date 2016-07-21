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

import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Class contains name and parameter for method in generator specification.
 * 
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class GeneratorMethod implements AnnotationToString {

    /** The name of method. */
    public String name;

    /** The optional string method parameter. */
    public String parameter;

    /**
     * Instantiates a new generator method.
     */
    public GeneratorMethod() {
    }

    /**
     * Instantiates a new generator method.
     * 
     * @param name
     *            the name
     * @param parameter
     *            the parameter
     */
    public GeneratorMethod(String name, String parameter) {
        this.name = name;
        this.parameter = parameter;
    }

    /**
     * Gets the name of method.
     * 
     * @return the name of method
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of method.
     * 
     * @param name
     *            the new name of method
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the optional string method parameter.
     * 
     * @return the optional string method parameter
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * Sets the optional string method parameter.
     * 
     * @param parameter
     *            the new optional string method parameter
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * Hash code.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
        return result;
    }

    /**
     * Equals.
     * 
     * @param obj
     *            the obj
     * @return true, if successful
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
        GeneratorMethod other = (GeneratorMethod) obj;
        return EqualsUtils.safeEquals(this.name, other.name)
                && EqualsUtils.safeEquals(this.parameter, other.parameter);
    }

    /**
     * To string.
     * 
     * @return String representation reflecting SPL grammar.
     */
    @Override
    public String toString() {
        return getDeclarationString();
    }

    @Override
    public String getDeclarationString() {
        if (parameter == null) {
            return String.format("%s()", name);
        } else {
            return String.format("%s('%s')", name, parameter);
        }
    }

    @Override
    public String getIdentificationString() {
        if (parameter == null) {
            return String.format("%s()", name);
        } else {
            return String.format("%s('%s')", name, StringUtils.encodeToBase64(parameter));
        }
    }

}
