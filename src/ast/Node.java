package ast;

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



