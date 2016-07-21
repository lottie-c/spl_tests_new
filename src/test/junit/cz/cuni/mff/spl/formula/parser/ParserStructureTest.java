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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.ExpandedVariable;
import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.GeneratorMethod;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.Machine;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.Method.DeclarationType;
import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.annotation.Project;
import cz.cuni.mff.spl.annotation.Repository;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.expander.Expander.ExpanderException;
import cz.cuni.mff.spl.utils.ReturningSet;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * @author Jaroslav Kotrc
 * 
 */
public class ParserStructureTest {

    private static final SplLog LOG = SplLogger.getLogger(ParserStructureTest.class);

    @Before
    public void init() {
        LogManager.getRootLogger().setLevel(Level.FATAL);
    }

    /**
     * Initializing parser context.
     * 
     * @return Initialized parser context
     */
    private static ParserContext initContext() {
        ParserContext context = new ParserContext();
        // add parameter "x"
        HashMap<String, Double> map = new HashMap<String, Double>();
        map.put("x", 77.0);
        map.put("y", 11.11);
        context.putAllParameters(map);
        // setup THIS project with two revisions
        Project project = new Project();
        project.setAlias("THIS");
        project.setRepository(new Repository("test", null));
        project.addRevision(createRevision("HEAD", "getting revision HEAD", project));
        project.addRevision(createRevision("first", "getting revision first", project));
        context.addProject("THIS", project);

        // setup SELF method
        context.addMethod("SELF",
                Method.createMethod(null, null, "pkg.selfMethodPkg", null, "selfMethod", new ArrayList<String>(), DeclarationType.WITHOUT_PARAMETERS, context));
        // setup other method
        context.addMethod("other",
                Method.createMethod(null, "first", "pkg.otherMethodPkg", null, "otherMethod", null, DeclarationType.WITHOUT_PARAMETERS, context));

        // setup generator instanceGen1
        context.addGenerator("instanceGen1", Generator.createGenerator(null, null, "pkg.generatorPkg", null, new GeneratorMethod("genMethod", null), context));

        // setup machine where measurements are executed
        context.setMachine(new Machine("testingMachine", "PC1"));

        return context;
    }

    private static Revision createRevision(String alias, String value, Project project) {
        Revision revision = new Revision();
        revision.setAlias(alias);
        revision.setValue(value);
        return revision;
    }

