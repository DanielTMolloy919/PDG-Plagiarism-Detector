package plagiarism_graph_comparison;

import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DDG {
    Graph<Integer, DefaultEdge> cfg;
    MethodDeclaration method_node;
    Graph<BasicBlock, DefaultEdge> node_graph;
    int counter;

    List<Statement> statements; // a list of all the statements found in the method
    LinkedHashMap<String, BasicBlock> Statement_to_BasicBlock;
    
    
    public DDG(MethodDeclaration method_node, int counter) {
        // this.cfg = cfg.node_graph;
        this.counter = counter;
        this.method_node = method_node;

        statements = method_node.findAll(Statement.class); // load all the statements

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph
        Statement_to_BasicBlock = new LinkedHashMap<String, BasicBlock>();

        for (int i = 0; i < statements.size(); i++) {
            BasicBlock bb = new BasicBlock(statements.get(i), i);
            node_graph.addVertex(bb); // load all the statement nodes
            Statement_to_BasicBlock.put(Integer.toString(i), bb);
        }

        for (int i = 0; i < statements.size(); i++) {
            System.out.println(Statement_to_BasicBlock.get(Integer.toString(i)).toString());
        }

        System.out.println("yay");
    }

    
}
