package plagiarism_graph_comparison;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
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

// This is the class that holds the data dependence graph, and associated information
public class DDG {
    List<UniqueStatement> statements;
    List<BasicBlock> basic_blocks;
    LinkedHashMap<String, BasicBlock> Statement_id_to_BasicBlock;
    LinkedHashMap<UniqueStatement, BasicBlock> Statement_to_BasicBlock;
    int counter;
    static int count = 0;

    CFG cfg;
    EdgeReversedGraph<BasicBlock,DefaultEdge> reversed_cfg;

    DefaultDirectedGraph<BasicBlock, DependencyEdge> node_graph; // The method's data dependence graph

    List<UniqueExpression> expressions;
    LinkedHashMap<UniqueExpression, BasicBlock> Expression_to_BasicBlock;
    LinkedHashMap<String, BasicBlock> Variable_to_BasicBlock;
    
    public DDG(Blocks blocks, int counter, CFG cfg) throws IOException {
        
        this.statements = blocks.statements;
        this.basic_blocks = blocks.blocks;
        this.Statement_id_to_BasicBlock = blocks.Statement_id_to_BasicBlock;
        this.Statement_to_BasicBlock = blocks.Statement_to_BasicBlock;
        this.counter = counter;
        
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
        for (UniqueStatement unique_statement : statements) {
                
            Statement statement = unique_statement.statement;
            BasicBlock bb = Statement_to_BasicBlock.get(unique_statement);
            UniqueExpression expression;

            // find all statements that might contain variables
            // based on the type of statement, extract all variable expressions

            if (statement.isExpressionStmt()) {
                expression = new UniqueExpression(statement.asExpressionStmt().getExpression(),bb);
                expression_importer(expression, unique_statement,"expression");
            }

            else if (statement.isIfStmt()) {
                expression = new UniqueExpression(statement.asIfStmt().getCondition(),bb);
                expression_importer(expression, unique_statement,"control");
            }

            else if (statement.isDoStmt()) {
                expression = new UniqueExpression(statement.asDoStmt().getCondition(),bb);
                expression_importer(expression, unique_statement,"control");
            }

            else if (statement.isWhileStmt()) {
                expression = new UniqueExpression(statement.asWhileStmt().getCondition(),bb);
                expression_importer(expression, unique_statement,"control");
            }

            else if (statement.isForEachStmt()) {
                // expression = new UniqueExpression(statement.asForEachStmt().getIterable());
                expression = new UniqueExpression(statement.asForEachStmt().getIterable(),bb);
                expression_importer(expression, unique_statement,"control");
                expression = new UniqueExpression(statement.asForEachStmt().getVariable(),bb);
                expression_importer(expression, unique_statement,"control");
            }

            else if (statement.isForStmt()) {
                List<Expression> expressions = statement.asForStmt().getInitialization();
                for (Expression for_expression : expressions) {
                    UniqueExpression unique_expression = new UniqueExpression(for_expression,bb);
                    expression_importer(unique_expression, unique_statement,"control");
                }
                expressions = statement.asForStmt().getUpdate();
                for (Expression for_expression : expressions) {
                    UniqueExpression unique_expression = new UniqueExpression(for_expression,bb);
                    expression_importer(unique_expression, unique_statement,"control");
                }
            }

            else if (statement.isReturnStmt()) {
                Optional<Expression> potential_expression = statement.asReturnStmt().getExpression();
                if (potential_expression.isPresent()) {
                    expression = new UniqueExpression(potential_expression.get(),bb);
                    expression_importer(expression, unique_statement,"return");
                }
                
            }
         }

        // for each expression containing variables
        // depending on the type of expression, do the following three steps
        // 1. extract any variables that are being defined  e.g. int i = 4
        // 2. extract any variables that are being used  e.g. add(i)
        // 3. if expression contains sub expressions, add them to the end of the queue
        for (int i = 0; i < expressions.size(); i++) {
            
            UniqueExpression uexpression = expressions.get(i);
            Expression expression = uexpression.expression;
            BasicBlock bb = Expression_to_BasicBlock.get(uexpression);

            List<String> defined_variables = new ArrayList<>();
            List<String> used_variables = new ArrayList<>();

            if (expression.isVariableDeclarationExpr()) { // if a variable is being declared, i.e. int i = 1;
                defined_variables = expression.asVariableDeclarationExpr().getVariables().stream().map(x -> x.getName().asString()).collect(Collectors.toList()); // extract variable strings being assigned
                bb.add_attribute("declaration");
            }
            
            else if (expression.isAssignExpr()) {
                // get the two expression comprising an assignment and add to the queue
                add_to_queue(uexpression, expression.asAssignExpr().getValue(),false);
                add_to_queue(uexpression, expression.asAssignExpr().getTarget(),true);
                bb.add_attribute("assignment");
            }

            else if (expression.isMethodCallExpr()) {
                List<Expression> mc_expressions = expression.asMethodCallExpr().getArguments();
                for (Expression mc_expression : mc_expressions) {
                    add_to_queue(uexpression, mc_expression,false);
                }

                if (expression.asMethodCallExpr().getScope().isPresent()) {
                    Expression scope = expression.asMethodCallExpr().getScope().get();
                    add_to_queue(uexpression, scope,false);
                }

                bb.add_attribute("method-call");
            }

            else if (expression.isObjectCreationExpr()) {
                List<Expression> oc_expressions = expression.asObjectCreationExpr().getArguments();
                for (Expression oc_expression : oc_expressions) {
                    add_to_queue(uexpression, oc_expression,false);
                }

                if (expression.asObjectCreationExpr().getScope().isPresent()) {
                    Expression scope = expression.asMethodCallExpr().getScope().get();
                    add_to_queue(uexpression, scope,false);
                }
            }

            else if (expression.isBinaryExpr()) {
                add_to_queue(uexpression, expression.asBinaryExpr().getLeft());
                add_to_queue(uexpression, expression.asBinaryExpr().getRight());
            }

            else if (expression.isInstanceOfExpr()) {
                add_to_queue(uexpression, expression.asInstanceOfExpr().getExpression(),false);
            }

            else if (expression.isFieldAccessExpr()) { 
                used_variables.add(expression.asFieldAccessExpr().getNameAsString());
            }

            else if (expression.isNameExpr()) {
                if (uexpression.is_defined) {
                    defined_variables.add(expression.asNameExpr().getNameAsString());
                }
                else {
                    used_variables.add(expression.asNameExpr().getNameAsString());
                }
            }

            //load corresponding basic block with variable data
            Expression_to_BasicBlock.get(uexpression).add_variables(defined_variables, used_variables);
        }

        reversed_cfg = new EdgeReversedGraph<BasicBlock, DefaultEdge>(cfg.node_graph);

        // for each expression, link up variables to the last time they were used in the DDG
        for (int j = 0; j < expressions.size(); j++) {
            link_statement(Expression_to_BasicBlock.get(expressions.get(j)));
        }
    }

