grammar Calc;

program: stmt EOF ;
stmt: expr ;

expr
    : '(' expr ')' # parensExpr
    | number       # numberExpr
    ;

number returns [Number value]
    : token=INT {$value = Long.decode($token.text);}
    | token=FLOAT {$value = Double.valueOf($token.text);}
    ;

L_PAREN : '(' ;
R_PAREN : ')' ;

INT : [+-]? ('0'[0-7]* | [1-9][0-9]* | '0'[xX][0-9a-fA-F]+);
FLOAT : [+-]? ([0-9]+ '.' [0-9]* | '.' [0-9]+);
WS  : [ \r\t\n]+ -> skip ;
