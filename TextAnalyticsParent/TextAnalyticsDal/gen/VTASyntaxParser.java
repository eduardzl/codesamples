// Generated from C:/GIT/TextAnalytics_WebApplication/Eclipse/TextAnalyticsParent/TextAnalyticsDal/src/main/resources\VTASyntax.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VTASyntaxParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, DQUOTE=4, AGENT_IDENT=5, CUSTOMER_IDENT=6, INT=7, 
		NOT=8, AND=9, OR=10, NEAR=11, NOTIN=12, WORD=13, WS=14;
	public static final int
		RULE_vtaexpr = 0, RULE_exprOr = 1, RULE_exprAnd = 2, RULE_exprBasic = 3, 
		RULE_exprNear = 4, RULE_termsNoSPSNear = 5, RULE_termsAgentNear = 6, RULE_termsCustomerNear = 7, 
		RULE_term = 8, RULE_customer_phrase = 9, RULE_agent_phrase = 10, RULE_customer_word = 11, 
		RULE_agent_word = 12, RULE_phrase = 13, RULE_word = 14, RULE_not = 15, 
		RULE_and = 16, RULE_or = 17, RULE_near = 18;
	public static final String[] ruleNames = {
		"vtaexpr", "exprOr", "exprAnd", "exprBasic", "exprNear", "termsNoSPSNear", 
		"termsAgentNear", "termsCustomerNear", "term", "customer_phrase", "agent_phrase", 
		"customer_word", "agent_word", "phrase", "word", "not", "and", "or", "near"
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

	@Override
	public String getGrammarFileName() { return "VTASyntax.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public VTASyntaxParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class VtaexprContext extends ParserRuleContext {
		public ExprOrContext exprOr() {
			return getRuleContext(ExprOrContext.class,0);
		}
		public VtaexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vtaexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterVtaexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitVtaexpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitVtaexpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VtaexprContext vtaexpr() throws RecognitionException {
		VtaexprContext _localctx = new VtaexprContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_vtaexpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(38);
			exprOr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprOrContext extends ParserRuleContext {
		public List<ExprAndContext> exprAnd() {
			return getRuleContexts(ExprAndContext.class);
		}
		public ExprAndContext exprAnd(int i) {
			return getRuleContext(ExprAndContext.class,i);
		}
		public List<OrContext> or() {
			return getRuleContexts(OrContext.class);
		}
		public OrContext or(int i) {
			return getRuleContext(OrContext.class,i);
		}
		public ExprOrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprOr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprOrContext exprOr() throws RecognitionException {
		ExprOrContext _localctx = new ExprOrContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_exprOr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(40);
			exprAnd();
			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << DQUOTE) | (1L << AGENT_IDENT) | (1L << CUSTOMER_IDENT) | (1L << INT) | (1L << NOT) | (1L << OR) | (1L << WORD))) != 0)) {
				{
				{
				setState(42);
				_la = _input.LA(1);
				if (_la==OR) {
					{
					setState(41);
					or();
					}
				}

				setState(44);
				exprAnd();
				}
				}
				setState(49);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprAndContext extends ParserRuleContext {
		public List<ExprBasicContext> exprBasic() {
			return getRuleContexts(ExprBasicContext.class);
		}
		public ExprBasicContext exprBasic(int i) {
			return getRuleContext(ExprBasicContext.class,i);
		}
		public List<AndContext> and() {
			return getRuleContexts(AndContext.class);
		}
		public AndContext and(int i) {
			return getRuleContext(AndContext.class,i);
		}
		public ExprAndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprAnd; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprAndContext exprAnd() throws RecognitionException {
		ExprAndContext _localctx = new ExprAndContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_exprAnd);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			exprBasic();
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(51);
				and();
				setState(52);
				exprBasic();
				}
				}
				setState(58);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprBasicContext extends ParserRuleContext {
		public ExprBasicContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprBasic; }
	 
		public ExprBasicContext() { }
		public void copyFrom(ExprBasicContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ExprBasicInParentContext extends ExprBasicContext {
		public ExprOrContext exprOr() {
			return getRuleContext(ExprOrContext.class,0);
		}
		public ExprBasicInParentContext(ExprBasicContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprBasicInParent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprBasicInParent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprBasicInParent(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprBasicNearContext extends ExprBasicContext {
		public ExprNearContext exprNear() {
			return getRuleContext(ExprNearContext.class,0);
		}
		public ExprBasicNearContext(ExprBasicContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprBasicNear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprBasicNear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprBasicNear(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprBasicTermContext extends ExprBasicContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public ExprBasicTermContext(ExprBasicContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprBasicTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprBasicTerm(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprBasicTerm(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprBasicNotContext extends ExprBasicContext {
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public ExprBasicContext exprBasic() {
			return getRuleContext(ExprBasicContext.class,0);
		}
		public ExprBasicNotContext(ExprBasicContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterExprBasicNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitExprBasicNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitExprBasicNot(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class TermNotContext extends ExprBasicContext {
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermNotContext(ExprBasicContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterTermNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitTermNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitTermNot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprBasicContext exprBasic() throws RecognitionException {
		ExprBasicContext _localctx = new ExprBasicContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_exprBasic);
		try {
			setState(71);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new ExprBasicTermContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(59);
				term();
				}
				break;
			case 2:
				_localctx = new ExprBasicInParentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(60);
				match(T__0);
				setState(61);
				exprOr();
				setState(62);
				match(T__1);
				}
				break;
			case 3:
				_localctx = new TermNotContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(64);
				not();
				setState(65);
				term();
				}
				break;
			case 4:
				_localctx = new ExprBasicNotContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(67);
				not();
				setState(68);
				exprBasic();
				}
				break;
			case 5:
				_localctx = new ExprBasicNearContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(70);
				exprNear();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprNearContext extends ParserRuleContext {
		public ExprNearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprNear; }
	 
		public ExprNearContext() { }
		public void copyFrom(ExprNearContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class CustomerTermsNearClauseContext extends ExprNearContext {
		public TermsCustomerNearContext termsCustomerNear() {
			return getRuleContext(TermsCustomerNearContext.class,0);
		}
		public CustomerTermsNearClauseContext(ExprNearContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterCustomerTermsNearClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitCustomerTermsNearClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitCustomerTermsNearClause(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AgentTermsNearClauseContext extends ExprNearContext {
		public TermsAgentNearContext termsAgentNear() {
			return getRuleContext(TermsAgentNearContext.class,0);
		}
		public AgentTermsNearClauseContext(ExprNearContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAgentTermsNearClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAgentTermsNearClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAgentTermsNearClause(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NoSPSTermsNearClauseContext extends ExprNearContext {
		public TermsNoSPSNearContext termsNoSPSNear() {
			return getRuleContext(TermsNoSPSNearContext.class,0);
		}
		public NoSPSTermsNearClauseContext(ExprNearContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterNoSPSTermsNearClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitNoSPSTermsNearClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitNoSPSTermsNearClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprNearContext exprNear() throws RecognitionException {
		ExprNearContext _localctx = new ExprNearContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_exprNear);
		try {
			setState(76);
			switch (_input.LA(1)) {
			case DQUOTE:
			case INT:
			case WORD:
				_localctx = new NoSPSTermsNearClauseContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(73);
				termsNoSPSNear();
				}
				break;
			case AGENT_IDENT:
				_localctx = new AgentTermsNearClauseContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(74);
				termsAgentNear();
				}
				break;
			case CUSTOMER_IDENT:
				_localctx = new CustomerTermsNearClauseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(75);
				termsCustomerNear();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermsNoSPSNearContext extends ParserRuleContext {
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public List<PhraseContext> phrase() {
			return getRuleContexts(PhraseContext.class);
		}
		public PhraseContext phrase(int i) {
			return getRuleContext(PhraseContext.class,i);
		}
		public List<NearContext> near() {
			return getRuleContexts(NearContext.class);
		}
		public NearContext near(int i) {
			return getRuleContext(NearContext.class,i);
		}
		public TermsNoSPSNearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termsNoSPSNear; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterTermsNoSPSNear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitTermsNoSPSNear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitTermsNoSPSNear(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsNoSPSNearContext termsNoSPSNear() throws RecognitionException {
		TermsNoSPSNearContext _localctx = new TermsNoSPSNearContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_termsNoSPSNear);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			switch (_input.LA(1)) {
			case INT:
			case WORD:
				{
				setState(78);
				word();
				}
				break;
			case DQUOTE:
				{
				setState(79);
				phrase();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(87); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(82);
				near();
				setState(85);
				switch (_input.LA(1)) {
				case INT:
				case WORD:
					{
					setState(83);
					word();
					}
					break;
				case DQUOTE:
					{
					setState(84);
					phrase();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(89); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEAR );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermsAgentNearContext extends ParserRuleContext {
		public List<Agent_wordContext> agent_word() {
			return getRuleContexts(Agent_wordContext.class);
		}
		public Agent_wordContext agent_word(int i) {
			return getRuleContext(Agent_wordContext.class,i);
		}
		public List<Agent_phraseContext> agent_phrase() {
			return getRuleContexts(Agent_phraseContext.class);
		}
		public Agent_phraseContext agent_phrase(int i) {
			return getRuleContext(Agent_phraseContext.class,i);
		}
		public List<NearContext> near() {
			return getRuleContexts(NearContext.class);
		}
		public NearContext near(int i) {
			return getRuleContext(NearContext.class,i);
		}
		public TermsAgentNearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termsAgentNear; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterTermsAgentNear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitTermsAgentNear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitTermsAgentNear(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsAgentNearContext termsAgentNear() throws RecognitionException {
		TermsAgentNearContext _localctx = new TermsAgentNearContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_termsAgentNear);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(91);
				agent_word();
				}
				break;
			case 2:
				{
				setState(92);
				agent_phrase();
				}
				break;
			}
			setState(100); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(95);
				near();
				setState(98);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(96);
					agent_word();
					}
					break;
				case 2:
					{
					setState(97);
					agent_phrase();
					}
					break;
				}
				}
				}
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEAR );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermsCustomerNearContext extends ParserRuleContext {
		public List<Customer_wordContext> customer_word() {
			return getRuleContexts(Customer_wordContext.class);
		}
		public Customer_wordContext customer_word(int i) {
			return getRuleContext(Customer_wordContext.class,i);
		}
		public List<Customer_phraseContext> customer_phrase() {
			return getRuleContexts(Customer_phraseContext.class);
		}
		public Customer_phraseContext customer_phrase(int i) {
			return getRuleContext(Customer_phraseContext.class,i);
		}
		public List<NearContext> near() {
			return getRuleContexts(NearContext.class);
		}
		public NearContext near(int i) {
			return getRuleContext(NearContext.class,i);
		}
		public TermsCustomerNearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termsCustomerNear; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterTermsCustomerNear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitTermsCustomerNear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitTermsCustomerNear(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermsCustomerNearContext termsCustomerNear() throws RecognitionException {
		TermsCustomerNearContext _localctx = new TermsCustomerNearContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_termsCustomerNear);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(104);
				customer_word();
				}
				break;
			case 2:
				{
				setState(105);
				customer_phrase();
				}
				break;
			}
			setState(113); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(108);
				near();
				setState(111);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(109);
					customer_word();
					}
					break;
				case 2:
					{
					setState(110);
					customer_phrase();
					}
					break;
				}
				}
				}
				setState(115); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==NEAR );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	 
		public TermContext() { }
		public void copyFrom(TermContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class AgentWordContext extends TermContext {
		public Agent_wordContext agent_word() {
			return getRuleContext(Agent_wordContext.class,0);
		}
		public AgentWordContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAgentWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAgentWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAgentWord(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CustomerPhraseContext extends TermContext {
		public Customer_phraseContext customer_phrase() {
			return getRuleContext(Customer_phraseContext.class,0);
		}
		public CustomerPhraseContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterCustomerPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitCustomerPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitCustomerPhrase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NoSPSPhraseContext extends TermContext {
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public NoSPSPhraseContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterNoSPSPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitNoSPSPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitNoSPSPhrase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NOSPSWordContext extends TermContext {
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public NOSPSWordContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterNOSPSWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitNOSPSWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitNOSPSWord(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class AgentPhraseContext extends TermContext {
		public Agent_phraseContext agent_phrase() {
			return getRuleContext(Agent_phraseContext.class,0);
		}
		public AgentPhraseContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAgentPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAgentPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAgentPhrase(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class CustomerWordContext extends TermContext {
		public Customer_wordContext customer_word() {
			return getRuleContext(Customer_wordContext.class,0);
		}
		public CustomerWordContext(TermContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterCustomerWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitCustomerWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitCustomerWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_term);
		try {
			setState(123);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				_localctx = new CustomerWordContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(117);
				customer_word();
				}
				break;
			case 2:
				_localctx = new AgentWordContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				agent_word();
				}
				break;
			case 3:
				_localctx = new CustomerPhraseContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(119);
				customer_phrase();
				}
				break;
			case 4:
				_localctx = new AgentPhraseContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(120);
				agent_phrase();
				}
				break;
			case 5:
				_localctx = new NoSPSPhraseContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(121);
				phrase();
				}
				break;
			case 6:
				_localctx = new NOSPSWordContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(122);
				word();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Customer_phraseContext extends ParserRuleContext {
		public TerminalNode CUSTOMER_IDENT() { return getToken(VTASyntaxParser.CUSTOMER_IDENT, 0); }
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public Customer_phraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_customer_phrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterCustomer_phrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitCustomer_phrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitCustomer_phrase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Customer_phraseContext customer_phrase() throws RecognitionException {
		Customer_phraseContext _localctx = new Customer_phraseContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_customer_phrase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			match(CUSTOMER_IDENT);
			setState(126);
			match(T__2);
			setState(127);
			phrase();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Agent_phraseContext extends ParserRuleContext {
		public TerminalNode AGENT_IDENT() { return getToken(VTASyntaxParser.AGENT_IDENT, 0); }
		public PhraseContext phrase() {
			return getRuleContext(PhraseContext.class,0);
		}
		public Agent_phraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_agent_phrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAgent_phrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAgent_phrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAgent_phrase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Agent_phraseContext agent_phrase() throws RecognitionException {
		Agent_phraseContext _localctx = new Agent_phraseContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_agent_phrase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(AGENT_IDENT);
			setState(130);
			match(T__2);
			setState(131);
			phrase();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Customer_wordContext extends ParserRuleContext {
		public TerminalNode CUSTOMER_IDENT() { return getToken(VTASyntaxParser.CUSTOMER_IDENT, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Customer_wordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_customer_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterCustomer_word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitCustomer_word(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitCustomer_word(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Customer_wordContext customer_word() throws RecognitionException {
		Customer_wordContext _localctx = new Customer_wordContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_customer_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(CUSTOMER_IDENT);
			setState(134);
			match(T__2);
			setState(135);
			word();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Agent_wordContext extends ParserRuleContext {
		public TerminalNode AGENT_IDENT() { return getToken(VTASyntaxParser.AGENT_IDENT, 0); }
		public WordContext word() {
			return getRuleContext(WordContext.class,0);
		}
		public Agent_wordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_agent_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAgent_word(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAgent_word(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAgent_word(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Agent_wordContext agent_word() throws RecognitionException {
		Agent_wordContext _localctx = new Agent_wordContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_agent_word);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(137);
			match(AGENT_IDENT);
			setState(138);
			match(T__2);
			setState(139);
			word();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PhraseContext extends ParserRuleContext {
		public List<TerminalNode> DQUOTE() { return getTokens(VTASyntaxParser.DQUOTE); }
		public TerminalNode DQUOTE(int i) {
			return getToken(VTASyntaxParser.DQUOTE, i);
		}
		public List<WordContext> word() {
			return getRuleContexts(WordContext.class);
		}
		public WordContext word(int i) {
			return getRuleContext(WordContext.class,i);
		}
		public PhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitPhrase(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitPhrase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PhraseContext phrase() throws RecognitionException {
		PhraseContext _localctx = new PhraseContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_phrase);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			match(DQUOTE);
			setState(143); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(142);
				word();
				}
				}
				setState(145); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==INT || _la==WORD );
			setState(147);
			match(DQUOTE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WordContext extends ParserRuleContext {
		public TerminalNode WORD() { return getToken(VTASyntaxParser.WORD, 0); }
		public TerminalNode INT() { return getToken(VTASyntaxParser.INT, 0); }
		public WordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_word; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterWord(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitWord(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitWord(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WordContext word() throws RecognitionException {
		WordContext _localctx = new WordContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_word);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(149);
			_la = _input.LA(1);
			if ( !(_la==INT || _la==WORD) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NotContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(VTASyntaxParser.NOT, 0); }
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitNot(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitNot(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_not);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(151);
			match(NOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AndContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(VTASyntaxParser.AND, 0); }
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			match(AND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class OrContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(VTASyntaxParser.OR, 0); }
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitOr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(155);
			match(OR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NearContext extends ParserRuleContext {
		public TerminalNode NEAR() { return getToken(VTASyntaxParser.NEAR, 0); }
		public TerminalNode INT() { return getToken(VTASyntaxParser.INT, 0); }
		public NearContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_near; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).enterNear(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VTASyntaxListener ) ((VTASyntaxListener)listener).exitNear(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VTASyntaxVisitor ) return ((VTASyntaxVisitor<? extends T>)visitor).visitNear(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NearContext near() throws RecognitionException {
		NearContext _localctx = new NearContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_near);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			match(NEAR);
			setState(160);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(158);
				match(T__2);
				setState(159);
				match(INT);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\20\u00a5\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\3\2\3\2\3\3\3\3\5\3-\n\3\3\3\7\3\60\n\3\f\3\16\3"+
		"\63\13\3\3\4\3\4\3\4\3\4\7\49\n\4\f\4\16\4<\13\4\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5J\n\5\3\6\3\6\3\6\5\6O\n\6\3\7\3\7\5\7S"+
		"\n\7\3\7\3\7\3\7\5\7X\n\7\6\7Z\n\7\r\7\16\7[\3\b\3\b\5\b`\n\b\3\b\3\b"+
		"\3\b\5\be\n\b\6\bg\n\b\r\b\16\bh\3\t\3\t\5\tm\n\t\3\t\3\t\3\t\5\tr\n\t"+
		"\6\tt\n\t\r\t\16\tu\3\n\3\n\3\n\3\n\3\n\3\n\5\n~\n\n\3\13\3\13\3\13\3"+
		"\13\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\6\17"+
		"\u0092\n\17\r\17\16\17\u0093\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3"+
		"\23\3\23\3\24\3\24\3\24\5\24\u00a3\n\24\3\24\2\2\25\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&\2\3\4\2\t\t\17\17\u00aa\2(\3\2\2\2\4*\3\2\2"+
		"\2\6\64\3\2\2\2\bI\3\2\2\2\nN\3\2\2\2\fR\3\2\2\2\16_\3\2\2\2\20l\3\2\2"+
		"\2\22}\3\2\2\2\24\177\3\2\2\2\26\u0083\3\2\2\2\30\u0087\3\2\2\2\32\u008b"+
		"\3\2\2\2\34\u008f\3\2\2\2\36\u0097\3\2\2\2 \u0099\3\2\2\2\"\u009b\3\2"+
		"\2\2$\u009d\3\2\2\2&\u009f\3\2\2\2()\5\4\3\2)\3\3\2\2\2*\61\5\6\4\2+-"+
		"\5$\23\2,+\3\2\2\2,-\3\2\2\2-.\3\2\2\2.\60\5\6\4\2/,\3\2\2\2\60\63\3\2"+
		"\2\2\61/\3\2\2\2\61\62\3\2\2\2\62\5\3\2\2\2\63\61\3\2\2\2\64:\5\b\5\2"+
		"\65\66\5\"\22\2\66\67\5\b\5\2\679\3\2\2\28\65\3\2\2\29<\3\2\2\2:8\3\2"+
		"\2\2:;\3\2\2\2;\7\3\2\2\2<:\3\2\2\2=J\5\22\n\2>?\7\3\2\2?@\5\4\3\2@A\7"+
		"\4\2\2AJ\3\2\2\2BC\5 \21\2CD\5\22\n\2DJ\3\2\2\2EF\5 \21\2FG\5\b\5\2GJ"+
		"\3\2\2\2HJ\5\n\6\2I=\3\2\2\2I>\3\2\2\2IB\3\2\2\2IE\3\2\2\2IH\3\2\2\2J"+
		"\t\3\2\2\2KO\5\f\7\2LO\5\16\b\2MO\5\20\t\2NK\3\2\2\2NL\3\2\2\2NM\3\2\2"+
		"\2O\13\3\2\2\2PS\5\36\20\2QS\5\34\17\2RP\3\2\2\2RQ\3\2\2\2SY\3\2\2\2T"+
		"W\5&\24\2UX\5\36\20\2VX\5\34\17\2WU\3\2\2\2WV\3\2\2\2XZ\3\2\2\2YT\3\2"+
		"\2\2Z[\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\\r\3\2\2\2]`\5\32\16\2^`\5\26\f\2"+
		"_]\3\2\2\2_^\3\2\2\2`f\3\2\2\2ad\5&\24\2be\5\32\16\2ce\5\26\f\2db\3\2"+
		"\2\2dc\3\2\2\2eg\3\2\2\2fa\3\2\2\2gh\3\2\2\2hf\3\2\2\2hi\3\2\2\2i\17\3"+
		"\2\2\2jm\5\30\r\2km\5\24\13\2lj\3\2\2\2lk\3\2\2\2ms\3\2\2\2nq\5&\24\2"+
		"or\5\30\r\2pr\5\24\13\2qo\3\2\2\2qp\3\2\2\2rt\3\2\2\2sn\3\2\2\2tu\3\2"+
		"\2\2us\3\2\2\2uv\3\2\2\2v\21\3\2\2\2w~\5\30\r\2x~\5\32\16\2y~\5\24\13"+
		"\2z~\5\26\f\2{~\5\34\17\2|~\5\36\20\2}w\3\2\2\2}x\3\2\2\2}y\3\2\2\2}z"+
		"\3\2\2\2}{\3\2\2\2}|\3\2\2\2~\23\3\2\2\2\177\u0080\7\b\2\2\u0080\u0081"+
		"\7\5\2\2\u0081\u0082\5\34\17\2\u0082\25\3\2\2\2\u0083\u0084\7\7\2\2\u0084"+
		"\u0085\7\5\2\2\u0085\u0086\5\34\17\2\u0086\27\3\2\2\2\u0087\u0088\7\b"+
		"\2\2\u0088\u0089\7\5\2\2\u0089\u008a\5\36\20\2\u008a\31\3\2\2\2\u008b"+
		"\u008c\7\7\2\2\u008c\u008d\7\5\2\2\u008d\u008e\5\36\20\2\u008e\33\3\2"+
		"\2\2\u008f\u0091\7\6\2\2\u0090\u0092\5\36\20\2\u0091\u0090\3\2\2\2\u0092"+
		"\u0093\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0095\3\2"+
		"\2\2\u0095\u0096\7\6\2\2\u0096\35\3\2\2\2\u0097\u0098\t\2\2\2\u0098\37"+
		"\3\2\2\2\u0099\u009a\7\n\2\2\u009a!\3\2\2\2\u009b\u009c\7\13\2\2\u009c"+
		"#\3\2\2\2\u009d\u009e\7\f\2\2\u009e%\3\2\2\2\u009f\u00a2\7\r\2\2\u00a0"+
		"\u00a1\7\5\2\2\u00a1\u00a3\7\t\2\2\u00a2\u00a0\3\2\2\2\u00a2\u00a3\3\2"+
		"\2\2\u00a3\'\3\2\2\2\23,\61:INRW[_dhlqu}\u0093\u00a2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}