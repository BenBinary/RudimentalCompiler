package start;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * 
 * To filter out whitespaces and returns of the programm code
 * 
 * @author benediktkurz
 *
 */
public class Filter {
	
    LinkedList<Token> tokenlist = new LinkedList<>();
    Lexer lexer;
    int current;
    int index;
    List<CompilerError> errors;
    int line = 1;
    int column = 1;

    public Filter(Lexer lexer, List errors) {
        this.lexer = lexer;
        this.errors = errors;
    }
    
    void fillBuffer(int n) throws IOException {
        for (int i=0; i<n;i++) tokenlist.add(getNextFilteredToken());
    }

    Token getNextFilteredToken() throws IOException {
        Token erg;
        do {
            erg = lexer.getNextToken();
            erg.line = line;
            erg.column = column;
            if (erg.content.equals("\n") || erg.content.equals("\r")) {
                line++;
                column = 1;
            }
            else column += erg.content.length();
            if (erg.kind == Token.Type.ERROR) errors.add(new LexerError(erg, "Unknown token "+erg.content));
        } while (erg.kind == Token.Type.WS || erg.kind==Token.Type.ERROR);
        
        if (erg.kind == Token.Type.IDENTIFIER) {
        	
        	// In this detected just as normal strings and now figuring out if it is a keyword
        	
            switch (erg.content) {
                case "if": erg.kind = Token.Type.IF; break;
                case "else": erg.kind = Token.Type.ELSE; break;
                case "while": erg.kind = Token.Type.WHILE; break;
                case "double": erg.kind = Token.Type.KEYDOUBLE; break;
                case "int": erg.kind = Token.Type.KEYINT; break;
                case "print": erg.kind = Token.Type.PRINT; break;
            }
        }
        erg.index = index++;
        return erg;
    }

    public Token getToken(int i) throws IOException {
        fillBuffer(current+i+1 - tokenlist.size());
        return tokenlist.get(current+i);
    }
    
    public Token getToken() throws IOException {
        return getToken(0);
    }
    
    public void matchToken() {
        current++;
    }
    
    public void matchToken(Token.Type type, Set<Token.Type> sync) throws IOException, ParserError {
        if (getToken().kind == type) matchToken();
        else {
            Token currentToken = getToken();
            ParserError error = new ParserError(currentToken, type+" expected instead of "+currentToken.kind);
            errors.add(error);
            while (!sync.contains(getToken().kind)) matchToken();
            throw error;
        }
    }
}
