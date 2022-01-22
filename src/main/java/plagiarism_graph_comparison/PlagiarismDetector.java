package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import plagiarism_graph_comparison.CFG.StatementNotFoundException;



@Command(name = "PlagiarismDetector")
public class PlagiarismDetector implements Callable<Integer>{

    @Parameters(index = "0", description = "The path to the submissions directory")
    private Path path;

    @Override
    public Integer call() throws Exception { // your business logic goes here...
        
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(path);

        File[] files = path.toFile().listFiles();

        for (File file : files) {
            try {
                Submission test_submission = new Submission(file);
            } catch (IOException | StatementNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Directory Parsed, found " + Submission.submission_count + " submissions and " + Submission.method_count + " methods");

        return 0;
    }
    
    public static void main(String... args) {
        int exitCode = new CommandLine(new PlagiarismDetector()).execute(args);
        System.exit(exitCode);
    }
}