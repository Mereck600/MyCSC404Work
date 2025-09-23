package lox;

import java.util.List;

import lox.Expr.Call;
import lox.Expr.Comma;
import lox.Expr.Ternary;
import lox.Stmt.Break;
import lox.Stmt.Function;

import java.util.ArrayList;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    
    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() { return 0; }

            @Override
            public Object call(Interpreter interpreter,
                                List<Object> arguments) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() { return "<native fn clock>"; }
        });

        globals.define("str", new LoxCallable() {
            @Override public int arity() { return 1; }
            @Override public Object call(Interpreter i, List<Object> args) {
                // since stringify handels null we dont have to worry
                return i.stringify(args.get(0));
            }
            @Override public String toString() { return "<native fn str>"; }
            });

        globals.define("exit", new LoxCallable() {
            @Override public int arity() { return 0; }
            @Override public Object call(Interpreter i, List<Object> args) {
                System.exit(0);
                return null; //need bc otherwise get error 
            }
            @Override public String toString() { return "<native fn exit>"; }
        });
        globals.define("type", new LoxCallable() {
            @Override public int arity() { return 1; }
            @Override public Object call(Interpreter i, List<Object> args) {
                Object v = args.get(0);
                String t;
                if (v == null) {
                    t = "nil";
                } else if (v instanceof Double) {
                    t = "number";
                } else if (v instanceof String) {
                    t = "string";
                } else if (v instanceof Boolean) {
                    t = "bool";
                } else if (v instanceof LoxCallable) {
                    t = "function";
                } else {
                    t = "object"; 
                }
                return t;
            }
            @Override public String toString() { return "<native fn type>"; }
            });



  }
    
    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }
    
    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }
        
        return evaluate(expr.right);
    }
    
    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        
        switch (expr.operator.type) {
            case BANG:
            return !isTruthy(right);
            case MINUS:
            checkNumberOperand(expr.operator, right);
            return -(double)right;
        }
        
        // Unreachable.
        return null;
    }
    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // if the variable is found, return it, but if it is null, then give an error.
        Object var = environment.get(expr.name);

        if (var != null) {
            return var;
        }

        throw new RuntimeError(expr.name, "Cannot access '" + expr.name.lexeme + "' before initialization.");
        // return environment.get(expr.name);
    }
    
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }
    
    private void checkNumberOperands(Token operator,
    Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    
    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }
    
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        
        return a.equals(b);
    }
    
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }
    
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }
    
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }
    
    void executeBlock(List<Stmt> statements,
    Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
    
    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

      @Override
        public Void visitFunctionStmt(Stmt.Function stmt) {
            LoxFunction function = new LoxFunction(stmt, environment);
            environment.define(stmt.name.lexeme, function);
            return null;
        }
            
    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }
    
    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

      @Override
        public Void visitReturnStmt(Stmt.Return stmt) {
            Object value = null;
            if (stmt.value != null) value = evaluate(stmt.value);

            throw new Return(value);
        }
    
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
         }else{
             value = Environment.UNINITIALIZED;
         }
        
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                    execute(stmt.body);
                } catch (BreakSignal b) {
                    break; //exit lloop
                }
        }
        return null;
    }
    
    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }
    
    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right); 
        
        switch (expr.operator.type) {
            case MINUS:
            checkNumberOperands(expr.operator, left, right);
            return (double)left - (double)right;
            case PLUS:
            if (left instanceof Double && right instanceof Double) {
                return (double)left + (double)right;
            } 
            
            if (left instanceof String && right instanceof String) {
                return (String)left + (String)right;
            }
            throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings.");
            case GREATER:
            checkNumberOperands(expr.operator, left, right);
            
            return (double)left > (double)right;
            case GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left >= (double)right;
            case LESS:
            checkNumberOperands(expr.operator, left, right);
            return (double)left < (double)right;
            case LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            
            case SLASH:
            checkNumberOperands(expr.operator, left, right);
            return (double)left / (double)right;
            case STAR:
            checkNumberOperands(expr.operator, left, right);
            return (double)left * (double)right;
        }
        
        // Unreachable.
        return null;
    }
    
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }
    
    public String stringify(Object object) { //i use this for the native and changed from private to public hope this right
        if (object == null) return "nil";
        
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        
        return object.toString();
    }
    private static class BreakSignal extends RuntimeException {
        BreakSignal() {
            super(null, null, false, false); // no message, no stack trace
        }
    }

    @Override
    public Object visitCommaExpr(Comma expr) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'visitCommaExpr'");
        evaluate(expr.left);
        return evaluate(expr.right);
    }

    @Override
    public Object visitTernaryExpr(Ternary expr) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'visitTernaryExpr'");
        Object condition = evaluate(expr.condition);
        if (isTruthy(condition)) {
            return evaluate(expr.thenBranch);
        } else {
            return evaluate(expr.elseBranch);
        }
        }

    @Override
    public Void visitBreakStmt(Break stmt) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'visitBreakStmt'");
        throw new BreakSignal();
    }

      @Override
        public Object visitCallExpr(Expr.Call expr) {
            Object callee = evaluate(expr.callee);

            List<Object> arguments = new ArrayList<>();
            for (Expr argument : expr.arguments) { 
                arguments.add(evaluate(argument));
            }
            LoxCallable function = (LoxCallable)callee;
            if (!(callee instanceof LoxCallable)) {
                throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
                }

            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
                }


            
            return function.call(this, arguments);
        }

      
    
    
    
}