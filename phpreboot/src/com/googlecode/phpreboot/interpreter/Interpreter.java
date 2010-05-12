package com.googlecode.phpreboot.interpreter;

import java.io.PrintWriter;
import java.util.List;

import com.googlecode.phpreboot.ast.ASTGrammarEvaluator;
import com.googlecode.phpreboot.ast.Action;
import com.googlecode.phpreboot.ast.Assignment;
import com.googlecode.phpreboot.ast.AxisNameToken;
import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.BoolLiteralToken;
import com.googlecode.phpreboot.ast.Declaration;
import com.googlecode.phpreboot.ast.DisableLineComment;
import com.googlecode.phpreboot.ast.ElseIf;
import com.googlecode.phpreboot.ast.EnableLineComment;
import com.googlecode.phpreboot.ast.Eoi;
import com.googlecode.phpreboot.ast.Expr;
import com.googlecode.phpreboot.ast.Flwor;
import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.ast.Funcall;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Instr;
import com.googlecode.phpreboot.ast.Label;
import com.googlecode.phpreboot.ast.LabeledInstr;
import com.googlecode.phpreboot.ast.LcurlToken;
import com.googlecode.phpreboot.ast.Member;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NodeTypeToken;
import com.googlecode.phpreboot.ast.NullLiteralToken;
import com.googlecode.phpreboot.ast.Parameter;
import com.googlecode.phpreboot.ast.Parameters;
import com.googlecode.phpreboot.ast.PathIdToken;
import com.googlecode.phpreboot.ast.PortNumberToken;
import com.googlecode.phpreboot.ast.RcurlToken;
import com.googlecode.phpreboot.ast.RegexAnycharacterToken;
import com.googlecode.phpreboot.ast.ReturnType;
import com.googlecode.phpreboot.ast.RootDirToken;
import com.googlecode.phpreboot.ast.Script;
import com.googlecode.phpreboot.ast.Sql;
import com.googlecode.phpreboot.ast.StringLiteralToken;
import com.googlecode.phpreboot.ast.Type;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.XmlTextToken;
import com.googlecode.phpreboot.ast.Xmls;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

import fr.umlv.tatoo.runtime.buffer.impl.LocationTracker;

public class Interpreter extends ASTGrammarEvaluator implements TerminalEvaluator<CharSequence> {
  private final LocationTracker locationTracker;
  private final Echoer echoer;
  private Scope currentScope;
  private int interpreter = 0;
  boolean enableLineComment = true; //comments that starts with '//' clashes with xquery-like syntax
  
  public Interpreter(LocationTracker locationTracker, PrintWriter writer, Scope scope) {
    this.locationTracker = locationTracker;
    this.echoer = Echoer.writerEchoer(writer);
    this.currentScope = scope;
  }
  
  public Echoer getEchoer() {
    return echoer;
  }
  
  // --- helper methods
  
  private void eval(Node node) {
    Evaluator.INSTANCE.eval(node, new EvalEnv(currentScope, echoer, null));
  }

  // --- terminal evaluator
  
  @Override
  public IdToken id(CharSequence data) {
    return computeTokenAnnotation(new IdToken(data.toString()));
  }
  @Override
  public NullLiteralToken null_literal(CharSequence data) {
    return computeTokenAnnotation(new NullLiteralToken());
  }
  @Override
  public BoolLiteralToken bool_literal(CharSequence data) {
    return computeTokenAnnotation(new BoolLiteralToken(Boolean.parseBoolean(data.toString())));
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
    return computeTokenAnnotation(new ValueLiteralToken(value));
  }
  @Override
  public StringLiteralToken string_literal(CharSequence data) {
    return computeTokenAnnotation(new StringLiteralToken(data.subSequence(1, data.length() - 1).toString()));
  }
  @Override
  public XmlTextToken xml_text(CharSequence data) {
    return computeTokenAnnotation(new XmlTextToken(data.toString()));
  }
  
  @Override
  public RegexAnycharacterToken regex_anycharacter(CharSequence data) {
    return computeTokenAnnotation(new RegexAnycharacterToken(data.toString()));
  }
  
  
  // --- xpath
  
  @Override
  public AxisNameToken axis_name(CharSequence data) {
    return computeTokenAnnotation(new AxisNameToken(data.toString()));
  }
  @Override
  public NodeTypeToken node_type(CharSequence data) {
    return computeTokenAnnotation(new NodeTypeToken(data.toString()));
  }
  
  
  // --- uri
  
  @Override
  public RootDirToken root_dir(CharSequence data) {
    return computeTokenAnnotation(new RootDirToken(data.charAt(0))); 
  }
  @Override
  public PathIdToken path_id(CharSequence data) {
    return computeTokenAnnotation(new PathIdToken(data.toString()));
  }
  @Override
  public PortNumberToken port_number(CharSequence data) {
    int port = Integer.parseInt(data.toString());
    return computeTokenAnnotation(new PortNumberToken(port));
  }

  @Override
  public LcurlToken lcurl(CharSequence data) {
    if (interpreter == 0) {
      // push a new scope
      currentScope = new Scope(currentScope);
    }
    return computeTokenAnnotation(new LcurlToken());
  }
  @Override
  public RcurlToken rcurl(CharSequence data) {
    if (interpreter == 0) {
      // pop the current scope
      currentScope = currentScope.getParent();
    }
    return computeTokenAnnotation(new RcurlToken());
  }
  
