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
 * Für jede Klasse ein Ausdrucke (auch für jedes Statment) beschleunigt den Übersetzungsprozess.
 * 
 * JavaCC hat keine Hierarchie bei der AST-Erzeugung, diese muss man sich nachträglich bauen.
 * AntLR ist besser für AST.
 * Lex und YaCC unterstützen dies gar nicht. 
 * 
 * @author benediktkurz
 *
 */
class StmntNode extends Node {
	
	public StmntNode(Token start, Token end) {
		super(start, end);
	}
	
}

/**
 * 
 * For all if-Nodes 
 * 
 * Is checking if the if-Statement has an else-Clause
 * 
 * @author benediktkurz
 *
 */
class IfNode extends StmntNode {
	
	Node expr;
	StmntNode stmntNode;
	StmntNode elseStmnt;

	public IfNode(Token start, Node expr, StmntNode stmnt, StmntNode elseStmnt) {
		
		super(start, elseStmnt!=null ? elseStmnt.end : stmnt.end );
		
		this.expr = expr;
		this.stmntNode = stmnt;
		this.elseStmnt = elseStmnt;
		
		
	}
	
}

/**
 * 
 * @author benediktkurz
 *
 */
class WhileNode extends StmntNode {
	
	Node expr;
	StmntNode stmntNode;
	
	public WhileNode(Token start, Node expr, StmntNode stmnt) {
		
		super(start, stmnt.end);
		this.expr = expr;
		this.stmntNode = stmnt;
		
	}
	
	
}

class ExprNode extends StmntNode {
	
	ExprNode expr;
	
	
	public ExprNode(ExprNode expr, Token end) {
		
		super(expr.start, end);
		this.expr = expr;
		
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