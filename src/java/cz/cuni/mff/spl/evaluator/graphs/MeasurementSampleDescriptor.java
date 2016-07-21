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
package cz.cuni.mff.spl.evaluator.graphs;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;

import cz.cuni.mff.spl.evaluator.statistics.ComparisonEvaluator;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.utils.EqualsUtils;

/**
 * The Class MeasurementSampleData.
 * 
 * @author Martin Lacina
 */
public class MeasurementSampleDescriptor {

    /** The apply lambda. */
    private final boolean           applyLambda;

    /** The lambda. */
    private final double            lambdaMultiplier;

    /** The measurement sample. */
    private final MeasurementSample measurementSample;

    /**
     * Checks if is applies the lambda.
     * 
     * @return True, if is applies the lambda.
     */
    public boolean isApplyLambda() {
        return applyLambda;
    }

    /**
     * Gets the lambda multiplier it it should be applied, or value {@code 1} if
     * not.
     * 
     * @return The lambda multiplier.
     */
    public double getLambdaMultiplier() {
        if (applyLambda) {
            return lambdaMultiplier;
        } else {
            return 1;
        }
    }

    /**
     * Gets the measurement sample.
     * 
     * @return The measurement sample.
     */
    public MeasurementSample getMeasurementSample() {
        return measurementSample;
    }

    /**
     * Instantiates a new measurement sample data.
     * 
     * @param applyLambda
     *            The apply lambda flag.
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @param measurementSample
     *            The measurement sample.
     */
    public MeasurementSampleDescriptor(boolean applyLambda, double lambdaMultiplier, MeasurementSample measurementSample) {
        if (applyLambda) {
            this.applyLambda = applyLambda;
            this.lambdaMultiplier = lambdaMultiplier;
        } else {
            this.applyLambda = false;
            this.lambdaMultiplier = -1;
        }
        this.measurementSample = measurementSample;
    }

    /**
     * Instantiates a new measurement sample data with no lambda to apply.
     * 
     * @param measurementSample
     *            The measurement sample.
     */
    public MeasurementSampleDescriptor(MeasurementSample measurementSample) {
        this(false, 1, measurementSample);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (applyLambda ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(lambdaMultiplier);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((measurementSample == null) ? 0 : measurementSample.hashCode());
        return result;
    }

    /**
     * Note that two measurement descriptors are equal, when they have same
     * lambda and measurement in measurement sample.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MeasurementSampleDescriptor other = (MeasurementSampleDescriptor) obj;
        if (applyLambda != other.applyLambda) {
            return false;
        }
        if (Double.doubleToLongBits(lambdaMultiplier) != Double.doubleToLongBits(other.lambdaMultiplier)) {
            return false;
        }

        if (measurementSample == null) {
            if (other.measurementSample != null) {
                return false;
            } else {
                return true;
            }
        } else if (other.measurementSample == null) {
            return false;
        } else {
            return EqualsUtils.safeEquals(measurementSample.getMeasurement(), other.measurementSample.getMeasurement());
        }
    }

    @Override
    public String toString() {
        return getSpecification();
    }

    /**
     * @return
     */
    public StatisticalSummary getStatisticalSummary() {
        if (applyLambda) {
            return ComparisonEvaluator.transformStatisticalSummary(measurementSample.getStatisticalSummary(), lambdaMultiplier);
        } else {
            return measurementSample.getStatisticalSummary();
        }

    }

    /**
     * @return
     */
    public String getSpecification() {
        String prefix = applyLambda ? Double.toString(lambdaMultiplier) + " * " : "";
        return prefix + measurementSample.getSpecification();
    }

}
