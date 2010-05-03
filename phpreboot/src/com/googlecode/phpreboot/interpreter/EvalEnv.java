package com.googlecode.phpreboot.interpreter;


public class EvalEnv {
  private final Scope scope;
  private final Echoer echoer;
  private final Interpreter interpreter;
  private final String label;
  
  public EvalEnv(Scope scope, Interpreter interpreter, Echoer echoer, /*maybenull*/String label) {
    this.scope = scope;
    this.interpreter = interpreter;
    this.echoer = echoer;
    this.label = label;
  }
  
  public Scope getScope() {
    return scope;
  }
  public Interpreter getInterpreter() {
    return interpreter;
  }
  public Echoer getEchoer() {
    return echoer;
  }
  public /*maybenull*/String getLabel() {
    return label;
  }
}