    /**
     * Test method for parsing simple formula with more subformulas in the form
     * A & B | ( C ==> B). Checking for formula tree structure.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testSubformulasFormula() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method[Generator1()]" + "& SELF[Generator2()] <= pckg.AClass#method[Generator2()]" + "| ("
                + "SELF[Generator1()] <= pckg.AClass#method()[Generator2()]" + "==>" + "SELF[Generator2()] <= pckg.AClass#method()[Generator2()]" + ")";
        LOG.info(str);
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        context.printErrors();

        assertTrue(formula instanceof Expression);
        Expression expr = (Expression) formula;

        assertEquals(Operator.OR, expr.getOperator());

        // left subtree
        assertTrue(expr.getLeft() instanceof Expression);
        Expression leftExpr = (Expression) expr.getLeft();
        assertEquals(leftExpr.getOperator(), Operator.AND);

        assertTrue(leftExpr.getLeft() instanceof Comparison);
        assertTrue(leftExpr.getRight() instanceof Comparison);

        // right subtree
        assertTrue(expr.getRight() instanceof Expression);
        Expression rightExpr = (Expression) expr.getRight();
        assertEquals(rightExpr.getOperator(), Operator.IMPL);

        assertTrue(rightExpr.getLeft() instanceof Comparison);
        assertTrue(rightExpr.getRight() instanceof Comparison);

        for (Method m : context.getMethods()) {
            LOG.info("Method: "
                    + String.format("id = %s\tproject = %s\trevision = %s\tpath = %s\tname = %s\t parameter = %s", m.getId(), m.getRevision().getProject()
                            .getRepository().toString(), m.getRevision().getValue(), m.getPath(), m.getName(), m.getParameter()));
        }
    }

    /**
     * Tests simple formula with same measurement on both side of comparison.
     * Checks if context identifies that method, generator and measurements are
     * the same and stores it without duplications.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testEqualsMeasurementsFormula() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= (1, 3) SELF[Generator1()]";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        assertEquals(2, context.getGenerators().size());
        assertEquals(2, context.getMethods().size());
        assertEquals(1, context.getMeasurements().size());

        Comparison cmp = (Comparison) formula;
        assertEquals((Double) 1.0, cmp.getLeftLambda().getConstants().get(0));
        assertEquals((Double) 3.0, cmp.getRightLambda().getConstants().get(0));

        assertTrue(cmp.getLeftMeasurement() == cmp.getRightMeasurement());

        for (Method m : context.getMethods()) {
            LOG.info("Method: "
                    + String.format("id = %s\tproject = %s\trevision = %s\tpath = %s\tname = %s\t parameter = %s", m.getId(), m.getRevision().getProject()
                            .getRepository().toString(), m.getRevision().getValue(), m.getPath(), m.getName(), m.getParameter()));
        }
    }

    /**
     * Tests simple formula without variables declaration with the same
     * measurement on both side of comparison that use not declared variable
     * and checks if expander recognize that as an error.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testNotDeclaredVariable() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()](i) <= (1,3) SELF[Generator1()](i)";
        ParserContext context = initContext();

        boolean expanderExceptionThrown = false;
        try {
            Parser.parseAndExpandFormula(str, context);
        } catch (ParseException e) {
            if (e.getCause() instanceof ExpanderException) {
                expanderExceptionThrown = true;
            }
        }

        assertTrue("Expander exception was not thrown", expanderExceptionThrown);

        assertEquals(0, context.getWarnings().size());
        assertEquals(1, context.getErrors().size());
        assertEquals(context.getErrors().iterator().next().getText(), "Variable i was not declared");

        for (Method m : context.getMethods()) {
            LOG.info("Method: "
                    + String.format("id = %s\tproject = %s\trevision = %s\tpath = %s\tname = %s\t parameter = %s", m.getId(), m.getRevision().getProject()
                            .getRepository().toString(), m.getRevision().getValue(), m.getPath(), m.getName(), m.getParameter()));
        }
    }

    /**
     * Test of expanding single variable with one value for measurements.
     * Declared variable is not used in formula body. Checks that expanding
     * proceeds without error and expanded variables have not any values.
     * Also checks that warning about unused variable is risen.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testSingleVariableSingleValueExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1})  SELF[Generator1()] <= pckg.AClass#method[Generator2()]";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(1, context.getWarnings().size());
        assertEquals("The value of the variable i is not used", context.getWarnings().iterator().next().getText());

        for (Method m : context.getMethods()) {
            LOG.info("Method: "
                    + String.format("id = %s\tproject = %s\trevision = %s\tpath = %s\tname = %s\t parameter = %s", m.getId(), m.getRevision().getProject()
                            .getRepository().toString(), m.getRevision().getValue(), m.getPath(), m.getName(), m.getParameter()));
        }

        ExpandedVariable param1 = ((ExpandedVariable) ((Comparison) formula).getLeftMeasurement().getVariable());
        ExpandedVariable param2 = ((ExpandedVariable) ((Comparison) formula).getRightMeasurement().getVariable());
        assertEquals(0, param1.getVariables().size());
        assertEquals(0, param2.getVariables().size());
    }

    /**
     * Test of expanding single variable with three values for measurements.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testSingleVariableMultipleValueExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1, 12, 123})  SELF[Generator1()](i) <= pckg.AClass#method()[Generator2()](i)";
        ParserContext context = initContext();

        Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        // checks how many measurements have variables with declared values
        int i1 = 0, i2 = 0, i3 = 0;
        assertEquals(6, context.getMeasurements().size());
        for (Measurement m : context.getMeasurements()) {
            List<Integer> p1 = ((ExpandedVariable) m.getVariable()).getVariables();
            assertEquals(1, p1.size());
            switch (p1.get(0).intValue()) {
                case 1:
                    ++i1;
                    break;
                case 12:
                    ++i2;
                    break;
                case 123:
                    ++i3;
                    break;
            }
        }

        assertEquals(2, i1);
        assertEquals(2, i2);
        assertEquals(2, i3);
    }

    /**
     * Test of expanding two variables both with one value for measurements.
     * Variables are used in measurements at both sides of measurement but in
     * the second measurement they have switched order (1. is 2. and 2. is 1.).
     * Checks that expanding proceeds without error and expanded variables have
     * correct values in the expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testMultipleVariableSingleValueExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1} j{12})  SELF[Generator1()](i,j) <= pckg.AClass#method()[Generator2()](j, i)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ExpandedVariable param1 = ((ExpandedVariable) ((Comparison) formula).getLeftMeasurement().getVariable());
        ExpandedVariable param2 = ((ExpandedVariable) ((Comparison) formula).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(1, 12))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(12, 1))));
    }

    /**
     * Test of expanding variable and parameter both with one value for
     * measurements.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values in the expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testVariableAndParameterExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1})  SELF[Generator1()](i,x) <= pckg.AClass#method()[Generator2()](x, i)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ExpandedVariable param1 = ((ExpandedVariable) ((Comparison) formula).getLeftMeasurement().getVariable());
        ExpandedVariable param2 = ((ExpandedVariable) ((Comparison) formula).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(1, 77))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(77, 1))));
    }

    /** Stores two value of integers to be used as a key into map */
    private class VariablePair {
        public int i, j;

