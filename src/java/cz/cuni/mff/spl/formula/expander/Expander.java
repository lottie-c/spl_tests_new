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
package cz.cuni.mff.spl.formula.expander;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Expression;
import cz.cuni.mff.spl.annotation.Formula;
import cz.cuni.mff.spl.annotation.Operator;
import cz.cuni.mff.spl.formula.context.ParserContext;
import cz.cuni.mff.spl.formula.context.ParserContext.Problem;
import cz.cuni.mff.spl.formula.parser.Parser.ParserStructure;
import cz.cuni.mff.spl.formula.parser.Parser.VariableSequence;

/**
 * Class for expanding formula created by parser with concrete values of
 * variables.
 * 
 * @author Jaroslav Kotrc
 */
public class Expander {

    /**
     * Interface for variable holders so different type of variables can be
     * stored in one array
     * 
     * @author Jaroslav Kotrc
     */
    private interface VariableHolder {
        /**
         * Fills variable value to the array of values. If given map is not null
         * then stores index of this value to the map where variable name points
         * to the index of its value in value array.
         * 
         * @param valuesArr
         *            array of values of all variables
         * @param valuesIdx
         *            index to the array of values where this variable entity
         *            starts
         * @param variableValueIdx
         *            index of the value of this variable
         * @param position
         *            mapping name of simple variable to its value index. In
         *            array of values is on this index stored value of variable.
         * @return return new index to the value array that points just after
         *         last value of this variable entity
         */
        public int fillValue(int[] valuesArr, int valuesIdx, int variableValueIdx, Map<String, Integer> position);

        /**
         * Returns number of values this variable has
         * 
         * @return number of values this variable has
         */
        public int valuesCount();
    }

    /**
     * Class for storing information about one variable. It contains variable
     * name and list of its values.
     * 
     * @author Jaroslav Kotrc
     */
    private static class SimpleVariableHolder implements VariableHolder {
        private final String             name;
        private final ArrayList<Integer> values;

        public SimpleVariableHolder(String name, ArrayList<Integer> values) {
            this.name = name;
            this.values = values;
        }

        @Override
        public int fillValue(int[] valuesArr, int valuesIdx, int variableValueIdx, Map<String, Integer> position) {
            if (position != null) {
                position.put(name, valuesIdx);
            }
            valuesArr[valuesIdx] = values.get(variableValueIdx);
            return valuesIdx + 1;
        }

        @Override
        public int valuesCount() {
            return values.size();
        }

    }

    /**
     * Class for storing information about one sequence variable.
     * 
     * @author Jaroslav Kotrc
     */
    private static class SequenceVariableHolder implements VariableHolder {
        private final VariableSequence sequence;

        /**
         * @param next
         */
        public SequenceVariableHolder(VariableSequence sequence) {
            this.sequence = sequence;
        }

        @Override
        public int fillValue(int[] valuesArr, int valuesIdx, int variableValueIdx, Map<String, Integer> position) {
            Set<Entry<String, Integer>> set = sequence.getEntrySet();
            int newValuesIdx = valuesIdx;
            for (Entry<String, Integer> entry : set) {
                if (position != null) {
                    position.put(entry.getKey(), newValuesIdx);
                }
                valuesArr[newValuesIdx] = sequence.getValue(entry.getValue(), variableValueIdx);
                ++newValuesIdx;
            }
            return newValuesIdx;
        }

        @Override
        public int valuesCount() {
            return sequence.valuesCount();
        }
    }

    /**
     * Expand formula with variables generated by parser to form where every
     * variable have concrete value. Also add all generators, methods and
     * concrete measurements into parser context where they are stored without
     * duplications.
     * 
     * Provided parse context can not contain any errors in time of call.
     * 
     * Errors and warnings that occurred during expansion are stored in provided
     * parser context. When errors are found, than {@link ExpanderException} is
     * thrown.
     * 
     * @param structure
     *            Structure generated by parser.
     * @param context
     *            Structure for storing measurements. Can not contain any errors
     *            in time of of call. Found errors and warnings are stored
     *            in this variable.
     * @return Expanded formula.
     * 
     * @throws IllegalStateException
     *             Thrown when provided parser context already contains any
     *             errors.
     * @throws ExpanderException
     *             This exception is thrown when errors occur during expansion.
     *             Found errors are stored in both exception and provided parser
     *             context.
     */
    public static Formula expandStructure(ParserStructure structure, ParserContext context) throws ExpanderException {
        if (!context.getErrors().isEmpty()) {
            throw new IllegalStateException("Parser context contains errors before expansion.");
        }

        // mapping variable name to its index in array of values
        Map<String, Integer> position = new LinkedHashMap<String, Integer>();
        if (structure.variables != null) {
            int size = structure.variables.variableCount();
            Map<String, Double> parameters = context.getParameters();
            int parameterCount = parameters.size();
            size += parameterCount;
            if (size > 0) {
                // contains values of all variable for single measurement
                int[] values = new int[size];
                // contains variable entities that can be simple or sequence
                // variable
                int entityCount = structure.variables.entityCount() + parameterCount;
                VariableHolder[] variables = new VariableHolder[entityCount];

                // copy variable names and values to the holder and store it in
                // array
                Iterator<Entry<String, ArrayList<Integer>>> simpleIt = structure.variables.variables.entrySet().iterator();
                int idx = 0;
                while (simpleIt.hasNext()) {
                    Entry<String, ArrayList<Integer>> variable = simpleIt.next();
                    variables[idx] = new SimpleVariableHolder(variable.getKey(), variable.getValue());
                    ++idx;
                }

                // copy parameter names and values to the holder and store it in
                // array
                Iterator<Entry<String, Double>> paramIt = parameters.entrySet().iterator();
                while (paramIt.hasNext()) {
                    Entry<String, Double> parameter = paramIt.next();
                    ArrayList<Integer> list = new ArrayList<Integer>(1);
                    Integer value = parameter.getValue().intValue();
                    list.add(value);
                    variables[idx] = new SimpleVariableHolder(parameter.getKey(), list);
                    ++idx;
                }

                // copy variable sequences to the holder and store it in array
                Iterator<VariableSequence> sequenceIt = structure.variables.variableSequences.iterator();
                while (sequenceIt.hasNext()) {
                    variables[idx] = new SequenceVariableHolder(sequenceIt.next());
                    ++idx;
                }
                Formula formula = expand(structure, context, values, 0, variables, 0, position);
                // after expansion check for unused variables
                context.checkAndReportUnusedVariables();
                if (context.getErrors().isEmpty()) {
                    return formula;
                } else {
                    throw new ExpanderException(context.getErrors());
                }
            }
        }
        // Formula has not any variables thus expanded form is the same.
        // Also checks if the formula really does not have variables and if it
        // has return error
        Formula formula = structure.formula.expand(context, new int[0], position);
        if (context.getErrors().isEmpty()) {
            return formula;
        } else {
            throw new ExpanderException(context.getErrors());
        }
    }

