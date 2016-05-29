package parser;

import lexer.LexerGenerator.Token;

/**
 * Recursive descent parser for assignment of the form: int id = num;
 * 
 * Grammar:
 *  1: start      -> assignment SEMICOLON EOF
 *  2: assignment -> INT ID ASSIGN expr
 *  3: expr       -> ID subexpr
 *  4: expr       -> NUMBER subexpr
 *  5: expr       -> LPAR expr RPAR
 *  6: expr       -> READ LPAR RPAR subexpr
 *  7: subexpr    -> PLUS expr
 *  8: subexpr    -> MINUS expr
 *  9: subexpr    -> TIMES expr
 * 10: subexpr    -> DIV expr
 * 11: subexpr    -> MOD expr
 * 12: subexpr    -> eps
 */
public class RecursiveDescentParserAssignment extends RecursiveDescentParser {

	/**
	 * Starting symbol of the grammar.
	 * 
	 * @throws ParserException
	 *             Exception from the parser.
	 */
	protected void main() throws ParserException {
       
         // TODO implement
		next();
		start();
		
        if (!symbols.isEmpty()) {
			printError("Symbols remaining.");
		}
	}
	
	private void start() throws ParserException {
		if(token == Token.INT) {
			print(1);
			assignment();
			if(token == Token.SEMICOLON) {
				next();
				if(token == Token.EOF) {
					return;
				} else {
					printError("Incorrect token: " + token + ", expected " + Token.EOF);
				}
			} else {
				printError("Incorrect token: " + token + ", expected " + Token.SEMICOLON);
			}
		} else {
			printError("Token "+token+" not in any lookahead set");
		}
	}
	
	private void assignment() throws ParserException {
		if(token == Token.INT) {
			print(2);
			next();
			if(token == Token.ID) {
				next();
				if(token == Token.ASSIGN) {
					next();
					expr();
				} else {
					printError("Incorrect token: " + token + ", expected " + Token.ASSIGN);
				}
			} else {
				printError("Incorrect token: " + token + ", expected " + Token.ID);
			}
		} else {
			printError("Token "+token+" not in any lookahead set");
		}
	}
	
	private void expr() throws ParserException {
		switch(token){
		case ID:
			print(3);
			next();
			subexpr();
			break;
		case NUMBER:
			print(4);
			next();
			subexpr();
			break;
		case LPAR:
			print(5);
			next();
			expr();
			if(token == Token.RPAR) {
				next();
			} else {
				printError("Incorrect token: " + token + ", expected " + Token.RPAR);
			}
			break;
		case READ:
			print(6);
			next();
			if(token == Token.LPAR) {
				next();
				if(token == Token.RPAR) {
					next();
					subexpr();
				} else {
					printError("Incorrect token: " + token + ", expected " + Token.RPAR);
				}
			} else {
				printError("Incorrect token: " + token + ", expected " + Token.LPAR);
			}
			break;
		default:	
			printError("Token "+token+" not in any lookahead set");	
		}
	}
	
	private void subexpr() throws ParserException {
		switch(token) {
		case PLUS:
			print(7);
			next();
			expr();
			break;
		case MINUS:
			print(8);
			next();
			expr();
			break;
		case TIMES:
			print(9);
			next();
			expr();
			break;
		case DIV:
			print(10);
			next();
			expr();
			break;
		case MOD:
			print(11);
			next();
			expr();
			break;
		case SEMICOLON:
		case RPAR:
			print(12);
			break;
		default:
			printError("Token "+token+" not in any lookahead set");	
		}
	}
}