    // Adds any found sub expressions to the queue
    public void add_to_queue(UniqueExpression parent_expression, Expression sub_expression) {
        UniqueExpression unique_expression = new UniqueExpression(sub_expression, parent_expression.bb);
        expressions.add(unique_expression);
        Expression_to_BasicBlock.put(unique_expression, parent_expression.bb);
        // if parent expression contains used/defined data, copy this over to the subexpression
        if (parent_expression.is_defined || parent_expression.is_used) {
            unique_expression.metadata(parent_expression.is_defined);
        }
    }

    // Additional variable 'is_defined' remembers whether the expression is being used or defined. Useful for expressions like 'nameExpr' that could be either
    public void add_to_queue(UniqueExpression parent_expression, Expression sub_expression, boolean is_defined) {
        UniqueExpression unique_expression = new UniqueExpression(sub_expression, parent_expression.bb);
        expressions.add(unique_expression);
        Expression_to_BasicBlock.put(unique_expression, parent_expression.bb);

        // if parent expression contains used/defined data, copy this over to the subexpression
        if (parent_expression.is_defined || parent_expression.is_used) {
            unique_expression.metadata(parent_expression.is_defined);
        }
        else {
            unique_expression.metadata(is_defined);
        }
    }

    // Add expressions extracted from statements to the queue
    public void expression_importer(UniqueExpression expression,UniqueStatement unique_statement,String attribute) {
        BasicBlock bb = Statement_to_BasicBlock.get(unique_statement);
        expressions.add(expression);
        Expression_to_BasicBlock.put(expression, bb); // makes sure the expression's associated statement can be found later on
        bb.add_attribute(attribute);
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

    // finds the last time a given variable was used in the program
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

// A small class to make every expression unique.
// Allows us to differentiate between identical expressions on different lines
class UniqueExpression {
    Expression expression;
    String uid;
    BasicBlock bb;
    boolean is_defined;
    boolean is_used;

    public UniqueExpression(Expression expression, BasicBlock bb) {
        this.expression = expression;
        this.bb = bb;
        uid = UUID.randomUUID().toString();
    }

    public void metadata(boolean is_defined) {
        if (is_defined) {
            this.is_defined = true;
        }

        else {
            this.is_used = true;
        }
    }
}