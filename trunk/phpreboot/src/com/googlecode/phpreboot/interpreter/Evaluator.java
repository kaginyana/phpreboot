package com.googlecode.phpreboot.interpreter;

import java.dyn.MethodHandle;
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
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprLiteral;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.ExprXmls;
import com.googlecode.phpreboot.ast.ForInit;
import com.googlecode.phpreboot.ast.ForInitAssignment;
import com.googlecode.phpreboot.ast.ForInitDeclaration;
import com.googlecode.phpreboot.ast.ForInitFuncall;
import com.googlecode.phpreboot.ast.ForStep;
import com.googlecode.phpreboot.ast.ForStepAssignment;
import com.googlecode.phpreboot.ast.ForStepFuncall;
import com.googlecode.phpreboot.ast.Funcall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrBreak;
import com.googlecode.phpreboot.ast.InstrContinue;
import com.googlecode.phpreboot.ast.InstrEcho;
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
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralArrayEntry;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.PrimaryArrayAccess;
import com.googlecode.phpreboot.ast.PrimaryFieldAccess;
import com.googlecode.phpreboot.ast.PrimaryFuncall;
import com.googlecode.phpreboot.ast.PrimaryParens;
import com.googlecode.phpreboot.ast.PrimaryPrimaryArrayAccess;
import com.googlecode.phpreboot.ast.PrimaryPrimaryFieldAccess;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.XmlsEmptyTag;
import com.googlecode.phpreboot.ast.XmlsStartEndTag;
import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.compiler.Type;
import com.googlecode.phpreboot.compiler.TypeChecker;
import com.googlecode.phpreboot.interpreter.sql.SQLConnection;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.model.Symbol;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.ArrayAccess;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.XML;

public class Evaluator extends Visitor<Object, EvalEnv, RuntimeException> {
  @SuppressWarnings("serial")
  static class ReturnError extends Error {
    Object value;
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
  
  public Object eval(Node node, EvalEnv env) {
    return node.accept(this, env);
  }
  
  // --- helper methods
  
  private Object lookupVarValue(String name, Scope scope) {
    Symbol symbol = scope.lookup(name);
    if (symbol == null) {
      throw RT.error("variable %s not defined", name);
    }
    if (symbol instanceof ScriptVar) {
      ScriptVar var = (ScriptVar)symbol;
      return var.getValue();
    } else {
      throw RT.error("Function as variable, NYI");
    }
  }
  
  private static void checkVar(String name, Scope scope) {
    Symbol symbol = scope.lookup(name);
    if (symbol != null) {
      throw RT.error("symbol %s already exists", name);
    }
  }
  
  private static Type asType(Object value) {
    if (value instanceof Integer) {
      return PrimitiveType.INT;
    }
    if (value instanceof Boolean) {
      return PrimitiveType.BOOLEAN;
    }
    if (value instanceof String) {
      return PrimitiveType.STRING;
    }
    if (value instanceof Double) {
      return PrimitiveType.DOUBLE;
    }
    return PrimitiveType.ANY;
  }
  
