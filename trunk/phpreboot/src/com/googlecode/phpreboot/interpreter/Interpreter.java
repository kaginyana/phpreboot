package com.googlecode.phpreboot.interpreter;

import java.dyn.MethodHandle;
import java.util.List;

import com.googlecode.phpreboot.ast.ASTGrammarEvaluator;
import com.googlecode.phpreboot.ast.ArrayValue;
import com.googlecode.phpreboot.ast.ArrayValueEntry;
import com.googlecode.phpreboot.ast.ArrayValueSingle;
import com.googlecode.phpreboot.ast.Assignment;
import com.googlecode.phpreboot.ast.AssignmentArray;
import com.googlecode.phpreboot.ast.AssignmentField;
import com.googlecode.phpreboot.ast.AssignmentId;
import com.googlecode.phpreboot.ast.AssignmentPrimaryArray;
import com.googlecode.phpreboot.ast.AssignmentPrimaryField;
import com.googlecode.phpreboot.ast.AttrsEmpty;
import com.googlecode.phpreboot.ast.AttrsStringLiteral;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.BoolLiteralToken;
import com.googlecode.phpreboot.ast.ContentEmpty;
import com.googlecode.phpreboot.ast.ContentText;
import com.googlecode.phpreboot.ast.ContentXmls;
import com.googlecode.phpreboot.ast.Declaration;
import com.googlecode.phpreboot.ast.DeclarationLet;
import com.googlecode.phpreboot.ast.DeclarationTypeEmpty;
import com.googlecode.phpreboot.ast.DeclarationTypeInit;
import com.googlecode.phpreboot.ast.DoToken;
import com.googlecode.phpreboot.ast.ElseIf;
import com.googlecode.phpreboot.ast.ElseIfElse;
import com.googlecode.phpreboot.ast.ElseIfElseIf;
import com.googlecode.phpreboot.ast.ElseIfEmpty;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.ExprId;
import com.googlecode.phpreboot.ast.ExprPrimary;
import com.googlecode.phpreboot.ast.ExprXmls;
import com.googlecode.phpreboot.ast.ForInit;
import com.googlecode.phpreboot.ast.ForStep;
import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.ast.Funcall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.IfToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.InstrEcho;
import com.googlecode.phpreboot.ast.InstrIf;
import com.googlecode.phpreboot.ast.InstrLabeled;
import com.googlecode.phpreboot.ast.InstrXmls;
import com.googlecode.phpreboot.ast.Label;
import com.googlecode.phpreboot.ast.LabeledInstr;
import com.googlecode.phpreboot.ast.LabeledInstrDoWhile;
import com.googlecode.phpreboot.ast.LabeledInstrFor;
import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.LcurlToken;
import com.googlecode.phpreboot.ast.LiteralArray;
import com.googlecode.phpreboot.ast.LiteralBool;
import com.googlecode.phpreboot.ast.LiteralNull;
import com.googlecode.phpreboot.ast.LiteralSingle;
import com.googlecode.phpreboot.ast.LiteralString;
import com.googlecode.phpreboot.ast.LiteralValue;
import com.googlecode.phpreboot.ast.Member;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NullLiteralToken;
import com.googlecode.phpreboot.ast.Parameter;
import com.googlecode.phpreboot.ast.PrimaryLiteral;
import com.googlecode.phpreboot.ast.RcurlToken;
import com.googlecode.phpreboot.ast.Script;
import com.googlecode.phpreboot.ast.Signature;
import com.googlecode.phpreboot.ast.StringLiteralToken;
import com.googlecode.phpreboot.ast.TextToken;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.WhileToken;
import com.googlecode.phpreboot.ast.Xmls;
import com.googlecode.phpreboot.ast.XmlsEmptyTag;
import com.googlecode.phpreboot.ast.XmlsStartEndTag;
import com.googlecode.phpreboot.compiler.PrimitiveType;
import com.googlecode.phpreboot.compiler.Type;
import com.googlecode.phpreboot.compiler.TypeChecker;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.ScriptVar;
import com.googlecode.phpreboot.model.Symbol;
import com.googlecode.phpreboot.parser.ProductionEnum;
import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.XML;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