        /**
         * @param i
         * @param j
         */
        public VariablePair(int i, int j) {
            super();
            this.i = i;
            this.j = j;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + i;
            result = prime * result + j;
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
            VariablePair other = (VariablePair) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (i != other.i) {
                return false;
            }
            if (j != other.j) {
                return false;
            }
            return true;
        }

        private ParserStructureTest getOuterType() {
            return ParserStructureTest.this;
        }
    }

    /**
     * Stores map of variable pairs as keys and number of its appearance and
     * number of logical AND in the formula
     */
    private class VariableCount {
        public HashMap<VariablePair, Integer> map    = new HashMap<VariablePair, Integer>();
        public int                            andCnt = 0;
    }

    /**
     * Go through formula tree and count number of logical AND and variable pair
     * appearances. Every measurement should have two variables - variable pair
     * and this method will count number of the variable pairs appearances.
     * 
     * @param formula
     *            formula that will be checked
     * @param varCnt
     *            class for storing found numbers
     */
    private void checkFormula(Formula formula, VariableCount varCnt) {
        if (formula instanceof Expression) {
            Expression expr = (Expression) formula;
            assertEquals(expr.getOperator(), Operator.AND);
            ++varCnt.andCnt;
            checkFormula(expr.getLeft(), varCnt);
            checkFormula(expr.getRight(), varCnt);
        } else if (formula instanceof Comparison) {
            Comparison cmp = (Comparison) formula;
            ExpandedVariable param = (ExpandedVariable) cmp.getLeftMeasurement().getVariable();
            checkVariable(varCnt, param);
            param = (ExpandedVariable) cmp.getRightMeasurement().getVariable();
            checkVariable(varCnt, param);
        } else {
            assertTrue(false);
        }
    }

    /**
     * Checks that passed variable has exactly two values and increase its
     * number in the map of value pairs and its appearances count.
     * 
     * @param varCnt
     *            class storing map of variables pairs and its count
     * @param variable
     *            variable to be checked
     */
    private void checkVariable(VariableCount varCnt, ExpandedVariable variable) {
        assertEquals(2, variable.getVariables().size());
        int i = variable.getVariables().get(0).intValue();
        int j = variable.getVariables().get(01).intValue();
        VariablePair pair = new VariablePair(i, j);
        Integer cnt = varCnt.map.get(pair);
        if (cnt == null) {
            assertTrue(false);
        } else {
            varCnt.map.put(pair, cnt + 1);
        }
    }

