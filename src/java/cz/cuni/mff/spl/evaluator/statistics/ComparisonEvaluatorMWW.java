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
package cz.cuni.mff.spl.evaluator.statistics;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.MeasurementState;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonResult;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;


/**
 * Processes comparison evaluation.
 * 
 * @author Martin Lacina
 * 
 */
public class ComparisonEvaluatorMWW {

    /** The TTest singleton instance to be used. */
    private static final MannWhitneyUTest              MWWTEST = new MannWhitneyUTest();

    /** The confidence to be used for p-value comparison. */
    private final StatisticValueChecker     confidenceChecker;

    /** The configuration. */
    private final SplEvaluatorConfiguration configuration;

    /**
     * Instantiates a new comparison evaluator.
     * 
     * @param configuration
     *            The configuration.
     * @param checker
     */
    public ComparisonEvaluatorMWW(SplEvaluatorConfiguration configuration, StatisticValueChecker checker) {
        this.configuration = configuration;
        this.confidenceChecker = checker;
    }

    /**
     * Gets the lambda multiplier.
     * 
     * @param lambda
     *            The lambda.
     * @return The evaluated lambda multiplier.
     */
    public static double getLambdaMultiplier(Lambda lambda) {
        double result = 1;
        if (lambda == null) {
            return result;
        }
        for (double multiplier : lambda.getConstants()) {
            result *= multiplier;
        }
        return result;
    }

    /**
     * Evaluates comparison.
     * Applies lambda function on samples
     * 
     * @param comparison
     *            The comparison.
     * @param leftMeasurementSample
     *            The left measurement sample.
     * @param rightMeasurementSample
     *            The right measurement sample.
     * @param confidenceChecker
     *            The confidence.
     * @return The comparison result.
     */
    public ComparisonResult evaluate(Comparison comparison, MeasurementSample leftMeasurementSample,
     MeasurementSample rightMeasurementSample) {
        if (leftMeasurementSample.getMeasurement().getMeasurementState().isOk() && rightMeasurementSample.getMeasurement().getMeasurementState().isOk()) {
            if (leftMeasurementSample.getSampleCount() >= 2 && rightMeasurementSample.getSampleCount() >= 2) {

            	double leftMedian = leftMeasurementSample.getMedian();
            	double rightMedian = rightMeasurementSample.getMedian();

                StatisticalSummary leftSummary = transformStatisticalSummary(leftMeasurementSample.getStatisticalSummary(),
                        getLambdaMultiplier(comparison.getLeftLambda()));
                StatisticalSummary rightSummary = transformStatisticalSummary(rightMeasurementSample.getStatisticalSummary(),
                        getLambdaMultiplier(comparison.getRightLambda()));
                double[] leftMeasurement = transformMeasuredArray(leftMeasurementSample, getLambdaMultiplier(comparison.getLeftLambda()));
                double[] rightMeasurement = transformMeasuredArray(rightMeasurementSample, getLambdaMultiplier(comparison.getRightLambda()));


                return processComparison(comparison, leftSummary, rightSummary, 
                    leftMeasurement, rightMeasurement, comparison.getSign());
            } else {
                String errorMessage;

                MeasurementState leftMeasurementState = leftMeasurementSample.getMeasurement().getMeasurementState();
                MeasurementState rightMeasurementState = rightMeasurementSample.getMeasurement().getMeasurementState();

                if (leftMeasurementSample.getSampleCount() == 0 && rightMeasurementSample.getSampleCount() == 0) {
                    errorMessage = "Both measurements have no samples available.";

                    leftMeasurementState.setOk(false);
                    leftMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    leftMeasurementState.setMessage("Measurement has no samples available.");

                    rightMeasurementState.setOk(false);
                    rightMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    rightMeasurementState.setMessage("Measurement has no samples available.");
                } else if (leftMeasurementSample.getSampleCount() == 0) {
                    errorMessage = "Left measurement has no samples available.";

                    leftMeasurementState.setOk(false);
                    leftMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    leftMeasurementState.setMessage("Measurement has no samples available.");
                } else if (rightMeasurementSample.getSampleCount() == 0) {
                    errorMessage = "Right measurement has no samples available.";

                    rightMeasurementState.setOk(false);
                    rightMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    rightMeasurementState.setMessage("Measurement has no samples available.");
                } else if (leftMeasurementSample.getSampleCount() < 2 && rightMeasurementSample.getSampleCount() < 2) {
                    errorMessage = "Both measurements have less than 2 samples.";

                    leftMeasurementState.setOk(false);
                    leftMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    leftMeasurementState.setMessage("Measurement has less than 2 samples.");

                    rightMeasurementState.setOk(false);
                    rightMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    rightMeasurementState.setMessage("Measurement has less than 2 samples.");
                } else if (leftMeasurementSample.getSampleCount() < 2) {
                    errorMessage = "Left measurement has less than 2 samples.";

                    leftMeasurementState.setOk(false);
                    leftMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    leftMeasurementState.setMessage("Measurement has less than 2 samples.");
                } else if (rightMeasurementSample.getSampleCount() < 2) {
                    errorMessage = "Right measurement has less than 2 samples.";

                    rightMeasurementState.setOk(false);
                    rightMeasurementState.setLastPhase(LastPhase.EVALUATE);
                    rightMeasurementState.setMessage("Measurement has less than 2 samples.");
                } else {
                    errorMessage = "Mann Whitney Wilcox test prerequisites not satisfied.";
                }

                return ComparisonResult.createNotComputedComparisonResult(errorMessage);
            }
        } else {
            // don't add message to measurements as it would override current
            // failed message
            String errorMessage;

            if (!leftMeasurementSample.getMeasurement().getMeasurementState().isOk() && !rightMeasurementSample.getMeasurement().getMeasurementState().isOk()) {
                errorMessage = "Both measurements are not prepared for evaluation:\n"
                        + "Left: " + leftMeasurementSample.getMeasurement().getMeasurementState().getMessage() + "\n"
                        + "Right: " + rightMeasurementSample.getMeasurement().getMeasurementState().getMessage();
            } else if (!leftMeasurementSample.getMeasurement().getMeasurementState().isOk()) {
                errorMessage = "Left measurement is not prepared for evaluation: " + leftMeasurementSample.getMeasurement().getMeasurementState().getMessage();
            } else if (!rightMeasurementSample.getMeasurement().getMeasurementState().isOk()) {
                errorMessage = "Right measurement is not prepared for evaluation: "
                        + rightMeasurementSample.getMeasurement().getMeasurementState().getMessage();
            } else {
                errorMessage = "Measurements are not prepared for evaluation.";
            }

            return ComparisonResult.createNotComputedComparisonResult(errorMessage);
        }
    }

