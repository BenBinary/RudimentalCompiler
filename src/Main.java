// import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
    	
        List<CompilerError> errors = new LinkedList<>();
        
        try {
            Token current;
            Lexer lexer = new Lexer("src/hello.scrt");
            Filter filter = new Filter(lexer, errors);
            Parser parser = new Parser(filter, errors);
            
            try {
                parser.compilationUnit();
            } catch (ParserError error) {
                System.out.println(error);
            }
            /* do {
                current = filter.getToken();
                filter.matchToken();
                System.out.println(current.line+","+current.column+":");
                System.out.println(" "+current.index + ": " + current.kind+"  "+current.content);
            } while (current.kind != Token.Type.EOF); */
            
            
            // Ausgabe der Errors
            if (errors.size()>0) {
                System.out.println("Errors:");
                int i=1;
                for (CompilerError error:errors)
                    System.out.println(i++ +") "+error);
            }
            
            lexer.close();
        } catch (IOException ex) {
            System.out.println("Problem: "+ex);
        }
    }
}
