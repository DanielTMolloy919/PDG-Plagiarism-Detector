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
    Statement statement;
    public BasicBlock(Statement statement, int id) {
        this.statement = statement;
        this.id = id;
    }

    public Statement get_statement() {
        return this.statement;
    }

    public int get_id() {
        return this.id;
    }

    @Override
    public String toString() {
        // String extract =  Integer.toString(id); //+ statement.toString().substring(0,20);
        // extract = extract.replace("\"", "\\\"");
        // extract = "\"" + extract + "\"";
        String extract = DOTEscaper.DOTEscape(Integer.toString(id));
        return extract;
    }
}

class DOTEscaper{
    
    private static final CharSequenceTranslator ESCAPE_CUSTOM;
    
    static {
        final Map<CharSequence, CharSequence> escapeCustomMap = new HashMap<>();
                    
        escapeCustomMap.put("\"" ,"" );
        ESCAPE_CUSTOM = new AggregateTranslator(new LookupTranslator(escapeCustomMap));
    }

    public static final String DOTEscape(final String input) {
        return ESCAPE_CUSTOM.translate(input);
    }
}
