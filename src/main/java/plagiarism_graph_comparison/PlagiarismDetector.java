package plagiarism_graph_comparison;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.LongStream;

import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

import org.apache.commons.math3.util.CombinatoricsUtils;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import plagiarism_graph_comparison.CFG.StatementNotFoundException;

@Command(name = "PlagiarismDetector")
public class PlagiarismDetector implements Callable<Integer>{

    // variables to change sensitivity of the comparison
    static double gamma = 0.8;
    static int minimum_node_count = 10;
    static boolean remove_insignificant_edges = true;

    @Parameters(index = "0", description = "The path to the submissions directory")
    private Path path; 

    @Option(names = {"-d", "--debugging"}, description = "Outputs the underlying graph set for each submission")
    boolean debugging;

    @Override
    public Integer call() throws Exception {

        // set sensitivity variables
        Export.debugging = debugging;
        SubmissionCompare.gamma = gamma;
        Submission.minimum_node_count = minimum_node_count;
        Method.remove_insignificant_edges = remove_insignificant_edges;
        
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(path);

        // create a submission for each file found in the provided directory
        File[] files = path.toFile().listFiles();

        List<Submission> submissions = new ArrayList<>();
        List<SubmissionCompare> submission_pairs = new ArrayList<>();

        for (File file : files) {
            try {
                submissions.add(new Submission(file));
            } catch (IOException | StatementNotFoundException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Directory Parsed, found " + Submission.submission_count + " submissions and " + Submission.method_count + " methods");

        // find all combinations of submissions to compare
        Iterator<int[]> combinations = CombinatoricsUtils.combinationsIterator(submissions.size(),2);

        // create a new SubmissionCompare object for each
        while (combinations.hasNext()) {
            final int[] combination = combinations.next();
            submission_pairs.add(new SubmissionCompare(submissions.get(combination[0]), submissions.get(combination[1])));
        }

        // print score for each submission pair
        for (SubmissionCompare pair : submission_pairs) {
            System.out.println("Score for submission pair " + pair.sb1.submission_name + " and " + pair.sb2.submission_name + " is " + Double.toString(pair.score));
        }

        return 0;
    }
    
    public static void main(String... args) {
        int exitCode = new CommandLine(new PlagiarismDetector()).execute(args);
        System.exit(exitCode);
    }
}