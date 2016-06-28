package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.stmt.PrintStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.backend.Backend;
import com.abusalimov.mrcalc.runtime.Evaluable;
import com.abusalimov.mrcalc.runtime.Runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Compiler plays the essential role in transforming source code into a runnable function.
 * <p>
 * Compiler operates on statements level: it takes AST {@link StmtNode}s and turns them into {@link Stmt} instances. It
 * compiles statements in two phases:
 * <p>
 * <li> First, it invokes {@link TypeInferrer#infer(ExprHolderNode, Map)} that fills in an {@link ExprTypeInfo} instance
 * with a proper {@link com.abusalimov.mrcalc.compile.type.Type} of each sub-expression of the statement. The
 * TypeInferrer is also responsible for reporting all the possible type errors that get emitted by the compiler.
 * <p>
 * <li> Next, it calls {@link ExprBuilder#buildFunction(ExprTypeInfo)} which, in turn, based on the inferred type of
 * each expression, delegates to a proper {@code backend} expression builder to compose the expression into a callable.
 * <p>
 * A Compiler instance is stateful: it maintains and preserves a list of defined {@link Variable}s so that one can refer
 * to a variable defined through another call to the {@link #compile(ProgramNode)} method.
 *
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractNodeDiagnosticEmitter {

    private final Map<String, Variable> globalVariableMap = new LinkedHashMap<>();

    private final TypeInferrer typeInferrer;
    private final ExprBuilder exprBuilder;

    private int syntheticVariableCounter;

    /**
     * Creates a new Compiler instance that will use the specified backend to compose the resulting callable.
     *
     * @param backend the backend implementation
     */
    public Compiler(Backend<?, ?> backend) {
        typeInferrer = new TypeInferrer();
        exprBuilder = new ExprBuilder<>(backend);
    }

    /**
     * Compiles an AST root into a list of {@link Stmt#exec(Runtime, Map) executable} statements.
     * <p>
     * Calling this method is almost identical to invoking {@link #compile(StmtNode)} multiple times, except that all
     * diagnostics reported, if any, are composed into a single {@link CompileErrorException exception} instance.
     *
     * @param node the AST root node
     * @return the list of {@link Stmt statements}
     * @throws CompileErrorException in case the AST has semantic errors, like type mismatch
     */
    public List<Stmt> compile(ProgramNode node) throws CompileErrorException {
        try (DiagnosticCollectorCloseable<CompileErrorException> diagnosticsToThrow =
                     collectDiagnosticsToThrow(CompileErrorException::new)) {
            List<Stmt> stmts = compileProgram(node);
            if (!stmts.stream().allMatch(Stmt::isComplete)) {
                throw diagnosticsToThrow.createException();
            }
            return stmts;
        }
    }

    /**
     * Compiles a single statement.
     *
     * @param node the statement AST node to compile
     * @return a compiled {@link Stmt statement}
     * @throws CompileErrorException in case of semantic errors
     * @see #compile(ProgramNode)
     */
    public Stmt compile(StmtNode node) throws CompileErrorException {
        try (DiagnosticCollectorCloseable<CompileErrorException> diagnosticsToThrow =
                     collectDiagnosticsToThrow(CompileErrorException::new)) {
            Stmt stmt = compileStmt(node);
            if (!stmt.isComplete()) {
                throw diagnosticsToThrow.createException();
            }
            return stmt;
        }
    }

    /**
     * Naked version of {@link #compile(ProgramNode)} that doesn't throw errors.
     *
     * @param node a {@link ProgramNode root} node to compile
     * @return a list of compiled statements, some of that may be {@link Stmt#isComplete() incomplete} (in case of
     * diagnostic errors)
     */
    protected List<Stmt> compileProgram(ProgramNode node) {
        return node.getStmts().stream()
                .map(this::compileStmt)
                .collect(Collectors.toList());
    }

    /**
     * Naked version of {@link #compile(StmtNode)} that doesn't throw errors.
     *
     * @param node a single {@link StmtNode statement} node to compile
     * @return a compiled statement, {@link Stmt#isComplete() incomplete} in case of diagnostic errors
     */
    protected Stmt compileStmt(StmtNode node) {
        return new NodeVisitor<Stmt>() {
            @Override
            public Stmt doVisit(VarDefStmtNode node) {
                String name = node.getName();
                /*
                 * Need to visit the value prior to defining a variable in the scope in order
                 * to forbid self-recursive variable references from within the definition:
                 *
                 *   var r = r  # error
                 */
                Stmt stmt = compileInternal(node, name);

                if (globalVariableMap.containsKey(name)) {
                    emitNodeDiagnostic(node,
                            String.format("Variable '%s' is already defined", name));
                } else {
                    globalVariableMap.put(name, stmt.getOutputVariable());
                }

                return stmt;
            }

            @Override
            public Stmt doVisit(PrintStmtNode node) {
                return compileInternal(node, nextSyntheticVariableName());
            }

            @Override
            public Stmt doVisit(Node node) {
                throw new UnsupportedOperationException("Statements only");
            }
        }.visit(node);
    }

    protected Stmt compileInternal(ExprHolderNode node, String outputVariableName) {
        ExprTypeInfo exprTypeInfo = inferTypeInfo(node);

        Evaluable<?> exprFunction = exprTypeInfo.isComplete() ? buildExprFunction(exprTypeInfo) : null;

        List<Variable> inputVariables = exprTypeInfo.getReferencedVariables();
        Variable outputVariable = new Variable(outputVariableName, exprTypeInfo.getExprType());

        return new Stmt(exprFunction, inputVariables, outputVariable, node.getExpr().getLocation());
    }

    /**
     * Invokes the {@link TypeInferrer type inferrer} setting it up to pass its diagnostics through this instance.
     *
     * @param node a node holding an expression to infer types for; usually some {@link StmtNode statement}
     * @return an {@link ExprTypeInfo} instance filled in, {@link ExprTypeInfo#isComplete() incomplete} in case of type
     * errors
     */
    protected ExprTypeInfo inferTypeInfo(ExprHolderNode node) {
        return typeInferrer.infer(node, globalVariableMap, this::emitDiagnostic);
    }

    /**
     * Given a {@link ExprTypeInfo#isComplete() complete} {@link ExprTypeInfo} instance, invokes the {@link ExprBuilder
     * expression builder} that assembles the expression into a callable function.
     *
     * @param exprTypeInfo the expression type info
     * @return a callable
     * @throws IllegalArgumentException if the exprTypeInfo is {@link ExprTypeInfo#isComplete() incomplete}
     */
    protected Evaluable<?> buildExprFunction(ExprTypeInfo exprTypeInfo) {
        return exprBuilder.buildFunction(exprTypeInfo);
    }

    private String nextSyntheticVariableName() {
        return "$print" + (++syntheticVariableCounter);
    }

}
