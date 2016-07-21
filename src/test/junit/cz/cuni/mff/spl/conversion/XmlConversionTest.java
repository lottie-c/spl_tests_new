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
package cz.cuni.mff.spl.conversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import cz.cuni.mff.spl.annotation.AnnotationLocation;
import cz.cuni.mff.spl.annotation.Build;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.ExpandedVariable;
import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.FormulaDeclaration;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorAliasDeclaration;
import cz.cuni.mff.spl.annotation.GeneratorMethod;
import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.annotation.MethodAliasDeclaration;
import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.annotation.Sign;

/**
 * @author Jiri Daniel
 * @author Martin Lacina
 */
public class XmlConversionTest {

    private static final String XMLprefix                     = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private final String        machineXML                    = "<machine identification=\"localhost\" name=\"Local computer\"/>";
    private Machine             machineObject;

    private final String        build1XML                     = "<build>command 1</build>";
    private Build               build1Object;
    private final String        build2XML                     = "<build>command 2</build>";
    private Build               build2Object;

    private final String        revision11XML                 = "<revision rid=\"p1r1\"><alias>p1r1</alias><value>get revision 1 1</value><comment>a comment string</comment><identification>revision1identification</identification></revision>";
    private Revision            revision11Object;
    private final String        revision12XML                 = "<revision rid=\"p1r2\"><alias>p1r2</alias><value>get revision 1 2</value></revision>";
    private Revision            revision12Object;
    private final String        revision21XML                 = "<revision rid=\"p2r1\"><alias>p2r1</alias><value>get revision 2 1</value></revision>";
    private Revision            revision21Object;

    private final String        repository1XML                = "<repository type=\"repoType1\" url=\"url 1\">"
                                                                      + "<revisions>" + revision11XML + revision12XML + "</revisions>"
                                                                      + "</repository>";
    private Repository          repository1Object;
    private final String        repository2XML                = "<repository type=\"repoType2\" url=\"url 2\">"
                                                                      + "<revisions>" + revision21XML + "</revisions>"
                                                                      + "</repository>";
    private Repository          repository2Object;

    private final String        project1XML                   = "<project pid=\"p1\"><alias>project 1</alias>" + build1XML
                                                                      + "<classpaths><classpath>project1classpath</classpath></classpaths>" + repository1XML
                                                                      + "</project>";
    private Project             project1Object;
    private final String        project2XML                   = "<project pid=\"p2\"><alias>project 2</alias>" + build2XML
                                                                      + "<scanPatterns><scanPattern>project2scanPattern</scanPattern></scanPatterns>"
                                                                      + repository2XML + "</project>";
    private Project             project2Object;

    private final String        generatorMethod1XML           = "<genMethod><name>generator method 1</name><parameter>generator method 1 parameter</parameter></genMethod>";
    private GeneratorMethod     generatorMethod1Object;
    private final String        generatorMethod2XML           = "<genMethod><name>generator method 2</name><parameter>generator method 2 parameter</parameter></genMethod>";
    private GeneratorMethod     generatorMethod2Object;

    private final String        generator1XML                 = "<generator gid=\"g1\"><path>gen.Gen1</path><parameter>param1</parameter>"
                                                                      + generatorMethod1XML + "<revision rref=\"p1r1\"/></generator>";
    private Generator           generator1Object;
    private final String        generator2XML                 = "<generator gid=\"g2\"><path>gen.Gen2</path><parameter>param2</parameter>"
                                                                      + generatorMethod2XML + "<revision rref=\"p2r1\"/></generator>";
    private Generator           generator2Object;

    private final String        globalGeneratorMethod1XML     = "<genMethod><name>global generator method 1</name><parameter>global generator method 1 parameter</parameter></genMethod>";
    private GeneratorMethod     globalGeneratorMethod1Object;
    private final String        globalGenerator1XML           = "<generator gid=\"global-g1\"><path>gen.GlobalGen1</path><parameter>globalParam1</parameter>"
                                                                      + globalGeneratorMethod1XML + "<revision rref=\"p1r1\"/></generator>";
    private Generator           globalGenerator1Object;

