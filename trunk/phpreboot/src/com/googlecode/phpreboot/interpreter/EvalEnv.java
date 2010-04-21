package com.googlecode.phpreboot.interpreter;


public class EvalEnv {
  private final Scope scope;
  private final Echoer echoer;
  private final String label;
  
  public EvalEnv(Scope scope, /*maybenull*/Echoer echoer, /*maybenull*/String label) {
    this.scope = scope;
    this.echoer = echoer;
    this.label = label;
  }
  
  public Scope getScope() {
    return scope;
  }
  public /*maybenull*/Echoer getEchoer() {
    return echoer;
  }
  public /*maybenull*/String getLabel() {
    return label;
  }
}
