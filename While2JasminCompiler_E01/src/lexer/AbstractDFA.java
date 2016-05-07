package lexer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import helper.Pair;
import lexer.LexerGenerator.Token;

/**
 * Abstract class for Deterministic Finite Automata.
 */
public abstract class AbstractDFA {

	protected Token token; // Token that is recognized by this automaton

	// TODO: use data structure for representing
	// - states
	// - final states (and sink states)
	// - transitions of the form (state, input) -> state
	// - current state
	
	int maxState; // 0 is start state, Integer.MAX_VALUE is the sink state, sink state is not included in maxState
	HashSet<Integer> finalStates;
	HashMap<Integer, Boolean> isStateProductive;
	HashMap<Pair<Integer, Character>, Integer> delta; //non-existant transistions go implicitly to Integer.MAX_VALUE, the sink state
	int currentState;

	/**
	 * Reset the automaton to the initial state.
	 */
	public void reset() {
		// TODO: reset automaton to initial state
		currentState = 0;
	}

	/**
	 * Performs one step of the DFA for a given letter. If there is a transition
	 * for the given letter, then the automaton proceeds to the successor state.
	 * Otherwise it goes to the sink state. By construction it will stay in the
	 * sink for every input letter.
	 * 
	 * @param letter
	 *            The current input.
	 */
	public void doStep(char letter) {
		// TODO: do step by going to the next state according to the current
		// state and the read letter.
		
		 //non-existant transistions go implicitly to Integer.MAX_VALUE, the sink state
		Integer goal = delta.get(new Pair<Integer, Character>(Integer.valueOf(currentState), Character.valueOf(letter)));
		currentState = goal == null ? Integer.MAX_VALUE : goal.intValue();
	}

	/**
	 * Check if the automaton is currently accepting.
	 * 
	 * @return True, if the automaton is currently in the accepting state.
	 */
	public boolean isAccepting() {
		// TODO: return if the current state is accepting
		return finalStates.contains(Integer.valueOf(currentState));
	}

	/**
	 * Run the DFA on the input.
	 * 
	 * @param inputWord
	 *            String that contains the input word
	 * @return True, if if the word is accepted by this automaton
	 */
	public boolean run(String inputWord) {
		this.reset();
		char[] inputCharWord = inputWord.toCharArray();
		for (char letter : inputCharWord) {
			doStep(letter);
		}
		return isAccepting();
	}

	/**
	 * Checks if the final state can be reached from the current state.
	 * 
	 * @return True, if the state is productive, i.e. the final state can be
	 *         reached.
	 */
	public boolean isProductive() {
		// TODO: return if the current state is productive
		

		//We cache which state were productive in isStateProductive to be efficient
		Boolean productive = isStateProductive.get(Integer.valueOf(currentState));
		if(productive != null)
			return productive.booleanValue();
		
		if(currentState == Integer.MAX_VALUE) {
			isStateProductive.put(Integer.MAX_VALUE, Boolean.FALSE);
			return false;
		}
		
		if(finalStates.contains(Integer.valueOf(currentState))) {
			isStateProductive.put(Integer.valueOf(currentState), Boolean.TRUE);
			return true;
		}
		
		//BreadthFirstSearch to find a final state, stops when no change in visited states is detected
		HashSet<Integer> visited = new HashSet<Integer>();
		visited.add(Integer.valueOf(currentState));
		boolean addedState = true, foundFinal = false;
		while(addedState && !foundFinal) {
			for(Entry<Pair<Integer, Character>, Integer> entry : delta.entrySet()) {
				if(visited.contains(entry.getKey().getFirst())) {
					if(visited.add(entry.getValue()))
						addedState = true;
					if(finalStates.contains(entry.getValue())) {
						foundFinal = true;
						break;
					}
				}
			}
		}

		if(foundFinal) {
			isStateProductive.put(Integer.valueOf(currentState), Boolean.TRUE);
			return true;
		} else {
			for(Integer state : visited) {
				isStateProductive.put(state, Boolean.FALSE);
			}
			return false;
		}
	}

	/**
	 * @return The Token that this automaton recognizes
	 */
	public Token getToken() {
		return token;
	}
}
