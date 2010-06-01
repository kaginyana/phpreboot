package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.compiler.LoopStack.Labels;
import com.googlecode.phpreboot.model.Type;

class GenEnv {
  private final int shift;
  private final /*@Nullable*/IfParts ifParts;
  private final LoopStack<Labels> loopStack;
  private final Type expectedType;
  
  public GenEnv(int shift, /*@Nullable*/IfParts ifParts, LoopStack<Labels> loopStack, Type expectedType) {
    this.shift = shift;
    this.ifParts = ifParts;
    this.loopStack = loopStack;
    this.expectedType = expectedType;
  }
  
  public int getShift() {
    return shift;
  }
  public /*@Nullable*/IfParts getIfParts() {
    return ifParts;
  }
  public LoopStack<Labels> getLoopStack() {
    return loopStack;
  }
  public Type getExpectedType() {
    return expectedType;
  }

  public GenEnv expectedType(Type expectedType) {
    if (this.expectedType == expectedType)
      return this;
    return new GenEnv(shift, ifParts, loopStack, expectedType);
  }
  
  public GenEnv ifParts(/*@Nullable*/IfParts ifParts) {
    return new GenEnv(shift, ifParts, loopStack, expectedType);
  }
}