  private static Object defaultValue(Type type) {
    if (!(type instanceof PrimitiveType)) {
      return null;
    }
    
    switch((PrimitiveType)type) {
    case BOOLEAN:
      return false;
    case INT:
      return 0;
    case DOUBLE:
      return 0.0;
    case STRING:
      return "";
    default:
      return null;
    }
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
      while(checkBoolean(expr, env)) {
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
    Sequence sequence = RT.foreach_expression(expression);
    if (sequence == null)
      return null;
    
    Scope foreachScope = new Scope(env.getScope());
    EvalEnv foreachEnv = new EvalEnv(foreachScope, env.getEchoer(), env.getLabel());
    String name = labeled_instr_foreach.getId().getValue();
    ScriptVar value = new ScriptVar(name, PrimitiveType.ANY, null);
    foreachScope.register(value);
    
    Instr instr = labeled_instr_foreach.getInstr();
    try {
      while(sequence != null) {
        value.setValue(sequence.__value__());
        try {
          eval(instr, foreachEnv);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
        sequence = sequence.__next__();
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
    Sequence sequence = RT.foreach_expression(expression);
    if (sequence == null)
      return null;
    
    Scope foreachScope = new Scope(env.getScope());
    EvalEnv foreachEnv = new EvalEnv(foreachScope, env.getEchoer(), env.getLabel());
    String keyName = labeled_instr_foreach_entry.getId().getValue();
    String valueName = labeled_instr_foreach_entry.getId2().getValue();
    ScriptVar key = new ScriptVar(keyName, PrimitiveType.ANY, null);
    ScriptVar value = new ScriptVar(valueName, PrimitiveType.ANY, null);
    foreachScope.register(key);
    foreachScope.register(value);
    
    Instr instr = labeled_instr_foreach_entry.getInstr();
    try {
      while(sequence != null) {
        key.setValue(sequence.__key__());
        value.setValue(sequence.__value__());
        try {
          eval(instr, foreachEnv);
        } catch(ContinueError e) {
          e.mayRethrow(env);
        }
        sequence = sequence.__next__();
      }
    } catch(BreakError e) {
      e.mayRethrow(env);
    }
    return null;
  }
  
  // --- declaration & assignment
  
  @Override
  public Object visit(DeclarationLet declaration_let, EvalEnv env) {
    String name = declaration_let.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    Object value = eval(declaration_let.getExpr(), env);
    ScriptVar var = new ScriptVar(name, asType(value), value);
    scope.register(var);
    return null;
  }
  @Override
  public Object visit(DeclarationTypeEmpty declaration_type_empty, EvalEnv env) {
    String name = declaration_type_empty.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    Type type = TypeChecker.asType(declaration_type_empty.getType());
    ScriptVar var = new ScriptVar(name, type, defaultValue(type));
    scope.register(var);
    return null;
  }
  @Override
  public Object visit(DeclarationTypeInit declaration_type_init, EvalEnv env) {
    String name = declaration_type_init.getId().getValue();
    Scope scope = env.getScope();
    checkVar(name, scope);
    Object value = eval(declaration_type_init.getExpr(), env);
    Type type = TypeChecker.asType(declaration_type_init.getType());
    
    TypeChecker.isCompatible(type, asType(value));
    
    ScriptVar var = new ScriptVar(name, type, value);
    scope.register(var);
    return null;
  }
  
  
  private static ScriptVar checkAssignmentVar(String name, Scope scope) {
    Symbol symbol = scope.lookup(name);
    if (symbol == null) {
      return null;
    } 
    if (symbol instanceof ScriptVar) {
      return (ScriptVar)symbol;
    } 
    throw RT.error("illegal assignment, %s is a function", name);
  }
  
  @Override
  public Object visit(AssignmentId assignment_id, EvalEnv env) {
    String name = assignment_id.getId().getValue();
    Scope scope = env.getScope();
    ScriptVar var = checkAssignmentVar(name, scope);
    
    Object value = eval(assignment_id.getExpr(), env);
    if (var == null) {
      // auto declaration
      var = new ScriptVar(name, PrimitiveType.ANY, value);
      scope.register(var);
    } else {
      var.setValue(value);
    }
    return null;
  }
  
  private static void arraySet(Object varValue, Object key, Object value, boolean keyMustExist) {
    if (!(varValue instanceof Array)) {
      throw RT.error("value is not an array: %s", varValue);
    }
    Array array = (Array)varValue;
    if (keyMustExist && array.__get__(key) == null) {
      throw RT.error("member %s doesn't exist for array: %s", key, array);
    }
    array.__set__(key, value);
  }
  
  @Override
  public Object visit(AssignmentArray assignment_array, EvalEnv env) {
    String name = assignment_array.getId().getValue();
    ScriptVar var = checkAssignmentVar(name, env.getScope());
    if (var == null) {
      throw RT.error("unknown variable named %s", name);
    } 
    
    Object key = eval(assignment_array.getExpr(), env);
    Object value = eval(assignment_array.getExpr2(), env);  
    arraySet(var.getValue(), key, value, false);
    return null;
  }
  
  @Override
  public Object visit(AssignmentPrimaryArray assignment_primary_array, EvalEnv env) {
    Object array = eval(assignment_primary_array.getPrimary(), env);
    Object key = eval(assignment_primary_array.getExpr(), env);
    Object value = eval(assignment_primary_array.getExpr2(), env);
    
    arraySet(array, key, value, false);
    return null;
  }
  
  @Override
  public Object visit(AssignmentField assignment_field, EvalEnv env) {
    String name = assignment_field.getId().getValue();
    Scope scope = env.getScope();
    ScriptVar var = checkAssignmentVar(name, scope);
    if (var == null) {
      throw RT.error("unknown variable named %s", name);
    } 
    
    Object value = eval(assignment_field.getExpr(), env);  
    arraySet(var.getValue(), assignment_field.getId2().getValue(), value, true);
    return null;
  }
  
  @Override
  public Object visit(AssignmentPrimaryField assignment_primary_field, EvalEnv env) {
    Object array = eval(assignment_primary_field.getPrimary(), env);
    Object value = eval(assignment_primary_field.getExpr(), env);
    
    arraySet(array, assignment_primary_field.getId().getValue(), value, true);
    return null;
  }
  
  @Override
  public Object visit(InstrSql instr_sql, EvalEnv env) {
    Scope scope = env.getScope();
    SQLConnection sqlConnection = (SQLConnection)lookupVarValue("SQL_CONNECTION", scope);
    sqlConnection.executeStatement(instr_sql.getSql(), scope);
    return null;
  }
  
  // --- function call
  
  @Override
  public Object visit(Funcall funcall, EvalEnv env) {
    String name = funcall.getId().getValue();
    Scope scope = env.getScope();
    Symbol symbol = scope.lookup(name);
    if (symbol == null) {
      throw RT.error("unknown function %s", name);
    }
    com.googlecode.phpreboot.compiler.Type type = symbol.getType();
    if (!(type instanceof Function)) {
      throw RT.error("variable %s is not a function", name);
    }
    
    Function function = (Function)type;
    MethodHandle mh = function.getMethodHandle(scope);
    
    Object[] values = new Object[1 + funcall.getExprStar().size()];
    values[0] = scope;
    int i=1;
    for(Expr expr: funcall.getExprStar()) {
      values[i++] = eval(expr, env);
    }
    
    try {
      return mh.invokeVarargs(values);
    } catch (Throwable e) {
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      if (e instanceof Error) {
        throw (Error)e;
      }
      throw new RuntimeException(e);
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
  
  private static Object arrayGet(Object array, Object key, boolean keyMustExist) {
    if (!(array instanceof ArrayAccess)) {
      throw RT.error("value is not as an array or a cursor: %s", array);
    }
    Object value = ((ArrayAccess)array).__get__(key);
    if (keyMustExist && value == null) {
      throw RT.error("member %s doesn't exist for array/cursor: %s", key, array);
    }
    return value;
  }
  
  @Override
  public Object visit(PrimaryArrayAccess primary_array_access, EvalEnv env) {
    Object array = lookupVarValue(primary_array_access.getId().getValue(), env.getScope());
    Object key = eval(primary_array_access.getExpr(), env);
    return arrayGet(array, key, false);
  }
  @Override
  public Object visit(PrimaryPrimaryArrayAccess primary_primary_array_access, EvalEnv env) {
    Object array = eval(primary_primary_array_access.getPrimary(), env);
    Object key = eval(primary_primary_array_access.getExpr(), env);
    return arrayGet(array, key, false);
  }
  @Override
  public Object visit(PrimaryFieldAccess primary_field_access, EvalEnv env) {
    Object array = lookupVarValue(primary_field_access.getId().getValue(), env.getScope());
    String key = primary_field_access.getId2().getValue();
    return arrayGet(array, key, true);
  }
  @Override
  public Object visit(PrimaryPrimaryFieldAccess primary_primary_field_access, EvalEnv env) {
    Object array = eval(primary_primary_field_access.getPrimary(), env);
    String key = primary_primary_field_access.getId().getValue();
    return arrayGet(array, key, true);
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
        array.__add__(value);
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
    array.__set__(attributes_string_literal.getId(), eval(attributes_string_literal.getStringLiteral(), env));
    return array;
  }
  
  @Override
  public Object visit(ContentEmpty content_empty, EvalEnv env) {
    return new Array();
  }
  @Override
  public Object visit(ContentText content_text, EvalEnv env) {
    Array array = (Array)eval(content_text.getContent(), env);
    array.__add__(content_text.getText().getValue());
    return array;
  }
  @Override
  public Object visit(ContentXmls content_xmls, EvalEnv env) {
    Array array = (Array)eval(content_xmls.getContent(), env);
    array.__add__(eval(content_xmls.getXmls(), env));
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
    array.__add__(String.valueOf(eval(content_dollar_access.getDollarAccess(), env)));
    return array;
  }
}