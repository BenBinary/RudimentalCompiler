package ast;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ### PANIC MODUS ###
 * Man läuft in die Tiefe bis man ein Terminal gefunden hat.
 * Falls man kein Terminal gefunden hat, 
 * wird eine Exception geworfen und man klettert über alle NT im Stack wieder nach oben.
 * Bis wieder das entsprechende Synchronisierungszeichen gefunden wurde. 
 * 
 * Synchronisierungszeichen sind Aufsetzpunkte bei denen der Parser weitermachen kann. 
 * 
 * 
 * Fehler können noch zusätzlich in eine Liste eingetragen werden. 
 * 
 * 
 * ### Fehlerausgabe ###
 * Nach einer bestimmten Anzahl an Fehlern - meistens 3.
 * Diese Fehler werden dann im Anschluss ausgegeben.
 * 
 * @author benediktkurz
 *
 */

/**
 * 
 * ### AST ###
 * Es gibt beim normalen Syntaxbaum erstmal viele andere Knoten wie:
 *  -  Compilation Unit
 *  -  Declaration
 *  -  Statement
 *  
 *  Der Baum ist ziemlich aufgebläht. Man möchte nur den wesentlichen Baum haben. 
 *  Jeder Knoten hat eine Bedeutung.  
 *  
 *  Es ist der Syntaxbaum reduziert um die Knoten, die ich nicht brauche.
 * 
 * Hier sind einige Knoten zu viele bzw. die nur andere Knoten wieder aufrufen.
 * Bei Multiplikation: Produkt --> Atom -> Zahl --> Summe  
 * 
 * Dafür gibt es Knoten als Rückgabewerte der einzelnen Methoden im Parser.
 * Die Knoten gebe ich von unten nach oben durch. 
 * 
 * @author benediktkurz
 *
 */
public class Parser {
	
    Filter filter;
    List<CompilerError> errors;
    
    public Parser(Filter filter, List errors) {
        this.filter = filter;
        this.errors = errors;
    }

    // compilationUnit = { decl | stmnt } EOF
    // sync part of First(decl) + First(stmnt) + EOF
    
    // Betrachten als Wurzelknoten
    /**
     * 
     * @return
     * @throws IOException
     * @throws ParserError
     */
    CUNode compilationUnit() throws IOException, ParserError {
    	
    	// Tokentypen definieren
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.EOF);
        sync.add(Token.Type.KEYDOUBLE);
        sync.add(Token.Type.KEYINT);
        sync.add(Token.Type.IF);
        sync.add(Token.Type.WHILE);
        sync.add(Token.Type.PRINT);
        sync.add(Token.Type.SEM);
        sync.add(Token.Type.BLOCKSTART);
        
        CUNode result = new CUNode();

        while (filter.getToken().kind != Token.Type.EOF) {
            try {
                if (filter.getToken().kind == Token.Type.KEYDOUBLE
                        || filter.getToken().kind == Token.Type.KEYINT)
                    result.add(decl(sync));
                else result.add(stmnt(sync));;
            } catch (ParserError error) {
                // nothing to be done -- just proceed with decl, stmnt or EOF
            }
        }
        
        return result; 
    }

    // decl = type IDENTIFIER SEM
    // sync: SEM 
    /**
     * 
     * Sofern es sich um eine Deklaration handelt.
     * Dann wird ein DeclNode gebaut und zurückgegeben.
     * 
     * @param synco
     * @return
     * @throws IOException
     * @throws ParserError
     */
    DeclNode decl(Set<Token.Type> synco) throws IOException, ParserError {
    	
        Set<Token.Type> sync = new HashSet<>(synco);
        
        sync.add(Token.Type.SEM);

        // Herausfinden ob es ein Double oder ein int ist
        Token typ = type();
        Token name = null;
        Token end = null;
        
        try {
        	
        	name= filter.getToken();
            filter.matchToken(Token.Type.IDENTIFIER, sync);
            filter.matchToken(Token.Type.SEM, sync);
            typ = filter.getToken();
            
        } catch (ParserError error) {
        	
            if (filter.getToken().kind == Token.Type.SEM) {
            	// hier ist man am Ende einer Deklaration 
                filter.matchToken();
                return new DeclNode(end, typ, name);
            }
            
            // falls nicht, gebe ich es an das nächst höhere weiter
            else throw error;
        }
        
        return new DeclNode(end, typ, name);
    }
    
    
    
    /**
     * type = "double" | "int" -- we already know it is int or double !
     * 
     * @return
     * @throws IOException
     * @throws ParserError
     */
    Token type() throws IOException, ParserError {
    	
    	
    	Token res = filter.getToken();
    	// Synchronisierunszeichen 
        filter.matchToken();
        
		return res;
    }


    /**
     * stmnt = ifStmnt | whileStmnt | exprStmnt | emptyStmnt | block  | printStmnt
     * no syncs
     * 
     * Sammelknoten für die Grammatik 
     * Man arbeitet normalerweise nicht mit weiteren Kindknoten wie etwa für While-Statements
     * 
     * @param synco
     * @throws IOException
     * @throws ParserError
     */
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
    	
    	// Eigene Synchronisierungszeichen - welches vom Originalen Sync die Werte abnimmt
        // Normalerweise ist es mit Flags einfacher
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
            	// Wenn das aktuelle Token ein ELSE ist
                if (filter.getToken().kind == Token.Type.ELSE) ;
                else throw error;
            }
        }
    	// Wenn das aktuelle Token ein ELSE ist
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
    void expr(Set<Token.Type> synco) throws IOException, ParserError {
    	
        Set<Token.Type> sync =  new HashSet<>();
        sync.add(Token.Type.SETTO);

        if (filter.getToken(1).kind == Token.Type.SETTO) {
        	
            try {
            	
                filter.matchToken(Token.Type.IDENTIFIER, sync);
                
            } catch (ParserError error) {
                // ignore whatever there is on the left hand side
            	
            }
            
            filter.matchToken();
            
            expr(synco);
        }
        else comp(synco);
    }

    // comp = sum "<" sum | sum
    // sync:      ^ "<"
    void comp(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.COMP);

        try {
            sum(sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.COMP) ;
            else throw error;
        }
        if (filter.getToken().kind == Token.Type.COMP) {
            filter.matchToken();
            sum(synco);
        }
    }

    // sum = { prod { ("+"|"-") prod } }
    
    // 5 * 3 '+' 2 
    // hierbei 2 als Atom in atom
    
    
    // no syncs ???
    void sum(Set<Token.Type> synco) throws IOException, ParserError {
        prod(synco);
        while (filter.getToken().kind == Token.Type.POP) {
            filter.matchToken();
            prod(synco);
        }
    }
    // prod = { atom { ("*"|"/"|"%") atom } }
    // sync:         ^ "*" ...
    void prod(Set<Token.Type> synco) throws IOException, ParserError {
        Set<Token.Type> sync =  new HashSet<>(synco);
        sync.add(Token.Type.LOP);
        try {
            atom(sync);
        } catch (ParserError error) {
            if (filter.getToken().kind == Token.Type.LOP) ;
            else throw error;
        }
        while (filter.getToken().kind == Token.Type.LOP) {
            filter.matchToken();
            atom(synco);
        }
    }

    // atom = DOUBLE | INT | IDENTIFIER | ("+"|"-") atom | "(" expr ")"
    // no syncs
    void atom(Set<Token.Type> synco) throws IOException, ParserError {
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
            throw error;
        }
    }
}
