package com.abusalimov.mrcalc;

import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.backend.impl.bytebuddy.BytebuddyBackendImpl;
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncBackendImpl;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Facade class that runs on a separate thread.
 *
 * @author - Eldar Abusalimov
 */
public class CalcExecutor {
    private ExecutorService executor;
    private BackendImplSwitch backendImplSwitch = BackendImplSwitch.DEFAULT;
    private boolean isParallel = true;
    private boolean executionInProgress;
    private List<Consumer<Boolean>> listeners = new LinkedList<>();

    public BackendImplSwitch getBackendImplSwitch() {
        return backendImplSwitch;
    }

    public void setBackendImplSwitch(BackendImplSwitch backendImplSwitch) {
        this.backendImplSwitch = Objects.requireNonNull(backendImplSwitch, "backendImplSwitch");
    }

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
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    private List<Stmt> compile(String sourceCode) throws CompileErrorException, SyntaxErrorException {
        Parser parser = new ANTLRParserImpl();
        Backend backend = backendImplSwitch.getBackend();
        Compiler compiler = new Compiler(backend);
        ProgramNode node = parser.parse(sourceCode);
        return compiler.compile(node);
    }

    private void run(List<Stmt> stmts, Supplier<OutputStream> outputStreamSupplier,
                     DiagnosticListener diagnosticListener) {
        Runtime runtime = new StreamRuntime(isParallel);
        Interpreter interpreter = new Interpreter(runtime);

        fireExecutionListeners(true);
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
        } finally {
            fireExecutionListeners(false);
        }
    }

    public synchronized void addAndFireExecutionListener(Consumer<Boolean> listener) {
        listeners.add(listener);
        listener.accept(executionInProgress);
    }

    protected synchronized void fireExecutionListeners(boolean executionInProgress) {
        this.executionInProgress = executionInProgress;
        listeners.forEach(listener -> listener.accept(executionInProgress));
    }

    public boolean isParallel() {
        return isParallel;
    }

    public void setParallel(boolean isParallel) {
        this.isParallel = isParallel;
    }

    /**
     * A switch for {@link Backend} implementations.
     */
    public enum BackendImplSwitch {
        BYTECODE("JVM Bytecode", new BytebuddyBackendImpl()),
        INTERPRETED("Interpreted", new FuncBackendImpl());

        public static final BackendImplSwitch DEFAULT = BYTECODE;

        private final String name;
        private final Backend backend;

        BackendImplSwitch(String name, Backend backend) {
            this.name = name;
            this.backend = backend;
        }

        public String getName() {
            return name;
        }

        public Backend getBackend() {
            return backend;
        }
    }
}
