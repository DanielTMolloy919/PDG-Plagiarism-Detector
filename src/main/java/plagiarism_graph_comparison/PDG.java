package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;
import com.google.common.util.concurrent.Service.State;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;

public class PDG {
    MethodDeclaration method_node;
    int counter;

    List<Statement> statements; // a list of all the statements found in the method
    
    List<Integer> statement_blacklist; // a list of statements to skip iteration - they have already been taken into
                                        // account

    Graph<Integer, DefaultEdge> node_graph; // The method's control flow diagram

    int end_id; // This is the ID of the last statement in the method

    int current_id; // When looping through each statement, this is the ID of the current statement

    private Statement original_statement;
    private int test_id;


    public PDG(MethodDeclaration method_node, int counter) throws IOException, StatementNotFoundException {

        this.method_node = method_node;
        this.counter = counter;

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        statements = method_node.findAll(Statement.class); // load all the statements

        Export.exporter(method_node, counter);

        Export.exporter(statements, counter);

        statement_blacklist = new ArrayList<Integer>();

        end_id = 999;

        if (statements.size() == 0) {
            node_graph.addVertex(0);
            node_graph.addVertex(end_id);
            node_graph.addEdge(0, end_id);
            Export.exporter(this,counter);
            return;
        }

        else if (statements.size() == 1) {
            node_graph.addVertex(0);
            node_graph.addVertex(1);
            node_graph.addVertex(end_id);
            node_graph.addEdge(0, 1);
            node_graph.addEdge(1, end_id);
            Export.exporter(this,counter);
            return;
        }

        for (int i = 0; i < statements.size(); i++) {
            node_graph.addVertex(i); // load all the statement nodes
        }

        int last_branching_id = 0;

        List<Integer> non_special_children = new ArrayList<>();

        statements.get(0).findAll(Statement.class).forEach(node -> System.out.println("* " + node));

        for (int i = 0; i < statements.size(); i++) {
            non_special_children.add(i);
        }
        
        // for (int i = 1; i < statements.size(); i++) {
        //     if (is_statement_special(statements.get(i))) {
        //         int last_child_id = get_last_child(i);
        //         for (int j = i; j < last_branching_id; j++) {
        //             if (non_special_children.contains(j)) {
        //                 non_special_children.remove(j);
        //             }
        //         }
        //     }

        //     else {
        //         non_special_children.add(i)
        //     }
        // }

        // children.stream().skip(1).forEach(statement -> {
        //     node_graph.addEdge(last_branching_id, statement_to_id(statement));
        //     if (is_statement_special(statement)) { 

        //     }
        // });
        
        return;
    }

    // private List<Statement> get_first_level_children(int id) {
    //     List<Statement> all_children = statements.get(id).findAll(Statement.class);

    //     for (Statement statement : all_children) {
    //         if (statement.getParentNode())
    //     }

    //     return all_children;
    // }

    private int statement_to_id(Statement statement) {
        OptionalInt statement_position = IntStream.range(0, statements.size())
                .filter(id -> statements.get(id).equals(statement))
                .findFirst();

        if (statement_position.isPresent()) {
            return statement_position.getAsInt(); // add 1 to convert from list position to id
        }

        else {
            System.out.println("Couldn't find an ID for the given statement");
            return -1;
        }
    }

    private int get_last_child(int id) {
        List<Statement> children = statements.get(id).findAll(Statement.class);

        return id + children.size() - 1;
    }

    private int get_last_child(Statement parent) throws StatementNotFoundException {
        List<Statement> children = parent.findAll(Statement.class);

        return statement_to_id(parent) + children.size() - 1;
    }

    private int get_subsequent_sibling(int id) {
        original_statement = statements.get(id);
        List<Statement> children = statements.get(id).findAll(Statement.class);

        test_id = id;

        while (test_id < statements.size()) {
            // System.out.println(test_id);
            // for each node in the sequence after id, if it can't be found in the children
            // list, it must be the next sibling. Therefore return it
            if (!children.stream()
                    .filter(child -> child.equals(statements.get(test_id)))
                    .findFirst()
                    .isPresent()) {
                return test_id;
            } else {
                test_id++;
            }
        }

        return end_id;
        // throw new StatementNotFoundException("Subsequent sibling function has looped
        // through 200 children, cannot find sibling for statement: " + "[" + id + "]" +
        // original_statement.toString());
    }

    private boolean is_statement_special(int id) {
        Statement statement = statements.get(id);

        return statement.isBreakStmt() || statement.isContinueStmt() || statement.isDoStmt()
                || statement.isForEachStmt() || statement.isForStmt() || statement.isIfStmt()
                || statement.isReturnStmt() || statement.isSwitchStmt() || statement.isTryStmt()
                || statement.isWhileStmt();
    }

    private boolean is_statement_special(Statement statement) {

        return statement.isBreakStmt() || statement.isContinueStmt() || statement.isDoStmt()
                || statement.isForEachStmt() || statement.isForStmt() || statement.isIfStmt()
                || statement.isReturnStmt() || statement.isSwitchStmt() || statement.isTryStmt()
                || statement.isWhileStmt();
    }
}
