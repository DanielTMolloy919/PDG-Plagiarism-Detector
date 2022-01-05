package plagiarism_graph_comparison;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
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
        LinkedHashMap<String, BasicBlock> Variable_to_BasicBlock = new LinkedHashMap<String, BasicBlock>();

        LinkedHashMap<Expression, BasicBlock> Expression_to_BasicBlock = new LinkedHashMap<Expression, BasicBlock>();

        List<Expression> expressions = new ArrayList<Expression>();
        
        // statements.stream().filter(statement -> statement.isExpressionStmt()).map(statement -> statement.asExpressionStmt().getExpression()).collect(Collectors.toList());

        // get all statements that are expressions
        for (Statement statement : statements) {
            if (statement.isExpressionStmt()) {
                Expression expression = statement.asExpressionStmt().getExpression();
                expressions.add(expression);
                Expression_to_BasicBlock.put(expression, Statement_to_BasicBlock.get(statement)); // make sure the expressions associated statement can be found
            }
        }

        // extract the variables out of each expression
        for (Expression expression : expressions) {
            List<String> variables = new ArrayList<>();

            if (expression.isVariableDeclarationExpr()) { // if a variable is being declared, i.e. int i = 1;
                variables = expression.asVariableDeclarationExpr().getVariables().stream().map(x -> x.getName().asString()).collect(Collectors.toList()); // extract variable strings being assigned
            }
            
            else if (expression.isAssignExpr()) {
                // get the two variables comprising an assignment expression
                variables.add(expression.asAssignExpr().getTarget().toString());
                variables.add(expression.asAssignExpr().getValue().toString());
            }

            variables.stream().forEach(x -> Variable_to_BasicBlock.put(x, Expression_to_BasicBlock.get(expression))); // add a new entry, as this is the first time we will have seen the variable
        }

        System.out.println("yay");
    }

    
}
