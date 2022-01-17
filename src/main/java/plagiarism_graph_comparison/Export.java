package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;

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
            String lines_array[] = statements.get(j).toString().split("\n");
            List<String> lines = Arrays.asList(lines_array);

            for (String line : lines_array) {
                if(line.charAt(0) == '/' && line.charAt(1) == '/') {
                    continue;
                }
                String addition = "[" + j + "] " + line + "\n";
                body += addition;
                break;
            }
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

    public static void exporter(Graph<BasicBlock, DependencyEdge> cdg,int counter) throws IOException {

        File export_file = new File("graphs\\CDGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(cdg, f);
    }

    public static void exporter(Graph<BasicBlock, DependencyEdge> cdg,int counter, String raw) throws IOException {

        File export_file = new File("graphs\\Raw-CDGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(cdg, f);
    }

    public static void exporter(DDG ddg,int counter) throws IOException {

        File export_file = new File("graphs\\DDGs\\file" + counter + ".dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(ddg.node_graph, f);
    }

    // public static void exporter(DDG ddg ,int counter) throws IOException {
    //     File export_file = new File("graphs\\DDGs\\file" + counter + ".graphml");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     GraphMLExporter<BasicBlock, DependencyEdge> export = new GraphMLExporter<>(v -> v.toString());
    //     export.setExportEdgeLabels(true);
    //     export.setExportVertexLabels(true);

    //     export.exportGraph(ddg.node_graph, f);
    // }

    public static void exporter(PDG pdg ,int counter) throws IOException {
        File export_file = new File("graphs\\PDGs\\file" + counter + ".graphml");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        GraphMLExporter<BasicBlock, DependencyEdge> export = new GraphMLExporter<>(v -> v.toString());
        export.setExportEdgeLabels(true);
        export.setExportVertexLabels(true);

        export.exportGraph(pdg.node_graph, f);
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
