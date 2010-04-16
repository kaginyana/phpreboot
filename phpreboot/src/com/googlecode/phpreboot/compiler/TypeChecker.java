package com.googlecode.phpreboot.compiler;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrBlock;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.Parameter;
import com.googlecode.phpreboot.ast.ParameterTyped;
import com.googlecode.phpreboot.ast.PrimaryLiteral;
import com.googlecode.phpreboot.ast.Signature;
import com.googlecode.phpreboot.ast.TypeAny;
import com.googlecode.phpreboot.ast.TypeBoolean;
import com.googlecode.phpreboot.ast.TypeDouble;
import com.googlecode.phpreboot.ast.TypeInt;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.LocalVar;
import com.googlecode.phpreboot.model.Symbol;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.RT;

public class TypeChecker extends Visitor<Type, TypeCheckEnv, RuntimeException> {
  
  public void typeCheck(Fun functionNode, Scope scope) {
    Signature signature = functionNode.getSignature();
    
    com.googlecode.phpreboot.ast.Type typeOptional = signature.getTypeOptional();
    Type returnType = (typeOptional !=null)? asType(typeOptional): PrimitiveType.VOID;
    
    TypeCheckEnv env = new TypeCheckEnv(new Scope(scope), returnType);
    
    ArrayList<Type> parametertypes = new ArrayList<Type>();
    for(Parameter parameter: signature.getParameterStar()) {
      Type type = (parameter instanceof ParameterTyped)?
          asType(((ParameterTyped)parameter).getType()):
          PrimitiveType.ANY;
      parametertypes.add(type);
    }
    
    typeCheck(functionNode.getBlock(), env);
  }
  
  
  // --- ast type node to type conversions
  
  public static Type asType(com.googlecode.phpreboot.ast.Type typeNode) {
    return typeNode.accept(TYPE_VISITOR, null);
  }
  private static Visitor<Type, Void, RuntimeException> TYPE_VISITOR =
    new Visitor<Type, Void, RuntimeException>() {
    
    @Override
    public Type visit(TypeAny type_any, Void unused) {
      return PrimitiveType.ANY; 
    }
    @Override
    public Type visit(TypeBoolean type_boolean, Void unused) {
      return PrimitiveType.BOOLEAN; 
    }
    @Override
    public Type visit(TypeInt type_int, Void unused) {
      return PrimitiveType.INT; 
    }
    @Override
    public Type visit(TypeDouble type_double, Void unused) {
      return PrimitiveType.DOUBLE; 
    }
  };
  
  // ---
  
  public static Function createFunction(Fun fun) {
    Signature signature = fun.getSignature();
    String name = signature.getId().getValue();
    com.googlecode.phpreboot.ast.Type typeOptional = signature.getTypeOptional();
    Type returnType = (typeOptional !=null)? asType(typeOptional): PrimitiveType.VOID;
    
    ArrayList<Type> parameterTypes = new ArrayList<Type>();
    for(Parameter parameter: signature.getParameterStar()) {
      Type type = (parameter instanceof ParameterTyped)?
          asType(((ParameterTyped)parameter).getType()):
          PrimitiveType.ANY;
      parameterTypes.add(type);
    }
    
    Function function = new Function(name, returnType, parameterTypes, fun);
    return function;
  }
  
  
  // ---
  
  private Type typeCheck(Node node, TypeCheckEnv env) {
    Type type = node.accept(this, env);
    node.setTypeAttribute(type);
    return type;
  }
  
  
  // --- helpers
  
  private void checksIn(Type type, Type expectedType) {
    if (type == PrimitiveType.ANY || type == expectedType)
      return;
    throw RT.error("illegal type %s for expression", type);
  }
  
  private void checksIn(Type type, Type expectedType1, Type expectedType2) {
    if (type == PrimitiveType.ANY || type == expectedType1 || type == expectedType2)
      return;
    throw RT.error("illegal type %s for expression", type);
  }
  
  private Type typeCheckBinaryOp(Type left, Type right) {
    checksIn(left, PrimitiveType.INT, PrimitiveType.DOUBLE);
    checksIn(right, PrimitiveType.INT, PrimitiveType.DOUBLE);
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
  
  
  // --- visit instruction
  
  
  
  @Override
  public Type visit(Block block, TypeCheckEnv env) {
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(new Scope(env.getScope()), env.getDeclaredReturnType());
    for(Instr instr: block.getInstrStar()) {
      typeCheck(instr, typeCheckEnv);
    }
    return null;
  }
  
  @Override
  public Type visit(InstrBlock instr_block, TypeCheckEnv env) {
    return typeCheck(instr_block.getBlock(), env);
  }
  
  @Override
  public Type visit(InstrEcho instr_echo, TypeCheckEnv env) {
    typeCheck(instr_echo.getExpr(), env);
    return null;
  }
  
  @Override
  public Type visit(AssignmentId assignment_id, TypeCheckEnv env) {
    String name = assignment_id.getId().getValue();
    Scope scope = env.getScope();
    Symbol symbol = scope.lookup(name);
    Type varType;
    if (symbol == null) {
      // auto-declaration
      LocalVar var = new LocalVar(name, PrimitiveType.ANY);
      scope.register(var);
      varType = PrimitiveType.ANY;
    } else {
      varType = symbol.getType();
    }
    
    Type type = typeCheck(assignment_id.getExpr(), env);
    isCompatible(varType, type);
    return null;
  }
  
  
  // --- visit expression
   
  @Override
  public Type visit(ExprId expr_id, TypeCheckEnv env) {
    String name = expr_id.getId().getValue();
    Symbol var = env.getScope().lookup(name);
    if (var != null)
      return var.getType();
    throw RT.error("unknown variable %s", name);
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
      checksIn(type, PrimitiveType.INT, PrimitiveType.DOUBLE);
      return type;
    case expr_unary_minus:
      checksIn(type, PrimitiveType.INT, PrimitiveType.DOUBLE);
      return type;
    case expr_unary_not:
      checksIn(type, PrimitiveType.BOOLEAN);
      return PrimitiveType.BOOLEAN;
    default:
    }

    Node node2 = nodeList.get(1);
    Type left = type;
    Type right = typeCheck(node2, env);
    switch(kind) {
    case expr_plus:
      return typeCheckBinaryOp(left, right);
    case expr_minus:
      return typeCheckBinaryOp(left, right);
    case expr_mult:
      return typeCheckBinaryOp(left, right);
    case expr_div:
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
  public Type visit(PrimaryLiteral primary_literal, TypeCheckEnv env) {
    return typeCheck(primary_literal.getLiteral(), env);
  }
  
  @Override
  public Type visit(LiteralBool literal_bool, TypeCheckEnv env) {
    return PrimitiveType.BOOLEAN;
  }
  
  @Override
  public Type visit(LiteralNull literal_null, TypeCheckEnv env) {
    return PrimitiveType.NULL;
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
}
