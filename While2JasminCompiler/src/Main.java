import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import checker.DeclarationChecker;
import lexer.*;
import parser.*;
import parser.grammar.*;

/**
 * Main class for running the compiler.
 */
public class Main {

	/**
	 * Starting method.
	 * 
	 * @param args
	 *            Arguments which should contain the path to the text file to
	 *            compile.
	 */
	public static void main(String[] args) {
		// If args is not a path to a text file, show help.
		// Otherwise open the file

		String inputProgram = "";

		if (args.length != 1) {
			showHelp();
			System.exit(0);
		} else {
			try {
				inputProgram = file2String(args[0]);
			} catch (IOException e) {
				e.printStackTrace();
				showHelp();
				System.exit(0);
			}
		}

		// Append symbol for EOF
		inputProgram += "$";

		// Lexical Analysis

		List<Symbol> symbols = null;
		try {
			symbols = LexerGenerator.analyse(inputProgram);
			System.out.println("Symbol stream: " + symbols);
		} catch (LexerException e) {
			System.out.println("LexErr");
			System.out.println(e.getMessage());
			System.out.println(e.getAnalysisBeforeFailure());
		}

		// Syntactical Analysis
		AbstractGrammar grammar = WhileGrammar.getInstance();
		SLR1Parser parser = new SLR1Parser(grammar);
		List<Rule> analysis = null;
		try {
			analysis = parser.parse(symbols);
			System.out.println(analysis);
		} catch (ParserException e) {
			System.out.println("ParseErr");
			System.out.println(e.getMessage());
			System.out.println(e.getAnalysisBeforeFailure());
		}

		// Semantical Analysis
		DeclarationChecker checker = new DeclarationChecker(symbols, analysis);
		// System.out.println(checker.getAst().ast2dot());
		System.out.println("Every variable was declared before use: " + checker.checkDeclaredBeforeUsed());

		// Byte Code Generation

	}

	/**
	 * Show help.
	 */
	public static void showHelp() {
		System.out.println("Usage: java Main PATH_TO_SOURCE_FILE");
	}

	/**
	 * Convert file to string.
	 * 
	 * @param filename
	 *            File
	 * @return String with content of file.
	 * @throws IOException
	 *             FileNotFoundExeption when the file does not exist or cannot
	 *             be read.
	 */
	public static String file2String(String filename) throws IOException {
		// Try to open file
		FileReader in = new FileReader(filename);

		// Read file into string
		StringBuilder str = new StringBuilder();
		int countBytes = 0;
		char[] bytesRead = new char[512];
		while ((countBytes = in.read(bytesRead)) > 0) {
			str.append(bytesRead, 0, countBytes);
		}

		// Close stream
		in.close();
		return str.toString();
	}

}
