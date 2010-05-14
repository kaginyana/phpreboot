package com.googlecode.phpreboot.compiler;

import java.util.Iterator;
import java.util.List;

import com.googlecode.phpreboot.ast.ArrayEntry;
import com.googlecode.phpreboot.ast.ArrayValue;
import com.googlecode.phpreboot.ast.ArrayValueEntry;
import com.googlecode.phpreboot.ast.ArrayValueSingle;
import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.FuncallCall;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrAssign;
import com.googlecode.phpreboot.ast.InstrBlock;
import com.googlecode.phpreboot.ast.InstrDecl;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.InstrFuncall;
import com.googlecode.phpreboot.ast.InstrReturn;
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralArrayEntry;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.RT;

import static com.googlecode.phpreboot.compiler.LivenessType.*;

public class TypeChecker extends Visitor<Type, TypeCheckEnv, RuntimeException> {
  // ---
  
  public Type typeCheck(Node node, TypeCheckEnv env) {
    Type type = node.accept(this, env);
    node.setTypeAttribute(type);
    return type;
  }
  
  
  // --- helpers
  
  private Type typeCheckUnaryOp(Type type) {
    if (type == PrimitiveType.ANY ||  type == PrimitiveType.INT ||  type == PrimitiveType.DOUBLE)
      return type;
    throw RT.error("illegal type %s for unary expression", type);
  }
  
  private Type typeCheckBinaryOp(Type left, Type right) {
    if (left == PrimitiveType.ANY || right == PrimitiveType.ANY )
      return PrimitiveType.ANY;
    if (left == PrimitiveType.INT) {
      if (right == PrimitiveType.INT) {
        return PrimitiveType.INT;
      }
      if (right == PrimitiveType.DOUBLE) {
        return PrimitiveType.DOUBLE;
      }
    } else {
      if (left == PrimitiveType.DOUBLE) {
        if (right == PrimitiveType.INT || right == PrimitiveType.DOUBLE) {
          return PrimitiveType.DOUBLE;
        }
      }
    }
    throw RT.error("illegal types %s,%s for binary expression", left, right);
  }
  
  public static void isCompatible(Type type, Type exprType) {
    if (type == PrimitiveType.ANY)
      return;
    if (type == exprType)
      return;
    if (type == PrimitiveType.DOUBLE && exprType == PrimitiveType.INT)
      return;
    throw RT.error("incompatible type %s %s", type, exprType);
  }
  
  // --- default visit
  
  @Override
  protected Type visit(Node node, TypeCheckEnv env) {
    System.err.println("code is not compilable: "+node.getKind());
    throw CodeNotCompilableException.INSTANCE;
  }
  
  
  // --- visit instructions
  
  // for all instructions the visitor must return the liveness
  // of the next instruction. By example, visit(Return) should returns
  // that the next instruction is dead.
  // Convention: the liveness value can be LivenessType#ALIVE is the flow
  // is not stopped or any other value if the liveness is dead
  
  @Override
  public Type visit(Block block, TypeCheckEnv env) {
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(new LocalScope(env.getScope()), env.getFunctionReturnType(), env.getBindMap());
    Type liveness = LivenessType.ALIVE;
    
    for(Iterator<Instr> it = block.getInstrStar().iterator(); it.hasNext();) {
      Instr instr = it.next();
      if (liveness != LivenessType.ALIVE) {
        it.remove(); // remove dead code
        continue;
      }
      liveness = typeCheck(instr, typeCheckEnv);
    }
    return liveness;
  }
  
  @Override
  public Type visit(InstrBlock instr_block, TypeCheckEnv env) {
    return typeCheck(instr_block.getBlock(), env);
  }
  
  @Override
  public Type visit(InstrEcho instr_echo, TypeCheckEnv env) {
    typeCheck(instr_echo.getExpr(), env);
    return ALIVE;
  }
  
  @Override
  public Type visit(InstrReturn instr_return, TypeCheckEnv env) {
    Expr expr = instr_return.getExprOptional();
    Type exprType = (expr == null)? PrimitiveType.VOID: typeCheck(expr, env);
    
    Type functionReturnType = env.getFunctionReturnType();
    isCompatible(functionReturnType, exprType);
    return functionReturnType;    //HACK: it also means !Liveness.ALIVE 
  }
  
