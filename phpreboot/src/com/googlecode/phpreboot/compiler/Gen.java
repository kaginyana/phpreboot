package com.googlecode.phpreboot.compiler;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC;
import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC_OWNER;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.FuncallCall;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrBlock;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.InstrReturn;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public class Gen extends Visitor<Type, GenEnv, RuntimeException> {
  private final MethodVisitor mv;
  
  public Gen(MethodVisitor mv) {
    this.mv = mv;
  }
  
  
  
  
  public Type gen(Node node, GenEnv env) {
    return node.accept(this, env);
  }
  
  
  // --- helpers
  
  private void indy(String name, Type returnType, Type type) {
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, name,
        org.objectweb.asm.Type.getMethodDescriptor(asASMType(returnType),
            new org.objectweb.asm.Type[]{asASMType(type)}));
  }
  
  private void indy(String name, Type returnType, Type type1, Type type2) {
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, name,
        org.objectweb.asm.Type.getMethodDescriptor(asASMType(returnType),
            new org.objectweb.asm.Type[]{asASMType(type1), asASMType(type2)}));
  }
  
  // --- ASM helper
  
  private static org.objectweb.asm.Type asASMType(Type type) {
    if (type instanceof PrimitiveType) {
      switch((PrimitiveType)type) {
      case ANY:
        return ASM_ANY_TYPE;
      case VOID:
        return org.objectweb.asm.Type.VOID_TYPE;
      case BOOLEAN:
        return org.objectweb.asm.Type.BOOLEAN_TYPE;
      case INT:
        return org.objectweb.asm.Type.INT_TYPE;
      case DOUBLE:
        return org.objectweb.asm.Type.DOUBLE_TYPE;
      case STRING:
        return ASM_STRING_TYPE;
      case ARRAY:
        return ASM_ARRAY_TYPE;
      case SEQUENCE:
        return ASM_SEQUENCE_TYPE;
      case XML:
        return ASM_XML_TYPE;
      case URI:
        return ASM_URI_TYPE;
      default:
        throw new AssertionError("unknown primitive type "+type);
      }
    } else {
      return org.objectweb.asm.Type.VOID_TYPE;
    }
  }
  
  private static final org.objectweb.asm.Type ASM_ANY_TYPE = org.objectweb.asm.Type.getType(Object.class);
  private static final org.objectweb.asm.Type ASM_STRING_TYPE = org.objectweb.asm.Type.getType(String.class);
  private static final org.objectweb.asm.Type ASM_ARRAY_TYPE = org.objectweb.asm.Type.getType(Array.class);
  private static final org.objectweb.asm.Type ASM_SEQUENCE_TYPE = org.objectweb.asm.Type.getType(Sequence.class);
  private static final org.objectweb.asm.Type ASM_XML_TYPE = org.objectweb.asm.Type.getType(XML.class);
  private static final org.objectweb.asm.Type ASM_URI_TYPE = org.objectweb.asm.Type.getType(URI.class);

  
  private void insertCast(Type type, Type exprType) {
    if (type == exprType)
      return;
    
    if (type == PrimitiveType.DOUBLE && exprType == PrimitiveType.INT) {
      mv.visitInsn(I2D);
    }
    
    // boxing
    if (type == PrimitiveType.ANY) {
      switch((PrimitiveType)exprType) {
      case BOOLEAN:
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
        return;
      case INT:
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
        return;
      case DOUBLE:
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
        return;
      default:
      }
      return;
    }
    
    // unboxing
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, "*checkcast",
          '('+asASMType(exprType).getDescriptor()+')'+asASMType(type).getDescriptor());
  }
  
  // --- visit instructions
  
  @Override
  public Type visit(Block block, GenEnv env) {
    for(Instr instr: block.getInstrStar()) {
      gen(instr, env);
    }
    return null;
  }
  
  @Override
  public Type visit(InstrBlock instr_block, GenEnv env) {
    return gen(instr_block.getBlock(), env);
  }
  
  @Override
  public Type visit(InstrEcho instr_echo, GenEnv env) {
    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    Type type = gen(instr_echo.getExpr(), env);
    org.objectweb.asm.Type asmType = asASMType(type);
    if (asmType.getSort() == org.objectweb.asm.Type.OBJECT)
      asmType = ASM_ANY_TYPE;
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", '('+asmType.getDescriptor()+")V");
    return null;
  }
  
  @Override
  public Type visit(InstrReturn instr_return, GenEnv env) {
    Type type = instr_return.getTypeAttribute();
    
    Expr expr = instr_return.getExprOptional();
    if (expr != null) {
      gen(expr, env);
      insertCast(type, expr.getTypeAttribute());
    }
    
    mv.visitInsn(asASMType(type).getOpcode(IRETURN));
    return null;
  }
  
  @Override
  public Type visit(AssignmentId assignment_id, GenEnv env) {
    Type type = gen(assignment_id.getExpr(), env);
    LocalVar var = assignment_id.getSymbolAttribute();
    insertCast(var.getType(), type);
    mv.visitVarInsn(asASMType(var.getType()).getOpcode(ISTORE), var.getSlot(env.getShift()));
    return null;
  }
  
  
