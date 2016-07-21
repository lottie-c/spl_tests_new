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
 * Interface for statistical value checks.
 * 
 * Defines which output implementations are enabled.
 * 
 * Provides p-value for t-test.
 * 
 * Contains values used for marking suspicious values in output (such as big
 * difference between median and mean).
 * 
 * @author Martin Lacina
 * 
 */
public interface StatisticValueChecker {

    /**
     * Checks if is sample count acceptable.
     * 
     * @param sampleCount
     *            The sample count.
     * @return true, if is sample count acceptable
     */
    boolean isSampleCountAcceptable(long sampleCount);

    /**
     * Checks if is standard deviation vs mean acceptable.
     * 
     * @param standardDeviationVsMean
     *            The standard deviation vs mean.
     * @return true, if is standard deviation vs mean acceptable
     */
    boolean isStandardDeviationVsMeanAcceptable(double standardDeviationVsMean);

    /**
     * Checks if is median vs mean acceptable.
     * 
     * @param medianVsMean
     *            The median vs mean.
     * @return true, if is median vs mean acceptable
     */
    boolean isMedianVsMeanAcceptable(double medianVsMean);

    /**
     * Checks if is t-test p-value is acceptable.
     * 
     * @param pValue
     *            The value.
     * @return True, if is p-value is acceptable.
     */
    boolean isPvalueAcceptable(double pValue);

    /**
     * Gets the limit p-value for t-test.
     * 
     * @return The limit p-value for t-test.
     */
    double getPvalueLimit();
}