public class Interpreter extends ASTGrammarEvaluator implements TerminalEvaluator<CharSequence> {
  private final Evaluator evaluator = new Evaluator();
  private Scope currentScope;
  private int interpreter = 0;

  public Interpreter() {
    currentScope = new Scope(null);
  }

  // --- terminal evaluator
  
  @Override
  public IdToken id(CharSequence data) {
    return new IdToken(data.toString());
  }
  @Override
  public NullLiteralToken null_literal(CharSequence data) {
    return new NullLiteralToken();
  }
  @Override
  public BoolLiteralToken bool_literal(CharSequence data) {
    return new BoolLiteralToken(Boolean.parseBoolean(data.toString()));
  }
  @Override
  public ValueLiteralToken value_literal(CharSequence data) {
    String text = data.toString();
    Object value;
    try {
      value = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      value = Double.parseDouble(text);
    }
    return new ValueLiteralToken(value);
  }
  @Override
  public StringLiteralToken string_literal(CharSequence data) {
    return new StringLiteralToken(data.toString());
  }
  @Override
  public TextToken text(CharSequence data) {
    return new TextToken(data.toString());
  }

  @Override
  public LcurlToken lcurl(CharSequence data) {
    if (interpreter == 0) {
      // push a new scope
      currentScope = new Scope(currentScope);
    }
    return null;
  }
  @Override
  public RcurlToken rcurl(CharSequence data) {
    if (interpreter == 0) {
      // pop the current scope
      currentScope = currentScope.getParent();
    }
    return null;
  }
  
  @Override
  public IfToken _if(CharSequence data) {
    interpreter++;
    return null;
  }
  
  @Override
  public DoToken _do(CharSequence data) {
    return null;
  }
  @Override
  public WhileToken _while(CharSequence data) {
    return null;
  }
  
  @Override
  public void multiline_comment(CharSequence data) {
    // comments
  }
  @Override
  public void oneline_comment(CharSequence data) {
    // comments
  }
  
  
  // --- grammar evaluator

  @Override
  public Script script_member(Member member) {
    return null;
  }
  @Override
  public Script script_script_member(Script script, Member member) {
    return null;
  }
  
  @Override
  public Member member_instr(Instr instr) {
    return null;
  }
  @Override
  public Member member_fun(Fun fun) {
    return null;
  }
  
  
  // --- instructions
  
  @Override
  public Instr instr_echo(Expr expr) {
    Instr instr_echo = super.instr_echo(expr);
    if (interpreter != 0) {
      return instr_echo;
    }
    evaluator.eval(instr_echo, currentScope);
    return null;
  }
  
  @Override
  public Instr instr_xmls(Xmls xmls) {
    Instr instr_xmls = super.instr_xmls(xmls);
    if (interpreter != 0) { 
      return instr_xmls;
    }
    evaluator.eval(instr_xmls, currentScope);
    return null;
  }
  
  @Override
  public Instr instr_decl(Declaration declaration) {
    Instr instr_decl = super.instr_decl(declaration);
    if (interpreter != 0) {
      return instr_decl;
    }
    evaluator.eval(instr_decl, currentScope);
    return null; 
  }
  
  @Override
  public Instr instr_assign(Assignment assignment) {
    Instr instr_assign = super.instr_assign(assignment);
    if (interpreter != 0) {
      return instr_assign;
    }
    evaluator.eval(instr_assign, currentScope);
    return null;
  }
  
  @Override
  public Instr instr_funcall(Funcall funcall) {
    Instr instr_funcall = super.instr_funcall(funcall);
    if (interpreter != 0) {
      return instr_funcall;
    }
    evaluator.eval(instr_funcall, currentScope);
    return null;
  }
  
  @Override
  public Instr instr_if(IfToken _if, Expr expr, Instr instr, ElseIf else_if) {
    Instr instr_if = super.instr_if(_if, expr, instr, else_if);
    interpreter--;
    if (interpreter != 0) {
      return instr_if;
    }
    evaluator.eval(instr_if, currentScope);
    return null;
  }
  
  @Override
  public Instr instr_block(Block block) {
    Instr instr_block = super.instr_block(block);
    if (interpreter != 0) {
      return instr_block;
    }
    return null;
  }
  
  @Override
  public Instr instr_labeled(Label label_optional_3, LabeledInstr labeled_instr) {
    throw RT.error("NYI");
  }
  
  
  // --- function declaration
  
