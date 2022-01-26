package plagiarism_graph_comparison;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

// All the basic blocks in a method
public class Blocks {
    List<UniqueStatement> statements;
    List<BasicBlock> blocks;
    MethodDeclaration method_node;
    Method method;

    static int count = 0; // for debugging
    
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    LinkedHashMap<UniqueStatement, BasicBlock> Statement_to_BasicBlock;

    public Blocks(Method method) {
        this.method = method;
        this.method_node = method.method_node;

        List<Statement> method_statements = method_node.findAll(Statement.class);
        statements = new ArrayList<>();

        for (Statement statement : method_statements) {
            UniqueStatement unique_statement = new UniqueStatement(statement);
            statements.add(unique_statement);
        }

        blocks = new ArrayList<>();
        Statement_id_to_BasicBlock = new LinkedHashMap<>();
        Statement_to_BasicBlock = new LinkedHashMap<>();

        List<String> method_parameters = new ArrayList<>();

        // extract all the variables defined in the method declaration
        method_parameters.addAll(method_node.getParameters().stream().map(x -> x.getName().asString()).collect(Collectors.toList()));

        // count++;
        // System.out.println(count);

        BasicBlock bb = new BasicBlock(statements.get(0), 0);
        blocks.add(bb);
        bb.set_variables(method_parameters);
        Statement_id_to_BasicBlock.put(Integer.toString(0), bb);

        for (int i = 1; i < statements.size(); i++) {
            bb = new BasicBlock(statements.get(i), i);
            blocks.add(bb);
            Statement_id_to_BasicBlock.put(Integer.toString(i), bb);
            Statement_to_BasicBlock.put(statements.get(i), bb);
        }
    }
}
