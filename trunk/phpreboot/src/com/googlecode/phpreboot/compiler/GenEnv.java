package com.googlecode.phpreboot.compiler;


public class GenEnv {
  private final Type expectedType;
  
  
  public GenEnv(Type expectedType) {
    this.expectedType = expectedType;
  }
  
  public Type getExpectedType() {
    return expectedType;
  }

  public GenEnv expectedType(Type expectedType) {
    if (this.expectedType == expectedType)
      return this;
    return new GenEnv(expectedType);
  }
}
