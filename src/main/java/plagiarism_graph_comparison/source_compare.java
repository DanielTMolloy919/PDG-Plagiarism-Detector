package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;


public class source_compare {

    private static final String parent_folder = "test_files\\Base Programs";
    public static void main(String[] args) throws Exception {

        // String parent_folder = args[0]; // collect parent folder location from user

        test(parent_folder);

    }

    // private static List<Submission> submission_generator(String path) {

    //     File[] directories = new File(path).listFiles(File::isDirectory);

    //     System.out.println(Arrays.toString(directories))

    //     List<Submission> placeholder;
    //     return placeholder;
    // }

    public static void test(String path) throws Exception  {
        File[] subdirectories = new File(path).listFiles(File::isDirectory); // get all the subdirectories of the root folder

        ArrayList<Submission> submission_set = new ArrayList<Submission>();
    }
}