package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.Statement;

public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        Path test_path = Paths.get("ReversePolishNotation.java");
        File test_file = test_path.toFile();

        CompilationUnit cu = StaticJavaParser.parse(test_file);
        
        cu.findAll(Statement.class).forEach(node -> System.out.println("* " + node));
        
    }
}
