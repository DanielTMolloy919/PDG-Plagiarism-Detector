package plagiarism_graph_comparison;

import java.io.IOException;

import com.github.javaparser.ast.body.MethodDeclaration;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;

public class Method {
    String file_name;
    String method_name;

    static int counter; // How many objects this class has created - used for naming export files
    
    MethodDeclaration method_node; // The method node given to the constructor when a new method object is created

    Blocks statement_graph; // The directed graph containing statement nodes

    CFG cfg; // method's control flow graph

    DDG ddg; // method's data dependency graph

    PDG pdg; // method's program dependency graph

    GraphCompare graph_compare;

    public Method(MethodDeclaration method_node,String file_name) throws IOException, StatementNotFoundException {
        this.file_name = file_name;
        this.method_name = method_node.getNameAsString();

        this.method_node = method_node;

        Export.exporter(method_node, counter);

        this.statement_graph = new Blocks(method_node);
        
        Export.exporter(statement_graph.statements, counter);

        this.cfg = new CFG(statement_graph, counter);

        Export.exporter(cfg, counter);

        this.ddg = new DDG(statement_graph, counter, cfg);

        Export.exporter(ddg, counter);

        this.pdg = new PDG(ddg, counter);

        Export.exporter(pdg.cdg, counter);
        Export.exporter(pdg, counter);

        this.graph_compare = new GraphCompare(pdg, pdg, counter);

        counter++;
    }

    @Override
    public String toString() {
        return "Method \'" + method_name + "\' in file \'" + file_name + "\'";
    }
}
