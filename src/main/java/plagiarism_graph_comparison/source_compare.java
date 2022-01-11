package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;



public class source_compare {

    private static final String root_folder_location = "test_files\\Base Programs\\3\\seng2200";
    
    public static void main(String[] args) throws IOException, StatementNotFoundException {
        
        Path root_folder = Paths.get(root_folder_location);

        SourceRoot source_root = new SourceRoot(root_folder);

        Submission test_submission = new Submission(source_root);
    }
}