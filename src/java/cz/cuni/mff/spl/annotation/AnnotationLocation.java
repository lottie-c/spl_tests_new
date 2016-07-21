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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * SPL annotation location description with local declarations.
 * 
 * @author Martin Lacina
 */
public class AnnotationLocation extends AbstractXmlTransformationReference {

    /** The string used to concatenate argument type names. */
    public static final String                   ARGUMENTS_CONCATENATE_STRING = ", ";

    /** The default package name. */
    private static final String                  DEFAULT_PACKAGE_NAME         = "<default package>";

    /** The annotation generator aliases. */
    private final Set<GeneratorAliasDeclaration> generators                   = new LinkedHashSet<>();

    /** The annotation method aliases. */
    private final Set<MethodAliasDeclaration>    methods                      = new LinkedHashSet<>();

    /** The annotation formulas. */
    private final Set<FormulaDeclaration>        formulas                     = new LinkedHashSet<>();

    /** The package name. */
    private String                               packageName;

    /** The class name. */
    private String                               className;

    /** The method name. */
    private String                               methodName;

    /** The arguments. */
    private String                               arguments;

    /** The arguments class names only. */
    private String                               argumentsShort;

    /**
     * The return type canonical name.
     * 
     * @see Class#getCanonicalName()
     */
    private String                               returnType;
    /**
     * The return type class name without package.
     * 
     * @see Class#getSimpleName()
     */
    private String                               returnTypeShort;

    /** The full signature of annotated method. */
    private String                               fullSignature;

    /**
     * The basic signature of annotated method without {@code throws}
     * declarations.
     */
    private String                               basicSignature;

    /**
     * Gets the package name.
     * 
     * @return The package name.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name.
     * 
     * @param packageName
     *            The new package name.
     */
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Gets the class name.
     * 
     * @return The class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name.
     * 
     * @param className
     *            The new class name.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Gets the method name.
     * 
     * @return The method name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     * 
     * @param methodName
     *            The new method name.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the arguments.
     * 
     * @return The arguments.
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments class names only.
     * 
     * @param arguments
     *            The new arguments.
     */
    public void setArgumentsShort(String arguments) {
        this.argumentsShort = arguments;
    }

    /**
     * Gets the arguments class names only.
     * 
     * @return The arguments class names only.
     */
    public String getArgumentsShort() {
        return argumentsShort;
    }

    /**
     * Sets the arguments.
     * 
     * @param arguments
     *            The new arguments.
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    /**
     * Gets the return type.
     * 
     * @return The return type.
     */
    public String getReturnType() {
        return returnType;
    }

    /**
     * Sets the return type.
     * 
     * @param returnType
     *            The new return type.
     */
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * Gets the return type class name without package.
     * 
     * @return The return type class name without package.
     */
    public String getReturnTypeShort() {
        return returnTypeShort;
    }

    /**
     * Sets the return type class name without package.
     * 
     * @param returnTypeShort
     *            The new return type class name without package.
     */
    public void setReturnTypeShort(String returnTypeShort) {
        this.returnTypeShort = returnTypeShort;
    }

    /**
     * Gets the full signature.
     * 
     * @return The full signature.
     */
    public String getFullSignature() {
        return fullSignature;
    }

    /**
     * Sets the full signature.
     * 
     * @param fullSignature
     *            The new full signature.
     */
    public void setFullSignature(String fullSignature) {
        this.fullSignature = fullSignature;
    }

    /**
     * Gets the basic signature.
     * 
     * @return The basic signature.
     */
    public String getBasicSignature() {
        return basicSignature;
    }

    /**
     * Sets the basic signature.
     * 
     * @param basicSignature
     *            The new basic signature.
     */
    public void setBasicSignature(String basicSignature) {
        this.basicSignature = basicSignature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = hashCodeWithoutFormulas();
        result = prime * result + ((generators == null) ? 0 : generators.hashCode());
        result = prime * result + ((methods == null) ? 0 : methods.hashCode());
        result = prime * result + ((formulas == null) ? 0 : formulas.hashCode());
        return result;
    }

    /**
     * Hash code without formulas.
     * 
     * @return The hashcode.
     */
    int hashCodeWithoutFormulas() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + ((argumentsShort == null) ? 0 : argumentsShort.hashCode());
        result = prime * result + ((basicSignature == null) ? 0 : basicSignature.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((fullSignature == null) ? 0 : fullSignature.hashCode());
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        result = prime * result + ((returnTypeShort == null) ? 0 : returnTypeShort.hashCode());
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
        AnnotationLocation other = (AnnotationLocation) obj;
        return EqualsUtils.safeEquals(this.fullSignature, other.fullSignature)

                // skip all other checks for package, class, ... as they are
                // part of full signature and if they don't match, than full
                // signature does not match either

                && EqualsUtils.safeEquals(this.formulas, other.formulas)
                && EqualsUtils.safeEquals(this.generators, other.generators)
                && EqualsUtils.safeEquals(this.methods, other.methods);
    }

