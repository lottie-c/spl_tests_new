/**
 * 
 */
package cz.cuni.mff.spl.evaluator.output.impl.html2;

import cz.cuni.mff.spl.evaluator.graphs.GraphDefinition;

/**
 * The measurement graph reference.
 */
public class GraphReference {

    public String graphName;

    public String filename;

    public String gdid;

    /**
     * Instantiates a new measurement graph.
     */
    public GraphReference() {
    }

    /**
     * Instantiates a new measurement graph.
     * 
     * @param graphType
     *            The graph type.
     * @param filename
     *            The filename.
     */
    @SuppressWarnings("deprecation")
    public GraphReference(GraphDefinition graphType, String filename) {
        this.graphName = graphType.getGraphNamePrettyString();
        this.filename = filename;
        this.gdid = graphType.getId();
    }
}