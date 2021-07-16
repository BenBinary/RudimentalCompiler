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
 * if (expr) { stmntNode } else { elseStmnt }
 * 
 * Kann man statt der stmnt auch Blocks nehmen?
 * 
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
 * 
 * while (expr) { stmnt }
 * 
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

/**
 * Done
 * 
 * 
 * @author benediktkurz
 *
 */
class ExprStmntNode extends StmntNode {
	
	ExprNode expr;
	
	public ExprStmntNode(ExprNode expr, Token end) {
		
		super(expr.start, end);
		this.expr = expr;
		
	}
	
}

/**
 * 
 * Brauche ich hier weitere Informationen? 
 * Es müsste ja reichen, den Anfang und das Ende des Knoten zu kennen.
 * 
 * Anfang vor oder nach den Klammern - gleiche Frage auch für das Ende?
 * 
 */
class BlockNode extends StmntNode {
	
	StmntNode block;
	
	public BlockNode(StmntNode stmntNode) {
		
		super(stmntNode.start, stmntNode.end);
			
	}	
}


class PrintNode extends StmntNode {
	
	// BlockNode block;
	
	public PrintNode(Token start, Token end) {
		
		super(start, end);
		
	}
	
}


class EmptyStmntNode extends StmntNode {
	
	public EmptyStmntNode(Token token) {
		// TODO Auto-generated constructor stub
		
		super(token, token);
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
	
	// Alle Deklarationen und Statements
	List<Node> declAndStmtns = new LinkedList<Node>();
	
	public CUNode() {
		
		super(null, null);
		
		
	}	
	
	public void add(Node node) {
		
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


class ExprNode extends Node {
	
	public ExprNode(Node node) {
		
		super(node.start, node.end);
	
	}
	
}

/**
 * 
 * Braucht der CompNode Unterknoten von Sum?
 * 
 * @author benediktkurz
 *
 */
class CompNode extends Node {
	
	SumNode rightSumNode;
	SumNode leftSumNode;
	// Token start;
	// Token end;
	Token compToken;
	

	public CompNode(Token start, Token end, SumNode rightSumNode, SumNode leftSumNode, Token compToken) {
		super(start, end);
		this.rightSumNode = rightSumNode;
		this.leftSumNode = leftSumNode;
		this.compToken = compToken;
	}	
	
}

class SumNode extends Node {
	
	ProdNode leftProdNode;
	ProdNode rightProdNode;
	
	public SumNode(Token start, Token end, ProdNode leftProdNode, ProdNode rightProdNode) {
		super(start, end);
		this.leftProdNode = leftProdNode;
		this.rightProdNode = rightProdNode;
	}


	
	
}

class ProdNode extends Node {
	
	AtomNode leftAtomNode;
	AtomNode rightAtomNode;
	
	public ProdNode(Token start, Token end, AtomNode leftAtomNode, AtomNode rightAtomNode) {
		super(start, end);
		this.leftAtomNode = leftAtomNode;
		this.rightAtomNode = rightAtomNode;
	}
	
	
}

/**
 * 
 * Tokens können Strings allgemeiner Art sein.
 * 
 * @author benediktkurz
 *
 */
class AtomNode extends Node {
	
	Token typ;
	Token start;
	Token end;
	
	public AtomNode(Token typ, Token start, Token end) {
		
		super(start, end);
		this.start = start;
		this.end = end;
		
	}
	
}