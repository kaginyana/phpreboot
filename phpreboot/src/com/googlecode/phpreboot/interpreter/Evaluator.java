package com.googlecode.phpreboot.interpreter;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.googlecode.phpreboot.ast.ArrayEntry;
import com.googlecode.phpreboot.ast.ArrayValue;
import com.googlecode.phpreboot.ast.ArrayValueEntry;
import com.googlecode.phpreboot.ast.ArrayValueSingle;
import com.googlecode.phpreboot.ast.AssignmentArray;
import com.googlecode.phpreboot.ast.AssignmentField;
import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.AssignmentPrimaryArray;
import com.googlecode.phpreboot.ast.AssignmentPrimaryField;
import com.googlecode.phpreboot.ast.AttrsDollarAccess;
import com.googlecode.phpreboot.ast.AttrsEmpty;
import com.googlecode.phpreboot.ast.AttrsStringLiteral;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.ContentBlock;
import com.googlecode.phpreboot.ast.ContentDollarAccess;
import com.googlecode.phpreboot.ast.ContentEmpty;
import com.googlecode.phpreboot.ast.ContentText;
import com.googlecode.phpreboot.ast.ContentXmls;
import com.googlecode.phpreboot.ast.DeclarationLet;
import com.googlecode.phpreboot.ast.DeclarationTypeEmpty;
import com.googlecode.phpreboot.ast.DeclarationTypeInit;
import com.googlecode.phpreboot.ast.DollarAccessExpr;
import com.googlecode.phpreboot.ast.DollarAccessId;
import com.googlecode.phpreboot.ast.ElseIfElse;
import com.googlecode.phpreboot.ast.ElseIfElseIf;
import com.googlecode.phpreboot.ast.ElseIfEmpty;
import com.googlecode.phpreboot.ast.EoiEoln;
import com.googlecode.phpreboot.ast.EoiSemi;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprFun;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.ExprRegexMatch;
import com.googlecode.phpreboot.ast.ExprRegexReplace;
import com.googlecode.phpreboot.ast.ExprToType;
import com.googlecode.phpreboot.ast.ExprUri;
import com.googlecode.phpreboot.ast.ExprXmls;
import com.googlecode.phpreboot.ast.ForInit;
import com.googlecode.phpreboot.ast.ForInitAssignment;
import com.googlecode.phpreboot.ast.ForInitDeclaration;
import com.googlecode.phpreboot.ast.ForInitFuncall;
import com.googlecode.phpreboot.ast.ForStep;
import com.googlecode.phpreboot.ast.ForStepAssignment;
import com.googlecode.phpreboot.ast.ForStepFuncall;
import com.googlecode.phpreboot.ast.FunNoReturnType;
import com.googlecode.phpreboot.ast.FunReturnType;
import com.googlecode.phpreboot.ast.FuncallApply;
import com.googlecode.phpreboot.ast.FuncallCall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrBreak;
import com.googlecode.phpreboot.ast.InstrContinue;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.InstrFlwor;
import com.googlecode.phpreboot.ast.InstrIf;
import com.googlecode.phpreboot.ast.InstrLabeled;
import com.googlecode.phpreboot.ast.InstrReturn;
import com.googlecode.phpreboot.ast.InstrSql;
import com.googlecode.phpreboot.ast.InstrXmls;
import com.googlecode.phpreboot.ast.Label;
import com.googlecode.phpreboot.ast.LabelId;
import com.googlecode.phpreboot.ast.LabeledInstrDoWhile;
import com.googlecode.phpreboot.ast.LabeledInstrFor;
import com.googlecode.phpreboot.ast.LabeledInstrForeach;
import com.googlecode.phpreboot.ast.LabeledInstrForeachEntry;
import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.LetDeclaration;
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralArrayEntry;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.ParameterAny;
import com.googlecode.phpreboot.ast.ParameterTyped;
import com.googlecode.phpreboot.ast.Parameters;
import com.googlecode.phpreboot.ast.PrimaryArrayAccess;
import com.googlecode.phpreboot.ast.PrimaryFieldAccess;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.PrimaryParens;
import com.googlecode.phpreboot.ast.PrimaryPrimaryArrayAccess;
import com.googlecode.phpreboot.ast.PrimaryPrimaryFieldAccess;
import com.googlecode.phpreboot.ast.ReturnType;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.XmlsEmptyTag;
import com.googlecode.phpreboot.ast.XmlsStartEndTag;
import com.googlecode.phpreboot.compiler.Compiler;
import com.googlecode.phpreboot.flwor.XPathExprVisitor;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.regex.RegexEvaluator;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.XML;
import com.googlecode.phpreboot.sql.SQLConnection;
import com.googlecode.phpreboot.uri.URIVisitor;

