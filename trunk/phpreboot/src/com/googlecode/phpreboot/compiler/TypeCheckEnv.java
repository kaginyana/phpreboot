package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

class TypeCheckEnv {
  private final LocalScope scope;
  private final LoopStack<Boolean> loopStack;
  private final Type functionReturnType;
  
  public TypeCheckEnv(LocalScope scope, LoopStack<Boolean> loopStack, Type functionReturnType) {
    this.scope = scope;
    this.loopStack = loopStack;
    this.functionReturnType = functionReturnType;
  }
 
  public LocalScope getScope() {
    return scope;
  }
  public LoopStack<Boolean> getLoopStack() {
    return loopStack;
  }
  public Type getFunctionReturnType() {
    return functionReturnType;
  }
}
