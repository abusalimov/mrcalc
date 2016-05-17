MapReduce calculator [![Build Status][travis-image]][travis-url]
====================

MrCalc is a simple calculator application supporting basic arithmetic operations on numbers and
[MapReduce](https://en.wikipedia.org/wiki/MapReduce) functions on sequences.

Language grammar
----------------
```py
program = (stmt)* EOF ;

stmt = "var" ID "=" expr
     | "out" expr
     | "print" STRING
     ;
expr = ID
     | number
     | '(' expr ')'
     | expr OP expr
     | '{' expr ',' expr '}'
     | "map" '(' expr ',' ID '->' expr ')'
     | "reduce" '(' expr ',' expr ',' ID ID '->' expr ')'
     ;

number = INT | FLOAT ;

INT   = r'0[0-7]*' | r'[1-9][0-9]*' | r'0[xX][0-9a-fA-F]+' ;
FLOAT = r'[0-9]+\.[0-9]*|\.[0-9]+' ;

OP = '+' | '-' | '*' | '/' | '^' ;
ID = r'[a-zA-Z_][a-zA-Z_0-9]*' ;
```

[travis-url]: https://travis-ci.org/abusalimov/mrcalc
[travis-image]: https://travis-ci.org/abusalimov/mrcalc.svg?branch=master
