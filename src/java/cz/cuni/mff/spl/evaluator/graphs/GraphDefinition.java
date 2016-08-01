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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.spl.conversion.AbstractXmlTransformationReference;
import cz.cuni.mff.spl.evaluator.statistics.DataClipper;
import cz.cuni.mff.spl.utils.EqualsUtils;
import cz.cuni.mff.spl.utils.parsers.GraphDefinitionParser;
import cz.cuni.mff.spl.utils.parsers.ParseException;

/**
 * Class for configuration of generated graphs.
 * 
 * @author Martin Lacina
 * 
 */
public class GraphDefinition extends AbstractXmlTransformationReference {

    /**
     * The graph type.
     */
    public enum GraphType {
        /** The default value, generate no graph. */
        NotDefined {
            @Override
            public String toPrettyString() {
                return "Not defined graph";
            }
        },

        /** The Histogram graph type. */
        Histogram {
            @Override
            public String toPrettyString() {
                return "Histogram";
            }
        },

        /** The Time diagram graph type. */
        TimeDiagram {
            @Override
            public String toPrettyString() {
                return "Time diagram";
            }
        },

        /** The Density comparison graph type. */
        DensityComparison {
            @Override
            public String toPrettyString() {
                return "Density comparison";
            }
        },

        /** The EmpiricalDistribution comparison graph type. */
        Edf {
            @Override
            public String toPrettyString() {
                return "Empirical Distribution Comparison";
            }
        };
        public abstract String toPrettyString();

    }

    /**
     * The data clip type to apply on measurement samples.
     */
    public enum DataClipType {

        /** Use raw sample data. */
        None,

        /**
         * Use sigma clipping.
         * <p>
         * Expects up to two non negative parameters.
         * <p>
         * First is multiplication for sigma (default 1).
         * <p>
         * Second is iteration count (default 1), integer value expected.
         * 
         * @see DataClipper#sigmaClip(List<Double>, double, double, double, int)
         */
        Sigma,

        /**
         * The Quantile clipping.
         * <p>
         * Expects up to two parameters.
         * <p>
         * The lower clip. In percent, i. e. value in interval [0.0, 100.0].
         * <p>
         * The upper clip. In percent, i. e. value in interval [0.0, 100.0].
         * <p>
         * When only one parameter specified, than it is used as lower and upper
         * clip.
         * <p>
         * When no parameter specified, than no clipping will be done.
         * 
         * @see DataClipper#quantileClip(List<Double>, double, double)
         */
        Quantile
    }

    /** The basic graph type. */
    private GraphType    basicGraphType;

    /** The data clip type. */
    private DataClipType dataClipType;

    /** The data clip parameters. */
    private List<Double> dataClipParameters;

    /**
     * Instantiates a new graph type configuration.
     */
    public GraphDefinition() {
        basicGraphType = GraphType.NotDefined;
        dataClipType = DataClipType.None;
        dataClipParameters = new ArrayList<Double>(0);
    }

    /**
     * Gets the basic graph type.
     * 
     * @return The basic graph type.
     */
    public GraphType getBasicGraphType() {
        return basicGraphType;
    }

    /**
     * Sets the basic graph type.
     * 
     * @param basicGraphType
     *            The new basic graph type.
     */
    public void setBasicGraphType(GraphType basicGraphType) {
        this.basicGraphType = basicGraphType;
    }

    /**
     * Gets the data clip type.
     * 
     * @return The data clip type.
     */
    public DataClipType getDataClipType() {
        return dataClipType;
    }

    /**
     * Sets the data clip type.
     * 
     * @param dataClipType
     *            The new data clip type.
     */
    public void setDataClipType(DataClipType dataClipType) {
        this.dataClipType = dataClipType;
    }

    /**
     * Gets the data clip parameters.
     * 
     * @return The data clip parameters.
     */
    public List<Double> getDataClipParameters() {
        return dataClipParameters;
    }

