package plagiarism_graph_comparison;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.stmt.Statement;

public class BasicBlock {
    Integer id;
    Statement statement;
    public BasicBlock(Statement statement, int id) {
        this.statement = statement;
        this.id = id;
    }

    public Statement get_statement() {
        return this.statement;
    }

    public int get_id() {
        return this.id;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + statement.toString().substring(0,15);
    }
}
