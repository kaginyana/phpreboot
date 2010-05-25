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
  private final /*@Nullable*/Scope scope;
  private final Block block;
  private final /*@Nullable*/IntrinsicInfo intrinsicInfo;
  private /*volatile*/ MethodHandle methodHandle;
  
  public Function(String name, List<Parameter> parameters, Type returnType, /*@Nullable*/Scope scope, /*@Nullable*/IntrinsicInfo intrinsicInfo, Block block) {
    this.name = name;
    this.parameters = parameters;
    this.returnType = returnType;
    this.scope = scope;
    this.intrinsicInfo = intrinsicInfo;
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
  public /*@Nullable*/Scope getScope() {
    return scope;
  }
  public /*@Nullable*/IntrinsicInfo getIntrinsicInfo() {
    return intrinsicInfo;
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
