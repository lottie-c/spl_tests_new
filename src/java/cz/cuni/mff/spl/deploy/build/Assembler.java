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
package cz.cuni.mff.spl.deploy.build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.ZipUtils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class retrieves detailed information needed for building of samplers
 * using reflection from built code of generator and method projects.
 * </p>
 * 
 * <p>
 * Retrieved information is used to generate and build sampling code. This code
 * is packed into zip archive in the memory and returned via {@link Sampler}
 * object.
 * </p>
 * 
 * @author Frantisek Haas
 * @author Martin Lacina
 * 
 */
@SuppressWarnings("unused")
public class Assembler {

    private static final SplLog logger = SplLogger.getLogger(Assembler.class);

    /**
     * <p>
     * Determines what kind of generator is used.
     * 
     * <ul>
     * <li>Generator may be the whole class extending for example
     * {@link ArrayList}.</li>
     * <li>Generator may be a static method on a class which serves as a factory
     * for an {@link ArrayList}.</li>
     * <li>Generator may be a member method on a class which serves as a factory
     * for an {@link ArrayList}.</li>
     * 
     */
    enum GeneratorKind {
        CLASS_ITSELF, STATIC_FACTORY, INSTANCE_FACTORY;
    }

    /** Name of the file to put sampler archive in. */
    private final static String        SAMPLER_ARCHIVE = "Sampler.zip";

    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File                 localStoreRootDirectory;

    /** Identification of the sampler. */
    private final SampleIdentification sid;
    /** String representation of the sampler. */
    private final String               identification;

    /** Directory to put generated files. */
    private final String               outputPath;

    /** Details about the generator. */
    private final Generator            generatorInfo;
    /** Details about the method to measure. */
    private final Method               methodInfo;

    /** Paths to class paths generator code needs. */
    private final File[]               generatorPaths;
    /** Paths to class paths measured method code needs. */
    private final File[]               methodPaths;

    /** Class loader to obtain details about generator using reflection. */
    private final URLClassLoader       generatorClassLoader;
    /** Class loader to obtain details about measured method using reflection. */
    private final URLClassLoader       methodClassLoader;

    /** Kind of generator. Whether the whole class is used or (static) method. */
    private GeneratorKind              generatorKind;

    /** The type measured method accepts. */
    private Class<?>                   generatorType;

    /** Generator class obtained via reflection. To get details. */
    private Class<?>                   generatorClass;
    /** Measured method class obtained via reflection. To get details. */
    private Class<?>                   methodClass;

    /**
     * Generator method obtained via reflection. If (static) method is used.
     * 
     * @see GeneratorKind
     */
    private java.lang.reflect.Method   generatorFunction;

    /**
     * Measured method obtained via reflection. Always used.
     */
    private java.lang.reflect.Method   methodFunction;

    private final List<Integer>        generatorNumericalArguments;

    /** Configuration of the framework. */
    private final ConfigurationBundle  config;

    public Assembler(SampleIdentification sampleIdentification, Generator generator, File[] generatorPaths, Method method, File[] methodPaths,
            List<Integer> generatorNumericalArguments, String outputPath, ConfigurationBundle config, File localStoreRootDirectory)
            throws BuildException {
        this.sid = sampleIdentification;
        this.identification = sampleIdentification.getIdentification();
        this.outputPath = outputPath;

        this.generatorInfo = generator;
        this.generatorPaths = generatorPaths;
        this.generatorClassLoader = createUrlClassLoader(generatorPaths);

        this.methodInfo = method;
        this.methodPaths = methodPaths;
        this.methodClassLoader = createUrlClassLoader(methodPaths);

        this.generatorNumericalArguments = generatorNumericalArguments;

        this.config = config;
        this.localStoreRootDirectory = localStoreRootDirectory;
    }

    /**
     * Creates URL class loader with paths specified.
     * 
     * @param paths
     * @return
     * @throws BuildException
     */
    private URLClassLoader createUrlClassLoader(File[] paths)
            throws BuildException {
        try {
            LinkedList<URL> urls = new LinkedList<>();
            for (File url : paths) {
                urls.add(url.toURI().toURL());
            }
            return new URLClassLoader(urls.toArray(new URL[urls.size()]));
        } catch (MalformedURLException e) {
            throw new BuildException(String.format("Failed to create URL class loader due to [%s].", e.getMessage()), e);
        }
    }