// --- visit fun call
  
  @Override
  public Type visit(FuncallCall funcall_call, GenEnv env) {
    LocalVar localVar = funcall_call.getSymbolAttribute();
    Function function = (Function)localVar.getValue();
    
    mv.visitVarInsn(ALOAD, localVar.getSlot(0));
    mv.visitMethodInsn(INVOKEVIRTUAL, Function.class.getName().replace('.', '/'), "getMethodHandle", "()Ljava/dyn/MethodHandle;");
    
    mv.visitVarInsn(ALOAD, env.getShift()); // environment
    
    List<Expr> exprStar = funcall_call.getExprStar();
    List<Parameter> parameters = function.getParameters();
    StringBuilder desc = new StringBuilder().append("(L").append(/*EvalEnv.class*/Object.class.getName().replace('.', '/')).append(';');
    for(int i=0; i<exprStar.size(); i++) {
      Expr expr = exprStar.get(i);
      Type expectedType = parameters.get(i).getType();
      Type exprType = gen(expr, new GenEnv(env.getShift(), expectedType));
      insertCast(expectedType, exprType);
      desc.append(asASMType(expectedType).getDescriptor());
    }
    
    desc.append(')');
    Type returnType = function.getReturnType();
    if (returnType == PrimitiveType.ANY) {
      returnType = env.getExpectedType();
    }
    desc.append(asASMType(returnType).getDescriptor());
    
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/dyn/MethodHandle", /*"invokeExact"*/ "invoke", desc.toString());
    
    return returnType;
  }
  
  
  // --- visit primary
  
  @Override
  public Type visit(PrimaryFuncall primary_funcall, GenEnv env) {
    return gen(primary_funcall.getFuncall(), env);
  }
  
  // --- visit expression
   
  @Override
  public Type visit(ExprId expr_id, GenEnv env) {
    LocalVar localVar = expr_id.getSymbolAttribute();
    if (!localVar.isConstant()) {
      Type type = localVar.getType();
      mv.visitVarInsn(asASMType(type).getOpcode(ILOAD), localVar.getSlot(env.getShift())); 
      return type;
    }
    
    // bound constants
    if (!localVar.isConstantFoldable()) {
      mv.visitVarInsn(asASMType(localVar.getType()).getOpcode(ILOAD), localVar.getSlot(0)); 
      return localVar.getType();
    }
    
    Object value = localVar.getValue();
    PrimitiveType type = (PrimitiveType)expr_id.getTypeAttribute();
    switch(type) {
    case BOOLEAN:
      mv.visitInsn(((Boolean)value)? ICONST_1 :ICONST_0);
      break;
    case INT:
    case DOUBLE:
    case STRING:
      mv.visitLdcInsn(value);
      break; 
    default:
      throw new AssertionError("unknown primitive type "+type);
    }
    return type;
  }
  
  @Override
  public Type visit(ExprPrimary expr_primary, GenEnv env) {
    return gen(expr_primary.getPrimary(), env);
  }
  
  private Type visitUnaryOp(String opName, int opcode, Node exprNode, Type type, GenEnv env) {
    Type exprType = gen(exprNode, env.expectedType(PrimitiveType.ANY));
    
    if (exprType == PrimitiveType.ANY) {
      Type expectedType = env.getExpectedType();
      indy(opName, expectedType, exprType);
      return expectedType;
    } else {
      mv.visitInsn(asASMType(type).getOpcode(opcode));
      return type;
    }
  }
  
  private Type visitBinaryOp(String opName, int opcode, Node leftNode, Node rightNode, Type type, GenEnv env) {
    Type expectedType = env.getExpectedType();
    GenEnv newEnv = env.expectedType(PrimitiveType.ANY);
    
    insertCast(type, leftNode.getTypeAttribute());
    Type left = gen(leftNode, newEnv);
    insertCast(type, rightNode.getTypeAttribute());
    Type right = gen(rightNode, newEnv);
    
    if (left == PrimitiveType.ANY || right == PrimitiveType.ANY) {
      indy(opName, expectedType, left, right);
      return expectedType;
    } else {
      mv.visitInsn(asASMType(type).getOpcode(opcode));
      return type;
    }
  }
  
  private Type visitBinaryTest(String opName, int opcode, Node leftNode, Node rightNode, GenEnv env) {
    env = env.expectedType(PrimitiveType.ANY);
    Type left = gen(leftNode, env);
    Type right = gen(rightNode, env);
    
    if (left == PrimitiveType.ANY || right == PrimitiveType.ANY) {
      indy(opName, PrimitiveType.BOOLEAN, left, right);
    } else {
      Label truePart = new Label();
      Label end = new Label();
      mv.visitJumpInsn(opcode, truePart);
      mv.visitInsn(ICONST_0);
      mv.visitJumpInsn(GOTO, end);
      mv.visitLabel(truePart);
      mv.visitInsn(ICONST_1);
      mv.visitLabel(end);
    }
    return PrimitiveType.BOOLEAN;
  }
  
  
  @Override
  protected Type visit(Expr expr, GenEnv env) {
    Type type = expr.getTypeAttribute();
    
    List<Node> nodeList = expr.nodeList();
    Node unaryNode = nodeList.get(0);
    ProductionEnum kind = expr.getKind();
    switch(kind) {
    case expr_unary_plus:
      return type;
      
    case expr_unary_minus:
      return visitUnaryOp("-", INEG, unaryNode, type, env);
    
    case expr_unary_not:
      /*
      if (type == PrimitiveType.ANY) {
        indy("unary_not", joinReturnType(env.getExpectedType(), PrimitiveType.BOOLEAN), type);
      } else {
        mv.visitInsn(INOT);
      }
      return null;
      */
      throw new UnsupportedOperationException("NYI");
    default:
    }

    Node binaryNode = nodeList.get(1);
    switch(kind) {
    case expr_plus:
      return visitBinaryOp("plus", IADD, unaryNode, binaryNode, type, env);
    case expr_minus:
      return visitBinaryOp("minus", ISUB, unaryNode, binaryNode, type, env);
    case expr_mult:
      return visitBinaryOp("mult", IMUL, unaryNode, binaryNode, type, env);
    case expr_div:
      return visitBinaryOp("div", IDIV, unaryNode, binaryNode, type, env);

    case expr_lt:
      return visitBinaryTest("lt", IF_ICMPLT, unaryNode, binaryNode, env);
    case expr_le:
      return visitBinaryTest("le", IF_ICMPLE, unaryNode, binaryNode, env);
    case expr_gt:
      return visitBinaryTest("gt", IF_ICMPGT, unaryNode, binaryNode, env);
    case expr_ge:
      return visitBinaryTest("ge", IF_ICMPGE, unaryNode, binaryNode, env);

    default:
    }

    throw new AssertionError("unknown expression "+kind);
  }
  
  
  // --- literals
  
  @Override
  public Type visit(ExprLiteral primary_literal, GenEnv env) {
    return gen(primary_literal.getLiteral(), env);
  }
  
  @Override
  public Type visit(LiteralSingle literal_single, GenEnv env) {
    return gen(literal_single.getSingleLiteral(), env);
  }
  
  @Override
  public Type visit(LiteralBool literal_bool, GenEnv env) {
    boolean value = literal_bool.getBoolLiteral().getValue();
    mv.visitInsn((value)? ICONST_1 :ICONST_0);
    return PrimitiveType.BOOLEAN;
  }
  
  @Override
  public Type visit(LiteralNull literal_null, GenEnv env) {
    mv.visitInsn(ACONST_NULL);
    return PrimitiveType.ANY;
  }
  
  @Override
  public Type visit(LiteralValue literal_value, GenEnv env) {
    Object value = literal_value.getValueLiteral().getValue();
    mv.visitLdcInsn(value);
    return literal_value.getTypeAttribute();
  }
  
  @Override
  public Type visit(LiteralString literal_string, GenEnv env) {
    String text = literal_string.getStringLiteral().getValue();
    mv.visitLdcInsn(text);
    return PrimitiveType.STRING;
  }


}