    private final String        integratedGeneratorMethod1XML = "<genMethod><name>integrated generator method 1</name><parameter>integrated generator method 1 parameter</parameter></genMethod>";
    private GeneratorMethod     integratedGeneratorMethod1Object;
    private final String        integratedGenerator1XML       = "<generator gid=\"integrated-g1\"><path>gen.IntegratedGen1</path><parameter>integratedParam1</parameter>"
                                                                      + integratedGeneratorMethod1XML + "<revision rref=\"p1r1\"/></generator>";
    private Generator           integratedGenerator1Object;

    private final String        method1XML                    = "<method mid=\"m1\"><path>meth.Meth1</path><parameter>method param1</parameter><name>method1</name><revision rref=\"p1r1\"/><declarated type=\"WITHOUT_PARAMETERS\"/></method>";
    private Method              method1Object;
    private final String        method2XML                    = "<method mid=\"m2\"><path>meth.Meth2</path><parameter>method param2</parameter><name>method2</name><revision rref=\"p2r1\"/><declarated type=\"WITHOUT_PARAMETERS\"/></method>";
    private Method              method2Object;

    private final String        globalMethod1XML              = "<method mid=\"global-m1\"><path>meth.GlobalMeth1</path><parameter>global method param1</parameter><name>globalMethod1</name><revision rref=\"p1r1\"/><declarated type=\"WITHOUT_PARAMETERS\"/></method>";
    private Method              globalMethod1Object;

    private final String        measurement1XML               = "<measurement msid=\"ms1\" mref=\"m1\" gref=\"g1\" computer-id=\"machine1\" computer-name=\"PC1\"><variables><variable>0</variable><variable>1</variable></variables><measurement-state ok=\"true\" last-phase=\"INITIALIZED\"/></measurement>";
    private Measurement         measurement1Object;
    private final String        measurement2XML               = "<measurement msid=\"ms2\" mref=\"m2\" gref=\"g2\" computer-id=\"machine2\" computer-name=\"PC2\"><variables><variable>3</variable><variable>1</variable></variables><measurement-state ok=\"true\" last-phase=\"INITIALIZED\"/></measurement>";
    private Measurement         measurement2Object;

    private final String        comparison1XML                = "<comparison fid=\"comp1\">"
                                                                      + "<leftLambda><const>2.0</const></leftLambda>"
                                                                      + "<rightLambda><const>10.2</const></rightLambda>"
                                                                      + "<leftMethod msref=\"ms1\"/><sign type=\"LE\"/><rightMethod msref=\"ms2\"/>"
                                                                      + "</comparison>";
    private Comparison          comparison1Object;

    private final String        comparison2XML                = "<comparison fid=\"comp2\">"
                                                                      + "<leftLambda><const>0.0</const></leftLambda><rightLambda><const>10.2</const></rightLambda>"
                                                                      + "<leftMethod msref=\"ms2\"/><sign type=\"GT\"/><rightMethod msref=\"ms1\"/>"
                                                                      + "</comparison>";
    private Comparison          comparison2Object;

    private final String        expression1XML                = "<expression fid=\"expr1\">"
                                                                      + "<leftFormula>" + comparison1XML
                                                                      + "</leftFormula><rightFormula>"
                                                                      + comparison2XML
                                                                      + "</rightFormula><operator op=\"AND\"/>"
                                                                      + "</expression>";
    private Expression          expression1Object;

    private final String        expression2XML                = "<expression fid=\"expr2\">"
                                                                      + "<leftFormula>" + comparison2XML
                                                                      + "</leftFormula><rightFormula>"
                                                                      + comparison1XML
                                                                      + "</rightFormula><operator op=\"OR\"/>"
                                                                      + "</expression>";
    private Expression          expression2Object;

    private final String        annotationLocationXML         = "<annotation-location aid=\"aloc1\" package=\"cz.cuni.mff.spl.conversion\" class=\"XmlConversionTest\" method=\"init\" arguments=\"\" arguments-short=\"\" return-type=\"void\" return-type-short=\"void\" full-signature=\"public void cz.cuni.mff.spl.conversion.XmlConversionTest.init() throws cz.cuni.mff.spl.conversion.ConversionException\" basic-signature=\"public void cz.cuni.mff.spl.conversion.XmlConversionTest.init()\">"
                                                                      + "<generator-declaration pdid=\"pd-g-1\" gref=\"g1\"><alias>generator 1</alias><image>generator 1</image></generator-declaration>"
                                                                      + "<method-declaration pdid=\"pd-m-1\" mref=\"m1\"><alias>method 1</alias><image>method 1</image></method-declaration>"
                                                                      + "<formula-declaration pdid=\"pd-f-1\"><image>formula 1</image>"
                                                                      + expression1XML + "</formula-declaration>"
                                                                      + "<formula-declaration pdid=\"pd-f-2\"><image>formula 2</image>"
                                                                      + expression2XML + "</formula-declaration>"
                                                                      + "</annotation-location>";

