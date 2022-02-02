package plagiarism_graph_comparison;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.Statement;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.graphml.GraphMLExporter;

public class Export {

    static boolean debugging = false; // toggles all graph exports - 

    public static void exportMD(Method method,int counter) throws IOException {

        if (!debugging) return;

        MethodDeclaration method_node = method.method_node;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/methods.txt");


        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file); 
            
        f.write(method_node.toString());

        f.flush();
        f.close();
    }

    public static void exportStmts(Method method,int counter) throws IOException {

        if (!debugging) return;

        List<UniqueStatement> statements = method.statement_graph.statements;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/statements.txt");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        String body = new String();

        for (int j = 0; j < statements.size(); j++) {
            String lines_array[] = statements.get(j).statement.toString().split("\n");
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

    public static void exportCFG(Method method,int counter) throws IOException {

        if (!debugging) return;

        CFG cfg = method.cfg;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/CFG.dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(cfg.node_graph, f);
    }

    public static void exportCDG(Method method,int counter) throws IOException {

        if (!debugging) return;

        Graph<BasicBlock, DependencyEdge> cdg = method.pdg.cdg;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/CDG.dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(cdg, f);
    }

    public static void exportSubPDG(AsSubgraph<BasicBlock, DependencyEdge> spdg,SubmissionCompare submission_compare,int counter) throws IOException {

        if (!debugging) return;

        File subgraph_file = new File("graphs/0 - Comparisons/" + submission_compare.sb1.submission_name + " <-> " + submission_compare.sb2.submission_name + "/" + submission_compare.first_method.method_name + " <-> " + submission_compare.second_method.method_name + "/" + counter + "/" + "subgraph_S.dot");

        subgraph_file.getParentFile().mkdirs();
        subgraph_file.createNewFile();

        FileWriter f1 = new FileWriter(subgraph_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export_subgraph = new DOTExporter<>(v -> v.toString());

        export_subgraph.exportGraph(spdg, f1);

        File graph_file = new File("graphs/0 - Comparisons/" + submission_compare.sb1.submission_name + " <-> " + submission_compare.sb2.submission_name + "/" + submission_compare.first_method.method_name + " <-> " + submission_compare.second_method.method_name + "/" + counter + "/" + "graph_G_prime.dot");

        graph_file.getParentFile().mkdirs();
        graph_file.createNewFile();

        FileWriter f2 = new FileWriter(graph_file);

        DOTExporter<BasicBlock, DependencyEdge> export_graph = new DOTExporter<>(v -> v.toString());
        export_graph.exportGraph(submission_compare.first_method.pdg.node_graph, f2);

        
    }

    // public static void exporter(Method method,int counter, String raw) throws IOException {

    //     Graph<BasicBlock, DependencyEdge> cdg = me

    //     File export_file = new File("graphs/" + counter + "/CDG-Raw.dot");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

    //     export.exportGraph(cdg, f);
    // }

    public static void exportDDG(Method method,int counter) throws IOException {

        if (!debugging) return;

        DDG ddg = method.ddg;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/DDG.dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(ddg.node_graph, f);
    }

    public static void exportPDG(Method method,int counter) throws IOException {

        if (!debugging) return;

        PDG pdg = method.pdg;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/PDG.dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(pdg.node_graph, f);
    }

    public static void exportRawPDG(Method method,int counter) throws IOException {

        if (!debugging) return;

        PDG pdg = method.pdg;

        File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/PDG_Raw.dot");

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);
            
        DOTExporter<BasicBlock, DependencyEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(pdg.node_graph, f);
    }

    // public static void exporter(DDG ddg ,int counter) throws IOException {
    //     File export_file = new File("graphs/DDGs/file" + counter + ".graphml");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     GraphMLExporter<BasicBlock, DependencyEdge> export = new GraphMLExporter<>(v -> v.toString());
    //     export.setExportEdgeLabels(true);
    //     export.setExportVertexLabels(true);

    //     export.exportGraph(ddg.node_graph, f);
    // }

    // public static void exportPDG(Method method ,int counter) throws IOException {
    //     PDG pdg = method.pdg;

    //     File export_file = new File("graphs/" + method.submission_name + "/" + method.method_name + "/PDG.graphml");

    //     export_file.getParentFile().mkdirs();
    //     export_file.createNewFile();

    //     FileWriter f = new FileWriter(export_file);
            
    //     GraphMLExporter<BasicBlock, DependencyEdge> export = new GraphMLExporter<>(v -> v.toString());
    //     export.setExportEdgeLabels(true);
    //     export.setExportVertexLabels(true);

    //     export.exportGraph(pdg.node_graph, f);
    // }

    public static void exporter(EdgeReversedGraph<BasicBlock, DefaultEdge> node_graph, int counter) throws IOException {

        if (!debugging) return;

        File export_file = new File("graphs/" + counter + "/CFG-Reversed.dot");        

        export_file.getParentFile().mkdirs();
        export_file.createNewFile();

        FileWriter f = new FileWriter(export_file);

        DOTExporter<BasicBlock, DefaultEdge> export = new DOTExporter<>(v -> v.toString());

        export.exportGraph(node_graph, f);
    }
}
