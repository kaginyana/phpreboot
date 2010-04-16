package com.googlecode.phpreboot.compiler;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INEG;
import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC;
import static org.objectweb.asm.Opcodes.INVOKEDYNAMIC_OWNER;
import static org.objectweb.asm.Opcodes.ISUB;

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.RT;

public class Gen extends Visitor<Void, GenEnv, RuntimeException> {
  private final MethodVisitor mv;
  

  public Gen(MethodVisitor mv) {
    this.mv = mv;
  }


  public void gen(Fun functionNode) {
    throw new UnsupportedOperationException("NYI");
    //gen(functionNode, new GenEnv(PrimitiveType.VOID));
  }
  
  /*
  private Type gen(Node node, GenEnv env) {
    Type type = node.getTypeAttribute();
    if (type == null) {
      type = PrimitiveType.VOID;
    }
    node.accept(this, env.expectedType(type));
    return type;
  }
  
  
  // --- helpers
    
  private Type joinReturnType(Type expectedReturnType, Type currentReturnType) {
    if (expectedReturnType == null)
      return currentReturnType;
    if (expectedReturnType == PrimitiveType.ANY)
      return currentReturnType;
    return expectedReturnType;
  }
  
  private void indy(String name, Type returnType, Type type) {
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, name,
        org.objectweb.asm.Type.getMethodDescriptor(returnType.asASMType(),
            new org.objectweb.asm.Type[]{type.asASMType()}));
  }
  
  private void indy(String name, Type returnType, Type type1, Type type2) {
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, name,
        org.objectweb.asm.Type.getMethodDescriptor(returnType.asASMType(),
            new org.objectweb.asm.Type[]{type1.asASMType(), type2.asASMType()}));
  }
  
  // --- visit expression
  
  
  private Type visitBinaryOp(String opName, int opcode, Type left, Type right, GenEnv env) {
    if (left == PrimitiveType.ANY || right == PrimitiveType.ANY) {
      indy(opName, joinReturnType(env.getExpectedType(), result), left, right);
    } else {
      mv.visitInsn(result.asASMType().getOpcode(opcode));
    }
    return result;
  }
  
  private Type visitBinaryTest(String opName, int opcode, Node leftNode, Node rightNode, TypeCheckEnv env) {
    Type left = compile(leftNode, env.expectedType(null));
    Type right = compile(rightNode, env.expectedType(null));
    Type result = typeCheckBinaryOp(left, right);
    if (left == PrimitiveType.ANY || right == PrimitiveType.ANY) {
      indy("opName", joinReturnType(env.getExpectedType(), result), left, right);
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
    return result;
  }
  
  
  @Override
  protected Void visit(Expr expr, GenEnv env) {
    List<Node> nodeList = expr.nodeList();
    Node unaryNode = nodeList.get(0);
    Type type = gen(unaryNode, env);
    ProductionEnum kind = expr.getKind();
    switch(kind) {
    case expr_unary_plus:
      return null;
    case expr_unary_minus:
      if (type == PrimitiveType.ANY) {
        indy("unary_minus", joinReturnType(env.getExpectedType(), type), type);
      } else {
        mv.visitInsn(type.asASMType().getOpcode(INEG));
      }
      return null;
    case expr_unary_not:
      if (type == PrimitiveType.ANY) {
        indy("unary_not", joinReturnType(env.getExpectedType(), PrimitiveType.BOOLEAN), type);
      } else {
        mv.visitInsn(INOT);
      }
      return null;
    default:
    }

    Node binaryNode = nodeList.get(1);
    switch(kind) {
    case expr_plus:
      return visitBinaryOp("plus", IADD, unaryNode, binaryNode, env);
    case expr_minus:
      return visitBinaryOp("minus", ISUB, unaryNode, binaryNode, env);
    case expr_mult:
      return visitBinaryOp("mult", IMUL, unaryNode, binaryNode, env);
    case expr_div:
      return visitBinaryOp("div", IDIV, unaryNode, binaryNode, env);

    case expr_eq:
      return RT.eq(left, right);
    case expr_ne:
      return RT.ne(left, right);

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
  public Void visit(LiteralBool literal_bool, TypeCheckEnv env) {
    boolean value = literal_bool.getBoolLiteral().getValue();
    mv.visitInsn((value)? ICONST_1 :ICONST_0);
    return null;
  }
  
  @Override
  public Void visit(LiteralNull literal_null, TypeCheckEnv env) {
    mv.visitInsn(ACONST_NULL);
    return null;
  }
  
  @Override
  public Void visit(LiteralValue literal_value, TypeCheckEnv env) {
    Object value = literal_value.getValueLiteral().getValue();
    mv.visitLdcInsn(value);
    return null;
  }
  
  @Override
  public Void visit(LiteralString literal_string, TypeCheckEnv env) {
    String text = literal_string.getStringLiteral().getValue();
    mv.visitLdcInsn(text);
    return null;
  }




  */  
}
