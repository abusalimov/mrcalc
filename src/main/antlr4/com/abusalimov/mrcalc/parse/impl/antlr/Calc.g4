grammar Calc;

program: STMT_DELIM? stmt (STMT_DELIM stmt)* STMT_DELIM? EOF ;

stmt
    : expr  # exprStmt
    ;

expr
    : number                      # numberExpr
    | op=('+'|'-') expr           # unaryOpExpr
    | a=expr op='^'       b=expr  # binaryOpExpr
    | a=expr op=('*'|'/') b=expr  # binaryOpExpr
    | a=expr op=('+'|'-') b=expr  # binaryOpExpr
    | '(' expr ')'                # parensExpr
    ;

number returns [Number value]
    : token=INT   {$value = Long.decode($token.text);}
    | token=FLOAT {$value = Double.valueOf($token.text);}
    ;

STMT_DELIM : [\r\n;]+ ;

WS : [ \t]+ -> skip ;

ADD_OP : '+' ;
SUB_OP : '-' ;
MUL_OP : '*' ;
DIV_OP : '/' ;
POW_OP : '^' ;

INT   : ('0'[0-7]* | [1-9][0-9]* | '0'[xX][0-9a-fA-F]+);
FLOAT : ([0-9]+ '.' [0-9]* | '.' [0-9]+);

L_PAREN : '(' ;
R_PAREN : ')' ;