    private AnnotationLocation  annotationLocationObject;

    private final String        annotationLocations           = "<annotation-locations>" + annotationLocationXML + "</annotation-locations>";

    private final String        globalGenerators              = "<global-generators><generator-declaration pdid=\"pd-gg-1\" gref=\"global-g1\"><alias>global generator 1</alias><image>global generator 1</image></generator-declaration></global-generators>";
    private final String        globalMethods                 = "<global-methods><method-declaration  pdid=\"pd-gm-1\" mref=\"global-m1\"><alias>global method 1</alias><image>global method 1</image></method-declaration></global-methods>";
    private final String        integratedGenerators          = "<spl-integrated-generators><spl-integrated-generator pdid=\"pd-ig-1\" gref=\"integrated-g1\"><alias>integrated generator 1</alias><image>integrated generator 1</image></spl-integrated-generator></spl-integrated-generators>";

    private final String        info1XML                      = "<info><projects>" + project1XML + project2XML + "</projects>"
                                                                      + "<generators>" + generator1XML + generator2XML + globalGenerator1XML
                                                                      + integratedGenerator1XML + "</generators>"
                                                                      + "<methods>" + method1XML + method2XML + globalMethod1XML + "</methods>"
                                                                      + "<measurements>" + measurement1XML + measurement2XML + "</measurements>"
                                                                      + globalGenerators + globalMethods + integratedGenerators
                                                                      + annotationLocations
                                                                      + "<parameters>" + "<parameter><name>P1</name><value>1.0</value></parameter>"
                                                                      + "<parameter><name>P2</name><value>2.0</value></parameter>" + "</parameters>"
                                                                      + "</info>";

    private Info                info1Object;

