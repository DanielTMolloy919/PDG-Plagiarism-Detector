package plagiarism_graph_comparison;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

public class Blocks {
    List<Statement> statements;
    List<BasicBlock> blocks;
    
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    
    public Blocks(MethodDeclaration method_node) {
        statements = method_node.findAll(Statement.class); // load all the statements

        blocks = new ArrayList<>();
        Statement_id_to_BasicBlock = new LinkedHashMap<>();

        BasicBlock bb;

        for (int i = 0; i < statements.size(); i++) {
            bb = new BasicBlock(statements.get(i), i);
            blocks.add(bb);
            Statement_id_to_BasicBlock.put(Integer.toString(i), bb);
        }
    }
}
