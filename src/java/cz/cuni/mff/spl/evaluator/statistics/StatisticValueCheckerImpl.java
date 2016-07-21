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

import cz.cuni.mff.spl.configuration.SplEvaluatorConfiguration;

/**
 * Statistical value checker implementation based on
 * {@link SplEvaluatorConfiguration} instance.
 * 
 * @author Martin Lacina
 * 
 */
public class StatisticValueCheckerImpl implements StatisticValueChecker {

    /**
     * The limit p-value for statistical t-test.
     * 
     * Comparison of measurement is satisfied when t-test p-value is higher than
     * limit.
     */
    private final double TTestLimitPValue;

    /**
     * The minimum sample count warning limit.
     * 
     * Warning will be issued, when sample count is below this limit.
     */
    private final long   minimumSampleCount;

    /** The maximum standard deviation compared to mean difference multiplier. */
    private final double maximumStandardDeviationVsMeanDifference;

    /** The maximum median compared to mean difference lower multiplier. */
    private final double maximumMedianVsMeanDifferenceLower;

    /** The maximum median compared to mean difference upper multiplier. */
    private final double maximumMedianVsMeanDifferenceUpper;

    /**
     * Instantiates a new evaluator configuration statistic value checker with
     * provided configuration.
     * 
     * @param configuration
     *            The configuration.
     */
    public StatisticValueCheckerImpl(SplEvaluatorConfiguration configuration) {

        this.TTestLimitPValue = configuration.getTTestLimitPValue();

        this.minimumSampleCount = configuration.getMinimumSampleCountWarningLimit();

        this.maximumStandardDeviationVsMeanDifference = configuration.getMaximumStandardDeviationVsMeanDifferenceWarningLimit() / 100d;

        double maximumMedianVsMeanDifference = Math.abs(configuration.getMaximumMedianVsMeanDifferenceWarningLimit()) / 100d;
        this.maximumMedianVsMeanDifferenceLower = Math.max(1 - maximumMedianVsMeanDifference, 0);
        this.maximumMedianVsMeanDifferenceUpper = 1 + maximumMedianVsMeanDifference;
    }

    @Override
    public boolean isSampleCountAcceptable(long sampleCount) {
        return sampleCount > minimumSampleCount;
    }

    @Override
    public boolean isStandardDeviationVsMeanAcceptable(double standardDeviationVsMean) {
        return standardDeviationVsMean <= maximumStandardDeviationVsMeanDifference;
    }

    @Override
    public boolean isMedianVsMeanAcceptable(double medianVsMean) {
        return ((medianVsMean >= maximumMedianVsMeanDifferenceLower) && (medianVsMean <= maximumMedianVsMeanDifferenceUpper));
    }

    @Override
    public double getPvalueLimit() {
        return TTestLimitPValue;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Tested null hypothesis is that the means of the populations from which
     * the two samples were taken are equal.
     * <p>
     * If the calculated p-value is below the threshold chosen for statistical
     * significance (usually the 0.10, the 0.05, or 0.01 level), then the null
     * hypothesis is rejected in favor of the alternative hypothesis.
     */
    @Override
    public boolean isPvalueAcceptable(double pValue) {
        return !(pValue < TTestLimitPValue);
    };

}
