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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.util.ArrayList;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;
import cz.cuni.mff.spl.evaluator.output.impl.html2.MeasurementResultDescriptor.MeasurementValidationFlags;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluator;
import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluatorMWW;
/*import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluatorKS;
*/

import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.StatisticValueChecker;

/**
 * The comparison evaluation result descriptor for XSLT transformation.
 * 
 * @author Martin Lacina
 */
public class ComparisonResultDescriptor extends OutputResultDescriptor {

    /** The comparison evaluation result. */
    private final ComparisonEvaluationResult comparisonEvaluationResult;



    /**
     * Gets the comparison evaluation result.
     * 
     * @return The comparison evaluation result.
     */
    public ComparisonEvaluationResult getComparisonEvaluationResult() {
        return comparisonEvaluationResult;
    }


    /** The comparison validation flags. */
    private final ComparisonValidationFlags comparisonValidationFlags = new ComparisonValidationFlags();

    /**
     * Gets the comparison validation flags.
     * 
     * @return The comparison validation flags.
     */
    public ComparisonValidationFlags getComparisonValidationFlags() {
        return comparisonValidationFlags;
    }

    /** The left measurement sample. */
    private final MeasurementSample leftMeasurementSample;

    /**
     * Gets the left measurement sample.
     * 
     * @return The left measurement sample.
     */
    public MeasurementSample getLeftMeasurementSample() {
        return leftMeasurementSample;
    }

    /** The right measurement sample. */
    private final MeasurementSample rightMeasurementSample;

    /**
     * Gets the right measurement sample.
     * 
     * @return The right measurement sample.
     */
    public MeasurementSample getRightMeasurementSample() {
        return rightMeasurementSample;
    }

    /** The validation flags for left sample. */
    private final MeasurementValidationFlags leftSampleValidationFlags = new MeasurementValidationFlags();

    /**
     * Gets the validation flags for left sample.
     * 
     * @return The validation flags for left sample.
     */
    public MeasurementValidationFlags getLeftSampleValidationFlags() {
        return leftSampleValidationFlags;
    }

    /** The validation flags for right sample. */
    private final MeasurementValidationFlags rightSampleValidationFlags = new MeasurementValidationFlags();

    /**
     * Gets the validation flags for right sample.
     * 
     * @return The validation flags for right sample.
     */
    public MeasurementValidationFlags getRightSampleValidationFlags() {
        return rightSampleValidationFlags;
    }

    /**
     * Instantiates a new measurement result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration
     *            The configuration.
     * @param comparisonEvaluationResult
     *            The comparison evaluation result.
     * @param checker
     *            The checker.
     * @param graphsMapping
     *            The graphs mapping.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */

   
    public ComparisonResultDescriptor(Info info, ConfigurationBundle configuration, ComparisonEvaluationResult comparisonEvaluationResult, 
                StatisticValueChecker checker, BasicOutputFileMapping graphsMapping, ArrayList<Link> outputLinks, AnnotationValidationFlags globalAliasesSummary) {
        super(info, configuration, outputLinks, globalAliasesSummary);
        this.comparisonEvaluationResult = comparisonEvaluationResult;

        this.leftMeasurementSample = comparisonEvaluationResult.leftMeasurementSample;
        this.rightMeasurementSample = comparisonEvaluationResult.rightMeasurementSample;

        this.leftSampleValidationFlags.setFlags(comparisonEvaluationResult.leftMeasurementSample, checker);
        this.rightSampleValidationFlags.setFlags(comparisonEvaluationResult.rightMeasurementSample, checker);

        this.comparisonValidationFlags.setFlags(comparisonEvaluationResult, checker);

        fillGraphReferences(graphsMapping, configuration.getEvaluatorConfig().getComparisonGraphTypes(), comparisonEvaluationResult.getComparison());
    }

    /**
     * The comparison validation flags.
     * 
     * @author Martin Lacina
     */
    public static class ComparisonValidationFlags {

        /** The left lambda. */
        public double leftLambda;

        /** The right lambda. */
        public double rightLambda;

        /** The left mean with lambda applied. */
        public double leftMeanWithLambda;

        /** The right mean with lambda applied. */
        public double rightMeanWithLambda;

        /**
         * Instantiates a new comparison validation flags.
         */
        public ComparisonValidationFlags() {

        }

        /**
         * Sets the flags.
         * 
         * @param result
         *            The comparison evaluation result.
         * @param checker
         *            The checker.
         */
        public void setFlags(ComparisonEvaluationResult result, StatisticValueChecker checker) {
            leftLambda = ComparisonEvaluator.getLambdaMultiplier(result.getComparison().getLeftLambda());
            rightLambda = ComparisonEvaluator.getLambdaMultiplier(result.getComparison().getRightLambda());
            leftMeanWithLambda = leftLambda * result.leftMeasurementSample.getMean();
            rightMeanWithLambda = rightLambda * result.rightMeasurementSample.getMean();
        }

    }

}
