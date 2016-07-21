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

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.parser.Parser;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Represents method that will be measured.
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 */
public class Method extends AbstractXmlTransformationReference implements AnnotationToString {

    /** Project and revision information. */
    private Revision revision;
    /** The fully qualified name of class. */
    private String   path;
    /** The optional parameter of class constructor. */
    private String   parameter;
    /** The method name. */
    private String   name;

    /**
     * Type of method declaration. Method can be declared without parameters,
     * then proper method will be selected by generator return type or if method
     * was declared with parameter types then method with specified parameter
     * types will be selected.
     * 
     */
    public enum DeclarationType {
        /**
         * Method was declared with parameters types. Types are stored in
         * <code>parameterTypes</code>
         */
        WITH_PARAMETERS,
        /** Method was declared without specified parameter types. */
        WITHOUT_PARAMETERS
    }

    /**
     * Type of method declaration. If method was declared with parameters then
     * <code>parameterTypes</code> contains types of method parameters.
     */
    private DeclarationType         declarated     = null;

    /**
     * List of types of method parameters. This field can not be null even if
     * method was declared with zero parameters - then this field should be list
     * with size 0.
     */
    private final ArrayList<String> parameterTypes = new ArrayList<String>();

    /**
     * Add given parameter type to the list of method parameter types.
     * 
     * @param type
     *            type of method parameter to add
     */
    public void addParameterType(String type) {
        if (declarated == DeclarationType.WITHOUT_PARAMETERS) {
            throw new IllegalStateException("Cannot add parameter to the method declared as without parameters");
        }
        parameterTypes.add(type);
    }

    /**
     * Gets list of method parameter types
     * 
     * @return list of method parameter types
     */
    public ArrayList<String> getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Sets list of method parameter types. It will not just set passed list,
     * instead it will clear list in this instance and add all elements of
     * passed list.
     * 
     * @param parameterTypes
     *            list of method parameter types to set
     */
    public void setParameterTypes(ArrayList<String> parameterTypes) {
        this.parameterTypes.clear();
        if (parameterTypes != null) {
            this.parameterTypes.addAll(parameterTypes);
        }
    }

    /**
     * Get type of method declaration.
     * 
     * @return type of method declaration
     */
    public DeclarationType getDeclarated() {
        return declarated;
    }

    /**
     * Set type of method declaration. Just for XML conversion, others should
     * set this in factory method.
     * 
     * @param declarated
     *            new type of method declaration
     */
    @Deprecated
    public void setDeclarated(DeclarationType declarated) {
        this.declarated = declarated;
    }

    /**
     * Create method from given name as alias.
     * 
     * @param name
     *            the name
     * @param context
     *            the context
     * @return the method
     */
    public static Method createMethod(String name, ParserContext context) {
        Method method = context.getMethod(name);
        return method;
    }

    /**
     * Create new instance factory method.
     * 
     * @param projectName
     *            name of the method project where method is
     * @param revisionName
     *            name of the revision where method is
     * @param path
     *            full path name including packages and classes
     * @param parameter
     *            the optional String parameter for constructor of methods class
     * @param name
     *            the name of the method
     * @param context
     *            parser context for searching of revision and projects
     * @param parameterTypes
     *            list with method parameter types
     * @param declarated
     *            type of method declaration - if method was declaration
     *            contains parameter types
     * @return object representing the method
     */
    public static Method createMethod(String projectName, String revisionName, String path, String parameter, String name, ArrayList<String> parameterTypes,
            DeclarationType declarated, ParserContext context) {
        Revision revision = context.findRevision(projectName, revisionName);
        return new Method(revision, path, parameter, name, parameterTypes, declarated);
    }

    /**
     * Just for XML conversion and tests
     */
    @Deprecated
    public Method() {
    }

    /**
     * Instantiates a new method.
     * 
     * @param id
     *            the unique id of the method for XML conversion
     * @param revision
     *            project and revision information
     * @param path
     *            the path
     * @param parameter
     *            the parameter
     * @param name
     *            the name
     * @param parameterTypes
     *            list of this methods parameter types
     */
    private Method(Revision revision, String path, String parameter, String name, ArrayList<String> parameterTypes, DeclarationType declarated) {
        this.revision = revision;
        this.path = path;
        this.parameter = parameter;
        this.name = name;
        this.declarated = declarated;

        setParameterTypes(parameterTypes);
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
     * Gets the method name.
     * 
     * @return the method name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the method name.
     * 
     * @param name
     *            the new method name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
        result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((revision == null) ? 0 : revision.hashCode());
        // hashCode for ENUM is NOT stable between JVM instances
        result = prime * result + ((declarated == null) ? 0 : declarated.ordinal());
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
        Method other = (Method) obj;
        return EqualsUtils.safeEquals(this.name, other.name)
                && EqualsUtils.safeEquals(this.parameter, other.parameter)
                && EqualsUtils.safeEquals(this.parameterTypes, other.parameterTypes)
                && EqualsUtils.safeEquals(this.path, other.path)
                && EqualsUtils.safeEquals(this.revision, other.revision)
                && EqualsUtils.safeEquals(this.declarated, other.declarated);
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

        addParameterAndMethodDeclaration(buffer, false);

        return buffer.toString();
    }

    /**
     * Adds optional parameter and prescribed method name and parameters types
     * to the passed buffer.
     * 
     * @param buffer
     *            buffer to add the information
     */
    private void addParameterAndMethodDeclaration(StringBuilder buffer, boolean encodeToBase64) {
        if (parameter != null) {
            String param;
            if (encodeToBase64) {
                param = StringUtils.encodeToBase64(parameter);
            } else {
                param = parameter;
            }
            buffer.append(String.format("('%s')", param));
        }

        buffer.append('#');
        buffer.append(name);
        if (declarated == DeclarationType.WITH_PARAMETERS) {
            buffer.append('(');
            for (int idx = 0; idx < parameterTypes.size(); ++idx) {
                if (idx > 0) {
                    buffer.append(", ");
                }
                buffer.append(parameterTypes.get(idx));
            }
            buffer.append(')');
        }
    }

    /**
     * Gets the declaration string without package and project
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

        addParameterAndMethodDeclaration(buffer, false);

        return buffer.toString();
    }

    @Override
    public String getIdentificationString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(revision.getIdentificationString());
        buffer.append(":");
        buffer.append(path);

        addParameterAndMethodDeclaration(buffer, true);

        return buffer.toString();
    }
}
