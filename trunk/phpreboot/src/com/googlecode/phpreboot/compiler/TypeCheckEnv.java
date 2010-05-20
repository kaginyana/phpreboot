package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

public class TypeCheckEnv {
  private final LocalScope scope;
  private final LoopStack loopStack;
  private final Type functionReturnType;
  private final BindMap bindMap;
  
  public TypeCheckEnv(LocalScope scope, LoopStack loopStack, Type functionReturnType, BindMap bindMap) {
    this.scope = scope;
    this.loopStack = loopStack;
    this.functionReturnType = functionReturnType;
    this.bindMap = bindMap;
  }
 
  public LocalScope getScope() {
    return scope;
  }
  public LoopStack getLoopStack() {
    return loopStack;
  }
  public Type getFunctionReturnType() {
    return functionReturnType;
  }
  public BindMap getBindMap() {
    return bindMap;
  }
}
