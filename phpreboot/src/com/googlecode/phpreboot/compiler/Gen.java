package com.googlecode.phpreboot.compiler;

import static com.googlecode.phpreboot.compiler.LivenessType.ALIVE;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IDIV;
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

import com.googlecode.phpreboot.ast.ArrayEntry;
import com.googlecode.phpreboot.ast.ArrayValue;
import com.googlecode.phpreboot.ast.ArrayValueEntry;
import com.googlecode.phpreboot.ast.ArrayValueSingle;
import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.ElseIfElse;
import com.googlecode.phpreboot.ast.ElseIfElseIf;
import com.googlecode.phpreboot.ast.ElseIfEmpty;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprIf;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.FuncallCall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrAssign;
import com.googlecode.phpreboot.ast.InstrBlock;
import com.googlecode.phpreboot.ast.InstrBreak;
import com.googlecode.phpreboot.ast.InstrContinue;
import com.googlecode.phpreboot.ast.InstrDecl;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.InstrFuncall;
import com.googlecode.phpreboot.ast.InstrIf;
import com.googlecode.phpreboot.ast.InstrLabeled;
import com.googlecode.phpreboot.ast.InstrReturn;
import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralArrayEntry;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.PrimaryParens;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.compiler.LoopStack.Labels;
import com.googlecode.phpreboot.interpreter.Echoer;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.IntrinsicInfo;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public class Gen extends Visitor<Type, GenEnv, RuntimeException> {
  private static final String VAR_INTERNAL_NAME = getInternalName(Var.class);
  private static final String FUNCTION_INTERNAL_NAME = getInternalName(Function.class);
  private static final String ARRAY_INTERNAL_NAME = getInternalName(Array.class);
  private static final String ECHOER_INTERNAL_NAME = getInternalName(Echoer.class);
  private static final String EVAL_ENV_INTERNAL_NAME = getInternalName(EvalEnv.class);
  private static final String RT_INTERNAL_NAME = getInternalName(RT.class);
  
  
  
  final MethodVisitor mv;
  
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

  void defaultReturn(Type returnType) {
    switch((PrimitiveType)returnType) {
    case VOID:
      break;
    case BOOLEAN:
    case INT:
      mv.visitInsn(ICONST_0);
      break;
    case DOUBLE:
      mv.visitInsn(DCONST_0);
      break;
    default:
      mv.visitInsn(ACONST_NULL);
    }
    
    mv.visitInsn(asASMType(returnType).getOpcode(IRETURN));
  }
  
  private void insertCast(Type type, Type exprType) {
    if (type == exprType)
      return;
    
    if (type == PrimitiveType.DOUBLE && exprType == PrimitiveType.INT) {
      mv.visitInsn(I2D);
      return;
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
    mv.visitMethodInsn(INVOKEDYNAMIC, INVOKEDYNAMIC_OWNER, "checkcast",
          '('+asASMType(exprType).getDescriptor()+')'+asASMType(type).getDescriptor());
  }
  
  private static String getInternalName(Class<?> runtimeClass) {
    return runtimeClass.getName().replace('.', '/');
  }
  
  
  // restore environment at the end of a trace and
  // prepare arguments for the first call
  void restoreEnv(List<LocalVar> references, int slotCount, Scope scope, Object[] args) {
    int size = references.size();
    int outputVarIndex = size + 1;
    int outputSlotIndex = slotCount + 1;
    for(int i=0; i< size; i++) {
      LocalVar localVar = references.get(i);
      args[i + 1] = localVar.getValue();
      
      //FIXME doesn't work with read-only value
      //localVar.setValue(null);   // avoid a memory leak
      
      Var var = scope.lookup(localVar.getName());
      if (!var.isReadOnly()) {
        args[outputVarIndex++] = var;
        Type type = localVar.getType();
        mv.visitVarInsn(ALOAD, outputSlotIndex++);
        mv.visitVarInsn(asASMType(type).getOpcode(ILOAD), localVar.getSlot(0));
        insertCast(PrimitiveType.ANY, type);
        mv.visitMethodInsn(INVOKEVIRTUAL, VAR_INTERNAL_NAME, "setValue", "(Ljava/lang/Object;)V");
      }
    }
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
    mv.visitLineNumber(instr_block.getLineNumberAttribute(), new Label());
    return gen(instr_block.getBlock(), env);
  }
  
  @Override
  public Type visit(InstrEcho instr_echo, GenEnv env) {
    mv.visitLineNumber(instr_echo.getLineNumberAttribute(), new Label());
    mv.visitVarInsn(ALOAD, 0); // load env
    mv.visitTypeInsn(CHECKCAST, EVAL_ENV_INTERNAL_NAME); //FIXME remove when environment is no more an Object
    
    mv.visitMethodInsn(INVOKEVIRTUAL, EVAL_ENV_INTERNAL_NAME, "getEchoer",
        "()L"+ECHOER_INTERNAL_NAME+';');
    Type exprType = gen(instr_echo.getExpr(), env.expectedType(PrimitiveType.ANY));
    insertCast(PrimitiveType.ANY, exprType);
    mv.visitMethodInsn(INVOKEVIRTUAL, ECHOER_INTERNAL_NAME, "echo",
        "(Ljava/lang/Object;)V");
    return null;
  }
  
  @Override
  public Type visit(InstrIf instr_if, GenEnv env) {
    mv.visitLineNumber(instr_if.getLineNumberAttribute(), new Label());
    Node elseIf = instr_if.getElseIf();
    elseIf = (elseIf instanceof ElseIfEmpty)? null: elseIf;
    IfParts ifParts = new IfParts(true, generator(instr_if.getInstr()), generator(elseIf));
    gen(instr_if.getExpr(), env.ifParts(ifParts));
    return null;
  }
  @Override
  public Type visit(ElseIfElseIf else_if_else_if, GenEnv env) {
    Node elseIf = else_if_else_if.getElseIf();
    elseIf = (elseIf instanceof ElseIfEmpty)? null: elseIf;
    IfParts ifParts = new IfParts(true, generator(else_if_else_if.getInstr()), generator(elseIf));
    gen(else_if_else_if.getExpr(), env.ifParts(ifParts));
    return null;
  }
  @Override
  public Type visit(ElseIfElse else_if_else, GenEnv env) {
    return gen(else_if_else.getInstr(), env);
  }
  @Override
  public Type visit(ElseIfEmpty else_if_empty, GenEnv env) {
    throw new AssertionError("must not be called");
  }
  
  @Override
  public Type visit(InstrReturn instr_return, GenEnv env) {
    mv.visitLineNumber(instr_return.getLineNumberAttribute(), new Label());
    Expr expr = instr_return.getExprOptional();
    Type type = instr_return.getTypeAttribute();
    
    if (expr != null) {
      gen(expr, env.expectedType(type));
      insertCast(type, expr.getTypeAttribute());
    }
    
    mv.visitInsn(asASMType(type).getOpcode(IRETURN));
    return null;
  }
  
  @Override
  public Type visit(InstrDecl instr_decl, GenEnv env) {
    mv.visitLineNumber(instr_decl.getLineNumberAttribute(), new Label());
    return gen(instr_decl.getDeclaration(), env);
  }
  
  @Override
  public Type visit(InstrFuncall instr_funcall, GenEnv env) {
    mv.visitLineNumber(instr_funcall.getLineNumberAttribute(), new Label());
    return gen(instr_funcall.getFuncall(), env);
  }
  
  @Override
  public Type visit(InstrAssign instr_assign, GenEnv env) {
    mv.visitLineNumber(instr_assign.getLineNumberAttribute(), new Label());
    return gen(instr_assign.getAssignment(), env);
  }
  
  
  
  @Override
  public Type visit(AssignmentId assignment_id, GenEnv env) {
    LocalVar var = (LocalVar)assignment_id.getSymbolAttribute();
    Type type = var.getType();
    Type exprType = gen(assignment_id.getExpr(), env.expectedType(type));
    insertCast(type, exprType);
    int shift = (var.isConstant())?0:env.getShift();
    mv.visitVarInsn(asASMType(type).getOpcode(ISTORE), var.getSlot(shift));
    return null;
  }
  

  
  // --- visit test condition
  
  private /*@Nullable*/GeneratorClosure generator(final /*@Nullable*/Node node) {
    if (node == null)
      return null;
    
    return new GeneratorClosure() {
      @Override
      Type gen(GenEnv env) {
        return Gen.this.gen(node, env);
      }
      @Override
      Type liveness() {
        return node.getTypeAttribute();
      }
    };
  }
  
  private static int inverseTestOpcode(int opcode) {
    return (opcode % 2 == 0)? opcode - 1: opcode + 1;
  }
  
  private static String inverseTestOpName(String opName) {
    //FIXME replace by a switch on string when eclipse supports 1.7
    if (opName == "lt")
      return "ge";
    if (opName == "le")
      return "gt";
    if (opName == "gt")
      return "le";
    if (opName == "ge")
      return "lt";
    if (opName == "eq")
      return "ne";
    if (opName == "ne")
      return "eq";
    throw new AssertionError("unknown opName");
  }
  
  private void genInstrOrExprBranches(IfParts ifParts, Label trueLabel, Label endLabel, boolean lastInstrIsAMethodCall, GenEnv env) {
    if (!lastInstrIsAMethodCall || ifParts.inCondition) {
      if (lastInstrIsAMethodCall) {
        mv.visitJumpInsn(IFNE, trueLabel);
      }
      GeneratorClosure falsePart = ifParts.falsePart;
      falsePart.gen(env);
      GeneratorClosure truePart = ifParts.truePart;
      if (truePart != null) {
        // don't generate a goto if false part is an instruction that doesn't live anymore
        boolean live = /*!ifParts.inCondition ||*/ falsePart.liveness() == LivenessType.ALIVE;
        if (live) {
          mv.visitJumpInsn(GOTO, endLabel);
        }
        mv.visitLabel(trueLabel);
        truePart.gen(env);
        if (live) {
          mv.visitLabel(endLabel);
        }
      } else {
        mv.visitLabel(trueLabel);
      }
      return;
    } 
  }
  
  private void genOnlyOneBranch(IfParts ifParts, int opcode, GenEnv env) {
    if (opcode == IF_ICMPEQ) {
      ifParts.falsePart.gen(env);
    } else {
      GeneratorClosure truePart = ifParts.truePart;
      if (truePart != null) {
        truePart.gen(env);
      }
    }
  }
  
  private Type visitBinaryEq(String opName, int opcode, Node leftNode, Node rightNode, GenEnv env) {
    Label trueLabel = new Label();
    Label endLabel = new Label();
    IfParts ifParts = env.getIfParts();
    if (ifParts == null) {  // if not a condition, create fake true/false nodes
      ifParts = new IfParts(false, trueGenerator, falseGenerator);
    } else {
      // create an invariant: false part is never null if in condition
      if (ifParts.falsePart == null) {
        ifParts = ifParts.swap();
        opcode = inverseTestOpcode(opcode);
        opName = inverseTestOpName(opName);
      } 
    }
    
    GenEnv newEnv = env.expectedType(PrimitiveType.ANY).ifParts(null);
    Type left = leftNode.getTypeAttribute();
    Type right = rightNode.getTypeAttribute();
    
    switch((PrimitiveType)left) {
    case BOOLEAN:
      switch((PrimitiveType)right) {
      case BOOLEAN:
        gen(leftNode, newEnv);
        gen(rightNode, newEnv);
        mv.visitJumpInsn(opcode, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      case ANY:
        gen(leftNode, newEnv);
        insertCast(PrimitiveType.ANY, PrimitiveType.BOOLEAN);
        gen(rightNode, newEnv);
        mv.visitMethodInsn(INVOKESTATIC, RT_INTERNAL_NAME, opName, "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, true, env);
        return PrimitiveType.BOOLEAN;
      default:
        gen(leftNode, newEnv);
        mv.visitInsn(POP);
        gen(rightNode, newEnv);
        mv.visitInsn((right == PrimitiveType.DOUBLE)? POP2: POP);
        genOnlyOneBranch(ifParts, opcode, env);
        return PrimitiveType.BOOLEAN;
      }
    case INT:
      switch((PrimitiveType)right) {
      case INT:
        gen(leftNode, newEnv);
        gen(rightNode, newEnv);
        mv.visitJumpInsn(opcode, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      case DOUBLE:
        gen(leftNode, newEnv);
        insertCast(PrimitiveType.DOUBLE, PrimitiveType.INT);
        gen(rightNode, newEnv);
        mv.visitInsn(DCMPG);
        mv.visitJumpInsn(opcode - IF_ICMPEQ + IFEQ, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      case ANY:
        gen(leftNode, newEnv);
        insertCast(PrimitiveType.ANY, PrimitiveType.INT);
        gen(rightNode, newEnv);
        mv.visitMethodInsn(INVOKESTATIC, RT_INTERNAL_NAME, opName, "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, true, env);
        return PrimitiveType.BOOLEAN;
      default:
        gen(leftNode, newEnv);
        mv.visitInsn(POP);
        gen(rightNode, newEnv);
        mv.visitInsn(POP);
        genOnlyOneBranch(ifParts, opcode, env);
        return PrimitiveType.BOOLEAN;
      }
    case DOUBLE:
      switch((PrimitiveType)right) {
      case INT:
      case DOUBLE:
        gen(leftNode, newEnv);
        gen(rightNode, newEnv);
        insertCast(PrimitiveType.DOUBLE, right);
        mv.visitInsn(DCMPG);
        mv.visitJumpInsn(opcode - IF_ICMPEQ + IFEQ, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      case ANY:
        gen(leftNode, newEnv);
        insertCast(PrimitiveType.ANY, PrimitiveType.DOUBLE);
        gen(rightNode, newEnv);
        mv.visitMethodInsn(INVOKESTATIC, RT_INTERNAL_NAME, opName, "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, true, env);
        return PrimitiveType.BOOLEAN;
      default:
        gen(leftNode, newEnv);
        mv.visitInsn(POP2);
        gen(rightNode, newEnv);
        mv.visitInsn(POP);
        genOnlyOneBranch(ifParts, opcode, env);
        return PrimitiveType.BOOLEAN;
      }
    default:
      gen(leftNode, newEnv);
      gen(rightNode, newEnv);
      insertCast(PrimitiveType.ANY, right);
      mv.visitMethodInsn(INVOKESTATIC, RT_INTERNAL_NAME, opName, "(Ljava/lang/Object;Ljava/lang/Object;)Z");
      genInstrOrExprBranches(ifParts, trueLabel, endLabel, true, env);
      return PrimitiveType.BOOLEAN;
    }
  }

  private Type visitUnaryNot(Node unaryNode, GenEnv env) {
    IfParts ifParts = env.getIfParts();
    if (ifParts == null) {  // if not a condition, create fake true/false nodes inverted
      env = env.ifParts(new IfParts(false, falseGenerator, trueGenerator));
    } else {
      // invert parts
      env = env.ifParts(ifParts.swap());
    }
    
    gen(unaryNode, env.expectedType(PrimitiveType.BOOLEAN));
    return PrimitiveType.BOOLEAN;
  }
  
  private Type visitBinaryTest(String opName, int opcode, Node leftNode, Node rightNode, GenEnv env) {
    Label trueLabel = new Label();
    Label endLabel = new Label();
    IfParts ifParts = env.getIfParts();
    if (ifParts == null) {  // if not a condition, create fake true/false nodes
      ifParts = new IfParts(false, trueGenerator, falseGenerator);
    } else {
      // create an invariant: false part is never null if in condition
      if (ifParts.falsePart == null) {
        ifParts = ifParts.swap();
        opcode = inverseTestOpcode(opcode);
        opName = inverseTestOpName(opName);
      } 
    }
    
    GenEnv newEnv = env.expectedType(PrimitiveType.ANY).ifParts(null);
    Type left = leftNode.getTypeAttribute();
    Type right = rightNode.getTypeAttribute();
    
    top: switch((PrimitiveType)left) {
    case INT:
      switch((PrimitiveType)right) {
      case INT:
        gen(leftNode, newEnv);
        gen(rightNode, newEnv);
        mv.visitJumpInsn(opcode, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      case DOUBLE:
        gen(leftNode, newEnv);
        insertCast(PrimitiveType.DOUBLE, PrimitiveType.INT);
        gen(rightNode, newEnv);
        mv.visitInsn(DCMPG);
        mv.visitJumpInsn(opcode - IF_ICMPEQ + IFEQ, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      default:
        break top;
      }
    case DOUBLE:
      switch((PrimitiveType)right) {
      case INT:
      case DOUBLE:
        gen(leftNode, newEnv);
        gen(rightNode, newEnv);
        insertCast(PrimitiveType.DOUBLE, right);
        mv.visitInsn(DCMPG);
        mv.visitJumpInsn(opcode - IF_ICMPEQ + IFEQ, trueLabel);
        genInstrOrExprBranches(ifParts, trueLabel, endLabel, false, env);
        return PrimitiveType.BOOLEAN;
      default:
        break top;
      }
    default:
    }
    
    gen(leftNode, newEnv);
    gen(rightNode, newEnv);
    indy(opName, PrimitiveType.BOOLEAN, left, right);
    genInstrOrExprBranches(ifParts, trueLabel, endLabel, true, env);
    return PrimitiveType.BOOLEAN;
  }
  
  class ConstGenerator extends GeneratorClosure {
    private final boolean value;

    ConstGenerator(boolean value) {
      this.value = value;
    }
    @Override
    Type liveness() {
      return ALIVE;
    }

    @Override
    Type gen(GenEnv env) {
      mv.visitInsn((value)?ICONST_1: ICONST_0);
      return PrimitiveType.BOOLEAN;
    }
  }
  private final ConstGenerator trueGenerator = new ConstGenerator(true);
  private final ConstGenerator falseGenerator = new ConstGenerator(false);
  
  
  // --- labeled instructions
  
  @Override
  public Type visit(InstrLabeled instr_labeled, GenEnv env) {
    return gen(instr_labeled.getLabeledInstr(), env);
  }
  
  @Override
  public Type visit(LabeledInstrWhile labeled_instr_while, GenEnv env) {
    mv.visitLineNumber(labeled_instr_while.getLineNumberAttribute(), new Label());
    
    String label = TypeChecker.getLoopLabel(labeled_instr_while);
    LoopStack<Labels> loopStack = env.getLoopStack();
    
    final Label start = new Label();
    Label end = new Label();
    loopStack.push(label, new Labels(end, start));
    
    final Instr instr = labeled_instr_while.getInstr();
    
    mv.visitLabel(start);
    IfParts ifParts = new IfParts(true, new GeneratorClosure() {
      @Override
      Type liveness() {
        // liveness is not needed because there is no true part
        throw new AssertionError();
      }
      
      @Override
      Type gen(GenEnv env) {
        Gen.this.gen(instr, env);
        if (instr.getTypeAttribute() == ALIVE) {
          mv.visitJumpInsn(GOTO, start);
        }
        return null;
      }
    }, null);
    
    try {
      gen(labeled_instr_while.getExpr(), env.ifParts(ifParts));
    } finally {
      loopStack.pop();
    }
    mv.visitLabel(end);
    
    return null;
  }
  
  @Override
  public Type visit(InstrBreak instr_break, GenEnv env) {
    LoopStack<Labels> loopStack = env.getLoopStack();
    IdToken idToken = instr_break.getIdOptional();
    Labels labels;
    if (idToken == null) {
      labels = loopStack.current();
    } else {
      labels = loopStack.lookup(idToken.getValue());
    }
    mv.visitLabel(labels.breakLabel);
    return null;
  }
  
  @Override
  public Type visit(InstrContinue instr_continue, GenEnv env) {
    LoopStack<Labels> loopStack = env.getLoopStack();
    IdToken idToken = instr_continue.getIdOptional();
    Labels labels;
    if (idToken == null) {
      labels = loopStack.current();
    } else {
      labels = loopStack.lookup(idToken.getValue());
    }
    mv.visitLabel(labels.continueLabel);
    return null;
  }
  
  // --- visit fun call
  
  @Override
  public Type visit(FuncallCall funcall_call, GenEnv env) {
    LocalVar localVar = (LocalVar)funcall_call.getSymbolAttribute();
    Function function = (Function)localVar.getValue();
    
    StringBuilder desc = new StringBuilder();
    IntrinsicInfo intrinsicInfo = function.getIntrinsicInfo();
    if (intrinsicInfo == null) {
      desc.append("(L").append(getInternalName(/*EvalEnv.class*/Object.class)).append(';');
    } else {
      desc.append('(');
    }
    
    List<Expr> exprStar = funcall_call.getExprStar();
    List<Parameter> parameters = function.getParameters();
    for(int i=0; i<exprStar.size(); i++) {
      Expr expr = exprStar.get(i);
      Type expectedType = parameters.get(i).getType();
      Type exprType = gen(expr, env.expectedType(expectedType));
      insertCast(expectedType, exprType);
      desc.append(asASMType(expectedType).getDescriptor());
    }
    desc.append(')');
    Type returnType = function.getReturnType();
    if (returnType == PrimitiveType.ANY) {   //FIXME: not sure that this condition is correct !
      returnType = env.getExpectedType();
    }
    desc.append(asASMType(returnType).getDescriptor());
    
    if (intrinsicInfo == null) {
      mv.visitVarInsn(ALOAD, localVar.getSlot(0));
      mv.visitMethodInsn(INVOKEVIRTUAL, FUNCTION_INTERNAL_NAME, "getMethodHandle", "()Ljava/dyn/MethodHandle;");
      mv.visitVarInsn(ALOAD, 0); // environment
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/dyn/MethodHandle", /*"invokeExact"*/ "invoke", desc.toString());
    } else {
      mv.visitMethodInsn(INVOKESTATIC, getInternalName(intrinsicInfo.getDeclaringClass()), intrinsicInfo.getName(), desc.toString());
    }
    
    return returnType;
  }
  
  
  // --- visit primary
  
  @Override
  public Type visit(PrimaryFuncall primary_funcall, GenEnv env) {
    return gen(primary_funcall.getFuncall(), env);
  }
  
  @Override
  public Type visit(PrimaryParens primary_parens, GenEnv env) {
    return gen(primary_parens.getExpr(), env);
  }
  
  
  // --- visit expression
   
  @Override
  public Type visit(ExprId expr_id, GenEnv env) {
    LocalVar localVar = (LocalVar)expr_id.getSymbolAttribute();
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
  
  @Override
  public Type visit(ExprIf expr_if, GenEnv env) {
    //FIXME this will not work if the two expressions don't have the same type (insert cast) 
    IfParts ifParts = new IfParts(false, generator(expr_if.getExpr2()), generator(expr_if.getExpr3()));
    gen(expr_if.getExpr(), env.ifParts(ifParts));
    return expr_if.getTypeAttribute();
  }
  
  private Type visitUnaryOp(String opName, int opcode, Node exprNode, GenEnv env) {
    Type exprType = gen(exprNode, env.expectedType(PrimitiveType.ANY));
    
    if (exprType == PrimitiveType.ANY) {
      Type expectedType = env.getExpectedType();
      indy(opName, expectedType, exprType);
      return expectedType;
    } else {
      mv.visitInsn(asASMType(exprType).getOpcode(opcode));
      return exprType;
    }
  }
  
  private Type visitBinaryOp(String opName, int opcode, Node leftNode, Node rightNode, Type type, GenEnv env) {
    Type expectedType = env.getExpectedType();
    GenEnv newEnv = env.expectedType(PrimitiveType.ANY);
    
    Type left, right;
    switch((PrimitiveType)type) {
    case ANY:
      left = gen(leftNode, newEnv);
      right = gen(rightNode, newEnv);
      indy(opName, expectedType, left, right);
      return expectedType;
    case STRING:
      left = gen(leftNode, newEnv);
      right = gen(rightNode, newEnv);
      String desc;
      if (left == PrimitiveType.STRING) {
        desc = (right == PrimitiveType.INT)? "(Ljava/lang/String;I)Ljava/lang/String;":
          (right == PrimitiveType.DOUBLE)? "(Ljava/lang/String;D)Ljava/lang/String;":
          "(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;";
      } else { // right == Primitive.STRING
        desc = (left == PrimitiveType.INT)? "(ILjava/lang/String;)Ljava/lang/String;":
          (left == PrimitiveType.DOUBLE)? "(DLjava/lang/String;)Ljava/lang/String;":
          "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;";
      }
      mv.visitMethodInsn(INVOKESTATIC, RT_INTERNAL_NAME, "plus", desc);
      return type;
    default:
      left = gen(leftNode, newEnv);
      insertCast(type, left);
      right = gen(rightNode, newEnv);
      insertCast(type, right);
      mv.visitInsn(asASMType(type).getOpcode(opcode));
      return type;
    }
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
      return visitUnaryOp("-", INEG, unaryNode, env);
      
    case expr_unary_not:
      return visitUnaryNot(unaryNode, env);
    
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
    case expr_mod:
      return visitBinaryOp("mod", IREM, unaryNode, binaryNode, type, env);

    case expr_eq:
      return visitBinaryEq("eq", IF_ICMPEQ, unaryNode, binaryNode, env);
    case expr_ne:
      return visitBinaryEq("ne", IF_ICMPNE, unaryNode, binaryNode, env);
      
    case expr_lt:
      return visitBinaryTest("lt", IF_ICMPLT, unaryNode, binaryNode, env);
    case expr_le:
      return visitBinaryTest("le", IF_ICMPLE, unaryNode, binaryNode, env);
    case expr_gt:
      return visitBinaryTest("gt", IF_ICMPGT, unaryNode, binaryNode, env);
    case expr_ge:
      return visitBinaryTest("ge", IF_ICMPGE, unaryNode, binaryNode, env);

    default:
      throw new AssertionError("unknown expression "+kind);  
    }
  }
  
  
  // --- literals
  
  private  void integerConst(int value) {
    if (value >= -1 && value <= 5) {
      mv.visitInsn(ICONST_0 + value);
      return;
    }
    if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
      mv.visitIntInsn(BIPUSH, value);
      return;
    }
    if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
      mv.visitIntInsn(SIPUSH, value);
      return;
    }
    mv.visitLdcInsn(value);
  }
  
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
    if (value instanceof Integer) {
      integerConst((Integer)value);
    } else {
      mv.visitLdcInsn(value);
    }
    return literal_value.getTypeAttribute();
  }
  
  @Override
  public Type visit(LiteralString literal_string, GenEnv env) {
    String text = literal_string.getStringLiteral().getValue();
    mv.visitLdcInsn(text);
    return PrimitiveType.STRING;
  }

  
  // --- array literal
  
  @Override
  public Type visit(LiteralArray literal_array, GenEnv env) {
    mv.visitTypeInsn(NEW, ARRAY_INTERNAL_NAME);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, ARRAY_INTERNAL_NAME, "<init>", "()V");
    for(ArrayValue arrayValue: literal_array.getArrayValueStar()) {
      gen(arrayValue, env);
    }
    return PrimitiveType.ARRAY;
  }
  
  @Override
  public Type visit(LiteralArrayEntry literal_array_entry, GenEnv env) {
    mv.visitTypeInsn(NEW, ARRAY_INTERNAL_NAME);
    mv.visitInsn(DUP);
    mv.visitMethodInsn(INVOKESPECIAL, ARRAY_INTERNAL_NAME, "<init>", "()V");
    
    for(ArrayEntry arrayEntry: literal_array_entry.getArrayEntryStar()) {
      gen(arrayEntry, env);
    }
    return PrimitiveType.ARRAY;
  }
  
  @Override
  public Type visit(ArrayEntry array_entry, GenEnv env) {
    mv.visitInsn(DUP);
    Type exprTypeKey = gen(array_entry.getExpr(), env.expectedType(PrimitiveType.ANY));
    insertCast(PrimitiveType.ANY, exprTypeKey);
    Type exprTypeValue = gen(array_entry.getExpr2(), env.expectedType(PrimitiveType.ANY));
    insertCast(PrimitiveType.ANY, exprTypeValue);
    mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_INTERNAL_NAME, "set",
        "(Ljava/lang/Object;Ljava/lang/Object;)V");
    return null; // we don't care about this type
  }
  
  @Override
  public Type visit(ArrayValueSingle array_value_single, GenEnv env) {
    mv.visitInsn(DUP);
    Type exprType = gen(array_value_single.getExpr(), env.expectedType(PrimitiveType.ANY));
    insertCast(PrimitiveType.ANY, exprType);
    mv.visitMethodInsn(INVOKEVIRTUAL, ARRAY_INTERNAL_NAME, "add",
        "(Ljava/lang/Object;)V");
    return null; // we don't care about this type
  }
  @Override
  public Type visit(ArrayValueEntry array_value_entry, GenEnv env) {
    return gen(array_value_entry.getArrayEntry(), env);
  }
}
