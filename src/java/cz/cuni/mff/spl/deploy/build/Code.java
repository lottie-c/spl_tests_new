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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.build.Assembler.GeneratorKind;
import cz.cuni.mff.spl.deploy.build.exception.BuildException;
import cz.cuni.mff.spl.deploy.store.LocalStore;
import cz.cuni.mff.spl.utils.DateFormatPattern;
import cz.cuni.mff.spl.utils.FileUtils;
import cz.cuni.mff.spl.utils.StringUtils;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * <p>
 * This class generates and compiles sampling code based on sampling code
 * details set. Code generation is based on Velocity template library.
 * 
 * <p>
 * All generator and method dependencies are copied into the folder where
 * generator code is created. After compiling the whole directory structure is
 * packed into ZIP archive.
 * 
 * <p>
 * The structure of the sampling code is following:
 * 
 * <p>
 * The main class is SMeasurement.java. First task of this class is to load the
 * generator and method code. There maybe dependency troubles in the code when
 * generator and method comes from different revisions. To solve this
 * SMeasurement uses hierarchy of class loaders and loads classes implementing
 * ISGenerator interface with dependencies from generator's code class path and
 * does the same with classes implementing ISMethod interface.
 * 
 * <p>
 * The ISGenerator.java provides generator to SMeasurement. CSGenerator.java is
 * generated and compiled based on actual project's generator source code. Same
 * thing happens for ISMethod.java.
 * 
 * 
 * @author Frantisek Haas
 * @author Jiri Daniel
 * @author Martin Lacina
 * 
 */
public class Code {

    /** Main to execute sampler with. */
    public static final String MAIN_CLASS = "SMeasurement";

    /** Builds command to execute the sampler. */
    public static final String runCommand(String javaPath, String arguments) {
        if (arguments.isEmpty()) {
            return javaPath + " " + MAIN_CLASS;
        } else {
            return javaPath + " " + arguments + " " + MAIN_CLASS;
        }
    }

    /**
     * The {@link LocalStore} root directory or {@code null} when not available.
     */
    private final File               localStoreRootDirectory;

    /** Name of the data result file. */
    public static final String       RESULT_FILE_NAME                  = "result.dat";

    public static final String       COMMENT                           = "#";

    public static final String       SIGN                              = "=";

    /** Key to property in result file. Sampler identification. Not used now. */
    public static final String       PROPERTY_IDENTIFICATION           = "#identification=";
    /** Key to property in result file. Measurement date. */
    public static final String       PROPERTY_DATE                     = "#date=";
    /** Key to property in result file. Warmup cycles performed. */
    public static final String       PROPERTY_WARMUP_COUNT             = "#warmup=";
    /** Key to property in result file. Data samples measured. */
    public static final String       PROPERTY_SAMPLE_COUNT             = "#count=";
    /** Key to property in result file. Starts data section, data follows. */
    public static final String       MARK_SAMPLES_BEGIN                = "#begin";
    /** Key to property in result file. Ends data section, nothing follows. */
    public static final String       MARK_SAMPLES_END                  = "#end";

    /** Sampler identification. */
    private String                   sid;

    private static final String      SAMPLER_IMPLEMENTATION            = "SMeasurement.java";

    private static final String      SAMPLER_IMPLEMENTATION_TEMPLATE   = "measurement.vm";

    /**
     * Determines if generated data comes from a function or is represented by
     * the class itself.
     */
    private GeneratorKind            gKind;

    /** Type of generated data. */
    private String                   gType;

    /** Generator class name. */
    private String                   gClass;

    /**
     * The constructor string argument. Null means no argument, any
     * other instance of String means argument value.
     */
    private String                   gConstructorArgument;

    /** Name of function that generates data. */
    private String                   gFunction;

    /** Argument to function generating data. */
    private String                   gFunctionArgument;

    /** Directory with generator implementation classes. */
    private File[]                   gClasspaths;

    /** The variables to pass to generator. */
    private List<Integer>            gVariables;

    private static final String      GENERATOR_DIRECTORY               = "generator";

    private static final String      GENERATOR_IMPLEMENTATION          = "CSGenerator.java";

    private static final String      GENERATOR_IMPLEMENTATION_TEMPLATE = "cgenerator.vm";

    private static final String      GENERATOR_INTERFACE               = "ISGenerator.java";

    private static final String      GENERATOR_INTERFACE_TEMPLATE      = "igenerator.vm";

    /**
     * <p>
     * Prefix of directories containing the real implementation of generator
     * logic. These classes are wrapped using CGenerator and used via IGenerator
     * interface.
     */
    private static final String      GENERATOR_DEPENDENCY_PREFIX       = "generatorCP";

