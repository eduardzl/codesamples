package com.verint.textanalytics.dal.darwin.vtasyntax;// Generated from VTASyntax.g4 by ANTLR 4.5.2
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link VTASyntaxParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface VTASyntaxVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#vtaexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVtaexpr(VTASyntaxParser.VtaexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#exprOr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprOr(VTASyntaxParser.ExprOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#exprAnd}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprAnd(VTASyntaxParser.ExprAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprBasicTerm}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBasicTerm(VTASyntaxParser.ExprBasicTermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprBasicInParent}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBasicInParent(VTASyntaxParser.ExprBasicInParentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TermNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermNot(VTASyntaxParser.TermNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprBasicNot}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBasicNot(VTASyntaxParser.ExprBasicNotContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ExprBasicNear}
	 * labeled alternative in {@link VTASyntaxParser#exprBasic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprBasicNear(VTASyntaxParser.ExprBasicNearContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NoSPSTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoSPSTermsNearClause(VTASyntaxParser.NoSPSTermsNearClauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AgentTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgentTermsNearClause(VTASyntaxParser.AgentTermsNearClauseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CustomerTermsNearClause}
	 * labeled alternative in {@link VTASyntaxParser#exprNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomerTermsNearClause(VTASyntaxParser.CustomerTermsNearClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#termsNoSPSNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermsNoSPSNear(VTASyntaxParser.TermsNoSPSNearContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#termsAgentNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermsAgentNear(VTASyntaxParser.TermsAgentNearContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#termsCustomerNear}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermsCustomerNear(VTASyntaxParser.TermsCustomerNearContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CustomerWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomerWord(VTASyntaxParser.CustomerWordContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AgentWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgentWord(VTASyntaxParser.AgentWordContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CustomerPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomerPhrase(VTASyntaxParser.CustomerPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AgentPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgentPhrase(VTASyntaxParser.AgentPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NoSPSPhrase}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoSPSPhrase(VTASyntaxParser.NoSPSPhraseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NOSPSWord}
	 * labeled alternative in {@link VTASyntaxParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNOSPSWord(VTASyntaxParser.NOSPSWordContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#customer_phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomer_phrase(VTASyntaxParser.Customer_phraseContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#agent_phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgent_phrase(VTASyntaxParser.Agent_phraseContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#customer_word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCustomer_word(VTASyntaxParser.Customer_wordContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#agent_word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAgent_word(VTASyntaxParser.Agent_wordContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#phrase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPhrase(VTASyntaxParser.PhraseContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#word}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWord(VTASyntaxParser.WordContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#not}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNot(VTASyntaxParser.NotContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(VTASyntaxParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(VTASyntaxParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link VTASyntaxParser#near}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNear(VTASyntaxParser.NearContext ctx);
}