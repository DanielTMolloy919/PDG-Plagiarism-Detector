package plagiarism_graph_comparison;

import java.util.UUID;

import com.github.javaparser.ast.stmt.Statement;

public class UniqueStatement {
    Statement statement;
    String uuid;

    public UniqueStatement(Statement statement) {
        this.statement = statement;
        this.uuid = UUID.randomUUID().toString();
    }
}
