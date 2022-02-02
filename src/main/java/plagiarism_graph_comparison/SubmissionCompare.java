package plagiarism_graph_comparison;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javaparser.ast.body.MethodDeclaration;

import org.apache.commons.math3.geometry.spherical.twod.Vertex;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.CombinatoricsUtils;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultDirectedGraph;

// This class holds the plagiarism results from a given submission pair
public class SubmissionCompare {
    Comparator<BasicBlock> vertex_comparator;
    Comparator<DependencyEdge> edge_comparator;

    static Mean m = new Mean();
    static double gamma;

    Submission sb1;
    Submission sb2;

    Method first_method;
    Method second_method;

    PDG first_pdg;
    PDG second_pdg;

    ArrayList<Method> sb1_methods;
    ArrayList<Method> sb2_methods;

    static List<Long> average = new ArrayList<>();

    double score;

    static int counter = 0;

    ArrayList<ArrayList<Method>> plagiarized_pairs;

    public SubmissionCompare(Submission sb1, Submission sb2) throws IOException {
        this.sb1 = sb1;
        this.sb2 = sb2;

        sb1_methods = sb1.method_objects;
        sb2_methods = sb2.method_objects;

        plagiarized_pairs = new ArrayList<>();

        // compare every combination of the two method sets
        for (Method method_1 : sb1_methods) {
            for (Method method_2 : sb2_methods) {
                compare(method_1, method_2);
            }
        }

        this.score = score();
        
    }

    // returns a score for fraction of the submission that is plagiarised
    // It counts up the number of nodes in plagiarised methods and returns it as a fraction of the total number of nodes across the submission
    private double score() {
        int total_nodes = 0;

        for (MethodDeclaration md : sb1.significant_mds) {
            total_nodes += sb1.method_node_count.get(md);
        }

        for (MethodDeclaration md : sb2.significant_mds) {
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
        // List out each detected plagiarsed method pair
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

        counter++;

        score = (double) plagiarized_nodes / total_nodes;

        return score;

        
        // return (double) plagiarized_nodes/total_nodes;
    }

    // Test two methods for plagarism
    private void compare(Method first_method, Method second_method) throws IOException {
        this.first_method = first_method;
        this.second_method = second_method;

        PDG first_pdg= first_method.pdg;
        PDG second_pdg = second_method.pdg;

        // generates node type for all nodes in both methods
        // 'type' determines whether two nodes are identical or not
        Set<BasicBlock> bb_set = first_pdg.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        bb_set = second_pdg.node_graph.vertexSet();
        for (BasicBlock bb : bb_set) {
            bb.generate_type();
        }

        // if the method PDGs are gamma isomorphic, add them to the list of plagiarised method pairs
        if (is_gamma_isomorphic(first_pdg, second_pdg)) {
            ArrayList<Method> pair = new ArrayList<>();
            pair.add(first_method);
            pair.add(second_method);
            plagiarized_pairs.add(pair);
        }
    }

    // Tests whether two PDGs have a gamma isomorphism
    // Based on the paper 'GPLAG: detection of software plagiarism by program dependence graph analysis' by Liu et al. 2006
    private boolean is_gamma_isomorphic(PDG first_pdg, PDG second_pdg) throws IOException {

        this.first_pdg = first_pdg;
        this.second_pdg = second_pdg;

        // step one - does a theoretical 'S' have more than gamma * G' nodes
        // the largest subgraph 'S' would be the whole graph G, therefore thats our maximum scenario

        Set<BasicBlock> first_pdg_set = first_pdg.node_graph.vertexSet();
        Set<BasicBlock> second_pdg_set = second_pdg.node_graph.vertexSet();

        if (first_pdg_set.size() < Math.round(gamma * second_pdg_set.size())) {
            return false;
        }
        
        // step two - does an isomorphism exist between an 'S' and an equivalent sized 'T'
        
        // part one - get all combinations of G with nodes larger than gamma * G'

        Iterator<int[]> combinations = CombinatoricsUtils.combinationsIterator(first_pdg_set.size(),
                (int) Math.round(gamma * first_pdg_set.size()));
        
        // part two - for each 'S', is it subgraph isomorphic to G'?

        // telling the program to find identical nodes based on class attribute 'type', and identical edges based on whether its a dependency or control edge
        vertex_comparator = (BasicBlock left, BasicBlock right) -> comparator_proxy(right, left);
        edge_comparator = (DependencyEdge left, DependencyEdge right) -> comparator_proxy(right, left);

        int iterator_count = 0;

        List<BasicBlock> first_pdg_list = new ArrayList<>(first_pdg_set);

        int combinations_count  = 0;

        // if no isomorphisms exist between 100000 test subgraphs and G', then it's likely the pair isn't plagiarised

        while (combinations.hasNext() && combinations_count < 10000) {
            final int[] combination = combinations.next();
            Set<BasicBlock> subgraph_vertexes = new HashSet<BasicBlock>();

            for (int i : combination) {
                subgraph_vertexes.add(first_pdg_list.get(i));
            }

            AsSubgraph<BasicBlock, DependencyEdge> subgraph = new AsSubgraph<>(first_pdg.node_graph, subgraph_vertexes);

            first_pdg.Statement_id_to_BasicBlock.get("1");

            if (is_subgraph_isomorphic(subgraph, second_pdg.node_graph)) {
                    // System.out.println("Isomorphism Detected");
                    
                    return true;
                }

            combinations_count++;

        }

        return false;
    }

    // Tests whether two graphs have a subgraph isomorphism
    private boolean is_subgraph_isomorphic(AsSubgraph<BasicBlock, DependencyEdge> subgraph_S, DefaultDirectedGraph<BasicBlock, DependencyEdge> Graph_G_prime) throws IOException {

        // Uses JGraphT's built in subgraph isomorphism tester
        VF2SubgraphIsomorphismInspector<BasicBlock,DependencyEdge> iso_inspector = new VF2SubgraphIsomorphismInspector<>(Graph_G_prime,subgraph_S,vertex_comparator,edge_comparator);

        Export.exportSubPDG(subgraph_S,this,counter);

        System.out.println(counter);
        
        counter++;
        // if an isomorphism exists, return true;
        return iso_inspector.isomorphismExists();
    }

    // Comparators that define whether two nodes or edges are identical
    // For nodes, this is based on 'type', for edges this is based on 'label'
    private int comparator_proxy(BasicBlock left, BasicBlock right) {
        return left.type.equals(right.type) ? 0 : -1;
    }

    private int comparator_proxy(DependencyEdge left, DependencyEdge right) {
        return left.label.equals(right.label) ? 0 : -1;
    }
}