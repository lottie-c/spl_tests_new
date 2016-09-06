
package cz.cuni.mff.spl.evaluator.statistics;


import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.MeasurementState;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonResult;

/**
 * Processes comparison evaluation using a TTest.
 * 
 * @author Lottie Carruthers
 * 
 */
public class ComparisonEvaluatorT extends ComparisonEvaluator {

    /** The TTest singleton instance to be used. */
    private static final TTest              TTEST = new TTest();


    /**
     * Instantiates a new comparison evaluator.
     * 
     * @param configuration
     *            The configuration.
     * @param checker
     */
    public ComparisonEvaluatorT(SplEvaluatorConfiguration configuration, StatisticValueChecker checker) {
	super(configuration, checker);
    }


    public ComparisonResult processComparison(Comparison comparison, 
             double[] dataArray1, double[] dataArray2, StatisticalSummary measuredData1, 
             StatisticalSummary measuredData2, double median1, double median2, Sign comparisonType) {
	
    	return(processComparison( comparison,  measuredData1,  measuredData2,
    				  comparisonType));
    }

    /**
     * Processes comparison of samples using a TTest.
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
     * @see TTest#tTest(StatisticalSummary, StatisticalSummary)
     * @see TTest#tTest(StatisticalSummary, StatisticalSummary, double)
     */
    private ComparisonResult processComparison(Comparison comparison, StatisticalSummary measuredData1, StatisticalSummary measuredData2,
            Sign comparisonType) {

        if (measuredData1.getN() < 2 || measuredData2.getN() < 2) {
            return ComparisonResult.createNotComputedComparisonResult("Not enough measurement samples for statistical t-test.");
        }

        switch (comparisonType) {
            case EQI:
                // this is the most complex case as we need to evaluate equality
                // with interval check
                return processIntervalEqualityComparison(comparison, measuredData2, measuredData1);

            case GE:
                // just swap values and test for LE
                return processComparison(comparison, measuredData2, measuredData1, Sign.LE);
            case GT:
                // just swap values and test for LT
                return processComparison(comparison, measuredData2, measuredData1, Sign.LT);
            case LE: {
                ComparisonResult lt = processComparison(comparison, measuredData1, measuredData2, Sign.LT);
                ComparisonResult eq = processComparison(comparison, measuredData1, measuredData2, Sign.EQ);
                
                if (lt.isSatisfied() && eq.isSatisfied()) {
                    return new ComparisonResult(Math.max(lt.getPValue(), eq.getPValue()), true);
                } else if (lt.isSatisfied()) {
                    return lt;
                } else {
                    return eq;
                }
            }
            case LT:
                double mean1 = measuredData1.getMean();
                double mean2 = measuredData2.getMean();
                // see javadoc for tTest(...),
                // it says "check first mean is less than second"
                // and if so, than do t-test with double confidence
                // or divide p-value by 2
                if (mean1 < mean2) {
                    double pValueNegate = TTEST.tTest(measuredData1, measuredData2) / 2.0;
                    // t-test validation says, that both series means are equal
                    // but we don't want this result, we want negation
                    boolean result1 = !confidenceChecker.isPvalueAcceptable(pValueNegate);
                    boolean result2 = TTEST.tTest(measuredData1, measuredData2, 2 * confidenceChecker.getPvalueLimit());
                    if (result1 != result2) {
                        throw new IllegalStateException(
                                "Statistical evaluation is inconsistent.");
                    }
                    return new ComparisonResult(pValueNegate, result1);
                } else {
                    // means are not in correct relation => it is certain
                    return new ComparisonResult(0, false);
                }
            case EQ:
                double pValue = TTEST.tTest(measuredData1, measuredData2);
                boolean acceptable = confidenceChecker.isPvalueAcceptable(pValue);
                return new ComparisonResult(pValue, acceptable);
            default:
                throw new IllegalStateException("Unexpected switch value " + comparisonType.toString());
        }
    }


    /**
     * Calls the  equality processor for this class, ommitting extra
     * parameters
     *
     * @param comparison
     *            The comparison.
     * @param dataArray1
     *            The left measurement sample.
     * @param dataArray2
     *            The right measurement sample.
     * @param  measuredData1
     *            The statistical summary of the left measurement.
     * @param measuredData2
     *            The statistical summary of the right measurement.
     * @param median1 
     *            The median of the left measurement
     * @param median2 
     *            The median of the right measurement
     * @param comparisonType
     *            The comparison type.
     * @return The comparison result.
     */
    public ComparisonResult processIntervalEqualityComparison(Comparison comparison, 
             double[] dataArray2, double[] dataArray1, StatisticalSummary measuredData2, 
             StatisticalSummary measuredData1, double median2, double median1) {

	   return( processIntervalEqualityComparison( comparison,  measuredData2,  measuredData1));
    }


    /**
     * Process interval equality comparison using a TTest.
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
    private ComparisonResult processIntervalEqualityComparison(Comparison comparison, StatisticalSummary measuredData2, StatisticalSummary measuredData1) {

        Double interval = comparison.getInterval();
        interval = interval != null ? interval : configuration.getEqualityInterval();

        StatisticalSummary leftLowerSummary = transformStatisticalSummary(measuredData1, 1.0d - interval);
        StatisticalSummary rightLowerSummary = transformStatisticalSummary(measuredData2, 1.0d + interval);

        StatisticalSummary leftGreaterSummary = transformStatisticalSummary(measuredData1, 1.0d + interval);
        StatisticalSummary rightGreaterSummary = transformStatisticalSummary(measuredData2, 1.0d - interval);

        ComparisonResult lowerResult = processComparison(comparison, leftLowerSummary, rightLowerSummary, Sign.LE);
        ComparisonResult greaterResult = processComparison(comparison, leftGreaterSummary, rightGreaterSummary, Sign.GE);

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