  @Override
  public Type visit(InstrDecl instr_decl, TypeCheckEnv env) {
    typeCheck(instr_decl.getDeclaration(), env);
    return ALIVE;
  }
  
  @Override
  public Type visit(InstrFuncall instr_funcall, TypeCheckEnv env) {
    typeCheck(instr_funcall.getFuncall(), env);
    return ALIVE;
  }
  
  @Override
  public Type visit(InstrAssign instr_assign, TypeCheckEnv env) {
    typeCheck(instr_assign.getAssignment(), env);
    return ALIVE;
  }
  
  
  @Override
  public Type visit(AssignmentId assignment_id, TypeCheckEnv env) {
    Type exprType = typeCheck(assignment_id.getExpr(), env);
    
    String name = assignment_id.getId().getValue();
    LocalScope scope = env.getScope();
    Var var = scope.lookup(name);
    if (var == null) {
      // auto-declaration
      var = new LocalVar(name, false, PrimitiveType.ANY, scope.nextSlot(PrimitiveType.ANY));
      scope.register(var);
    } else {
      
      if (var.isReadOnly())
        throw RT.error("try to assign a read only variable %s", name);
      
      isCompatible(var.getType(), exprType);
    }
    
    assignment_id.setSymbolAttribute((LocalVar)var);
    return var.getType();
  }
  
  
  // --- visit fun call
  
  @Override
  public Type visit(FuncallCall funcall_call, TypeCheckEnv env) {
    String name = funcall_call.getId().getValue();
    List<Expr> exprStar = funcall_call.getExprStar();
    Var var = env.getScope().lookup(name);
    if (var != null) {
      Object value = var.getValue();
      if (!(value instanceof Function)) {
        throw RT.error("variable %s doesn't reference a function: %s", name, value);
      }
      
      Function function = (Function)value;
      int size = exprStar.size();
      List<Parameter> parameters = function.getParameters();
      
      if (size != parameters.size()) {
        throw RT.error("argument number mismath with function %s", name);
      }
      
      for(int i=0; i<size; i++) {
        Type exprType = typeCheck(exprStar.get(i), env);
        isCompatible(parameters.get(i).getType(), exprType);
      }
      
      LocalVar localVar = env.getBindMap().bind(function, PrimitiveType.FUNCTION);
      funcall_call.setSymbolAttribute(localVar);
      return function.getReturnType();
    }
    
    throw CodeNotCompilableException.INSTANCE;
  }
  
  
  // --- visit primary
  
  @Override
  public Type visit(PrimaryFuncall primary_funcall, TypeCheckEnv env) {
    return typeCheck(primary_funcall.getFuncall(), env);
  }
  
  
  // --- visit expression
   
  @Override
  public Type visit(ExprId expr_id, TypeCheckEnv env) {
    String name = expr_id.getId().getValue();
    Var var = env.getScope().lookup(name);
    if (var == null) {
      throw RT.error("unknown variable %s", name);
    }
      
    Type type = var.getType();
    if (var instanceof LocalVar) {
      expr_id.setSymbolAttribute((LocalVar)var);
      return type;
    }
    
    // constants
    Object value = var.getValue();
    if (value instanceof Boolean) {
      expr_id.setSymbolAttribute(LocalVar.createConstantFoldable(value));
      return PrimitiveType.BOOLEAN;
    }
    if (value instanceof Integer) {
      expr_id.setSymbolAttribute(LocalVar.createConstantFoldable(value));
      return PrimitiveType.INT;
    }
    if (value instanceof Double) {
      expr_id.setSymbolAttribute(LocalVar.createConstantFoldable(value));
      return PrimitiveType.DOUBLE;
    }
    if (value instanceof String) {
      expr_id.setSymbolAttribute(LocalVar.createConstantFoldable(value));
      return PrimitiveType.STRING;
    }
    
    // bound constant
    LocalVar bindVar = env.getBindMap().bind(value, type);
    expr_id.setSymbolAttribute(bindVar);
    return type;
  }
  
