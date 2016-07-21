package cz.cuni.mff.spl.annotation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.conversion.ConversionException;
import cz.cuni.mff.spl.conversion.InfoConverter;
import cz.cuni.mff.spl.conversion.XmlConversionTest;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.scanner.Scanner;
import cz.cuni.mff.spl.scanner.ScannerException;
import cz.cuni.mff.spl.scanner.ScannerTest;
import cz.cuni.mff.spl.utils.IDSupervisor;

public class HashCodeEqualsTest {

    private Machine                   machine1;
    private Machine                   machine2;

    private Build                     build1;
    private Build                     build2;

    private Revision                  revision1;
    private Revision                  revision2;

    private Repository                repository1;
    private Repository                repository2;

    private Project                   project1;
    private Project                   project2;

    private GeneratorMethod           generatorMethod1;
    private GeneratorMethod           generatorMethod2;

    private Generator                 generator1;
    private Generator                 generator2;

    private Method                    method1;
    private Method                    method2;

    private Measurement               measurement1;
    private Measurement               measurement2;

    private Lambda                    lambda1;
    private Lambda                    lambda2;

    private Comparison                comparison1;
    private Comparison                comparison2;

    private Expression                expression1;
    private Expression                expression2;

    private GeneratorAliasDeclaration generatorDeclaration1;
    private GeneratorAliasDeclaration generatorDeclaration2;

    private MethodAliasDeclaration    methodDeclaration1;
    private MethodAliasDeclaration    methodDeclaration2;

    private FormulaDeclaration        formulaDeclaration1;
    private FormulaDeclaration        formulaDeclaration2;

    private AnnotationLocation        annotationLocation1;
    private AnnotationLocation        annotationLocation2;

    private Info                      info1;
    private Info                      info2;

