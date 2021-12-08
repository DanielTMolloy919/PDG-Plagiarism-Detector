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
import com.github.javaparser.utils.SourceRoot;


public class Submission {

    private static final String root_folder_location = "test_files\\Base Programs\\1\\producer";

    // public Submission(File subfolder) throws IOException {
    public static void main(String[] args) throws IOException {        
        Path root_folder = Paths.get(root_folder_location);

        SourceRoot source_root = new SourceRoot(root_folder);
        source_root.tryToParse();

        List<CompilationUnit> compilations = source_root.getCompilationUnits(); // get all the compilation units from the SourceRoot

        ArrayList<MethodDeclaration> method_nodes = new ArrayList<MethodDeclaration>(); // empty list of method nodes
        
        compilations.stream().forEach(cp -> method_nodes.addAll(cp.findAll(MethodDeclaration.class))); // loop through each compilation unit, find all the method nodes and add them to the list

        method_nodes.stream();

        method_nodes.forEach(node -> System.out.println("* " + node));
    }
}
