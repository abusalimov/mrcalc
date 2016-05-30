package com.abusalimov.mrcalc

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

    void testParsesValidInput() {
        assert null != parse("0")
        assert null != parse("(1)")
        assert null != parse("(  (2 ))")
        assert null != parse(" ( ( ( 3 ) ) ) ")
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
    }
}
