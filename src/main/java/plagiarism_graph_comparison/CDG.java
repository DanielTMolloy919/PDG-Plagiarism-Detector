package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class CDG {

    Graph<Integer, DefaultEdge> cfg;
    Graph<Integer, DefaultEdge> node_graph;
    List<Statement> statements;
    List<Integer> ipdominator_dict;
    int counter;

    public CDG(CFG cfg_object,int counter) throws NoSinglePostdominatorException, IOException {
        this.cfg = cfg_object.node_graph;
        this.statements = cfg_object.statements;
        this.counter = counter;

        ipdominator_dict = new ArrayList<>(); // contains the immediate post dominator for each statement id

        for (int i = 0; i < statements.size(); i++) { // for each statement i
            ipdominator_dict.add(get_immediate_pdominator(i));
        }

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (int i = 0; i < statements.size(); i++) {
            node_graph.addVertex(i); // load all the statement nodes
        }

        for (int i = 0; i < statements.size(); i++) {
            for (int j = i + 1; j <= statements.size(); j++) {
                if (!is_connection_eliminated(i, j)) {
                    node_graph.addEdge(i, j);
                    Export.exporter(this, counter);
                }
            }
        }


    }

    private boolean is_connection_eliminated(int start_node, int end_node) {
        
        AllDirectedPaths<Integer, DefaultEdge> all_directed_paths = new AllDirectedPaths<>(cfg);
        
        List<GraphPath<Integer, DefaultEdge>> potential_paths = all_directed_paths.getAllPaths(start_node, end_node, true, 100);

        int true_connection_count = 0;

        if (potential_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential CDG paths found between Node " + start_node + " and Node " + end_node);
        }

        for (GraphPath<Integer,DefaultEdge> graphPath : potential_paths) {
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex == ipdominator_dict.get(start_node))) {
                true_connection_count++;
            }
        }

        if (true_connection_count > 0) {
            return false;
        }

        return true;
    }

    private int get_immediate_pdominator(int id) throws NoSinglePostdominatorException {
        List<Integer> candidates = new ArrayList<>(); 

        for (int j = id + 1; j <= statements.size(); j++) { // any node that postdominates id goes on the candidates list
            if (is_post_dominator(id, j)) {
                candidates.add(j);
            }
        }

        if (candidates.size() == 1) { // if there's only one candidate, it must be the immediate postdominator
            return candidates.get(0);
        }

        List<Integer> remaining_candidates = candidates; 

        for (Integer candidate : candidates) { // for each potential immediate post dominator 'candidate'
            List<Integer> other_candidates = candidates;
            other_candidates.remove(candidate);

            // if 'candidate' is postdominated by any 'other-candidate', it cant be the immediate postdominator, therefore remove it from the 'remaining candidates'
            if (other_candidates.stream().anyMatch(other_candidate -> is_post_dominator(candidate, other_candidate))) {
                remaining_candidates.remove(candidate);
            }
        }

        if (remaining_candidates.size() != 1)  {
            String throw_string = "Candidate " + id + " has no single postdominator";
            throw new NoSinglePostdominatorException(throw_string);
        }

        return remaining_candidates.get(0);
    }

    private boolean is_post_dominator(int dominated_id, int dominator_id) {

        AllDirectedPaths<Integer, DefaultEdge> all_directed_paths = new AllDirectedPaths<>(cfg);
        
        List<GraphPath<Integer, DefaultEdge>> postdominance_paths = all_directed_paths.getAllPaths(dominated_id, 999, true, 100);

        if (postdominance_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential postdominance paths found for Method " + counter + " Statement " + dominated_id);
        }

        for (GraphPath<Integer,DefaultEdge> graphPath : postdominance_paths) { // loop through each path
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex == dominator_id)) { // unless dominator_id shows up in every single postdominance path, return false. If there's a single path that doesn't contain dominator_id, dominator_id cannot postdominate dominated_id
                return false;
            }
        }

        return true;
    }

    public class NoSinglePostdominatorException extends Exception {
        public NoSinglePostdominatorException(String str) {
            super(str);
        }
    }
    
}
