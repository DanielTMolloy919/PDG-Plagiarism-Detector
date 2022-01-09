package plagiarism_graph_comparison;

import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class StatementGraph {

    Graph<BasicBlock, DefaultEdge> node_graph;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    List<Statement> statements;

    public StatementGraph(MethodDeclaration method_node) {

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        
        statements = method_node.findAll(Statement.class); // load all the statements

        BasicBlock bb;

        for (int i = 0; i < statements.size(); i++) {
            bb = new BasicBlock(statements.get(i), i);
            node_graph.addVertex(bb); // load all the statement nodes
            Statement_id_to_BasicBlock.put(Integer.toString(i), bb);
        }
    }
}