    /** Name of the class that contains measured method. */
    private String                   mClass;

    /** Argument of measured method class constructor. */
    private String                   mConstructorArgument;

    /** Function to measure. */
    private java.lang.reflect.Method mFunction;

    /** Directory with measured method implementation classes. */
    private File[]                   mClasspaths;

    private static final String      METHOD_DIRECTORY                  = "method";

    private static final String      METHOD_IMPLEMENTATION             = "CSMethod.java";

    private static final String      METHOD_IMPLEMENTATION_TEMPLATE    = "cmethod.vm";

    private static final String      METHOD_INTERFACE                  = "ISMethod.java";

    private static final String      METHOD_INTERFACE_TEMPLATE         = "imethod.vm";

    /**
     * <p>
     * Prefix of directories containing the real implementation of measured
     * method logic. These classes are wrapped using CMethod and used via
     * IMethod interface.
     */
    private static final String      METHOD_DEPENDENCY_PREFIX          = "methodCP";

    /** Various configuration. */
    private ConfigurationBundle      configBundle;

    static {
        // set the log4j properties for global velocity logging
        Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        Velocity.setProperty("runtime.log.logsystem.log4j.logger", SplLogger.VELOCITY_LOGGER_NAME);

        // set the log4j properties for velocity engine
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        ve.setProperty("runtime.log.logsystem.log4j.logger", SplLogger.VELOCITY_LOGGER_NAME);
        ve.init();
    }

    /**
     * Instantiates a new Code instance.
     * 
     * @param localStoreRootDirectory
     *            The local store root directory or {@code null} when not
     *            available.
     */
    public Code(File localStoreRootDirectory) {
        this.localStoreRootDirectory = localStoreRootDirectory;
    }

    public void setIdentification(String identification) {
        this.sid = identification;
    }

    /**
     * Sets generator properties.
     * 
     * @param gKind
     *            Determines if generated data comes from a function or is
     *            represented by
     *            the class itself.
     * @param gType
     *            Type of generated data.
     * @param gClass
     *            Generator class name.
     * @param gConstructorArgument
     *            The constructor string argument. Null means no argument, any
     *            other instance of String means argument value.
     * @param gFunction
     *            The function.
     * @param gFunctionArgument
     *            The function argument.
     * @param gVariables
     *            The variables to pass to generator.
     * @param gClasspaths
     *            Directory with generator implementation classes.
     */
    public void setGenerator(
            GeneratorKind gKind,
            String gType,
            String gClass,
            String gConstructorArgument,
            String gFunction,
            String gFunctionArgument,
            List<Integer> gVariables,
            File[] gClasspaths) {
        this.gKind = gKind;
        this.gType = gType;
        this.gClass = gClass;
        this.gConstructorArgument = StringEscapeUtils.escapeJava(gConstructorArgument);
        this.gFunction = gFunction;
        this.gFunctionArgument = StringEscapeUtils.escapeJava(gFunctionArgument);
        this.gClasspaths = gClasspaths;

        this.gVariables = gVariables;

    }

    /**
     * Sets measured method properties.
     * 
     * @param mClass
     *            Name of the class that contains measured method.
     * @param mConstructorArgument
     *            Argument of meaured method class constructor.
     * @param mFunction
     *            Function to measure.
     * @param mClasspaths
     *            Directory with measured method implementation classes.
     */
    public void setMethod(
            String mClass,
            String mConstructorArgument,
            java.lang.reflect.Method mFunction,
            File[] mClasspaths) {
        this.mClass = mClass;
        this.mConstructorArgument = StringEscapeUtils.escapeJava(mConstructorArgument);
        this.mFunction = mFunction;
        this.mClasspaths = mClasspaths;
    }

    /**
     * Invokes compiler on files specified with classpaths set.
     * 
     * @param filePath
     *            The files to compile.
     * @param classpaths
     *            Dependency classpaths.
     * @throws BuildException
     */
    private void compileCode(String filePath, List<String> classpaths)
            throws BuildException {
        Compiler.call(Arrays.asList(filePath), classpaths);
    }

