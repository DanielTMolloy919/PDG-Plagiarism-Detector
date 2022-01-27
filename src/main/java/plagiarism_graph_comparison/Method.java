package plagiarism_graph_comparison;

import java.io.IOException;

import com.github.javaparser.ast.body.MethodDeclaration;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;

public class Method {
    String file_name;
    String submission_name;
    String method_name;

    static int counter; // How many objects this class has created - used for naming export files
    
    int node_count;
    
    MethodDeclaration method_node; // The method node given to the constructor when a new method object is created

    Blocks statement_graph; // The directed graph containing statement nodes

    CFG cfg; // method's control flow graph

    DDG ddg; // method's data dependency graph

    PDG pdg; // method's program dependency graph

    SubmissionCompare graph_compare;

    public Method(MethodDeclaration method_node,Submission submission) throws IOException, StatementNotFoundException {

        System.out.println(counter);
        
        this.file_name = submission.md_to_file.get(method_node).getPath();
        this.submission_name = submission.submission_name;
        this.method_name = method_node.getNameAsString();

        this.method_node = method_node;

        Export.exportMD(this, counter);

        this.statement_graph = new Blocks(this);
        
        Export.exportStmts(this, counter);

        this.cfg = new CFG(statement_graph, counter);

        Export.exportCFG(this, counter);

        this.ddg = new DDG(statement_graph, counter, cfg);

        Export.exportDDG(this, counter);

        this.pdg = new PDG(this, counter);

        Export.exportCDG(this, counter);
        Export.exportRawPDG(this, counter);

        pdg.remove_insignificant_edges();

        Export.exportPDG(this, counter);

        this.node_count = this.pdg.node_graph.vertexSet().size();

        counter++;
    }

    @Override
    public String toString() {
        return "Method \'" + method_name + "\' in file \'" + file_name + "\'\n";
    }
}
