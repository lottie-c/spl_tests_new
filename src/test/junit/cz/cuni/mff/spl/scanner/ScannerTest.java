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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Build;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.ExpandedVariable;
import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Repository.RevisionNotFoundException;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.annotation.Variable;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.InfoConverter;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.utils.Utils;

/**
 * @author Jirka
 * 
 */
public class ScannerTest {

    public static final String   basicTestConfig = "<info><projects><project pid = \"THIS\"><alias>THIS</alias><build></build><repository type=\"test\" url=\"url\"><revisions><revision rid = \"HEAD\"><alias>HEAD</alias><value/></revision></revisions></repository></project></projects></info>";
    private static Info          expectedResultAnnotatedClass;
    private static Info          expectedResultBoth;
    private static Info          expectedResultOtherCp;

    @Rule
    public final TemporaryFolder folder          = new TemporaryFolder();

    @Before
    public void logger() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Before
    public void init() {
        expectedResultAnnotatedClass = getConfig();
        addAnnotatedClass(expectedResultAnnotatedClass);
        expectedResultAnnotatedClass.initializeIntegratedGeneratorsLibrary();

        expectedResultBoth = getConfig();
        addAnnotatedClass(expectedResultBoth);
        addOtherAnnotatedClass(expectedResultBoth);
        expectedResultBoth.initializeIntegratedGeneratorsLibrary();

        expectedResultOtherCp = getConfig();
        addAnnotatedClassOtherCp(expectedResultOtherCp);
        expectedResultOtherCp.initializeIntegratedGeneratorsLibrary();
    }

    @Test
    public void testInfo() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.AnnotatedClass" };

        Info inputOutput = InfoConverter.loadInfoFromString(basicTestConfig);

        Scanner scanner = new Scanner(inputOutput, patterns);
        scanner.scan();