    /**
     * Test of expanding two variables both with two values for measurements.
     * Variables are used in measurements at both sides of measurement but in
     * the second measurement they have switched order (1. is 2. and 2. is 1.).
     * Checks that expanding proceeds without error. Checks if expanded
     * variables of the comparison have correct values in the
     * expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testMultipleVariableMultipleValueExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1, 11} j{12, 22})  SELF[Generator1()](i,j) <= pckg.AClass#method()[Generator2()](j, i)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        VariableCount cnt = new VariableCount();
        ArrayList<VariablePair> pairs = new ArrayList<VariablePair>();
        pairs.add(new VariablePair(1, 12));
        pairs.add(new VariablePair(1, 22));
        pairs.add(new VariablePair(11, 12));
        pairs.add(new VariablePair(11, 22));
        pairs.add(new VariablePair(12, 1));
        pairs.add(new VariablePair(22, 1));
        pairs.add(new VariablePair(12, 11));
        pairs.add(new VariablePair(22, 11));

        for (VariablePair pair : pairs) {
            cnt.map.put(pair, 0);
        }
        checkFormula(formula, cnt);

        assertEquals(3, cnt.andCnt);
        for (VariablePair pair : pairs) {
            assertEquals(1, (int) cnt.map.get(pair));
        }
    }

    /**
     * Test of expanding one sequence variable. Sequence has two variables and
     * two values.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values in the expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testSingleSequenceVariableMultipleValueExpansion() throws ParseException, ExpanderException {
        String str = "for((a,b){(1,2), (3,4)})  SELF[Generator1()](a) <= pckg.AClass#method()[Generator2()](b)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ExpandedVariable param1 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getLeft()).getLeftMeasurement().getVariable());
        ExpandedVariable param2 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getLeft()).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(1))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(3))));

        param1 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getRight()).getLeftMeasurement().getVariable());
        param2 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getRight()).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(2))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(4))));
    }

    /**
     * Test of expanding two sequence variable. First sequence has two variables
     * and two values.
     * Second sequence has two variables and one value.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values in the expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testMultipleSequenceVariableMultipleValueExpansion() throws ParseException, ExpanderException {
        String str = "for((a,b){(1,2), (2,4)} (c,d){(11), (22)})  SELF[Generator1()](a, c) <= pckg.AClass#method()[Generator2()](d,b)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ExpandedVariable param1 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getLeft()).getLeftMeasurement().getVariable());
        ExpandedVariable param2 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getLeft()).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(1, 11))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(22, 2))));

        param1 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getRight()).getLeftMeasurement().getVariable());
        param2 = ((ExpandedVariable) ((Comparison) ((Expression) formula).getRight()).getRightMeasurement().getVariable());
        assertThat(param1.getVariables(), is(equalTo(Arrays.asList(2, 11))));
        assertThat(param2.getVariables(), is(equalTo(Arrays.asList(22, 4))));
    }

    /**
     * Test of expanding sequence variable and simple variable. Sequence has two
     * variables
     * and two values. Simple variable has two values.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values in the expected order.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testSequenceAndSimpleVariablesExpansion() throws ParseException, ExpanderException {
        String str = "for((a,b){(1,2), (3,4)} c{22,33})  SELF[Generator1()](a, c) <= pckg.AClass#method()[Generator2()](b,c)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        VariableCount cnt = new VariableCount();
        ArrayList<VariablePair> pairs = new ArrayList<VariablePair>();
        pairs.add(new VariablePair(1, 22));
        pairs.add(new VariablePair(3, 22));
        pairs.add(new VariablePair(1, 33));
        pairs.add(new VariablePair(3, 33));
        pairs.add(new VariablePair(2, 22));
        pairs.add(new VariablePair(4, 22));
        pairs.add(new VariablePair(2, 33));
        pairs.add(new VariablePair(4, 33));

        for (VariablePair pair : pairs) {
            cnt.map.put(pair, 0);
        }
        checkFormula(formula, cnt);

        assertEquals(3, cnt.andCnt);
        for (VariablePair pair : pairs) {
            assertEquals(1, (int) cnt.map.get(pair));
        }
    }

    /**
     * Test of expanding lambda variable and parameters.
     * Checks that expanding proceeds without error and expanded variables have
     * correct values.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testLambdaExpansion() throws ParseException, ExpanderException {
        String str = "for(i{1, 33})  SELF[Generator1()](i,x) <= (11*i, x*i*55)pckg.AClass#method()[Generator2()](x, i)";
        ParserContext context = initContext();

        Formula formula = Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        Lambda ll = ((Comparison) ((Expression) formula).getLeft()).getLeftLambda();
        Lambda lr = ((Comparison) ((Expression) formula).getLeft()).getRightLambda();
        Lambda rl = ((Comparison) ((Expression) formula).getRight()).getLeftLambda();
        Lambda rr = ((Comparison) ((Expression) formula).getRight()).getRightLambda();

        assertEquals(2, ll.getConstants().size());
        assertEquals(0, ll.getParameters().size());
        assertEquals(11, ll.getConstants().get(0).intValue());
        assertEquals(1, ll.getConstants().get(1).intValue());

        assertEquals(2, rl.getConstants().size());
        assertEquals(0, rl.getParameters().size());
        assertEquals(11, rl.getConstants().get(0).intValue());
        assertEquals(33, rl.getConstants().get(1).intValue());

        assertEquals(3, lr.getConstants().size());
        assertEquals(0, lr.getParameters().size());
        assertEquals(55, lr.getConstants().get(0).intValue());
        assertEquals(77, lr.getConstants().get(1).intValue());
        assertEquals(1, lr.getConstants().get(2).intValue());

        assertEquals(3, rr.getConstants().size());
        assertEquals(0, rr.getParameters().size());
        assertEquals(55, rr.getConstants().get(0).intValue());
        assertEquals(77, rr.getConstants().get(1).intValue());
        assertEquals(33, rr.getConstants().get(2).intValue());
    }

    /**
     * Checking that parser detects that parameter used in generator
     * declaration is double and rise correct warning.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseDoubleParameterUsedInGenerator() throws ParseException, ExpanderException {
        String str = "for(i{1,2}) SELF[instanceGen1](i,x) <= SELF[Generator1()](x,y)";
        ParserContext context = initContext();

        Parser.parseAndExpandFormula(str, context);

        assertEquals(0, context.getErrors().size());
        assertEquals(1, context.getWarnings().size());
        assertEquals("Parameter y is real number, it will be converted to integer.",
                context.getWarnings().iterator().next().getText());
    }

    /**
     * Checking that parser detects that generator is declared as class in
     * default package and rise correct warning.
     */
    @Test
    public void testDefaultPackageGenerator() throws ParseException, ExpanderException {
        String str = "for(i{1,2}) SELF[defaultPkgGenerator](i) <= SELF[Generator1()]";
        ParserContext context = initContext();

        Parser.parseAndExpandFormula(str, context);

        assertEquals(0, context.getErrors().size());
        assertEquals(1, context.getWarnings().size());
        assertEquals(
                "Generator defaultPkgGenerator is declared as class in default package. Did you mean not yet defined generator alias 'defaultPkgGenerator'?",
                context.getWarnings().iterator().next().getText());
    }

