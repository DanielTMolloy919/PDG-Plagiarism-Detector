package plagiarism_graph_comparison;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DDG {
    Graph<Integer, DefaultEdge> cfg;
    MethodDeclaration method_node;
    Graph<Integer, DefaultEdge> node_graph;
    int counter;

    List<Statement> statements; // a list of all the statements found in the method
    
    
    public DDG(MethodDeclaration method_node, int counter) {
        // this.cfg = cfg.node_graph;
        this.counter = counter;
        this.method_node = method_node;

        statements = method_node.findAll(Statement.class); // load all the statements

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        for (int i = 0; i < statements.size(); i++) {
            node_graph.addVertex(i); // load all the statement nodes
        }

        for (Statement statement : statements) {
            if (statement.isExpressionStmt()) {
                System.out.println(statement);
            }
        }

        System.out.println("yay");
    }

    
}
