package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.*;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.stmt.PrintStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.backend.*;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Type;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractNodeDiagnosticEmitter {

    private Map<String, Variable> globalVariableMap = new HashMap<>();
    private int syntheticVariableCounter;

    private Backend<?> backend;

    public Compiler(Backend<?> backend) {
        this.backend = backend;
    }

    public List<Stmt> compile(ProgramNode node) throws CompileErrorException {
        return compileOrThrow(() -> compileProgram(node));
    }

    public Stmt compile(StmtNode node) throws CompileErrorException {
        return compileOrThrow(() -> compileStmt(node));
    }

    private <R> R compileOrThrow(Supplier<R> function) throws CompileErrorException {
        try (DiagnosticCollectorCloseable<CompileErrorException> ignored =
                     collectDiagnosticsToThrow(CompileErrorException::new)) {
            return function.get();
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
                throw new UnsupportedOperationException();
            }
        }.visit(node);
    }

    private String nextSyntheticVariableName() {
        return "$print" + (++syntheticVariableCounter);
    }

    protected Stmt compileInternal(ExprHolderNode node, String outputVariableName) {
        ExprTypeInfo exprTypeInfo = inferTypeInfo(node);

        List<Variable> referencedVariables = getReferencedVariables(exprTypeInfo.getExprNode());
        Function<Object[], ?> exprFunction = buildExprFunction(exprTypeInfo, referencedVariables);

        Variable outputVariable = new Variable(outputVariableName, exprTypeInfo.getExprType());
        return new Stmt(exprFunction, referencedVariables, outputVariable);
    }

    private ExprTypeInfo inferTypeInfo(ExprHolderNode node) {
        ExprTypeInfo exprTypeInfo = new ExprTypeInfo(node, new HashMap<>(globalVariableMap));
        inferType(exprTypeInfo);
        return exprTypeInfo;
    }

    private Type inferType(ExprTypeInfo exprTypeInfo) {
        return new TypeInferrer().infer(exprTypeInfo, this::emitDiagnostic);
    }

    private List<Variable> getReferencedVariables(ExprNode node) {
        return getReferencedVariables(node, globalVariableMap);
    }

    private List<Variable> getReferencedVariables(ExprNode node,
                                                  Map<String, Variable> variableMap) {
        Set<Variable> variableSet = new LinkedHashSet<>();

        new NodeVisitor<Void>() {
            @Override
            public Void doVisit(VarRefNode node) {
                Variable variable = variableMap.get(node.getName());
                if (variable != null) {
                    variableSet.add(variable);
                }
                return null;
            }

            @Override
            public Void doVisit(LambdaNode node) {
                /* Do not visit children of lambda since it introduces a new scope. */
                return null;
            }
        }.visit(node);

        return new ArrayList<>(variableSet);
    }


    protected <E extends Expr> Function<Object[], ?> buildExprFunction(
            ExprTypeInfo exprTypeInfo, List<Variable> referencedVariables) {
        Backend<E> backend = getBackend();

        ObjectMath<Object, ? extends Expr, E> objectMath = backend.getObjectMath(Object.class);

        NumberMath<Long, E, E> integerMath = backend.getNumberMath(Long.class);
        NumberMath<Double, E, E> floatMath = backend.getNumberMath(Double.class);
        NumberCast<E, E> i2fCast= backend.getNumberCast(Double.class, Long.class);
        NumberCast<E, E> f2iCast = backend.getNumberCast(Long.class, Double.class);

        ObjectExprVisitor<Object, ? extends Expr, E> objectExprVisitor =
                new ObjectExprVisitor<>(objectMath, referencedVariables);

        ExprVisitor<Long, E> integerExprVisitor = new ExprVisitor<>(integerMath,
                referencedVariables);
        ExprVisitor<Double, E> floatExprVisitor = new ExprVisitor<>(floatMath,
                referencedVariables);

        Function<Node, E> visitInteger = integerExprVisitor::visit;
        Function<Node, E> visitFloat = floatExprVisitor::visit;

        Map<Primitive, Function<Node, E>> visitIntegerMap = new EnumMap<Primitive, Function<Node, E>>(
                Primitive.class) {{
            put(Primitive.INTEGER, visitInteger);
            put(Primitive.FLOAT, visitFloat.andThen(f2iCast::cast));
        }};

        Map<Primitive, Function<Node, E>> visitFloatMap = new EnumMap<Primitive, Function<Node, E>>(
                Primitive.class) {{
            put(Primitive.INTEGER, visitInteger.andThen(i2fCast::cast));
            put(Primitive.FLOAT, visitFloat);
        }};

        objectExprVisitor.setDelegate(node -> visitIntegerMap
                .get(exprTypeInfo.getExprType(node)).apply(node));
        integerExprVisitor.setDelegate(node -> visitIntegerMap
                .get(exprTypeInfo.getExprType(node)).apply(node));
        floatExprVisitor.setDelegate(node -> visitFloatMap
                .get(exprTypeInfo.getExprType(node)).apply(node));

        ExprNode rootNode = exprTypeInfo.getExprNode();
        Type rootType = exprTypeInfo.getExprType(rootNode);
        if (!(rootType instanceof Primitive)) {
            return objectExprVisitor.buildFunction(rootNode);
        }

        switch ((Primitive) rootType) {
            case INTEGER:
                return integerExprVisitor.buildFunction(rootNode);
            case FLOAT:
                return floatExprVisitor.buildFunction(rootNode);
            case UNKNOWN:
            default:
                return args -> {
                    throw new UnsupportedOperationException("Incomplete type");
                };
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Expr> Backend<E> getBackend() {
        return (Backend<E>) this.backend;
    }

}
