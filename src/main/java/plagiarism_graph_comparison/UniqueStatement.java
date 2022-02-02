package plagiarism_graph_comparison;

import java.util.UUID;

import com.github.javaparser.ast.stmt.Statement;


// two statements may be identical, but on different lines
// This class ensures that every statement is unique
public class UniqueStatement {
    Statement statement;
    String uuid;

    public UniqueStatement(Statement statement) {
        this.statement = statement;
        this.uuid = UUID.randomUUID().toString();
    }
}
