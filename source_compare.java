/**
 * source_compare
 */

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class source_compare {

    public static void main(String[] args) {
        // if (args.length != 1) { // if the user didn't supply one argument  
        //     if (!is_valid_path(args[0])) { // if the user didn't give a valid path
        //         System.out.println("Proper usage is: source_compare <submissions_folder>");
        //         return;
        //     }
        // }

        String parent_folder = args[0];

        method_test(parent_folder);

        // List<Submission> submission_set = submission_generator(parent_folder);

        
    }

    // private static List<Submission> submission_generator(String path) {

    //     File[] directories = new File(path).listFiles(File::isDirectory);

    //     System.out.println(Arrays.toString(directories))

    //     List<Submission> placeholder;
    //     return placeholder;

    // }

    public static void method_test(String path)  {
        File[] directories = new File(path).listFiles(File::isDirectory);

        System.out.println(Arrays.toString(directories));
    }

    public static boolean is_valid_path(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }
}