package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl;
import com.abusalimov.mrcalc.compile.CompileErrorException;
import com.abusalimov.mrcalc.compile.Compiler;
import com.abusalimov.mrcalc.compile.Stmt;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.SyntaxErrorException;
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl;
import com.abusalimov.mrcalc.runtime.Runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Eldar Abusalimov
 */
public class REPL {
    public static final String PROMPT = ">>> ";
    public static final String GREETING = "Welcome to MrCalc and have a lot of fun!";
    public static final String GOODBYE = "Goodbye!";

    private Parser parser;
    private Compiler compiler;
    private Interpreter interpreter;

    public REPL() {
        parser = new ANTLRParserImpl();
        Backend<?> exprBuilderFactory = new FuncBackendImpl();
        compiler = new Compiler(exprBuilderFactory);
        Runtime runtime = new Runtime();
        interpreter = new Interpreter(runtime, System.out);
    }

    public void loop() {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(GREETING);
        while (true) {
            System.out.print(PROMPT);
            String line;
            try {
                line = input.readLine();
            } catch (IOException e) {
                line = null;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() == 0) {
                continue;
            }
            try {
                ProgramNode node = parser.parse(line);
                List<Stmt> stmts = compiler.compile(node);
                interpreter.exec(stmts);
            } catch (SyntaxErrorException | CompileErrorException e) {
                List<Diagnostic> diagnostics = e.getDiagnostics();
                for (int i = 0; i < diagnostics.size(); i++) {
                    Diagnostic diagnostic = diagnostics.get(i);
                    if (i == 0) {
                        // Only the first error (if any at all) gets its caret printed.
                        System.err.println(diagnostic.getCaretLine(PROMPT.length()));
                    }
                    System.err.println(diagnostic.toString());
                }
            }
        }
        System.out.println(GOODBYE);
    }
}
