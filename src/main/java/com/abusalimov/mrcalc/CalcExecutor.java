package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.impl.bytebuddy.BytebuddyBackendImpl;
import com.abusalimov.mrcalc.compile.Compiler;
import com.abusalimov.mrcalc.compile.Stmt;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticException;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl;
import com.abusalimov.mrcalc.runtime.Runtime;
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    public void execute(String sourceCode, Supplier<OutputStream> outputStreamSupplier) {
        singleExecutor.execute(() -> run(sourceCode, outputStreamSupplier));
    }

    private void run(String sourceCode, Supplier<OutputStream> outputStreamSupplier) {
        Parser parser = new ANTLRParserImpl();
        Backend backend = new BytebuddyBackendImpl();
        Compiler compiler = new Compiler(backend);
        Runtime runtime = new StreamRuntime();
        Interpreter interpreter = new Interpreter(runtime);

        try (PrintStream printStream = new PrintStream(outputStreamSupplier.get())) {
            interpreter.setOutStream(printStream);
            ProgramNode node = parser.parse(sourceCode);
            List<Stmt> stmts = compiler.compile(node);
            interpreter.exec(stmts);
        } catch (DiagnosticException e) {
            if (callback != null)
                callback.accept(e.getDiagnostics());
        }
    }
}