    /**
     * Resolve classes from specified URLs.
     * 
     * @throws BuildException
     */
    private void getClasses()
            throws BuildException {
        try {
            generatorClass = generatorClassLoader.loadClass(generatorInfo.getPath());
        } catch (ClassNotFoundException e) {
            logger.error("Could not load generator class %s (using %s).",
                    generatorInfo.toString(),
                    StringUtils.createOneString(generatorClassLoader.getURLs(), ", "));
            throw new BuildException("Could not load generator class with custom URLClassLoader: " + generatorInfo.toString(), e);
        }

        try {
            methodClass = methodClassLoader.loadClass(methodInfo.getPath());
        } catch (ClassNotFoundException e) {
            logger.error("Could not load method class.", generatorInfo.toString());
            for (URL url : methodClassLoader.getURLs()) {
                logger.error("Method class loader url: %s.", url.toString());
            }
            throw new BuildException("Could not load method class with custom URLClassLoader: " + methodInfo.toString(), e);
        }
    }

    /**
     * Resolve kind of generator and method using reflection.
     * 
     * @throws BuildException
     */
    private void getKinds()
            throws BuildException {
        java.lang.reflect.Method[] generatorClassMethods = generatorClass.getMethods();
        java.lang.reflect.Method[] methodClassMethods = methodClass.getMethods();

        if (generatorInfo == null) {
            throw new BuildException("No measurement generator specified.");
        }
        if (generatorInfo.getMethod() == null) {
            generatorKind = GeneratorKind.CLASS_ITSELF;
        } else {
            for (java.lang.reflect.Method m : generatorClassMethods) {
                if (m.getName().equals(generatorInfo.getMethod().getName())) {
                    generatorFunction = m;
                    if (Modifier.isStatic(generatorFunction.getModifiers())) {
                        generatorKind = GeneratorKind.STATIC_FACTORY;
                    } else {
                        generatorKind = GeneratorKind.INSTANCE_FACTORY;
                    }
                }
            }
        }

        if (methodInfo == null) {
            throw new BuildException("No measurement method specified.");
        } else {
            selectMethodToCall();
        }

        if (generatorKind == null) {
            throw new BuildException("Matching generator not found: " + generatorInfo.toString());
        }
    }

