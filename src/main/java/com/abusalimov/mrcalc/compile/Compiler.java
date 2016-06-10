package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.ExprHolderNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.stmt.PrintStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.backend.ExprFactory;
import com.abusalimov.mrcalc.backend.impl.exprfunc.FuncExprFactoryImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractNodeDiagnosticEmitter {

    private final Map<String, Variable> globalVariableMap = new LinkedHashMap<>();

    private final TypeInferrer typeInferrer;
    private final ExprBuilder<?> exprBuilder;

    private int syntheticVariableCounter;

    public Compiler() {
        this(new FuncExprFactoryImpl());
    }

    public Compiler(ExprFactory<?> exprFactory) {
        typeInferrer = new TypeInferrer();
        exprBuilder = new ExprBuilder<>(exprFactory);
    }

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

    protected List<Stmt> compileProgram(ProgramNode node) {
        return node.getStmts().stream()
                .map(this::compileStmt)
                .collect(Collectors.toList());
    }

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

        Function<Object[], ?> exprFunction = exprTypeInfo.isComplete() ? buildExprFunction(exprTypeInfo) : null;

        List<Variable> inputVariables = exprTypeInfo.getReferencedVariables();
        Variable outputVariable = new Variable(outputVariableName, exprTypeInfo.getExprType());

        return new Stmt(exprFunction, inputVariables, outputVariable);

    }

    protected ExprTypeInfo inferTypeInfo(ExprHolderNode node) {
        return typeInferrer.inferWithDiagnosticListener(node, globalVariableMap, this::emitDiagnostic);
    }

    protected Function<Object[], ?> buildExprFunction(ExprTypeInfo exprTypeInfo) {
        if (!exprTypeInfo.isComplete()) {
            throw new IllegalArgumentException("!exprTypeInfo.isComplete()");
        }
        return exprBuilder.buildFunction(exprTypeInfo);
    }

    private String nextSyntheticVariableName() {
        return "$print" + (++syntheticVariableCounter);
    }

}
