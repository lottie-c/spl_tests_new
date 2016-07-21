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

import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.rank.Median;

import cz.cuni.mff.spl.deploy.build.SampleIdentification;
import cz.cuni.mff.spl.deploy.store.IStore;
import cz.cuni.mff.spl.deploy.store.IStoreReadonly;
import cz.cuni.mff.spl.deploy.store.exception.StoreException;
import cz.cuni.mff.spl.deploy.store.utils.MeasurementData;
import cz.cuni.mff.spl.evaluator.statistics.DataClipper;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSampleStatisticalData;
import cz.cuni.mff.spl.utils.logging.SplLog;
import cz.cuni.mff.spl.utils.logging.SplLogger;

/**
 * Basic measurement data provider which loads data from specified
 * {@link IStore} instance.
 * 
 * @author Martin Lacina
 */
public class StoreMeasurementDataProvider implements MeasurementDataProvider {

    /** The logger. */
    private static final SplLog  logger = SplLogger.getLogger(StoreMeasurementDataProvider.class);

    /** The store. */
    private final IStoreReadonly store;

    /**
     * Instantiates a new folder measurement data provider.
     * 
     * @param store
     *            The store.
     */
    public StoreMeasurementDataProvider(IStoreReadonly store) {
        if (store == null) {
            logger.debug("Store can't be null.");
            throw new IllegalArgumentException("Store can't be null.");
        }
        this.store = store;
    }

    /**
     * Gets the measurement data.
     * 
     * @param identification
     *            The identification.
     * @return The measurement data.
     * @throws MeasurementDataNotFoundException
     *             The measurement data not found exception.
     */
    @Override
    public MeasurementSampleDataProvider getMeasurementData(SampleIdentification identification) throws MeasurementDataNotFoundException {
        try {
            if (this.store.measurementExists(identification)) {
                return new MeasurementDataImpl(identification);
            }
        } catch (StoreException e) {
            logger.debug(e, "Unable to load data for measurement sample [%s]", identification.getIdentification());
            throw new MeasurementDataNotFoundException(identification, e);
        }
        logger.debug("Unable to load data for measurement sample [%s]", identification.getIdentification());
        throw new MeasurementDataNotFoundException(identification);
    }

    /**
     * Checks if measurement exists.
     * 
     * @param identification
     *            The identification.
     * @return True, if measurement exists, otherwise false (even when store
     *         failed to check with exception).
     */
    @Override
    public boolean measurementExists(SampleIdentification identification) {
        try {
            return this.store.measurementExists(identification);
        } catch (StoreException e) {
            logger.error("Unable to load check if measurement data exists '%s'", identification.getIdentification());
        }
        return false;
    }

    /**
     * The Class MeasurementDataImpl.
     */
    private class MeasurementDataImpl implements MeasurementSampleDataProvider {

        /** The identification. */
        private final SampleIdentification       identification;

        /** The cache buffer. */
        private double[]                         cacheBuffer;

        /** The statistical summary. */
        private StatisticalSummary               statisticalSummary;

        /** The statistical data. */
        private MeasurementSampleStatisticalData statisticalData;

        /**
         * Instantiates a new measurement data instance.
         * 
         * @param identification
         *            The measurement sample identification.
         */
        public MeasurementDataImpl(SampleIdentification identification) {
            this.identification = identification;
        }

        /**
         * Acquire data to cache.
         * 
         * @throws MeasurementDataNotFoundException
         *             The measurement data not found exception.
         */
        @Override
        public void acquireDataToCache() throws MeasurementDataNotFoundException {
            if (cacheBuffer == null) {
                cacheBuffer = loadRawMeasurementData();
            }
        }

        /**
         * Release data from cache.
         */
        @Override
        public void releaseDataFromCache() {
            this.cacheBuffer = null;
        }

        /**
         * Same as calling {@link #loadMeasurementData(double)} with lambda
         * multiplier 1.
         */
        private double[] loadRawMeasurementData() throws MeasurementDataNotFoundException {
            return loadMeasurementData(1);
        }

        /**
         * Loads measurement sample data.
         * 
         * @param identification
         *            The measurement sample identification.
         * @param buffer
         *            The buffer to use.
         * @return The measurement sample data.
         * @throws MeasurementDataNotFoundException
         *             Thrown when the measurement sample data were not found.
         */
        private double[] loadMeasurementData(double lambdaMultiplier) throws MeasurementDataNotFoundException {
            try (MeasurementData data = new MeasurementData(StoreMeasurementDataProvider.this.store.loadMeasurement(identification))) {
                double[] dataArray = data.readSamples(lambdaMultiplier);

                if (Double.doubleToLongBits(lambdaMultiplier) == Double.doubleToLongBits(1)) {
                    if (statisticalSummary == null) {
                        statisticalSummary = new DescriptiveStatistics(dataArray);
                    }
                    if (statisticalData == null) {
                        statisticalData = new MeasurementSampleStatisticalData();
                        statisticalData.warmupCount = data.getWarmupCount();
                        statisticalData.measuredDate = data.getDate();

                        statisticalData.sampleCount = statisticalSummary.getN();
                        statisticalData.standardDeviation = statisticalSummary.getStandardDeviation();
                        statisticalData.mean = statisticalSummary.getMean();
                        statisticalData.median = new Median().evaluate(dataArray);
                        statisticalData.minimum = statisticalSummary.getMin();
                        statisticalData.maximum = statisticalSummary.getMax();
                    }
                }
                return dataArray;
            } catch (StoreException | IOException e) {
                logger.warn(e, "Unable to load data for measurement sample '%s'", identification.getIdentification());
                throw new MeasurementDataNotFoundException(identification, e);
            }
        }

        /**
         * Load raw data.
         * 
         * @return The double[].
         * @throws MeasurementDataNotFoundException
         *             The measurement data not found exception.
         */
        @Override
        public double[] loadRawData(double lambdaMultiplier) throws MeasurementDataNotFoundException {
            if (lambdaMultiplier <= 0) {
                System.out.println("lambdaMultiplier <=0");
            }

            if (cacheBuffer != null && Double.doubleToLongBits(lambdaMultiplier) == Double.doubleToLongBits(1)) {
                return cacheBuffer;
            } else if (cacheBuffer != null) {
                double[] result = new double[cacheBuffer.length];
                for (int i = 0; i < cacheBuffer.length; ++i) {
                    result[i] = lambdaMultiplier * cacheBuffer[i];
                }
                return result;
            } else {
                return loadMeasurementData(lambdaMultiplier);
            }
        }

        @Override
        public double[] loadSigmaClippedData(double lambdaMultiplier, double sigmaMultiplier, int maxIterations) throws MeasurementDataNotFoundException {
            return DataClipper.sigmaClip(loadRawData(lambdaMultiplier), sigmaMultiplier, maxIterations);
        }

        @Override
        public double[] loadQuantileClippedData(double lambdaMultiplier, double lowerClip, double upperClip) throws MeasurementDataNotFoundException {
            return DataClipper.quantileClip(loadRawData(lambdaMultiplier), lowerClip, upperClip);
        }

        @Override
        public MeasurementSampleStatisticalData loadMeasurementDescriptionSummaryForRawData()
                throws MeasurementDataNotFoundException {
            if (statisticalData == null) {
                loadRawMeasurementData();
            }
            return statisticalData;
        }

        @Override
        public StatisticalSummary loadStatisticalSummaryForRawData()
                throws MeasurementDataNotFoundException {
            if (statisticalSummary == null) {
                loadRawMeasurementData();
            }
            return statisticalSummary;
        }

    }

}
