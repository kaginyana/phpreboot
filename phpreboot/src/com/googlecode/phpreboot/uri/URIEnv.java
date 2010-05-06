package com.googlecode.phpreboot.uri;

import com.googlecode.phpreboot.interpreter.EvalEnv;

public class URIEnv {
  private final EvalEnv evalEnv;
  private final PathBuilder pathBuilder;
  
  public URIEnv(EvalEnv evalEnv, PathBuilder pathBuilder) {
    this.evalEnv = evalEnv;
    this.pathBuilder = pathBuilder;
  }
  
  public EvalEnv getEvalEnv() {
    return evalEnv;
  }
  
  public PathBuilder getPathBuilder() {
    return pathBuilder;
  }
}
