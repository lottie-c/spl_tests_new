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

/**
 * The comparison result information containing boolean value indicating
 * whether comparison is satisfied or not and p-value which lead to
 * the decision.
 * 
 * @author Martin Lacina
 */
public class ComparisonResult {

    /**
     * The p-value.
     * This field is meant to be read only.
     */
    private double            pValue;

    /**
     * Value indicating if comparison is satisfied.
     * This field is meant to be read only.
     */
    private StatisticalResult statisticalResult;

    /** The error message. */
    private String            errorMessage;

    /**
     * Gets the p value.
     * 
     * @return The p value.
     */
    public double getPValue() {
        return pValue;
    }

    /**
     * Sets the p value. For XML transformation only.
     * 
     * @param pValue
     *            The new p value.
     */
    @Deprecated
    public void setPValue(double pValue) {
        this.pValue = pValue;
    }

    /**
     * Gets the statistical evaluation result.
     * 
     * @return The statistical evaluation result.
     */
    public StatisticalResult getStatisticalResult() {
        return statisticalResult;
    }

    /**
     * Sets the statistical evaluation result. For XML transformation only.
     * 
     * @param isSatisfied
     *            The new statistical evaluation result.
     */
    @Deprecated
    public void setStatisticalResult(StatisticalResult isSatisfied) {
        this.statisticalResult = isSatisfied;
    }

    /**
     * Gets the error message.
     * 
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message. For XML transformation only.
     * 
     * @param errorMessage
     *            The new error message.
     */
    @Deprecated
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new comparison result.
     * 
     * @param pValue
     *            The p-value.
     * @param isSatisfied
     *            The satisfied indicator.
     * @param errorMessage
     *            The error message. Can be {@code null}.
     */
    public ComparisonResult(double pValue, StatisticalResult isSatisfied, String errorMessage) {
        this.pValue = pValue;
        this.statisticalResult = isSatisfied;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates the not computed comparison result.
     * 
     * @param errorMessage
     *            The error message. Can be {@code null}.
     * @return The failed comparison result.
     */
    public static ComparisonResult createNotComputedComparisonResult(String errorMessage) {
        return new ComparisonResult(Double.NaN, StatisticalResult.NOT_COMPUTED, errorMessage);
    }

    /**
     * Checks if comparison is satisfied.
     * 
     * @return True, iff comparison result is {@link StatisticalResult.OK}
     */
    public boolean isSatisfied() {
        return this.statisticalResult == StatisticalResult.OK;
    }

    /**
     * Instantiates a new comparison result.
     * 
     * Use when comparison result is {@link StatisticalResult#OK} or
     * {@link StatisticalResult#FAILED}.
     * When comparison result is {@link StatisticalResult#NOT_COMPUTED}, than
     * use {@link ComparisonResult#createNotComputedComparisonResult()} instead.
     * 
     * 
     * @param pValue
     *            The p-value.
     * @param isSatisfied
     *            The satisfied indicator.
     */
    public ComparisonResult(double pValue, boolean isSatisfied) {
        this.pValue = pValue;
        if (isSatisfied) {
            this.statisticalResult = StatisticalResult.OK;
        } else {
            this.statisticalResult = StatisticalResult.FAILED;
        }
    }

    /**
     * Instantiates a new comparison result.
     * 
     * For XML transformation only.
     */
    @Deprecated
    public ComparisonResult() {
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // hashCode for ENUM is NOT stable between JVM instances
        result = prime * result + ((statisticalResult == null) ? 0 : statisticalResult.ordinal());
        long temp;
        temp = Double.doubleToLongBits(pValue);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ComparisonResult) {
            ComparisonResult otherResult = (ComparisonResult) other;

            boolean check1 = otherResult.getStatisticalResult() == this.statisticalResult;

            boolean check2 = otherResult.getPValue() == this.pValue;
            return check1 && check2;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s", this.statisticalResult, this.pValue);
    }

}
