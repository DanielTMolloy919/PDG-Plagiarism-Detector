package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.AsSubgraph;

public class SubmissionCompare {
    Comparator<BasicBlock> vertex_comparator;
    Comparator<DependencyEdge> edge_comparator;

    ArrayList<Method> sb1_methods;
    ArrayList<Method> sb2_methods;

    public SubmissionCompare(Submission sb1, Submission sb2) throws IOException {
        sb1_methods = sb1.method_objects;
        sb2_methods = sb2.method_objects;

        for (Method method_1 : sb1_methods) {
            for (Method method_2 : sb2_methods) {
                compare(method_1, method_2);
            }
        }
        // Iterator<int[]> combinations = CombinatoricsUtils.combinationsIterator(sb1_methods.size(),sb2_methods.size());
        // ArrayList<ArrayList<Method>> plagiarism_pairs = new ArrayList<ArrayList<Method>>();

        // while (combinations.hasNext()) {
        //     final int[] combination = combinations.next();

        //     compare(sb1_methods.get(combination[0]),sb2_methods.get(combination[1]));

        //     itertools
        // }
    }

    private void compare(Method m1, Method m2) throws IOException {
        PDG pdg1= m1.pdg;
        PDG pdg2 = m2.pdg;

        Set<BasicBlock> bb_set = pdg1.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        bb_set = pdg2.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        if (is_gamma_isomorphic(pdg1, pdg2)) {
            System.out.println("Potential Plagiarism between submissions " + m1.toString() + " and " + m2.toString());
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
            // System.out.println(iterator_count);

            if (is_isomorphic(pdg1.node_graph, subgraph)) {
                // System.out.println("Isomorphism Detected");
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

// class TwoListCombine<T> implements Iterator<List<T>> {

//     final List<T> list1;
//     final List<T> list2;
//     int list1_position;
//     int list2_position;

//     public TwoListCombine(List<T> list1, List<T> list2) {
//         this.list1 = list1;
//         this.list2 = list2;
//         list1_position = 0;
//         list2_position = 0;
//     }

//     @Override
//     public boolean hasNext() {
//         // has first index not yet reached max position?
//         return list1_position < list1.size() || list2_position < list2.size();
//     }

//     @Override
//     public List<T> next() {
//         List<T> result = new ArrayList<>(2);
               
//     }
// }