package com.googlecode.phpreboot.interpreter;


public class EvalEnv {
  private final Scope scope;
  private final Echoer echoer;
  
  public EvalEnv(Scope scope, Echoer echoer) {
    this.scope = scope;
    this.echoer = echoer;
  }
  
  public Scope getScope() {
    return scope;
  }
  public Echoer getEchoer() {
    return echoer;
  }
  
  //called by the AOT runtime
  public static EvalEnv defaultEvalEnv() {
    return new EvalEnv(new Scope(null), Echoer.defaultWriterEchoer());
  }
}
