package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.compile.Code;
import com.abusalimov.mrcalc.compile.Compiler;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author Eldar Abusalimov
 */
public class REPL {
    public static final String PROMPT = ">>> ";
    public static final String GREETING = "Welcome to MrCalc and have a lot of fun!";
    public static final String GOODBYE = "Goodbye!";

    private Compiler compiler;
    private Interpreter interpreter;

    public REPL() {
        compiler = new Compiler();
        interpreter = new Interpreter();
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
            ArrayList<Diagnostic> diagnostics = new ArrayList<>();
            DiagnosticListener diagnosticCollector = diagnostics::add;
            compiler.addDiagnosticListener(diagnosticCollector);
            try {
                Code code = compiler.compile(line);
                Number result = interpreter.eval(code);
                System.out.println(result);
            } catch (SyntaxErrorException e) {
                for (int i = 0; i < diagnostics.size(); i++) {
                    Diagnostic diagnostic = diagnostics.get(i);
                    if (i == 0) {
                        // Only the first error (if any at all) gets its caret printed.
                        System.err.println(diagnostic.getCaretLine(PROMPT.length()));
                    }
                    System.err.println(diagnostic.toString());
                }
            } finally {
                compiler.removeDiagnosticListener(diagnosticCollector);
            }
        }
        System.out.println(GOODBYE);
    }
}
