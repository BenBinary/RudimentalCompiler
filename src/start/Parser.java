package start;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {
	
    Filter filter;
    List<CompilerError> errors;
    
    public Parser(Filter filter, List errors) {
        this.filter = filter;
        this.errors = errors;
    }

    // compilationUnit = { decl | stmnt } EOF
    // sync part of First(decl) + First(stmnt) + EOF
    void compilationUnit() throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.EOF);
        sync.add(Token.Type.KEYDOUBLE);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);

        while (filter.getToken().kind != Token.Type.EOF) {
            try {
                if (filter.getToken().kind == Token.Type.KEYDOUBLE
                        || filter.getToken().kind == Token.Type.KEYINT)
                    decl(sync);
                else stmnt(sync);
            } catch (ParserError error) {
                // nothing to be done -- just proceed with decl, stmnt or EOF
            }
        }
    }

    // decl = type IDENTIFIER SEM
    // sync: SEM
    void decl(Set<Token.Type> synco) throws IOException, ParserError {
    	
        Set<Token.Type> sync = new HashSet<>(synco);
        
        sync.add(Token.Type.SEM);

        type();
        // try {
            filter.matchToken(Token.Type.IDENTIFIER, sync);
            filter.matchToken(Token.Type.SEM, sync);
        /*
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) {
                filter.matchToken();
                return;
            }
            else throw error;
        } */
    }
    // type = "double" | "int" -- we already know it is int or double !
    void type() throws IOException, ParserError {
        filter.matchToken();
    }

    // stmnt = ifStmnt | whileStmnt | exprStmnt | emptyStmnt | block
    //         | printStmnt
    // no syncs
    void stmnt(Set<Token.Type> synco) throws IOException, ParserError {
    	
       Token.Type kind = filter.getToken().kind;
       if (kind == Token.Type.IF) ifStmnt(synco);
       else if (kind == Token.Type.WHILE) whileStmnt(synco);
       else if (kind == Token.Type.BLOCKSTART) block(synco);
       else if (kind == Token.Type.PRINT) printStmnt(synco);
       else if (kind == Token.Type.SEM) filter.matchToken();
       else exprStmnt(synco);
       
    }

    // printStmnt = "print" expr ";"
    // sync: ";"
    void printStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        filter.matchToken();
        try {
            expr(sync);
            filter.matchToken(Token.Type.SEM,sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) {
                filter.matchToken();
                return;
            }
            else throw error;
        }
    }
    // ifStmnt = "if" "(" expr ")" stmnt [ "else" stmnt ]
    // sync:                    ^ else, First(stmnt)
    void ifStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.ELSE);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.BLOCKSTART);
        sync.add(Token.Type.SEM);

        filter.matchToken(); // must be "if"
        toElse: {
            try {
                filter.matchToken(Token.Type.BR, sync);
                expr(sync);
                filter.matchToken(Token.Type.BRC, sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.ELSE) break toElse;
                else if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
                        || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                        || filter.getToken().kind == Token.Type.BLOCKSTART) ;
                else throw error;
            }
            sync = new HashSet<>(synco);
            sync.add(Token.Type.ELSE);
            try {
                stmnt(sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.ELSE) ;
                else throw error;
            }
        }
        if (filter.getToken().kind == Token.Type.ELSE) {
            filter.matchToken();
            stmnt(synco);
        }
    }

    // whileStmnt = "while" "(" expr ")" stmnt
    // sync:                          ^ else, First(stmnt)
    void whileStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.BLOCKSTART);
        sync.add(Token.Type.SEM);

        filter.matchToken(); // must be "while"
        try {
            filter.matchToken(Token.Type.BR,sync);
            expr(sync);
            filter.matchToken(Token.Type.BRC,sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
                    || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                    || filter.getToken().kind == Token.Type.BLOCKSTART) ;
            else throw error;
        }
        stmnt(synco);
    }

    // exprStmnt = expr ";"
    // sync: ";"
    void exprStmnt(Set<Token.Type> synco) throws IOException, ParserError {
        
    	Set<Token.Type> sync = new HashSet<>(synco);
        sync.add(Token.Type.SEM);

        try {
            expr(sync);
            filter.matchToken(Token.Type.SEM, sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.SEM) ;
            else throw error;
        }
    }

    // emptyStmnt = ";"  -- see above stmnt

    // block = "{" { decl | stmnt } "}"
    // sync: First(decl) + first(stmnt) + "}"
    void block(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.BLOCKEND);
        sync.add(Token.Type.KEYDOUBLE);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);

        filter.matchToken(); // must be "{"
        toBlockend: while (filter.getToken().kind != Token.Type.BLOCKEND
                && filter.getToken().kind != Token.Type.EOF
        ) {
            try {
                if (filter.getToken().kind == Token.Type.KEYDOUBLE
                        || filter.getToken().kind == Token.Type.KEYINT)
                    decl(sync);
                else stmnt(sync);
            } catch (ParserError error) {
                if (filter.getToken().kind == Token.Type.BLOCKEND) break toBlockend;
                else if (filter.getToken().kind == Token.Type.IF || filter.getToken().kind == Token.Type.WHILE
                        || filter.getToken().kind == Token.Type.PRINT || filter.getToken().kind == Token.Type.SEM
                        || filter.getToken().kind == Token.Type.BLOCKSTART) ;
                else throw error;
            }
        }
        sync = new HashSet<>(synco);
        sync.add(Token.Type.BLOCKEND);
        
        try {
            filter.matchToken(Token.Type.BLOCKEND, sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.BLOCKEND) filter.matchToken();
            else throw error;
        }
        
    }

    // expr = IDENTIFIER "=" expr | comp
    // sync:            ^ "="
    void expr(Set<Token.Type> synco) {
    	
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.SETTO);

        if (filter.getToken(1).kind == Token.Type.SETTO) {
        		
            filter.matchToken(Token.Type.IDENTIFIER, sync);
             
            filter.matchToken();
            
            expr(synco);
        }
        else comp(synco);
    }

    // comp = sum "<" sum | sum
    // sync:      ^ "<"
    void com(Set<Token.Type> synco) {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.COMP);

        // try {
        sum(sync);
        /*
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.COMP) ;
            else throw error;
        } */
        if (filter.getToken().kind == Token.Type.COMP) {
            filter.matchToken();
            sum(synco);
        }
    }

    // sum = { prod { ("+"|"-") prod } }
    
    // 5 * 3 '+' 2 
    // hierbei 2 als Atom in atom
    
    
    // no syncs ???
    void sum(Set<Token.Type> synco) {
        prod(synco);
        while (filter.getToken().kind == Token.Type.POP) {
            filter.matchToken();
            prod(synco);
        }
    }
    // prod = { atom { ("*"|"/"|"%") atom } }
    // sync:         ^ "*" ...
    void prod(Set<Token.Type> synco) {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.LOP);
        
        // try {
        atom(sync);
        /* } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.LOP) ;
            else throw error;
        } */
        
        while (filter.getToken().kind == Token.Type.LOP) {
            filter.matchToken();
            atom(synco);
        }
    }

    // atom = DOUBLE | INT | IDENTIFIER | ("+"|"-") atom | "(" expr ")"
    // no syncs
    void atom(Set<Token.Type> synco) {
        if (filter.getToken().kind == Token.Type.DOUBLE) {
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.INT) {
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.IDENTIFIER) {
            filter.matchToken();
        }
        else if (filter.getToken().kind == Token.Type.POP) {
           filter.matchToken();
           atom(synco);
        }
        else if (filter.getToken().kind == Token.Type.BR) {
            filter.matchToken();
            expr(synco);
            filter.matchToken(Token.Type.BRC, synco);
        }
        else {
            Token currentToken = filter.getToken();
            ParserError error = new ParserError(currentToken, "Expression expected");
            errors.add(error);
            // panic !!!
            while (!synco.contains(filter.getToken().kind)) filter.matchToken();
            // throw error;
        }
    }
}
