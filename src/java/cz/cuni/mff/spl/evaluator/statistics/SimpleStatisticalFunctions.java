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

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 * <p>
 * Simple statistical functions for sample data.
 * <p>
 * Method names are same as in the R project for convenience.
 * 
 * @author Martin Lacina
 */
public class SimpleStatisticalFunctions {

    /**
     * Calculates standard deviation of sample.
     * 
     * @param data
     *            The sample data.
     * @return The standard deviation of sample.
     */
    public static double sd(double[] data) {
        StandardDeviation sd = new StandardDeviation();
        return sd.evaluate(data);
    }

    /**
     * Calculates mean of sample.
     * 
     * @param data
     *            The sample data.
     * @return The mean of sample.
     */
    public static double mean(double[] data) {
        Mean mean = new Mean();
        return mean.evaluate(data);
    }

    /**
     * Calculates median of sample.
     * 
     * @param data
     *            The sample data.
     * @return The median of sample.
     */
    public static double median(double[] data) {
        Median median = new Median();
        return median.evaluate(data);
    }

    /**
     * Calculates minimal value of sample.
     * 
     * @param data
     *            The sample data.
     * @return The minimal value of sample.
     */
    public static double min(double[] data) {
        return StatUtils.min(data);
    }

    /**
     * Calculates maximal value of sample.
     * 
     * @param data
     *            The sample data.
     * @return The maximal value of sample.
     */
    public static double max(double[] data) {
        return StatUtils.max(data);
    }
}
