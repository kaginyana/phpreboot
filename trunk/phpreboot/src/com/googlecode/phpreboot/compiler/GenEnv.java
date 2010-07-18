package com.googlecode.phpreboot.compiler;

import org.objectweb.asm.MethodVisitor;

import com.googlecode.phpreboot.compiler.LoopStack.Labels;
import com.googlecode.phpreboot.model.Type;

class GenEnv {
  private final MethodVisitor mv;
  private final int shift;
  private final Type functionReturnType;   
  private final /*@Nullable*/IfParts ifParts;
  private final LoopStack<Labels> loopStack;
  private final Type expectedType;
  
  GenEnv(MethodVisitor mv, int shift, Type functionReturnType, /*@Nullable*/IfParts ifParts, LoopStack<Labels> loopStack, Type expectedType) {
    this.mv = mv;
    this.shift = shift;
    this.functionReturnType = functionReturnType;
    this.ifParts = ifParts;
    this.loopStack = loopStack;
    this.expectedType = expectedType;
  }
  
  public MethodVisitor getMethodVisitor() {
    return mv;
  }
  public int getShift() {
    return shift;
  }
  public Type getFunctionReturnType() {
    return functionReturnType;
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
    return new GenEnv(mv, shift, functionReturnType, ifParts, loopStack, expectedType);
  }
  
  public GenEnv ifParts(/*@Nullable*/IfParts ifParts) {
    return new GenEnv(mv, shift, functionReturnType, ifParts, loopStack, expectedType);
  }
}