    /**
     * Selects which method in {@link #methodClass} to call by definition in
     * {@link #methodInfo}.
     * 
     * @throws BuildException
     *             The build exception is thrown when either non method
     *             specified by {@link #methodInfo} exists, or two or more
     *             methods match it.
     */
    private void selectMethodToCall() throws BuildException {

        logger.trace("Selecting method for declaration: [%s]", methodInfo.getDeclarationString());

        java.lang.reflect.Method[] methodClassMethods = methodClass.getMethods();

        boolean staticAllowed = methodInfo.getParameter() == null;

        this.methodFunction = null;

        String methodName = methodInfo.getName();
        boolean hasParameterTypesSpecified = methodInfo.getDeclarated() == DeclarationType.WITH_PARAMETERS;
        String[] declaredParameterTypes = methodInfo.getParameterTypes().toArray(new String[methodInfo.getParameterTypes().size()]);
        for (java.lang.reflect.Method m : methodClassMethods) {
            if (m.getName().equals(methodInfo.getName())) {
                if (Modifier.isStatic(m.getModifiers()) && !staticAllowed) {
                    logger.trace("Skipping method as static method is not allowed: [%s]", m);
                    continue;
                }
                if (hasParameterTypesSpecified) {
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length != methodInfo.getParameterTypes().size()) {
                        logger.trace("Skipping method as parameter count does not match: [%s]", m);
                        continue;
                    }
                    boolean matches = true;
                    for (int index = 0; index < declaredParameterTypes.length; ++index) {
                        if (!matchesType(declaredParameterTypes[index], paramTypes[index])) {
                            logger.trace("Skipping method as parameters don't match: [%s]", m);
                            matches = false;
                            break;
                        }
                    }
                    if (!matches) {
                        continue;
                    }
                }
                // when we get here, than method is a match
                if (this.methodFunction != null) {
                    // collision
                    logger.trace("Unable to decide which method to call: [%s] or [%s]", this.methodFunction, m);
                    throw new BuildException(String.format("Unable to decide which method to call: [%s] or [%s]", this.methodFunction, m));
                }
                this.methodFunction = m;
            }
        }
        if (this.methodFunction == null) {
            logger.trace("Found no method to satisfy declaration: [%s]", this.methodInfo.getDeclarationString());
            throw new BuildException(String.format("Found no method to satisfy declaration: [%s]", this.methodInfo.getDeclarationString()));
        }
    }

    /**
     * Checks if string declaration matches provided type.
     * <p>
     * String declaration can be fully qualified (i.e. contains dots) or not.
     * <p>
     * Declared type string is checked against canonical name (
     * {@link Class#getCanonicalName()}) and Java name of class (
     * {@link Class#getName()}). When declared type string is not fully
     * qualified, than it is additionally checked against class name only (
     * {@link Class#getSimpleName()}).
     * 
     * @param declaredType
     *            The declared type to check.
     * @param paramType
     *            The Java class type to check.
     * @return True, if at least one check is satisfied.
     */
    private boolean matchesType(String declaredType, Class<?> paramType) {
        boolean isFullyQualifiedName = declaredType.contains(".");

        String canonicalName = paramType.getCanonicalName();
        String paramFullName = paramType.getName();
        String paramClassName = paramType.getSimpleName();

        return EqualsUtils.safeEquals(declaredType, paramFullName)
                || EqualsUtils.safeEquals(declaredType, canonicalName)
                || (!isFullyQualifiedName && EqualsUtils.safeEquals(declaredType, paramClassName));
    }

    /**
     * This method finds all interfaces the passed class implements.
     * 
     * @param c
     * @return
     */
    private Set<Class<?>> getAllInterfaces(Class<?> c) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        if (c != null) {
            if (c.isInterface()) {
                interfaces.add(c);
            }
            for (Class<?> i : c.getInterfaces()) {
                interfaces.add(i);
                interfaces.addAll(getAllInterfaces(i));
            }
            for (Class<?> s : getAllSuperclasses(c)) {
                interfaces.addAll(getAllInterfaces(s));
            }
        }
        return interfaces;
    }

    /**
     * This method finds all classes the passed class is derived from.
     * 
     * @param c
     * @return
     */
    private Set<Class<?>> getAllSuperclasses(Class<?> c) {
        Set<Class<?>> superclasses = new HashSet<Class<?>>();
        if (c != null && c.getSuperclass() != null) {
            superclasses.add(c.getSuperclass());
            superclasses.addAll(getAllSuperclasses(c.getSuperclass()));
        }
        return superclasses;
    }

    /**
     * Resolve kind of generator and method.
     * 
     * Assuming generator is java.lang.Iterable of Object[] and that is array of
     * arguments for a single method call.
     * 
     * @throws BuildException
     */
    private void getTypes()
            throws BuildException {
        switch (generatorKind) {
            case CLASS_ITSELF:
                generatorType = generatorClass;
                break;
            case STATIC_FACTORY:
                generatorType = generatorFunction.getReturnType();
                break;
            case INSTANCE_FACTORY:
                generatorType = generatorFunction.getReturnType();
                break;
        }
        Set<Class<?>> interfaces = getAllInterfaces(generatorType);
        if (!interfaces.contains(java.lang.Iterable.class)) {
            throw new BuildException("Generator type is not iterable.");
        }
    }

    /**
     * Creates sampling code and invokes compiler on it.
     * 
     * @throws BuildException
     */
    private void createAndCompileCode()
            throws BuildException {
        Code code = new Code(localStoreRootDirectory);

        code.setIdentification(identification);

        code.setGenerator(
                generatorKind,
                generatorType.getCanonicalName(),
                generatorClass.getCanonicalName(),
                generatorInfo.getParameter(),
                generatorInfo.getMethod() == null ? null : generatorInfo.getMethod().getName(),
                generatorInfo.getMethod() == null ? null : generatorInfo.getMethod().getParameter(),
                generatorNumericalArguments,
                generatorPaths);

        code.setMethod(
                methodClass.getCanonicalName(),
                methodInfo.getParameter(),
                methodFunction,
                methodPaths);

        code.setConfiguration(config);

        code.call(outputPath);
    }

    /**
     * <p>
     * Packs the whole generated code directory together with all dependencies
     * into virtual zip archive.
     * 
     * @return
     *         File pointing to the archive with the sampler.
     * @throws BuildException
     */
    private File packCode()
            throws BuildException {
        File sampler = new File(outputPath, SAMPLER_ARCHIVE);

        Set<String> excludePaths = new HashSet<String>();
        excludePaths.add(SAMPLER_ARCHIVE);

        try (OutputStream output = new FileOutputStream(sampler)) {
            String relativePath = "";
            ZipUtils.zip(outputPath, relativePath, output, excludePaths);
            return sampler;
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * <p>
     * Actually generated and builds the sampling code packed in {@link Sampler}.
     * 
     * @return
     *         The prepared {@link Sampler}.
     * @throws BuildException
     */
    public Sampler call()
            throws BuildException {
        getClasses();
        getKinds();
        getTypes();
        createAndCompileCode();

        return new Sampler(sid, packCode(), Code.runCommand(
                config.getDeploymentConfig().getJavaPath(), config.getDeploymentConfig().getSamplerArguments()), Code.RESULT_FILE_NAME);
    }
}
