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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Facade class that runs on a separate thread.
 *
 * @author - Eldar Abusalimov
 */
public class CalcExecutor {
    private final Executor singleExecutor;
    private Consumer<List<Diagnostic>> callback;

    public CalcExecutor() {
        singleExecutor = Executors.newSingleThreadExecutor();
    }

    public void setCallback(Consumer<List<Diagnostic>> callback) {
        this.callback = callback;
    }

    public void execute(String sourceCode, OutputStream outputStream) {
        singleExecutor.execute(() -> run(sourceCode, outputStream));
    }

    private void run(String sourceCode, OutputStream outputStream) {
        Parser parser = new ANTLRParserImpl();
        Backend backend = new FuncBackendImpl();
        Compiler compiler = new Compiler(backend);
        PrintStream printStream = new PrintStream(outputStream);
        Interpreter interpreter = new Interpreter(printStream);
        try {
            ProgramNode node = parser.parse(sourceCode);
            List<Stmt> stmts = compiler.compile(node);
            interpreter.exec(stmts);
        } catch (SyntaxErrorException | CompileErrorException e) {
            if (callback != null)
                callback.accept(e.getDiagnostics());
        }
    }
}
