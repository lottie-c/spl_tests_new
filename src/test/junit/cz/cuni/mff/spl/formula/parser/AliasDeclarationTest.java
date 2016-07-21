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
package cz.cuni.mff.spl.formula.parser;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.parser.Parser.SplittedAliasDeclaration;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Tests parser on declaration aliases.
 * 
 * @author Martin Lacina
 * @author Jaroslav Kotrc
 * 
 */
public class AliasDeclarationTest {

    private static final SplLog LOG = SplLogger.getLogger(AliasDeclarationTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    ParserContext context;

    @Before
    public void initContext() {
        // init parser context
        context = new ParserContext();
        // add parameter "x"
        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put("x", 77.0);
        map.put("y", 11.11);
        context.putAllParameters(map);
        // setup THIS project with three revisions
        Project project = new Project();
        project.setAlias("THIS");
        project.setRepository(new Repository("test", null));
        project.addRevision(createRevision("HEAD", "getting THIS revision HEAD", project));
        project.addRevision(createRevision("first", "getting revision first", project));
        project.addRevision(createRevision("second", "getting revision second", project));
        context.addProject("THIS", project);
        // setup THIS project with three revisions
        project = new Project();
        project.setAlias("testingrepository1");
        project.setRepository(new Repository("test", null));
        project.addRevision(createRevision("HEAD", "getting testingrepository1 revision HEAD", project));
        project.addRevision(createRevision("first", "getting testingrepository1 revision first", project));
        project.addRevision(createRevision("second", "getting testingrepository1 revision second", project));
        context.addProject("testingrepository1", project);

        // setup SELF method
        context.addMethod("SELF",
                Method.createMethod(null, null, "pkg.selfMethodPkg", null, "selfMethod", new ArrayList<String>(), DeclarationType.WITHOUT_PARAMETERS, context));
        // setup other method
        context.addMethod("other",
                Method.createMethod(null, "first", "pkg.otherMethodPkg", null, "otherMethod", null, DeclarationType.WITHOUT_PARAMETERS, context));

        // setup machine where measurements are executed
        context.setMachine(new Machine("testingMachine", "PC1"));
    }

    private static Revision createRevision(String alias, String value, Project project) {
        Revision revision = new Revision();
        revision.setAlias(alias);
        revision.setValue(value);
        return revision;
    }

    /**
     * Runs test on method alias.
     * 
     * @param index
     *            Index to {@link #methodAliasesTests}.
     * @throws ParseException
     */
    private void testParseGeneratorAlias(String str) throws ParseException {
        try {
            LOG.info(str);
            Assert.assertNotNull(Parser.parseGeneratorAlias(str, context));
            context.printErrors();
        } catch (ParseException e) {
            Assert.fail("'" + str + "' failed: " + e.getMessage());
        }
    }

    @Test
    public void parseInstanceGeneratorAliasWithoutStrings() throws ParseException {
        String testValue = "instanceGen2=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGenerator('100')";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseInstanceGeneratorAliasWithStrings() throws ParseException {
        String testValue = "instanceGen1=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGenerator()";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseInstanceFactoryGeneratorAliasWithoutStrings() throws ParseException {
        String testValue = "instanceFactoryGen1=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorInstanceFactory('100')#factory()";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseInstanceFactoryGeneratorAliasWithStrings() throws ParseException {
        String testValue = "instanceFactoryGen2=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorInstanceFactory('100')#factory('100')";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseStaticFactoryGeneratorAliasWithoutStrings() throws ParseException {
        String testValue = "staticFactoryGen1=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorStaticFactory#factory()";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseStaticFactoryGeneratorAliasWithStrings() throws ParseException {
        String testValue = "staticFactoryGen2=testingrepository1@second:"
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorStaticFactory#factory('100')";
        testParseGeneratorAlias(testValue);
    }

    @Test
    public void parseCipherGeneratorAlias() throws ParseException {
        String testValue = "gen=testingrepository1@second:cz.cuni.mff.d3s.spl.examples.SplPaperCiphers#cipherGen()";
        testParseGeneratorAlias(testValue);
    }

    /**
     * Runs test on method alias.
     * 
     * @param index
     *            Index to {@link #methodAliasesTests}.
     * @throws ParseException
     */
    private void testParseMethodAlias(String str) throws ParseException {
        try {
            LOG.info(str);
            Assert.assertNotNull(Parser.parseMethodAlias(str, context));
            context.printErrors();
        } catch (ParseException e) {
            Assert.fail("'" + str + "' failed: " + e.getMessage());
        }
    }

    @Test
    public void parseStaticMethodAlias() throws ParseException {
        String testValue = "staticMethod=testingrepository2"
                + "cz.cuni.mff.d3s.spl.tools.basictests.classes."
                + "IntegerSumCalculatorStatic#calculateSumOfIntegers()";
        testParseMethodAlias(testValue);
    }

    @Test
    public void parseInstanceMethodAlias() throws ParseException {
        String testValue = "instanceMethod="
                + "cz.cuni.mff.d3s.spl.tools.basictests.classes."
                + "IntegerSumCalculator('ignored')#calculateSumOfIntegers()";
        testParseMethodAlias(testValue);
    }

    @Test
    public void parseRijndaelMethodAlias() throws ParseException {
        String testValue = "rijndael=testingrepository1:cz.cuni.mff.d3s.spl.examples.SplPaperCiphersJce('AES:128')#cipher()";
        testParseMethodAlias(testValue);
    }

    @Test
    public void parseBlowfishMethodAlias() throws ParseException {
        String testValue = "blowfish=testingrepository1:cz.cuni.mff.d3s.spl.examples.SplPaperCiphersJce('Blowfish:128')#cipher()";
        testParseMethodAlias(testValue);
    }

    /**
     * Tests splitting alias declaration into two parts- name and declaration.
     */
    @Test
    public void testAliasSplitting() throws ParseException {
        String name = "alfa";
        String declaration = "testingrepository1:cz.cuni.mff.d3s.spl.examples.SplPaperCiphersJce('AES:128')#cipher";
        String text = name + '=' + declaration;

        SplittedAliasDeclaration splitted = Parser.splitAliasNameAndDefinition(text);
        Assert.assertEquals(name, splitted.getAliasName());
        Assert.assertEquals(declaration, splitted.getAliasDeclaration());
    }
}
