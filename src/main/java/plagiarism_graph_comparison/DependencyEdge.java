package plagiarism_graph_comparison;

import org.jgrapht.graph.DefaultEdge;
// a custom graph edge object that stores whether it's a control or data dependence
public class DependencyEdge extends DefaultEdge {
    String label;


    public DependencyEdge(String label)
    {
        this.label = label;
    }

    /**
     * Gets the label associated with this edge.
     *
     * @return edge label
     */
    public String getLabel()
    {
        return label;
    }

    @Override
    public String toString()
    {
        return label;
    }
}
