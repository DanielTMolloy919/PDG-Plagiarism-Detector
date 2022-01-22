package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;

public class GraphCompare {
    Comparator<BasicBlock> vertex_comparator;
    Comparator<DependencyEdge> edge_comparator;

    public GraphCompare(PDG pdg1, PDG pdg2, int counter) throws IOException {

        Set<BasicBlock> bb_set = pdg1.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        bb_set = pdg2.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        if (is_gamma_isomorphic(pdg1, pdg2)) {
            System.out.println("Potential Plagiarism between submissions " + pdg1.counter + " and " + pdg2.counter);
        }
    }

    private boolean is_gamma_isomorphic(PDG pdg1, PDG pdg2) {
        double gamma = 0.9;

        Set<BasicBlock> pdg2_set = pdg2.node_graph.vertexSet();
        List<BasicBlock> pdg2_list = new ArrayList<>(pdg2_set);

        Iterator<int[]> combinations = CombinatoricsUtils.combinationsIterator(pdg2_set.size(),
                (int) Math.round(gamma * pdg2_set.size()));

        int iterator_count = 0;
        
        // vertex_comparator = new VertexComparator();
        // edge_comparator = new EdgeComparator();

        vertex_comparator = (BasicBlock left, BasicBlock right) -> comparator_proxy(right, left);
        edge_comparator = (DependencyEdge left, DependencyEdge right) -> comparator_proxy(right, left);

        while (combinations.hasNext()) {
            final int[] combination = combinations.next();
            Set<BasicBlock> subgraph_vertexes = new HashSet<BasicBlock>();

            for (int i : combination) {
                subgraph_vertexes.add(pdg2_list.get(i));
            }

            AsSubgraph<BasicBlock, DependencyEdge> subgraph = new AsSubgraph<>(pdg2.node_graph, subgraph_vertexes);
            // Export.exporter(subgraph, counter, iterator_count);
            System.out.println(iterator_count);

            if (is_isomorphic(pdg1.node_graph, subgraph)) {
                System.out.println("Isomorphism Detected");
                return true;
            }

            iterator_count++;
        }

        return false;
    }

    private boolean is_isomorphic(Graph<BasicBlock, DependencyEdge> node_graph, AsSubgraph<BasicBlock, DependencyEdge> pdg2) {

        VF2SubgraphIsomorphismInspector<BasicBlock,DependencyEdge> iso_inspector = new VF2SubgraphIsomorphismInspector<>(pdg2,pdg2,vertex_comparator,edge_comparator);
        // VF2SubgraphIsomorphismInspector<BasicBlock,DependencyEdge> iso_inspector = new VF2SubgraphIsomorphismInspector<>(pdg2,pdg2);

        return iso_inspector.isomorphismExists();
    }

    private int comparator_proxy(BasicBlock left, BasicBlock right) {
        return left.type.equals(right.type) ? 0 : -1;
    }

    private int comparator_proxy(DependencyEdge left, DependencyEdge right) {
        return left.label.equals(right.label) ? 0 : -1;
    }
}

// class VertexComparator implements Comparator<BasicBlock> {
    
//     @Override
//     public int compare(BasicBlock bb1, BasicBlock bb2) {
//         if (bb1.type.equals(bb2.type)) {
//             return 1;
//         }
//         else {
//             return -1;
//         }
//     }
// }

// class EdgeComparator implements Comparator<DependencyEdge> {
    
//     @Override
//     public int compare(DependencyEdge de1, DependencyEdge de2) {
//         if (de1.label.equals(de1.label)) {
//             return 1;
//         }
//         else {
//             return -1;
//         }
//     }
// }