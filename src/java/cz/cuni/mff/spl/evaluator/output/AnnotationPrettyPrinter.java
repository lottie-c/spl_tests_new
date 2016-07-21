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
package cz.cuni.mff.spl.evaluator.output;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.ExpandedVariable;
import cz.cuni.mff.spl.annotation.Generator;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.Measurement;
import cz.cuni.mff.spl.annotation.Method;
import cz.cuni.mff.spl.annotation.ParserVariable;
import cz.cuni.mff.spl.annotation.Revision;
import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.annotation.Variable;
import cz.cuni.mff.spl.evaluator.output.flatformula.FlatEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.flatformula.FlatLogicalOperationEvaluationResult;
import cz.cuni.mff.spl.evaluator.output.results.EvaluationResult;
import cz.cuni.mff.spl.utils.StringUtils;

/**
 * Pretty printer for SPL annotation parts used in evaluator.
 * 
 * @author Martin Lacina
 */
public class AnnotationPrettyPrinter {

    /**
     * Creates the lambda output string.
     * 
     * @param lambda
     *            The lambda.
     * @return The lambda output string.
     */
    public static String createLambdaOutput(Lambda lambda) {
        return createLambdaOutput(lambda, new StringBuilder()).toString();
    }

    /**
     * Creates the lambda output.
     * 
     * @param lambda
     *            The lambda.
     * @param buffer
     *            The buffer.
     * @return The string buffer.
     */
    public static StringBuilder createLambdaOutput(Lambda lambda, StringBuilder buffer) {
        if (lambda == null) {
            buffer.append('x');
            return buffer;
        }

        if (lambda.getConstants().size() > 0) {
            buffer.append(StringUtils.createOneString(lambda.getConstants(), " * ", "", " * "));
        }

        buffer.append("x");

        return buffer;
    }

    /**
     * Creates the method output.
     * 
     * @param method
     *            The method.
     * @return The method string.
     */
    public static String createMethodOutput(Method method) {
        return method.getDeclarationString();
    }

    /**
     * Creates the method output.
     * 
     * @param method
     *            The method.
     * @return The short method string.
     */
    public static String createMethodOutputShort(Method method) {
        return method.getShortDeclarationString();
    }

    /**
     * Creates the generator output.
     * 
     * @param generator
     *            The generator.
     * @return The generator string.
     */
    public static String createGeneratorOutput(Generator generator) {
        return generator.getDeclarationString();
    }

    /**
     * Creates the generator output.
     * 
     * @param generator
     *            The generator.
     * @return The generator short string.
     */
    public static String createGeneratorOutputShort(Generator generator) {
        return generator.getShortDeclarationString();
    }

    /**
     * Creates the generator Variable output.
     * 
     * @param variable
     *            The Variable.
     * @param buffer
     *            The generator Variable buffer.
     */
    public static String createGeneratorVariableOutput(Variable variable) {
        if (variable instanceof ExpandedVariable) {
            ExpandedVariable param = (ExpandedVariable) variable;
            return StringUtils.createOneString(param.getVariables(), ", ");
        }
        if (variable instanceof ParserVariable) {
            ParserVariable param = (ParserVariable) variable;

            return StringUtils.createOneString(param.getVariables(), ", ");
        }
        throw new IllegalArgumentException("Unsupported type of Variable argument: " + variable.getClass().toString());
    }

    /**
     * Creates the measurement output.
     * 
     * @param measurement
     *            The measurement.
     * @return The measurement string.
     */
    public static String createMeasurementOutput(Measurement measurement) {
        return createMeasurementOutput(measurement, new StringBuilder()).toString();
    }

    /**
     * Creates the measurement output.
     * 
     * @param measurement
     *            The measurement.
     * @param buffer
     *            The buffer.
     * @return The string buffer.
     */
    private static StringBuilder createMeasurementOutput(Measurement measurement, StringBuilder buffer) {

        createRevisionOutput(measurement.getMethod().getRevision(), buffer);
        buffer.append(':');
        buffer.append(createMethodOutputShort(measurement.getMethod()));

        buffer.append(" [");
        createRevisionOutput(measurement.getGenerator().getRevision(), buffer);
        buffer.append(':');
        buffer.append(createGeneratorOutputShort(measurement.getGenerator()));
        buffer.append("] ");

        buffer.append('(');
        buffer.append(createGeneratorVariableOutput(measurement.getVariable()));
        buffer.append(')');

        return buffer;
    }

    /**
     * Creates the comparison output.
     * 
     * @param comparison
     *            The comparison.
     * @param defaultEqualityInterval
     *            The default equality interval for {@link Sign#EQI}.
     * @return The comparison string.
     */
    public static String createComparisonOutput(Comparison comparison, double defaultEqualityInterval) {
        return createComparisonOutput(comparison, new StringBuilder(), defaultEqualityInterval).toString();
    }

