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

import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 * Class providing sigma and quantile clipping capabilities to sample data
 * arrays.
 * 
 * @author Martin Lacina
 * 
 */
public class DataClipper {

    /**
     * Clip sample data based on provided standard deviation (sigma) multiplier.
     * Allows to set maximum number of iterations.
     * Sample data clipping is performed until data convergence state is
     * achieved (all samples lie around mean and not farther than
     * 
     * @param data
     *            The sample data.
     * @param sigmaMultiplier
     *            The standard deviation multiplier.
     * @param maxIterations
     *            The max iterations, one iteration will be always done.
     * @return Clipped sample data. {@code sigmaMultiplier * sigma} around
     *         {@code mean} or maximum number of
     *         iterations were done, whatever comes first.
     * 
     *         Note that new mean and sigma is computed for clipped data for
     *         recursive
     *         call.
     */

    public static double[] sigmaClip(double[] data, double sigmaMultiplier, int maxIterations) {

        double mean = SimpleStatisticalFunctions.mean(data);
        double sigma = SimpleStatisticalFunctions.sd(data);

        final double clipLow = mean - (sigmaMultiplier * sigma);
        final double clipUp = mean + (sigmaMultiplier * sigma);

        double[] newData = filterData(data, new AcceptableValue() {

            @Override
            public boolean isAcceptable(double value) {
                return (value >= clipLow && value <= clipUp);
            }
        });

        if (newData.length != data.length && --maxIterations > 0) {
            newData = sigmaClip(newData, sigmaMultiplier, maxIterations);
        }
        return newData;

    }

    /**
     * Clip sample data based on provided percentile values.
     * 
     * The upper clip value has to be higher than lower clip value.
     * 
     * @param data
     *            The sample data.
     * @param lowerClip
     *            The lower clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @param upperClip
     *            The upper clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @return Clipped sample data.
     * 
     * @throws IllegalArgumentException
     *             When provided clip values are outside of range [0.0, 100.0]
     *             or when lower clip is higher than upper clip.
     * 
     * @see Percentile
     */
    public static double[] quantileClip(double[] data, double lowerClip,
            double upperClip) {

        if (lowerClip < 0 || lowerClip > 100 || upperClip < 0
                || upperClip > 100) {
            throw new IllegalArgumentException(
                    "Clip has to be in interval [0.0, 100.0].");
        }
        if (lowerClip >= upperClip) {
            throw new IllegalArgumentException(
                    "Lower clip has to be lower than upper clip.");
        }

        final double clipLow = (lowerClip == 0 ? 0 : StatUtils.percentile(data, lowerClip));
        final double clipUp = StatUtils.percentile(data, upperClip);

        return filterData(data, new AcceptableValue() {

            @Override
            public boolean isAcceptable(double value) {
                return (value >= clipLow && value <= clipUp);
            }
        });
    }

    /**
     * Filters data. Always works on copy of input data.
     * 
     * @param data
     *            The data to filter.
     * @param acceptor
     *            The value acceptor.
     * @return Filtered data.
     */
    public static double[] filterData(final double[] data, AcceptableValue acceptor) {

        double[] newData = Arrays.copyOf(data, data.length);

        int targetIndex = 0;

        for (int index = 0; index < newData.length; ++index) {
            double value = newData[index];
            if (acceptor.isAcceptable(value)) {
                newData[targetIndex] = value;
                ++targetIndex;
            }
        }

        if (targetIndex == newData.length) {
            return newData;
        } else {
            return Arrays.copyOf(newData, targetIndex);
        }
    }
}
