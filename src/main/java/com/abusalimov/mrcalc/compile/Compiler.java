package com.abusalimov.mrcalc.compile;

import com.abusalimov.mrcalc.ast.LambdaNode;
import com.abusalimov.mrcalc.ast.Node;
import com.abusalimov.mrcalc.ast.NodeVisitor;
import com.abusalimov.mrcalc.ast.ProgramNode;
import com.abusalimov.mrcalc.ast.expr.ExprNode;
import com.abusalimov.mrcalc.ast.expr.VarRefNode;
import com.abusalimov.mrcalc.ast.stmt.PrintStmtNode;
import com.abusalimov.mrcalc.ast.stmt.StmtNode;
import com.abusalimov.mrcalc.ast.stmt.VarDefStmtNode;
import com.abusalimov.mrcalc.compile.exprtree.*;
import com.abusalimov.mrcalc.compile.impl.function.FuncExprBuilderFactoryImpl;
import com.abusalimov.mrcalc.compile.type.Primitive;
import com.abusalimov.mrcalc.compile.type.Type;
import com.abusalimov.mrcalc.diagnostic.Diagnostic;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


/**
 * @author Eldar Abusalimov
 */
public class Compiler extends AbstractNodeDiagnosticEmitter {

    private Map<String, Variable> variableMap = new HashMap<>();
    private Map<ExprNode, Type> exprTypeMap = new HashMap<>();
    private int syntheticVariableCounter;

    private ExprBuilderFactory<?, ?> exprBuilderFactory;

    public Compiler() {
        this(new FuncExprBuilderFactoryImpl());
    }

    public Compiler(ExprBuilderFactory<?, ?> exprBuilderFactory) {
        this.exprBuilderFactory = exprBuilderFactory;
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
                ExprNode expr = node.getExpr();
                /*
                 * Need to visit the value prior to defining a variable in the scope in order
                 * to forbid self-recursive variable references from within the definition:
                 *
                 *   var r = r  # error
                 */
                Stmt stmt = compileInternal(expr, name);

                if (variableMap.containsKey(name)) {
                    emitNodeDiagnostic(node,
                            String.format("Variable '%s' is already defined", name));
                } else {
                    variableMap.put(name, stmt.getOutputVariable());
                }

                return stmt;
            }

            @Override
            public Stmt doVisit(PrintStmtNode node) {
                return compileInternal(node.getExpr(), nextSyntheticVariableName());
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

    protected Stmt compileInternal(ExprNode rootNode, String outputVariableName) {
        Type type = inferType(rootNode);

        List<Variable> variables = getReferencedVariables(rootNode);
        Function<Object[], ?> exprFunction = buildExprFunction(rootNode, variables);
        Variable outputVariable = new Variable(outputVariableName, type);
        return new Stmt(exprFunction, variables, outputVariable);
    }

    private Type inferType(ExprNode node) {
        return inferType(node, variableMap, exprTypeMap);
    }

    private Type inferType(ExprNode node,
                           Map<String, Variable> variableMap,
                           Map<ExprNode, Type> exprTypeMap) {
        TypeInferrer typeInferrer = new TypeInferrer(variableMap) {
            @Override
            public Type visit(Node node) {
                Type type = super.visit(node);
                exprTypeMap.put((ExprNode) node, Objects.requireNonNull(type));
                return type;
            }
        };
        return typeInferrer.runWithDiagnosticListener(() -> typeInferrer.visit(node),
                this::emitDiagnostic);
    }

    private List<Variable> getReferencedVariables(ExprNode node) {
        return getReferencedVariables(node, variableMap);
    }

    private List<Variable> getReferencedVariables(ExprNode node, Map<String, Variable> variableMap) {
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


    protected <I extends Expr<Long>, F extends Expr<Double>> Function<Object[], ?> buildExprFunction(
            ExprNode rootNode, List<Variable> variables) {
        ExprBuilderFactory<I, F> factory = getExprBuilderFactory();

        ObjectOpBuilder<Object, ? extends Expr<Object>, I> objectOpBuilder = factory
                .createObjectOpBuilder();

        PrimitiveOpBuilder<Long, I> integerOpBuilder = factory.createIntegerOpBuilder();
        PrimitiveOpBuilder<Double, F> floatOpBuilder = factory.createFloatOpBuilder();
        PrimitiveCastBuilder<I, F> primitiveCastBuilder = factory.createPrimitiveCastBuilder();

        ObjectExprVisitor<Object, ? extends Expr<Object>, I> objectExprVisitor =
                new ObjectExprVisitor<>(objectOpBuilder, variables);

        ExprVisitor<Long, I> integerExprVisitor = new ExprVisitor<>(integerOpBuilder, variables);
        ExprVisitor<Double, F> floatExprVisitor = new ExprVisitor<>(floatOpBuilder, variables);

        Function<Node, I> visitInteger = integerExprVisitor::visit;
        Function<Node, F> visitFloat = floatExprVisitor::visit;

        Map<Primitive, Function<Node, I>> visitIntegerMap = new EnumMap<Primitive, Function<Node, I>>(
                Primitive.class) {{
            put(Primitive.INTEGER, visitInteger);
            put(Primitive.FLOAT, visitFloat.andThen(primitiveCastBuilder::toInteger));
        }};

        Map<Primitive, Function<Node, F>> visitFloatMap = new EnumMap<Primitive, Function<Node, F>>(
                Primitive.class) {{
            put(Primitive.INTEGER, visitInteger.andThen(primitiveCastBuilder::toFloat));
            put(Primitive.FLOAT, visitFloat);
        }};

        objectExprVisitor.setDelegate(node -> visitIntegerMap.get(getNodeType(node)).apply(node));
        integerExprVisitor.setDelegate(node -> visitIntegerMap.get(getNodeType(node)).apply(node));
        floatExprVisitor.setDelegate(node -> visitFloatMap.get(getNodeType(node)).apply(node));

        Type rootType = getNodeType(rootNode);
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
    private <I extends Expr<Long>, F extends Expr<Double>> ExprBuilderFactory<I, F> getExprBuilderFactory() {
        return (ExprBuilderFactory<I, F>) this.exprBuilderFactory;
    }

    private Type getNodeType(ExprNode node) {
        return exprTypeMap.getOrDefault(node, Primitive.UNKNOWN);
    }

}