  @Override
  public Signature signature(com.googlecode.phpreboot.ast.Type type_optional_1, IdToken id, List<Parameter> parameters) {
    Signature signature = super.signature(type_optional_1, id, parameters);
    interpreter++;
    return signature;
  }
  
  @Override
  public Fun fun(Signature signature, Block block) {
    Fun fun = super.fun(signature, block);
    
    String name = signature.getId().getValue();
    Scope scope = currentScope;
    Symbol symbol = scope.lookup(name);
    if (symbol != null) {
      throw RT.error("a variable %s is already defined", name);
    }
    
    Function function = TypeChecker.createFunction(fun);
    scope.register(function);
    
    interpreter--;
    return fun;
  }

  

  static class Evaluator extends Visitor<Object, Scope, RuntimeException> {
    Object eval(Node node, Scope scope) {
      return node.accept(this, scope);
    }
    
    // --- helper methods
    
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
    protected Object visit(Instr instr, Scope scope) {
      for(Node node: instr.nodeList()) {
        eval(node, scope);
      }
      return null;
    }
    
    @Override
    public Object visit(InstrEcho instr_echo, Scope scope) {
      Object result = eval(instr_echo.getExpr(), scope);
      System.out.println(result);
      return null;
    }
    
    @Override
    public Object visit(InstrXmls instr_xmls, Scope scope) {
      Object result = eval(instr_xmls.getXmls(), scope);
      System.out.println(result);
      return null;
    }
    
    @Override
    public Object visit(InstrLabeled instr_labeled, Scope scope) {
      //FIXME need to register a label
      eval(instr_labeled.getLabeledInstr(), scope);
      return null;
    }
    
    @Override
    public Object visit(Block block, Scope scope) {
      Scope newScope = new Scope(scope);
      for(Instr instr: block.getInstrStar()) {
        eval(instr, newScope);
      }
      return null;
    }
    
    private boolean checkBoolean(Node node, Scope scope) {
      Object value = eval(node, scope);
      if (!(value instanceof Boolean)) {
        throw RT.error("condition must be a boolean: %s", value);
      }
      return (Boolean)value;
    }
    
    @Override
    public Object visit(InstrIf instr_if, Scope scope) {
      boolean condition = checkBoolean(instr_if.getExpr(), scope);
      if(condition) {
        eval(instr_if.getInstr(), scope);  
      } else {
        eval(instr_if.getElseIf(), scope);
      }
      return null;
    }
    
    @Override
    public Object visit(ElseIfEmpty else_if_empty, Scope scope) {
      return null;
    }
    
    @Override
    public Object visit(ElseIfElse else_if_else, Scope scope) {
      return eval(else_if_else.getInstr(), scope);
    }
    
    @Override
    public Object visit(ElseIfElseIf else_if_else_if, Scope scope) {
      Object value = eval(else_if_else_if.getExpr(), scope);
      if (!(value instanceof Boolean)) {
        throw RT.error("condition must be a boolean: %s", value);
      }
      
      if((Boolean)value) {
        eval(else_if_else_if.getInstr(), scope);  
      } else {
        eval(else_if_else_if.getElseIf(), scope);
      }
      return null;
    }
    
    
    // --- labeled instructions
    
    @Override
    public Object visit(LabeledInstrWhile labeled_instr_while, Scope scope) {
      Instr instr = labeled_instr_while.getInstr();
      Expr expr = labeled_instr_while.getExpr();
      while(checkBoolean(expr, scope)) {
        eval(instr, scope);
      }
      return null;
    }
    
    @Override
    public Object visit(LabeledInstrDoWhile labeled_instr_do_while, Scope scope) {
      Instr instr = labeled_instr_do_while.getInstr();
      Expr expr = labeled_instr_do_while.getExpr();
      do {
        eval(instr, scope);
      } while(checkBoolean(expr, scope));
      return null;
    }
    
