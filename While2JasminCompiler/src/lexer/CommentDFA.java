package lexer;

import java.util.HashMap;
import java.util.HashSet;

import helper.Pair;
import lexer.LexerGenerator.Token;

/**
 * DFA recognizing comments.
 */
public class CommentDFA extends AbstractDFA {

	/**
	 * Construct a new DFA that recognizes comments within source code. There
	 * are two kinds of comments: A single line comment starts with // and ends
	 * with a newline and a multiline comment that starts with /* and ends with
	 * * / (without the space)
	 */
	public CommentDFA() {
		token = Token.COMMENT;

		// TODO: build DFA recognizing comments
		this.maxState = 7;
		this.finalStates = new HashSet<Integer>();
		this.finalStates.add(Integer.valueOf(4));
		this.finalStates.add(Integer.valueOf(6));
		this.finalStates.add(Integer.valueOf(7));
		this.isStateProductive = new HashMap<Integer, Boolean>();
		for(int i = 0; i <= this.maxState; i++)
			this.isStateProductive.put(Integer.valueOf(i), Boolean.TRUE);
		this.delta = new HashMap<Pair<Integer, Character>, Integer>();
		delta.put(new Pair<Integer, Character>(Integer.valueOf(0),  Character.valueOf('/')), Integer.valueOf(1));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(1),  Character.valueOf('*')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(2),  Character.valueOf('*')), Integer.valueOf(3));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(3),  Character.valueOf('/')), Integer.valueOf(4));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(3),  Character.valueOf('*')), Integer.valueOf(3));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(1),  Character.valueOf('/')), Integer.valueOf(5));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(5),  Character.valueOf('\r')), Integer.valueOf(6));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(6),  Character.valueOf('\n')), Integer.valueOf(7));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(5),  Character.valueOf('\n')), Integer.valueOf(7));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(5),  Character.valueOf('a')), Integer.valueOf(5));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(5),  Character.valueOf('*')), Integer.valueOf(5));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(5),  Character.valueOf('/')), Integer.valueOf(5));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(2),  Character.valueOf('a')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(2),  Character.valueOf('\r')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(2),  Character.valueOf('\n')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(2),  Character.valueOf('/')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(3),  Character.valueOf('a')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(3),  Character.valueOf('\r')), Integer.valueOf(2));
		delta.put(new Pair<Integer, Character>(Integer.valueOf(3),  Character.valueOf('\n')), Integer.valueOf(2));
		this.reset();
	}

	/**
	 * Performs one step of the DFA for a given letter. This method works
	 * differently than in the superclass AbstractDFA.
	 * 
	 * @param letter
	 *            The current input.
	 */
	@Override
	public void doStep(char letter) {
		// TODO: implement accordingly
		if(letter == '/' || letter == '*' || letter == '\r' || letter == '\n')
			super.doStep(letter);
		else
			super.doStep('a');
	}

	/**
	 * Check if the automaton is currently accepting.
	 * 
	 * @return True, if the automaton is currently in the accepting state.
	 */
	@Override
	public boolean isAccepting() {
		// TODO: implement accordingly
		return super.isAccepting();
	}
}
