package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

public class TypeCheckEnv {
  private final LocalScope scope;
  private final Type functionReturnType;
  private final BindMap bindMap;
  
  public TypeCheckEnv(LocalScope scope, Type functionReturnType, BindMap bindMap) {
    this.scope = scope;
    this.functionReturnType = functionReturnType;
    this.bindMap = bindMap;
  }
 
  public LocalScope getScope() {
    return scope;
  }
  public Type getFunctionReturnType() {
    return functionReturnType;
  }
  public BindMap getBindMap() {
    return bindMap;
  }
}
