package plagiarism_graph_comparison;

import java.io.IOException;

import com.github.javaparser.ast.body.MethodDeclaration;

import plagiarism_graph_comparison.CDG.NoSinglePostdominatorException;
import plagiarism_graph_comparison.CFG.StatementNotFoundException;

public class Method {
    static int counter; // How many objects this class has created - used for naming export files
    
    MethodDeclaration method_node; // The method node given to the constructor when a new method object is created

    Blocks statement_graph; // The directed graph containing statement nodes

    CFG cfg; // method's control flow graph

    // CDG cdg; // method's control dependency graph

    DDG ddg; // method's data dependency graph

    PDG pdg;

    public Method(MethodDeclaration method_node) throws IOException, StatementNotFoundException, NoSinglePostdominatorException {
        this.method_node = method_node;

        Export.exporter(method_node, counter);

        this.statement_graph = new Blocks(method_node);
        
        Export.exporter(statement_graph.statements, counter);

        this.cfg = new CFG(statement_graph, counter);

        Export.exporter(cfg, counter);

        // this.cdg = new CDG(cfg, counter);

        this.ddg = new DDG(statement_graph, counter, cfg);

        // this.pdg = new PDG(method_node, counter);

        counter++;
    }
}
