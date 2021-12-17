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

    Graph<Integer, DefaultEdge> cfg;
    static int counter;
    List<Statement> statements; // a list of all the statements in the method - this doesn't change
    List<Integer> statement_blacklist;
    MethodDeclaration method_node;

    private Statement original_statement;
    private int test_index;

    public Method(MethodDeclaration method_node) throws IOException {

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

        for (int i = 0; i < statements.size() - 1; i++) { // loop through each statement
            
            if (statement_blacklist.contains(i)) { // if the statement is on the blacklist, don't go any further
                continue;
            }

            Statement statement = statements.get(i);

            int current_index = i;

            // System.out.println("[" + i + "] " + statement);

            // if its an 'If' statement
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
            
            // if its a 'for' or `do-while` statement
            else if(statement.isForStmt() || statement.isDoStmt() || statement.isForEachStmt()) {
                cfg.addEdge(get_last_child(current_index), current_index);
            }

            // if its a 'while' statement
            else if(statement.isWhileStmt()) {
                int last_child_index = get_last_child(current_index);

                cfg.addEdge(get_last_child(last_child_index), current_index);
                statement_blacklist.add(last_child_index);

                cfg.addEdge(current_index, get_subsequent_sibling(current_index));
            }

            // if its a 'case-switch' statement
            else if(statement.isSwitchStmt()) {
                SwitchStmt switchstmt = statement.asSwitchStmt();

                NodeList<SwitchEntry> switch_entries = switchstmt.getEntries();

                List<Statement> entries = new ArrayList<Statement>();

                switch_entries.stream().forEach(entry -> entry.getStatements().addAll(entries));                

                for (Statement entry : entries) {
                    int entry_index = statement_to_id(entry);
                    int last_child_index = get_last_child(entry_index);

                    cfg.addEdge(current_index, entry_index);
                    cfg.addEdge(last_child_index, get_subsequent_sibling(current_index));
                    statement_blacklist.add(last_child_index);
                }
            }
            
            // if its a `break` statement
            else if(statement.isBreakStmt()) {
                Statement parent = statement.findAncestor(Statement.class).get();

                cfg.addEdge(current_index, get_last_child(parent));
            }

            // if its a `return` statement
            else if(statement.isReturnStmt()) {
                cfg.addEdge(current_index, statements.size());
            }

            // if its a `continue` statement
            else if(statement.isContinueStmt()) {
                cfg.addEdge(current_index, statement_to_id(statement.findAncestor(Statement.class).get()));
            }

            else {
                cfg.addEdge(current_index, current_index+1);
            }

            
            exporter("CFGs");
        }      
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

    private int get_last_child(Statement parent) {
        List<Statement> children = parent.findAll(Statement.class);

        return statement_to_id(parent) + children.size() - 1;
    }

    private int get_subsequent_sibling(int id) {
        original_statement = statements.get(id);
        List<Statement> children = statements.get(id).findAll(Statement.class);

        test_index = id + 1;
        

        while (true) {
            // System.out.println(test_index);
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

    // private void export_graph() throws IOException {

    //     DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>();

    //     String file_path = "graphs\\CFG\\file" + counter + ".dot";

    //     export.exportGraph(cfg, new FileWriter(file_path));
    // }

    // private void export_method(MethodDeclaration md) throws IOException {

    //     String file_path = "graphs\\Methods\\file" + counter + ".txt";

    //     FileWriter f = new FileWriter(file_path);

    //     f.write(md.toString()); 
    //     f.flush();
    //     f.close();
    // }

    // private void export_statements(List<Statement> stmts) throws IOException {
    //     File export_file = new File("graphs\\Statements\\file" + counter + ".txt");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);

    //     String body = new String();

    //     for (int j = 0; j < stmts.size(); j++) {
    //         String addition = "[" + j + "] " + stmts.get(j).toString();
    //         body += addition;          
    //     }

    //     f.write(body); 
    //     f.flush();
    //     f.close();
    // }

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
                String addition = "[" + j + "] " + this.statements.get(j).toString();
                body += addition;          
            }
            f.write(body); 

            f.flush();
            f.close();
        }

        else if(type =="CFGs") {
            DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>();

            export.exportGraph(cfg, f);
        }
    }
}
