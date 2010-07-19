package com.googlecode.phpreboot.compiler;

import static com.googlecode.phpreboot.compiler.LivenessType.ALIVE;
import static com.googlecode.phpreboot.compiler.LivenessType.DEAD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.phpreboot.ast.ArrayEntry;
import com.googlecode.phpreboot.ast.ArrayValue;
import com.googlecode.phpreboot.ast.ArrayValueEntry;
import com.googlecode.phpreboot.ast.ArrayValueSingle;
import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.ConstDeclaration;
import com.googlecode.phpreboot.ast.ElseIf;
import com.googlecode.phpreboot.ast.ElseIfElse;
import com.googlecode.phpreboot.ast.ElseIfElseIf;
import com.googlecode.phpreboot.ast.ElseIfEmpty;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprIf;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.ExprXmls;
import com.googlecode.phpreboot.ast.FunNoReturnType;
import com.googlecode.phpreboot.ast.FunReturnType;
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
import com.googlecode.phpreboot.ast.LabelId;
import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralArrayEntry;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.MemberConst;
import com.googlecode.phpreboot.ast.MemberFun;
import com.googlecode.phpreboot.ast.MemberInstr;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.Parameters;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.PrimaryParens;
import com.googlecode.phpreboot.ast.ScriptMember;
import com.googlecode.phpreboot.ast.ScriptScriptMember;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.interpreter.Profile.IfProfile;
import com.googlecode.phpreboot.interpreter.Profile.VarProfile;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.IntrinsicInfo;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.RT;

class TypeChecker extends Visitor<Type, TypeCheckEnv, RuntimeException> {
  private final HashMap<Node, Type> typeAttributeMap =
    new HashMap<Node, Type>();
  private final HashMap<Node, Symbol> symbolAttributeMap =
    new HashMap<Node, Symbol>();
  private final HashMap<Function,LocalVar> functionToLocalMap =
    new HashMap<Function, LocalVar>();
  private final boolean breakOrContinueAsException;
  private final /*@Nullable*/Node rootTraceNode;  // null if not in trace mode
  private final /*@Nullable*/Scope rootScope;     // null if not in trace mode
  private final boolean inferReturnType;
  private Type inferedReturnType;
  private final BindMap bindMap;
  private final TypeProfileMap typeProfileMap;
  private final boolean allowOptimisticType;
  
  TypeChecker(boolean breakOrContinueAsException, /*@Nullable*/Node rootTraceNode, /*@Nullable*/Scope rootScope, boolean inferReturnType, BindMap bindMap, TypeProfileMap typeProfileMap, boolean allowOptimisticType) {
    this.breakOrContinueAsException = breakOrContinueAsException;
    this.rootTraceNode = rootTraceNode;
    this.rootScope = rootScope;
    this.inferReturnType = inferReturnType;
    this.bindMap = bindMap;
    this.typeProfileMap = typeProfileMap;
    this.allowOptimisticType = allowOptimisticType;
  }
  
  public Type typeCheck(Node node, TypeCheckEnv env) {
    Type type = node.accept(this, env);
    typeAttributeMap.put(node, type);
    return type;
  }
  
  public Map<Node, Type> getTypeAttributeMap() {
    return typeAttributeMap;
  }
  public Map<Node, Symbol> getSymbolAttributeMap() {
    return symbolAttributeMap;
  }
  public Type getInferedReturnType() {
    return inferedReturnType;
  }
  
  
  // --- scope helpers
  
  private static void checkVar(String name, LocalScope scope) {
    Var var = scope.lookup(name);
    if (var != null) {
      throw RT.error("variable %s already exists", name);
    }
  }
  
  // --- helpers
  
  private static Type typeCheckUnaryOp(Type type) {
    if (type == PrimitiveType.ANY ||  type == PrimitiveType.INT ||  type == PrimitiveType.DOUBLE)
      return type;
    throw RT.error("illegal type %s for unary expression", type);
  }
  
  private static Type typeCheckBinaryOp(Type left, Type right) {
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
    throw RT.error("illegal types %s,%s for a binary expression", left, right);
  }
  
  private static void checkIsTestable(Type type) {
    switch((PrimitiveType)type) {
    case ANY:
    case INT:
    case DOUBLE:
    case STRING:
      return;
    default:
      throw RT.error("illegal types %s for a binary test", type);
    }
  }
  
