package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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

    CFG cfg;
    Graph<BasicBlock, DependencyEdge> node_graph; // The method's data dependence graph

    LinkedHashMap<BasicBlock, BasicBlock> bb_ipdom;

    public PDG(DDG ddg, int counter) throws IOException {
        this.statements = ddg.statements;
        this.basic_blocks = ddg.basic_blocks;
        this.Statement_id_to_BasicBlock = ddg.Statement_id_to_BasicBlock;
        this.counter = counter;
        
        this.cfg = ddg.cfg;
        this.node_graph = ddg.node_graph;

        bb_ipdom = new LinkedHashMap<>(); // contains the immediate post dominator for each statement 
        
        for (BasicBlock bb : basic_blocks) {
            bb_ipdom.put(bb, get_ipdom(bb));
        }

        for (int i = 0; i < basic_blocks.size(); i++) {
            for (int j = i + 1; j <= statements.size(); j++) {
                if (!is_connection_eliminated(basic_blocks.get(i), basic_blocks.get(j))) {
                    node_graph.addEdge(basic_blocks.get(i), basic_blocks.get(j), new DependencyEdge("CD"));
                    Export.exporter(this, counter);
                }
            }
        }
    }

    private boolean is_connection_eliminated(BasicBlock start_bb, BasicBlock end_bb) {

        AllDirectedPaths<BasicBlock, DefaultEdge> all_directed_paths = new AllDirectedPaths<>(cfg.node_graph);

        List<GraphPath<BasicBlock, DefaultEdge>> potential_paths = all_directed_paths.getAllPaths(start_bb, end_bb, true, 100);

        int true_connection_count = 0;

        if (potential_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential CDG paths found between Node " + start_bb.get_id() + " and Node " + end_bb.get_id());
        }
        
        for (GraphPath<BasicBlock,DefaultEdge> graphPath : potential_paths) {
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex == bb_ipdom.get(start_bb))) {
                true_connection_count++;
            }
        }

        if (true_connection_count > 0) {
            return false;
        }

        return true;
    }

    private BasicBlock get_ipdom(BasicBlock bb) {

        int id = bb.get_id();
        List<BasicBlock> candidates = new ArrayList<>();

        // for each 
        for (int i = id + 1; i <= statements.size(); i++) {
            if (is_pdom(bb, basic_blocks.get(i))) {
                candidates.add(bb);
            }
        }
        return bb;


    }

    private boolean is_pdom(BasicBlock dominated_bb, BasicBlock dominator_bb) {

        AllDirectedPaths<BasicBlock, DefaultEdge> all_directed_paths = new AllDirectedPaths<>(cfg.node_graph);

        List<GraphPath<BasicBlock, DefaultEdge>> pdom_paths = all_directed_paths.getAllPaths(dominated_bb, Statement_id_to_BasicBlock.get("END"), true, 100);

        if (pdom_paths.size() > 100) { // Warns the user of unusual behaviour
            System.out.println("Warning: greater than 100 potential postdominance paths found for Method " + counter + " Statement " + dominated_bb.get_id());
        }

        for (GraphPath<BasicBlock, DefaultEdge> graphPath : pdom_paths) {// loop through each path
            if (!graphPath.getVertexList().stream().anyMatch(vertex -> vertex.equals(dominator_bb))) { // unless dominator_id shows up in every single postdominance path, return false. If there's a single path that doesn't contain dominator_id, dominator_id cannot postdominate dominated_id
                return false;
            }
        }

        return true;
    }
}
