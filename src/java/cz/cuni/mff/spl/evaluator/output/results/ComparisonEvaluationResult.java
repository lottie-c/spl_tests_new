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
package cz.cuni.mff.spl.evaluator.output.results;

import cz.cuni.mff.spl.annotation.Comparison;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;

/**
 * Result of comparison evaluation in SPL formula.
 * 
 * @author Martin Lacina
 * 
 */
/*Edited by Lottie Carruthers*/

/*Stores the results of evaluating comparison using a variety of tests*/
public class ComparisonEvaluationResult extends AbstractEvaluationResult {

    /** The comparison. */
    private Comparison              comparison;

    /** The comparison result performing a t test. */
    private ComparisonResult        comparisonResultT;

    /** The comparison result performing a Mann Whitney U test. */
    private ComparisonResult    comparisonResultMWW;

    /** The comparison result performing a Kolmogorov Smirnov test.*/
    private ComparisonResult comparisonResultKS;
    

    /** The left measurement sample. */
    public MeasurementSample leftMeasurementSample;

    /** The right measurement sample. */
    public MeasurementSample rightMeasurementSample;

    /**
     * Instantiates a new comparison evaluation result implementation.
     * 
     * @param comparison
     *            The comparison.
     * @param comparisonResult
     *            The comparison result.
     * @param leftMeasurementSample
     *            The left measurement sample.
     * @param rightMeasurementSample
     *            The right measurement sample.
     */
    
    public ComparisonEvaluationResult(Comparison comparison, ComparisonResult comparisonResultT, 
        ComparisonResult comparisonResultMWW, ComparisonResult comparisonResultKS, MeasurementSample leftMeasurementSample,
        MeasurementSample rightMeasurementSample) {
        this.comparison = comparison;
        this.comparisonResultT = comparisonResultT;
        this.comparisonResultMWW = comparisonResultMWW;
        this.comparisonResultKS = comparisonResultKS;
        this.leftMeasurementSample = leftMeasurementSample;
        this.rightMeasurementSample = rightMeasurementSample;
    }

    @Override
    public boolean isComparisonEvaluationResult() {
        return true;
    }

    @Override
    public ComparisonEvaluationResult asComparisonEvaluationResult() {
        return this;
    }

    @Override
    public EvaluationResultType getResultType() {
        return EvaluationResultType.COMPARISON;
    }

    public ComparisonResult getComparisonResultT() {
        return this.comparisonResultT;
    }

    public ComparisonResult getComparisonResultMWW() {
        return this.comparisonResultMWW;
    }


    public ComparisonResult getComparisonResultKS() {
        return this.comparisonResultKS;
    }

    public Comparison getComparison() {
        return this.comparison;
    }

    public MeasurementSample getLeftMeasurementSample() {
        return this.leftMeasurementSample;
    }

    public MeasurementSample getRightMeasurementSample() {
        return this.rightMeasurementSample;
    }

    @Override
    public StatisticalResult getStatisticalResult() {
        return comparisonResultT.getStatisticalResult();
    }

    public StatisticalResult getStatisticalResultMWW() {
        return comparisonResultMWW.getStatisticalResult();
    }

    public StatisticalResult getStatisticalResultKS() {
        return comparisonResultKS.getStatisticalResult();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comparison == null) ? 0 : comparison.hashCode());
        result = prime * result + ((comparisonResultT == null) ? 0 : comparisonResultT.hashCode());
        result = prime * result + ((comparisonResultMWW == null) ? 0 : comparisonResultMWW.hashCode());
        result = prime * result + ((comparisonResultKS == null) ? 0 : comparisonResultKS.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ComparisonEvaluationResult) {
            ComparisonEvaluationResult otherResult = (ComparisonEvaluationResult) other;

            return otherResult.isComparisonEvaluationResult()
                    && otherResult.getComparison().equals(comparison)
                    && otherResult.getComparisonResultT().equals(comparisonResultT)
                    && otherResult.getComparisonResultMWW().equals(comparisonResultMWW)
                    && otherResult.getComparisonResultKS().equals(comparisonResultKS)
                    && otherResult.getLeftMeasurementSample().equals(leftMeasurementSample)
                    && otherResult.getRightMeasurementSample().equals(rightMeasurementSample);
        } else {
            return false;
        }
    }

    /**
     * Instantiates a new comparison evaluation result instance.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public ComparisonEvaluationResult() {
    }

    /**
     * Sets the comparison.
     * 
     * For XML transformation only.
     * 
     * @param comparison
     *            The new comparison.
     */
    @Deprecated
    public void setComparison(Comparison comparison) {
        this.comparison = comparison;
    }

    /**
     * Sets the t test comparison result.
     * 
     * For XML transformation only.
     * 
     * @param comparisonResult
     *            The new comparison result.
     */
    @Deprecated
    public void setComparisonResultT(ComparisonResult comparisonResultT) {
        this.comparisonResultT = comparisonResultT;
    }

    /**
     * Sets the Mann whitney u test comparison result .
     * 
     * For XML transformation only.
     * 
     * @param comparisonResult
     *            The new comparison result.
     */
    @Deprecated
    public void setComparisonResultMWW(ComparisonResult comparisonResultMWW) {
        this.comparisonResultMWW = comparisonResultMWW;
    }

    /**
     * Sets the Kolmogorov Smirnov test comparison result.
     * 
     * For XML transformation only.
     * 
     * @param comparisonResult
     *            The new comparison result.
     */
     public void setComparisonResultKS(ComparisonResult comparisonResultKS) {
        this.comparisonResultKS = comparisonResultKS;
      }
     

    /**
     * Sets the left measurement sample.
     * 
     * For XML transformation only.
     * 
     * @param leftMeasurementSample
     *            The new left measurement sample.
     */
    @Deprecated
    public void setLeftMeasurementSample(MeasurementSample leftMeasurementSample) {
        this.leftMeasurementSample = leftMeasurementSample;
    }

    /**
     * Sets the right measurement sample.
     * 
     * For XML transformation only.
     * 
     * @param rightMeasurementSample
     *            The new right measurement sample.
     */
    @Deprecated
    public void setRightMeasurementSample(MeasurementSample rightMeasurementSample) {
        this.rightMeasurementSample = rightMeasurementSample;
    }
}
