package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
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
    CFG cfg;
    MethodDeclaration method_node;
    Graph<BasicBlock, DefaultEdge> node_graph;
    int counter;

    List<UniqueExpression> expressions;
    LinkedHashMap<UniqueExpression, BasicBlock> Expression_to_BasicBlock;
    LinkedHashMap<String, BasicBlock> Variable_to_BasicBlock;

    List<Statement> statements; // a list of all the statements found in the method
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    LinkedHashMap<Integer, BasicBlock> Statement_to_BasicBlock;
    
    public DDG(MethodDeclaration method_node, int counter, CFG cfg) throws IOException {
        this.cfg = cfg;
        this.counter = counter;
        this.method_node = method_node;

        statements = method_node.findAll(Statement.class); // load all the statements

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class); // initialize the jgrapht graph
        Statement_id_to_BasicBlock = new LinkedHashMap<>();

        for (int i = 0; i < statements.size(); i++) {
            BasicBlock bb = new BasicBlock(statements.get(i), i);
            node_graph.addVertex(bb); // load all the statement nodes
            Statement_id_to_BasicBlock.put(Integer.toString(i), bb);
        }

        // map that returns the last time a given variable was assigned
        Variable_to_BasicBlock = new LinkedHashMap<>();
        Expression_to_BasicBlock = new LinkedHashMap<>();

        expressions = new ArrayList<UniqueExpression>();
        
        // statements.stream().filter(statement -> statement.isExpressionStmt()).map(statement -> statement.asExpressionStmt().getExpression()).collect(Collectors.toList());

        // get all statements that are expressions
        for (int i = 0; i < statements.size(); i++) {
            Statement statement = statements.get(i);
            UniqueExpression expression;

            if (statement.isExpressionStmt()) {
                expression = new UniqueExpression(statement.asExpressionStmt().getExpression());
                ExpressionImporter(expression, i);
            }

            else if (statement.isIfStmt()) {
                expression = new UniqueExpression(statement.asIfStmt().getCondition());
                ExpressionImporter(expression, i);
            }

            else if (statement.isDoStmt()) {
                expression = new UniqueExpression(statement.asDoStmt().getCondition());
                ExpressionImporter(expression, i);
            }

            else if (statement.isWhileStmt()) {
                expression = new UniqueExpression(statement.asWhileStmt().getCondition());
                ExpressionImporter(expression, i);
            }
        }
        
        // if (expressions.size() >= 10) {
        //     Export.exporter(method_node, counter);
        //     Export.exporter(statements, counter);
        // }

        // extract the variables out of each expression
        for (UniqueExpression uexpression : expressions) {
            Expression expression = uexpression.expression;

            List<String> variables = new ArrayList<>();

            if (expression.isVariableDeclarationExpr()) { // if a variable is being declared, i.e. int i = 1;
                variables = expression.asVariableDeclarationExpr().getVariables().stream().map(x -> x.getName().asString()).collect(Collectors.toList()); // extract variable strings being assigned
            }
            
            else if (expression.isAssignExpr()) {
                // get the two variables comprising an assignment expression
                variables.add(expression.asAssignExpr().getTarget().toString());
                variables.add(expression.asAssignExpr().getValue().toString());
            }

            for (String variable : variables) {
                if (Variable_to_BasicBlock.containsKey(variable)) { // if there's a previous occurance of this variable, add a link to it in the DDG
                    node_graph.addEdge(Expression_to_BasicBlock.get(uexpression), Variable_to_BasicBlock.get(variable));
                }

                Variable_to_BasicBlock.put(variable, Expression_to_BasicBlock.get(uexpression)); // update the last occurrence of this variable
            }
        }

        if (expressions.size() >= 10) {
            Export.exporter(this, counter);
        }

        System.out.println("yay");
    }

    public void ExpressionImporter(UniqueExpression expression, int i) {
        expressions.add(expression);
        Expression_to_BasicBlock.put(expression, Statement_id_to_BasicBlock.get(Integer.toString(i))); // makes sure the expression's associated statement can be found later on
    }

    public void cfg_annotator() {
        
    }
}

class UniqueExpression {
    Expression expression;
    String id;

    public UniqueExpression(Expression expression) {
        this.expression = expression;
        id = UUID.randomUUID().toString();
    }
}