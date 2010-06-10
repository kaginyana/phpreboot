package com.googlecode.phpreboot.interpreter;

import java.util.List;

import com.googlecode.phpreboot.ast.ASTGrammarEvaluator;
import com.googlecode.phpreboot.ast.AxisNameToken;
import com.googlecode.phpreboot.ast.BoolLiteralToken;
import com.googlecode.phpreboot.ast.DisableLineComment;
import com.googlecode.phpreboot.ast.EnableLineComment;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.LcurlToken;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NodeTypeToken;
import com.googlecode.phpreboot.ast.NullLiteralToken;
import com.googlecode.phpreboot.ast.PathIdToken;
import com.googlecode.phpreboot.ast.PortNumberToken;
import com.googlecode.phpreboot.ast.RcurlToken;
import com.googlecode.phpreboot.ast.RegexAnycharacterToken;
import com.googlecode.phpreboot.ast.RootDirToken;
import com.googlecode.phpreboot.ast.StringLiteralToken;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.XmlTextToken;
import com.googlecode.phpreboot.tools.TerminalEvaluator;

import fr.umlv.tatoo.runtime.buffer.impl.LocationTracker;

public class ASTHandler extends ASTGrammarEvaluator implements TerminalEvaluator<CharSequence>{
  private final LocationTracker locationTracker;
  boolean enableLineComment = true; //comments that starts with '//' clashes with xquery-like syntax
  
  public ASTHandler(LocationTracker locationTracker) {
    this.locationTracker = locationTracker;
  }

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
    } catch (NumberFormatException ignored) {
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
    return computeTokenAnnotation(new LcurlToken());
  }
  @Override
  public RcurlToken rcurl(CharSequence data) {
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
  
  
  
  private <N extends Node> N computeTokenAnnotation(N node) {
    node.setLineNumberAttribute(locationTracker.getLineNumber());
    node.setColumnNumberAttribute(locationTracker.getColumnNumber());
    return node;
  }
  
  @Override
  protected void computeAnnotation(Node node) {
    List<Node> nodeList = node.nodeList();
    if (!nodeList.isEmpty()) {
      int nodeListSize = nodeList.size();
      for(int i=0; i<nodeListSize; i++) {
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
}