  private static Type typeCheckBinaryTest(Type left, Type right) {
    checkIsTestable(left);
    checkIsTestable(right);
    return PrimitiveType.BOOLEAN;
  }
  
  private static void isCompatible(Type type, Type exprType) {
    if (type == PrimitiveType.ANY)
      return;
    if (type == exprType)
      return;
    if (type == PrimitiveType.DOUBLE && exprType == PrimitiveType.INT)
      return;
    throw RT.error("incompatible type %s %s", type, exprType);
  }
  
  private static boolean isOptimiticCompatible(Var var, Type exprType) {
    LocalVar localVar;
    if ((!(var instanceof LocalVar) || (!((localVar = (LocalVar)var)).isOptimistic()))) {
      isCompatible(var.getType(), exprType);
      return true; // reach only if compatible
    }
    
    return checkOptimisticCompatible(localVar.getType(), exprType);
  }
  
  private static boolean checkOptimisticCompatible(Type type, Type exprType) {
    return type == exprType || (type == PrimitiveType.DOUBLE && exprType == PrimitiveType.INT);
  }
  
  private static Type commonSuperType(Type type1, Type type2) {
    if (type1 == type2)
      return type1;
    if ((type1 == PrimitiveType.INT && type2 == PrimitiveType.DOUBLE) ||
        (type1 == PrimitiveType.DOUBLE && type2 == PrimitiveType.INT)) {
      return PrimitiveType.DOUBLE;
    }
    return PrimitiveType.ANY;
  }
  
  // --- default visit
  
  @Override
  protected Type visit(Node node, TypeCheckEnv env) {
    System.err.println("code is not compilable: "+node.getKind()+
        " at "+node.getLineNumberAttribute()+':'+node.getColumnNumberAttribute());
    throw CodeNotCompilableTypeCheckException.INSTANCE;
  }
  
  
  // --- visit members
  
  @Override
  public Type visit(ScriptMember scriptMember, TypeCheckEnv env) {
    return typeCheck(scriptMember.getMember(), env);
  }
  @Override
  public Type visit(ScriptScriptMember scriptScriptMember, TypeCheckEnv env) {
    Type liveness = typeCheck(scriptScriptMember.getScript(), env);
    if (liveness != ALIVE)
      return liveness;
    return typeCheck(scriptScriptMember.getMember(), env);
  }
  
  @Override
  public Type visit(MemberFun memberFun, TypeCheckEnv env) {
    return typeCheck(memberFun.getFun(), env);
  }
  @Override
  public Type visit(MemberConst memberConst, TypeCheckEnv env) {
    return typeCheck(memberConst.getConstDeclaration(), env);
  }
  @Override
  public Type visit(MemberInstr memberInstr, TypeCheckEnv env) {
    return typeCheck(memberInstr.getInstr(), env);
  }
  
  
  // --- visit function definition
  
  private void visitFun(Node node, String name, Parameters parametersNode, Block block, TypeCheckEnv env) {
    assert(!allowOptimisticType);  // this method is only called by AOT compiler
    
    LocalScope scope = env.getScope();
    checkVar(name, scope);
    
    IntrinsicInfo intrinsicInfo = new IntrinsicInfo(null/*current class*/, name, -1);
    Function function = Function.createFunction(name, parametersNode, intrinsicInfo, scope, block);
    
    LocalScope localScope = new LocalScope(function.getScope());
    localScope.register(new Var(name, true, true, PrimitiveType.ANY, function));
    List<Parameter> parameters = function.getParameters();
    int size = parameters.size();
    for(int i=0; i<size; i++) {
      Parameter parameter = parameters.get(i);
      Type type = parameter.getType();
      localScope.register(LocalVar.createLocalVar(parameter.getName(), true, type, null, localScope.nextSlot(type)));
    }
    
    LoopStack<Boolean> loopStack = new LoopStack<Boolean>();
    typeCheck(block, new TypeCheckEnv(localScope, loopStack, false, function.getReturnType()));
    
    Var var = new Var(name, true, true, PrimitiveType.ANY, function);
    scope.register(var);
    
    symbolAttributeMap.put(node, LocalVar.createConstantFoldable(function));
  }
  
