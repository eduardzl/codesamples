// Generated from C:/GIT/TextAnalytics_WebApplication/Eclipse/TextAnalyticsParent/TextAnalyticsDal/src/main/resources\VTASyntax.g4 by ANTLR 4.5.3
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link VTASyntaxParser}.
 */
public interface VTASyntaxListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#vtaexpr}.
	 * @param ctx the parse tree
	 */
	void enterVtaexpr(VTASyntaxParser.VtaexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#vtaexpr}.
	 * @param ctx the parse tree
	 */
	void exitVtaexpr(VTASyntaxParser.VtaexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#exprOr}.
	 * @param ctx the parse tree
	 */
	void enterExprOr(VTASyntaxParser.ExprOrContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#exprOr}.
	 * @param ctx the parse tree
	 */
	void exitExprOr(VTASyntaxParser.ExprOrContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#exprAnd}.
	 * @param ctx the parse tree
	 */
	void enterExprAnd(VTASyntaxParser.ExprAndContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#exprAnd}.
	 * @param ctx the parse tree
	 */
	void exitExprAnd(VTASyntaxParser.ExprAndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprBasicTerm}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void enterExprBasicTerm(VTASyntaxParser.ExprBasicTermContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprBasicTerm}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void exitExprBasicTerm(VTASyntaxParser.ExprBasicTermContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprBasicInParent}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void enterExprBasicInParent(VTASyntaxParser.ExprBasicInParentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprBasicInParent}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void exitExprBasicInParent(VTASyntaxParser.ExprBasicInParentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code TermNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void enterTermNot(VTASyntaxParser.TermNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code TermNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void exitTermNot(VTASyntaxParser.TermNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprBasicNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void enterExprBasicNot(VTASyntaxParser.ExprBasicNotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprBasicNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void exitExprBasicNot(VTASyntaxParser.ExprBasicNotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprBasicNear}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void enterExprBasicNear(VTASyntaxParser.ExprBasicNearContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprBasicNear}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 */
	void exitExprBasicNear(VTASyntaxParser.ExprBasicNearContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NoSPSTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void enterNoSPSTermsNearClause(VTASyntaxParser.NoSPSTermsNearClauseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NoSPSTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void exitNoSPSTermsNearClause(VTASyntaxParser.NoSPSTermsNearClauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AgentTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void enterAgentTermsNearClause(VTASyntaxParser.AgentTermsNearClauseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AgentTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void exitAgentTermsNearClause(VTASyntaxParser.AgentTermsNearClauseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CustomerTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void enterCustomerTermsNearClause(VTASyntaxParser.CustomerTermsNearClauseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CustomerTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 */
	void exitCustomerTermsNearClause(VTASyntaxParser.CustomerTermsNearClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#termsNoSPSNear}.
	 * @param ctx the parse tree
	 */
	void enterTermsNoSPSNear(VTASyntaxParser.TermsNoSPSNearContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#termsNoSPSNear}.
	 * @param ctx the parse tree
	 */
	void exitTermsNoSPSNear(VTASyntaxParser.TermsNoSPSNearContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#termsAgentNear}.
	 * @param ctx the parse tree
	 */
	void enterTermsAgentNear(VTASyntaxParser.TermsAgentNearContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#termsAgentNear}.
	 * @param ctx the parse tree
	 */
	void exitTermsAgentNear(VTASyntaxParser.TermsAgentNearContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#termsCustomerNear}.
	 * @param ctx the parse tree
	 */
	void enterTermsCustomerNear(VTASyntaxParser.TermsCustomerNearContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#termsCustomerNear}.
	 * @param ctx the parse tree
	 */
	void exitTermsCustomerNear(VTASyntaxParser.TermsCustomerNearContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CustomerWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterCustomerWord(VTASyntaxParser.CustomerWordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CustomerWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitCustomerWord(VTASyntaxParser.CustomerWordContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AgentWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterAgentWord(VTASyntaxParser.AgentWordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AgentWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitAgentWord(VTASyntaxParser.AgentWordContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CustomerPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterCustomerPhrase(VTASyntaxParser.CustomerPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CustomerPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitCustomerPhrase(VTASyntaxParser.CustomerPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AgentPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterAgentPhrase(VTASyntaxParser.AgentPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AgentPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitAgentPhrase(VTASyntaxParser.AgentPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NoSPSPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterNoSPSPhrase(VTASyntaxParser.NoSPSPhraseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NoSPSPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitNoSPSPhrase(VTASyntaxParser.NoSPSPhraseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NOSPSWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void enterNOSPSWord(VTASyntaxParser.NOSPSWordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NOSPSWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 */
	void exitNOSPSWord(VTASyntaxParser.NOSPSWordContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#customer_phrase}.
	 * @param ctx the parse tree
	 */
	void enterCustomer_phrase(VTASyntaxParser.Customer_phraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#customer_phrase}.
	 * @param ctx the parse tree
	 */
	void exitCustomer_phrase(VTASyntaxParser.Customer_phraseContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#agent_phrase}.
	 * @param ctx the parse tree
	 */
	void enterAgent_phrase(VTASyntaxParser.Agent_phraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#agent_phrase}.
	 * @param ctx the parse tree
	 */
	void exitAgent_phrase(VTASyntaxParser.Agent_phraseContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#customer_word}.
	 * @param ctx the parse tree
	 */
	void enterCustomer_word(VTASyntaxParser.Customer_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#customer_word}.
	 * @param ctx the parse tree
	 */
	void exitCustomer_word(VTASyntaxParser.Customer_wordContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#agent_word}.
	 * @param ctx the parse tree
	 */
	void enterAgent_word(VTASyntaxParser.Agent_wordContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#agent_word}.
	 * @param ctx the parse tree
	 */
	void exitAgent_word(VTASyntaxParser.Agent_wordContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterPhrase(VTASyntaxParser.PhraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitPhrase(VTASyntaxParser.PhraseContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#word}.
	 * @param ctx the parse tree
	 */
	void enterWord(VTASyntaxParser.WordContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#word}.
	 * @param ctx the parse tree
	 */
	void exitWord(VTASyntaxParser.WordContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#not}.
	 * @param ctx the parse tree
	 */
	void enterNot(VTASyntaxParser.NotContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#not}.
	 * @param ctx the parse tree
	 */
	void exitNot(VTASyntaxParser.NotContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(VTASyntaxParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(VTASyntaxParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(VTASyntaxParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(VTASyntaxParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link VTASyntaxParser#near}.
	 * @param ctx the parse tree
	 */
	void enterNear(VTASyntaxParser.NearContext ctx);
	/**
	 * Exit a parse tree produced by {@link VTASyntaxParser#near}.
	 * @param ctx the parse tree
	 */
	void exitNear(VTASyntaxParser.NearContext ctx);
}