    @Before
    public void logger() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    @Before
    public void init() throws ConversionException {
        machine1 = new Machine("localhost", "Local computer");
        machine2 = new Machine("localhost", "Local computer");

        build1 = new Build("command");
        build2 = new Build("command");

        repository1 = new Repository("repoType", "url");
        repository2 = new Repository("repoType", "url");

        revision1 = createRevision("p1r1", "p1r1", "get revision", repository1);
        revision2 = createRevision("p1r1", "p1r1", "get revision", repository2);

        project1 = createProject("p1", "project", build1, repository1);
        project2 = createProject("p1", "project", build2, repository2);

        generatorMethod1 = new GeneratorMethod("generator method", "generator method parameter");
        generatorMethod2 = new GeneratorMethod("generator method", "generator method parameter");

        generator1 = new Generator(revision1, "gen.Gen", "param", generatorMethod1);
        generator2 = new Generator(revision2, "gen.Gen", "param", generatorMethod2);

        method1 = createMethod("m1", revision1, "meth.Meth", "method param", "method_name");
        method2 = createMethod("m2", revision2, "meth.Meth", "method param", "method_name");

        measurement1 = new Measurement(method1, generator1, machine1);
        measurement2 = new Measurement(method2, generator2, machine2);

        lambda1 = createLambda(7.0);
        lambda2 = createLambda(7.0);

        comparison1 = new Comparison(measurement1, lambda1, Sign.LE, measurement1, lambda1);
        comparison2 = new Comparison(measurement2, lambda2, Sign.LE, measurement2, lambda2);

        expression1 = new Expression(comparison1, Operator.AND, comparison1);
        expression2 = new Expression(comparison2, Operator.AND, comparison2);

        try {
            annotationLocation1 = AnnotationLocation.createAnnotationLocation(XmlConversionTest.class.getMethod("init"));
            annotationLocation2 = AnnotationLocation.createAnnotationLocation(XmlConversionTest.class.getMethod("init"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        {
            Set<Problem> errors1 = createProblems("error");
            Set<Problem> errors2 = createProblems("error");
            Set<Problem> warnings1 = createProblems("warning");
            Set<Problem> warnings2 = createProblems("warning");

            generatorDeclaration1 = new GeneratorAliasDeclaration("generator", generator1, "g image", errors1, warnings1);
            generatorDeclaration2 = new GeneratorAliasDeclaration("generator", generator2, "g image", errors2, warnings2);
            annotationLocation1.addGeneratorAlias(generatorDeclaration1);
            annotationLocation2.addGeneratorAlias(generatorDeclaration2);

            methodDeclaration1 = new MethodAliasDeclaration("method", method1, "m image", errors1, warnings1);
            methodDeclaration2 = new MethodAliasDeclaration("method", method2, "m image", errors2, warnings2);
            annotationLocation1.addMethodAlias(methodDeclaration1);
            annotationLocation2.addMethodAlias(methodDeclaration2);

            formulaDeclaration1 = new FormulaDeclaration(annotationLocation1, expression1, "f image", errors1, warnings1);
            formulaDeclaration2 = new FormulaDeclaration(annotationLocation2, expression2, "f image", errors2, warnings2);

            annotationLocation1.addFormula(formulaDeclaration1);
            annotationLocation2.addFormula(formulaDeclaration2);
        }
        info1 = createInfo(project1, method1, generator1, measurement1, annotationLocation1);
        info2 = createInfo(project2, method2, generator2, measurement2, annotationLocation2);
    }

    @SuppressWarnings("deprecation")
    private static Method createMethod(String id, Revision revision, String path, String parameter, String name) {
        Method method = new Method();
        method.setId(id);
        method.setRevision(revision);
        method.setPath(path);
        method.setParameter(parameter);
        method.setName(name);
        method.setDeclarated(DeclarationType.WITHOUT_PARAMETERS);
        return method;
    }

    @SuppressWarnings("deprecation")
    private static Project createProject(String id, String alias, Build build, Repository repository) {
        Project project = new Project();
        project.setId(id);
        project.setAlias(alias);
        project.setBuild(build);
        project.setRepository(repository);
        project.getClasspaths().add("project1classpath");
        project.getScanPatterns().add("project2scanPattern");
        return project;
    }

    @SuppressWarnings("deprecation")
    private static Revision createRevision(String id, String alias, String value, Repository repository) {
        Revision revision = new Revision();
        revision.setId(id);
        revision.setAlias(alias);
        revision.setValue(value);
        repository.addRevision(revision);
        return revision;
    }

    private static Lambda createLambda(double... parameters) {
        Lambda l = new Lambda();

        for (double d : parameters) {
            l.add(d);
        }
        return l;

    }

    private Set<Problem> createProblems(String string) {
        Set<Problem> p = new HashSet<>();
        p.add(new Problem(string));
        return p;
    }

    private static Info createInfo(Project project, Method method, Generator generator, Measurement measurement, AnnotationLocation annotationLocation) {
        Revision revision = project.getRepository().getRevisions().values().iterator().next();

        Info info = new Info();
        info.addProject(project);
        info.addGenerator(generator);

        Generator globGen = new Generator(revision, "glob path", "globGen param", new GeneratorMethod(
                "globGenMethod", "globGenMethodArg"));
        info.addGenerator(globGen);
        info.addGlobalGeneratorAlias(new GeneratorAliasDeclaration("globGen", globGen, "globGen image", null, null));

        info.addMethod(method);
        Method globMeth = createMethod(IDSupervisor.getId(), revision, "globMeth path", "globMethArg", "globMethName");

        info.addMethod(globMeth);
        info.addGlobalMethodAlias(new MethodAliasDeclaration("globMeth", globMeth, "globMeth image", null, null));

        info.addMeasurement(measurement);
        info.addAnnotationLocation(annotationLocation);
        info.addParameter("P1", 1.0);
        info.addParameter("P2", 2.0);
        return info;
    }

    public static void generateTestCode() {
        String[] fields = new String[] { "machine",
                "build",
                "revision",
                "repositoryAccess",
                "repository",
                "project",
                "generatorMethod",
                "generator",
                "method",
                "measurement",
                "lambda",
                "comparison",
                "expression",
                "generatorDeclaration",
                "methodDeclaration",
                "formulaDeclaration",
                "annotationLocation",
                "info" };

        for (String field : fields) {
            System.out.printf("@Test\npublic void %s_hashCode() {\nassertEquals(%s1.hashCode(), %s2.hashCode());\n}\n", field, field, field);
            System.out.printf("@Test\npublic void %s_equals() {\nassertEquals(%s1, %s2);\n}\n", field, field, field);
        }
    }

    @Test
    public void machine_hashCode() {
        assertEquals(machine1.hashCode(), machine2.hashCode());
    }

    @Test
    public void machine_equals() {
        assertEquals(machine1, machine2);
    }

    @Test
    public void build_hashCode() {
        assertEquals(build1.hashCode(), build2.hashCode());
    }

    @Test
    public void build_equals() {
        assertEquals(build1, build2);
    }

    @Test
    public void revision_hashCode() {
        assertEquals(revision1.hashCode(), revision2.hashCode());
    }

    @Test
    public void revision_equals() {
        assertEquals(revision1, revision2);
    }

    @Test
    public void repository_hashCode() {
        assertEquals(repository1.hashCode(), repository2.hashCode());
    }

    @Test
    public void repository_equals() {
        assertEquals(repository1, repository2);
    }

    @Test
    public void project_hashCode() {
        assertEquals(project1.hashCode(), project2.hashCode());
    }

    @Test
    public void project_equals() {
        assertEquals(project1, project2);
    }

    @Test
    public void generatorMethod_hashCode() {
        assertEquals(generatorMethod1.hashCode(), generatorMethod2.hashCode());
    }

    @Test
    public void generatorMethod_equals() {
        assertEquals(generatorMethod1, generatorMethod2);
    }

    @Test
    public void generator_hashCode() {
        assertEquals(generator1.hashCode(), generator2.hashCode());
    }

    @Test
    public void generator_equals() {
        assertEquals(generator1, generator2);
    }

    @Test
    public void method_hashCode() {
        assertEquals(method1.hashCode(), method2.hashCode());
    }

    @Test
    public void method_equals() {
        assertEquals(method1, method2);
    }

    @Test
    public void measurement_hashCode() {
        assertEquals(measurement1.hashCode(), measurement2.hashCode());
    }

    @Test
    public void measurement_equals() {
        assertEquals(measurement1, measurement2);
    }

    @Test
    public void lambda_hashCode() {
        assertEquals(lambda1.hashCode(), lambda2.hashCode());
    }

    @Test
    public void lambda_equals() {
        assertEquals(lambda1, lambda2);
    }

    @Test
    public void comparison_hashCode() {
        assertEquals(comparison1.hashCode(), comparison2.hashCode());
    }

    @Test
    public void comparison_equals() {
        assertEquals(comparison1, comparison2);
    }

    @Test
    public void expression_hashCode() {
        assertEquals(expression1.hashCode(), expression2.hashCode());
    }

    @Test
    public void expression_equals() {
        assertEquals(expression1, expression2);
    }

    @Test
    public void generatorDeclaration_hashCode() {
        assertEquals(generatorDeclaration1.hashCode(), generatorDeclaration2.hashCode());
    }

    @Test
    public void generatorDeclaration_equals() {
        assertEquals(generatorDeclaration1, generatorDeclaration2);
    }

    @Test
    public void methodDeclaration_hashCode() {
        assertEquals(methodDeclaration1.hashCode(), methodDeclaration2.hashCode());
    }

    @Test
    public void methodDeclaration_equals() {
        assertEquals(methodDeclaration1, methodDeclaration2);
    }

    @Test
    public void formulaDeclaration_hashCode() {
        assertEquals(formulaDeclaration1.hashCode(), formulaDeclaration2.hashCode());
    }

    @Test
    public void formulaDeclaration_equals() {
        assertEquals(formulaDeclaration1, formulaDeclaration2);
    }

    @Test
    public void annotationLocation_hashCode() {
        assertEquals(annotationLocation1.hashCode(), annotationLocation2.hashCode());
    }

    @Test
    public void annotationLocation_equals() {
        assertEquals(annotationLocation1, annotationLocation2);
    }

    @Test
    public void info_hashCode() {
        assertEquals(info1.hashCode(), info2.hashCode());
    }

    @Test
    public void info_equals() {
        assertEquals(info1, info2);
    }

    @Test
    public void scanner_info_hashCode() throws ConversionException, IOException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.AnnotatedClass" };

        Info inputOutput1 = InfoConverter.loadInfoFromString(ScannerTest.basicTestConfig);
        Info inputOutput2 = InfoConverter.loadInfoFromString(ScannerTest.basicTestConfig);

        Scanner scanner1 = new Scanner(inputOutput1, patterns);
        scanner1.scan();

        Scanner scanner2 = new Scanner(inputOutput2, patterns);
        scanner2.scan();

        assertEquals(inputOutput1.hashCode(), inputOutput2.hashCode());
    }

    @Test
    public void scanner_info_equals() throws ConversionException, IOException, ScannerException {
        String[] patterns = new String[] { "cz.cuni.mff.spl.scanner.AnnotatedClass" };

        Info inputOutput1 = InfoConverter.loadInfoFromString(ScannerTest.basicTestConfig);
        Info inputOutput2 = InfoConverter.loadInfoFromString(ScannerTest.basicTestConfig);

        Scanner scanner1 = new Scanner(inputOutput1, patterns);
        scanner1.scan();

        Scanner scanner2 = new Scanner(inputOutput2, patterns);
        scanner2.scan();

        assertEquals(inputOutput1, inputOutput2);
    }

}