    /**
     * Checking that parser correctly parse method parameter names and its
     * values.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseParameterTypes() throws ParseException, ExpanderException {
        String str = "SELF[Generator1()] <= pckg.AClass#method(" +
                "java.lang.String stringName, int idx, double, java.lang.Integer," +
                "java.lang.String[][] stringName, int[] idx, double[][][], java.lang.Integer[]" +
                ")[Generator1()]";
        ParserContext context = initContext();

        Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ReturningSet<Method> methods = context.getMethods();
        for (Method method : methods) {
            if ("method".equals(method.getName())) {
                ArrayList<String> types = method.getParameterTypes();
                assertThat(types, is(equalTo(Arrays.asList("java.lang.String", "int", "double", "java.lang.Integer"
                        , "java.lang.String[][]", "int[]", "double[][][]", "java.lang.Integer[]"))));
            }
        }
    }

    /**
     * Checking that parser recognize that method does not have declared
     * parameter types and the second event is that it has declared zero
     * parameters.
     * 
     * @throws ExpanderException
     */
    @Test
    public void testParseParameterTypesDistinction() throws ParseException, ExpanderException {
        String str = "pckg.AClass#method1[Generator1()] <= pckg.AClass#method2()[Generator1()]";
        ParserContext context = initContext();

        Parser.parseAndExpandFormula(str, context);
        assertEquals(0, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());

        ReturningSet<Method> methods = context.getMethods();
        for (Method method : methods) {
            if ("method1".equals(method.getName())) {
                assertEquals(method.getDeclarated(), Method.DeclarationType.WITHOUT_PARAMETERS);
                assertEquals(0, method.getParameterTypes().size());
            } else if ("method2".equals(method.getName())) {
                assertEquals(method.getDeclarated(), Method.DeclarationType.WITH_PARAMETERS);
                assertEquals(0, method.getParameterTypes().size());
            }
        }
    }

