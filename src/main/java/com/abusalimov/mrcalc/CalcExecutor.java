package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.impl.bytebuddy.BytebuddyBackendImpl;
import com.abusalimov.mrcalc.compile.CompileErrorException;
import com.abusalimov.mrcalc.compile.Compiler;
import com.abusalimov.mrcalc.compile.Stmt;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;
import com.abusalimov.mrcalc.diagnostic.DiagnosticListener;
import com.abusalimov.mrcalc.parse.Parser;
import com.abusalimov.mrcalc.parse.SyntaxErrorException;
import com.abusalimov.mrcalc.parse.impl.antlr.ANTLRParserImpl;
import com.abusalimov.mrcalc.runtime.Runtime;
import com.abusalimov.mrcalc.runtime.RuntimeErrorException;
import com.abusalimov.mrcalc.runtime.impl.stream.StreamRuntime;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Facade class that runs on a separate thread.
 *
 * @author - Eldar Abusalimov
 */
public class CalcExecutor {
    private ExecutorService executor;

    /**
     * Compiles the source code and post an execution task.
     *
     * @param sourceCode           the source to compile and execute
     * @param outputStreamSupplier a factory for the output stream to print the execution results to
     * @throws CompileErrorException in case of compilation issues
     * @throws SyntaxErrorException  in case of compilation issues
     */
    public synchronized void execute(String sourceCode, Supplier<OutputStream> outputStreamSupplier)
            throws CompileErrorException, SyntaxErrorException {
        execute(sourceCode, outputStreamSupplier, null);
    }

    /**
     * Compiles the source code and post an execution task.
     *
     * @param sourceCode           the source to compile and execute
     * @param outputStreamSupplier a factory for the output stream to print the execution results to
     * @param diagnosticListener   listener to report runtime issues to
     * @throws CompileErrorException in case of compilation issues
     * @throws SyntaxErrorException  in case of compilation issues
     */
    public synchronized void execute(String sourceCode, Supplier<OutputStream> outputStreamSupplier,
                                     DiagnosticListener diagnosticListener)
            throws CompileErrorException, SyntaxErrorException {
        cancel();
        List<Stmt> stmts = compile(sourceCode);
        (executor = new ForkJoinPool()).submit(() -> run(stmts, outputStreamSupplier, diagnosticListener));
    }

    /**
     * Cancels all running executions, if any.
     */
    public synchronized void cancel() {
        executor.shutdownNow();
    }

    private List<Stmt> compile(String sourceCode) throws CompileErrorException, SyntaxErrorException {
        Parser parser = new ANTLRParserImpl();
        Backend backend = new BytebuddyBackendImpl();
        Compiler compiler = new Compiler(backend);
        ProgramNode node = parser.parse(sourceCode);
        return compiler.compile(node);
    }

    private void run(List<Stmt> stmts, Supplier<OutputStream> outputStreamSupplier,
                     DiagnosticListener diagnosticListener) {
        Runtime runtime = new StreamRuntime();
        Interpreter interpreter = new Interpreter(runtime);

        try (PrintStream printStream = new PrintStream(outputStreamSupplier.get())) {
            interpreter.setOutStream(printStream);
            for (Stmt stmt : stmts) {
                if (Thread.interrupted()) {
                    /*
                     * Generally, this can only happen when the runtime doesn't use a ForkJoin pool,
                     * thus doesn't support natural cancellation (StreamRuntime with parallel=false is an example).
                     */
                    throw new RuntimeErrorException(new Diagnostic(stmt.getLocation(),
                            new CancellationException().toString()));
                }
                interpreter.exec(stmt);
            }
        } catch (RuntimeErrorException e) {
            if (diagnosticListener != null) {
                e.getDiagnostics().forEach(diagnosticListener::report);
            }
        }
    }
}
