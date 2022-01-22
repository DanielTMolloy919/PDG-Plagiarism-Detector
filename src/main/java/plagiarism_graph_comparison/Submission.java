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
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;


public class Submission {

    String file_name;

    ArrayList<MethodDeclaration> method_nodes;
    ArrayList<MethodDeclaration> significant_mds;
    ArrayList<Method> method_objects;
    List<CompilationUnit> compilations;

    int counter;

    static int submission_count = 0;
    static int method_count = 0;

    LinkedHashMap<MethodDeclaration,File> md_to_file;

    public Submission(File root_dir) throws IOException, StatementNotFoundException  {

        file_name = root_dir.getPath();

        submission_count++;

        compilations = new ArrayList<>();

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

        method_nodes = new ArrayList<>(); // empty list of method nodes
        significant_mds = new ArrayList<>();

        method_objects = new ArrayList<Method>();

        md_to_file = new LinkedHashMap<>();

        for (CompilationUnit cp : compilations) {
            List<MethodDeclaration> methods = cp.findAll(MethodDeclaration.class);
            for (MethodDeclaration method : methods) {
                md_to_file.put(method, cu_to_file.get(cp));
                method_nodes.add(method);
            }
        }
        
        // compilations.stream().forEach(cp -> this.method_nodes.addAll(cp.findAll(MethodDeclaration.class))); // loop through each compilation unit, find all the method nodes and add them to the list

        significant_mds.addAll(method_nodes);

        method_count += method_nodes.size();


        // remove methods with less than 5 statements from the list
        for (MethodDeclaration method_node : method_nodes) {
            if (method_node.findAll(Statement.class).size() < 5) {
                significant_mds.remove(method_node);
            }
        }

        // iterate over all methods
        significant_mds.stream().forEach(method_node -> {
            try {
                method_objects.add(new Method(method_node,md_to_file.get(method_node).getPath()));
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } catch (StatementNotFoundException e) {
                e.printStackTrace();
            }
        }); // create a method object for each node, which will build a pdg for each
    }

    private void project_importer(SourceRoot source_root) {
        // Collection files = FileUtils.listFiles(source_root.);
    }
}
