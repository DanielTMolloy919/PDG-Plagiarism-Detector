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
    boolean is_start;
    boolean is_end;

    public BasicBlock(Statement statement, int id) {
        this.statement = statement;
        this.id = id;
        is_start = false;
        is_end = false;
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

    public Statement get_statement() {
        return this.statement;
    }

    public int get_id() {
        return this.id;
    }

    @Override
    public String toString() {
        String extract =  Integer.toString(id) + " - " + statement.toString().substring(0,20) ;
        extract = DOTEscaper.DOTEscape(extract);
        // extract=  "\"" + extract + "\"";
        return extract;
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
