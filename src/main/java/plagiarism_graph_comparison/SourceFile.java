package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class SourceFile {
    String path;
    CompilationUnit cu;
    
    public SourceFile(File file) throws FileNotFoundException {
        path = file.getAbsolutePath();
        cu = StaticJavaParser.parse(file);
    }
}
