package com.verint.textanalytics.dal.darwin.vtasyntax.errors;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by EZlotnik on 2/28/2016.
 */
public class UnderlineErrorListener extends BaseErrorListener {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String newline = "\n";

	@Getter
	private List<VTASyntaxRecognitionError> errors;

	@Setter
	private boolean reportAmbiquityErrors;

	@Setter
	private boolean reportFullContextErrors;

	@Setter
	private boolean reportContextSensitivityErrors;

	/**
	 * Invoked when parser recognizes error.
	 * @param recognizer  recognizer
	 * @param offendingSymbol offendingSymbol
	 * @param line line
	 * @param charPositionInLine charPositionInLine
	 * @param msg message
	 * @param e error
	 */
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
		StringBuilder errorMessage = new StringBuilder();

		errorMessage.append("line " + line + ":" + charPositionInLine + " " + msg);
		errorMessage.append(this.getUnderlineError(recognizer, (org.antlr.v4.runtime.Token) offendingSymbol, line, charPositionInLine));

		this.ensureErrors();

		this.errors.add(new VTASyntaxRecognitionError(AntlrErrorType.SyntaxError,
		                                              this.getUnderlineError(recognizer, (org.antlr.v4.runtime.Token) offendingSymbol, line, charPositionInLine),
		                                              line, charPositionInLine, e));

		logger.warn("VTA Syntax recongnitino error {}", errorMessage.toString());

		throw new VTASyntaxRecognitionException(AntlrErrorType.ParsingError, errorMessage.toString(), this.errors);
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact, BitSet ambigAlts, ATNConfigSet configs) {

		super.reportAmbiguity(recognizer, dfa, startIndex, stopIndex, exact, ambigAlts, configs);

		if (reportAmbiquityErrors) {

			this.ensureErrors();

			this.errors.add(
					new VTASyntaxRecognitionError(AntlrErrorType.AmbiguityError, String.format("Ambiguity error between %s and %s positions", startIndex, stopIndex), startIndex, stopIndex,
					                              null));

			logger.warn("VTA Syntax ambiguity error found during VTA Syntax Query processing at start index  {}, stop index {}", startIndex, stopIndex);
		}
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex, BitSet conflictingAlts, ATNConfigSet configs) {

		super.reportAttemptingFullContext(recognizer, dfa, startIndex, stopIndex, conflictingAlts, configs);

		if (reportFullContextErrors) {
			this.ensureErrors();

			this.errors.add(new VTASyntaxRecognitionError(AntlrErrorType.AttemptingFullContextError,
			                                              String.format("Attempting Full ContextError error between %s and %s positions", startIndex, stopIndex), startIndex, stopIndex,
			                                              null));

			logger.warn("VTA Syntax Attempting Full ContextError error found during Term Query processing at start index  {}, stop index {}", startIndex, stopIndex);
		}
	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction, ATNConfigSet configs) {

		super.reportContextSensitivity(recognizer, dfa, startIndex, stopIndex, prediction, configs);

		if (reportFullContextErrors) {
			this.ensureErrors();

			this.errors.add(new VTASyntaxRecognitionError(AntlrErrorType.ContextSensitivityError,
			                                              String.format("Context Sensitivity Full ContextError error between %s and %s positions", startIndex, stopIndex), startIndex,
			                                              stopIndex, null));

			logger.warn("VTA Syntax Context Sensitivity error found during query processing at start index  {}, stop index {}", startIndex, stopIndex);
		}
	}


	protected String getUnderlineError(Recognizer recognizer, org.antlr.v4.runtime.Token offendingToken, int line, int charPositionInLine) {
		StringBuilder error = new StringBuilder();

		CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
		String input = tokens.getTokenSource().getInputStream().toString();
		String[] lines = input.split("\n");
		String errorLine = lines[line - 1];
		error.append(errorLine);

		for (int i = 0; i < charPositionInLine; i++) {
			error.append(" ");
		}

		int start = offendingToken.getStartIndex();
		int stop = offendingToken.getStopIndex();

		if (start >= 0 && stop >= 0) {
			for (int i = start; i <= stop; i++)
				error.append("^");
		}

		error.append(newline);
		return error.toString();
	}

	private void ensureErrors() {
		if (this.errors == null) {
			this.errors = new ArrayList<>();
		}
	}
}
