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
import symbols.Tokens.Epsilon;

/**
 * SLR(1) parser.
 */
public class SLR1Parser {

	// Generator for LR(0) sets
	private LR0SetGenerator generatorLR0;

	// Generator for first and follow sets
	private LookAheadGenerator generatorLookAhead;

	// Start symbol of grammar
	private NonTerminal start;

	/**
	 * Constructor.
	 * 
	 * @param grammar
	 *            Grammar.
	 */
	public SLR1Parser(AbstractGrammar grammar) {
		this.generatorLR0 = new LR0SetGenerator(grammar);
		this.generatorLookAhead = new LookAheadGenerator(grammar);
		this.start = grammar.getStart();
	}

	/**
	 * Parse the input via SLR(1) parsing.
	 * 
	 * @param lexOutput
	 *            List of symbols
	 * @return A list of rules which corresponds to the right-most analysis
	 * @throws ParserException
	 *             Parser exception
	 */
	public List<Rule> parse(List<Symbol> lexOutput) throws ParserException {
		List<Rule> analysis = new LinkedList<Rule>();

		// TODO implement SLR(1) parser
		
		boolean accept = false;
		List<Symbol> input = lexOutput;
		Stack<LR0Set> stack = new Stack<LR0Set>();
		stack.push(generatorLR0.getInitialState());
		
		while(!accept) {
			Alphabet first = !input.isEmpty() ? input.get(0).getToken() : Epsilon.EPS;
			if(stack.peek().containsFinalItem(start) && first == Epsilon.EPS) { // act = accept
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
				if(generatorLookAhead.containsFollow(rule.getLhs(), first)) {
					analysis.add(rule);
					for(int i = 0; i < rule.getRhs().length; i++) {
						stack.pop();
					}
					stack.push(generatorLR0.getSuccessor(stack.peek(), rule.getLhs()));
					continue;
				}
			}
			if(first == Epsilon.EPS)
				throw new ParserException("Unexpected end of input.", analysis);
			LR0Set succ = generatorLR0.getSuccessor(stack.peek(), first);
			if(succ != null) { // act = shift
				input.remove(0);
				stack.push(succ);
			} else { // act = error
				throw new ParserException("Could neither accept, reduce nor shift.", analysis);
			}
		}

		return analysis;
	}
}
