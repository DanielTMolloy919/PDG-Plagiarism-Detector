package plagiarism_graph_comparison;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Test {
    // public static void main(String[] args) {
        // DefaultDirectedGraph<Integer,DefaultEdge> graph1 = new DefaultDirectedGraph<Integer,DefaultEdge>(DefaultEdge.class);

        // graph1.addVertex(1);
        // graph1.addVertex(2);
        // graph1.addVertex(3);
        // graph1.addVertex(4);
        // graph1.addVertex(5);

        // for (int i = 1; i < 5; i++) {
        //     graph1.addEdge(i, i+1);
        // }

        // Set<Integer> subgraph_set = new HashSet<>();
        // for (int i = 1; i < 5; i++) {
        //     subgraph_set.add(i);
        // }

        // AsSubgraph<Integer,DefaultEdge> subgraph1 = new AsSubgraph<>(graph1,subgraph_set);

        // VF2SubgraphIsomorphismInspector<Integer,DefaultEdge> iso_inspector = new VF2SubgraphIsomorphismInspector<>(graph1,subgraph1);

        // iso_inspector.isomorphismExists();
    // }
}
