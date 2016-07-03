package checker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lexer.Symbol;
import parser.Rule;
import symbols.NonTerminals.NonTerminal;
import symbols.Tokens.Token;

/**
 * Checks if identifiers are declared before using them.
 */
public class DeclarationChecker {

	// AST
	private AST ast;

	/**
	 * Get AST
	 * 
	 * @return AST
	 */
	public AST getAst() {
		return ast;
	}

	/**
	 * Constructor. Requires a right most analysis from the parser to initiate a
	 * new abstract syntax tree. This tree is then used for subsequent semantic
	 * checks.
	 * 
	 * @param symbols
	 *            List of symbols.
	 * @param analysis
	 *            Right most analysis of the parser.
	 */
	public DeclarationChecker(List<Symbol> symbols, List<Rule> analysis) {
		ast = new AST(symbols, analysis);
	}

	/**
	 * Check if every identifier which is used has been declared before.
	 * 
	 * @return true, if everything is correct, false if an identifier used
	 *         before its declaration.
	 */
	public boolean checkDeclaredBeforeUsed() {
		 System.out.println(ast);

		// TODO: implement check for declaration

		return false;
	}

	/**
	 * Get declared id as attribute of node.
	 * 
	 * @param node
	 *            Node
	 * @return Declared id.
	 */
	/**
	 * @param node
	 * @return
	 */
	private String findDeclaredID(ASTNode node) {
		assert (node.getAlphabet().equals(NonTerminal.DECLARATION));
		assert (node.getChildren().size() == 2);
		return node.getChildren().get(1).getAttribute();
	}

}
