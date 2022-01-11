package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.nio.dot.DOTExporter;

public class Export {
    public static void exporter(MethodDeclaration method_node,int counter) throws IOException {

        File export_file = new File("graphs\\Methods\\file" + counter + ".txt");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file); 
            
        f.write(method_node.toString());

        f.flush();
        f.close();
    }

    public static void exporter(List<Statement> statements,int counter) throws IOException {

        File export_file = new File("graphs\\Statements\\file" + counter + ".txt");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        String body = new String();

        for (int j = 0; j < statements.size(); j++) {
            String addition = "[" + j + "] " + statements.get(j).toString() + "\n";
            body += addition;
        }
        f.write(body);

        f.flush();
        f.close();
    }

    public static void exporter(CFG cfg,int counter) throws IOException {

        File export_file = new File("graphs\\CFGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(cfg.node_graph, f);
    }

    // public static void exporter(CDG cdg,int counter) throws IOException {

    //     File export_file = new File("graphs\\CDGs\\file" + counter + ".dot");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

    //     export.exportGraph(cdg.node_graph, f);
    // }

    // public static void exporter(PDG pdg,int counter) throws IOException {

    //     File export_file = new File("graphs\\CDGs\\file" + counter + ".dot");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     DOTExporter<Integer, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

    //     export.exportGraph(pdg.node_graph, f);
    // }

    public static void exporter(DDG ddg ,int counter) throws IOException {
        File export_file = new File("graphs\\DDGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(ddg.node_graph, f);
    }

    public static void exporter(EdgeReversedGraph<BasicBlock, DefaultEdge> node_graph, int counter) throws IOException {
        File export_file = new File("graphs\\Reversed_CFGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(node_graph, f);
    }
}