    /**
     * Fills the template with context specified and writes result into a file.
     * 
     * @param context
     *            Context with specified variables.
     * @param templateName
     *            Template to load and fill.
     * @param fileName
     *            Where to write filled template.
     * @throws BuildException
     */
    @SuppressWarnings("deprecation")
    private void writeCode(VelocityContext context, String templateName, String fileName)
            throws BuildException {
        InputStream template = this.getClass().getResourceAsStream(templateName);
        StringWriter code = new StringWriter();
        Velocity.evaluate(context, code, "", template);
        try {
            FileWriter writer = new FileWriter(new File(fileName));
            writer.write(code.toString().replace("\t", "  "));
            writer.close();

        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * <p>
     * Creates generator implementation source file using properties specified.
     * The implementation inside calls original generator code and implements
     * sampling interface. That makes the usage possible without reflection.
     * 
     * <p>
     * Generated source files are compiled using original classpaths for
     * dependencies.
     * 
     * @param classpath
     * @throws BuildException
     */
    private void makeAndCompileGeneratorCode(String classpath)
            throws BuildException {
        VelocityContext igContext = new VelocityContext();
        VelocityContext cgContext = new VelocityContext();

        File generatorDirectory = new File(classpath, GENERATOR_DIRECTORY);
        generatorDirectory.mkdirs();

        File igFile = new File(classpath, GENERATOR_INTERFACE);
        File cgFile = new File(generatorDirectory.getPath(), GENERATOR_IMPLEMENTATION);

        cgContext.put("identification", sid);
        cgContext.put("gKind", gKind.toString());
        cgContext.put("gType", gType);
        cgContext.put("gClass", gClass);

        cgContext.put("gConstructorString", gConstructorArgument);

        cgContext.put("gFunction", gFunction);
        cgContext.put("gFunctionString", gFunctionArgument);

        String intArguments = StringUtils.createOneString(gVariables, ", ");
        if (!intArguments.isEmpty()) {
            if (gFunction != null) {
                cgContext.put("gFunctionInts", intArguments);
            } else {
                cgContext.put("gConstructorInts", intArguments);
            }
        }

        writeCode(igContext, GENERATOR_INTERFACE_TEMPLATE, igFile.getPath());
        writeCode(cgContext, GENERATOR_IMPLEMENTATION_TEMPLATE, cgFile.getPath());

        List<String> classpaths = new LinkedList<>();
        classpaths.add(classpath);

        compileCode(igFile.getPath(), classpaths);

        classpaths.add(generatorDirectory.getPath());
        for (File file : gClasspaths) {
            classpaths.add(file.getPath());
        }

        compileCode(cgFile.getPath(), classpaths);
    }

    /**
     * <p>
     * Creates method implementation source file using properties specified. The
     * implementation inside calls original method code and implements sampling
     * interface. That makes the usage possible without reflection.
     * 
     * <p>
     * Generated source files are compiled using original classpaths for
     * dependencies.
     * 
     * @param classpath
     * @throws BuildException
     */
    private void makeAndCompileMethodCode(String classpath)
            throws BuildException {
        VelocityContext imContext = new VelocityContext();
        VelocityContext cmContext = new VelocityContext();

        File methodDirectory = new File(classpath, METHOD_DIRECTORY);
        methodDirectory.mkdirs();

        File imFile = new File(classpath, METHOD_INTERFACE);
        File cmFile = new File(methodDirectory.getPath(), METHOD_IMPLEMENTATION);

        cmContext.put("identification", sid);
        cmContext.put("mClass", mClass);
        cmContext.put("mConstructorString", mConstructorArgument);
        cmContext.put("mFunction", mFunction);
        cmContext.put("mFunctionIsStatic", Modifier.isStatic(mFunction.getModifiers()));

        cmContext.put("mFunctionName", mClass + "." + mFunction.getName());

        cmContext.put("mHelper", new CodeHelper());

        writeCode(imContext, METHOD_INTERFACE_TEMPLATE, imFile.getPath());
        writeCode(cmContext, METHOD_IMPLEMENTATION_TEMPLATE, cmFile.getPath());

        List<String> classpaths = new LinkedList<>();
        classpaths.add(classpath);

        compileCode(imFile.getPath(), classpaths);

        classpaths.add(methodDirectory.getPath());
        for (File file : mClasspaths) {
            classpaths.add(file.getPath());
        }

        compileCode(cmFile.getPath(), classpaths);
    }

    /**
     * <p>
     * Creates sampling code that uses interfaces of generator and measured
     * method. Classes implementing these interfaces are directly loaded using
     * class loaders.
     * 
     * @param classpath
     * @throws BuildException
     */
    private void makeAndCompileMeasurementCode(String classpath)
            throws BuildException {
        VelocityContext measurementContext = new VelocityContext();
        File measurementFile = new File(classpath, SAMPLER_IMPLEMENTATION);

        measurementContext.put("identification", sid);
        measurementContext.put("propertyDate", PROPERTY_DATE);
        measurementContext.put("propertyWarmupCount", PROPERTY_WARMUP_COUNT);
        measurementContext.put("propertySampleCount", PROPERTY_SAMPLE_COUNT);
        measurementContext.put("markSamplesBegin", MARK_SAMPLES_BEGIN);
        measurementContext.put("markSamplesEnd", MARK_SAMPLES_END);
        measurementContext.put("resultFileName", RESULT_FILE_NAME);
        measurementContext.put("dateFormatPattern", DateFormatPattern.getSystemPattern());

        measurementContext.put("warmupCycles", configBundle.getDeploymentConfig().getWarmupCycles());
        measurementContext.put("timeSource", configBundle.getDeploymentConfig().getTimeSource());
        measurementContext.put("warmupTime", configBundle.getDeploymentConfig().getWarmupTime());
        measurementContext.put("measurementCycles", configBundle.getDeploymentConfig().getMeasurementCycles());
        measurementContext.put("measurementTime", configBundle.getDeploymentConfig().getMeasurementTime());

        writeCode(measurementContext, SAMPLER_IMPLEMENTATION_TEMPLATE, measurementFile.getPath());

        List<String> classpaths = new LinkedList<>();
        classpaths.add(classpath);

        compileCode(measurementFile.getPath(), classpaths);
    }

    /**
     * For all class paths passed creates a directory based on naming convention
     * and fills it with all files from source directory. These class paths are
     * later used by the measurement code to load classes.
     * 
     * Each library folder (class path) referenced by generator project is
     * copied into generatorCP# folder which is then loaded by measurement code.
     * 
     * @param classpath
     * @throws BuildException
     */
    private void copyGeneratorBinaries(String classpath)
            throws BuildException {
        for (int i = 0; i < gClasspaths.length; i++) {
            try {
                File target = new File(classpath, GENERATOR_DEPENDENCY_PREFIX + i);
                FileUtils.copyDirectory(gClasspaths[i], target, localStoreRootDirectory);
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    /**
     * For all class paths passed creates a directory based on naming convention
     * and fills it with all files from source directory. These class paths are
     * later used by the measurement code to load classes.
     * 
     * Each library folder (class path) referenced by method project is
     * copied into methodCP# folder which is then loaded by measurement code.
     * 
     * @param classpath
     * @throws BuildException
     */
    private void copyMethodBinaries(String classpath)
            throws BuildException {
        for (int i = 0; i < mClasspaths.length; i++) {
            try {
                File target = new File(classpath, METHOD_DEPENDENCY_PREFIX + i);
                FileUtils.copyDirectory(mClasspaths[i], target, localStoreRootDirectory);
            } catch (IOException e) {
                throw new BuildException(e);
            }
        }
    }

    /**
     * Creates the sampler code based on configuration set in the specified
     * directory and build the code. Packs the sampler code into archive file.
     * 
     * @param classpath
     *            The path where to create the code, copy the required libraries
     *            and build the code.
     * @throws BuildException
     */
    public void call(String classpath)
            throws BuildException {
        makeAndCompileGeneratorCode(classpath);
        makeAndCompileMethodCode(classpath);
        makeAndCompileMeasurementCode(classpath);
        copyGeneratorBinaries(classpath);
        copyMethodBinaries(classpath);
    }

    /**
     * The helper for Velocity code generation.
     * 
     * @author Martin Lacina
     */
    public class CodeHelper {

        /**
         * Checks if modifier static flag (as defined for
         * {@link java.lang.reflect.Modifier} is set.
         * 
         * @param modifiers
         *            The modifiers.
         * @return True, if static flag is set.
         */
        public boolean isStatic(int modifiers) {
            return Modifier.isStatic(modifiers);
        }

        /**
         * Gets the class name for "instanceof" declarations.
         * 
         * Returns class canonical name for classes derived from Object,
         * return appropriate wrapper type canonical name for primitive types.
         * 
         * @param clazz
         *            The clazz.
         * @return The class name for "instanceof" declarations.
         */
        public String getClassNameForInstanceOf(Class<?> clazz) {
            String result = clazz.getCanonicalName();
            if (clazz.isPrimitive()) {
                switch (result) {
                    case "byte":
                        return Byte.class.getCanonicalName();
                    case "short":
                        return Short.class.getCanonicalName();
                    case "int":
                        return Integer.class.getCanonicalName();
                    case "long":
                        return Long.class.getCanonicalName();
                    case "float":
                        return Float.class.getCanonicalName();
                    case "double":
                        return Double.class.getCanonicalName();
                    case "boolean":
                        return Boolean.class.getCanonicalName();
                    case "char":
                        return Character.class.getCanonicalName();
                    default:
                        throw new IllegalStateException("Unexpected primitive type " + result);
                }
            } else {
                return result;
            }
        }
    }

    /**
     * Sets configuration.
     * 
     * @param configBundle
     */
    public void setConfiguration(ConfigurationBundle configBundle) {
        this.configBundle = configBundle;
    }
}
