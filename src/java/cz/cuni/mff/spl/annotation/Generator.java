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

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.parser.Parser;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Represents single generator used in measurement as input for measured method.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Generator extends AbstractXmlTransformationReference implements AnnotationToString {

    /** Project and revision information. */
    private Revision        revision;
    /** The fully qualified name of class. */
    private String          path;
    /** The optional parameter of class constructor. */
    private String          parameter;
    /** The optional generator method. */
    private GeneratorMethod method;

    /**
     * Create generator for given name. It can be static class or alias.
     * 
     * @param name
     *            the name
     * @param context
     *            the context
     * @return the generator
     */
    public static Generator createGenerator(String name, ParserContext context) {
        return context.getGenerator(name);
    }

    /**
     * Create fully specified generator.
     * 
     * @param projectName
     *            the project name
     * @param revisionName
     *            the revision name
     * @param path
     *            the path
     * @param parameter
     *            the parameter
     * @param method
     *            the method
     * @param context
     *            the context
     * @return the generator
     */
    public static Generator createGenerator(String projectName, String revisionName, String path, String parameter, GeneratorMethod method,
            ParserContext context) {
        Revision revision = context.findRevision(projectName, revisionName);
        return new Generator(revision, path, parameter, method);
    }

    /**
     * Instantiates a new generator.
     * <p>
     * Just for XML conversion and tests
     */
    @Deprecated
    public Generator() {
    }

    /**
     * Instantiates a new generator.
     * 
     * @param revision
     *            The revision.
     * @param path
     *            The path.
     * @param parameter
     *            The parameter.
     * @param method
     *            The method.
     */
    public Generator(Revision revision, String path, String parameter, GeneratorMethod method) {
        this.revision = revision;
        this.path = path;
        this.parameter = parameter;
        this.method = method;
    }

    /**
     * Gets the project and revision information.
     * 
     * @return the project and revision information
     */
    public Revision getRevision() {
        return revision;
    }

    /**
     * Sets the project and revision information.
     * 
     * @param revision
     *            the new project and revision information
     */
    public void setRevision(Revision revision) {
        this.revision = revision;
    }

    /**
     * Gets the fully qualified name of class.
     * 
     * @return the fully qualified name of class
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the fully qualified name of class.
     * 
     * @param path
     *            the new fully qualified name of class
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the optional parameter of class constructor.
     * 
     * @return the optional parameter of class constructor
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * Sets the optional parameter of class constructor.
     * 
     * @param parameter
     *            the new optional parameter of class constructor
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * Gets the optional generator method.
     * 
     * @return the optional generator method
     */
    public GeneratorMethod getMethod() {
        return method;
    }

    /**
     * Sets the optional generator method.
     * 
     * @param method
     *            the new optional generator method
     */
    public void setMethod(GeneratorMethod method) {
        this.method = method;
    }

    /**
     * ID has no influence to hashCode.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        return result;
    }

    /**
     * ID has no influence to equals.
     * 
     * @param obj
     *            the obj
     * @return True, if generators match.
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
        Generator other = (Generator) obj;
        return EqualsUtils.safeEquals(this.method, other.method)
                && EqualsUtils.safeEquals(this.parameter, other.parameter)
                && EqualsUtils.safeEquals(this.path, other.path)
                && EqualsUtils.safeEquals(this.revision, other.revision);
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

    /**
     * {@inheritDoc}
     * 
     * To get full alias declaration use {@link Generator#getAlias()} and this
     * method as parameters for
     * {@link Parser#mergeAliasNameAndDefinition(String, String)}.
     * 
     * @see Parser#mergeAliasNameAndDefinition(String, String)
     */
    @Override
    public String getDeclarationString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(revision.getDeclarationString());
        buffer.append(":");
        buffer.append(path);

        if (parameter != null) {
            buffer.append(String.format("('%s')", parameter));
        }

        if (method != null) {
            buffer.append("#");
            buffer.append(method.getDeclarationString());
        }

        return buffer.toString();
    }

    @Override
    public String getIdentificationString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(revision.getIdentificationString());
        buffer.append(":");
        buffer.append(path);

        if (parameter != null) {
            buffer.append(String.format("('%s')", StringUtils.encodeToBase64(parameter)));
        }

        if (method != null) {
            buffer.append("#");
            buffer.append(method.getIdentificationString());
        }

        return buffer.toString();
    }

    /**
     * Gets the short declaration string without package and project
     * declarations.
     * Note that all white-spaces or formatting which was in original source is
     * not available.
     * 
     * @return The declaration string.
     *         {@link Parser#mergeAliasNameAndDefinition(String, String)}.
     */
    public String getShortDeclarationString() {
        StringBuilder buffer = new StringBuilder();

        int index = path.lastIndexOf('.');
        if (index <= 0) {
            buffer.append(path);
        } else {
            buffer.append(path.substring(index + 1));
        }

        if (parameter != null) {
            buffer.append(String.format("('%s')", parameter));
        }

        if (method != null) {
            buffer.append("#");
            buffer.append(method);
        }

        return buffer.toString();
    }
}
