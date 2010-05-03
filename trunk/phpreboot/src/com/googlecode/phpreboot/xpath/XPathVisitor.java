package com.googlecode.phpreboot.xpath;

import com.googlecode.phpreboot.ast.AbsoluteLocationPathAbbreviated;
import com.googlecode.phpreboot.ast.AxisSpecifierAbbreviated;
import com.googlecode.phpreboot.ast.AxisSpecifierName;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.NodeTestId;
import com.googlecode.phpreboot.ast.NodeTestIdStar;
import com.googlecode.phpreboot.ast.NodeTestQualified;
import com.googlecode.phpreboot.ast.NodeTestStar;
import com.googlecode.phpreboot.ast.NodeTestType;
import com.googlecode.phpreboot.ast.Predicate;
import com.googlecode.phpreboot.ast.RelativeLocationPathAbbreviated;
import com.googlecode.phpreboot.ast.RelativeLocationPathPathStep;
import com.googlecode.phpreboot.ast.StepDot;
import com.googlecode.phpreboot.ast.StepDoubleDot;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.ast.XpathLiteral;
import com.googlecode.phpreboot.interpreter.Evaluator;

public class XPathVisitor extends Visitor<StringBuilder, XPathEnv, RuntimeException> {
  private XPathVisitor() {
    // enforec singleton
  }
  
  public static final XPathVisitor INSTANCE = new XPathVisitor();
  
  public StringBuilder xpath(Node node, XPathEnv env) {
    return node.accept(this, env);
  }
  
  @Override
  protected StringBuilder visit(Node node, XPathEnv env) {
    StringBuilder builder = env.getBuilder();
    for(Node child: node.nodeList()) {
      xpath(child, env);
    }
    return builder;
  }
  
  @Override
  public StringBuilder visit(AbsoluteLocationPathAbbreviated absolute_location_path_abbreviated, XPathEnv env) {
    env.append("//");
    return xpath(absolute_location_path_abbreviated.getRelativeLocationPath(), env);
  }
  
  @Override
  public StringBuilder visit(RelativeLocationPathPathStep relative_location_path_path_step, XPathEnv env) {
    xpath(relative_location_path_path_step.getRelativeLocationPath(), env);
    env.append("/");
    return xpath(relative_location_path_path_step.getStep(), env);
  }
  @Override
  public StringBuilder visit(RelativeLocationPathAbbreviated relative_location_path_abbreviated, XPathEnv env) {
    xpath(relative_location_path_abbreviated.getRelativeLocationPath(), env);
    env.append("//");
    return xpath(relative_location_path_abbreviated.getStep(), env);
  }
  
  @Override
  public StringBuilder visit(StepDot step_dot, XPathEnv env) {
    return env.append(".");
  }
  @Override
  public StringBuilder visit(StepDoubleDot step_double_dot, XPathEnv env) {
    return env.append("..");
  }
  
  @Override
  public StringBuilder visit(AxisSpecifierName axis_specifier_name, XPathEnv env) {
    return env.append(axis_specifier_name.getAxisName().getValue()).append("::");
  }
  @Override
  public StringBuilder visit(AxisSpecifierAbbreviated axis_specifier_abbreviated, XPathEnv env) {
    return env.append("@");
  }
  
  @Override
  public StringBuilder visit(NodeTestStar node_test_star, XPathEnv env) {
    return env.append("*");
  }
  @Override
  public StringBuilder visit(NodeTestId node_test_id, XPathEnv env) {
    return env.append(node_test_id.getId().getValue());
  }
  @Override
  public StringBuilder visit(NodeTestIdStar node_test_id_star, XPathEnv env) {
    return env.append(node_test_id_star.getId().getValue()).append(":*");
  }
  @Override
  public StringBuilder visit(NodeTestQualified node_test_qualified, XPathEnv env) {
    env.append(node_test_qualified.getId().getValue());
    for(IdToken token: node_test_qualified.getIdPlus()) {
      env.append(".").append(token.getValue());
    }
    return env.getBuilder();
  }
  @Override
  public StringBuilder visit(NodeTestType node_test_type, XPathEnv env) {
    return env.append(node_test_type.getNodeType().getValue()).append("()");
  }
  
  
  @Override
  public StringBuilder visit(Predicate predicate, XPathEnv env) {
    env.append("[");
    return xpath(predicate.getXpathExpr(), env).append(']');
  }
  
  @Override
  public StringBuilder visit(XpathLiteral xpath_literal, XPathEnv env) {
    Object value = Evaluator.INSTANCE.eval(xpath_literal.getSingleLiteral(), env.getEvalEnv());
    return env.append(value.toString());  //FIXME escape !!
  }
}