    /**
     * Instantiates a new annotation location.
     */
    public AnnotationLocation() {
    }

    /**
     * Creates the annotation location based on provided reflection method.
     * 
     * @param method
     *            The method.
     * @return The annotation location.
     */
    public static AnnotationLocation createAnnotationLocation(java.lang.reflect.Method method) {
        AnnotationLocation annotationLocation = new AnnotationLocation();
        annotationLocation.fullSignature = method.toGenericString();

        annotationLocation.basicSignature = annotationLocation.fullSignature.substring(0, annotationLocation.fullSignature.indexOf(')') + 1);

        Package pkg = method.getDeclaringClass().getPackage();
        if (pkg != null) {
            annotationLocation.packageName = pkg.getName();
        } else {
            annotationLocation.packageName = DEFAULT_PACKAGE_NAME;
        }

        annotationLocation.className = getClassName(method.getDeclaringClass());
        annotationLocation.methodName = method.getName();
        annotationLocation.returnType = method.getReturnType().getCanonicalName();
        annotationLocation.returnTypeShort = method.getReturnType().getSimpleName();

        List<String> argumentNames = new ArrayList<>(method.getParameterTypes().length);
        List<String> argumentNamesShort = new ArrayList<>(method.getParameterTypes().length);

        for (Class<?> c : method.getParameterTypes()) {
            argumentNames.add(c.getCanonicalName());
            argumentNamesShort.add(c.getSimpleName());
        }

        annotationLocation.arguments = StringUtils.createOneString(argumentNames, ", ");
        annotationLocation.argumentsShort = StringUtils.createOneString(argumentNamesShort, ", ");

        return annotationLocation;
    }

    /**
     * Gets the class name.
     * <p>
     * If class is normal class defined in package, than just its simple name is
     * returned.
     * <p>
     * If class is inner class, than defining class name and inner class name
     * concatenated with {@code $}. If inner class is placed recursively, than
     * name is composed recursively too.
     * 
     * @param clazz
     *            The class to get name for.
     * @return The class name.
     */
    private static String getClassName(Class<?> clazz) {

        String name = clazz.getName();

        int lastDotIndex = name.lastIndexOf('.');
        if (lastDotIndex >= 0) {
            name = name.substring(lastDotIndex + 1);
        }

        return name;
    }

    @Override
    public String toString() {
        return "AnnotationLocation [generators=" + generators + ", methods=" + methods + ", formulas=" + formulas + ", packageName=" + packageName
                + ", className=" + className + ", methodName=" + methodName + ", arguments=" + arguments + ", argumentsShort=" + argumentsShort
                + ", returnType=" + returnType + ", returnTypeShort=" + returnTypeShort + ", fullSignature=" + fullSignature + ", basicSignature="
                + basicSignature + "]";
    }

    /**
     * Gets the generator aliases declarations in the annotation.
     * 
     * @return The annotation generator aliases declarations.
     */
    public Set<GeneratorAliasDeclaration> getGeneratorAliases() {
        return generators;
    }

    /**
     * Adds generator alias declaration to the annotation.
     * 
     * @param formula
     *            The generator alias declaration.
     */
    public void addGeneratorAlias(GeneratorAliasDeclaration formula) {
        generators.add(formula);
    }

    /**
     * Gets the method aliases declarations in the annotation.
     * 
     * @return The annotation method aliases declarations.
     */
    public Set<MethodAliasDeclaration> getMethodAliases() {
        return methods;
    }

    /**
     * Adds method alias declaration to the annotation.
     * 
     * @param method
     *            The method alias declaration.
     */
    public void addMethodAlias(MethodAliasDeclaration method) {
        methods.add(method);
    }

    /**
     * Gets the formula declarations in the annotation.
     * 
     * @return The formula declarations in the annotation.
     */
    public Set<FormulaDeclaration> getFormulas() {
        return formulas;
    }

    /**
     * Adds formula declaration to the annotation.
     * 
     * @param formula
     *            The formula declaration.
     */
    public void addFormula(FormulaDeclaration formula) {
        formulas.add(formula);
    }
}
