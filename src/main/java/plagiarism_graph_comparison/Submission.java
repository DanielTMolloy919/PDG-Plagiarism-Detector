package plagiarism_graph_comparison;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.SourceRoot;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


public class Submission {

    ArrayList<MethodDeclaration> method_nodes;
    ArrayList<Method> method_objects;
    int counter;

    public Submission(SourceRoot root_dir) throws IOException {

        SourceRoot source = root_dir;
        
        source.tryToParse();

        method_nodes = new ArrayList<MethodDeclaration>(); // empty list of method nodes

        method_objects = new ArrayList<Method>();

        List<CompilationUnit> compilations = source.getCompilationUnits(); // get all the compilation units from the SourceRoot
        
        compilations.stream().forEach(cp -> this.method_nodes.addAll(cp.findAll(MethodDeclaration.class))); // loop through each compilation unit, find all the method nodes and add them to the list

        method_nodes.stream().forEach(method_node -> {
            try {
                method_objects.add(new Method(method_node));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }); // create a method object for each node, which will build a pdg for each
    }

    

    
}
