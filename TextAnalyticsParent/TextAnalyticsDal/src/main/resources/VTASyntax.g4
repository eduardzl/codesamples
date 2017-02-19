/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar VTASyntax;

options {

}


fragment A:('a'|'A');
fragment B:('b'|'B');
fragment C:('c'|'C');
fragment D:('d'|'D');
fragment E:('e'|'E');
fragment F:('f'|'F');
fragment G:('g'|'G');
fragment H:('h'|'H');
fragment I:('i'|'I');
fragment J:('j'|'J');
fragment K:('k'|'K');
fragment L:('l'|'L');
fragment M:('m'|'M');
fragment N:('n'|'N');
fragment O:('o'|'O');
fragment P:('p'|'P');
fragment Q:('q'|'Q');
fragment R:('r'|'R');
fragment S:('s'|'S');
fragment T:('t'|'T');
fragment U:('u'|'U');
fragment V:('v'|'V');
fragment W:('w'|'W');
fragment X:('x'|'X');
fragment Y:('y'|'Y');
fragment Z:('z'|'Z');
fragment VBAR  : '|' ;
fragment AMPER : '&' ;

fragment SPACE : (' '|'\r'|'\t'|'\u000C'|'\n') ;
fragment DIGIT : [0|1|2|3|4|5|6|7|8|9];


DQUOTE	:	('\u0022'|'\u05F4'|'\uFF02'|'\u02EE'|'\u05F2'|'\u1CD3'|'\u2033'|'\u02BA'|'\u02DD'|'\u02F6'|'\u201F'|'\u201D'|'\u201C');
AGENT_IDENT : ('A'| 'AGENT' | 'E' | 'EMPLOYEE');
CUSTOMER_IDENT : ('C'| 'CUSTOMER');
INT : DIGIT+ ;

NOT   : 'NOT';
AND   : 'AND';
OR    : 'OR'  ;
NEAR  : 'NEAR';
NOTIN : 'NOTIN';

WORD : ~('\\'
        |'\u0022'|'\u05F4'|'\uFF02'|'\u02EE'|'\u05F2'|'\u1CD3'|'\u2033'|'\u02BA'|'\u02DD'|'\u02F6'|'\u201F'|'\u201D'|'\u201C'
        |' '
        |'\r'|'\t'|'\u000C'|'\n'
        |':'
        |'['|']'
        |'<'| '>'
        |'(' |')'
        |'{'|'}'
        | '/')+
        ;

WS : SPACE+ -> skip;

/*
Lexer rules are matched from top to bottom. In case two (or more) rules match the same number of characters,
the one that is defined first has precedence over the one(s) later defined in the grammar.
*/


vtaexpr : exprOr;

exprOr : exprAnd (or? exprAnd)*;
exprAnd: exprBasic (and exprBasic)*;

exprBasic : term   # ExprBasicTerm
          | '(' exprOr ')' # ExprBasicInParent
          |  not term    # TermNot
          |  not exprBasic # ExprBasicNot
          |  exprNear          # ExprBasicNear
;

exprNear :  termsNoSPSNear # NoSPSTermsNearClause
          | termsAgentNear # AgentTermsNearClause
          | termsCustomerNear # CustomerTermsNearClause
;

termsNoSPSNear :  (word|phrase) (near (word|phrase))+;
termsAgentNear :  (agent_word|agent_phrase) (near (agent_word|agent_phrase))+;
termsCustomerNear : (customer_word|customer_phrase) (near (customer_word|customer_phrase))+;

term
	:    customer_word # CustomerWord
        |agent_word # AgentWord
        |customer_phrase # CustomerPhrase
        |agent_phrase    # AgentPhrase
        |phrase          # NoSPSPhrase
        |word            # NOSPSWord
    ;

customer_phrase: CUSTOMER_IDENT ':' phrase;
agent_phrase: AGENT_IDENT  ':' phrase;
customer_word: CUSTOMER_IDENT ':' word;
agent_word: AGENT_IDENT ':' word;
phrase: DQUOTE word+  DQUOTE;
word: (WORD|INT);


not	: NOT;
and :	AND;
or 	:	OR;
near : NEAR(':'INT)?;





        