    /**
     * Transforms the statistical summary with provided lambda multiplier.
     * Returned statistical summary represents original measurement sample data
     * multiplied by lambda multiplier.
     * 
     * In fact only statistical adjustment with lambda multiplier is done in
     * 
     * @param statisticalSummary
     *            The statistical summary to transform.
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @return The transformed statistical summary.
     *         {@link StatisticalSummaryValues#StatisticalSummaryValues(double, double, long, double, double, double)}
     *         :
     *         <ul>
     *         <li>Sample count is unchanged.</li>
     *         <li>Mean, maximum, minimum and sum are multiplied with lambda
     *         multiplier.</li>
     *         <li>Variance is multiplied with lambda multiplier twice as
     *         standard deviation is square root of variance and it would need
     *         only one multiplication with lambda multiplier.</li>
     *         </ul>
     * 
     *         Note than slight difference in counted values will be always
     *         present due
     *         to double arithmetics as statistical values are not computed from
     *         multiplied data directly.
     */
    public static StatisticalSummary transformStatisticalSummary(StatisticalSummary statisticalSummary, double lambdaMultiplier) {
        return new StatisticalSummaryValues(lambdaMultiplier * statisticalSummary.getMean(), lambdaMultiplier * lambdaMultiplier
                * statisticalSummary.getVariance(),
                statisticalSummary.getN(),
                lambdaMultiplier * statisticalSummary.getMax(), lambdaMultiplier * statisticalSummary.getMin(), lambdaMultiplier * statisticalSummary.getSum());
    }

    /**
     * Transforms the measured array with the provided lambda multiplier.
     * Returned array is original measurement sample data multiplied by 
     * lambda multiplier
     * @param measurement
     *       the measurement to transform
     * @param lambdaMultipler
     *      The lambda multiplier.
     * @return The transformed measurement.
     */
    public static double[] transformMeasuredArray(MeasurementSample measurement, double lambdaMultiplier){
       	

        try{
        	double[] data = measurement.getSampleDataProvider().loadRawData(lambdaMultiplier);
        	return data;
    	}catch (MeasurementDataNotFoundException e){
    		System.out.println("Error obtaining raw data for MWW test: " + e);
    		double[] data = {-1};
    		return data;
    	}
        
    }

	/**
     * Transforms the input array with the provided lambda multiplier.
     * Returned array is original measurement sample data multiplied by 
     * lambda multiplier
     * @param measurement
     *       the measurement to transform
     * @param lambdaMultipler
     *      The lambda multiplier.
     * @return The transformed measurement.
     */
    public static double[] transformMeasuredArray(double[] array, double lambdaMultiplier){
        for (int i = 0; i < array.length; i++){
            array[i] *= lambdaMultiplier;
        }
        return array;
    }

    public static double getMedian(double[] array){
    	int n = array.length;
    	int midPoint = n/2;
    	double median = array[midPoint];
    	return median;
    }


