package com.googlecode.phpreboot.xpath;

import org.jaxen.expr.XPathFactory;

import com.googlecode.phpreboot.interpreter.EvalEnv;

public class XPathExprEnv {
  private final XPathFactory factory;
  private final int inheritedAxis;
  private final EvalEnv evalEnv;
  
  public XPathExprEnv(XPathFactory factory, int inheritedAxis, EvalEnv evalEnv) {
    this.factory = factory;
    this.inheritedAxis = inheritedAxis;
    this.evalEnv = evalEnv;
  }

  public XPathFactory getFactory() {
    return factory;
  }
  public int getInheritedAxis() {
    return inheritedAxis;
  }
  public EvalEnv getEvalEnv() {
    return evalEnv;
  }
}
