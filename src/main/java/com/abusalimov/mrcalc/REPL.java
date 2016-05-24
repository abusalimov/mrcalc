package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.compile.Code;
import com.abusalimov.mrcalc.compile.Compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
        try {
            System.out.println(GREETING);
            while (true) {
                System.out.print(PROMPT);
                String line = input.readLine();
                if (line == null) {
                    break;
                }
                if (line.trim().length() == 0) {
                    continue;
                }
                try {
                    Code code = compiler.compile(line);
                    Number result = interpreter.eval(code);
                    System.out.println(result);
                } catch (SyntaxErrorException e) {
                    System.err.println(e.getMessage());
                }
            }
            System.out.println(GOODBYE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
