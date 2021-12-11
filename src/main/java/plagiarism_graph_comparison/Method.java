package plagiarism_graph_comparison;

import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Method {

    Graph<statement_vertex, DefaultEdge> cfg;
    static int counter;

    public Method(MethodDeclaration method_node) {
        counter = 0;

        cfg = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        List<Statement> found_statements = method_node.findAll(Statement.class);
        
        found_statements.forEach(node -> node_constructor(node)); // create all the jgrapht nodes

        found_statements.forEach(statement -> { // add all the edges
            if (statement.isIfStmt()) {
                System.out.println("pogg");
            } 
            
            else {
                cfg.addEdge(arg0, arg1)
            }
        });
    }

    private void node_constructor(Statement statement) {
        cfg.addVertex(new statement_vertex(counter, statement));

        counter++;
    }
    
    // a simple class to group the statement node and a unique identifying number
    class statement_vertex {
        int key; // this is the unique block number
        Statement statement; // this is the actual statement node

        statement_vertex (int key, Statement statement) {
            this.key = key;
            this.statement = statement;
        }
    }
}
