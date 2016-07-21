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
package cz.cuni.mff.spl.evaluator.output.impl.xml;

import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.output.results.ComparisonEvaluationResult;

/**
 * Allows interchange of information about comparison graph data.
 * 
 * @author Martin Lacina
 */
public class ComparisonGraphReference {

    /** The graph type. */
    private GraphDefinition            graphType;

    /** The comparison. */
    private ComparisonEvaluationResult comparison;

    /** The graph file name. */
    private String                     graphFileName;

    /**
     * Instantiates a new comparison graph reference.
     */
    public ComparisonGraphReference() {
    }

    /**
     * Instantiates a new comparison graph reference.
     * 
     * @param graphType
     *            The graph type.
     * @param comparison
     *            The comparison.
     * @param graphFileName
     *            The string.
     */
    public ComparisonGraphReference(GraphDefinition graphType, ComparisonEvaluationResult comparison, String graphFileName) {
        this.graphType = graphType;
        this.comparison = comparison;
        this.graphFileName = graphFileName;
    }

    /**
     * Gets the graph type.
     * 
     * @return The graph type.
     */
    public GraphDefinition getGraphType() {
        return graphType;
    }

    /**
     * Sets the graph type.
     * 
     * @param graphType
     *            The new graph type.
     */
    public void setGraphType(GraphDefinition graphType) {
        this.graphType = graphType;
    }

    /**
     * Gets the comparison.
     * 
     * @return The comparison.
     */
    public ComparisonEvaluationResult getComparison() {
        return comparison;
    }

    /**
     * Sets the comparison.
     * 
     * @param comparison
     *            The new comparison.
     */
    public void setComparison(ComparisonEvaluationResult comparison) {
        this.comparison = comparison;
    }

    /**
     * Gets the graph file name.
     * 
     * @return The graph file name.
     */
    public String getGraphFileName() {
        return graphFileName;
    }

    /**
     * Sets the graph file name.
     * 
     * @param graphFileName
     *            The new graph file name.
     */
    public void setGraphFileName(String graphFileName) {
        this.graphFileName = graphFileName;
    }

}