public class Evaluator extends Visitor<Object, EvalEnv, RuntimeException> {
  @SuppressWarnings("serial")
  static class ReturnError extends Error {
    public Object value;
    ReturnError() {
      // do nothing
    }
    
    static ReturnError instance(Object value) {
      ReturnError instance = INSTANCE;
      instance.value = value;
      return instance;
    }
    
    private static final ReturnError INSTANCE = new ReturnError();
  }
  
  @SuppressWarnings("serial")
  static class BreakError extends Error {
    String label;
    BreakError() {
      // do nothing
    }
    
    public void mayRethrow(EvalEnv env) {
      String label = this.label;
      if (label != null && !label.equals(env.getLabel()))
        throw this;
    }
    
    static BreakError instance(String label) {
      BreakError instance = INSTANCE;
      instance.label = label;
      return instance;
    }
    
    private static final BreakError INSTANCE = new BreakError();
  }
  
  @SuppressWarnings("serial")
  static class ContinueError extends Error {
    String label;
    ContinueError() {
      // do nothing
    }
    
    public void mayRethrow(EvalEnv env) {
      String label = this.label;
      if (label != null && !label.equals(env.getLabel()))
        throw this;
    }
    
    static ContinueError instance(String label) {
      ContinueError instance = INSTANCE;
      instance.label = label;
      return instance;
    }
    
    private static final ContinueError INSTANCE = new ContinueError();
  }
  
  public static final Evaluator INSTANCE = new Evaluator();
  private static final int LOOP_COUNTER_THRESOLD = 300;
  
  // ---
  
  private Evaluator() {
    // enforce singleton
  }
  
  public Object eval(Node node, EvalEnv env) {
    return node.accept(this, env);
  }
  
  public Object evalFunction(Function function, Object[] arguments, EvalEnv env) {
    Scope scope = new Scope(function.getScope());
    scope.register(new Var(function.getName(), true, PrimitiveType.ANY, function));
    
    List<Parameter> parameters = function.getParameters();
    for(int i = 0; i<parameters.size(); i++) {
      Parameter parameter = parameters.get(i);
      Var var = new Var(parameter.getName(), true, parameter.getType(), arguments[i]);
      scope.register(var);
    }
    
    Block block = function.getBlock();
    EvalEnv evalEnv = new EvalEnv(scope, env.getEchoer(), null);
    try {
      eval(block, evalEnv);
    } catch(ReturnError e) {
      return function.getReturnType().getRuntimeClass().cast(e.value);
    }
    return null;
  }
  
  // --- helper methods
  
  private Object lookupVarValue(String name, Scope scope) {
    Var var = scope.lookup(name);
    if (var == null) {
      throw RT.error("variable %s not defined", name);
    }
    return var.getValue();
  }
  
  private static void checkVar(String name, Scope scope) {
    Var var = scope.lookup(name);
    if (var != null) {
      throw RT.error("variable %s already exists", name);
    }
  }
  
  // --- function definition
  
  private static void filterReadOnlyVars(HashMap<String,Var> map, Scope scope) {
    if (scope == null)
      return;
    filterReadOnlyVars(map, scope.getParent());
    for(Var var: scope.varMap()) {
      if (var.isReadOnly()) {
        String name = var.getName();
        map.put(name, new Var(name, true, var.getType(), var.getValue()));
      }
    }
  }
  
  private static Scope filterReadOnlyVars(Scope scope) {
    HashMap<String,Var> map = new HashMap<String, Var>();
    filterReadOnlyVars(map, scope);
    
    Scope newScope = new Scope(null);
    for(Var var: map.values()) {
      newScope.register(var);
    }
    return newScope;
  }
  
