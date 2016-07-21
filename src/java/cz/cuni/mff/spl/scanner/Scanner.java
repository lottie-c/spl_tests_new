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
package cz.cuni.mff.spl.scanner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.spl.InvokedExecutionConfiguration;
import cz.cuni.mff.spl.InvokedExecutionConfiguration.SplRunInterrupted;
import cz.cuni.mff.spl.Opts;
import cz.cuni.mff.spl.StringFilter;
import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.InfoConverter;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.formula.parser.ParseException;
import cz.cuni.mff.spl.formula.parser.Parser;
import cz.cuni.mff.spl.reflection.ClassListing;
import cz.cuni.mff.spl.reflection.ClassNamesByPaternFilter;
import cz.cuni.mff.spl.utils.Utils;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Find SPL-annotated methods.
 * 
 * The scanner tool scans all given classes for SPL annotations. If there are
 * some, it will parse them and store them into the result summary XML file,
 * which is the input for the measurement tool.
 * 
 * It supports the possibility to scan classes by patterns. These patterns are
 * joined in an OR condition. If no pattern is given, '*' is used (all classes
 * are searched.). There are four kinds of patterns:
 * <ul>
 * <li>'*' - everything</li>
 * <li>'cz.cuni.mff.*' - all classes in package cz.cuni.mff but not the classes
 * in the sub packages</li>
 * <li>'cz.cuni.mff.**' - all classes in package cz.cuni.mff and also the
 * classes in the sub packages</li>
 * <li>'cz.cuni.mff.Class' - just the one class</li>
 * </ul>
 * 
 * The Scanner expects, that all the scanned classes are built on the classpath
 * either as .class files in a directory tree or in a .jar file.
 * There have also the SPL.jar file and its dependencies to be on the classpath.
 * 
 * The input configuration file is expected on ./SPLconfig.xml. If you want to
 * use other one, set is using flag <code>-i</code> The output summary file is
 * created to ./info.xml. If you want to use other one, set is using flag
 * <code>-o</code>
 * 
 * USAGE:
 * java -cp SPL.jar;<SPL-lib-folder>;<scanned-project-classes-OR-jar>
 * cz.cuni.mff.spl.scanner.Scanner [-i inputFileName] [-o outputFileName]
 * [{pattern}]
 * 
 * @author Jiri Daniel
 * @author Jaroslav Kotrc
 * @author Martin Lacina
 * */
public class Scanner {

    /** The scanner logger. */
    private static final SplLog    LOGGER                   = SplLogger.getLogger(Scanner.class);

    /**
     * The generator instance to be used as representation for missing
     * generator.
     */
    private final static Generator MISSING_GENERATOR        = new Generator(null, "<Missing Generator>", null, null);

    private static final String    DEFAULT_INPUT_FILE_NAME  = "./SPLconfig.xml";
    private static final String    DEFAULT_OUTPUT_FILE_NAME = "./info.xml";
    private final BufferedReader   inputReader;
    private final BufferedWriter   outputWriter;

    private final String[]         patterns;
    private final URLClassLoader   classLoader;
    private ParserContext          scopeContext;
    private ParserContext          context;
    private Info                   info;

    Class<?>                       splClazz;

    public Scanner(String[] patterns) throws ConversionException, IOException {
        this(DEFAULT_INPUT_FILE_NAME, DEFAULT_OUTPUT_FILE_NAME, patterns);
    }

