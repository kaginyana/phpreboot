package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

/** This object *must* be immutable and duplicated when necessary.
 */
class TypeCheckEnv {
  private final LocalScope scope;
  private final LoopStack<Boolean> loopStack;
  private final boolean untakenBranch;
  private final Type functionReturnType;
  
  TypeCheckEnv(LocalScope scope, LoopStack<Boolean> loopStack, boolean untakenBranch, Type functionReturnType) {
    this.scope = scope;
    this.loopStack = loopStack;
    this.untakenBranch = untakenBranch;
    this.functionReturnType = functionReturnType;
  }
 
  public LocalScope getScope() {
    return scope;
  }
  public LoopStack<Boolean> getLoopStack() {
    return loopStack;
  }
  public boolean isUntakenBranch() {
    return untakenBranch;
  }
  public Type getFunctionReturnType() {
    return functionReturnType;
  }
}
