package plagiarism_graph_comparison;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

import org.apache.commons.math3.util.CombinatoricsUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
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

        List<Submission> submissions = new ArrayList<>();

        for (File file : files) {
            try {
                submissions.add(new Submission(file));
            } catch (IOException | StatementNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Directory Parsed, found " + Submission.submission_count + " submissions and " + Submission.method_count + " methods");

        Iterator<int[]> combinations = CombinatoricsUtils.combinationsIterator(submissions.size(),2);

        while (combinations.hasNext()) {
            final int[] combination = combinations.next();
            SubmissionCompare submission_compare = new SubmissionCompare(submissions.get(combination[0]), submissions.get(combination[1]));
        }

        return 0;
    }
    
    public static void main(String... args) {
        int exitCode = new CommandLine(new PlagiarismDetector()).execute(args);
        System.exit(exitCode);
    }
}