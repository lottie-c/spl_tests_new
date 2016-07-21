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

/**
 * The measurement sample statistical data.
 * <p>
 * Fields are public to allow direct access but not final to allow XML
 * conversion.
 * 
 * @author Martin Lacina
 */
public class MeasurementSampleStatisticalData {

    /** The sample count. Should not be changed manually. */
    public long   sampleCount;

    /** The warm-up measurement samples count. */
    public long   warmupCount;

    /** The measured date string. */
    public String measuredDate;

    /** The sample variance. Should not be changed manually. */
    public double variance;

    /** The standard deviation. Should not be changed manually. */
    public double standardDeviation;

    /** The mean. Should not be changed manually. */
    public double mean;

    /** The median. Should not be changed manually. */
    public double median;

    /** The minimum. Should not be changed manually. */
    public double minimum;

    /** The maximum. Should not be changed manually. */
    public double maximum;

    /**
     * Gets the sample count.
     * 
     * @return The sample count.
     */
    public long getSampleCount() {
        return sampleCount;
    }

    /**
     * Sets the sample count.
     * 
     * @param sampleCount
     *            The new sample count.
     */
    public void setSampleCount(long sampleCount) {
        this.sampleCount = sampleCount;
    }

    /**
     * Gets the warm-up measurement samples count.
     * 
     * @return The warm-up measurement samples count.
     */
    public long getWarmupCount() {
        return warmupCount;
    }

    /**
     * Sets the warm-up measurement samples count.
     * 
     * @param warmupCount
     *            The new warm-up measurement samples count.
     */
    public void setWarmupCount(long warmupCount) {
        this.warmupCount = warmupCount;
    }

    /**
     * Gets the measured date.
     * 
     * @return The measured date string.
     */
    public String getMeasuredDate() {
        return measuredDate;
    }

    /**
     * Sets the measured date.
     * 
     * @param measuredDate
     *            The new measured date string.
     */
    public void setMeasuredDate(String measuredDate) {
        this.measuredDate = measuredDate;
    }

    /**
     * Gets the standard deviation.
     * 
     * @return The standard deviation.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * Sets the standard deviation.
     * 
     * @param standardDeviation
     *            The new standard deviation.
     */
    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
        this.variance = this.standardDeviation * this.standardDeviation;
    }

    /**
     * Gets the sample variance.
     * 
     * @return The sample variance.
     */
    public double getVariance() {
        return variance;
    }

    /**
     * Sets the sample variance.
     * 
     * @param variance
     *            The new sample variance.
     */
    public void setVariance(double variance) {
        this.variance = variance;
    }

    /**
     * Gets the mean.
     * 
     * @return The mean.
     */
    public double getMean() {
        return mean;
    }

    /**
     * Sets the mean.
     * 
     * @param mean
     *            The new mean.
     */
    public void setMean(double mean) {
        this.mean = mean;
    }

    /**
     * Gets the median.
     * 
     * @return The median.
     */
    public double getMedian() {
        return median;
    }

    /**
     * Sets the median.
     * 
     * @param median
     *            The new median.
     */
    public void setMedian(double median) {
        this.median = median;
    }

    /**
     * Gets the minimum.
     * 
     * @return The minimum.
     */
    public double getMinimum() {
        return minimum;
    }

    /**
     * Sets the minimum.
     * 
     * @param minimum
     *            The new minimum.
     */
    public void setMinimum(double minimum) {
        this.minimum = minimum;
    }

    /**
     * Gets the maximum.
     * 
     * @return The maximum.
     */
    public double getMaximum() {
        return maximum;
    }

    /**
     * Sets the maximum.
     * 
     * @param maximum
     *            The new maximum.
     */
    public void setMaximum(double maximum) {
        this.maximum = maximum;
    }

    @Override
    public String toString() {
        return "MeasurementSampleStatisticalData [sampleCount=" + sampleCount + ", variance=" + variance + ", standardDeviation=" + standardDeviation
                + ", mean=" + mean + ", median=" + median + ", minimum=" + minimum + ", maximum=" + maximum + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maximum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(mean);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(median);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minimum);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + (int) (sampleCount ^ (sampleCount >>> 32));
        temp = Double.doubleToLongBits(standardDeviation);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(variance);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

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
        MeasurementSampleStatisticalData other = (MeasurementSampleStatisticalData) obj;
        if (Double.doubleToLongBits(maximum) != Double.doubleToLongBits(other.maximum)) {
            return false;
        }
        if (Double.doubleToLongBits(mean) != Double.doubleToLongBits(other.mean)) {
            return false;
        }
        if (Double.doubleToLongBits(median) != Double.doubleToLongBits(other.median)) {
            return false;
        }
        if (Double.doubleToLongBits(minimum) != Double.doubleToLongBits(other.minimum)) {
            return false;
        }
        if (sampleCount != other.sampleCount) {
            return false;
        }
        if (Double.doubleToLongBits(standardDeviation) != Double.doubleToLongBits(other.standardDeviation)) {
            return false;
        }
        if (Double.doubleToLongBits(variance) != Double.doubleToLongBits(other.variance)) {
            return false;
        }
        return true;
    }

}
