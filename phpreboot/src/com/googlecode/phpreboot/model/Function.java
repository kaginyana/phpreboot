package com.googlecode.phpreboot.model;

import java.dyn.MethodHandle;
import java.util.List;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;

public class Function {
  private final String name;
  private final List<Parameter> parameters;
  private final Type returnType;
  private final /*maybenull*/Scope scope;
  private final Block block;
  private /*volatile*/ MethodHandle methodHandle;
  
  public Function(String name, List<Parameter> parameters, Type returnType, /*maybenull*/Scope scope, Block block) {
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.scope = scope;
    this.block = block;
  }

  public String getName() {
    return name;
  }
  public List<Parameter> getParameters() {
    return parameters;
  }
  public Type getReturnType() {
    return returnType;
  }
  public /*maybenull*/Scope getScope() {
    return scope;
  }
  public Block getBlock() {
    return block;
  }
  
  public MethodHandle getMethodHandle() {
    return methodHandle;
  }
  public void setMethodHandle(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }
  
  @Override
  public String toString() {
    return name+parameters;
  }
  
  public Object call(/*EvalEnv*/Object env, Object[] arguments) {
    return Evaluator.INSTANCE.evalFunction(this, arguments, (EvalEnv)env);
  }
}
