package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.interpreter.Scope;


public class TypeCheckEnv {
  private final Scope scope;
  private final Type declaredReturntype;
  
  public TypeCheckEnv(Scope scope, Type declaredReturntype) {
    this.scope = scope;
    this.declaredReturntype = declaredReturntype;
  }
 
  public Scope getScope() {
    return scope;
  }
  public Type getDeclaredReturnType() {
    return declaredReturntype;
  }
}