  private Function createFunction(boolean lambda, String name, Parameters parametersNode, Block block, EvalEnv env) {
    List<com.googlecode.phpreboot.ast.Parameter> parameterStar = parametersNode.getParameterStar();
    int size = parameterStar.size();
    ArrayList<Parameter> parameters = new ArrayList<Parameter>(size);
    Class<?>[] signature = new Class<?>[1 + size];
    signature[0] = /*EvalEnv.class*/Object.class;
    
    for(int i=0; i<size; i++) {
      String parameterName;
      Type type;
      com.googlecode.phpreboot.ast.Parameter parameter = parameterStar.get(i);
      if (parameter instanceof ParameterAny) {
        parameterName = ((ParameterAny)parameter).getId().getValue();
        type = PrimitiveType.ANY;
      } else {
        ParameterTyped parameterType = (ParameterTyped)parameter;
        parameterName = parameterType.getId().getValue();
        type = (Type)eval(parameterType.getType(), env);
      }
      signature[i + 1] = type.getUnboxedRuntimeClass();
      parameters.add(new Parameter(parameterName, type));
    }
    
    ReturnType returnTypeNode = parametersNode.getReturnTypeOptional();
    Type returnType = (returnTypeNode == null)? PrimitiveType.ANY: (Type)eval(returnTypeNode.getType(), env);
    
    Function function = new Function(name, parameters, returnType, filterReadOnlyVars(env.getScope()), block);
    
    // try to compile it
    MethodHandle compileMH = Compiler.compile(function);
    if (compileMH != null) {
      function.setMethodHandle(compileMH);
      return function;
    }
    
    MethodHandle mh = MethodHandles.lookup().findVirtual(Function.class, "call",
        MethodType.methodType(Object.class, /*EvalEnv.class*/ Object.class, Object[].class));
    mh = MethodHandles.insertArguments(mh, 0, function);
    mh = MethodHandles.collectArguments(mh, MethodType.genericMethodType(1 + size));
    
    MethodType functionType = MethodType.methodType(returnType.getUnboxedRuntimeClass(), signature);
    mh = MethodHandles.convertArguments(mh, functionType);
    
    //System.err.println("create function handle "+name+" "+functionType);
    
    function.setMethodHandle(mh);
    return function;
  }
  
  private void visitFun(String name, Parameters parameters, Block block, EvalEnv env) {
    Scope scope = env.getScope();
    checkVar(name, scope);
    
    Function function = createFunction(false, name, parameters, block, env);
    Var var = new Var(name, true, PrimitiveType.ANY, function);
    scope.register(var);
  }
  @Override
  public Object visit(FunNoReturnType fun_no_return_type, EvalEnv env) {
    visitFun(fun_no_return_type.getId().getValue(),
        fun_no_return_type.getParameters(),
        fun_no_return_type.getBlock(),
        env);
    return null;
  }
  @Override
  public Object visit(FunReturnType fun_return_type, EvalEnv env) {
    visitFun(fun_return_type.getId().getValue(),
        fun_return_type.getParameters(),
        fun_return_type.getBlock(),
        env);
    return null;
  }
  
  // --- instructions
  
  @Override
  protected Object visit(Instr instr, EvalEnv env) {
    for(Node node: instr.nodeList()) {
      eval(node, env);
    }
    return null;
  }
  
  @Override
  public Object visit(EoiEoln eoi__eoln, EvalEnv env) {
    return null;
  }
  @Override
  public Object visit(EoiSemi eoi__semi, EvalEnv env) {
    return null;
  }
  
  @Override
  public Object visit(InstrEcho instr_echo, EvalEnv env) {
    env.getEchoer().echo(eval(instr_echo.getExpr(), env));
    return null;
  }
  
  @Override
  public Object visit(InstrLabeled instr_labeled, EvalEnv env) {
    Label label = instr_labeled.getLabel();
    if (label instanceof LabelId) {
      String labelText = ((LabelId)label).getId().getValue();
      env = new EvalEnv(env.getScope(), env.getEchoer(), labelText);
    }
    eval(instr_labeled.getLabeledInstr(), env);
    return null;
  }
  
  @Override
  public Object visit(InstrReturn instr_return, EvalEnv env) {
    Expr exprOptional = instr_return.getExprOptional();
    Object value =(exprOptional == null)? null: eval(exprOptional, env);
    throw ReturnError.instance(value);
  }
  @Override
  public Object visit(InstrBreak instr_break, EvalEnv env) {
    IdToken idToken = instr_break.getIdOptional();
    String label = (idToken == null)? env.getLabel(): idToken.getValue();
    throw BreakError.instance(label);
  }
  @Override
  public Object visit(InstrContinue instr_continue, EvalEnv env) {
    IdToken idToken = instr_continue.getIdOptional();
    String label = (idToken == null)? env.getLabel(): idToken.getValue();
    throw ContinueError.instance(label);
  }
  
  
  @Override
  public Object visit(Block block, EvalEnv env) {
    EvalEnv newEnv = new EvalEnv(new Scope(env.getScope()), env.getEchoer(), env.getLabel());
    for(Instr instr: block.getInstrStar()) {
      eval(instr, newEnv);
    }
    return null;
  }
  
  @Override
  public Object visit(InstrXmls instr_xmls, EvalEnv env) {
    env.getEchoer().echo(eval(instr_xmls.getXmls(), env));
    return null;
  }
  