    /**
     * Processes comparison of samples.
     * 
     * @param comparison
     *            The comparison.
     * @param measuredData1
     *            First measured data. Left operator of comparison type.
     * @param measuredData2
     *            Second measured data. Right operator of comparison type.
     * @param confidenceChecker
     *            The confidence to check p-value to.
     * @param comparisonType
     *            The comparison type.
     * @return The comparison result with result and p-value.
     * @see ComparisonResult
     * @see MannWhitneyUTest#MannWhitneyUTest(double[], double[])
     */
    private ComparisonResult processComparison(Comparison comparison, StatisticalSummary measuredData1, StatisticalSummary measuredData2,
             double[] dataArray1, double[] dataArray2, Sign comparisonType) {

        if (measuredData1.getN() < 2 || measuredData2.getN() < 2) {
            return ComparisonResult.createNotComputedComparisonResult("Not enough measurement samples for statistical Mann Whitney Wilcoxon test.");
        }

        switch (comparisonType) {
            case EQI:
                // this is the most complex case as we need to evaluate equality
                // with interval check
                return processIntervalEqualityComparison(comparison, measuredData2, 
                	measuredData1, dataArray2, dataArray1);

            case GE:
                // just swap values and test for LE
                return processComparison(comparison, measuredData2, measuredData1,
                 dataArray2, dataArray1, Sign.LE);
            case GT:
                // just swap values and test for LT
                return processComparison(comparison, measuredData2, measuredData1, 
                    dataArray2, dataArray1, Sign.LT);
            case LE: {
                ComparisonResult lt = processComparison(comparison, measuredData1, measuredData2, 
                    dataArray1, dataArray2, Sign.LT);
                ComparisonResult eq = processComparison(comparison, measuredData1, measuredData2, 
                    dataArray1, dataArray2, Sign.EQ);
                if (lt.isSatisfied() && eq.isSatisfied()) {
                    return new ComparisonResult(Math.max(lt.getPValue(), eq.getPValue()), true);
                } else if (lt.isSatisfied()) {
                    return lt;
                } else {
                    return eq;
                }
            }
            case LT:
                double median1 = getMedian(dataArray1);
                double median2 = getMedian(dataArray2);
                // need to divide answer by 2 for a one sided test
                // first need to check that the test statistic is not in upper tail
                if (median1 < median2) {
                    double pValueNegate = MWWTEST.mannWhitneyUTest(dataArray1, dataArray2) / 2.0;
                    // MWW test validation says, that both series means are equal
                    // but we don't want this result, we want negation
                    boolean result = !confidenceChecker.isPvalueAcceptable(pValueNegate);
                    System.out.println("in ComparisonEvaluatorMWW  m1:" + median1 + " < m2 :" + median2);
                    return new ComparisonResult(pValueNegate, result);
                } else {
                    // means are not in correct relation => it is certain
                    System.out.println("in ComparisonEvaluatorMWW  m1:" + median1 + " >= m2 :" + median2);

                    return new ComparisonResult(0, false);
                }
            case EQ:
                double pValue = MWWTEST.mannWhitneyUTest(dataArray1, dataArray2);
                boolean acceptable = confidenceChecker.isPvalueAcceptable(pValue);
                return new ComparisonResult(pValue, acceptable);
            default:
                throw new IllegalStateException("Unexpected switch value " + comparisonType.toString());
        }
    }

    /**
     * Process interval equality comparison.
     * 
     * @param comparison
     *            The comparison.
     * @param measuredData2
     *            The measured data2.
     * @param measuredData1
     *            The measured data1.
     * @param confidenceChecker
     *            The confidence checker.
     * @return The comparison result.
     */
    private ComparisonResult processIntervalEqualityComparison(Comparison comparison, 
        StatisticalSummary measuredData2, StatisticalSummary measuredData1, 
        double[] dataArray1, double[] dataArray2) {

        Double interval = comparison.getInterval();
        interval = interval != null ? interval : configuration.getEqualityInterval();

        StatisticalSummary leftLowerSummary = transformStatisticalSummary(measuredData1, 1.0d - interval);
        StatisticalSummary rightLowerSummary = transformStatisticalSummary(measuredData2, 1.0d + interval);

        double[] leftLowerMeasurement = transformMeasuredArray(dataArray1, 1.0d - interval);
        double[] rightLowerMeasurement = transformMeasuredArray(dataArray2, 1.0d + interval);

        StatisticalSummary leftGreaterSummary = transformStatisticalSummary(measuredData1, 1.0d + interval);
        StatisticalSummary rightGreaterSummary = transformStatisticalSummary(measuredData2, 1.0d - interval);

        double[] leftGreaterMeasurement = transformMeasuredArray(dataArray1, 1.0d + interval);
        double[] rightGreaterMeasurement = transformMeasuredArray(dataArray2, 1.0d - interval);

        ComparisonResult lowerResult = processComparison(comparison, leftLowerSummary, rightLowerSummary,
            leftLowerMeasurement, rightLowerMeasurement,  Sign.LE);
        ComparisonResult greaterResult = processComparison(comparison, leftGreaterSummary, rightGreaterSummary,
            leftGreaterMeasurement, rightGreaterMeasurement, Sign.GE);

        // combine results if satisfied, or return the one that failed
        if (lowerResult.isSatisfied() && greaterResult.isSatisfied()) {
            return new ComparisonResult(Math.min(lowerResult.getPValue(), greaterResult.getPValue()), true);
        } else if (lowerResult.isSatisfied()) {
            return greaterResult;
        } else {
            return lowerResult;
        }
    }
}
