package plagiarism_graph_comparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.stmt.Statement;

import org.apache.commons.text.translate.AggregateTranslator;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

public class BasicBlock {
    Integer id;
    UniqueStatement statement;

    // Support for special 'start' and 'end' CFG nodes
    boolean is_start;
    boolean is_end;

    // Variables defined and used in the statement
    List<String> defined_variables;
    List<String> used_variables;

    List<String> attributes;
    String type;

    public BasicBlock(UniqueStatement statement, int id) {
        this.statement = statement;
        this.id = id;

        is_start = false;
        is_end = false;
        
        defined_variables = new ArrayList<String>();
        used_variables = new ArrayList<String>();

        attributes = new ArrayList<String>();
    }

    public BasicBlock(boolean start) {
        if (start) {
            is_start = true;
            is_end = false;
        }

        else {
            is_start = false;
            is_end = true;
        }
    }

    public UniqueStatement get_statement() {
        return this.statement;
    }

    public int get_id() {
        return this.id;
    }

    // adds new variables to relevant lists if they aren't already on there
    public void add_variables(List<String> defined_variables, List<String> used_variables) {
        for (String variable : defined_variables) {
            if (!this.defined_variables.contains(variable)) {
                this.defined_variables.add(variable);
            }
        }

        for (String variable : used_variables) {
            if (!this.used_variables.contains(variable)) {
                this.used_variables.add(variable);
            }
        }
    }

    public void add_attribute(String attribute) {
        if (!attributes.contains(attribute)) {
            this.attributes.add(attribute);
        }
    }

    public void generate_type() {
        if (attributes.contains("control")) {
            this.type = "control";
        }

        else if (attributes.contains("jump")) {
            this.type = "jump";
        }

        else if (attributes.contains("return")) {
            this.type = "return";
        }

        else if (attributes.contains("declaration")) {
            this.type = "declaration";
        }

        else if (attributes.contains("assignment")) {
            this.type = "assignment";
        }
        
        else if (attributes.contains("method-call")) {
            this.type = "method-call";
        }

        else if (attributes.contains("expression")) {
            this.type = "assignment";
        }

        else {
            this.type = "other";
        }
    }

    public void set_variables(List<String> defined_variables) {
        this.defined_variables = defined_variables;
    }

    @Override
    public String toString() {
        if (is_start) {
            return "START";
        }

        else if (is_end) {
            return "END";
        }

        else {
            String extract =  Integer.toString(id) ;//+ " - " + statement.toString().substring(0,20) ;
            // extract = DOTEscaper.DOTEscape(extract);
            // extract=  "\"" + extract + "\"";
            return extract;
        }
        
    }
}

class DOTEscaper{
    
    private static final CharSequenceTranslator ESCAPE_CUSTOM;
    
    static {
        final Map<CharSequence, CharSequence> escapeCustomMap = new HashMap<>();
                    
        escapeCustomMap.put("\"" ,"" );
        escapeCustomMap.put("\'" ,"" );
        escapeCustomMap.put("[" ,"" );
        escapeCustomMap.put("]" ,"" );
        escapeCustomMap.put("(" ,"" );
        escapeCustomMap.put(")" ,"" );
        escapeCustomMap.put("{" ,"" );
        escapeCustomMap.put("\n" ,"" );
        escapeCustomMap.put(";" ,"" );
        escapeCustomMap.put(" ", "");
        ESCAPE_CUSTOM = new AggregateTranslator(new LookupTranslator(escapeCustomMap));
    }

    public static final String DOTEscape(final String input) {
        return ESCAPE_CUSTOM.translate(input);
    }
}
