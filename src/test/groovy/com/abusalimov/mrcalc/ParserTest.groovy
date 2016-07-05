package com.abusalimov.mrcalc

import com.abusalimov.mrcalc.ast.stmt.OutStmtNode
import com.abusalimov.mrcalc.parse.Parser
import com.abusalimov.mrcalc.parse.SyntaxErrorException
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl

/**
 * @author Eldar Abusalimov
 */
class ParserTest extends GroovyTestCase {
    private Parser parser

    void setUp() {
        super.setUp()
        parser = new ANTLRParserImpl()
    }

    def parse(String s) {
        parser.parse s
    }

    def parseOutStr(String s) {
        def node = parser.parse("out $s")
        def outStmt = node.getStmts().get(0) as OutStmtNode
        outStmt.string
    }

    void testParsesValidInput() {
        assert null != parse("0")
        assert null != parse("(1)")
        assert null != parse("(  (2 ))")
        assert null != parse(" ( ( ( 3 ) ) ) ")
        assert null != parse("0000000000000000000000000000000000000000000000")

    }

    void testParsesValidOpExpressions() {
        assert null != parse("-1")
        assert null != parse("+2")
        assert null != parse("(0-1)")
        assert null != parse("1+2-3*4/5^6")
    }

    void testParsesVariableReferences() {
        assert null != parse("foo")
        assert null != parse("foo-bar")
        assert null != parse("(foo)+1")
    }

    void testParsesPrintStatement() {
        assert null != parse("print 0")
        assert null != parse("print(1+2)")
        assert null != parse("print foo/bar")
    }

    void testParsesOutStrings() {
        assert "" == parseOutStr('""')
        assert " " == parseOutStr('" "')
        assert "foo" == parseOutStr('"foo"')

        shouldFail SyntaxErrorException, { parseOutStr '"' }
        shouldFail SyntaxErrorException, { parseOutStr '"""' }
        shouldFail SyntaxErrorException, { parseOutStr '"\n"' }
    }

    void testParsesRanges() {
        assert null != parse("{1, 2}")
        assert null != parse("({(0),(0)})")
        assert null != parse("{1-2, 3+4}")
        assert null != parse("{foo, bar}")
    }

    void testParsesMapReduce() {
        assert null != parse("map({1,2}, a -> a^2)")
        assert null != parse("reduce({0,9}, 0, a b -> a+b)")
        assert null != parse("map({0,9}, x -> reduce({1,x}, 1, a b -> a * b))")
    }

    void testThrowsSyntaxErrors() {
        shouldFail SyntaxErrorException, { parse "(" }
        shouldFail SyntaxErrorException, { parse ")" }
        shouldFail SyntaxErrorException, { parse "()" }
        shouldFail SyntaxErrorException, { parse "(-)" }
        shouldFail SyntaxErrorException, { parse "\$" }
        shouldFail SyntaxErrorException, { parse "((13)" }
        shouldFail SyntaxErrorException, { parse "(13))" }

        shouldFail SyntaxErrorException, { parse "(18+)" }
        shouldFail SyntaxErrorException, { parse "+" }
        shouldFail SyntaxErrorException, { parse "1++" }
        shouldFail SyntaxErrorException, { parse "***" }

        shouldFail SyntaxErrorException, { parse "print" }
        shouldFail SyntaxErrorException, { parse "1111111111111111111111111111111111111111111111" }
    }
}