    @SuppressWarnings("deprecation")
    @Before
    public void init() throws ConversionException {
        LogManager.getRootLogger().setLevel(Level.FATAL);

        machineObject = new Machine("localhost", "Local computer");

        build1Object = new Build("command 1");
        build2Object = new Build("command 2");

        repository1Object = new Repository("repoType1", "url 1");
        repository2Object = new Repository("repoType2", "url 2");

        revision11Object = createRevision("p1r1", "p1r1", "get revision 1 1", repository1Object);
        revision11Object.setRevisionIdentification("revision1identification");
        revision11Object.setComment("a comment string");
        revision12Object = createRevision("p1r2", "p1r2", "get revision 1 2", repository1Object);
        revision21Object = createRevision("p2r1", "p2r1", "get revision 2 1", repository2Object);

        project1Object = createProject("p1", "project 1", build1Object, repository1Object);
        project1Object.getClasspaths().add("project1classpath");

        project2Object = createProject("p2", "project 2", build2Object, repository2Object);
        project2Object.getScanPatterns().add("project2scanPattern");

        project1Object.addRevision(revision11Object);
        project1Object.addRevision(revision12Object);
        project2Object.addRevision(revision21Object);

        generatorMethod1Object = new GeneratorMethod("generator method 1", "generator method 1 parameter");
        generatorMethod2Object = new GeneratorMethod("generator method 2", "generator method 2 parameter");

        generator1Object = new Generator(revision11Object, "gen.Gen1", "param1", generatorMethod1Object);
        generator1Object.setId("g1");
        generator2Object = new Generator(revision21Object, "gen.Gen2", "param2", generatorMethod2Object);
        generator2Object.setId("g2");

        globalGeneratorMethod1Object = new GeneratorMethod("global generator method 1", "global generator method 1 parameter");
        globalGenerator1Object = new Generator(revision11Object, "gen.GlobalGen1", "globalParam1", globalGeneratorMethod1Object);
        globalGenerator1Object.setId("global-g1");

        integratedGeneratorMethod1Object = new GeneratorMethod("integrated generator method 1", "integrated generator method 1 parameter");
        integratedGenerator1Object = new Generator(revision11Object, "gen.IntegratedGen1", "integratedParam1", integratedGeneratorMethod1Object);
        integratedGenerator1Object.setId("integrated-g1");

        method1Object = createMethod("m1", revision11Object, "meth.Meth1", "method param1", "method1");
        method2Object = createMethod("m2", revision21Object, "meth.Meth2", "method param2", "method2");

        globalMethod1Object = createMethod("global-m1", revision11Object, "meth.GlobalMeth1", "global method param1", "globalMethod1");

        measurement1Object = new Measurement(method1Object, generator1Object, new Machine("machine1", "PC1"));
        measurement1Object.setId("ms1");
        List<Integer> params1 = new ArrayList<>(2);
        params1.add(0);
        params1.add(1);
        ExpandedVariable parameters1 = new ExpandedVariable();
        parameters1.setVariables(params1);
        measurement1Object.setVariable(parameters1);

        measurement2Object = new Measurement(method2Object, generator2Object, new Machine("machine2", "PC2"));
        measurement2Object.setId("ms2");
        List<Integer> params2 = new ArrayList<>(2);
        params2.add(3);
        params2.add(1);
        ExpandedVariable parameters2 = new ExpandedVariable();
        parameters2.setVariables(params2);
        measurement2Object.setVariable(parameters2);

        Lambda l2 = new Lambda();
        l2.add(2.0);
        Lambda l0 = new Lambda();
        l0.add(0);
        Lambda l10 = new Lambda();
        l10.add(10.2);

        comparison1Object = new Comparison(measurement1Object, l2, Sign.LE, measurement2Object, l10);
        comparison1Object.setId("comp1");
        comparison2Object = new Comparison(measurement2Object, l0, Sign.GT, measurement1Object, l10);
        comparison2Object.setId("comp2");

        try {
            annotationLocationObject = AnnotationLocation.createAnnotationLocation(XmlConversionTest.class.getMethod("init"));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        annotationLocationObject.setId("aloc1");

        expression1Object = new Expression(comparison1Object, Operator.AND, comparison2Object);
        expression1Object.setId("expr1");

        expression2Object = new Expression(comparison2Object, Operator.OR, comparison1Object);
        expression2Object.setId("expr2");

        FormulaDeclaration formulaDeclaration;

        formulaDeclaration = new FormulaDeclaration(annotationLocationObject, expression1Object, "formula 1", null, null);
        formulaDeclaration.setId("pd-f-1");
        annotationLocationObject.addFormula(formulaDeclaration);
        formulaDeclaration = new FormulaDeclaration(annotationLocationObject, expression2Object, "formula 2", null, null);
        formulaDeclaration.setId("pd-f-2");
        annotationLocationObject.addFormula(formulaDeclaration);

        MethodAliasDeclaration methodAliasDeclaration;

        methodAliasDeclaration = new MethodAliasDeclaration("method 1", method1Object, "method 1", null, null);
        methodAliasDeclaration.setId("pd-m-1");
        annotationLocationObject.addMethodAlias(methodAliasDeclaration);

        GeneratorAliasDeclaration generatorAliasDeclaration;

        generatorAliasDeclaration = new GeneratorAliasDeclaration("generator 1", generator1Object, "generator 1", null, null);
        generatorAliasDeclaration.setId("pd-g-1");
        annotationLocationObject.addGeneratorAlias(generatorAliasDeclaration);

        GeneratorAliasDeclaration globalGeneratorAliasDeclaration = new GeneratorAliasDeclaration("global generator 1", globalGenerator1Object,
                "global generator 1", null, null);
        globalGeneratorAliasDeclaration.setId("pd-gg-1");

        GeneratorAliasDeclaration integratedGeneratorAliasDeclaration = new GeneratorAliasDeclaration("integrated generator 1", integratedGenerator1Object,
                "integrated generator 1", null, null);
        integratedGeneratorAliasDeclaration.setId("pd-ig-1");

        info1Object = new Info();
        info1Object.addProject(project1Object);
        info1Object.addProject(project2Object);
        info1Object.addMethod(method1Object);
        info1Object.addMethod(method2Object);
        info1Object.addMethod(globalMethod1Object);
        methodAliasDeclaration = new MethodAliasDeclaration("global method 1", globalMethod1Object, "global method 1", null, null);
        methodAliasDeclaration.setId("pd-gm-1");
        info1Object.addGlobalMethodAlias(methodAliasDeclaration);
        info1Object.addGenerator(generator1Object);
        info1Object.addGenerator(generator2Object);
        info1Object.addGenerator(globalGenerator1Object);
        info1Object.addGlobalGeneratorAlias(globalGeneratorAliasDeclaration);
        info1Object.addGenerator(integratedGenerator1Object);
        info1Object.addSplIntegratedGeneratorAlias(integratedGeneratorAliasDeclaration);
        info1Object.addMeasurement(measurement1Object);
        info1Object.addMeasurement(measurement2Object);
        info1Object.addAnnotationLocation(annotationLocationObject);
        info1Object.addParameter("P1", 1.0);
        info1Object.addParameter("P2", 2.0);
    }

    @Test
    public void convertBuildToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(build1Object);
        assertEquals(xml1, XMLprefix + build1XML);
        String xml2 = XmlConversion.ConvertClassToXml(build2Object);
        assertEquals(xml2, XMLprefix + build2XML);
    }

