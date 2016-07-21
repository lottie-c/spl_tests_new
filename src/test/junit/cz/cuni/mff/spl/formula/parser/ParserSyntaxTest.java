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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;

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
import cz.cuni.mff.spl.formula.expander.Expander.ExpanderException;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Testing syntax of formula. Test passes if no exception is thrown.
 * 
 * @author Jaroslav Kotrc
 * 
 */
public class ParserSyntaxTest {

    private static final SplLog LOG = SplLogger.getLogger(ParserSyntaxTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    private void parseFormula(String formula) throws ParseException, ExpanderException {
        LOG.info(formula);

        // init parser context
        ParserContext context = new ParserContext();
        // add parameter "x"
        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put("x", 77.0);
        map.put("y", 11.11);
        map.put("CPU", 222.0);
        map.put("THREADS", 333.0);
        map.put("BFLM", 444.4);
        context.putAllParameters(map);
        // setup THIS project with two revisions
        Project project = new Project();
        project.setAlias("THIS");
        project.setRepository(new Repository("test", null));
        project.addRevision(createRevision("HEAD", "getting THIS revision HEAD", project));
        project.addRevision(createRevision("first", "getting revision first", project));
        context.addProject("THIS", project);
        // setup otherproject project with two revisions
        project = new Project();
        project.setAlias("otherproject");
        project.setRepository(new Repository("otherproject", null));
        project.addRevision(createRevision("HEAD", "getting otherproject revision HEAD", project));
        project.addRevision(createRevision("second", "getting revision second", project));
        context.addProject("otherproject", project);

        // setup SELF method
        context.addMethod("SELF",
                Method.createMethod(null, null, "pkg.selfMethodPkg", null, "selfMethod", new ArrayList<String>(), DeclarationType.WITHOUT_PARAMETERS, context));
        // setup other method
        context.addMethod("other",
                Method.createMethod(null, "first", "pkg.otherMethodPkg", null, "otherMethod", null, DeclarationType.WITHOUT_PARAMETERS, context));

        // setup machine where measurements are executed
        context.setMachine(new Machine("testingMachine", "PC1"));

        // parse formula
        Parser.parseAndExpandFormula(formula, context);
        context.printErrors();
    }

    private static Revision createRevision(String alias, String value, Project project) {
        Revision revision = new Revision();
        revision.setAlias(alias);
        revision.setValue(value);
        return revision;
    }

    /**
     * Test method checking syntax by parsing simple formula with more
     * subformulas in the form A & B | ( C ==> B). Test passes when no exception
     * is thrown.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseSubformulasFormula() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method[Generator1()]" + "& SELF[Generator2()] <= pckg.AClass#method[Generator2()]" + "| ("
                + "SELF[Generator1()] <= pckg.AClass#method()[Generator2()]" + "==>" + "SELF[Generator2()] <= pckg.AClass#method()[Generator2()]" + ")";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with aliases.
     * Testing one constructor variable and measurement multiplier with real
     * number.
     * Test passes when no exception is thrown.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseSimpleFormula() throws ParseException, ExpanderException {
        String str = "for (j {100}) SELF[instanceGen1](j) <=(3,10e-3) other[instanceGen1](j)";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula without aliases.
     * Testing full name for generator as class instance, two constructor
     * variables and two measure multipliers. Second method have full name too
     * and have fully specified generator as factory method of instantiated
     * class.
     * Test passes when no exception is thrown.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseSimpleFormulaWithoutAliases() throws ParseException, ExpanderException {
        String str = "for (j {100} k{1,2}) SELF[" + "cz.cuni.mff.d3s.spl.tools.basictests.generators.IntegerGenerator()" + "](j) <=(1,10e-3) "
                + "cz.cuni.mff.d3s.spl.tools.basictests.classes.IntegerSumCalculator('ignored')#calculateSumOfIntegers()" + "["
                + "cz.cuni.mff.d3s.spl.tools.basictests.generators.IntegerGeneratorInstanceFactory('100')#factory('100')" + "](j, k)";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with generators and
     * methods from other projects and revisions. One generator is as factory
     * method from static class from other project, second is the same but with
     * variable for factory method and from THIS project but other revision.
     * Second method is from static class.
     * Test passes when no exception is thrown.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseRemoteFormula() throws ParseException, ExpanderException {
        String str = "for (j {100})" + " SELF[ " + "otherproject@second:" + "cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorStaticFactory#factory()" + "](j) <= " + "otherproject:" + "cz.cuni.mff.d3s.spl.tools.basictests.classes."
                + "IntegerSumCalculatorStatic#calculateSumOfIntegers()" + "[" + "@first:cz.cuni.mff.d3s.spl.tools.basictests.generators."
                + "IntegerGeneratorStaticFactory#factory('100')" + "](j)";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula without generators.
     * This should raise ParseException because generator has to be specified.
     * 
     * @throws ExpanderException
     */
    @Test(expected = ParseException.class)
    public void testParseFormulaWithoutGenerators() throws ParseException, ExpanderException {
        String str = "SELF <= pckg.AClass#method()";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * primitive parameter.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodWithOnePrimitiveParameter() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(int)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * class parameter.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodWithOneClassParameter() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(java.lang.String)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * primitive parameter and parameter name.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodWithOnePrimitiveParameterAndName() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(int intName)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * class parameter and parameter name.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodWithOneClassParameterAndName() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(java.lang.String stringName)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * primitive and class parameters, arrays and some parameter names.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodWithMultipleParameters() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(java.lang.String stringName, java.lang.String, int idx, double[][][], java.lang.Integer[] arr)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * primitive array parameter.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodPrimitiveArrayParameter() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(int[])[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * class array parameter.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodClassArrayParameter() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(java.lang.String[])[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking syntax by parsing simple formula with method with
     * primitive array parameter and name.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseMethodPrimitiveArrayParameterAndName() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(int[] arr)[Generator1()]";
        parseFormula(str);
    }

    /**
     * Test method checking that parser detects that one variable is declared
     * twice.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseWithDuplicatedVariableDeclaration() throws ExpanderException {
        // simple original, simple duplicated variable
        String str = "for(i{1,2} i{3,4}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable i already defined as a variable\n", e.getMessage());
        }

        // simple original, duplicated variable in the sequence
        str = "for(i{1,2} (i,b){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable i already defined as a variable\n", e.getMessage());
        }

        // original in the sequence, simple duplicated variable
        str = "for( (i,b){(1,2), (2,4)} i{1,2}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable i already defined in sequence of variables\n", e.getMessage());
        }

        // original in the sequence, duplicated variable in the sequence
        str = "for( (i,b){(1,2), (2,4)}  (a,b){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable b already defined in sequence of variables\n", e.getMessage());
        }

        // original in the sequence, duplicated variable in the same sequence
        str = "for( (i,i){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable i already defined in the same sequence\n", e.getMessage());
        }

        // original as a parameter, duplicated variable in sequence
        str = "for( (i,x){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable x already defined as a parameter\n", e.getMessage());
        }

        // original as a parameter, simple duplicated variable
        str = "for(x{1,2}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        try {
            parseFormula(str);
            fail("ParseException should be thrown");
        } catch (ParseException e) {
            assertEquals("Encountered 1 errors during parsing:\nVariable x already defined as a parameter\n", e.getMessage());
        }
    }

    /**
     * Test method checking that parser syntax pass sequence variables.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseWithSequenceVariables() throws ParseException, ExpanderException {
        // single sequence variable
        String str = "for((a,b){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        parseFormula(str);
        // combined sequence and normal variable
        str = "for(i{1,2} (a,b){(1,2), (2,4)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        parseFormula(str);
        // combined three sequences
        str = "for((a,b){(1,2), (2,4)} (c,d){(1,2,3), (2,4,6)}" +
                "(e,f,g){(1, 2), (2, 4), (3, 6)}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        parseFormula(str);
    }

    /**
     * Tests simple formula without variables declaration with the same
     * measurement on both side of comparison and lambda expression.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testLambdaExpression() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= (1*2e+4*CPU, THREADS*28.12*3*4*BFLM) SELF[Generator1()]";
        parseFormula(str);

        // // Testing not expanded lambda parameter //////////
        // // now lambda is expanded so it does not have name in expanded
        // structure
        /* Parser parser = new Parser(str);
         System.out.println(str);

         ParserStructure structure = parser.parseFormula(initContext());
         ParserContext context = parser.getContext();
         context.printErrors();
         assertEquals(0, context.getErrors().size());

         Formula formula = Expander.expandStructure(structure, context);
         context.printErrors();

         Lambda leftLambda = ((Comparison) formula).getLeftLambda();
         List<Double> constants = leftLambda.getConstants();
         List<String> variables = leftLambda.getParameters();
         assertEquals(2, constants.size());
         assertEquals(1, variables.size());
         assertEquals(1, constants.get(0), 0);
         assertEquals(2e+4, constants.get(1), 0);
         assertEquals("CPU", variables.get(0));

         Lambda rightLambda = ((Comparison) formula).getRightLambda();
         constants = rightLambda.getConstants();
         variables = rightLambda.getParameters();
         assertEquals(3, constants.size());
         assertEquals(2, variables.size());
         assertEquals(28.12, constants.get(0), 0);
         assertEquals(3, constants.get(1), 0);
         assertEquals(4, constants.get(2), 0);
         assertEquals("THREADS", variables.get(0));
         assertEquals("BFLM", variables.get(1));*/
    }

    @Test
    public void testParseGeneratorHasNumbersDirectly() throws ParseException, ExpanderException {
        String str = "for(i{1,2}) SELF[Generator1()](CPU,7) <= pckg.AClass#method()[Generator1()](1,2,3)";
        parseFormula(str);
    }
}
