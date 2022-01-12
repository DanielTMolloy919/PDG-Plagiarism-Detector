package plagiarism_graph_comparison;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.SourceRoot;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;


public class Submission {

    ArrayList<MethodDeclaration> method_nodes;
    ArrayList<MethodDeclaration> significant_mds;
    ArrayList<Method> method_objects;
    int counter;

    public Submission(SourceRoot root_dir) throws IOException, StatementNotFoundException  {

        SourceRoot source = root_dir;
        
        source.tryToParse();

        method_nodes = new ArrayList<>(); // empty list of method nodes
        significant_mds = new ArrayList<>();

        method_objects = new ArrayList<Method>();

        List<CompilationUnit> compilations = source.getCompilationUnits(); // get all the compilation units from the SourceRoot
        
        compilations.stream().forEach(cp -> this.method_nodes.addAll(cp.findAll(MethodDeclaration.class))); // loop through each compilation unit, find all the method nodes and add them to the list

        significant_mds.addAll(method_nodes);

        // Method test_method = new Method(method_nodes.get(64));

        // remove methods with less than 5 statements from the list
        for (MethodDeclaration method_node : method_nodes) {
            if (method_node.findAll(Statement.class).size() < 5) {
                significant_mds.remove(method_node);
            }
        }

        // iterate over all methods

        significant_mds.stream().forEach(method_node -> {
            try {
                method_objects.add(new Method(method_node));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (StatementNotFoundException e) {
                e.printStackTrace();
            }
        }); // create a method object for each node, which will build a pdg for each
    }
}
