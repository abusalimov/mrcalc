grammar Calc;

program: (STMT_DELIM* stmt (STMT_DELIM+ stmt)*)? STMT_DELIM* EOF ;

stmt
    : 'var' name=ID '=' expr      # varDefStmt
    | ('print')? expr             # printStmt
    | 'out' string                # outStmt
    ;

expr
    : number                          # numberExpr
    | '{' start=expr ',' end=expr '}' # rangeExpr
    | name=ID                         # varRefExpr
    | op=('+'|'-') expr               # unaryOpExpr
    | a=expr op='^'       b=expr      # binaryOpExpr
    | a=expr op=('*'|'/') b=expr      # binaryOpExpr
    | a=expr op=('+'|'-') b=expr      # binaryOpExpr
    | '(' expr ')'                    # parensExpr
    | 'map' '(' expr ',' lambda ')'   # mapExpr
    | 'reduce' '(' expr ','
            expr ',' lambda ')'       # reduceExpr
    ;

lambda
    : ID* '->' expr ;

number returns [Number value]
    : token=INT   {$value = Long.decode($token.text);}
    | token=FLOAT {$value = Double.valueOf($token.text);}
    ;
    catch[NumberFormatException e] { throw new NoViableAltException(this, getInputStream(),
                                                                    getInputStream().LT(-1),
                                                                    getInputStream().LT(-1),
                                                                    null,
                                                                    _ctx); }

string returns [String value]
    : token=STRING
    ;

STMT_DELIM : [\r\n;] ;

WS : [ \t]+ -> channel(HIDDEN) ;

ADD_OP : '+' ;
SUB_OP : '-' ;
MUL_OP : '*' ;
DIV_OP : '/' ;
POW_OP : '^' ;

EQ_SIGN : '=' ;

VAR_KW    : 'var' ;
PRINT_KW  : 'print' ;
OUT_KW    : 'out' ;
MAP_KW    : 'map' ;
REDUCE_KW : 'reduce' ;

ID : [A-Za-z_][A-Za-z_0-9]* ;

INT   : ('0'[0-7]* | [1-9][0-9]* | '0'[xX][0-9a-fA-F]+);
FLOAT : ([0-9]+ '.' [0-9]* | '.' [0-9]+);

L_PAREN : '(' ;
R_PAREN : ')' ;

L_BRACE : '{' ;
R_BRACE : '}' ;

COMMA : ',' ;
ARROW : '->' ;

STRING : '"' ('\\'[\r\n\\"rnt] | ~[\r\n\\"])*? '"' ;

INVALID: .;
