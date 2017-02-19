package com.verint.textanalytics.dal.darwin.vtasyntax;// Generated from VTASyntax.g4 by ANTLR 4.5.2
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VTASyntaxLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, DQUOTE=4, AGENT_IDENT=5, CUSTOMER_IDENT=6, INT=7, 
		NOT=8, AND=9, OR=10, NEAR=11, NOTIN=12, WORD=13, WS=14;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
		"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", 
		"Y", "Z", "VBAR", "AMPER", "SPACE", "DIGIT", "DQUOTE", "AGENT_IDENT", 
		"CUSTOMER_IDENT", "INT", "NOT", "AND", "OR", "NEAR", "NOTIN", "WORD", 
		"WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "':'", null, null, null, null, "'NOT'", "'AND'", "'OR'", 
		"'NEAR'", "'NOTIN'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, "DQUOTE", "AGENT_IDENT", "CUSTOMER_IDENT", "INT", 
		"NOT", "AND", "OR", "NEAR", "NOTIN", "WORD", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public VTASyntaxLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "VTASyntax.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\20\u00e2\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3"+
		"\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3"+
		"\27\3\30\3\30\3\31\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3"+
		"\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\3#\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$"+
		"\3$\3$\3$\3$\3$\5$\u00af\n$\3%\3%\3%\3%\3%\3%\3%\3%\3%\5%\u00ba\n%\3&"+
		"\6&\u00bd\n&\r&\16&\u00be\3\'\3\'\3\'\3\'\3(\3(\3(\3(\3)\3)\3)\3*\3*\3"+
		"*\3*\3*\3+\3+\3+\3+\3+\3+\3,\6,\u00d8\n,\r,\16,\u00d9\3-\6-\u00dd\n-\r"+
		"-\16-\u00de\3-\3-\2\2.\3\3\5\4\7\5\t\2\13\2\r\2\17\2\21\2\23\2\25\2\27"+
		"\2\31\2\33\2\35\2\37\2!\2#\2%\2\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67\29\2"+
		";\2=\2?\2A\2C\2E\6G\7I\bK\tM\nO\13Q\fS\rU\16W\17Y\20\3\2 \4\2CCcc\4\2"+
		"DDdd\4\2EEee\4\2FFff\4\2GGgg\4\2HHhh\4\2IIii\4\2JJjj\4\2KKkk\4\2LLll\4"+
		"\2MMmm\4\2NNnn\4\2OOoo\4\2PPpp\4\2QQqq\4\2RRrr\4\2SSss\4\2TTtt\4\2UUu"+
		"u\4\2VVvv\4\2WWww\4\2XXxx\4\2YYyy\4\2ZZzz\4\2[[{{\4\2\\\\||\5\2\13\f\16"+
		"\17\"\"\4\2\62;~~\16\2$$\u02bc\u02bc\u02df\u02df\u02f0\u02f0\u02f8\u02f8"+
		"\u05f4\u05f4\u05f6\u05f6\u1cd5\u1cd5\u201e\u201f\u2021\u2021\u2035\u2035"+
		"\uff04\uff04\31\2\13\f\16\17\"\"$$*+\61\61<<>>@@]_}}\177\177\u02bc\u02bc"+
		"\u02df\u02df\u02f0\u02f0\u02f8\u02f8\u05f4\u05f4\u05f6\u05f6\u1cd5\u1cd5"+
		"\u201e\u201f\u2021\u2021\u2035\u2035\uff04\uff04\u00ca\2\3\3\2\2\2\2\5"+
		"\3\2\2\2\2\7\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3"+
		"\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2"+
		"\2\3[\3\2\2\2\5]\3\2\2\2\7_\3\2\2\2\ta\3\2\2\2\13c\3\2\2\2\re\3\2\2\2"+
		"\17g\3\2\2\2\21i\3\2\2\2\23k\3\2\2\2\25m\3\2\2\2\27o\3\2\2\2\31q\3\2\2"+
		"\2\33s\3\2\2\2\35u\3\2\2\2\37w\3\2\2\2!y\3\2\2\2#{\3\2\2\2%}\3\2\2\2\'"+
		"\177\3\2\2\2)\u0081\3\2\2\2+\u0083\3\2\2\2-\u0085\3\2\2\2/\u0087\3\2\2"+
		"\2\61\u0089\3\2\2\2\63\u008b\3\2\2\2\65\u008d\3\2\2\2\67\u008f\3\2\2\2"+
		"9\u0091\3\2\2\2;\u0093\3\2\2\2=\u0095\3\2\2\2?\u0097\3\2\2\2A\u0099\3"+
		"\2\2\2C\u009b\3\2\2\2E\u009d\3\2\2\2G\u00ae\3\2\2\2I\u00b9\3\2\2\2K\u00bc"+
		"\3\2\2\2M\u00c0\3\2\2\2O\u00c4\3\2\2\2Q\u00c8\3\2\2\2S\u00cb\3\2\2\2U"+
		"\u00d0\3\2\2\2W\u00d7\3\2\2\2Y\u00dc\3\2\2\2[\\\7*\2\2\\\4\3\2\2\2]^\7"+
		"+\2\2^\6\3\2\2\2_`\7<\2\2`\b\3\2\2\2ab\t\2\2\2b\n\3\2\2\2cd\t\3\2\2d\f"+
		"\3\2\2\2ef\t\4\2\2f\16\3\2\2\2gh\t\5\2\2h\20\3\2\2\2ij\t\6\2\2j\22\3\2"+
		"\2\2kl\t\7\2\2l\24\3\2\2\2mn\t\b\2\2n\26\3\2\2\2op\t\t\2\2p\30\3\2\2\2"+
		"qr\t\n\2\2r\32\3\2\2\2st\t\13\2\2t\34\3\2\2\2uv\t\f\2\2v\36\3\2\2\2wx"+
		"\t\r\2\2x \3\2\2\2yz\t\16\2\2z\"\3\2\2\2{|\t\17\2\2|$\3\2\2\2}~\t\20\2"+
		"\2~&\3\2\2\2\177\u0080\t\21\2\2\u0080(\3\2\2\2\u0081\u0082\t\22\2\2\u0082"+
		"*\3\2\2\2\u0083\u0084\t\23\2\2\u0084,\3\2\2\2\u0085\u0086\t\24\2\2\u0086"+
		".\3\2\2\2\u0087\u0088\t\25\2\2\u0088\60\3\2\2\2\u0089\u008a\t\26\2\2\u008a"+
		"\62\3\2\2\2\u008b\u008c\t\27\2\2\u008c\64\3\2\2\2\u008d\u008e\t\30\2\2"+
		"\u008e\66\3\2\2\2\u008f\u0090\t\31\2\2\u00908\3\2\2\2\u0091\u0092\t\32"+
		"\2\2\u0092:\3\2\2\2\u0093\u0094\t\33\2\2\u0094<\3\2\2\2\u0095\u0096\7"+
		"~\2\2\u0096>\3\2\2\2\u0097\u0098\7(\2\2\u0098@\3\2\2\2\u0099\u009a\t\34"+
		"\2\2\u009aB\3\2\2\2\u009b\u009c\t\35\2\2\u009cD\3\2\2\2\u009d\u009e\t"+
		"\36\2\2\u009eF\3\2\2\2\u009f\u00af\7C\2\2\u00a0\u00a1\7C\2\2\u00a1\u00a2"+
		"\7I\2\2\u00a2\u00a3\7G\2\2\u00a3\u00a4\7P\2\2\u00a4\u00af\7V\2\2\u00a5"+
		"\u00af\7G\2\2\u00a6\u00a7\7G\2\2\u00a7\u00a8\7O\2\2\u00a8\u00a9\7R\2\2"+
		"\u00a9\u00aa\7N\2\2\u00aa\u00ab\7Q\2\2\u00ab\u00ac\7[\2\2\u00ac\u00ad"+
		"\7G\2\2\u00ad\u00af\7G\2\2\u00ae\u009f\3\2\2\2\u00ae\u00a0\3\2\2\2\u00ae"+
		"\u00a5\3\2\2\2\u00ae\u00a6\3\2\2\2\u00afH\3\2\2\2\u00b0\u00ba\7E\2\2\u00b1"+
		"\u00b2\7E\2\2\u00b2\u00b3\7W\2\2\u00b3\u00b4\7U\2\2\u00b4\u00b5\7V\2\2"+
		"\u00b5\u00b6\7Q\2\2\u00b6\u00b7\7O\2\2\u00b7\u00b8\7G\2\2\u00b8\u00ba"+
		"\7T\2\2\u00b9\u00b0\3\2\2\2\u00b9\u00b1\3\2\2\2\u00baJ\3\2\2\2\u00bb\u00bd"+
		"\5C\"\2\u00bc\u00bb\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bc\3\2\2\2\u00be"+
		"\u00bf\3\2\2\2\u00bfL\3\2\2\2\u00c0\u00c1\7P\2\2\u00c1\u00c2\7Q\2\2\u00c2"+
		"\u00c3\7V\2\2\u00c3N\3\2\2\2\u00c4\u00c5\7C\2\2\u00c5\u00c6\7P\2\2\u00c6"+
		"\u00c7\7F\2\2\u00c7P\3\2\2\2\u00c8\u00c9\7Q\2\2\u00c9\u00ca\7T\2\2\u00ca"+
		"R\3\2\2\2\u00cb\u00cc\7P\2\2\u00cc\u00cd\7G\2\2\u00cd\u00ce\7C\2\2\u00ce"+
		"\u00cf\7T\2\2\u00cfT\3\2\2\2\u00d0\u00d1\7P\2\2\u00d1\u00d2\7Q\2\2\u00d2"+
		"\u00d3\7V\2\2\u00d3\u00d4\7K\2\2\u00d4\u00d5\7P\2\2\u00d5V\3\2\2\2\u00d6"+
		"\u00d8\n\37\2\2\u00d7\u00d6\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00d7\3"+
		"\2\2\2\u00d9\u00da\3\2\2\2\u00daX\3\2\2\2\u00db\u00dd\5A!\2\u00dc\u00db"+
		"\3\2\2\2\u00dd\u00de\3\2\2\2\u00de\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df"+
		"\u00e0\3\2\2\2\u00e0\u00e1\b-\2\2\u00e1Z\3\2\2\2\b\2\u00ae\u00b9\u00be"+
		"\u00d9\u00de\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}