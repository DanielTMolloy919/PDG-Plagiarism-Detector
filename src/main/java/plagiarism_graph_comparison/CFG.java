package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.*;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class CFG {
    List<Statement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    int counter;

    Graph<BasicBlock, DefaultEdge> node_graph; // The method's control flow diagram
    
    List<Integer> statement_blacklist; // a list of statements to skip iteration - they have already been taken into account

    int current_id; // When looping through each statement, this is the ID of the current statement

    int end_id;

    private Statement original_statement;
    private int test_id;


    public CFG(Blocks blocks, int counter) throws IOException, StatementNotFoundException {

        this.statements = blocks.statements;
        this.basic_blocks = blocks.blocks;
        this.Statement_id_to_BasicBlock = blocks.Statement_id_to_BasicBlock;
        this.counter = counter;

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        for (BasicBlock basicBlock : basic_blocks) {
            node_graph.addVertex(basicBlock); // load all the statement nodes
        }

        Export.exporter(statements, counter);

        statement_blacklist = new ArrayList<Integer>();

        BasicBlock bb;

        bb = new BasicBlock(true);
        node_graph.addVertex(bb);
        Statement_id_to_BasicBlock.put("START", bb);

        bb = new BasicBlock(false);
        node_graph.addVertex(bb);
        Statement_id_to_BasicBlock.put("END", bb);

        if (statements.size() == 0) {
            link("START", "END");
            Export.exporter(this,counter);
            return;
        }

        else if (statements.size() == 1) {
            link("START", 0);
            link(0, "END");

            Export.exporter(this,counter);
            return;
        }

        end_id = 999;

        link("START", 0);

        for (int i = 0; i < statements.size() - 1; i++) { // loop through each statement

            Statement statement = statements.get(current_id);

            // if its an 'If' statement
            if (statement.isIfStmt()) {
                IfStmt ifstmt = statement.asIfStmt(); // get the specific if statement object

                int then_id = statement_to_id(ifstmt.getThenStmt());
                int last_then_child_id = get_last_child(then_id);
                int subsequent_id = get_subsequent_sibling(current_id);

                if (!is_statement_special(last_then_child_id)) { // link the end of 'then' to the next node, unless the node is another branching statement
                    link(last_then_child_id, subsequent_id);
                }

                statement_blacklist.add(last_then_child_id); // ensure then isn't automatically linked to else

                link(current_id, then_id); // link if to then

                if (ifstmt.getElseStmt().isPresent()) { // if there is an else statement, link if to else
                    int else_id = statement_to_id(ifstmt.getElseStmt().get());

                    link(current_id, else_id);
                }

                else { // if there isn't and else statement and if evaluates to false, if should be
                        // linked to the subsequent node
                    link(current_id, subsequent_id);
                }
            }

            // if its a 'for' or `do-while` statement
            else if (statement.isForStmt() || statement.isDoStmt() || statement.isForEachStmt()) {
                link(get_last_child(current_id), current_id);
            }

            // if its a 'while' statement
            else if (statement.isWhileStmt()) {
                int last_child_id = get_last_child(current_id);

                link(get_last_child(last_child_id), current_id);
                statement_blacklist.add(last_child_id);

                int subsequent_id = get_subsequent_sibling(current_id);

                link(current_id, subsequent_id);
            }

            // if its a 'case-switch' statement
            else if (statement.isSwitchStmt()) {
                SwitchStmt switchstmt = statement.asSwitchStmt();

                NodeList<SwitchEntry> switch_entries = switchstmt.getEntries();

                List<Statement> entries = new ArrayList<Statement>();

                switch_entries.stream().forEach(entry -> entry.getStatements().addAll(entries));

                for (Statement entry : entries) {
                    int entry_id = statement_to_id(entry);
                    int last_child_id = get_last_child(entry_id);

                    link(current_id, entry_id);
                    link(last_child_id, get_subsequent_sibling(current_id));
                    statement_blacklist.add(last_child_id);
                }
            }

            // if its a 'try-catch' statement
            else if (statement.isTryStmt()) {
                TryStmt trystmt = statement.asTryStmt();

                List<Statement> try_statements = trystmt.getTryBlock().getStatements();

                int last_try_id = statement_to_id(try_statements.get(try_statements.size() - 1));

                int subsequent_id = get_subsequent_sibling(current_id);

                link(last_try_id, subsequent_id);
                link(current_id, current_id + 1);

                statement_blacklist.add(subsequent_id - 1);
            }

            // if its a `return` statement
            else if (statement.isReturnStmt()) {
                link(current_id, "END");
            }

            // if its a `break` statement
            else if (statement.isBreakStmt()) {
                link(current_id, "END");
            }

            // if its a `continue` statement
            else if (statement.isContinueStmt()) {
                link(current_id, statement_to_id(statement.findAncestor(Statement.class).get()));
            }

            else if (!statement_blacklist.contains(current_id)) { // if the statement isn't on the blacklist,
                link(current_id, current_id + 1);
            }

            Export.exporter(this,counter);

            current_id++;
        }

        int last_statement_id = statements.size() - 1;

        link(last_statement_id, "END");

        return;
    }

    public boolean path_exists(int start_id, int end_id) {
        

        return true;
    }

    private void link(int start_id, int end_id) {
        if (end_id == 999) {
            link_meta(Integer.toString(start_id), "END");
        }

        else {
            link_meta(Integer.toString(start_id), Integer.toString(end_id));
        }
    }

    private void link(String start_id, int end_id) {
        if (end_id == 999) {
            link_meta(start_id, "END");
        }

        else {
            link_meta(start_id, Integer.toString(end_id));
        }
    }

    private void link(int start_id, String end_id) {
        link_meta(Integer.toString(start_id), end_id);
    }

    private void link(String start_id, String end_id) {
        link_meta(start_id, end_id);
    }

    private void link_meta(String start_id, String end_id){
        node_graph.addEdge(Statement_id_to_BasicBlock.get(start_id), Statement_id_to_BasicBlock.get(end_id));
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

    public class StatementNotFoundException extends Exception {
        public StatementNotFoundException(String str) {
            super(str);
        }
    }
}
