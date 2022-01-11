package plagiarism_graph_comparison;

import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;

public class PDG {
    List<Statement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    int counter;

    CFG cfg;

    Graph<BasicBlock, DependencyEdge> node_graph; // The method's data dependence graph
}