    public Scanner(String[] patterns, ClassLoader classLoader) throws ConversionException, IOException {
        this(DEFAULT_INPUT_FILE_NAME, DEFAULT_OUTPUT_FILE_NAME, patterns, new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
    }

    public Scanner(String inputFileName, String outputFileName, String[] patterns) throws ConversionException, IOException {
        this(inputFileName, outputFileName, patterns, new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
    }

    public Scanner(String inputFileName, String outputFileName, String[] patterns, URLClassLoader uRLClassLoader) throws ConversionException,
            IOException {
        if (inputFileName == null) {
            inputFileName = DEFAULT_INPUT_FILE_NAME;
        }
        if (outputFileName == null) {
            outputFileName = DEFAULT_OUTPUT_FILE_NAME;
        }

        this.inputReader = new BufferedReader(new FileReader(inputFileName));
        this.outputWriter = new BufferedWriter(new FileWriter(outputFileName));
        this.patterns = patterns;
        this.classLoader = uRLClassLoader;
        this.init();
    }

    public Scanner(BufferedReader inputReader, BufferedWriter outputWriter, String[] patterns) throws ConversionException, IOException {
        this(inputReader, outputWriter, patterns, new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
    }

    public Scanner(BufferedReader inputReader, BufferedWriter outputWriter, String[] patterns, URLClassLoader uRLClassLoader)
            throws ConversionException, IOException {
        this.inputReader = inputReader;
        this.outputWriter = outputWriter;
        this.patterns = patterns;
        this.classLoader = uRLClassLoader;
        this.init();
    }

    public Scanner(Info inOutInfoObject, String[] patterns) throws ConversionException, IOException {
        this(inOutInfoObject, patterns, new URLClassLoader(new URL[0], ClassLoader.getSystemClassLoader()));
    }

    public Scanner(Info inOutInfoObject, String[] patterns, URLClassLoader uRLClassLoader) throws ConversionException, IOException {
        this.info = inOutInfoObject;
        this.inputReader = null;
        this.outputWriter = null;
        this.patterns = patterns;
        this.classLoader = uRLClassLoader;
        this.init();
    }

    /**
     * Initializes the ParserContext from the input reader or from the input
     * info object
     * 
     * @throws ConversionException
     *             thrown if the conversion from SPLconfig.xml does not succeed
     * @throws IOException
     *             thrown if the config file cannot be found or read
     */
    private void init() throws ConversionException, IOException {
        LOGGER.debug("Start initialization");

        if (inputReader != null) {
            LOGGER.debug("Converting configuration file..");

            info = InfoConverter.loadInfoFromReader(inputReader);

            LOGGER.debug("Converting finished.");
        }

        context = info.getParserContext();

        LOGGER.debug("Initialization completed.");
    }

    /**
     * 
     * @return
     * @throws ConversionException
     * @throws IOException
     * @throws ScannerException
     *             Thrown when {@link cz.cuni.mff.spl.SPL } annotation class is
     *             not available on scanned class path.
     * @throws
     */
    public Info scan() throws ScannerSplAnnotationClassNotFoundException {
        LOGGER.debug("Scanning ..");

        LOGGER.debug("Scanning classes matching patterns :");
        for (String s : patterns) {
            LOGGER.debug("  %s", s);
        }

        // do the work
        scanClasses();
        // print out the parser errors
        context.printErrors();

        LOGGER.debug("Scan complete.");
        return info;
    }

    /**
     * Runs the scanner.
     * 
     * Scans for classes on classpath matching the pattern for SPL annotation
     * and generates the output xml summary file
     * 
     * @param argsArray
     *            -i the input file name. If omitted, ./SPLconfig.xml is used
     *            -o the output file name. If omitted, ./info.xml is used
     *            other args are representing the matching patterns
     * @throws ScannerException
     *             an error occured
     * 
     */
    public static void main(String[] argsArray) throws ScannerException {
        Opts opts = new Opts(argsArray);
        opts.addOption("-i");
        opts.addOption("-o");

        String inputXmlConfigFilename = DEFAULT_INPUT_FILE_NAME;
        if (opts.isPresent("-i")) {
            inputXmlConfigFilename = opts.getValue("-i");
        }
        String outputXmlFilename = DEFAULT_OUTPUT_FILE_NAME;
        if (opts.isPresent("-o")) {
            outputXmlFilename = opts.getValue("-o");
        }

        String[] classPatterns = opts.getRemainingArguments().toArray(new String[0]);

        Scanner scanner = null;

        try {
            scanner = new Scanner(inputXmlConfigFilename, outputXmlFilename, classPatterns);
        } catch (IOException ioe) {
            LOGGER.error(ioe, "Error while accessing/reading the input configuration file.");
            throw new ScannerException("Error while accessing/reading the input configuration file. ", ioe);
        } catch (ConversionException ce) {
            LOGGER.error(ce, "Syntax error in the input configuration file.");
            throw new ScannerException("Syntax error in the input configuration file. ", ce);
        }

        scanner.scan();
        scanner.writeOutput();
    }

    private void writeOutput() throws ScannerException {
        if (outputWriter != null) {
            LOGGER.debug("Creating the output file");
            try {
                InfoConverter.saveInfoToWriter(info, outputWriter);
            } catch (ConversionException e) {
                if (e.getCause() instanceof IOException) {
                    LOGGER.error(e, "Error while accessing/reading the output file.");
                    throw new ScannerException("Error while accessing/reading the output file. ", e);
                } else {
                    LOGGER.error(e, "Conversion error in the output configuration file.");
                    throw new ScannerException("Conversion error in the output configuration file. ", e);
                }
            }
        }
    }

    /**
     * Scans the classPath for given class patterns
     * 
     * @throws ScannerException
     * 
     */
    private void scanClasses() throws ScannerSplAnnotationClassNotFoundException {
        try {
            splClazz = Class.forName(cz.cuni.mff.spl.SPL.class.getCanonicalName(), false, classLoader);
        } catch (ClassNotFoundException cnfe) {
            String message = String.format(
                    "The annotation class '%s' could not be found on class path.",
                    cz.cuni.mff.spl.SPL.class.getCanonicalName()
                    );
            LOGGER.fatal(cnfe, message);
            throw new ScannerSplAnnotationClassNotFoundException(message, cnfe);
        }

        StringFilter classesFilter = ClassNamesByPaternFilter.createFromPatternList(patterns);
        ClassListing classes = new ClassListing(classesFilter);
        List<String> scannedClasses = classes.getAll(classLoader.getURLs());

        for (String s : scannedClasses) {
            scanClass(s);
        }
    }

    /**
     * Try to load the class
     * 
     * @param s
     *            class name
     */
    private void scanClass(String s) {
        /*
         * We want to catch all errors because many of them are not actual
         * errors at all (e.g. stray JARs set-up by OS).
         */
        try {
            // we have to use the contextClassLoader for the case, the scan is
            // called from an runtime java application
            Class<?> c = Class.forName(s, false, classLoader);
            scanClass(c);
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Class '%s' not found, skipping.", s);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.debug("Skipping class '%s' (not found in library path).", s);
        } catch (SplRunInterrupted e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.debug(e, "Skipping class '%s' [Exception %s].", s, e);
        }
    }

    /**
     * Scan the given class object for SPL annotations
     * 
     * @param c
     *            class object
     */
    private void scanClass(Class<?> c) {
        LOGGER.trace("Scanning class [%s].", c.getCanonicalName());

        Set<java.lang.reflect.Method> allMethods = Utils.getAllClassMethods(c);

        for (java.lang.reflect.Method m : allMethods) {
            InvokedExecutionConfiguration.checkIfExecutionAborted();
            Annotation[] annotations = m.getAnnotations();
            for (Annotation a : annotations) {
                if (splClazz.isInstance(a)) {
                    LOGGER.debug("Scanning method '%s' of class '%s'", m.getName(), c.getName());
                    processAnnotation(splClazz, a, m);
                }
            }
        }
    }

    /**
     * Returns list of names of parameter types of given method
     * 
     * @param method
     *            which method parameters will be returned
     * @return list of names of parameter types of given method
     */
    private ArrayList<String> getParameterTypes(java.lang.reflect.Method method) {
        ArrayList<String> types = new ArrayList<String>();
        for (Class<?> parameter : method.getParameterTypes()) {
            types.add(parameter.getName());
        }
        return types;
    }

    /**
     * Processes the annotation of the given method.
     * 
     * Excepts the variable <code>context</code> contains all needed projects
     * and revisions.
     * Fills <code>info</code> with used generators, methods, measurements and
     * formulas. Also adds the method <b>SELF</b>.
     * 
     * @param annotation
     *            The annotation
     * @param self
     *            The method from which the annotation is
     */
    private void processAnnotation(Class<?> splClazz, Annotation annotation, java.lang.reflect.Method self) {
        String[] formulas;
        String[] generators;
        String[] methods;

        try {
            java.lang.reflect.Method formulaMethod = splClazz.getMethod("formula");
            java.lang.reflect.Method generatorsMethod = splClazz.getMethod("generators");
            java.lang.reflect.Method methodsMethod = splClazz.getMethod("methods");

            formulas = (String[]) formulaMethod.invoke(annotation);
            generators = (String[]) generatorsMethod.invoke(annotation);
            methods = (String[]) methodsMethod.invoke(annotation);
        } catch (NoSuchMethodException | SecurityException e) {
            LOGGER.debug(e, "Failed to get the SPL methods of the current scanned annotation %s", annotation);
            return;
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOGGER.debug(e, "Failed to invoke the SPL methods on the current scanned annotation %s", annotation);
            return;
        }

        scopeContext = context.getClone();

        AnnotationLocation annotationLocation = AnnotationLocation.createAnnotationLocation(self);

        /* We need to report all found annotations, even those without formula. */

        /* 
         * Each part of the annotation is parsed with an own parser, but with the same Parser Context
         * Add all generators, methods, measurements and formulas also into the result Info object
         */

        // parse aliases and fill the parser context

        for (String sGenerator : generators) {
            ParserContext generatorContext = scopeContext.getClone();
            GeneratorAliasDeclaration aliasGenerator = null;
            try {
                aliasGenerator = Parser.parseGeneratorAlias(sGenerator, generatorContext);
                scopeContext.addGenerator(aliasGenerator.getAlias(), aliasGenerator.getGenerator());
            } catch (ParseException pe) {
                addMissingGeneratorAlias(sGenerator, scopeContext);
                LOGGER.error(pe, "Skipping generator declaration %s in method %s.", sGenerator, self.getName());
                LOGGER.error("Following errors found:");
                for (Problem error : generatorContext.getErrors()) {
                    LOGGER.error(error.getText());
                }
                aliasGenerator = GeneratorAliasDeclaration.createFailedDeclaration(sGenerator, generatorContext.getErrors(), generatorContext.getWarnings());
            }
            annotationLocation.addGeneratorAlias(aliasGenerator);
        }

        for (String sMethod : methods) {
            ParserContext methodContext = scopeContext.getClone();
            MethodAliasDeclaration aliasMethod = null;
            try {
                aliasMethod = Parser.parseMethodAlias(sMethod, methodContext);
                scopeContext.addMethod(aliasMethod.getAlias(), aliasMethod.getMethod());
            } catch (ParseException pe) {
                LOGGER.error(pe, "Skipping method declaration %s in method %s.", sMethod, self.getName());
                LOGGER.error("Following errors found:");
                for (Problem error : methodContext.getErrors()) {
                    LOGGER.error(error.getText());
                }
                aliasMethod = MethodAliasDeclaration.createFailedDeclaration(sMethod, methodContext.getErrors(), methodContext.getWarnings());
            }
            annotationLocation.addMethodAlias(aliasMethod);
        }

        Method selfMethod = Method.createMethod(ParserContext.THIS_PROJECT, ParserContext.HEAD_REVISION, self.getDeclaringClass().getCanonicalName(), null,
                self.getName(), getParameterTypes(self), DeclarationType.WITH_PARAMETERS, context);
        if (!Modifier.isPublic(self.getModifiers()) || !Modifier.isPublic(self.getDeclaringClass().getModifiers())
                || self.getDeclaringClass().getDeclaringClass() != null) {
            Set<Problem> warnings = new HashSet<>(1);
            warnings.add(new Problem("SELF alias can not be used as it does not stand for public method in top level public class."));
            annotationLocation.addMethodAlias(
                    new MethodAliasDeclaration(ParserContext.SELF_METHOD, null,
                            Parser.mergeAliasNameAndDefinition(ParserContext.SELF_METHOD, selfMethod.getDeclarationString()),
                            null,
                            warnings));
        } else {
            selfMethod = scopeContext.addMethod(ParserContext.SELF_METHOD, selfMethod);
            annotationLocation.addMethodAlias(
                    new MethodAliasDeclaration(ParserContext.SELF_METHOD, selfMethod,
                            Parser.mergeAliasNameAndDefinition(ParserContext.SELF_METHOD, selfMethod.getDeclarationString()),
                            null,
                            null));
        }

        // parse formulas
        for (String sFormula : formulas) {
            ParserContext formulaContext = scopeContext.getClone();
            FormulaDeclaration formulaDeclaration = null;
            try {
                Formula formula = Parser.parseAndExpandFormula(sFormula, formulaContext);
                // if this proceeds without exception no error was found so we
                // can add created objects to the scope context
                for (Measurement measurement : formulaContext.getMeasurements()) {
                    if (isMeasurementValid(measurement)) {
                        scopeContext.addMeasurement(measurement);
                    } else {
                        formulaContext.addError(new Problem("Formula contains invalid measurement (probably missing invalid generator alias)."));
                        throw new ScannerException("Formula contains invalid measurement (probably missing invalid generator alias).");
                    }
                }
                for (Generator generator : formulaContext.getGenerators()) {
                    scopeContext.addGenerator(generator);
                }
                for (Method method : formulaContext.getMethods()) {
                    scopeContext.addMethod(method);
                }

                formulaDeclaration = new FormulaDeclaration(annotationLocation, formula, sFormula, formulaContext.getErrors(), formulaContext
                        .getWarnings());
            } catch (ParseException | ScannerException e) {
                LOGGER.error(e, "Skipping formula %s in method %s. [ParseException]", sFormula, self.getName());
                LOGGER.error("Following errors found:");
                for (Problem error : formulaContext.getErrors()) {
                    LOGGER.error(error.getText());
                }
                formulaDeclaration = FormulaDeclaration.createFailedDeclaration(annotationLocation, sFormula, formulaContext.getErrors(),
                        formulaContext.getWarnings());
            }
            annotationLocation.addFormula(formulaDeclaration);
        }

        // fill the result info object even if error occurred
        // and update parsing context with produced objects.
        info.addAnnotationLocation(annotationLocation);

        for (Generator generator : scopeContext.getGenerators()) {
            // really reference equality as missing generator is singleton
            if (generator != MISSING_GENERATOR) {
                info.addGenerator(generator);
                context.addGenerator(generator);
            }
        }

        for (Method method : scopeContext.getMethods()) {
            info.addMethod(method);
            context.addMethod(method);
        }

        for (Measurement measurement : scopeContext.getMeasurements()) {
            measurement.getMeasurementState().setLastPhase(LastPhase.SCANNER);
            info.addMeasurement(measurement);
            context.addMeasurement(measurement);
        }

    }

    /**
     * <p>
     * Tries to add the invalid generator alias to parser context to support
     * unsatisfiable measurements detection.
     * <p>
     * {@link #MISSING_GENERATOR} is added if alias name is retrieved from
     * declaration.
     * 
     * @param declaration
     *            The declaration.
     * @param context
     *            The context.
     */
    private void addMissingGeneratorAlias(String declaration, ParserContext context) {
        try {
            String aliasName = Parser.splitAliasNameAndDefinition(declaration).getAliasName();
            if (aliasName != null && !aliasName.isEmpty()) {
                context.addGenerator(Parser.splitAliasNameAndDefinition(declaration).getAliasName(), MISSING_GENERATOR);
            }
        } catch (ParseException e) {
        }
    }

    /**
     * Checks if is measurement is valid.
     * <p>
     * Measurement is valid when its generator and method are not {@code null}
     * and not equal to {@link #MISSING_GENERATOR} / {@link #MISSING_METHOD}.
     * 
     * @param measurement
     *            The measurement.
     * @return True, if is measurement is valid.
     */
    private boolean isMeasurementValid(Measurement measurement) {
        return measurement.getGenerator() != null
                && measurement.getMethod() != null
                && !MISSING_GENERATOR.equals(measurement.getGenerator());
    }
}