    @Test
    public void convertBuildFromXml() throws ConversionException {

        Build object1 = (Build) XmlConversion.ConvertClassFromXml(XMLprefix + build1XML);
        assertEquals(object1, build1Object);
        Object object2 = XmlConversion.ConvertClassFromXml(XMLprefix + build2XML);
        assertEquals(object2, build2Object);
    }

    @Test
    public void convertRepositoryToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(repository1Object);
        assertEquals(xml1, XMLprefix + repository1XML);
        String xml2 = XmlConversion.ConvertClassToXml(repository2Object);
        assertEquals(xml2, XMLprefix + repository2XML);
    }

    @Test
    public void convertRepositoryFromXml() throws ConversionException {

        Repository object1 = (Repository) XmlConversion.ConvertClassFromXml(XMLprefix + repository1XML);
        object1.setProject(project1Object);
        assertEquals(repository1Object, object1);

        Repository object2 = (Repository) XmlConversion.ConvertClassFromXml(XMLprefix + repository2XML);
        object2.setProject(project2Object);
        assertEquals(repository2Object, object2);
    }

    @Test
    public void convertRevisionToXml() throws ConversionException {

        String xml11 = XmlConversion.ConvertClassToXml(revision11Object);
        assertEquals(xml11, XMLprefix + revision11XML);
        String xml12 = XmlConversion.ConvertClassToXml(revision12Object);
        assertEquals(xml12, XMLprefix + revision12XML);
        String xml21 = XmlConversion.ConvertClassToXml(revision21Object);
        assertEquals(xml21, XMLprefix + revision21XML);
    }

    @Test
    public void convertRevisionFromXml() throws ConversionException {

        Revision object11 = (Revision) XmlConversion.ConvertClassFromXml(XMLprefix + revision11XML);
        // is null because referenced object is not in XML
        object11.setRepository(repository1Object);
        assertEquals(object11, revision11Object);
        Revision object12 = (Revision) XmlConversion.ConvertClassFromXml(XMLprefix + revision12XML);
        // is null because referenced object is not in XML
        object12.setRepository(repository1Object);
        assertEquals(object12, revision12Object);
        Revision object21 = (Revision) XmlConversion.ConvertClassFromXml(XMLprefix + revision21XML);
        // is null because referenced object is not in XML
        object21.setRepository(repository2Object);
        assertEquals(object21, revision21Object);
    }

    @Test
    public void convertProjectToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(project1Object);
        assertEquals(xml1, XMLprefix + project1XML);
        String xml2 = XmlConversion.ConvertClassToXml(project2Object);
        assertEquals(xml2, XMLprefix + project2XML);
    }

    @Test
    public void convertProjectFromXml() throws ConversionException {

        Project object1 = (Project) XmlConversion.ConvertClassFromXml(XMLprefix + project1XML);
        assertEquals(object1, project1Object);
        Project object2 = (Project) XmlConversion.ConvertClassFromXml(XMLprefix + project2XML);
        assertEquals(object2, project2Object);
    }

    @Test
    public void convertGeneratorMethodToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(generatorMethod1Object);
        assertEquals(xml1, XMLprefix + generatorMethod1XML);
        String xml2 = XmlConversion.ConvertClassToXml(generatorMethod2Object);
        assertEquals(xml2, XMLprefix + generatorMethod2XML);
    }

    @Test
    public void convertGeneratorMethodFromXml() throws ConversionException {

        GeneratorMethod object1 = (GeneratorMethod) XmlConversion.ConvertClassFromXml(XMLprefix + generatorMethod1XML);
        assertEquals(object1, generatorMethod1Object);
        GeneratorMethod object2 = (GeneratorMethod) XmlConversion.ConvertClassFromXml(XMLprefix + generatorMethod2XML);
        assertEquals(object2, generatorMethod2Object);
    }

    @Test
    public void convertGeneratorToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(generator1Object);
        assertEquals(xml1, XMLprefix + generator1XML);
        String xml2 = XmlConversion.ConvertClassToXml(generator2Object);
        assertEquals(xml2, XMLprefix + generator2XML);
    }

    @Test
    public void convertGeneratorFromXml() throws ConversionException {

        Generator object1 = (Generator) XmlConversion.ConvertClassFromXml(XMLprefix + generator1XML);
        // is null because referenced object is not in XML
        object1.setRevision(generator1Object.getRevision());
        assertEquals(object1, generator1Object);

        Generator object2 = (Generator) XmlConversion.ConvertClassFromXml(XMLprefix + generator2XML);
        // is null because referenced object is not in XML
        object2.setRevision(generator2Object.getRevision());
        assertEquals(object2, generator2Object);
    }

    @Test
    public void convertMethodToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(method1Object);
        assertEquals(xml1, XMLprefix + method1XML);
        String xml2 = XmlConversion.ConvertClassToXml(method2Object);
        assertEquals(xml2, XMLprefix + method2XML);
    }

    @Test
    public void convertMethodFromXml() throws ConversionException {

        Method object1 = (Method) XmlConversion.ConvertClassFromXml(XMLprefix + method1XML);
        // is null because referenced object is not in XML
        object1.setRevision(method1Object.getRevision());
        assertEquals(object1, method1Object);

        Method object2 = (Method) XmlConversion.ConvertClassFromXml(XMLprefix + method2XML);
        // is null because referenced object is not in XML
        object2.setRevision(method2Object.getRevision());
        assertEquals(object2, method2Object);
    }

    @Test
    public void convertMeasurementToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(measurement1Object);
        assertEquals(xml1, XMLprefix + measurement1XML);
        String xml2 = XmlConversion.ConvertClassToXml(measurement2Object);
        assertEquals(xml2, XMLprefix + measurement2XML);
    }

    @Test
    public void convertMeasurementFromXml() throws ConversionException {

        Measurement object1 = (Measurement) XmlConversion.ConvertClassFromXml(XMLprefix + measurement1XML);
        // is null because referenced object is not in XML
        object1.setGenerator(measurement1Object.getGenerator());
        object1.setMethod(measurement1Object.getMethod());
        assertEquals(object1, measurement1Object);

        Measurement object2 = (Measurement) XmlConversion.ConvertClassFromXml(XMLprefix + measurement2XML);
        // is null because referenced object is not in XML
        object2.setGenerator(measurement2Object.getGenerator());
        object2.setMethod(measurement2Object.getMethod());
        assertEquals(object2, measurement2Object);
    }

    @Test
    public void convertComparisonToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(comparison1Object);
        assertEquals(xml1, XMLprefix + comparison1XML);
        String xml2 = XmlConversion.ConvertClassToXml(comparison2Object);
        assertEquals(xml2, XMLprefix + comparison2XML);
    }

    @Test
    public void convertComparisonFromXml() throws ConversionException {

        Comparison object1 = (Comparison) XmlConversion.ConvertClassFromXml(XMLprefix + comparison1XML);
        // is null because referenced object is not in XML
        object1.setLeftMeasurement(comparison1Object.getLeftMeasurement());
        object1.setRightMeasurement(comparison1Object.getRightMeasurement());
        assertEquals(object1, comparison1Object);
        Comparison object2 = (Comparison) XmlConversion.ConvertClassFromXml(XMLprefix + comparison2XML);
        // is null because referenced object is not in XML
        object2.setLeftMeasurement(comparison2Object.getLeftMeasurement());
        object2.setRightMeasurement(comparison2Object.getRightMeasurement());
        assertEquals(object2, comparison2Object);
    }

    @Test
    public void convertExpressionToXml() throws ConversionException {

        String xml1 = XmlConversion.ConvertClassToXml(expression1Object);
        assertEquals(xml1, XMLprefix + expression1XML);
        String xml2 = XmlConversion.ConvertClassToXml(expression2Object);
        assertEquals(xml2, XMLprefix + expression2XML);
    }

    @Test
    public void convertExpressionFromXml() throws ConversionException {

        Expression object1 = (Expression) XmlConversion.ConvertClassFromXml(XMLprefix + expression1XML);
        // is null because referenced object is not in XML
        ((Comparison) object1.getLeft()).setLeftMeasurement(((Comparison) expression1Object.getLeft()).getLeftMeasurement());
        ((Comparison) object1.getLeft()).setRightMeasurement(((Comparison) expression1Object.getLeft()).getRightMeasurement());
        ((Comparison) object1.getRight()).setLeftMeasurement(((Comparison) expression1Object.getRight()).getLeftMeasurement());
        ((Comparison) object1.getRight()).setRightMeasurement(((Comparison) expression1Object.getRight()).getRightMeasurement());
        assertEquals(object1, expression1Object);

        Expression object2 = (Expression) XmlConversion.ConvertClassFromXml(XMLprefix + expression2XML);
        // is null because referenced object is not in XML
        ((Comparison) object2.getLeft()).setLeftMeasurement(((Comparison) expression2Object.getLeft()).getLeftMeasurement());
        ((Comparison) object2.getLeft()).setRightMeasurement(((Comparison) expression2Object.getLeft()).getRightMeasurement());
        ((Comparison) object2.getRight()).setLeftMeasurement(((Comparison) expression2Object.getRight()).getLeftMeasurement());
        ((Comparison) object2.getRight()).setRightMeasurement(((Comparison) expression2Object.getRight()).getRightMeasurement());
        assertEquals(object2, expression2Object);
    }

    @Test
    public void convertInfoToXml() throws ConversionException, SAXException, IOException {
        String xml = XmlConversion.ConvertClassToXml(info1Object);
        Diff diff = new Diff(XMLprefix + info1XML, xml);
        diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());

        if (!diff.similar()) {
            System.err.println(diff);
        }

        assertTrue(diff.similar());
    }

    @Test
    public void convertInfoFromXml() throws ConversionException {
        Info object = (Info) XmlConversion.ConvertClassFromXml(XMLprefix + info1XML);
        assertEquals(object, info1Object);
    }

    @Test
    public void testExampleFile() throws IOException, ConversionException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader("src" + File.separator + "examples" + File.separator + "info.xml"));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
                sb.append('\n');
            }
            Info object = (Info) XmlConversion.ConvertClassFromXml(sb.toString());

            assertEquals(info1Object, object);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void convertMachineToXml() throws ConversionException {
        String xml = XmlConversion.ConvertClassToXml(machineObject);
        assertEquals(XMLprefix + machineXML, xml);
    }

    @Test
    public void convertMachineFromXml() throws ConversionException {
        Machine object = (Machine) XmlConversion.ConvertClassFromXml(XMLprefix + machineXML);
        assertEquals(object, machineObject);
    }

    @SuppressWarnings("deprecation")
    private Method createMethod(String id, Revision revision, String path, String parameter, String name) {
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
    private Project createProject(String id, String alias, Build build, Repository repository) {
        Project project = new Project();
        project.setId(id);
        project.setAlias(alias);
        project.setBuild(build);
        project.setRepository(repository);
        return project;
    }

    @SuppressWarnings("deprecation")
    private Revision createRevision(String id, String alias, String value, Repository repository) {
        Revision revision = new Revision();
        revision.setId(id);
        revision.setAlias(alias);
        revision.setValue(value);
        repository.addRevision(revision);
        return revision;
    }
}
