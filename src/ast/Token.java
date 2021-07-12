package ast;
public class Token {
    public enum Type {  POP, LOP, BLOCKSTART,
                        BLOCKEND, BR, BRC, SEM,
                        COMP, LOG, PRINT,
                        SETTO, INT, DOUBLE, IDENTIFIER,
                        KEYDOUBLE, KEYINT, IF, ELSE, WHILE,
                        ERROR, WS, DOT, EOF
    };
    public Type kind;
    public String content;
    int index;
    int line, column;
}
