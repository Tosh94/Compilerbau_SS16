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
		// System.out.println(ast);

		// TODO: implement check for declaration
		
		class LocalChecker {
			private boolean allDeclared;
			
			public boolean check(ASTNode root) {
				allDeclared = true;
				checksub(new HashSet<String>(), root);
				return allDeclared;
			}
			
			private Set<String> checksub(Set<String> declared, ASTNode root) {
				if(root.getAlphabet().equals(NonTerminal.DECLARATION)){
					Set<String> returnSet = new HashSet<String>();
					returnSet.addAll(declared);
					returnSet.add(findDeclaredID(root));
					return returnSet;
				} else if(root.getAlphabet().equals(Token.ID)) {
					if(!declared.contains(root.getAttribute()))
						allDeclared = false;
				} else if(root.getAlphabet().equals(NonTerminal.BRANCH) || root.getAlphabet().equals(NonTerminal.LOOP)) {
					for(ASTNode child : root.getChildren()) {
						checksub(declared, child);
					}
				} else {
					for(ASTNode child : root.getChildren()) {
						declared = checksub(declared, child);
					}
				}
				return declared;
			}
		}
		
		LocalChecker lc = new LocalChecker();
		if(lc.check(ast.getRoot()))
			return true;
		
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
