package com.googlecode.phpreboot.interpreter;

import java.io.PrintWriter;
import java.util.List;

import com.googlecode.phpreboot.ast.ASTGrammarEvaluator;
import com.googlecode.phpreboot.ast.Action;
import com.googlecode.phpreboot.ast.Assignment;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.BoolLiteralToken;
import com.googlecode.phpreboot.ast.Declaration;
import com.googlecode.phpreboot.ast.ElseIf;
import com.googlecode.phpreboot.ast.Eoi;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.ast.Funcall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.Label;
import com.googlecode.phpreboot.ast.LabeledInstr;
import com.googlecode.phpreboot.ast.LcurlToken;
import com.googlecode.phpreboot.ast.Member;
import com.googlecode.phpreboot.ast.NullLiteralToken;
import com.googlecode.phpreboot.ast.Parameter;
import com.googlecode.phpreboot.ast.RcurlToken;
import com.googlecode.phpreboot.ast.Script;
import com.googlecode.phpreboot.ast.Signature;
import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.ast.StringLiteralToken;
import com.googlecode.phpreboot.ast.TextToken;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.Xmls;
import com.googlecode.phpreboot.compiler.TypeChecker;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Symbol;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

public class Interpreter extends ASTGrammarEvaluator implements TerminalEvaluator<CharSequence> {
  private final Echoer echoer;
  private final Evaluator evaluator;
  private Scope currentScope;
  private int interpreter = 0;

  public Interpreter(PrintWriter writer, Evaluator evaluator, Scope scope) {
    this.echoer = Echoer.writerEchoer(writer);
    this.evaluator = evaluator;
    this.currentScope = scope;
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
    return new StringLiteralToken(data.subSequence(1, data.length() - 1).toString());
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
  public Instr instr_echo(Expr expr, Eoi eoi) {
    Instr instr_echo = super.instr_echo(expr, eoi);
    if (interpreter != 0) {
      return instr_echo;
    }
    evaluator.eval(instr_echo, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  
  @Override
  public Instr instr_decl(Declaration declaration, Eoi eoi) {
    Instr instr_decl = super.instr_decl(declaration, eoi);
    if (interpreter != 0) {
      return instr_decl;
    }
    evaluator.eval(instr_decl, new EvalEnv(currentScope, echoer, null));
    return null; 
  }
  
  @Override
  public Instr instr_assign(Assignment assignment, Eoi eoi) {
    Instr instr_assign = super.instr_assign(assignment, eoi);
    if (interpreter != 0) {
      return instr_assign;
    }
    evaluator.eval(instr_assign, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  
  @Override
  public Instr instr_funcall(Funcall funcall, Eoi eoi) {
    Instr instr_funcall = super.instr_funcall(funcall, eoi);
    if (interpreter != 0) {
      return instr_funcall;
    }
    evaluator.eval(instr_funcall, new EvalEnv(currentScope, echoer, null));
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
  public Action action() {
    interpreter++;
    return null;
  }
  @Override
  public Instr instr_if(Action action, Expr expr, Instr instr, ElseIf else_if) {
    Instr instr_if = super.instr_if(action, expr, instr, else_if);
    interpreter--;     // match with action()
    if (interpreter != 0) {
      return instr_if;
    }
    evaluator.eval(instr_if, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  @Override
  public Instr instr_xmls(Action action, Xmls xmls) {
    Instr instr_xmls = super.instr_xmls(action, xmls);
    interpreter--;     // match with action()
    if (interpreter != 0) { 
      return instr_xmls;
    }
    evaluator.eval(instr_xmls, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  
  @Override
  public Label label_empty() {
    interpreter++;
    return super.label_empty();
  }
  @Override
  public Label label_id(IdToken id) {
    interpreter++;
    return super.label_id(id);
  }
  @Override
  public Instr instr_labeled(Label label, LabeledInstr labeled_instr) {
    Instr instr_labeled = super.instr_labeled(label, labeled_instr);
    interpreter--;   // match with label_empty() or label_id(IdToken)
    if (interpreter != 0)
      return instr_labeled;
    
    evaluator.eval(instr_labeled, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  
  @Override
  public Instr instr_return(Expr expr_optional_3, Eoi eoi) {
    Instr instr_return = super.instr_return(expr_optional_3, eoi);
    if (interpreter != 0)
      return instr_return;
    
    evaluator.eval(instr_return, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  @Override
  public Instr instr_break(IdToken id_optional_4, Eoi eoi) {
    Instr instr_break = super.instr_break(id_optional_4, eoi);
    if (interpreter != 0)
      return instr_break;
    
    evaluator.eval(instr_break, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  @Override
  public Instr instr_continue(IdToken id_optional_5, Eoi eoi) {
    Instr instr_continue = super.instr_continue(id_optional_5, eoi);
    if (interpreter != 0)
      return instr_continue;
    
    evaluator.eval(instr_continue, new EvalEnv(currentScope, echoer, null));
    return null;
  }
  
  @Override
  public Instr instr_sql(Sql sql, Eoi eoi) {
    Instr instr_sql = super.instr_sql(sql, eoi);
    interpreter--;   // match with action()
    if (interpreter != 0)
      return instr_sql;
    
    evaluator.eval(instr_sql, new EvalEnv(currentScope, echoer, null));
    return instr_sql;
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
}
