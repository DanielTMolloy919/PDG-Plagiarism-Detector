package plagiarism_graph_comparison;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

public class Method {

    Graph<Integer, DefaultEdge> cfg;
    static int counter;
    List<Statement> statements; // a list of all the statements in the method - this doesn't change
    List<Integer> statement_blacklist;

    private Statement original_statement;
    private int test_index;

    public Method(MethodDeclaration method_node) throws IOException {

        cfg = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        statements = method_node.findAll(Statement.class); // load all the statements

        statement_blacklist = new ArrayList<Integer>();

        for (int i = 0; i < statements.size(); i++) {
            cfg.addVertex(i); // load all the statement nodes
            // statement_ids.add(i); // load all the statement ids into the list
        }        

        for (int i = 0; i < statements.size() - 1; i++) { // loop through each statement
            
            if (statement_blacklist.contains(i)) { // if the statement is on the blacklist, don't go any further
                continue;
            }

            Statement statement = statements.get(i);

            int current_index = i;

            System.out.println("[" + i + "] " + statement);

            if (statement.isIfStmt()) {
                IfStmt ifstmt = statement.asIfStmt(); // get the specific if statement object

                int then_index = statement_to_id(ifstmt.getThenStmt());
                int subsequent_index = get_subsequent_sibling(current_index);
                int last_child_index = get_last_child(then_index);

                cfg.addEdge(current_index, then_index);
                cfg.addEdge(last_child_index, subsequent_index); // link the end of if, to the statement after if - skipping else
                statement_blacklist.add(last_child_index); // ensure then statement isn't double counted

                if (ifstmt.hasElseBlock()) { // if there is an else statement, do the same
                    int else_index = statement_to_id(ifstmt.getElseStmt().get());

                    cfg.addEdge(current_index, else_index);
                }

                else {
                    cfg.addEdge(current_index, subsequent_index);
                }
            }
            
            else {
                cfg.addEdge(current_index, current_index+1);
            }

            export_graph(); 
        }      
    }

    private Statement id_to_statement(int i) {
        return statements.get(i);
    }

    private int statement_to_id(Statement statement) {

        return IntStream.range(0, statements.size())
        .filter(index -> statements.get(index).equals(statement))
        .findFirst()
        .getAsInt();
    }

    private int get_last_child(int id) {
        List<Statement> children = statements.get(id).findAll(Statement.class);

        return id + children.size() - 1;
    }

    private int get_subsequent_sibling(int id) {
        original_statement = statements.get(id);
        List<Statement> children = statements.get(id).findAll(Statement.class);

        test_index = id + 1;
        

        while (true) {
            System.out.println(test_index);
            // for each node in the sequence after id, if it can't be found in the children list, it must be the next sibling. Therefore return it
            if (!children.stream()
            .filter(child -> child.equals(statements.get(test_index)))
            .findFirst()
            .isPresent()) {
                return test_index;
            }
            else {
                test_index++;
            }
        }
    }

    private void export_graph() throws IOException {

        DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>();

       export.exportGraph(cfg, new FileWriter("output.dot"));
    }
}