        assertEquals(expectedResultAnnotatedClass, inputOutput);
    }

    @Test
    public void testReaders() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.AnnotatedClass" };

        BufferedReader input = new BufferedReader(new StringReader(basicTestConfig));

        StringWriter sw = new StringWriter();
        BufferedWriter output = new BufferedWriter(sw);

        Scanner scanner = new Scanner(input, output, patterns);
        InfoConverter.saveInfoToWriter(scanner.scan(), output);

        Info outputObject = InfoConverter.loadInfoFromString(sw.getBuffer().toString());

        assertEquals(expectedResultAnnotatedClass, outputObject);
    }

    @Test
    public void testFiles() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.AnnotatedClass" };

        folder.create();
        File tmpInputFile = folder.newFile();
        File tmpOutputFile = folder.newFile();

        FileWriter fw = new FileWriter(tmpInputFile);
        fw.write(basicTestConfig);
        fw.close();

        Scanner scanner = new Scanner(tmpInputFile.getCanonicalPath(), tmpOutputFile.getCanonicalPath(), patterns);
        InfoConverter.saveInfoToFile(scanner.scan(), tmpOutputFile);

        Info outputObject = InfoConverter.loadInfoFromFile(tmpOutputFile);
        folder.delete();
        assertEquals(expectedResultAnnotatedClass, outputObject);
    }

    @Test
    public void testPatternPackage() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.*" };

        Info inputOutput = InfoConverter.loadInfoFromString(basicTestConfig);

        Scanner scanner = new Scanner(inputOutput, patterns);
        scanner.scan();

        expectedResultBoth.equals(inputOutput);

        assertEquals(expectedResultBoth, inputOutput);
    }

    @Test
    public void testPatternSubPackage() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.**" };

        Info inputOutput = InfoConverter.loadInfoFromString(basicTestConfig);

        Scanner scanner = new Scanner(inputOutput, patterns);
        scanner.scan();

        expectedResultBoth.getProjects().iterator().next().getRepository().equals(inputOutput.getProjects().iterator().next().getRepository());
        assertEquals(expectedResultBoth, inputOutput);
    }

    @Test
    public void testPatternAll() throws ConversionException, IOException, SAXException, ScannerException {
        String[] patterns = new String[] { "*" };

        Info inputOutput = InfoConverter.loadInfoFromString(basicTestConfig);

        Scanner scanner = new Scanner(inputOutput, patterns);
        scanner.scan();

        assertEquals(expectedResultBoth, inputOutput);
    }

    @Test
    public void testClassPath() throws ConversionException, IOException, InterruptedException, ScannerException {
        String[] cpItems = new String[] { "build" + File.separator + "classes-othercp" + File.separator };
        URLClassLoader classLoader = Utils.addClassPathItems(ClassLoader.getSystemClassLoader(), cpItems);

        String[] patterns = new String[] { "cz.cuni.mff.spl.scannertest.*" };

        Info inputOutput = InfoConverter.loadInfoFromString(basicTestConfig);

        Scanner scanner = new Scanner(inputOutput, patterns, classLoader);
        scanner.scan();

        assertEquals(expectedResultOtherCp, inputOutput);
    }

    private Info getConfig() {
        Info config = new Info();

        Revision rHEAD = new Revision();
        rHEAD.setId("HEAD");
        rHEAD.setAlias("HEAD");
        rHEAD.setValue("");

        Repository repo = new Repository("test", "url");

        Project pTHIS = new Project();
        pTHIS.setId(ParserContext.THIS_PROJECT);
        pTHIS.setAlias(ParserContext.THIS_PROJECT);
        pTHIS.setBuild(new Build());
        pTHIS.setRepository(repo);
        pTHIS.addRevision(rHEAD);

        config.addProject(pTHIS);

        config.getSplIntegratedProject();

        return config;
    }

    private void addAnnotatedClass(Info info) {
        Revision rHEAD = null;
        for (Project p : info.getProjects()) {
            if (p.getAlias().equals(ParserContext.THIS_PROJECT)) {
                try {
                    rHEAD = p.getRepository().getHeadRevision();
                } catch (RevisionNotFoundException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        }

        Generator generator = createGenerator("cz.cuni.mff.spl.scanner.AnnotatedClass", rHEAD);
        info.addGenerator(generator);

        Method mSELF = createMethod("annotatedMethod", "cz.cuni.mff.spl.scanner.AnnotatedClass", rHEAD, new ArrayList<String>());
        info.addMethod(mSELF);

        Method method = createMethod("otherMethod", "cz.cuni.mff.spl.scanner.AnnotatedClass", rHEAD, null);
        info.addMethod(method);

        Variable params100 = createParameter(100);

        Variable params200 = createParameter(200);

        Measurement ma1 = createMeasurement(generator, mSELF, params100);
        info.addMeasurement(ma1);

        Measurement ma2 = createMeasurement(generator, mSELF, params200);
        info.addMeasurement(ma2);

        Measurement mo1 = createMeasurement(generator, method, params100);
        info.addMeasurement(mo1);

        Measurement mo2 = createMeasurement(generator, method, params200);
        info.addMeasurement(mo2);

        AnnotationLocation annotationLocation = new AnnotationLocation();
        annotationLocation.setPackageName("cz.cuni.mff.spl.scanner");
        annotationLocation.setClassName("AnnotatedClass");
        annotationLocation.setMethodName("annotatedMethod");
        annotationLocation.setArguments("");
        annotationLocation.setArgumentsShort("");
        annotationLocation.setReturnType("void");
        annotationLocation.setReturnTypeShort("void");
        annotationLocation.setFullSignature("public void cz.cuni.mff.spl.scanner.AnnotatedClass.annotatedMethod()");
        annotationLocation.setBasicSignature("public void cz.cuni.mff.spl.scanner.AnnotatedClass.annotatedMethod()");

        Formula formula = new Expression(new Comparison(ma1, null, Sign.LE, mo1, null), Operator.AND, new Comparison(ma2, null, Sign.LE, mo2, null));

        Set<Problem> noProblems = new LinkedHashSet<>();
        annotationLocation.addGeneratorAlias(new GeneratorAliasDeclaration("generator", generator,
                "generator=cz.cuni.mff.spl.scanner.AnnotatedClass", noProblems,
                noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration("method", method,
                "method=THIS:cz.cuni.mff.spl.scanner.AnnotatedClass#otherMethod", noProblems,
                noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration(ParserContext.SELF_METHOD, mSELF,
                "SELF=THIS@HEAD:cz.cuni.mff.spl.scanner.AnnotatedClass#annotatedMethod()", noProblems,
                noProblems));
        annotationLocation.addFormula(new FormulaDeclaration(annotationLocation, formula, "for (j {100, 200})" + " SELF[generator](j) <= method[generator](j)",
                noProblems, noProblems));

        info.addAnnotationLocation(annotationLocation);
    }

    private void addOtherAnnotatedClass(Info info) {
        Revision rHEAD = null;
        for (Project p : info.getProjects()) {
            if (p.getAlias().equals(ParserContext.THIS_PROJECT)) {
                try {
                    rHEAD = p.getRepository().getHeadRevision();
                } catch (RevisionNotFoundException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        }

        Generator generator = createGenerator("cz.cuni.mff.spl.scanner.OtherAnnotatedClass", rHEAD);
        info.addGenerator(generator);

        Method method = createMethod("otherMethod", "cz.cuni.mff.spl.scanner.OtherAnnotatedClass", rHEAD, null);
        info.addMethod(method);

        Method mSELF = createMethod("annotatedMethod", "cz.cuni.mff.spl.scanner.OtherAnnotatedClass", rHEAD, new ArrayList<String>());
        info.addMethod(mSELF);

        Variable params100 = createParameter(100);

        Variable params200 = createParameter(200);

        Measurement ma1 = createMeasurement(generator, mSELF, params100);
        info.addMeasurement(ma1);

        Measurement ma2 = createMeasurement(generator, mSELF, params200);
        info.addMeasurement(ma2);

        Measurement mo1 = createMeasurement(generator, method, params100);
        info.addMeasurement(mo1);

        Measurement mo2 = createMeasurement(generator, method, params200);
        info.addMeasurement(mo2);

        AnnotationLocation annotationLocation = new AnnotationLocation();
        annotationLocation.setPackageName("cz.cuni.mff.spl.scanner");
        annotationLocation.setClassName("OtherAnnotatedClass");
        annotationLocation.setMethodName("annotatedMethod");
        annotationLocation.setArguments("");
        annotationLocation.setArgumentsShort("");
        annotationLocation.setReturnType("void");
        annotationLocation.setReturnTypeShort("void");
        annotationLocation.setFullSignature("public void cz.cuni.mff.spl.scanner.OtherAnnotatedClass.annotatedMethod()");
        annotationLocation.setBasicSignature("public void cz.cuni.mff.spl.scanner.OtherAnnotatedClass.annotatedMethod()");

        Formula formula = new Expression(new Comparison(ma1, null, Sign.LE, mo1, null), Operator.AND, new Comparison(ma2, null, Sign.LE, mo2, null));

        Set<Problem> noProblems = new LinkedHashSet<>();
        annotationLocation.addGeneratorAlias(new GeneratorAliasDeclaration("otherGenerator", generator,
                "otherGenerator=cz.cuni.mff.spl.scanner.OtherAnnotatedClass", noProblems,
                noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration("otherMethod", method,
                "otherMethod=THIS:cz.cuni.mff.spl.scanner.OtherAnnotatedClass#otherMethod",
                noProblems,
                noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration(ParserContext.SELF_METHOD, mSELF,
                "SELF=THIS@HEAD:cz.cuni.mff.spl.scanner.OtherAnnotatedClass#annotatedMethod()",
                noProblems, noProblems));
        annotationLocation.addFormula(new FormulaDeclaration(annotationLocation, formula,
                "for (j {100, 200}) SELF[otherGenerator](j) <= otherMethod[otherGenerator](j)", noProblems, noProblems));

        info.addAnnotationLocation(annotationLocation);
    }

    private void addAnnotatedClassOtherCp(Info info) {
        Revision rHEAD = null;
        for (Project p : info.getProjects()) {
            if (p.getAlias().equals(ParserContext.THIS_PROJECT)) {
                try {
                    rHEAD = p.getRepository().getHeadRevision();
                } catch (RevisionNotFoundException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        }

        Generator generator = createGenerator("cz.cuni.mff.spl.scanner.AnnotatedClass", rHEAD);
        info.addGenerator(generator);

        Method mSELF = createMethod("annotatedMethod", "cz.cuni.mff.spl.scannertest.AnnotatedClass", rHEAD, new ArrayList<String>());
        info.addMethod(mSELF);

        Method method = createMethod("otherMethod", "cz.cuni.mff.spl.scanner.AnnotatedClass", rHEAD, null);
        info.addMethod(method);

        Variable params100 = createParameter(100);

        Variable params200 = createParameter(200);

        Measurement ma1 = createMeasurement(generator, mSELF, params100);
        info.addMeasurement(ma1);

        Measurement ma2 = createMeasurement(generator, mSELF, params200);
        info.addMeasurement(ma2);

        Measurement mo1 = createMeasurement(generator, method, params100);
        info.addMeasurement(mo1);

        Measurement mo2 = createMeasurement(generator, method, params200);
        info.addMeasurement(mo2);

        AnnotationLocation annotationLocation = new AnnotationLocation();
        annotationLocation.setPackageName("cz.cuni.mff.spl.scannertest");
        annotationLocation.setClassName("AnnotatedClass");
        annotationLocation.setMethodName("annotatedMethod");
        annotationLocation.setArguments("");
        annotationLocation.setArgumentsShort("");
        annotationLocation.setReturnType("void");
        annotationLocation.setReturnTypeShort("void");
        annotationLocation.setFullSignature("public void cz.cuni.mff.spl.scannertest.AnnotatedClass.annotatedMethod()");
        annotationLocation.setBasicSignature("public void cz.cuni.mff.spl.scannertest.AnnotatedClass.annotatedMethod()");

        Formula formula = new Expression(new Comparison(ma1, null, Sign.LE, mo1, null), Operator.AND, new Comparison(ma2, null, Sign.LE, mo2, null));

        Set<Problem> noProblems = new LinkedHashSet<>();
        annotationLocation.addGeneratorAlias(new GeneratorAliasDeclaration("generator", generator,
                "generator=cz.cuni.mff.spl.scanner.AnnotatedClass", noProblems, noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration("method", method,
                "method=THIS:cz.cuni.mff.spl.scanner.AnnotatedClass#otherMethod", noProblems, noProblems));
        annotationLocation.addMethodAlias(new MethodAliasDeclaration(ParserContext.SELF_METHOD, mSELF,
                "SELF=THIS@HEAD:cz.cuni.mff.spl.scannertest.AnnotatedClass#annotatedMethod()", noProblems, noProblems));
        annotationLocation.addFormula(new FormulaDeclaration(annotationLocation, formula, "for (j {100, 200})" + " SELF[generator](j) <= method[generator](j)",
                noProblems, noProblems));

        info.addAnnotationLocation(annotationLocation);
    }

    private Method createMethod(String name, String path, Revision revision, ArrayList<String> parameterTypes) {
        Method method = new Method();
        method.setName(name);
        method.setPath(path);
        method.setRevision(revision);
        if (parameterTypes == null) {
            method.setDeclarated(Method.DeclarationType.WITHOUT_PARAMETERS);
        } else {
            method.setDeclarated(Method.DeclarationType.WITH_PARAMETERS);
            method.setParameterTypes(parameterTypes);
        }
        return method;
    }

    private Generator createGenerator(String path, Revision revision) {
        Generator generator = new Generator();
        generator.setPath(path);
        generator.setRevision(revision);
        return generator;
    }

    private Measurement createMeasurement(Generator generator, Method method, Variable parameter) {
        Measurement measurement = new Measurement();
        measurement.setGenerator(generator);
        measurement.setMethod(method);
        measurement.setVariable(parameter);
        return measurement;
    }

    private Variable createParameter(Integer... params) {
        ExpandedVariable parameter = new ExpandedVariable();
        List<Integer> values = new ArrayList<>(params.length);
        for (Integer d : params) {
            values.add(d);
        }
        parameter.setVariables(values);
        return parameter;
    }
}
