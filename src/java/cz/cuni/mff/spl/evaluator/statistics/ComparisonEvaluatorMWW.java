
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
 * Processes comparison evaluation for Mann Whitney U test.
 * 
 * @author Lottie Carruthers
 * 
 */
public class ComparisonEvaluatorMWW extends ComparisonEvaluator {

    /** The MannWhitneyUTest singleton instance to be used. */
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
     * Calls the comparison processor for this class, ommitting extra
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
    public ComparisonResult processComparison(Comparison comparison, 
             double[] dataArray1, double[] dataArray2, StatisticalSummary measuredData1, 
             StatisticalSummary measuredData2, double median1, double median2, Sign comparisonType) {

    	return(processComparison( comparison, median1, median2,
    				  dataArray1, dataArray2,  comparisonType));       
    }


    /**
     * Processes the comparison using a Mann Whitney U test
     *
     * @param comparison
     *            The comparison.
     * @param dataArray1
     *            The left measurement sample.
     * @param dataArray2
     *            The right measurement sample.
     * @param median1 
     *            The median of the left measurement
     * @param median2 
     *            The median of the right measurement
     * @param comparisonType
     *            The comparison type.
     * @return The comparison result.
     * @see ComparisonResult
     * @See MannWhitneyUTest#mannWhitneyUTest(double[], double[])
     */
    private ComparisonResult processComparison(Comparison comparison, double median1, double median2,
             double[] dataArray1, double[] dataArray2, Sign comparisonType) {

        if (dataArray1.length < 2 || dataArray2.length < 2) {
            return ComparisonResult.createNotComputedComparisonResult("Not enough measurement samples for statistical Mann Whitney Wilcoxon test.");
        }

        switch (comparisonType) {
            case EQI:
                // this is the most complex case as we need to evaluate equality
                // with interval check
                return processIntervalEqualityComparison(comparison, median2, 
                	median1, dataArray2, dataArray1);

            case GE:
                // just swap values and test for LE
                return processComparison(comparison, median2, median1,
                 dataArray2, dataArray1, Sign.LE);
            case GT:
                // just swap values and test for LT
                return processComparison(comparison, median2, median1, 
                    dataArray2, dataArray1, Sign.LT);
            case LE: {
                ComparisonResult lt = processComparison(comparison, median1, median2, 
                    dataArray1, dataArray2, Sign.LT);
                ComparisonResult eq = processComparison(comparison, median1, median2, 
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

	   return (processIntervalEqualityComparison( comparison,  median2, median1, dataArray2,
						   dataArray1));
    }


    /**
     * Process interval equality comparison
     * using a Mann Whitney U test.
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
         double median2, double median1, double[] dataArray2, double[] dataArray1) {

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

        ComparisonResult lowerResult = processComparison(comparison, leftLowerMedian, rightLowerMedian,
							 leftLowerMeasurement, rightLowerMeasurement,  Sign.LE);
        ComparisonResult greaterResult = processComparison(comparison, leftGreaterMedian, rightGreaterMedian,
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
