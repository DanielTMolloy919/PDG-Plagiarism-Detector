package plagiarism_graph_comparison;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;

import plagiarism_graph_comparison.Method.StatementNotFoundException;


public class Submission {

    ArrayList<MethodDeclaration> method_nodes;
    ArrayList<Method> method_objects;
    int counter;

    public Submission(SourceRoot root_dir) throws IOException, StatementNotFoundException {

        SourceRoot source = root_dir;
        
        source.tryToParse();

        method_nodes = new ArrayList<MethodDeclaration>(); // empty list of method nodes

        method_objects = new ArrayList<Method>();

        List<CompilationUnit> compilations = source.getCompilationUnits(); // get all the compilation units from the SourceRoot
        
        compilations.stream().forEach(cp -> this.method_nodes.addAll(cp.findAll(MethodDeclaration.class))); // loop through each compilation unit, find all the method nodes and add them to the list

        Method test_method = new Method(method_nodes.get(1));

        // iterate over all methods

        // method_nodes.stream().forEach(method_node -> {
        //     try {
        //         method_objects.add(new Method(method_node));
        //     } catch (IOException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     }
        // }); // create a method object for each node, which will build a pdg for each
    }

    

    
}