  @Override
  public Type visit(FunNoReturnType funNoReturnType, TypeCheckEnv env) {
    visitFun(funNoReturnType,
        funNoReturnType.getId().getValue(),
        funNoReturnType.getParameters(),
        funNoReturnType.getBlock(),
        env);
    return LivenessType.ALIVE;
  }
  @Override
  public Type visit(FunReturnType funReturnType, TypeCheckEnv env) {
    visitFun(funReturnType,
        funReturnType.getId().getValue(),
        funReturnType.getParameters(),
        funReturnType.getBlock(),
        env);
    return LivenessType.ALIVE;
  }
  
  
  // --- visit instructions
  
  // for all instructions the visitor must return the liveness
  // of the next instruction. By example, visit(Return) should returns
  // that the next instruction is dead.
  // Convention: the liveness value can be LivenessType#ALIVE is the flow
  // is not stopped or any other value if the liveness is dead
  
  @Override
  public Type visit(Block block, TypeCheckEnv env) {
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(new LocalScope(env.getScope()), env.getLoopStack(), env.isUntakenBranch(), env.getFunctionReturnType());
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
  
  private Type visitIf(Node ifNode, Expr expr, Instr instr, ElseIf elseIf, TypeCheckEnv env) {
    Type exprType = typeCheck(expr, env);
    isCompatible(PrimitiveType.BOOLEAN, exprType);
    
    BranchSymbol branchSymbol = (BranchSymbol)symbolAttributeMap.get(ifNode);
    if (branchSymbol == null) {
      branchSymbol = new BranchSymbol();
      symbolAttributeMap.put(ifNode, branchSymbol);
    }
    
    TypeCheckEnv leftPartEnv = env;
    TypeCheckEnv rightPartEnv = env;
    Scope reconstructedScope = null;
    LinkedHashMap<LocalVar,Var> varMapAssoc = null;
    
    boolean currentBranchUntaken = env.isUntakenBranch();
    IfProfile ifProfile = (IfProfile)ifNode.getProfileAttribute();
    if (ifProfile != null && !currentBranchUntaken) {
      //System.out.println("ifProfile "+ifProfile.isLeftPartTaken()+" "+ifProfile.isRightPartTaken());
      
      if (!ifProfile.isLeftPartTaken()) {  
        leftPartEnv = new TypeCheckEnv(env.getScope(), env.getLoopStack(), true, env.getFunctionReturnType());
        varMapAssoc = new LinkedHashMap<LocalVar, Var>();
        reconstructedScope = env.getScope().reconstructScope(varMapAssoc);
      } else
        if (!ifProfile.isRightPartTaken()) {
          rightPartEnv = new TypeCheckEnv(env.getScope(), env.getLoopStack(), true, env.getFunctionReturnType());
          varMapAssoc = new LinkedHashMap<LocalVar, Var>();
          reconstructedScope = env.getScope().reconstructScope(varMapAssoc);
        }
    }
    
    if (varMapAssoc != null) {         // need to restore values of bind vars
      for(LocalVar localVar: bindMap.getReferences()) {
        if (localVar.isReadOnly())
          continue;
        varMapAssoc.put(localVar, rootScope.lookup(localVar.getName()));
      }
      //System.out.println("reconstructed varMapAssoc "+varMapAssoc);
    }
    
    Type leftLiveness = DEAD;
    if (branchSymbol.leftPartActivated) {  // may be de-activated by previous typechecking pass
      try {
        leftLiveness = typeCheck(instr, leftPartEnv);
      } catch(UntakenBranchTypeCheckException e) {
        if (currentBranchUntaken)  // re-propagate if current branch is not taken
          throw e;
        
        assert varMapAssoc != null;
        branchSymbol.leftPartActivated = false;
        branchSymbol.localVarsToRestore = varMapAssoc.keySet();
        branchSymbol.escapeFunctionVar = EscapeTraceEvaluator.createEscapeTraceLocalVar(bindMap, typeProfileMap, instr, rootTraceNode, reconstructedScope, varMapAssoc);
      }
    }
    Type rightLiveness = DEAD;
    if (branchSymbol.rightPartActivated) {  // may be de-activated by previous typechecking pass
      try {
        rightLiveness = typeCheck(elseIf, rightPartEnv);
      } catch(UntakenBranchTypeCheckException e) {
        if (currentBranchUntaken)  // re-propagate if current branch is not taken
          throw e;
        
        assert varMapAssoc != null;
        branchSymbol.rightPartActivated = false;
        branchSymbol.localVarsToRestore = varMapAssoc.keySet();
        branchSymbol.escapeFunctionVar = EscapeTraceEvaluator.createEscapeTraceLocalVar(bindMap, typeProfileMap, elseIf, rootTraceNode, reconstructedScope, varMapAssoc);
      }
    }
    return (leftLiveness == ALIVE || rightLiveness == ALIVE)? ALIVE: DEAD;
  }
  @Override
  public Type visit(InstrIf instr_if, TypeCheckEnv env) {
    return visitIf(instr_if, instr_if.getExpr(), instr_if.getInstr(), instr_if.getElseIf(), env);
  }
  @Override
  public Type visit(ElseIfElseIf else_if_else_if, TypeCheckEnv env) {
    return visitIf(else_if_else_if, else_if_else_if.getExpr(), else_if_else_if.getInstr(), else_if_else_if.getElseIf(), env);
  }
  @Override
  public Type visit(ElseIfElse else_if_else, TypeCheckEnv env) {
    return typeCheck(else_if_else.getInstr(), env);
  }
  @Override
  public Type visit(ElseIfEmpty else_if_empty, TypeCheckEnv env) {
    return ALIVE;
  }
  
  @Override
  public Type visit(InstrReturn instr_return, TypeCheckEnv env) {
    Expr expr = instr_return.getExprOptional();
    Type exprType = (expr == null)? PrimitiveType.VOID: typeCheck(expr, env);
    
    if (inferReturnType) {      // return type inference
      if (inferedReturnType == null) {
        inferedReturnType = exprType;
      } else {
        inferedReturnType = commonSuperType(exprType, inferedReturnType);
      }
      return DEAD;
    }
    
    Type functionReturnType = env.getFunctionReturnType();
    isCompatible(functionReturnType, exprType);
    return DEAD;   
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
  
  
  // --- declaration 
  
  @Override
  public Type visit(ConstDeclaration const_declaration, TypeCheckEnv env) {
    String name = const_declaration.getId().getValue();
    LocalScope scope = env.getScope();
    Var var = scope.lookup(name);
    if (var != null) {
      throw RT.error("var %s already defined", name);
    }
    
    Type type = typeCheck(const_declaration.getExpr(), env);
    LocalVar localVar = LocalVar.createLocalVar(name, true, type, const_declaration, scope.nextSlot(type));
    scope.register(localVar);
    symbolAttributeMap.put(const_declaration, localVar);
    return ALIVE;
  }
  
  @Override
  public Type visit(AssignmentId assignment_id, TypeCheckEnv env) {
    Type exprType = typeCheck(assignment_id.getExpr(), env);
    
    String name = assignment_id.getId().getValue();
    LocalScope scope = env.getScope();
    Var var = scope.lookup(name);
    
    LocalVar localVar;
    TypeProfileMap typeProfileMap = this.typeProfileMap;
    if (var == null) {
      // auto-declaration
      Type type;
      if (allowOptimisticType) {

        // got a profile ?
        VarProfile profile = (VarProfile)assignment_id.getProfileAttribute();
        if (profile != null) {
          type = Compiler.inferType(profile.getVar().getValue());

          // check if inferred type is compatible otherwise revert back
          if (type != PrimitiveType.ANY && !checkOptimisticCompatible(type, exprType)) {
            type = PrimitiveType.ANY;
          }
        } else {
          // no profile, in general this mean that the branch was untaken
          // try optimistically to use the type of the expression
          type = Compiler.eraseAsProfile(exprType);
        }

        typeProfileMap.registerType(assignment_id, type);
      } else {
        // already typecked ?
        type = typeProfileMap.getType(assignment_id);
        if (type == null) {
          type = PrimitiveType.ANY;
          typeProfileMap.registerType(assignment_id, type);
        }
      }
      
      localVar = LocalVar.createLocalVar(name, false, type, (type != PrimitiveType.ANY)? assignment_id: null, scope.nextSlot(type));
      scope.register(localVar);
      
    } else {
      
      if (var.isReadOnly())
        throw RT.error("try to assign a read only variable %s", name);
      
      boolean isOptimisiticCompatible = isOptimiticCompatible(var, exprType);
      if (var instanceof LocalVar) {
        localVar = (LocalVar)var;
        if (localVar.isOptimistic() && !isOptimisiticCompatible) {
          //System.err.println("optimistic assertion failed ! "+localVar.getName() + " at "+assignment_id.getLineNumberAttribute());
          
          // optimistic assertion failed
          // if this branch was not taken, de-activate it, stop type-checking and generator will generate an escape code
          // if this branch was taken, record the failure and continue with any, the typecker must be re-run
          if (env.isUntakenBranch()) {  
            throw UntakenBranchTypeCheckException.INSTANCE;
          } 
          typeProfileMap.validate(false);
          typeProfileMap.registerType(localVar.getDeclaringNode(), PrimitiveType.ANY);
          localVar.setType(PrimitiveType.ANY);
        }
      } else {
        localVar = bindMap.bind(name, var.isReadOnly(), var.getValue(), var.getType(), allowOptimisticType, typeProfileMap, assignment_id);
        scope.register(localVar);
      }
    }
    
    symbolAttributeMap.put(assignment_id, localVar);
    return localVar.getType();
  }
  
  
  // --- loop instructions
  
  static String getLoopLabel(Node loop) {
    com.googlecode.phpreboot.ast.Label labelNode = ((InstrLabeled)loop.getParent()).getLabel();
    if (labelNode instanceof LabelId) {
      return ((LabelId)labelNode).getId().getValue();
    }
    return null;
  }
  
  @Override
  public Type visit(InstrLabeled instr_labeled, TypeCheckEnv env) {
    return typeCheck(instr_labeled.getLabeledInstr(), env);
  }
  
  @Override
  public Type visit(LabeledInstrWhile labeled_instr_while, TypeCheckEnv env) {
    Expr expr = labeled_instr_while.getExpr();
    Type exprType = typeCheck(expr, env);
    isCompatible(PrimitiveType.BOOLEAN, exprType);
    
    String label = getLoopLabel(labeled_instr_while);
    LoopStack<Boolean> loopStack = env.getLoopStack();
    loopStack.push(label, Boolean.TRUE);
    
    try {
      typeCheck(labeled_instr_while.getInstr(), env);
    } finally {
      loopStack.pop();
    }
    
    return ALIVE;
  }
  
  private Type visitBreakOrContinue(/*@Nullable*/IdToken idToken, TypeCheckEnv env) {
    if (breakOrContinueAsException) {   // mixed mode, if loopstack doesn't contains any labels 
      return DEAD;   // gen pass will generate an exception to go back into the interpreter
    }

    LoopStack<Boolean> loopStack = env.getLoopStack();
    if (idToken == null) {
      if (loopStack.current() == null) {
        throw RT.error("break/continue without a loop or a switch");
      }
    } else {
      String label = idToken.getValue();
      if (loopStack.lookup(label) == null) {
        throw RT.error("no label %s is available", label);
      }
    }
    return DEAD;
  }
  
  @Override
  public Type visit(InstrBreak instr_break, TypeCheckEnv env) {
    return visitBreakOrContinue(instr_break.getIdOptional(), env);
  }
  
  @Override
  public Type visit(InstrContinue instr_continue, TypeCheckEnv env) {
    return visitBreakOrContinue(instr_continue.getIdOptional(), env);
  }
  
  // --- visit fun call
  
  @Override
  public Type visit(FuncallCall funcall_call, TypeCheckEnv env) {
    String name = funcall_call.getId().getValue();
    List<Expr> exprStar = funcall_call.getExprStar();
    LocalScope scope = env.getScope();
    Var var = scope.lookup(name);
    if (var != null) {
      Object value = var.getValue();
      if (!(value instanceof Function)) {
        throw RT.error("variable %s doesn't reference a function: %s", name, value);
      }
      
      Function function = (Function)value;
      int size = exprStar.size();
      List<Parameter> parameters = function.getParameters();
      
      if (size != parameters.size()) {
        throw RT.error("argument number mismatch with function %s", name);
      }
      
      Type[] typeProfile = new Type[size];
      for(int i=0; i<size; i++) {
        Type exprType = typeCheck(exprStar.get(i), env);
        isCompatible(parameters.get(i).getType(), exprType);
        typeProfile[i] = Compiler.eraseAsProfile(exprType);
      }
      
      LocalVar localVar;
      if (function.getIntrinsicInfo() == null) {
        
        if (allowOptimisticType && var.isReallyConstant()) {   // try to specialize the method
          List<Type> typeProfileList = Arrays.asList(typeProfile);
          Function specializedFunction = function.lookupSignature(typeProfileList);
          if (specializedFunction == null) {
            if (env.isUntakenBranch()) {  // don't try to typecheck a function that was not called at least once
              throw UntakenBranchTypeCheckException.INSTANCE;
            }
            
            specializedFunction = Compiler.traceTypecheckFunction(function, typeProfileList); 
          } 
          if (specializedFunction != null) {
            LocalVar functionVar = functionToLocalMap.get(specializedFunction);
            if (functionVar == null) {
              functionVar = bindMap.bind(name, var.isReadOnly(), specializedFunction, PrimitiveType.FUNCTION, false, typeProfileMap, funcall_call);
              functionToLocalMap.put(specializedFunction, functionVar);
            }
            var = functionVar;
            function = specializedFunction;
          }
        }
        
        if (var instanceof LocalVar) {
          localVar = (LocalVar)var;
        } else {
          localVar = functionToLocalMap.get(function);
          if (localVar == null) {
            localVar = bindMap.bind(name, var.isReadOnly(), function, PrimitiveType.FUNCTION, false, typeProfileMap, funcall_call);
            functionToLocalMap.put(function, localVar);
          }
        }
        
      } else {
        // call will be intrinsicfied
        localVar = LocalVar.createConstantFoldable(function);
      }
      
      symbolAttributeMap.put(funcall_call, localVar);
      return function.getReturnType();
    }
    
    //FIXME implements Java bridge
    return super.visit(funcall_call, env);
  }
  
  
  // --- visit primary
  
  @Override
  public Type visit(PrimaryFuncall primary_funcall, TypeCheckEnv env) {
    return typeCheck(primary_funcall.getFuncall(), env);
  }
  
  @Override
  public Type visit(PrimaryParens primary_parens, TypeCheckEnv env) {
    return typeCheck(primary_parens.getExpr(), env);
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
      symbolAttributeMap.put(expr_id, (LocalVar)var);
      return type;
    }
    
    Object value = var.getValue();
    
    if (var.isReallyConstant()) {  // constant foldable
      if (value instanceof Boolean) {
        symbolAttributeMap.put(expr_id, LocalVar.createConstantFoldable(value));
        return PrimitiveType.BOOLEAN;
      }
      if (value instanceof Integer) {
        symbolAttributeMap.put(expr_id, LocalVar.createConstantFoldable(value));
        return PrimitiveType.INT;
      }
      if (value instanceof Double) {
        symbolAttributeMap.put(expr_id, LocalVar.createConstantFoldable(value));
        return PrimitiveType.DOUBLE;
      }
      if (value instanceof String) {
        symbolAttributeMap.put(expr_id, LocalVar.createConstantFoldable(value));
        return PrimitiveType.STRING;
      }
    }
    
    // bound constant
    LocalVar bindVar = bindMap.bind(name, var.isReadOnly(), value, type, allowOptimisticType, typeProfileMap, expr_id);
    env.getScope().register(bindVar);
    symbolAttributeMap.put(expr_id, bindVar);
    return bindVar.getType();
  }
  
  @Override
  public Type visit(ExprPrimary expr_primary, TypeCheckEnv env) {
    return typeCheck(expr_primary.getPrimary(), env);
  }
  
  @Override
  public Type visit(ExprIf expr_if, TypeCheckEnv env) {
    isCompatible(PrimitiveType.BOOLEAN, typeCheck(expr_if.getExpr(), env));
    return commonSuperType(
        typeCheck(expr_if.getExpr2(), env),
        typeCheck(expr_if.getExpr3(), env));
  }
   
  @Override
  public Type visit(ExprXmls expr_xmls, TypeCheckEnv env) {
    return typeCheck(expr_xmls.getXmls(), env);
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
      return typeCheckBinaryTest(left, right);
    case expr_le:
      return typeCheckBinaryTest(left, right);
    case expr_gt:
      return typeCheckBinaryTest(left, right);
    case expr_ge:
      return typeCheckBinaryTest(left, right);

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
