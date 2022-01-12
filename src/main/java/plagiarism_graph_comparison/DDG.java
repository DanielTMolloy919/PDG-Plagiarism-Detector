package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;

public class DDG {
    List<Statement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    int counter;
    static int count = 0;

    CFG cfg;
    EdgeReversedGraph<BasicBlock,DefaultEdge> reversed_cfg;

    Graph<BasicBlock, DependencyEdge> node_graph; // The method's data dependence graph

    List<UniqueExpression> expressions;
    LinkedHashMap<UniqueExpression, BasicBlock> Expression_to_BasicBlock;
    LinkedHashMap<String, BasicBlock> Variable_to_BasicBlock;
    
    public DDG(Blocks blocks, int counter, CFG cfg) throws IOException {
        
        this.statements = blocks.statements;
        this.basic_blocks = blocks.blocks;
        this.Statement_id_to_BasicBlock = blocks.Statement_id_to_BasicBlock;
        this.counter = counter;
        
        count++;
        System.out.println(count);

        node_graph = new DefaultDirectedGraph<>(DependencyEdge.class);

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

            else if (statement.isForEachStmt()) {
                // expression = new UniqueExpression(statement.asForEachStmt().getIterable());
                expression = new UniqueExpression(statement.asForEachStmt().getIterable());
                ExpressionImporter(expression, i);
                expression = new UniqueExpression(statement.asForEachStmt().getVariable());
                ExpressionImporter(expression, i);
            }

            else if (statement.isForStmt()) {
                List<Expression> expressions = statement.asForStmt().getInitialization();
                for (Expression for_expression : expressions) {
                    UniqueExpression unique_expression = new UniqueExpression(for_expression);
                    ExpressionImporter(unique_expression, i);
                }
                expressions = statement.asForStmt().getUpdate();
                for (Expression for_expression : expressions) {
                    UniqueExpression unique_expression = new UniqueExpression(for_expression);
                    ExpressionImporter(unique_expression, i);
                }
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

            reversed_cfg = new EdgeReversedGraph<BasicBlock, DefaultEdge>(cfg.node_graph);

            Export.exporter(reversed_cfg, counter);
        }

        for (int j = 0; j < expressions.size(); j++) {
            link_statement(Expression_to_BasicBlock.get(expressions.get(j)));
        }

        // if (expressions.size() >= 10) {
        Export.exporter(this, counter);
        // }
    }

    public void ExpressionImporter(UniqueExpression expression, int i) {
        expressions.add(expression);
        Expression_to_BasicBlock.put(expression, Statement_id_to_BasicBlock.get(Integer.toString(i))); // makes sure the expression's associated statement can be found later on
    }

    // If theres a previous instance of a variable being defined or used, link it in the DDG according to DDG construction rules
    private void link_statement(BasicBlock post_bb) {

        List<String> defined_variables = post_bb.defined_variables;
        List<String> used_variables = post_bb.used_variables;

        // if there are no variables in the statement, skip
        if (defined_variables.size() == 0 && used_variables.size() == 0) {
            return;
        }

        // get all paths in the cfg between this node and the start node
        AllDirectedPaths<BasicBlock, DefaultEdge> all_directed_paths = new AllDirectedPaths<>(reversed_cfg);

        List<GraphPath<BasicBlock, DefaultEdge>> paths = all_directed_paths.getAllPaths(post_bb, Statement_id_to_BasicBlock.get("START"), true, 100);


        // if no paths exist, there can't be a link
        if (paths.size() == 0) {
            return;
        }

        GraphPath<BasicBlock, DefaultEdge> path = paths.get(0);

        // if we've got defined_variables, link up previous instances
        if (defined_variables.size() != 0) {
            for (String variable : defined_variables) {
                
                find_previous_vars(path, post_bb ,variable, true);
            }
        }

        if (used_variables.size() != 0) {
            for (String variable : used_variables) {
                find_previous_vars(path, post_bb ,variable, false);
            }
        }
    }


    private void find_previous_vars(GraphPath<BasicBlock, DefaultEdge> path, BasicBlock post_bb, String variable, boolean defined_variable) {

        // get all the basic blocks in a cfg path, and iterate through them
        List<BasicBlock> vertexes = path.getVertexList();

        // remove the first vertex of the path, since this is going to be post_bb
        vertexes.remove(0);

        for (BasicBlock pre_bb : vertexes) {
            // if we run across a previous definition of the variable
            if (pre_bb.defined_variables != null) {
                if (pre_bb.defined_variables.contains(variable)) {
                    // link it in the dd
                    node_graph.addEdge(post_bb, pre_bb, new DependencyEdge("DD"));
                    // return since we've found another definition
                    return;
                }
            }       
            // or if we run across a previous use of the variable
            if (pre_bb.used_variables != null) {
                if (pre_bb.used_variables.contains(variable)) {
                    // if current variable is a defined variable link it, otherwise skip, since there's no connection between two variable usages
                    if (defined_variable) {
                        node_graph.addEdge(post_bb, pre_bb, new DependencyEdge("DD"));
                        return;
                    }
                }
            }
        }
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