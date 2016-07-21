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
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import java.util.ArrayList;
import java.util.Collection;

import cz.cuni.mff.spl.annotation.Info;
import cz.cuni.mff.spl.configuration.ConfigurationBundle;
import cz.cuni.mff.spl.deploy.store.IStore.IStoreDirectory.IStoreFile;
import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;
import cz.cuni.mff.spl.evaluator.output.BasicOutputFileMapping;
import cz.cuni.mff.spl.evaluator.output.impl.graphs.GraphKeyFactory;
import cz.cuni.mff.spl.evaluator.output.impl.html2.AnnotationResultDescriptor.AnnotationValidationFlags;

/**
 * The Class OutputResultDescriptor.
 * 
 * @author Martin Lacina
 */
public class OutputResultDescriptor {

    /** The configuration. */
    protected ConfigurationBundle configuration;

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public ConfigurationBundle getConfiguration() {
        return configuration;
    }

    /** The info. */
    protected Info info;

    /**
     * Gets the info.
     * 
     * @return The info.
     */
    public Info getInfo() {
        return info;
    }

    /** The measurement graphs. */
    protected final ArrayList<GraphReference> graphs = new ArrayList<>();

    /**
     * Gets the measurement graphs.
     * 
     * @return the measurement graphs
     */
    public ArrayList<GraphReference> getGraphs() {
        return graphs;
    }

    /** The global aliases summary. */
    private final AnnotationValidationFlags globalAliasesSummary;

    /**
     * Gets the global aliases summary.
     * 
     * @return The global aliases summary.
     */
    public AnnotationValidationFlags getGlobalAliasesSummary() {
        return globalAliasesSummary;
    }

    /** The links. */
    protected ArrayList<Link> links;

    /**
     * Gets the links.
     * 
     * @return The links.
     */
    public ArrayList<Link> getLinks() {
        return links;
    }

    /**
     * Instantiates a new output result descriptor.
     * 
     * @param info
     *            The info.
     * @param configuration2
     *            The configuration.
     * @param outputLinks
     *            The output links.
     * @param globalAliasesSummary
     *            The global aliases summary.
     */
    public OutputResultDescriptor(Info info, ConfigurationBundle configuration2, ArrayList<Link> outputLinks,
            AnnotationValidationFlags globalAliasesSummary) {
        this.info = info;
        this.configuration = configuration2;
        this.links = outputLinks;
        this.globalAliasesSummary = globalAliasesSummary;
    }

    /**
     * Fills the graph references.
     * 
     * @param graphsMapping
     *            The graphs mapping.
     * @param graphToLookFor
     *            The graph to look for.
     * @param graphKeyRelatedObject
     *            The graph key related object.
     */
    protected void fillGraphReferences(BasicOutputFileMapping graphsMapping, Collection<GraphDefinition> graphToLookFor, Object graphKeyRelatedObject) {
        if (graphsMapping != null) {
            for (GraphDefinition graphType : graphToLookFor) {
                Object graphKey = GraphKeyFactory.createGraphKey(graphKeyRelatedObject, graphType);
                IStoreFile graphFile = graphsMapping.getIStoreFile(graphKey);
                if (graphFile != null) {
                    String filename = graphFile.getName();
                    GraphReference reference = new GraphReference(graphType, filename);
                    this.graphs.add(reference);
                }
            }
        }
    }

    /**
     * Drops the configuration and info instances.
     */
    public void dropSharedInstances() {
        this.configuration = null;
        this.info = null;
        this.links = null;
    }
}
