package plagiarism_graph_comparison;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.javaparser.ast.body.MethodDeclaration;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jgrapht.Graph;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.AsSubgraph;


public class SubmissionCompare {
    Comparator<BasicBlock> vertex_comparator;
    Comparator<DependencyEdge> edge_comparator;

    Submission sb1;
    Submission sb2;

    ArrayList<Method> sb1_methods;
    ArrayList<Method> sb2_methods;

    double score;

    ArrayList<ArrayList<Method>> plagiarized_pairs;

    public SubmissionCompare(Submission sb1, Submission sb2) throws IOException {
        this.sb1 = sb1;
        this.sb2 = sb2;

        sb1_methods = sb1.method_objects;
        sb2_methods = sb2.method_objects;

        plagiarized_pairs = new ArrayList<>();

        for (Method method_1 : sb1_methods) {
            for (Method method_2 : sb2_methods) {
                compare(method_1, method_2);
            }
        }

        this.score = score();
        
    }

    private double score() {
        int total_nodes = 0;

        for (MethodDeclaration md : sb1.mds) {
            total_nodes += sb1.method_node_count.get(md);
        }

        for (MethodDeclaration md : sb2.mds) {
            total_nodes += sb2.method_node_count.get(md);
        }

        int plagiarized_nodes = 0;

        ArrayList<Method> plagiarized_list = new ArrayList<>();

        for (ArrayList<Method> pair : plagiarized_pairs) {
            if (!plagiarized_list.contains(pair.get(0))) {
                plagiarized_list.add(pair.get(0));
                plagiarized_nodes += pair.get(0).node_count;
            }
            if (!plagiarized_list.contains(pair.get(1))) {
                plagiarized_list.add(pair.get(1));
                plagiarized_nodes += pair.get(1).node_count;
            }
        }

        final Object[][] table = new String[plagiarized_pairs.size() + 2][];
        table[0] = new String[] { "Submission " + sb1.submission_name, "", "Submission " + sb2.submission_name};
        table[1] = new String[] { "---------", "---------", "---------"};

        for (int i = 0; i < plagiarized_pairs.size(); i++) {
            Method m1 = plagiarized_pairs.get(i).get(0);
            Method m2 =plagiarized_pairs.get(i).get(1);
            table[i+2] = new String[] { m1.method_name + " (" + m1.node_count + ")", "<------->", m2.method_name+ " (" + m2.node_count + ")"};
        }

        for (final Object[] row : table) {
            System.out.format("%-15s%-15s%-15s%n", row);
        }
        System.out.println("\n\n");

        return (double) plagiarized_nodes/total_nodes;
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
            // System.out.println("Potential Plagiarism between submissions " + m1.toString() + " and " + m2.toString());
            ArrayList<Method> pair = new ArrayList<>();
            pair.add(m1);
            pair.add(m2);
            plagiarized_pairs.add(pair);
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