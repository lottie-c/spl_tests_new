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
package cz.cuni.mff.spl.evaluator.input;

import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import cz.cuni.mff.spl.deploy.store.IStore;
import cz.cuni.mff.spl.evaluator.input.MeasurementDataProvider.MeasurementDataNotFoundException;
import cz.cuni.mff.spl.evaluator.statistics.DataClipper;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSampleStatisticalData;

/**
 * <p>
 * Interface for acquiring measurement sample data for specific measurement.
 * 
 * @author Martin Lacina
 * 
 */
public interface MeasurementSampleDataProvider {

    /**
     * Tells measurement sample data provider, that it should cache raw data.
     * <p>
     * When data are in cache, than each call to {@link #loadData()} returns
     * cached data.
     * <p>
     * Data stay cached until {@link #releaseDataFromCache()} is called.
     * <p>
     * When called when data are already in cache, than no action is done.
     * 
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     * 
     * @see #releaseDataFromCache()
     * @see #loadData()
     */
    void acquireDataToCache()
            throws MeasurementDataNotFoundException;

    /**
     * Tells measurement sample data provider, that cached data will not be
     * needed.
     * <p>
     * Every next call to {@link #loadData()} will load data from {@link IStore}
     * instance without caching.
     * 
     * @see #acquireDataToCache()
     * @see #loadData()
     */
    void releaseDataFromCache();

    /**
     * Loads raw measurement sample data.
     * <p>
     * When {@link #acquireDataToCache()} was called and there was no
     * <p>
     * When data are in cache, than each call to {@link #loadData()} returns
     * cached data.
     * 
     * @return The measurement sample data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     * 
     * @see #acquireDataToCache()
     * @see #releaseDataFromCache()
     */
    double[] loadRawData(double lambdaMultiplier)
            throws MeasurementDataNotFoundException;

    /**
     * Returns sigma clipped measurement sample data.
     * 
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @param sigmaMultiplier
     *            The sigma multiplier.
     * @param maxIterations
     *            The max iterations.
     * @return The sigma clipped data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     * @see DataClipper#sigmaClip(double[], double, int)
     */
    double[] loadSigmaClippedData(double lambdaMultiplier, double sigmaMultiplier, int maxIterations)
            throws MeasurementDataNotFoundException;

    /**
     * <p>
     * Returns quantile clipped measurement sample data.
     * 
     * @param lambdaMultiplier
     *            The lambda multiplier.
     * @param lowerClip
     *            The lower clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @param upperClip
     *            The upper clip. In percent, i. e. value in interval [0.0,
     *            100.0].
     * @return Clipped sample data.
     * @throws MeasurementDataNotFoundException
     *             Thrown when the measurement sample data were not found.
     * @see DataClipper#quantileClip(MeasurementSample, double, double)
     * @see Percentile
     */
    double[] loadQuantileClippedData(double lambdaMultiplier, double lowerClip, double upperClip)
            throws MeasurementDataNotFoundException;

    /**
     * Loads the statistical summary.
     * 
     * @return The statistical summary.
     * @throws MeasurementDataNotFoundException
     */
    StatisticalSummary loadStatisticalSummaryForRawData()
            throws MeasurementDataNotFoundException;

    /**
     * Loads the measurement description summary.
     * 
     * @return The measurement sample statistical data.
     */
    MeasurementSampleStatisticalData loadMeasurementDescriptionSummaryForRawData()
            throws MeasurementDataNotFoundException;

}
