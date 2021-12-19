package plagiarism_graph_comparison;

import java.io.IOException;

import com.github.javaparser.ast.body.MethodDeclaration;

import plagiarism_graph_comparison.CFG.StatementNotFoundException;

public class Method {
    static int counter; // How many objects this class has created - used for naming export files
    
    MethodDeclaration method_node; // The method node given to the constructor when a new method object is created

    CFG cfg;

    public Method(MethodDeclaration method_node) throws IOException, StatementNotFoundException {
        this.method_node = method_node;

        this.cfg = new CFG(method_node, counter);

        counter++;
    }
}
