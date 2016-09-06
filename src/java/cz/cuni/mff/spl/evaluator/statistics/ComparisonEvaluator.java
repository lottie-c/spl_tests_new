package cz.cuni.mff.spl.evaluator.statistics;
import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.annotation.Lambda;
import cz.cuni.mff.spl.annotation.MeasurementState;
import cz.cuni.mff.spl.annotation.MeasurementState.LastPhase;
import cz.cuni.mff.spl.annotation.Sign;
import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonResult;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.StatisticalSummaryValues;

/*Provides methods and data used by subclasses to evaluate comparisons*/ 
public abstract class ComparisonEvaluator {

    
    /** The confidence to be used for p-value comparison. */
    protected final StatisticValueChecker     confidenceChecker;

    /** The configuration. */
    protected final SplEvaluatorConfiguration configuration;
    


     /**
     * Instantiates a new comparison evaluator.
     * 
     * @param configuration
     *            The configuration.
     * @param checker
     */
    public ComparisonEvaluator(SplEvaluatorConfiguration configuration, StatisticValueChecker checker) {
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
    public ComparisonResult evaluate(Comparison comparison, MeasurementSample leftMeasurementSample, MeasurementSample rightMeasurementSample) {
        if (leftMeasurementSample.getMeasurement().getMeasurementState().isOk() && rightMeasurementSample.getMeasurement().getMeasurementState().isOk()) {
            if (leftMeasurementSample.getSampleCount() >= 2 && rightMeasurementSample.getSampleCount() >= 2) {

                StatisticalSummary leftSummary = transformStatisticalSummary(leftMeasurementSample.getStatisticalSummary(),
                        getLambdaMultiplier(comparison.getLeftLambda()));
                StatisticalSummary rightSummary = transformStatisticalSummary(rightMeasurementSample.getStatisticalSummary(),
                        getLambdaMultiplier(comparison.getRightLambda()));


                double[] leftMeasurement = transformMeasuredArray(leftMeasurementSample, getLambdaMultiplier(comparison.getLeftLambda()));
                double[] rightMeasurement = transformMeasuredArray(rightMeasurementSample, getLambdaMultiplier(comparison.getRightLambda()));

                double leftMedian = transformMedianValue(leftMeasurementSample.getMedian(), getLambdaMultiplier(comparison.getLeftLambda()));
                double rightMedian = transformMedianValue(rightMeasurementSample.getMedian(), getLambdaMultiplier(comparison.getRightLambda()));
                

                return processComparison(comparison,leftMeasurement, rightMeasurement, leftSummary, rightSummary,
                    leftMedian, rightMedian, comparison.getSign());
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
                    errorMessage = "T-test prerequisities not satisfied.";
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
     * An abstract method which is over written in each subclass with the correct implementation
     * for processing comparisons 
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
    public abstract ComparisonResult processComparison(Comparison comparison, 
                double[] dataArray1, double[] dataArray2, StatisticalSummary measuredData1, 
                StatisticalSummary measuredData2, double median1, double median2, Sign comparisonType);


    /**
     * An abstract method which is over written in each subclass with the correct implementation
     * for processing equality comparisons 
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
     * @return The comparison result.
     */
    public abstract ComparisonResult processIntervalEqualityComparison(Comparison comparison, 
                double[] dataArray2, double[] dataArray1, StatisticalSummary measuredData2, 
                StatisticalSummary measuredData1, double median2, double median1);

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
    		System.out.println("Error obtaining raw data for KS test: " + e);
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

    /**
     * Transforms the input median with the provided lambda multiplier.
     * Returned value is original median  multiplied by the
     * lambda multiplier
     * @param median
     *       the median to transform
     * @param lambdaMultipler
     *      The lambda multiplier.
     * @return The transformed median.
     */
     public static double transformMedianValue(double median, double lambdaMultiplier){
    	return median*lambdaMultiplier;
    }

}

