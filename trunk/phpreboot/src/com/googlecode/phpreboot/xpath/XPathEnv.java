package com.googlecode.phpreboot.xpath;

import com.googlecode.phpreboot.interpreter.EvalEnv;


public class XPathEnv {
  private final StringBuilder builder =
    new StringBuilder();
  private final EvalEnv evalEnv;
  
  public XPathEnv(EvalEnv evalEnv) {
    this.evalEnv = evalEnv;
  }
  
  public EvalEnv getEvalEnv() {
    return evalEnv;
  }

  public StringBuilder append(String s) {
    return builder.append(s);
  }
  
  public StringBuilder getBuilder() {
    return builder;
  }
}
