package plagiarism_graph_comparison;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;


public class Submission {
    String path;
    ArrayList<SourceFile> source_files = new ArrayList<SourceFile>();

    public Submission(File subfolder) throws FileNotFoundException {
        this.path = subfolder.getAbsolutePath();
        File[] subfolder_files = subfolder.listFiles(); // array of all the project files

        for (int i = 0; i < subfolder_files.length; i++) {
            source_files.add(new SourceFile(subfolder_files[i]));
        }
    }
}