  private boolean checkBoolean(Node node, EvalEnv env) {
    Object value = eval(node, env);
    if (!(value instanceof Boolean)) {
      throw RT.error("condition must be a boolean: %s", value);
    }
    return (Boolean)value;
  }
  
  @Override
  public Object visit(InstrIf instr_if, EvalEnv env) {
    boolean condition = checkBoolean(instr_if.getExpr(), env);
    if(condition) {
      eval(instr_if.getInstr(), env);  
    } else {
      eval(instr_if.getElseIf(), env);
    }
    return null;
  }
  
  @Override
  public Object visit(ElseIfEmpty else_if_empty, EvalEnv env) {
    return null;
  }
  
  @Override
  public Object visit(ElseIfElse else_if_else, EvalEnv env) {
    return eval(else_if_else.getInstr(), env);
  }
  
  @Override
  public Object visit(ElseIfElseIf else_if_else_if, EvalEnv env) {
    Object value = eval(else_if_else_if.getExpr(), env);
    if (!(value instanceof Boolean)) {
      throw RT.error("condition must be a boolean: %s", value);
    }
    
    if((Boolean)value) {
      eval(else_if_else_if.getInstr(), env);  
    } else {
      eval(else_if_else_if.getElseIf(), env);
    }
    return null;
  }
  
  
  // --- labeled instructions
  
