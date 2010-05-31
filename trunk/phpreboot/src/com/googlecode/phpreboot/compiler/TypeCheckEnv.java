package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

public class TypeCheckEnv {
  private final LocalScope scope;
  private final LoopStack<Boolean> loopStack;
  private final Type functionReturnType;
  private final BindMap bindMap;
  private final boolean allowOptimisticType;
  private final TypeProfileMap typeProfileMap;
  
  public TypeCheckEnv(LocalScope scope, LoopStack<Boolean> loopStack, Type functionReturnType, BindMap bindMap, boolean allowOptimisticType, TypeProfileMap typeProfileMap) {
    this.scope = scope;
    this.loopStack = loopStack;
    this.functionReturnType = functionReturnType;
    this.bindMap = bindMap;
    this.allowOptimisticType = allowOptimisticType;
    this.typeProfileMap = typeProfileMap;
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
  public BindMap getBindMap() {
    return bindMap;
  }
  public boolean allowOptimisticType() {
    return allowOptimisticType;
  }
  public TypeProfileMap getTypeProfileMap() {
    return typeProfileMap;
  }
}
