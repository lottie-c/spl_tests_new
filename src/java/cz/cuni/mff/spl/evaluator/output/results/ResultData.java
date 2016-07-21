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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.evaluator.output.impl.graphs.GraphKeyFactory;
import cz.cuni.mff.spl.evaluator.output.impl.xml.ComparisonGraphReference;
import cz.cuni.mff.spl.evaluator.output.impl.xml.MeasurementGraphReference;
import cz.cuni.mff.spl.evaluator.statistics.MeasurementSample;

/**
 * This class represents evaluation results representation.
 * 
 * @author Martin Lacina
 * 
 */
public class ResultData {

    /** The configuration. */
    private ConfigurationBundle                          configuration;

    /** The evaluated context. */
    private Info                                         info;

    /** The annotation evaluation results. */
    private Set<AnnotationEvaluationResult>              annotationEvaluationResults = new LinkedHashSet<>();

    /** The measurement samples. */
    private Set<MeasurementSample>                       measurementSamples          = new LinkedHashSet<>();

    /** The generated comparison graphs mapping. */
    private final Map<Object, ComparisonGraphReference>  comparisonGraphs            = new LinkedHashMap<>();

    /** The generated measurement graphs mapping. */
    private final Map<Object, MeasurementGraphReference> measurementGraphs           = new LinkedHashMap<>();

    /**
     * Instantiates a new empty result data.
     * <p>
     * For XML transformation only.
     */
    @Deprecated
    public ResultData() {

    }

    /**
     * Gets the configuration.
     * 
     * @return The configuration.
     */
    public ConfigurationBundle getConfiguration() {
        return configuration;
    }

    /**
     * Sets the configuration.
     * 
     * @param configuration
     *            The new configuration.
     */
    public void setConfiguration(ConfigurationBundle configuration) {
        this.configuration = configuration;
    }

    /**
     * Instantiates a new result data.
     * 
     * @param info
     *            The info.
     */
    public ResultData(Info info) {
        Info i = info.getClone();
        this.info = i;
    }

    /**
     * Gets the evaluated Info instance.
     * 
     * @return The evaluated Info instance.
     */
    public Info getInfo() {
        return info;
    }

    /**
     * Sets the evaluated Info instance.
     * <p>
     * For XML transformation only.
     * 
     * @param info
     *            The new evaluated Info instance.
     */
    @Deprecated
    public void setInfo(Info info) {
        this.info = info;
    }

    /**
     * Gets the annotation evaluation results.
     * 
     * @return The annotation evaluation results.
     */
    public Set<AnnotationEvaluationResult> getAnnotationEvaluationResults() {
        return annotationEvaluationResults;
    }

    /**
     * Sets the annotation evaluation results.
     * <p>
     * For XML transformation only.
     * 
     * @param annotationEvaluationResults
     *            The new annotation evaluation results.
     */
    @Deprecated
    public void setAnnotationEvaluationResults(Set<AnnotationEvaluationResult> annotationEvaluationResults) {
        this.annotationEvaluationResults = annotationEvaluationResults;
    }

    /**
     * Gets the measurement samples.
     * 
     * @return The measurement samples.
     */
    public Set<MeasurementSample> getMeasurementSamples() {
        return measurementSamples;
    }

    /**
     * Sets the measurement samples.
     * <p>
     * For XML transformation only.
     * 
     * @param measurementSamples
     *            The new measurement samples.
     */
    @Deprecated
    public void setMeasurementSamples(Set<MeasurementSample> measurementSamples) {
        this.measurementSamples = measurementSamples;
    }

    /**
     * Gets the comparison graph references.
     * 
     * @return The comparison graph references.
     */
    public Map<Object, ComparisonGraphReference> getComparisonGraphs() {
        return comparisonGraphs;
    }

    /**
     * Adds the comparison graph reference.
     * 
     * @param reference
     *            The measurement graph reference.
     */
    public void addComparisonGraphReference(ComparisonGraphReference reference) {
        Object graphKey = GraphKeyFactory.createGraphKey(reference.getComparison(), reference.getGraphType());
        comparisonGraphs.put(graphKey, reference);
    }

    /**
     * Gets the measurement graph references.
     * 
     * @return The measurement graph references.
     */
    public Map<Object, MeasurementGraphReference> getMeasurementGraphs() {
        return measurementGraphs;
    }

    /**
     * Adds the measurement graph reference.
     * 
     * @param reference
     *            The measurement graph reference.
     */
    public void addMeasurementGraphReference(MeasurementGraphReference reference) {
        Object graphKey = GraphKeyFactory.createGraphKey(reference.getMeasurement(), reference.getGraphType());
        measurementGraphs.put(graphKey, reference);
    }

    // generated by eclipse
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((annotationEvaluationResults == null) ? 0 : annotationEvaluationResults.hashCode());
        result = prime * result + ((comparisonGraphs == null) ? 0 : comparisonGraphs.hashCode());
        result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
        result = prime * result + ((info == null) ? 0 : info.hashCode());
        result = prime * result + ((measurementGraphs == null) ? 0 : measurementGraphs.hashCode());
        result = prime * result + ((measurementSamples == null) ? 0 : measurementSamples.hashCode());
        return result;
    }

    // generated by eclipse
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
        ResultData other = (ResultData) obj;
        if (annotationEvaluationResults == null) {
            if (other.annotationEvaluationResults != null) {
                return false;
            }
        } else if (!annotationEvaluationResults.equals(other.annotationEvaluationResults)) {
            return false;
        }
        if (comparisonGraphs == null) {
            if (other.comparisonGraphs != null) {
                return false;
            }
        } else if (!comparisonGraphs.equals(other.comparisonGraphs)) {
            return false;
        }
        if (configuration == null) {
            if (other.configuration != null) {
                return false;
            }
        } else if (!configuration.equals(other.configuration)) {
            return false;
        }
        if (info == null) {
            if (other.info != null) {
                return false;
            }
        } else if (!info.equals(other.info)) {
            return false;
        }
        if (measurementGraphs == null) {
            if (other.measurementGraphs != null) {
                return false;
            }
        } else if (!measurementGraphs.equals(other.measurementGraphs)) {
            return false;
        }
        if (measurementSamples == null) {
            if (other.measurementSamples != null) {
                return false;
            }
        } else if (!measurementSamples.equals(other.measurementSamples)) {
            return false;
        }
        return true;
    }

    // generated by eclipse
    @Override
    public String toString() {
        return "ResultData [configuration=" + configuration + ", info=" + info + ", annotationEvaluationResults=" + annotationEvaluationResults
                + ", measurementSamples=" + measurementSamples + ", comparisonGraphs=" + comparisonGraphs + ", measurementGraphs=" + measurementGraphs + "]";
    }

}
