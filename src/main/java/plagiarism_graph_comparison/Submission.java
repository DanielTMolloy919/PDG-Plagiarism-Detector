package plagiarism_graph_comparison;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.SourceRoot;

import org.apache.commons.io.FileUtils;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;

// A class that holds all the methods and data of a single project
public class Submission {

    String file_name;
    String submission_name;

    ArrayList<MethodDeclaration> mds;
    ArrayList<MethodDeclaration> significant_mds;
    ArrayList<Method> method_objects;
    List<CompilationUnit> compilations;

    LinkedHashMap<MethodDeclaration,Integer> method_node_count;

    int counter;

    static int submission_count = 0;
    static int method_count = 0;

    static int minimum_node_count;

    LinkedHashMap<MethodDeclaration,File> md_to_file;

    public Submission(File root_dir) throws IOException, StatementNotFoundException  {

        file_name = root_dir.getPath();

        submission_name = root_dir.getName();

        submission_count++;

        compilations = new ArrayList<>();
        
        // Extract the compilation units out of every file in the project
        Collection<File> source_files = FileUtils.listFiles(root_dir, new String[] { "java" }, true);

        JavaParser parser = new JavaParser();

        LinkedHashMap<CompilationUnit,File> cu_to_file = new LinkedHashMap<>();

        for (File file : source_files) {
            ParseResult<CompilationUnit> result =  parser.parse(file);

            if (result.isSuccessful() && result.getResult().isPresent()) {
                CompilationUnit cu = result.getResult().get();
                compilations.add(cu);
                cu_to_file.put(cu, file);
            }
        }

        mds = new ArrayList<>(); // empty list of method nodes
        significant_mds = new ArrayList<>();

        method_objects = new ArrayList<Method>();

        md_to_file = new LinkedHashMap<>();

        method_node_count = new LinkedHashMap<>();

        // extract the method declarations out of each compilation unit
        for (CompilationUnit cp : compilations) {
            List<MethodDeclaration> methods = cp.findAll(MethodDeclaration.class);
            for (MethodDeclaration method : methods) {
                md_to_file.put(method, cu_to_file.get(cp));
                mds.add(method);
                method_node_count.put(method, method.findAll(Statement.class).size());
            }
        }

        significant_mds.addAll(mds);

        method_count += mds.size();

        // remove methods with less than 10 statements from the list
        for (MethodDeclaration method_node : mds) {
            if (method_node.findAll(Statement.class).size() <= minimum_node_count) {
                significant_mds.remove(method_node);
            }
        }

        // create a new method object for each method declaration
        significant_mds.stream().forEach(method_node -> {
            try {
                method_objects.add(new Method(method_node,this));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (StatementNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
}
