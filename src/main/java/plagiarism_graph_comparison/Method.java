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

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;

public class Method {
    static int counter; // How many objects this class has created - used for naming export files

    MethodDeclaration method_node; // The method node given to the constructor when a new method object is created

    List<Statement> statements; // a list of all the statements found in the method
    List<Integer> statement_blacklist; // a list of statements to skip iteration - they have already been taken into account

    Graph<Integer, DefaultEdge> cfg; // The method's control flow diagram

    int end_id; // This is the ID of the last statement in the method

    int current_id; // When looping through each statement, this is the ID of the current statement

    private Statement original_statement;
    private int test_id;

    public Method(MethodDeclaration method_node) throws IOException, StatementNotFoundException {

        this.method_node = method_node;

        cfg = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph

        statements = method_node.findAll(Statement.class); // load all the statements

        exporter("Methods");

        exporter("Statements");

        statement_blacklist = new ArrayList<Integer>();

        for (int i = 0; i < statements.size(); i++) {
            cfg.addVertex(i); // load all the statement nodes
            // statement_ids.add(i); // load all the statement ids into the list
        }

        end_id = 999;
        
        cfg.addVertex(end_id); // end node

        for (int i = 0; i < statements.size()-1; i++) { // loop through each statement


            Statement statement = statements.get(current_id);

            // if its an 'If' statement
            if (statement.isIfStmt()) {
                IfStmt ifstmt = statement.asIfStmt(); // get the specific if statement object

                int then_id = statement_to_id(ifstmt.getThenStmt());
                int subsequent_id = get_subsequent_sibling(current_id);
                int last_child_id = get_last_child(then_id);

                if (!is_statement_special(last_child_id)) { // link the end of 'then' to the next node, unless the node is another branching statement
                    cfg.addEdge(last_child_id, subsequent_id);
                } 
                
                statement_blacklist.add(last_child_id); // ensure then isn't automatically linked to else

                cfg.addEdge(current_id, then_id); // link if to then

                if (ifstmt.hasElseBlock()) { // if there is an else statement, link if to else
                    int else_id = statement_to_id(ifstmt.getElseStmt().get());

                    cfg.addEdge(current_id, else_id);
                } 

                else { // if there isn't and else statement and if evaluates to false, if should be linked to the subsequent node
                    cfg.addEdge(current_id, subsequent_id);
                }
            }
            
            // if its a 'for' or `do-while` statement
            else if(statement.isForStmt() || statement.isDoStmt() || statement.isForEachStmt()) {
                cfg.addEdge(get_last_child(current_id), current_id);
            }

            // if its a 'while' statement
            else if(statement.isWhileStmt()) {
                int last_child_id = get_last_child(current_id);

                cfg.addEdge(get_last_child(last_child_id), current_id);
                statement_blacklist.add(last_child_id);

                int subsequent_id = get_subsequent_sibling(current_id);

                cfg.addEdge(current_id, subsequent_id);
            }

            // if its a 'case-switch' statement
            else if(statement.isSwitchStmt()) {
                SwitchStmt switchstmt = statement.asSwitchStmt();

                NodeList<SwitchEntry> switch_entries = switchstmt.getEntries();

                List<Statement> entries = new ArrayList<Statement>();

                switch_entries.stream().forEach(entry -> entry.getStatements().addAll(entries));                

                for (Statement entry : entries) {
                    int entry_id = statement_to_id(entry);
                    int last_child_id = get_last_child(entry_id);

                    cfg.addEdge(current_id, entry_id);
                    cfg.addEdge(last_child_id, get_subsequent_sibling(current_id));
                    statement_blacklist.add(last_child_id);
                }
            }

            // if its a 'try-catch' statement
            else if(statement.isTryStmt()) {
                TryStmt trystmt = statement.asTryStmt();

                int last_try_id = get_last_child(trystmt.getTryBlock().getStatements().stream().findFirst().get());

                int subsequent_id = get_subsequent_sibling(current_id);

                cfg.addEdge(last_try_id, subsequent_id);
                cfg.addEdge(current_id, current_id + 1);

                statement_blacklist.add(subsequent_id-1);
            }
            
            // if its a `return` statement
            else if(statement.isReturnStmt()) {
                cfg.addEdge(current_id, end_id);
            }

            // if its a `break` statement
            else if(statement.isBreakStmt()) {
                cfg.addEdge(current_id, end_id);
            }

            // if its a `continue` statement
            else if(statement.isContinueStmt()) {
                cfg.addEdge(current_id, statement_to_id(statement.findAncestor(Statement.class).get()));
            }

            else if(!statement_blacklist.contains(current_id)) { // if the statement isn't on the blacklist, 
                cfg.addEdge(current_id, current_id+1);
            } 

            exporter("CFGs");

            current_id++;
        }

        int last_statement_id = statements.size()-1;
    
        cfg.addEdge(last_statement_id, end_id);

        exporter("CFGs");
    }

    private int statement_to_id(Statement statement) throws StatementNotFoundException {
        OptionalInt statement_position = IntStream.range(0, statements.size())
        .filter(id -> statements.get(id).equals(statement))
        .findFirst();

        if (statement_position.isPresent()) {
            return statement_position.getAsInt(); // add 1 to convert from list position to id
        }
        
        else {
            throw new StatementNotFoundException("Couldn't find an ID for the given statement");
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
            // for each node in the sequence after id, if it can't be found in the children list, it must be the next sibling. Therefore return it
            if (!children.stream()
            .filter(child -> child.equals(statements.get(test_id)))
            .findFirst()
            .isPresent()) {
                return test_id;
            }
            else {
                test_id++;
            }
        }

        return end_id;
        // throw new StatementNotFoundException("Subsequent sibling function has looped through 200 children, cannot find sibling for statement: " + "[" + id + "]" + original_statement.toString());
    }

    private boolean is_statement_special(int id) {
        Statement statement = statements.get(id);

        return statement.isBreakStmt() || statement.isContinueStmt() || statement.isDoStmt() || statement.isForEachStmt() || statement.isForStmt() || statement.isIfStmt() || statement.isReturnStmt() || statement.isSwitchStmt() || statement.isTryStmt() || statement.isWhileStmt();
    }

    private boolean is_statement_special(Statement statement) {

        return statement.isBreakStmt() || statement.isContinueStmt() || statement.isDoStmt() || statement.isForEachStmt() || statement.isForStmt() || statement.isIfStmt() || statement.isReturnStmt() || statement.isSwitchStmt() || statement.isTryStmt() || statement.isWhileStmt();
    }

    private void exporter(String type) throws IOException {

        String file_extension = (type == "CFGs") ? ".dot" : ".txt";
        File export_file = new File("graphs\\" + type + "\\file" + counter + file_extension);

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);

        if (type == "Methods") {
            f.write(this.method_node.toString()); 

            f.flush();
            f.close();
        } 
        
        else if(type == "Statements") {
            String body = new String();

            for (int j = 0; j < this.statements.size(); j++) {
                String addition = "[" + j + "] " + this.statements.get(j).toString() + "\n";
                body += addition;          
            }
            f.write(body); 

            f.flush();
            f.close();
        }

        else if(type =="CFGs") {
            DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

            export.exportGraph(cfg, f);
        }
    }

    public class StatementNotFoundException extends Exception {
        public StatementNotFoundException (String str) {
            super(str);
        }
    }
}
