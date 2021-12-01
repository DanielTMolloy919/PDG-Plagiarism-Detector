package plagiarism_graph_comparison;

import java.io.File;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ast_analyser {

    private static final String test_file_path = "ReversePolishNotation.java";

    public static void main(String[] args) throws Exception {
        CompilationUnit cu = StaticJavaParser.parse(new File(test_file_path));

        VoidVisitor<Void> method_name_visitor = new method_name_printer();
        method_name_visitor.visit(cu, null);
    }

    private static class method_name_printer extends VoidVisitorAdapter<Void> {
        
        @Override
        public void visit(MethodDeclaration md, Void arg) { // make the second parameter a void since we aren't using it
            super.visit(md, arg);
            System.out.println("Method Name Printed: " + md.getName());
        }
    }
}