    /**
     * Creates the comparison output.
     * 
     * @param comparison
     *            The comparison.
     * @param buffer
     *            The buffer.
     * @param defaultEqualityInterval
     *            The default equality interval for {@link Sign#EQI}.
     * @return The string buffer.
     */
    public static StringBuilder createComparisonOutput(Comparison comparison, StringBuilder buffer, double defaultEqualityInterval) {

        createMeasurementOutput(comparison.getLeftMeasurement(), buffer);

        buffer.append(" ");
        buffer.append(comparison.getSign().getStringForOutput());
        buffer.append(" (");
        createLambdaOutput(comparison.getLeftLambda(), buffer);
        buffer.append(", ");
        createLambdaOutput(comparison.getRightLambda(), buffer);
        if (Sign.EQI.equals(comparison.getSign())) {
            buffer.append(", +-");
            double interval = comparison.getInterval() != null ? comparison.getInterval() : defaultEqualityInterval;
            buffer.append(interval * 100);
            buffer.append(" %");
        }
        buffer.append(") ");

        createMeasurementOutput(comparison.getRightMeasurement(), buffer);

        return buffer;
    }

    /**
     * Creates the formula output.
     * 
     * @param formula
     *            The formula.
     * @param defaultEqualityInterval
     *            The default equality interval for {@link Sign#EQI}.
     * @return The formula string.
     */
    public static String createFormulaOutput(EvaluationResult formula, double defaultEqualityInterval) {
        return createFormulaOutput(formula, new StringBuilder(), defaultEqualityInterval).toString();
    }

    /**
     * Creates the formula output.
     * 
     * @param formula
     *            The formula.
     * @param buffer
     *            The buffer.
     * @return The string buffer.
     */
    public static StringBuilder createFormulaOutput(EvaluationResult formula, StringBuilder buffer, double defaultEqualityInterval) {
        if (formula.isFormulaResult()) {
            createFormulaOutput(formula.asFormulaResult().getFormulaEvaluationResultRoot(), buffer, defaultEqualityInterval);
        } else if (formula.isLogicalOperationResult()) {
            buffer.append("{");
            createFormulaOutput(formula.asLogicalOperationResult().getLeftOperandResult(), buffer, defaultEqualityInterval);
            buffer.append("}");
            buffer.append(formula.asLogicalOperationResult().getLogicalOperator());
            buffer.append("{");
            createFormulaOutput(formula.asLogicalOperationResult().getRightOperandResult(), buffer, defaultEqualityInterval);
            buffer.append("}");
        } else if (formula.isComparisonEvaluationResult()) {
            createComparisonOutput(formula.asComparisonEvaluationResult().getComparison(), buffer, defaultEqualityInterval);
        } else {
            assert false : "unreachable";
        }
        return buffer;
    }

    /**
     * Creates the flat formula output.
     * 
     * @param flatFormula
     *            The flat formula.
     * @param defaultEqualityInterval
     *            The default equality interval for {@link Sign#EQI}.
     * @return The string.
     */
    public static String createFlatFormulaOutput(FlatEvaluationResult flatFormula, double defaultEqualityInterval) {
        return createFlatFormulaOutput(flatFormula, new StringBuilder(), defaultEqualityInterval).toString();

    }

    /**
     * Creates the flat formula output.
     * 
     * @param flatFormula
     *            The flat formula.
     * @param buffer
     *            The buffer.
     * @param defaultEqualityInterval
     *            The default equality interval for {@link Sign#EQI}.
     * @return The string buffer.
     */
    public static StringBuilder createFlatFormulaOutput(FlatEvaluationResult flatFormula, StringBuilder buffer, double defaultEqualityInterval) {
        if (flatFormula.isFlatLogicalOperationEvaluationResult()) {
            FlatLogicalOperationEvaluationResult logicalOperation = flatFormula.asFlatLogicalOperationEvaluationResult();

            buffer.append("(");
            boolean isFirst = true;
            for (FlatEvaluationResult operand : logicalOperation.getOperands()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buffer.append(" ");
                    buffer.append(logicalOperation.getLogicalOperator().getStringForOutput());
                    buffer.append(" ");
                }
                createFlatFormulaOutput(operand, buffer, defaultEqualityInterval);
            }
            buffer.append(")");
        } else if (flatFormula.isFlatComparisonEvaluationResult()) {
            createComparisonOutput(flatFormula.asFlatComparisonEvaluationResult().getComparison(), buffer, defaultEqualityInterval);
        }
        return buffer;
    }

    /**
     * Creates the revision output.
     * 
     * @param revision
     *            The revision.
     * @param buffer
     *            The buffer.
     * @return The string buffer.
     */
    public static StringBuilder createRevisionOutput(Revision revision, StringBuilder buffer) {
        buffer.append(revision.getDeclarationString());
        return buffer;
    }
}
