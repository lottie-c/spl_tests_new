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

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

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
public class ComparisonEvaluatorMWW extends ComparisonEvaluator {

    /** The TTest singleton instance to be used. */
    private static final MannWhitneyUTest              MWWTEST = new MannWhitneyUTest();

 
    /**
     * Instantiates a new comparison evaluator.
     * 
     * @param configuration
     *            The configuration.
     * @param checker
     */
    public ComparisonEvaluatorMWW(SplEvaluatorConfiguration configuration, StatisticValueChecker checker) {
	super(configuration, checker);
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
    public ComparisonResult processComparison(Comparison comparison, 
             double[] dataArray1, double[] dataArray2, StatisticalSummary measuredData1, 
             StatisticalSummary measuredData2, double median1, double median2, Sign comparisonType) {

        if (dataArray1.length < 2 || dataArray2.length < 2) {
            return ComparisonResult.createNotComputedComparisonResult("Not enough measurement samples for statistical Mann Whitney Wilcoxon test.");
        }

        switch (comparisonType) {
            case EQI:
                // this is the most complex case as we need to evaluate equality
                // with interval check
                return processIntervalEqualityComparison(comparison, dataArray2, dataArray1,
                     measuredData2, measuredData1, median2, median1);

            case GE:
                // just swap values and test for LE
                return processComparison(comparison, dataArray2, dataArray1,
                     measuredData2, measuredData1, median2, median1, Sign.LE);
            case GT:
                // just swap values and test for LT
                return processComparison(comparison, dataArray2, dataArray1,
                     measuredData2, measuredData1, median2, median1, Sign.LT);
            case LE: {
                ComparisonResult lt = processComparison(comparison, dataArray2, dataArray1,
                     measuredData2, measuredData1, median2, median1, Sign.LT);
                ComparisonResult eq = processComparison(comparison, dataArray2, dataArray1,
                     measuredData2, measuredData1, median2, median1, Sign.EQ);
                if (lt.isSatisfied() && eq.isSatisfied()) {
                    return new ComparisonResult(Math.max(lt.getPValue(), eq.getPValue()), true);
                } else if (lt.isSatisfied()) {
                    return lt;
                } else {
                    return eq;
                }
            }
            case LT:
                
                // need to divide answer by 2 for a one sided test
                // first need to check that the test statistic is not in upper tail
                if (median1 < median2) {
                    double pValueNegate = MWWTEST.mannWhitneyUTest(dataArray1, dataArray2) / 2.0;
                    // MWW test validation says, that both series means are equal
                    // but we don't want this result, we want negation
                    boolean result = !confidenceChecker.isPvalueAcceptable(pValueNegate);
                    return new ComparisonResult(pValueNegate, result);
                } else {
                    // medians are not in correct relation => it is certain
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
    public ComparisonResult processIntervalEqualityComparison(Comparison comparison, 
             double[] dataArray1, double[] dataArray2, StatisticalSummary measuredData1, 
             StatisticalSummary measuredData2, double median1, double median2) {

        Double interval = comparison.getInterval();
        interval = interval != null ? interval : configuration.getEqualityInterval();

        double leftLowerMedian = transformMedianValue(median1, 1.0d - interval);
        double rightLowerMedian = transformMedianValue(median2, 1.0d + interval);

        double[] leftLowerMeasurement = transformMeasuredArray(dataArray1, 1.0d - interval);
        double[] rightLowerMeasurement = transformMeasuredArray(dataArray2, 1.0d + interval);

        double leftGreaterMedian = transformMedianValue(median1, 1.0d + interval);
        double rightGreaterMedian = transformMedianValue(median2, 1.0d - interval);

        double[] leftGreaterMeasurement = transformMeasuredArray(dataArray1, 1.0d + interval);
        double[] rightGreaterMeasurement = transformMeasuredArray(dataArray2, 1.0d - interval);

        ComparisonResult lowerResult = processComparison(comparison, leftLowerMeasurement, rightLowerMeasurement,
            measuredData1, measuredData2, leftLowerMedian, rightLowerMedian, Sign.LE);
        ComparisonResult greaterResult = processComparison(comparison, leftGreaterMeasurement, 
            rightGreaterMeasurement,  measuredData1, measuredData2, leftGreaterMedian, rightGreaterMedian, Sign.GE);

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
