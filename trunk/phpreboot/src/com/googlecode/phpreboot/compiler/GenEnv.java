package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

public class GenEnv {
  private final int shift;
  private final /*@Nullable*/IfParts ifParts;
  private final Type expectedType;
  
  public GenEnv(int shift, /*@Nullable*/IfParts ifParts, Type expectedType) {
    this.shift = shift;
    this.ifParts = ifParts;
    this.expectedType = expectedType;
  }
  
  public int getShift() {
    return shift;
  }
  public /*@Nullable*/IfParts getIfParts() {
    return ifParts;
  }
  public Type getExpectedType() {
    return expectedType;
  }

  public GenEnv expectedType(Type expectedType) {
    if (this.expectedType == expectedType)
      return this;
    return new GenEnv(shift, ifParts, expectedType);
  }
  
  public GenEnv ifParts(/*@Nullable*/IfParts ifParts) {
    return new GenEnv(shift, ifParts, expectedType);
  }
}
