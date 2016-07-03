package parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import util.Pair;
import lexer.Symbol;
import parser.grammar.AbstractGrammar;
import symbols.Alphabet;
import symbols.NonTerminals.NonTerminal;
import symbols.Tokens.Token;

/**
 * LR(0) parser.
 */
public class LR0Parser {

	// Generator for LR(0) sets
	private LR0SetGenerator generatorLR0;

	// Start symbol of grammar
	private NonTerminal start;

	/**
	 * Constructor.
	 * 
	 * @param grammar
	 *            Grammar.
	 */
	public LR0Parser(AbstractGrammar grammar) {
		this.generatorLR0 = new LR0SetGenerator(grammar);
		this.start = grammar.getStart();
	}

	/**
	 * Parse the input via LR(0) parsing.
	 * 
	 * @param lexOutput
	 *            List of symbols
	 * @return A list of rules which corresponds to the right-most analysis
	 * @throws ParserException
	 *             Parser exception
	 */
	public List<Rule> parse(List<Symbol> lexOutput) throws ParserException {
		List<Rule> analysis = new LinkedList<Rule>();

		// TODO implement LR(0) parser
		
		boolean accept = false;
		List<Symbol> input = lexOutput;
		Stack<LR0Set> stack = new Stack<LR0Set>();
		stack.push(generatorLR0.getInitialState());
		
		while(!accept) {
			if(stack.peek().containsFinalItem(start)) { // act = accept
				if(!input.isEmpty())
					throw new ParserException("Action is accept, but input is not empty.", analysis);
				LR0Set I = stack.pop();
				if(stack.peek().equals(generatorLR0.getInitialState())) {
					analysis.add(I.getCompleteItem());
					stack.pop();
					accept = true;
					continue;
				} else {
					throw new ParserException("Action is accept, but state stack is not as expected.", analysis);
				}
			} else if(stack.peek().containsCompleteItem()) { // act = reduce
				LR0Item rule = stack.peek().getCompleteItem();
				analysis.add(rule);
				for(int i = 0; i < rule.getRhs().length; i++) {
					stack.pop();
				}
				stack.push(generatorLR0.getSuccessor(stack.peek(), rule.getLhs()));
			} else{ 
				if(input.isEmpty())
					throw new ParserException("Unexpected end of input.", analysis);
				LR0Set succ = generatorLR0.getSuccessor(stack.peek(), input.get(0).getToken());
				if(succ != null) { // act = shift
					input.remove(0);
					stack.push(succ);
				} else { // act = error
					throw new ParserException("Could neither accept, reduce nor shift.", analysis);
				}
			}
		}

		return analysis;
	}
}
