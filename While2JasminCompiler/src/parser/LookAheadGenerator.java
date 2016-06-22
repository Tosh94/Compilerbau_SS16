package parser;

import java.util.Iterator;
import java.util.List;

import parser.grammar.AbstractGrammar;
import symbols.Alphabet;
import symbols.NonTerminals.NonTerminal;
import symbols.Tokens.Epsilon;
import symbols.Tokens.Token;
import util.MapSet;

/**
 * Generator for first and follow sets for a given grammar.
 */
public class LookAheadGenerator {

	// Grammar.
	private AbstractGrammar grammar;

	// First sets for each non-terminal
	private MapSet<NonTerminal, Alphabet> first;

	// Follow sets for each non-terminal
	private MapSet<NonTerminal, Alphabet> follow;

	/**
	 * Constructor.
	 * 
	 * @param grammar
	 *            Grammar.
	 */
	public LookAheadGenerator(AbstractGrammar grammar) {
		this.grammar = grammar;
		computeFirst();
		computeFollow();
	}

	/**
	 * Compute the first set for each non-terminal.
	 */
	public void computeFirst() {
		first = new MapSet<NonTerminal, Alphabet>();
		// TODO implement
		boolean change = true;
		while(change) {
			change = false;
			for(Rule r : grammar.getRules()){
				int i = 0;
				while(i < r.getRhs().length) {
					Alphabet a = r.getRhs()[i];
					if(a instanceof NonTerminal){
						boolean containsEps = false;
						for(Alphabet b : first.get((NonTerminal) a)) {
							if(!(b instanceof Epsilon)) {
								change = change || first.get(r.getLhs()).add(b);
							} else {
								containsEps = true;
							}
						}
						if(!containsEps)
							break;
					} else if(a instanceof Token) {
						change = change || first.get(r.getLhs()).add(a);
						break;
					}
					i++;
				}
				if(i == r.getRhs().length) {
					change = change || first.get(r.getLhs()).add(Epsilon.EPS);
				}
			}
		}
	}

	/**
	 * Compute the follow set for each non-terminal. Assume that the first sets
	 * were computed beforehand.
	 */
	public void computeFollow() {
		assert (first != null);

		follow = new MapSet<NonTerminal, Alphabet>();
		// TODO implement

		follow.get(grammar.getStart()).add(Epsilon.EPS);
		boolean change = true;
		while(change) {
			change = false;
			for(Rule r : grammar.getRules()){
				for(int i = 0; i < r.getRhs().length - 1; i++) {
					Alphabet a = r.getRhs()[i];
					Alphabet b = r.getRhs()[i + 1];
					if(a instanceof NonTerminal) {
						if(b instanceof NonTerminal) {
							change = change || follow.get((NonTerminal) a).addAll(first.get((NonTerminal) b));
						} else {
							change = change || follow.get((NonTerminal) a).add(b);
						}
					}
				}
				Alphabet a = r.getRhs()[r.getRhs().length - 1];
				if(a instanceof NonTerminal) {
					change = change || follow.get((NonTerminal) a).addAll(follow.get(r.getLhs()));
				}
			}
		}
	}

	/**
	 * Check if the first set for the given non-terminal contains the given
	 * symbol.
	 * 
	 * @param nonTerminal
	 *            Non-terminal
	 * @param symbol
	 *            Symbol
	 * @return True iff the follow set contains the symbol
	 */
	public boolean containsFirst(NonTerminal nonTerminal, Alphabet symbol) {
		return first.contains(nonTerminal, symbol);
	}

	/**
	 * Check if the follow set for the given non-terminal contains the given
	 * symbol.
	 * 
	 * @param nonTerminal
	 *            Non-terminal
	 * @param symbol
	 *            Symbol
	 * @return True iff the follow set contains the symbol
	 */
	public boolean containsFollow(NonTerminal nonTerminal, Alphabet symbol) {
		return follow.contains(nonTerminal, symbol);
	}

	/**
	 * Print first sets.
	 */
	public void printFirstSets() {
		for (NonTerminal nonTerminal : first.keySet()) {
			System.out.print("fi(" + nonTerminal + "): {");
			Iterator<Alphabet> iter = first.get(nonTerminal).iterator();
			while (iter.hasNext()) {
				System.out.print(iter.next());
				if (iter.hasNext()) {
					System.out.print(", ");
				}
			}
			System.out.println("}");
		}
	}

	/**
	 * Print follow sets.
	 */
	public void printFollowSets() {
		for (NonTerminal nonTerminal : follow.keySet()) {
			System.out.print("fo(" + nonTerminal + "): {");
			Iterator<Alphabet> iter = follow.get(nonTerminal).iterator();
			while (iter.hasNext()) {
				System.out.print(iter.next());
				if (iter.hasNext()) {
					System.out.print(", ");
				}
			}
			System.out.println("}");
		}
	}
}
