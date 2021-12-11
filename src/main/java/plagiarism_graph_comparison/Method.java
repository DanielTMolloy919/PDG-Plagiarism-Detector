package plagiarism_graph_comparison;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class Method {

    Graph<Integer, DefaultEdge> cfg;
    static int counter;

    public Method(MethodDeclaration method_node) throws IOException {

        cfg = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        List<Statement> statements = method_node.findAll(Statement.class);

        for (int i = 0; i < statements.size(); i++) {
            cfg.addVertex(i);
        }

        for (int i = 1; i < statements.size(); i++) {
            // if (statements.get(i).isIfStmt() ) {
                
            // }

            cfg.addEdge(i-1, i);
        }

        export_graph();        
    }

    private void id_to_statement() {

    }

    private void statement_to_id() {

    }

    private void export_graph() throws IOException {

        DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>();

        export.export(new FileWriter("output.dot"), cfg);
    }
}
