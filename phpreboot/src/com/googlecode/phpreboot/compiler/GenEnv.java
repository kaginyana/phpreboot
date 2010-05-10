package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;


public class GenEnv {
  private final int shift;
  private final Type expectedType;
  
  public GenEnv(int shift, Type expectedType) {
    this.shift = shift;
    this.expectedType = expectedType;
  }
  
  public int getShift() {
    return shift;
  }
  public Type getExpectedType() {
    return expectedType;
  }

  public GenEnv expectedType(Type expectedType) {
    if (this.expectedType == expectedType)
      return this;
    return new GenEnv(shift, expectedType);
  }
}
