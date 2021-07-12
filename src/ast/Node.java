package ast;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Basisknotenklasse
 * Start und Endknoten 
 * 
 * Man braucht dies für Fehlermeldungen
 * 
 * 
 * @author benediktkurz
 *
 */
public class Node {
	
	// Bestimmen Anfang und Ende eines Ausdrucks
	Token start, end;
	
	
	public Node(Token start, Token end) {
		super();
		this.start = start;
		this.end = end;
	}


	public Node() {
		// TODO Auto-generated constructor stub
	}

}

/**
 * 
 * Folge von Deklarationenen und Statements
 * 
 * @author benediktkurz
 *
 */
class CUNode extends Node {
	
	List<Node> declAndStmtns = new LinkedList<Node>();
	
	public CUNode() {
		
		super(null, null);
		
		
	}	
	
	private void add(Node node) {
		
		this.declAndStmtns.add(node);
		
		if (start==null) {
			start=node.start;
			end = node.end; 
		} 

	}
	
}

/**
 * 
 * Für Deklarationen wie Double oder Int
 * 
 * @author benediktkurz
 *
 */
class DeclNode extends Node {
	
	Token typ;
	Token name;
	
	public DeclNode(Token end, Token typ, Token name) {
		super(typ, end);
		this.typ = typ;
		this.name = name;
	}
	
	
	
	
}