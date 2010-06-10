package com.googlecode.phpreboot.interpreter;


public class EvalEnv {
  private final Scope scope;
  private final Echoer echoer;
  private final String label;
  
  public EvalEnv(Scope scope, Echoer echoer, /*@Nullable*/String label) {
    this.scope = scope;
    this.echoer = echoer;
    this.label = label;
  }
  
  public Scope getScope() {
    return scope;
  }
  public Echoer getEchoer() {
    return echoer;
  }
  public /*@Nullable*/String getLabel() {
    return label;
  }
  
  //called by the AOT runtime
  public static EvalEnv defaultEvalEnv() {
    return new EvalEnv(new Scope(null), Echoer.defaultWriterEchoer(), null);
  }
}