    @Override
    public Object visit(LabeledInstrFor labeled_instr_for, Scope scope) {
      ForInit init = labeled_instr_for.getForInitOptional();
      Expr expr = labeled_instr_for.getExprOptional();
      Instr instr = labeled_instr_for.getInstr();
      ForStep step = labeled_instr_for.getForStepOptional();
      if (init != null) {
        eval(init, scope);
      }
      if (expr == null) {
        for(;;) {
          eval(instr, scope);
          if (step != null) {
            eval(step, scope);
          }
        }
      } else {
        while(checkBoolean(expr, scope)) {
          eval(instr, scope);
          if (step != null) {
            eval(step, scope);
          }
        }
      }
      return null;
    }
    
    
    
    
    // --- declaration & assignment
    
    @Override
    public Object visit(DeclarationLet declaration_let, Scope scope) {
      String name = declaration_let.getId().getValue();
      checkVar(name, scope);
      Object value = eval(declaration_let.getExpr(), scope);
      ScriptVar var = new ScriptVar(name, asType(value), value);
      scope.register(var);
      return null;
    }
    @Override
    public Object visit(DeclarationTypeEmpty declaration_type_empty, Scope scope) {
      String name = declaration_type_empty.getId().getValue();
      checkVar(name, scope);
      Type type = TypeChecker.asType(declaration_type_empty.getType());
      ScriptVar var = new ScriptVar(name, type, defaultValue(type));
      scope.register(var);
      return null;
    }
    @Override
    public Object visit(DeclarationTypeInit declaration_type_init, Scope scope) {
      String name = declaration_type_init.getId().getValue();
      checkVar(name, scope);
      Object value = eval(declaration_type_init.getExpr(), scope);
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
    public Object visit(AssignmentId assignment_id, Scope scope) {
      String name = assignment_id.getId().getValue();
      ScriptVar var = checkAssignmentVar(name, scope);
      
      Object value = eval(assignment_id.getExpr(), scope);
      if (var == null) {
        // auto declaration
        var = new ScriptVar(name, PrimitiveType.ANY, value);
        scope.register(var);
      } else {
        var.setValue(value);
      }
      return null;
    }
    
    private static void arraySet(Object varValue, Object key, Object value) {
      if (!(varValue instanceof Array)) {
        throw RT.error("value is not an array: %s", varValue);
      }
      ((Array)varValue).__set__(key, value);
    }
    
    @Override
    public Object visit(AssignmentArray assignment_array, Scope scope) {
      String name = assignment_array.getId().getValue();
      ScriptVar var = checkAssignmentVar(name, scope);
      if (var == null) {
        throw RT.error("unknown variable named %s", name);
      } 
      
      Object key = eval(assignment_array.getExpr(), scope);
      Object value = eval(assignment_array.getExpr2(), scope);  
      arraySet(var.getValue(), key, value);
      return null;
    }
    
    @Override
    public Object visit(AssignmentPrimaryArray assignment_primary_array, Scope scope) {
      Object array = eval(assignment_primary_array.getPrimary(), scope);
      Object key = eval(assignment_primary_array.getExpr(), scope);
      Object value = eval(assignment_primary_array.getExpr2(), scope);
      
      arraySet(array, key, value);
      return null;
    }
    
    @Override
    public Object visit(AssignmentField assignment_field, Scope scope) {
      String name = assignment_field.getId().getValue();
      ScriptVar var = checkAssignmentVar(name, scope);
      if (var == null) {
        throw RT.error("unknown variable named %s", name);
      } 
      
      Object value = eval(assignment_field.getExpr(), scope);  
      arraySet(var.getValue(), assignment_field.getId2().getValue(), value);
      return null;
    }
    
    @Override
    public Object visit(AssignmentPrimaryField assignment_primary_field, Scope scope) {
      Object array = eval(assignment_primary_field.getPrimary(), scope);
      Object value = eval(assignment_primary_field.getExpr(), scope);
      
      arraySet(array, assignment_primary_field.getId().getValue(), value);
      return null;
    }
    
    
    // --- function call
    
    @Override
    public Object visit(Funcall funcall, Scope scope) {
      String name = funcall.getId().getValue();
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
        values[i++] = eval(expr, scope);
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
    
    
    // --- expressions

    @Override
    public Object visit(ExprPrimary expr_primary, Scope scope) {
      return eval(expr_primary.getPrimary(), scope);
    }

    @Override
    public Object visit(ExprId expr_id, Scope scope) {
      String name = expr_id.getId().getValue();
      Symbol symbol = scope.lookup(name);
      if (symbol == null) {
        throw RT.error("variable %s not defined", name);
      }
      if (symbol instanceof ScriptVar) {
        ScriptVar var = (ScriptVar)symbol;
        return var.getValue();
      } else {
        throw RT.error("NYI");
      }
    }
    
    @Override
    public Object visit(ExprXmls expr_xmls, Scope scope) {
      return eval(expr_xmls.getXmls(), scope);
    }

    @Override
    protected Object visit(Expr expr, Scope scope) {
      List<Node> nodeList = expr.nodeList();
      Node unaryNode = nodeList.get(0);
      Object value = eval(unaryNode, scope);
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
      Object right = eval(binaryNode, scope);
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
    public Object visit(PrimaryLiteral primary_literal, Scope scope) {
      return eval(primary_literal.getLiteral(), scope);
    }

    @Override
    public Object visit(LiteralSingle literal_single, Scope scope) {
      return eval(literal_single.getSingleLiteral(), scope);
    }

    @Override
    public Object visit(LiteralValue literal_value, Scope scope) {
      return literal_value.getValueLiteral().getValue();
    }
    @Override
    public Object visit(LiteralBool literal_bool, Scope scope) {
      return literal_bool.getBoolLiteral().getValue();
    }
    @Override
    public Object visit(LiteralString literal_string, Scope scope) {
      return literal_string.getStringLiteral().getValue();
    }
    @Override
    public Object visit(LiteralNull literal_null, Scope scope) {
      return null;
    }
    
    
    // --- array literal
    
    @Override
    public Object visit(LiteralArray literal_array, Scope scope) {
      Array array = new Array();
      for(ArrayValue arrayValue: literal_array.getArrayValueStar()) {
        Object value = eval(arrayValue, scope);
        if (value instanceof Array.Entry) {
          array.__set__((Array.Entry)value);
        } else {
          array.__add__(value);
        }
      }
      return array;
    }
    
    @Override
    public Object visit(ArrayValueSingle array_value_single, Scope scope) {
      return eval(array_value_single.getLiteral(), scope);
    }
    
    @Override
    public Object visit(ArrayValueEntry array_value_entry, Scope scope) {
      Object key = eval(array_value_entry.getLiteral(), scope);
      Object value = eval(array_value_entry.getLiteral2(), scope);
      return new Array.Entry(key, value);
    }
    
    
    // --- xml
    
    @Override
    public Object visit(XmlsStartEndTag xmls_start_end_tag, Scope scope) {
      String name = xmls_start_end_tag.getId().getValue();
      String endName = xmls_start_end_tag.getId2().getValue();
      if (!name.equals(endName)) {
        throw RT.error("invalid end tag %s for tag %s", endName, name);
      }
      
      Array attributes = (Array)eval(xmls_start_end_tag.getAttrs(), scope);
      Array elements = (Array)eval(xmls_start_end_tag.getContent(), scope);
      return new XML(name, attributes, elements);
    }
    
    @Override
    public Object visit(XmlsEmptyTag xmls_empty_tag, Scope scope) {
      String name = xmls_empty_tag.getId().getValue();
      Array attributes = (Array)eval(xmls_empty_tag.getAttrs(), scope);
      return new XML(name, attributes, new Array()); 
    }
    
    @Override
    public Object visit(AttrsEmpty attributes_empty, Scope scope) {
      return new Array();
    }
    @Override
    public Object visit(AttrsStringLiteral attributes_string_literal, Scope scope) {
      Array array = (Array)eval(attributes_string_literal.getAttrs(), scope);
      array.__set__(attributes_string_literal.getId(), eval(attributes_string_literal.getStringLiteral(), scope));
      return array;
    }
    
    @Override
    public Object visit(ContentEmpty content_empty, Scope scope) {
      return new Array();
    }
    @Override
    public Object visit(ContentText content_text, Scope scope) {
      Array array = (Array)eval(content_text.getContent(), scope);
      array.__add__(content_text.getText().getValue()+' '); // FIXME should not add a space here ?
      return array;
    }
    @Override
    public Object visit(ContentXmls content_xmls, Scope scope) {
      Array array = (Array)eval(content_xmls.getContent(), scope);
      array.__add__(eval(content_xmls.getXmls(), scope));
      return array;
    }
  }
}
