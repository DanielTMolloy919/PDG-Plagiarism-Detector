/**
 * source_compare
 */

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class source_compare {

    public static void main(String[] args) {
        // if (args.length != 1) { // if the user didn't supply one argument  
        //     if (!isValidPath(args[0])) { // if the user didn't give a valid path
        //         System.out.println("Proper usage is: source_compare <submissions_folder>");
        //         return;
        //     }
        // }

        String parent_folder = args[0];

                
    }

    public static boolean isValidPath(String path) {
        try {
            Paths.get(path);
        } catch (InvalidPathException | NullPointerException ex) {
            return false;
        }
        return true;
    }
}