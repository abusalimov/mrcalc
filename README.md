MapReduce calculator [![Build Status][travis-image]][travis-url]
====================

MrCalc is a simple calculator application supporting basic arithmetic operations on numbers and
[MapReduce](https://en.wikipedia.org/wiki/MapReduce) functions on sequences.

![screenshot](https://cloud.githubusercontent.com/assets/530396/16604589/27f50a40-4327-11e6-8756-422647306c3b.png)

Download
--------
MrCalc uses the standard Maven build suite and requires Java 8 runtime.

```console
$ git clone https://github.com/abusalimov/mrcalc.git mrcalc
$ cd mrcalc
$ mvn package
$ java -jar mrcalc-<VERSION>-jar-with-dependencies.jar
```

Also the latest release is available for downloading [here](https://github.com/abusalimov/mrcalc/releases/latest).

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