    /**
     * Sets the data clip parameters and ensures that minimal defaults are met.
     * <p>
     * Note that {@link #dataClipType} has to be set first to allow setting
     * defaults.
     * 
     * @param dataClipParameters
     *            The new data clip parameters.
     */
    public void setDataClipParameters(List<Double> dataClipParameters) {
        this.dataClipParameters = dataClipParameters;
        switch (this.dataClipType) {
            case None:
                break;
            case Quantile:
                if (this.dataClipParameters.isEmpty()) {
                    this.dataClipType = DataClipType.None;
                } else if (this.dataClipParameters.size() == 1) {
                    // duplicate value
                    this.dataClipParameters.add(this.dataClipParameters.get(0));
                }
                break;
            case Sigma:
                if (this.dataClipParameters.size() == 0) {
                    this.dataClipParameters.add(3.0d);
                }
                if (this.dataClipParameters.size() == 1) {
                    this.dataClipParameters.add(1.0d);
                }
                break;
            default:
                break;
        }

    }

    /**
     * Gets the sigma multiplier. Value is non negative.
     * <p>
     * Allowed only when data clip type is {@link DataClipType#Sigma}.
     * 
     * @return The non negative sigma multiplier.
     * 
     * @throws IllegalStateException
     *             Thrown when clip type is not {@link DataClipType#Sigma}.
     */
    public double getSigmaMultiplier() {
        if (this.dataClipType != DataClipType.Sigma) {
            throw new IllegalStateException("Allowed only when data clip type is " + DataClipType.Sigma.name());
        }
        return Math.max(this.dataClipParameters.get(0), 0);
    }

    /**
     * Gets the maximum iterations for sigma clipping. Value is non negative.
     * <p>
     * Allowed only when data clip type is {@link DataClipType#Sigma}.
     * 
     * @return The non negative maximum iterations for sigma clipping.
     * 
     * @throws IllegalStateException
     *             Thrown when clip type is not {@link DataClipType#Sigma}.
     */
    public int getSigmaMaxIteration() {
        if (this.dataClipType != DataClipType.Sigma) {
            throw new IllegalStateException("Allowed only when data clip type is " + DataClipType.Sigma.name());
        }
        return Math.max(this.dataClipParameters.get(1).intValue(), 0);
    }

    /**
     * <p>
     * Gets the lower clip for measurement sample quantile clipping. Value of
     * lower clip has to be non negative - when parameter value does not satisfy
     * this condition, then 0 is returned.
     * <p>
     * Value is in percent, i. e. value in interval [0.0, 100.0].
     * 
     * @return The upper clip for measurement sample quantile clipping.
     * 
     * @throws IllegalStateException
     *             Thrown when clip type is not {@link DataClipType#Quantile}.
     * 
     * @see DataClipper#quantileClip(double[], double, double)
     */
    public double getQuantileLowerClip() {
        if (this.dataClipType != DataClipType.Quantile) {
            throw new IllegalStateException("Allowed only when data clip type is " + DataClipType.Quantile.name());
        }
        return Math.min(Math.max(this.dataClipParameters.get(0).doubleValue(), 0), 100);
    }

    /**
     * <p>
     * Gets the upper clip for measurement sample quantile clipping. Value of
     * upper clip has to be higher than {@link #getQuantileLowerClip()} - when
     * parameter value does not satisfy this condition, then 100 is returned.
     * <p>
     * Value is in percent, i. e. value in interval [0.0, 100.0].
     * 
     * @return The upper clip for measurement sample quantile clipping.
     * 
     * @throws IllegalStateException
     *             Thrown when clip type is not {@link DataClipType#Quantile}.
     * 
     * @see DataClipper#quantileClip(double[], double, double)
     */
    public double getQuantileUpperClip() {
        if (this.dataClipType != DataClipType.Quantile) {
            throw new IllegalStateException("Allowed only when data clip type is " + DataClipType.Quantile.name());
        }

        double lowerClip = getQuantileLowerClip();
        double upperClip = Math.min(Math.max(this.dataClipParameters.get(1).doubleValue(), 0), 100);
        if (upperClip <= lowerClip) {
            upperClip = 100;
        }
        return upperClip;
    }

