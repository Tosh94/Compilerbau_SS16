package generator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import checker.AST;
import checker.ASTNode;
import symbols.NonTerminals.NonTerminal;
import symbols.Tokens.Token;
import util.Pair;

/**
 * Generator which converts an abstract syntax tree into the Jasmin language.
 *
 */
public class JasminGenerator {

	// The symbol table mapping an identifier to its id
	//Changed definition: Each variable name holds a list of pairs of scope levels and ids
	private HashMap<String, List<Pair<Integer, Integer>>> symbolTable = new HashMap<String, List<Pair<Integer, Integer>>>();
	private int scopeLevel = 0; //Counts depth of scope
	private int varCount = 0; //Counts used vars, only gets decreased when leaving a scope, clearing the newest ids for reuse
	private int ifCount = 0;
	private int loopCount = 0;
	private int negCount = 0;
	private int relCount = 0;

	/**
	 * Given an abstract syntax tree with respect to WhileGrammar, this method
	 * translates it to the Jasmin language which is a textual representation of
	 * Java-Bytecode.
	 * 
	 * @param name
	 *            Name of the program
	 * @param ast
	 *            The abstract syntax tree
	 * @return Jasmin program as a String
	 * @throws GeneratorException
	 *             Exception while generation the Jasmin code
	 */
	public String translateWHILE(String name, AST ast) throws GeneratorException {
		StringBuilder result = new StringBuilder();

		// Define a class with the given name which is a subclass of Object.
		appendString(result, ".class public " + name);
		appendString(result, ".super java/lang/Object");
		appendString(result, ";");
		// Define the standard constructor which calls super().
		appendString(result, "; standard initializer");
		appendString(result, ".method public <init>()V");
		appendString(result, "  aload_0");
		appendString(result, "  invokenonvirtual java/lang/Object/<init>()V");
		appendString(result, "  return");
		appendString(result, ".end method");
		appendString(result, "");
		// Then start building the main method.
		appendString(result, ".method public static main([Ljava/lang/String;)V");
		// It simply reserves 100 registers and a data stack of depth 100 - not
		// beautiful but works for our small examples
		appendString(result, "  ; set limits used by this method");
		appendString(result, "  .limit locals 100");
		appendString(result, "  .limit stack 100");

		// Now walk the abstract syntax tree in-order and translate it to Jasmin
		// code
		// At the same time the symbol table is generated
		ASTNode root = ast.getRoot();
		assert (root.getAlphabet().equals(NonTerminal.START));
		assert (root.getChildren().size() == 2);
		result.append(translateProg(root.getChildren().get(0)));

		// here the main method ends
		appendString(result, "; done");
		appendString(result, "return");
		appendString(result, "");
		appendString(result, ".end method");

		return result.toString();
	}