  @Override
  public Type visit(ExprPrimary expr_primary, TypeCheckEnv env) {
    return typeCheck(expr_primary.getPrimary(), env);
  }
  
  @Override
  protected Type visit(Expr expr, TypeCheckEnv env) {
    List<Node> nodeList = expr.nodeList();
    Node node = nodeList.get(0);
    Type type = typeCheck(node, env); 
    ProductionEnum kind = expr.getKind();
    switch(kind) {
    case expr_unary_plus:
      return typeCheckUnaryOp(type);
    case expr_unary_minus:
      return typeCheckUnaryOp(type);
    case expr_unary_not:
      if (type != PrimitiveType.BOOLEAN && type != PrimitiveType.ANY)
        throw RT.error("incompatible type, unary not require a boolean or any");
      return PrimitiveType.BOOLEAN;
    default:
    }

    Node node2 = nodeList.get(1);
    Type left = type;
    Type right = typeCheck(node2, env);
    switch(kind) {
    case expr_plus:
      if (left == PrimitiveType.STRING || right == PrimitiveType.STRING)
        return PrimitiveType.STRING;
      return typeCheckBinaryOp(left, right);
    case expr_minus:
      return typeCheckBinaryOp(left, right);
    case expr_mult:
      return typeCheckBinaryOp(left, right);
    case expr_div:
      return typeCheckBinaryOp(left, right);
    case expr_mod:
      return typeCheckBinaryOp(left, right);

    case expr_eq:
    case expr_ne:
      return PrimitiveType.BOOLEAN;

    case expr_lt:
      return typeCheckBinaryOp(left, right);
    case expr_le:
      return typeCheckBinaryOp(left, right);
    case expr_gt:
      return typeCheckBinaryOp(left, right);
    case expr_ge:
      return typeCheckBinaryOp(left, right);

    default:
    }

    throw new AssertionError("unknown expression "+kind);
  }
  
  
  // --- literals
  
  @Override
  public Type visit(ExprLiteral primary_literal, TypeCheckEnv env) {
    return typeCheck(primary_literal.getLiteral(), env);
  }
  
  @Override
  public Type visit(LiteralSingle literal_single, TypeCheckEnv env) {
    return typeCheck(literal_single.getSingleLiteral(), env);
  }
  
  @Override
  public Type visit(LiteralBool literal_bool, TypeCheckEnv env) {
    return PrimitiveType.BOOLEAN;
  }
  
  @Override
  public Type visit(LiteralNull literal_null, TypeCheckEnv env) {
    return PrimitiveType.ANY;
  }
  
  @Override
  public Type visit(LiteralValue literal_value, TypeCheckEnv env) {
    Object value = literal_value.getValueLiteral().getValue();
    return (value instanceof Integer)? PrimitiveType.INT:PrimitiveType.DOUBLE;
  }
  
  @Override
  public Type visit(LiteralString literal_string, TypeCheckEnv env) {
    return PrimitiveType.STRING;
  }
  
  
  // --- array literal
  
  @Override
  public Type visit(LiteralArray literal_array, TypeCheckEnv env) {
    for(ArrayValue arrayValue: literal_array.getArrayValueStar()) {
      typeCheck(arrayValue, env);
    }
    return PrimitiveType.ARRAY;
  }
  
  @Override
  public Type visit(LiteralArrayEntry literal_array_entry, TypeCheckEnv env) {
    for(ArrayEntry arrayEntry: literal_array_entry.getArrayEntryStar()) {
      typeCheck(arrayEntry, env);
    }
    return PrimitiveType.ARRAY;
  }
  
  @Override
  public Type visit(ArrayEntry array_entry, TypeCheckEnv env) {
    typeCheck(array_entry.getExpr(), env);
    typeCheck(array_entry.getExpr2(), env);
    return null; // we don't care about this type
  }
  
  @Override
  public Type visit(ArrayValueSingle array_value_single, TypeCheckEnv env) {
    return typeCheck(array_value_single.getExpr(), env);
  }
  @Override
  public Type visit(ArrayValueEntry array_value_entry, TypeCheckEnv env) {
    return typeCheck(array_value_entry.getArrayEntry(), env);
  }
}
