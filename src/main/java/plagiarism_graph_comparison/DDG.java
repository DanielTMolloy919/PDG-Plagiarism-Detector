package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DDG {
    List<Statement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    int counter;

    CFG cfg;

    Graph<BasicBlock, DefaultEdge> node_graph; // The method's data dependence graph

    List<UniqueExpression> expressions;
    LinkedHashMap<UniqueExpression, BasicBlock> Expression_to_BasicBlock;
    LinkedHashMap<String, BasicBlock> Variable_to_BasicBlock;
    
    public DDG(Blocks blocks, int counter, CFG cfg) throws IOException {
        
        this.statements = blocks.statements;
        this.basic_blocks = blocks.blocks;
        this.Statement_id_to_BasicBlock = blocks.Statement_id_to_BasicBlock;
        this.counter = counter;

        node_graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        this.cfg = cfg;

        for (BasicBlock basicBlock : basic_blocks) {
            node_graph.addVertex(basicBlock); // load all the statement nodes
        }

        // map that returns the last time a given variable was assigned
        Variable_to_BasicBlock = new LinkedHashMap<>();
        Expression_to_BasicBlock = new LinkedHashMap<>();

        expressions = new ArrayList<UniqueExpression>();
        
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

        // extract the variables out of each expression
        for (UniqueExpression uexpression : expressions) {
            Expression expression = uexpression.expression;

            List<String> defined_variables = new ArrayList<>();
            List<String> used_variables = new ArrayList<>();

            if (expression.isVariableDeclarationExpr()) { // if a variable is being declared, i.e. int i = 1;
                defined_variables = expression.asVariableDeclarationExpr().getVariables().stream().map(x -> x.getName().asString()).collect(Collectors.toList()); // extract variable strings being assigned
            }
            
            else if (expression.isAssignExpr()) {
                // get the two variables comprising an assignment expression
                defined_variables.add(expression.asAssignExpr().getTarget().toString());
                used_variables.add(expression.asAssignExpr().getValue().toString());
            }

            else if (expression.isMethodCallExpr()) {
                used_variables.addAll(expression.asMethodCallExpr().getArguments().stream().map(x -> x.toString()).collect(Collectors.toList()));
            }

            //load corresponding basic block with variable data
            Expression_to_BasicBlock.get(uexpression).set_variables(defined_variables, used_variables);

            List<String> variables =  new ArrayList<>();
            variables.addAll(defined_variables);
            variables.addAll(used_variables);


            for (String variable : variables) {
                if (Variable_to_BasicBlock.containsKey(variable)) { // if there's a previous occurance of this variable, add a link to it in the DDG
                    node_graph.addEdge(Expression_to_BasicBlock.get(uexpression), Variable_to_BasicBlock.get(variable));
                }

                Variable_to_BasicBlock.put(variable, Expression_to_BasicBlock.get(uexpression)); // update the last occurrence of this variable
            }
        }

        // if (expressions.size() >= 10) {
        Export.exporter(this, counter);
        // }
    }

    public void ExpressionImporter(UniqueExpression expression, int i) {
        expressions.add(expression);
        Expression_to_BasicBlock.put(expression, Statement_id_to_BasicBlock.get(Integer.toString(i))); // makes sure the expression's associated statement can be found later on
    }
}

// A small class to make every expression unique (i.e. differentiate expressions with the same hashcode) for use in LinkedHashMaps
class UniqueExpression {
    Expression expression;
    String id;

    public UniqueExpression(Expression expression) {
        this.expression = expression;
        id = UUID.randomUUID().toString();
    }
}