    /**
     * Creates the graph definition.
     * 
     * @param graphType
     *            The graph type.
     * @param dataClipType
     *            The data clip type.
     * @param parameters
     *            The parameters.
     * @return The graph type configuration.
     */
    public static GraphDefinition createGraphDefinition(GraphType graphType, DataClipType dataClipType, Double... parameters) {
        return createGraphDefinition(graphType, dataClipType, Arrays.asList(parameters));
    }

    /**
     * Creates the graph definition.
     * 
     * @param graphType
     *            The graph type.
     * @param dataClipType
     *            The data clip type.
     * @param parameters
     *            The parameters.
     * @return The graph type configuration.
     */
    public static GraphDefinition createGraphDefinition(GraphType graphType, DataClipType dataClipType, List<Double> parameters) {
        GraphDefinition result = new GraphDefinition();
        result.setBasicGraphType(graphType);
        result.setDataClipType(dataClipType);
        result.setDataClipParameters(parameters);
        return result;
    }

    /**
     * Hash code.
     * 
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((basicGraphType == null) ? 0 : basicGraphType.ordinal());
        result = prime * result + ((dataClipType == null) ? 0 : dataClipType.ordinal());
        result = prime * result + ((dataClipParameters == null) ? 0 : dataClipParameters.hashCode());
        return result;
    }

    /**
     * Equals.
     * 
     * @param obj
     *            The obj.
     * @return True, if successful.
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
        GraphDefinition other = (GraphDefinition) obj;
        if (basicGraphType != other.basicGraphType) {
            return false;
        }
        if (dataClipType != other.dataClipType) {
            return false;
        }
        return EqualsUtils.safeEquals(dataClipParameters, other.dataClipParameters);
    }

    @Override
    public String toString() {
        return "GraphTypeConfiguration [basicGraphType=" + basicGraphType + ", dataClipType=" + dataClipType + ", dataClipParameters=" + dataClipParameters
                + "]";
    }

    public String getDataClipTypePrettyString() {
        switch (this.dataClipType) {
            case Quantile:
                return String.format("quantile clipped samples, %.1f %% - %.1f %%", getQuantileLowerClip(), getQuantileUpperClip());
            case Sigma:
                if (getSigmaMaxIteration() > 1) {
                    return String.format("sigma clipped samples (%s-times), %.1f * sigma", getSigmaMaxIteration(), getSigmaMultiplier());
                } else {
                    return String.format("sigma clipped samples, %.1f * sigma", getSigmaMultiplier());
                }
            case None:
                return "all samples";
            default:
                throw new UnsupportedOperationException(String.format("Unknown data clip type '%s'", dataClipType));
        }
    }

    public String getGraphNamePrettyString() {
        return String.format("%s(%s)", getBasicGraphType().toPrettyString(), getDataClipTypePrettyString());
    }

    /**
     * <p>
     * Gets the string which represents this instance in graph definition
     * grammar.
     * 
     * @return The string represents this instance in graph definition grammar.
     */
    public String getParserString() {
        return GraphDefinitionParser.createGraphDeclarationString(this);
    }

    /**
     * <p>
     * Sets the string which represents this instance in graph definition
     * grammar. Graph definition parser is used to get graph definition values.
     * 
     * @param definition
     *            The new parser string.
     * @throws ParseException
     *             The parse exception is thrown when provided definition does
     *             not represent valid graph definition.
     */
    public void setParserString(String definition) throws ParseException {
        GraphDefinition parsed = GraphDefinitionParser.ParseGraphDefinition(definition);
        this.setBasicGraphType(parsed.getBasicGraphType());
        this.setDataClipType(parsed.getDataClipType());
        this.setDataClipParameters(parsed.getDataClipParameters());
    }

}