  @Override
  public void multiline_comment(CharSequence data) {
    // comments
  }
  @Override
  public void oneline_comment(CharSequence data) {
    // comments
  }
  
  private <N extends Node> N computeTokenAnnotation(N node) {
    node.setLineNumberAttribute(locationTracker.getLineNumber());
    node.setColumnNumberAttribute(locationTracker.getColumnNumber());
    return node;
  }
  
  @Override
  protected void computeAnnotation(Node node) {
    List<Node> nodeList = node.nodeList();
    if (!nodeList.isEmpty()) {
      for(int i=0; i<nodeList.size(); i++) {
        Node firstNode = nodeList.get(i);
        if (firstNode == null) {
          continue;
        }
        node.setLineNumberAttribute(firstNode.getLineNumberAttribute());
        node.setColumnNumberAttribute(firstNode.getColumnNumberAttribute());
        return;
      }
    }
    
    node.setLineNumberAttribute(locationTracker.getLineNumber());
    node.setColumnNumberAttribute(locationTracker.getColumnNumber());
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
    eval(instr_echo);
    return null;
  }
  
  @Override
  public Instr instr_decl(Declaration declaration, Eoi eoi) {
    Instr instr_decl = super.instr_decl(declaration, eoi);
    if (interpreter != 0) {
      return instr_decl;
    }
    eval(instr_decl);
    return null; 
  }
  
  @Override
  public Instr instr_assign(Assignment assignment, Eoi eoi) {
    Instr instr_assign = super.instr_assign(assignment, eoi);
    if (interpreter != 0) {
      return instr_assign;
    }
    eval(instr_assign);
    return null;
  }
  
  @Override
  public Instr instr_funcall(Funcall funcall, Eoi eoi) {
    Instr instr_funcall = super.instr_funcall(funcall, eoi);
    if (interpreter != 0) {
      return instr_funcall;
    }
    eval(instr_funcall);
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
    eval(instr_if);
    return null;
  }
  @Override
  public Instr instr_xmls(Action action, Xmls xmls) {
    Instr instr_xmls = super.instr_xmls(action, xmls);
    interpreter--;     // match with action()
    if (interpreter != 0) { 
      return instr_xmls;
    }
    eval(instr_xmls);
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
    
    eval(instr_labeled);
    return null;
  }
  
  @Override
  public Instr instr_return(Expr expr_optional_3, Eoi eoi) {
    Instr instr_return = super.instr_return(expr_optional_3, eoi);
    if (interpreter != 0)
      return instr_return;
    
    eval(instr_return);
    return null;
  }
  @Override
  public Instr instr_break(IdToken id_optional_4, Eoi eoi) {
    Instr instr_break = super.instr_break(id_optional_4, eoi);
    if (interpreter != 0)
      return instr_break;
    
    eval(instr_break);
    return null;
  }
  @Override
  public Instr instr_continue(IdToken id_optional_5, Eoi eoi) {
    Instr instr_continue = super.instr_continue(id_optional_5, eoi);
    if (interpreter != 0)
      return instr_continue;
    
    eval(instr_continue);
    return null;
  }
  
  @Override
  public Instr instr_sql(Sql sql, Eoi eoi) {
    Instr instr_sql = super.instr_sql(sql, eoi);
    interpreter--;   // match with action()
    if (interpreter != 0)
      return instr_sql;
    
    eval(instr_sql);
    return null;
  }
  
  @Override
  public DisableLineComment disable_line_comment() {
    enableLineComment = false;
    return null;
  }
  @Override
  public EnableLineComment enable_line_comment() {
    enableLineComment = true;
    return null;
  }
  
  @Override
  public Instr instr_flwor(Flwor flwor, Eoi eoi) {
    Instr instr_flwor = super.instr_flwor(flwor, eoi);
    interpreter--;   // match with action()
    if (interpreter != 0)
      return instr_flwor;
    
    eval(instr_flwor);
    return null;
  }
  
  // --- function declaration & lambda
  
  @Override
  public Parameters parameters(List<Parameter> parameter_star, ReturnType return_type_optional) {
    Parameters parameters = super.parameters(parameter_star, return_type_optional);
    interpreter++;
    return parameters;
  }
  
  @Override
  public Fun fun_return_type(Type type, IdToken id, Parameters parameters, Block block) {
    Fun fun_return_type = super.fun_return_type(type, id, parameters, block);
    interpreter--;  // match with parameters()
    if (interpreter != 0)
      return fun_return_type;
    
    eval(fun_return_type);
    return null;
  }
  @Override
  public Fun fun_no_return_type(IdToken id, Parameters parameters, Block block) {
    Fun fun_no_return_type = super.fun_no_return_type(id, parameters, block);
    interpreter--;  // match with parameters()
    if (interpreter != 0)
      return fun_no_return_type;
    
    eval(fun_no_return_type);
    return null;
  }
  
  @Override
  public Expr expr_fun(Parameters parameters, Block block) {
    Expr expr_fun = super.expr_fun(parameters, block);
    interpreter--;  // match with parameters()
    return expr_fun;
  }
}
