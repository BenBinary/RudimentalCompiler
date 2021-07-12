package start;

public class CompilerError extends Exception {
    String message;
    public CompilerError(String msg) {
        message = msg;
    }
    public String toString(){
        return message;
    }
}

class LexerError extends CompilerError {
    Token token;
    public LexerError(Token t, String msg) {
        super(msg);
        token = t;
    }
    public String toString(){
        return "line "+token.line+", column "+token.column+":  "+super.toString();
    }
}

class ParserError extends CompilerError {
    Token token;

    public ParserError(Token ts, String msg) {
        super(msg);
        token = ts;
    }
    public String toString(){
        return "line "+token.line+", column "+token.column+":  "+super.toString();
    }
}