    /**
     * Tests if parser will generate ParseException if variable was declared
     * twice.
     */
    @Test
    public void testThrownParseException() {
        String str = "for(i{1,2} i{3,4}) SELF[Generator1()] <= pckg.AClass#method()[Generator1()]";
        ParserContext context = initContext();

        try {
            Parser.parseAndExpandFormula(str, context);
        } catch (ParseException e) {
            String msg = e.getMessage();
            assertEquals("Encountered 1 errors during parsing:\nVariable i already defined as a variable\n", msg);
            if (e.getCause() instanceof ExpanderException) {
                Assert.fail("Wrong exception was thrown, expansion should not be executed.");
            }
        }
        assertEquals(1, context.getErrors().size());
        assertEquals(0, context.getWarnings().size());
    }

    /**
     * Tests if parser will parse lambda expression with or without interval
     * specification.
     * 
     * @throws ParseException
     */
    @Test
    public void testIntervalLambda() throws ParseException {
        String str = "SELF[Generator1()] = (2, 3, 0.1) SELF[Generator1()]";
        ParserContext context = initContext();
        Comparison cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertEquals(0.1, cmp.getInterval().doubleValue(), 0);
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(2.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(3.0))));

        str = "SELF[Generator1()] = (0.2) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertEquals(0.2, cmp.getInterval().doubleValue(), 0);
        assertNull(cmp.getLeftLambda());
        assertNull(cmp.getRightLambda());

        str = "SELF[Generator1()] = (x*4*0.5, 4*3, 0.3) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertEquals(0.3, cmp.getInterval().doubleValue(), 0);
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 0.5, 77.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 3.0))));

        str = "SELF[Generator1()] = (x, 4*33, 0.3) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertEquals(0.3, cmp.getInterval().doubleValue(), 0);
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(77.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 33.0))));

        str = "SELF[Generator1()] = (1*2*3, 4*5*x, 0.4) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertEquals(0.4, cmp.getInterval().doubleValue(), 0);
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(1.0, 2.0, 3.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 5.0, 77.0))));

        str = "SELF[Generator1()] = (1*2*3, 4*5*x) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertNull(cmp.getInterval());
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(1.0, 2.0, 3.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 5.0, 77.0))));

        str = "SELF[Generator1()] = (11, 4) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertNull(cmp.getInterval());
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(11.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0))));

        str = "SELF[Generator1()] = (x*2*3, 4*5*x) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertNull(cmp.getInterval());
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(2.0, 3.0, 77.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 5.0, 77.0))));

        str = "SELF[Generator1()] = (x, 4*5) SELF[Generator1()]";
        context = initContext();
        cmp = (Comparison) Parser.parseAndExpandFormula(str, context);
        assertNull(cmp.getInterval());
        assertThat(cmp.getLeftLambda().getConstants(), is(equalTo(Arrays.asList(77.0))));
        assertThat(cmp.getRightLambda().getConstants(), is(equalTo(Arrays.asList(4.0, 5.0))));
    }

}
