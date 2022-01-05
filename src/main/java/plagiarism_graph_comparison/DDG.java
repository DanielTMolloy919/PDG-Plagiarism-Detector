package plagiarism_graph_comparison;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DDG {
    Graph<Integer, DefaultEdge> cfg;
    MethodDeclaration method_node;
    Graph<BasicBlock, DefaultEdge> node_graph;
    int counter;

    List<Statement> statements; // a list of all the statements found in the method
    LinkedHashMap<String, BasicBlock> Statement_to_BasicBlock;
    
    
    public DDG(MethodDeclaration method_node, int counter) {
        // this.cfg = cfg.node_graph;
        this.counter = counter;
        this.method_node = method_node;
        

        statements = method_node.findAll(Statement.class); // load all the statements

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph
        Statement_to_BasicBlock = new LinkedHashMap<String, BasicBlock>();

        for (int i = 0; i < statements.size(); i++) {
            BasicBlock bb = new BasicBlock(statements.get(i), i);
            node_graph.addVertex(bb); // load all the statement nodes
            Statement_to_BasicBlock.put(Integer.toString(i), bb);
        }

        // map that returns the last time a given variable was assigned
        LinkedHashMap<SimpleName, BasicBlock> Variable_to_BasicBlock = new LinkedHashMap<SimpleName, BasicBlock>();

        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            if (statement.isExpressionStmt()) {
                // System.out.println(Statement_to_BasicBlock.get(Integer.toString(i)).toString());

                // if its a variable assignment, continue
                if (statement.asExpressionStmt().getExpression().isVariableDeclarationExpr()) {
                    // get the variable associated with a variable assignment
                    List<SimpleName> variables = statement.asExpressionStmt().getExpression().asVariableDeclarationExpr().getVariables().stream().map(x -> x.getName()).collect(Collectors.toList());

                    for (SimpleName variable : variables) { // for each variable, 
                        BasicBlock new_bb = Variable_to_BasicBlock.get(variable);

                        if (Variable_to_BasicBlock.containsKey(variable)) { // if a previous assignment exists
                            node_graph.addEdge(Statement_to_BasicBlock.get(Integer.toString(i)), new_bb); // link it in the DDG
                        }

                        Variable_to_BasicBlock.put(variable, new_bb); // then update what the previous assignment for this variable is, or add a new entry if this is the variable's first assignment
                    }
                }
            }
        }

        System.out.println("yay");
    }

    
}
