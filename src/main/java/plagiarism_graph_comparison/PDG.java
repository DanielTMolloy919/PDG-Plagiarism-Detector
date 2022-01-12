package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;

public class PDG {
    List<Statement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    int counter;

    static int count = 0; // for debugging

    CFG cfg;
    Graph<BasicBlock, DependencyEdge> node_graph; // The method's data dependence graph

    LinkedHashMap<BasicBlock, BasicBlock> bb_ipdom;

    AllDirectedPaths<BasicBlock, DefaultEdge> all_directed_paths;
    List<GraphPath<BasicBlock, DefaultEdge>> pdom_paths;

    public PDG(DDG ddg, int counter) throws IOException {
        this.statements = ddg.statements;
        this.basic_blocks = ddg.basic_blocks;
        this.Statement_id_to_BasicBlock = ddg.Statement_id_to_BasicBlock;
        this.counter = counter;
        
        this.cfg = ddg.cfg;
        this.node_graph = ddg.node_graph;

        bb_ipdom = new LinkedHashMap<>(); // contains the immediate post dominator for each statement 
        
        // get the immediate post dominator for every statement but the last
        for (int i = 0; i < basic_blocks.size() - 1; i++) {
            BasicBlock bb = basic_blocks.get(i);
            bb_ipdom.put(bb, get_ipdom(bb));
        }

        // manually make the last statement's postdominator the 'END' node
        bb_ipdom.put(basic_blocks.get(basic_blocks.size() -1), Statement_id_to_BasicBlock.get("END"));

        // for each statement 'i'
        for (int i = 0; i < basic_blocks.size(); i++) {
            // for each node after statement 'i'
            for (int j = i + 1; j < basic_blocks.size(); j++) {

                // if a connection between 'i' and 'j' qualifies as a control dependency, link it up
                if (!is_connection_eliminated(basic_blocks.get(i), basic_blocks.get(j))) {
                    node_graph.addEdge(basic_blocks.get(i), basic_blocks.get(j), new DependencyEdge("CD"));
                    Export.exporter(this, counter);
                }
            }
        }
    }

    private boolean is_connection_eliminated(BasicBlock start_bb, BasicBlock end_bb) {

        // get all the paths between the two nodes
        List<GraphPath<BasicBlock, DefaultEdge>> potential_paths = all_directed_paths.getAllPaths(start_bb, end_bb, true, 100);

        int true_connection_count = 0;

        if (potential_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential CDG paths found between Node " + start_bb.get_id() + " and Node " + end_bb.get_id());
        }
        
        // for each path
        for (GraphPath<BasicBlock,DefaultEdge> graphPath : potential_paths) {

            // for the vertexes on this path, if one of the vertexes is start_bb's immediate postdominator, the path is eliminated
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex == bb_ipdom.get(start_bb))) {
                true_connection_count++;
            }
        }

        // if there exists at least one path that doesn't contain the ipdom, this is a control dependence. Therefore *don't* eliminate the connection
        if (true_connection_count > 0) {
            return false;
        }

        // otherwise if all paths contain the ipdom, eliminate the connection
        return true;
    }

    // Finds and returns a given node's immediate post dominator
    private BasicBlock get_ipdom(BasicBlock bb) {

        // count++;
        // System.out.println(count);

        int id = bb.get_id();
        List<BasicBlock> candidates = new ArrayList<>();

        // get all the cfg paths from bb to the end
        all_directed_paths = new AllDirectedPaths<>(cfg.node_graph);
        pdom_paths = all_directed_paths.getAllPaths(bb, Statement_id_to_BasicBlock.get("END"), true, 100);

        List<BasicBlock> cfg_bbs = new ArrayList<>();
        cfg_bbs.addAll(basic_blocks);
        cfg_bbs.add(Statement_id_to_BasicBlock.get("END"));

        // for each node after bb, check if it could be the immediate post dominator 
        for (int i = id + 1; i < cfg_bbs.size(); i++) {
            if (is_pdom(bb, cfg_bbs.get(i), pdom_paths)) {
                candidates.add(cfg_bbs.get(i));
            }
        }
        if (candidates.size() == 1) { // if there's only one candidate, it must be the immediate postdominator
            return candidates.get(0);
        };

        List<BasicBlock> remaining_candidates = new ArrayList<>();

        remaining_candidates.addAll(candidates);

        for (BasicBlock candidate : candidates) {// for each potential immediate post dominator
            List<BasicBlock> other_candidates = new ArrayList<>();
            other_candidates.addAll(candidates);
            other_candidates.remove(candidate);

            pdom_paths = all_directed_paths.getAllPaths(candidate, Statement_id_to_BasicBlock.get("END"), true, 100);
            
            // for all the other potential immediate postdominators besides 'candidate'
            for (BasicBlock other_candidate : other_candidates) {
                // if this 'other candidate' postdominates 'candidate', then 'other candidate' can't be the immediate postdominator of bb
                if (is_pdom(candidate, other_candidate, pdom_paths)) {
                    remaining_candidates.remove(other_candidate);
                }
            }
            // if (other_candidates.stream().anyMatch(other_candidate -> is_pdom(candidate, other_candidate,pdom_paths))) {
            //     remaining_candidates.remove(candidate);
            // }
        }

        // There should only be one immediate post dominator - if this isn't the case something has gone wrong
        if (remaining_candidates.size() != 1)  {
            String throw_string = "Candidate " + id + " has no single postdominator";
            System.out.println(throw_string);
        }

        return remaining_candidates.get(0);
    }

    private boolean is_pdom(BasicBlock dominated_bb, BasicBlock dominator_bb, List<GraphPath<BasicBlock, DefaultEdge>> pdom_paths) {

        if (pdom_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential postdominance paths found for Method " + counter + " Statement " + dominated_bb.get_id());
        }

        for (GraphPath<BasicBlock, DefaultEdge> graphPath : pdom_paths) {// loop through each path
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex.equals(dominator_bb))) { // unless dominator_bb shows up in every single postdominance path, return false. If there's a single path that doesn't contain dominator_bb, dominator_bb cannot postdominate dominated_bb
                return false;
            }
        }

        return true;
    }
}
