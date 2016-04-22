package lexer;

import java.util.HashMap;
import java.util.HashSet;

import helper.Pair;
import lexer.LexerGenerator.Token;

/**
 * DFA recognizing a given word.
 */
public class DFA extends AbstractDFA {

	/**
	 * Construct a new DFA that recognizes exactly the given word. Given a word
	 * "foo" the constructed automaton looks like: -> () -f-> () -o-> () -o-> []
	 * from every state (including the final one) every other input letter leads
	 * to a distinguished sink state in which the automaton then remains
	 * 
	 * @param word
	 *            A String that the automaton should recognize
	 * @param token
	 *            The token corresponding to the recognized word.
	 */
	public DFA(String word, Token token) {
		assert (word.length() > 0);

		this.token = token;

		// TODO: build DFA recognizing the given word
		this.maxState = word.length();
		this.finalStates = new HashSet<Integer>();
		this.finalStates.add(Integer.valueOf(this.maxState));
		this.isStateProductive = new HashMap<Integer, Boolean>();
		this.delta = new HashMap<Pair<Integer, Character>, Integer>();
		for(int i = 0; i < word.length(); i++) {
			delta.put(new Pair<Integer, Character>(Integer.valueOf(i), Character.valueOf(word.charAt(i))), Integer.valueOf(i + 1));
		}
		this.reset();
	}
}