    /**
     * For given variables it creates all combination of their values and
     * expanding formula from structure. Expanding is done recursively, each
     * call set variable on index <code>arrayIdx</code> in <code>valueArr</code>
     * to concrete value using values in <code>variables</code> and call itself
     * with incremented index. This is
     * done for every value of the variable. If <code>valueIdx</code> is out of
     * array, then we have all variable values set and can expand formula with
     * concrete values.
     * 
     * @param structure
     *            Structure generated by parser
     * @param context
     *            Structure for storing measurements
     * @param valuesArr
     *            Array of variable values
     * @param valuesIdx
     *            Index of actually processing variable in
     *            <code>valuesArr</code> and <code>variables</code>.
     * @param variables
     *            Map of all variables converted to array.
     * @param position
     *            Mapping variable name to index of its value in valuesArr
     * @return expanded formula
     */
    private static Formula expand(ParserStructure structure, ParserContext context, int[] valuesArr, int valuesIdx,
            VariableHolder[] variables, int variablesIdx, Map<String, Integer> position) {
        Formula formula;
        if (valuesIdx < valuesArr.length) {
            // we do not have all values in array so recursively call itself and
            // fill in the values
            assert (variablesIdx < variables.length);
            VariableHolder variable = variables[variablesIdx];
            int variableValueIdx = 0;
            int valuesCount = variable.valuesCount();

            // first variable value returns only one formula
            assert (variableValueIdx < valuesCount);
            int newValuesIdx = variable.fillValue(valuesArr, valuesIdx, variableValueIdx, position);
            int newVariablesIdx = variablesIdx + 1;
            formula = expand(structure, context, valuesArr, newValuesIdx, variables, newVariablesIdx, position);
            ++variableValueIdx;

            // every other value will create new formula from old one and
            // expanded one and connect them into tree related by AND
            // operator
            for (; variableValueIdx < valuesCount; ++variableValueIdx) {
                variable.fillValue(valuesArr, valuesIdx, variableValueIdx, null);
                Formula secondFormula = expand(structure, context, valuesArr, newValuesIdx, variables, newVariablesIdx, position);
                formula = new Expression(formula, Operator.AND, secondFormula);
            }

        } else {
            // all values have been set so we can expand formula with these
            // concrete values
            formula = structure.formula.expand(context, valuesArr, position);
        }
        return formula;
    }

    /**
     * This exception is thrown by
     * {@link Expander#expandStructure(ParserStructure, ParserContext)} as
     * indication, that expansion contains errors.
     * 
     * @see Expander#expandStructure(ParserStructure, ParserContext)
     */
    public static class ExpanderException extends Exception {

        /**
         * Serialization ID.
         */
        private static final long  serialVersionUID = -271737276896549093L;

        /** The expansion errors. */
        private final Set<Problem> expansionErrors;

        /**
         * Gets the expansion problems.
         * 
         * @return The expansion problems.
         */
        public Set<Problem> getExpansionErrors() {
            return expansionErrors;
        }

        /**
         * Instantiates a new expander exception.
         * 
         * @param expansionProblems
         *            The expansion problems.
         */
        public ExpanderException(Set<Problem> expansionProblems) {
            this.expansionErrors = expansionProblems;
        }

        @Override
        public String getMessage() {
            StringBuilder strb = new StringBuilder("Encountered ");
            strb.append(expansionErrors.size());
            strb.append(" errors during expansion:\n");
            for (Problem error : expansionErrors) {
                strb.append(error.getText());
                strb.append('\n');
            }
            return strb.toString();
        }

        @Override
        public String toString() {
            return String.format("%s [%s]", super.toString(), Arrays.deepToString(this.expansionErrors.toArray()));
        }
    }
}