  @Override
  public Object visit(LabeledInstrWhile labeled_instr_while, EvalEnv env) {
    Instr instr = labeled_instr_while.getInstr();
    Expr expr = labeled_instr_while.getExpr();
    try {
      for(int counter = 0; ; counter++) {
        if (counter == LOOP_COUNTER_THRESOLD) {
          Compiler.traceCompile(labeled_instr_while, env);
          break;
        }
        if (!checkBoolean(expr, env))
          break;
        try {
          eval(instr, env);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
      }
    } catch(BreakError e) {
      e.mayRethrow(env);
    }
    return null;
  }
  
  @Override
  public Object visit(LabeledInstrDoWhile labeled_instr_do_while, EvalEnv env) {
    Instr instr = labeled_instr_do_while.getInstr();
    Expr expr = labeled_instr_do_while.getExpr();
    try {
      do {
        try {
          eval(instr, env);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
      } while(checkBoolean(expr, env));
    } catch(BreakError e) {
      e.mayRethrow(env); 
    }
    return null;
  }
  
  @Override
  public Object visit(LabeledInstrFor labeled_instr_for, EvalEnv env) {
    ForInit init = labeled_instr_for.getForInitOptional();
    Expr expr = labeled_instr_for.getExprOptional();
    Instr instr = labeled_instr_for.getInstr();
    ForStep step = labeled_instr_for.getForStepOptional();
    if (init != null) {
      eval(init, env);
    }
    try {
      if (expr == null) {
        for(;;) {
          try {
            eval(instr, env);
          } catch(ContinueError e) {
            e.mayRethrow(env);
          }
          if (step != null) {
            eval(step, env);
          }
        }
      } else {
        while(checkBoolean(expr, env)) {
          try {
            eval(instr, env);
          } catch(ContinueError e) {
            e.mayRethrow(env);
          }
          if (step != null) {
            eval(step, env);
          }
        }
      }
    } catch(BreakError e) {
      e.mayRethrow(env);
    }
    return null;
  }
  @Override
  public Object visit(ForInitAssignment for_init_assignment, EvalEnv env) {
    eval(for_init_assignment.getAssignment(), env);
    return null;
  }
  @Override
  public Object visit(ForInitDeclaration for_init_declaration, EvalEnv env) {
    eval(for_init_declaration.getDeclaration(), env);
    return null;
  }
  @Override
  public Object visit(ForInitFuncall for_init_funcall, EvalEnv env) {
    eval(for_init_funcall.getFuncall(), env);
    return null;
  }
  @Override
  public Object visit(ForStepAssignment for_step_assignment, EvalEnv env) {
    eval(for_step_assignment.getAssignment(), env);
    return null;
  }
  @Override
  public Object visit(ForStepFuncall for_step_funcall, EvalEnv env) {
    eval(for_step_funcall.getFuncall(), env);
    return null;
  }
  
  @Override
  public Object visit(LabeledInstrForeach labeled_instr_foreach, EvalEnv env) {
    Expr expr = labeled_instr_foreach.getExpr();
    Object expression = eval(expr, env);
    Sequence sequence = RT.toSequence(expression);
    if (sequence == null)
      return null;
    
    String name = labeled_instr_foreach.getId().getValue();
    
    Instr instr = labeled_instr_foreach.getInstr();
    try {
      while(sequence != null) {
        Scope foreachScope = new Scope(env.getScope());
        EvalEnv foreachEnv = new EvalEnv(foreachScope, env.getEchoer(), env.getLabel());
        foreachScope.register(new Var(name, true, PrimitiveType.ANY, sequence.getValue()));
        
        try {
          eval(instr, foreachEnv);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
        sequence = sequence.next();
      }
    } catch(BreakError e) {
      e.mayRethrow(env);
    }
    return null;
  }
  
  @Override
  public Object visit(LabeledInstrForeachEntry labeled_instr_foreach_entry, EvalEnv env) {
    Expr expr = labeled_instr_foreach_entry.getExpr();
    Object expression = eval(expr, env);
    Sequence sequence = RT.toSequence(expression);
    if (sequence == null)
      return null;
    
    String keyName = labeled_instr_foreach_entry.getId().getValue();
    String valueName = labeled_instr_foreach_entry.getId2().getValue();
    
    Instr instr = labeled_instr_foreach_entry.getInstr();
    try {
      while(sequence != null) {
        Scope foreachScope = new Scope(env.getScope());
        EvalEnv foreachEnv = new EvalEnv(foreachScope, env.getEchoer(), env.getLabel());
        foreachScope.register(new Var(keyName, true, PrimitiveType.ANY, sequence.getKey()));
        foreachScope.register(new Var(valueName, true, PrimitiveType.ANY, sequence.getValue()));
        
        try {
          eval(instr, foreachEnv);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
        sequence = sequence.next();
      }
    } catch(BreakError e) {
      e.mayRethrow(env);
    }
    return null;
  }
  
  // --- declaration & assignment
  
  
  @Override
  public Object visit(LetDeclaration let_declaration, EvalEnv env) {
    String name = let_declaration.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    Object value = eval(let_declaration.getExpr(), env);
    Var var = new Var(name, true, PrimitiveType.ANY, value);
    scope.register(var);
    return null;
  }
  @Override
  public Object visit(DeclarationLet declaration_let, EvalEnv env) {
    return eval(declaration_let.getLetDeclaration(), env);
  }
  @Override
  public Object visit(DeclarationTypeEmpty declaration_type_empty, EvalEnv env) {
    String name = declaration_type_empty.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    PrimitiveType type = (PrimitiveType)eval(declaration_type_empty.getType(), env);
    Var var = new Var(name, false, type, type.getDefaultValue());
    scope.register(var);
    return null;
  }
  @Override
  public Object visit(DeclarationTypeInit declaration_type_init, EvalEnv env) {
    String name = declaration_type_init.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    Object value = eval(declaration_type_init.getExpr(), env);
    PrimitiveType type = (PrimitiveType)eval(declaration_type_init.getType(), env);
    
    Var var = new Var(name, false, type, value);
    scope.register(var);
    return null;
    
  }
  
  @Override
  public Object visit(AssignmentId assignment_id, EvalEnv env) {
    String name = assignment_id.getId().getValue();
    Scope scope = env.getScope();
    Var var = scope.lookup(name);
    
    Object value = eval(assignment_id.getExpr(), env);
    if (var == null) {
      // auto declaration
      var = new Var(name, false, PrimitiveType.ANY, value);
      scope.register(var);
    } else {
      var.setValue(value);
    }
    return null;
  }
  
  @Override
  public Object visit(AssignmentArray assignment_array, EvalEnv env) {
    String name = assignment_array.getId().getValue();
    Var var = env.getScope().lookup(name);
    if (var == null) {
      throw RT.error("unknown variable named %s", name);
    } 
    
    Object key = eval(assignment_array.getExpr(), env);
    Object value = eval(assignment_array.getExpr2(), env);  
    RT.interpreterArraySet(assignment_array, var.getValue(), key, value, false);
    return null;
  }
  
  @Override
  public Object visit(AssignmentPrimaryArray assignment_primary_array, EvalEnv env) {
    Object array = eval(assignment_primary_array.getPrimary(), env);
    Object key = eval(assignment_primary_array.getExpr(), env);
    Object value = eval(assignment_primary_array.getExpr2(), env);
    
    RT.interpreterArraySet(assignment_primary_array, array, key, value, false);
    return null;
  }
  
  @Override
  public Object visit(AssignmentField assignment_field, EvalEnv env) {
    String name = assignment_field.getId().getValue();
    Scope scope = env.getScope();
    Var var = scope.lookup(name);
    if (var == null) {
      throw RT.error("unknown variable named %s", name);
    } 
    
    Object value = eval(assignment_field.getExpr(), env);  
    RT.interpreterArraySet(assignment_field, var.getValue(), assignment_field.getId2().getValue(), value, true);
    return null;
  }
  
  @Override
  public Object visit(AssignmentPrimaryField assignment_primary_field, EvalEnv env) {
    Object array = eval(assignment_primary_field.getPrimary(), env);
    Object value = eval(assignment_primary_field.getExpr(), env);
    
    RT.interpreterArraySet(assignment_primary_field, array, assignment_primary_field.getId().getValue(), value, true);
    return null;
  }
  
  @Override
  public Object visit(InstrSql instr_sql, EvalEnv env) {
    Scope scope = env.getScope();
    SQLConnection sqlConnection = (SQLConnection)lookupVarValue("SQL_CONNECTION", scope);
    sqlConnection.executeStatement(instr_sql.getSql(), env);
    return null;
  }
  
  @Override
  public Object visit(InstrFlwor instr_flwor, EvalEnv evalEnv) {
    return XPathExprVisitor.INSTANCE.flwor(instr_flwor.getFlwor(), evalEnv);
  }
  
  
  // --- function call
  
  @Override
  public Object visit(FuncallCall funcall_call, EvalEnv env) {
    String name = funcall_call.getId().getValue();
    List<Expr> exprStar = funcall_call.getExprStar();
    Var var = env.getScope().lookup(name);
    if (var != null) {
      Object value = var.getValue();
      if (!(value instanceof Function)) {
        throw RT.error("variable %s doesn't reference a function: %s", name, value);
      }
      return methodHandleCall(value, exprStar, env);
    }
    
    int size = exprStar.size();
    if (size == 0) {
      throw RT.error("%s is not a valid variable name", name);
    }
    
    Object[] values = new Object[size];
    for(int i=0; i<size; i++) {
      Expr expr = exprStar.get(i);
      values[i] = eval(expr, env);
    }
    
    return RT.interpreterMethodCall(funcall_call, name, values);
  }
  
  @Override
  public Object visit(FuncallApply funcall_apply, EvalEnv env) {
    Object primary = eval(funcall_apply.getPrimary(), env);
    if (!(primary instanceof Function)) {
      throw RT.error("expression is not a function: %s", primary);
    }
    return methodHandleCall(primary, funcall_apply.getExprStar(), env);
  }
  
  private Object methodHandleCall(Object value, List<Expr> exprStar, EvalEnv env) {
    int size = exprStar.size();
    Object[] values = new Object[1 + size];
    values[0] = env;
    for(int i = 0; i< size; i++) {
      Expr expr = exprStar.get(i);
      values[i + 1] = eval(expr, env);
    }
    
    try {
      return ((Function)value).getMethodHandle().invokeVarargs(values);
    } catch(Error e) {
      throw e;
    } catch (Throwable e) {
      throw RT.error((Node)null, e);
    }
  }
  
  
  // --- primary
  
  @Override
  public Object visit(PrimaryParens primary_parens, EvalEnv env) {
    return eval(primary_parens.getExpr(), env);
  }
  
  @Override
  public Object visit(PrimaryFuncall primary_funcall, EvalEnv env) {
    return eval(primary_funcall.getFuncall(), env);
  }
  /*
  private static Object arrayGet(Object array, Object key, boolean keyMustExist) {
    if (!(array instanceof ArrayAccess)) {
      throw RT.error("value is not as an array or a cursor: %s", array);
    }
    Object value = ((ArrayAccess)array).get(key);
    if (keyMustExist && value == null) {
      throw RT.error("member %s doesn't exist for array/cursor: %s", key, array);
    }
    return value;
  }*/
  
  @Override
  public Object visit(PrimaryArrayAccess primary_array_access, EvalEnv env) {
    Object array = lookupVarValue(primary_array_access.getId().getValue(), env.getScope());
    Object key = eval(primary_array_access.getExpr(), env);
    return RT.interpreterArrayGet(primary_array_access, array, key, false);
  }
  @Override
  public Object visit(PrimaryPrimaryArrayAccess primary_primary_array_access, EvalEnv env) {
    Object array = eval(primary_primary_array_access.getPrimary(), env);
    Object key = eval(primary_primary_array_access.getExpr(), env);
    return RT.interpreterArrayGet(primary_primary_array_access, array, key, false);
  }
  @Override
  public Object visit(PrimaryFieldAccess primary_field_access, EvalEnv env) {
    Object array = lookupVarValue(primary_field_access.getId().getValue(), env.getScope());
    String key = primary_field_access.getId2().getValue();
    return RT.interpreterArrayGet(primary_field_access, array, key, true);
  }
  @Override
  public Object visit(PrimaryPrimaryFieldAccess primary_primary_field_access, EvalEnv env) {
    Object array = eval(primary_primary_field_access.getPrimary(), env);
    String key = primary_primary_field_access.getId().getValue();
    return RT.interpreterArrayGet(primary_primary_field_access, array, key, true);
  }
  
  // --- expressions

  @Override
  public Object visit(ExprPrimary expr_primary, EvalEnv env) {
    return eval(expr_primary.getPrimary(), env);
  }

  @Override
  public Object visit(ExprLiteral expr_literal, EvalEnv env) {
    return eval(expr_literal.getLiteral(), env);
  }
  
  @Override
  public Object visit(ExprId expr_id, EvalEnv env) {
    String name = expr_id.getId().getValue();
    return lookupVarValue(name, env.getScope());
  }
  
  @Override
  public Object visit(ExprXmls expr_xmls, EvalEnv env) {
    return eval(expr_xmls.getXmls(), env);
  }
  
  @Override
  public Object visit(ExprUri expr_uri, EvalEnv env) {
    return URIVisitor.INSTANCE.eval(expr_uri.getUri(), env);
  }
  
  @Override
  public Object visit(ExprFun expr_fun, EvalEnv env) {
    return createFunction(true, "it", expr_fun.getParameters(), expr_fun.getBlock(), env);
  }

  @Override
  protected Object visit(Expr expr, EvalEnv env) {
    List<Node> nodeList = expr.nodeList();
    Node unaryNode = nodeList.get(0);
    Object value = eval(unaryNode, env);
    ProductionEnum kind = expr.getKind();
    switch(kind) {
    case expr_unary_plus:
      return RT.unary_plus(value);
    case expr_unary_minus:
      return RT.unary_minus(value);
    case expr_unary_not:
      return RT.unary_not(value);
    default:
    }

    Object left = value;
    Node binaryNode = nodeList.get(1);
    Object right = eval(binaryNode, env);
    switch(kind) {
    case expr_plus:
      return RT.plus(left, right);
    case expr_minus:
      return RT.minus(left, right);
    case expr_mult:
      return RT.mult(left, right);
    case expr_div:
      return RT.div(left, right);
    case expr_mod:
      return RT.mod(left, right);  

    case expr_eq:
      return RT.eq(left, right);
    case expr_ne:
      return RT.ne(left, right);

    case expr_lt:
      return RT.lt(left, right);
    case expr_le:
      return RT.le(left, right);
    case expr_gt:
      return RT.gt(left, right);
    case expr_ge:
      return RT.ge(left, right);

    default:
    }

    throw new AssertionError("unknown expression "+kind);
  }

 
  // --- type conversions
  
  @Override
  public Object visit(com.googlecode.phpreboot.ast.Type type, EvalEnv env) {
    String name = type.getId().getValue();
    return PrimitiveType.lookup(name);
  }
  
  @Override
  public Object visit(ExprToType expr_to_type, EvalEnv env) {
    Object value = eval(expr_to_type.getExpr(), env);
    PrimitiveType type = (PrimitiveType)eval(expr_to_type.getType(), env);
    switch(type) {
    case ANY:
      return value;
    case BOOLEAN:
      return RT.toBoolean(value);
    case INT:
      return RT.toInt(value);
    case DOUBLE:
      return RT.toDouble(value);
    case STRING:
      return RT.toString(value);
    case ARRAY:
      return RT.toArray(value);
    case SEQUENCE:
      return RT.toSequence(value);
    default:
      throw new AssertionError("conversion to an unknown type "+type);
    }
  }
  
  // --- literals

  @Override
  public Object visit(LiteralSingle literal_single, EvalEnv env) {
    return eval(literal_single.getSingleLiteral(), env);
  }

  @Override
  public Object visit(LiteralValue literal_value, EvalEnv env) {
    return literal_value.getValueLiteral().getValue();
  }
  @Override
  public Object visit(LiteralBool literal_bool, EvalEnv env) {
    return literal_bool.getBoolLiteral().getValue();
  }
  @Override
  public Object visit(LiteralString literal_string, EvalEnv env) {
    return literal_string.getStringLiteral().getValue();
  }
  @Override
  public Object visit(LiteralNull literal_null, EvalEnv env) {
    return null;
  }
  
  
  // --- array literal
  
  @Override
  public Object visit(LiteralArray literal_array, EvalEnv env) {
    Array array = new Array();
    for(ArrayValue arrayValue: literal_array.getArrayValueStar()) {
      Object value = eval(arrayValue, env);
      if (value instanceof Array.Entry) {
        array.__set__((Array.Entry)value);
      } else {
        array.add(value);
      }
    }
    return array;
  }
  @Override
  public Object visit(LiteralArrayEntry literal_array_entry, EvalEnv env) {
    Array array = new Array();
    for(ArrayEntry arrayEntry: literal_array_entry.getArrayEntryStar()) {
      Array.Entry entry = (Array.Entry)eval(arrayEntry, env);
      array.__set__(entry);
    }
    return array;
  }
  
  @Override
  public Object visit(ArrayEntry array_entry, EvalEnv env) {
    Object key = eval(array_entry.getExpr(), env);
    Object value = eval(array_entry.getExpr2(), env);
    return new Array.Entry(key, value);
  }
  @Override
  public Object visit(ArrayValueSingle array_value_single, EvalEnv env) {
    return eval(array_value_single.getExpr(), env);
  }
  @Override
  public Object visit(ArrayValueEntry array_value_entry, EvalEnv env) {
    return eval(array_value_entry.getArrayEntry(), env);
  }
  
  // --- dollar access
  
  @Override
  public Object visit(DollarAccessId dollar_access_id, EvalEnv env) {
    String name = dollar_access_id.getId().getValue();
    return lookupVarValue(name, env.getScope());
  }
  @Override
  public Object visit(DollarAccessExpr dollar_access_expr, EvalEnv env) {
    return eval(dollar_access_expr.getExpr(), env);
  }
  
  
  // --- xml
  
  @Override
  public Object visit(XmlsStartEndTag xmls_start_end_tag, EvalEnv env) {
    String name = xmls_start_end_tag.getId().getValue();
    String endName = xmls_start_end_tag.getId2().getValue();
    if (!name.equals(endName)) {
      throw RT.error("invalid end tag %s for tag %s", endName, name);
    }
    
    Array attributes = (Array)eval(xmls_start_end_tag.getAttrs(), env);
    Array elements = (Array)eval(xmls_start_end_tag.getContent(), env);
    return new XML(name, attributes, elements);
  }
  
  @Override
  public Object visit(XmlsEmptyTag xmls_empty_tag, EvalEnv env) {
    String name = xmls_empty_tag.getId().getValue();
    Array attributes = (Array)eval(xmls_empty_tag.getAttrs(), env);
    return new XML(name, attributes, new Array()); 
  }
  
  @Override
  public Object visit(AttrsEmpty attributes_empty, EvalEnv env) {
    return new Array();
  }
  @Override
  public Object visit(AttrsStringLiteral attributes_string_literal, EvalEnv env) {
    Array array = (Array)eval(attributes_string_literal.getAttrs(), env);
    array.set(attributes_string_literal.getId().getValue(), attributes_string_literal.getStringLiteral().getValue());
    return array;
  }
  @Override
  public Object visit(AttrsDollarAccess attrs_dollar_access, EvalEnv env) {
    Array array = (Array)eval(attrs_dollar_access.getAttrs(), env);
    array.set(attrs_dollar_access.getId().getValue(), eval(attrs_dollar_access.getDollarAccess(), env));
    return array;
  }
  
  @Override
  public Object visit(ContentEmpty content_empty, EvalEnv env) {
    return new Array();
  }
  @Override
  public Object visit(ContentText content_text, EvalEnv env) {
    Array array = (Array)eval(content_text.getContent(), env);
    array.add(content_text.getXmlText().getValue());
    return array;
  }
  @Override
  public Object visit(ContentXmls content_xmls, EvalEnv env) {
    Array array = (Array)eval(content_xmls.getContent(), env);
    array.add(eval(content_xmls.getXmls(), env));
    return array;
  }
  @Override
  public Object visit(ContentBlock content_block, EvalEnv env) {
    Array array = (Array)eval(content_block.getContent(), env);
    EvalEnv xmlEnv = new EvalEnv(env.getScope(), Echoer.xmlEchoer(array), env.getLabel());
    eval(content_block.getBlock(), xmlEnv);
    return array;
  }
  @Override
  public Object visit(ContentDollarAccess content_dollar_access, EvalEnv env) {
    Array array = (Array)eval(content_dollar_access.getContent(), env);
    array.add(String.valueOf(eval(content_dollar_access.getDollarAccess(), env)));
    return array;
  }
  
  
  // --- regex literal
  
  @Override
  public Object visit(ExprRegexMatch expr_regex_match, EvalEnv env) {
    return RegexEvaluator.visit(expr_regex_match, env);
  }
  
  @Override
  public Object visit(ExprRegexReplace expr_regex_replace, EvalEnv env) {
    return RegexEvaluator.visit(expr_regex_replace, env);
  }
}