	private String translateProg(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.PROGRAM));
		StringBuilder result = new StringBuilder();

		assert (node.getChildren().size() == 1 || node.getChildren().size() == 2);
		result.append(translateStat(node.getChildren().get(0)));
		if(node.getChildren().size() == 2) {
			result.append(translateProg(node.getChildren().get(1)));
		}

		return result.toString();
	}
	
	private String translateStat(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.STATEMENT));
		StringBuilder result = new StringBuilder();

		assert(node.getChildren().size() == 1 || node.getChildren().size() == 2);
		ASTNode child = node.getChildren().get(0);
		assert(child.getAlphabet() instanceof NonTerminal);
		NonTerminal nt = (NonTerminal) child.getAlphabet();
		assert(nt == NonTerminal.DECLARATION || nt == NonTerminal.ASSIGNMENT || 
				nt == NonTerminal.BRANCH || nt == NonTerminal.LOOP || 
				nt == NonTerminal.OUT);
		switch(nt) {
		case DECLARATION:
			result.append(translateDecl(child));
			break;
		case ASSIGNMENT:
			result.append(translateAssi(child));
			break;
		case BRANCH:
			result.append(translateBran(child));
			break;
		case LOOP:
			result.append(translateLoop(child));
			break;
		case OUT:
			result.append(translateWrite(child));
			break;
		default:
		}

		return result.toString();
	}
	
	private String translateDecl(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.DECLARATION));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 2);
		assert(node.getChildren().get(1).getAlphabet().equals(Token.ID));
		String name = node.getChildren().get(1).getAttribute();
		if(symbolTable.containsKey(name)) { //Add variable to table if it's not in there, add variable to list, 
			// if its old scope level is smaller, overwrite reference if old variable has same scope (latest decl counts)
			List<Pair<Integer, Integer>> vars = symbolTable.get(name);
			if(vars.size() > 0){
				if(vars.get(vars.size() - 1).getFirst() < scopeLevel) {
					varCount++;
					vars.add(new Pair<Integer, Integer>(scopeLevel, varCount));
					appendString(result, "ldc 0");
					appendString(result, "istore " + varCount);
				} else if (vars.get(vars.size() - 1).getFirst() == scopeLevel){
					appendString(result, "ldc 0");
					appendString(result, "istore " + vars.get(vars.size() - 1).getSecond());
				} else {
					throw new GeneratorException("ScopeLevel in symbol table too high.");
				}
			} else {
				varCount++;
				vars.add(new Pair<Integer, Integer>(scopeLevel, varCount));
				appendString(result, "ldc 0");
				appendString(result, "istore " + varCount);
			}
		} else {
			symbolTable.put(name,  new ArrayList<Pair<Integer, Integer>>());
			List<Pair<Integer, Integer>> vars = symbolTable.get(name);
			varCount++;
			vars.add(new Pair<Integer, Integer>(scopeLevel, varCount));
			appendString(result, "ldc 0");
			appendString(result, "istore " + varCount);
		}
		
		return result.toString();
	}
	
	private String translateAssi(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.ASSIGNMENT));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 3 || node.getChildren().size() == 5);
		if(node.getChildren().size() == 3) {
			assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.EXPR));
			result.append(translateExpr(node.getChildren().get(2)));
		} else {
			assert(node.getChildren().get(2).getAlphabet().equals(Token.READ));
			result.append(translateReadInt());
		}
		assert(node.getChildren().get(0).getAlphabet().equals(Token.ID));
		List<Pair<Integer, Integer>> var = symbolTable.get(node.getChildren().get(0).getAttribute());
		int id = var.get(var.size() - 1).getSecond();
		appendString(result, "istore " + id);
		
		return result.toString();
	}
	
	private String translateBran(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.BRANCH));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 7 || node.getChildren().size() == 11);
		assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.GUARD));
		assert(node.getChildren().get(5).getAlphabet().equals(NonTerminal.PROGRAM));
		result.append(translateGuar(node.getChildren().get(2)));
		if(node.getChildren().size() > 7) {
			assert(node.getChildren().get(9).getAlphabet().equals(NonTerminal.PROGRAM));
			appendString(result, "ifeq else" + ifCount);
			scopeLevel++;
			result.append(translateProg(node.getChildren().get(5))); //Increase scope around this, remove scoped vars afterwards
			clearScopeVars();
			appendString(result, "goto endif" + ifCount);
			appendString(result, "else" + ifCount + ":");
			result.append(translateProg(node.getChildren().get(9))); //Increase scope around this, remove scoped vars afterwards
			clearScopeVars();
			scopeLevel--;
		} else {
			appendString(result, "ifeq endif" + ifCount);
			scopeLevel++;
			result.append(translateProg(node.getChildren().get(5))); //Increase scope around this, remove scoped vars afterwards
			clearScopeVars();
			scopeLevel--;
		}
		appendString(result, "endif" + ifCount + ":");
		ifCount++;
		
		return result.toString();
	}
	
	private String translateLoop(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.LOOP));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 7);
		assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.GUARD));
		assert(node.getChildren().get(5).getAlphabet().equals(NonTerminal.PROGRAM));
		appendString(result, "while" + loopCount + ":");
		result.append(translateGuar(node.getChildren().get(2)));
		appendString(result, "ifeq done" + loopCount);
		scopeLevel++;
		result.append(translateProg(node.getChildren().get(5))); //Increase scope around this, remove scoped vars afterwards
		clearScopeVars();
		scopeLevel--;
		appendString(result, "goto while" + loopCount);
		appendString(result, "done" + loopCount + ":");
		loopCount++;
		
		return result.toString();
	}
	
	private String translateGuar(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.GUARD));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 1 || node.getChildren().size() == 3 || node.getChildren().size() == 4);
		if(node.getChildren().size() == 1) {
			assert(node.getChildren().get(0).getAlphabet().equals(NonTerminal.SUBGUARD) ||
					node.getChildren().get(0).getAlphabet().equals(NonTerminal.RELATION));
			if(node.getChildren().get(0).getAlphabet().equals(NonTerminal.SUBGUARD)) {
				result.append(translateSubg(node.getChildren().get(0)));
			} else {
				result.append(translateRela(node.getChildren().get(0)));
			}
		} else if(node.getChildren().size() == 3) {
			assert(node.getChildren().get(1).getAlphabet().equals(NonTerminal.SUBGUARD));
			result.append(translateSubg(node.getChildren().get(1)));
		} else {
			assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.GUARD));
			result.append(translateGuar(node.getChildren().get(2)));
			appendString(result, "ifeq negnull" + negCount);
			appendString(result, "ldc 1");
			appendString(result, "goto endneg" + negCount);
			appendString(result, "negnull" + negCount + ":");
			appendString(result, "ldc 0");
			appendString(result, "endneg" + negCount + ":");
			negCount++;
		}
		
		return result.toString();
	}
	
	private String translateSube(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.SUBEXPR));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 3);
		assert(node.getChildren().get(1).getAlphabet().equals(Token.PLUS) ||
				node.getChildren().get(1).getAlphabet().equals(Token.MINUS) ||
				node.getChildren().get(1).getAlphabet().equals(Token.TIMES) ||
				node.getChildren().get(1).getAlphabet().equals(Token.DIV));
		assert(node.getChildren().get(0).getAlphabet().equals(NonTerminal.EXPR));
		assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.EXPR));
		result.append(translateExpr(node.getChildren().get(0)));
		result.append(translateExpr(node.getChildren().get(2)));
		if(node.getChildren().get(1).getAlphabet().equals(Token.PLUS)){
			appendString(result, "iadd");
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.MINUS)) {
			appendString(result, "isub");
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.TIMES)) {
			appendString(result, "imul");
		} else {
			appendString(result, "idiv");
		}
		
		return result.toString();
	}
	
	private String translateSubg(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.SUBGUARD));
		StringBuilder result = new StringBuilder();

		assert(node.getChildren().size() == 3);
		assert(node.getChildren().get(1).getAlphabet().equals(Token.AND) ||
				node.getChildren().get(1).getAlphabet().equals(Token.OR));
		assert(node.getChildren().get(0).getAlphabet().equals(NonTerminal.GUARD));
		assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.GUARD));
		result.append(translateGuar(node.getChildren().get(0)));
		result.append(translateGuar(node.getChildren().get(2)));
		if(node.getChildren().get(1).getAlphabet().equals(Token.AND)) {
			appendString(result, "iand");
		} else {
			appendString(result, "ior");
		}
		
		return result.toString();
	}
	
	private String translateRela(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.RELATION));
		StringBuilder result = new StringBuilder();
		
		assert(node.getChildren().size() == 3);
		assert(node.getChildren().get(1).getAlphabet().equals(Token.LT) ||
				node.getChildren().get(1).getAlphabet().equals(Token.LEQ) ||
				node.getChildren().get(1).getAlphabet().equals(Token.EQ) ||
				node.getChildren().get(1).getAlphabet().equals(Token.GT) ||
				node.getChildren().get(1).getAlphabet().equals(Token.GEQ) ||
				node.getChildren().get(1).getAlphabet().equals(Token.NEQ));
		assert(node.getChildren().get(0).getAlphabet().equals(NonTerminal.EXPR));
		assert(node.getChildren().get(2).getAlphabet().equals(NonTerminal.EXPR));
		result.append(translateExpr(node.getChildren().get(0)));
		result.append(translateExpr(node.getChildren().get(2)));
		if(node.getChildren().get(1).getAlphabet().equals(Token.LT)){
			appendString(result, "if_icmplt reltrue" + relCount);
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.LEQ)) {
			appendString(result, "if_icmple reltrue" + relCount);
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.EQ)) {
			appendString(result, "if_icmpeq reltrue" + relCount);
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.GT)) {
			appendString(result, "if_icmpgt reltrue" + relCount);
		} else if(node.getChildren().get(1).getAlphabet().equals(Token.GEQ)) {
			appendString(result, "if_icmpge reltrue" + relCount);
		} else {
			appendString(result, "if_icmpne reltrue" + relCount);
		}
		appendString(result, "ldc 0");
		appendString(result, "goto endrel" + relCount);
		appendString(result, "reltrue" + relCount + ":");
		appendString(result, "ldc 1");
		appendString(result, "endrel" + relCount + ":");
		relCount++;
		
		return result.toString();
	}
	

	/**
	 * Generate Jasmin code for an expression.
	 * 
	 * @param node
	 *            The node in the AST.
	 * @return Jasmin code as string.
	 * @throws GeneratorException
	 *             Exception while generating.
	 */
	private String translateExpr(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.EXPR));
		StringBuilder result = new StringBuilder();

		assert(node.getChildren().size() == 1 || node.getChildren().size() == 3);
		if(node.getChildren().size() == 3) {
			assert(node.getChildren().get(1).getAlphabet().equals(NonTerminal.SUBEXPR));
			result.append(translateSube(node.getChildren().get(1)));
		} else {
			assert(node.getChildren().get(0).getAlphabet().equals(NonTerminal.SUBEXPR) ||
					node.getChildren().get(0).getAlphabet().equals(Token.NUMBER) ||
					node.getChildren().get(0).getAlphabet().equals(Token.ID));
			if(node.getChildren().get(0).getAlphabet().equals(NonTerminal.SUBEXPR)) {
				result.append(translateSube(node.getChildren().get(0)));
			} else if(node.getChildren().get(0).getAlphabet().equals(Token.NUMBER)) {
				appendString(result, "ldc " + node.getChildren().get(0).getAttribute());
			} else {
				List<Pair<Integer, Integer>> var = symbolTable.get(node.getChildren().get(0).getAttribute());
				appendString(result, "iload " + var.get(var.size() - 1).getSecond());
			}
		}

		return result.toString();
	}

	/**
	 * Generate Jasmin code for reading an int from the console.
	 * 
	 * @return Jasmin code as string.
	 */
	private String translateReadInt() {
		StringBuilder result = new StringBuilder();
		appendString(result, "; int n = Integer.parseInt(System.console().readLine());");
		appendString(result, "; Console c = System.console();");
		appendString(result, "invokestatic java/lang/System/console()Ljava/io/Console;");
		appendString(result, "; Reads one line and stores in a String");
		appendString(result, "invokevirtual java/io/Console/readLine()Ljava/lang/String;");
		appendString(result, "; Parse String to int, do not handle exceptions");
		appendString(result, "invokestatic java/lang/Integer/parseInt(Ljava/lang/String;)I");
		return result.toString();
	}

	/**
	 * Generate Jasmin code for writing a string on the console.
	 * 
	 * @param node
	 *            The node in the AST
	 * @return Jasmin code as string.
	 * @throws GeneratorException
	 *             Exception while generating.
	 */
	private String translateWrite(ASTNode node) throws GeneratorException {
		assert (node.getAlphabet().equals(NonTerminal.OUT));
		StringBuilder result = new StringBuilder();

		assert (node.getChildren().size() == 4);
		ASTNode child = node.getChildren().get(2);
		if (child.getAlphabet() instanceof Token) {
			// out -> WRITE LBRAC STRING RBRAC
			assert (child.getAlphabet().equals(Token.STRING));
			// push PrintStream object
			appendString(result, "getstatic java/lang/System/out Ljava/io/PrintStream;");
			// push String
			// the extra quotes are already part of the string
			appendString(result, "ldc " + child.getAttribute());
			// print to command line
			appendString(result, "invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V");
		} else {
			// out -> WRITE LBRAC expr RBRAC
			assert (child.getAlphabet().equals(NonTerminal.EXPR));
			// evaluate expression

			result.append(translateExpr(child));

			// the result is now on the top of the operand stack
			// cast it to string and write to console
			appendString(result, "invokestatic java/lang/String/valueOf(I)Ljava/lang/String;");
			appendString(result, "; begin syso");
			appendString(result, "astore 0 	; store string object in register 0");
			appendString(result, "getstatic java/lang/System/out Ljava/io/PrintStream;");
			appendString(result, "aload 0   ; load the string");
			appendString(result, "invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V");
			appendString(result, "; end syso");
		}

		return result.toString();
	}

	/**
	 * Append string with newline at the end.
	 * 
	 * @param builder
	 *            Stringbuilder
	 * @param s
	 *            String to append
	 */
	private void appendString(StringBuilder builder, String s) {
		builder.append(s + System.lineSeparator());
	}
	
	/**
	 * Removes all variables of the current scope level from the symbol table.
	 * Also removes all empty variable lists.
	 */
	private void clearScopeVars() {
		for(List<Pair<Integer, Integer>> values : symbolTable.values()) {
			if(values.size() > 0 && values.get(values.size() - 1).getFirst() == scopeLevel) {
				values.remove(values.size() - 1);
				varCount--;
			}
		}
		for(Iterator<Map.Entry<String, List<Pair<Integer, Integer>>>> it = symbolTable.entrySet().iterator(); it.hasNext(); ) {
			Map.Entry<String, List<Pair<Integer, Integer>>> entry = it.next();
		    if(entry.getValue().size() == 0) {
		    	it.remove();
		    }
		}
	}
}
