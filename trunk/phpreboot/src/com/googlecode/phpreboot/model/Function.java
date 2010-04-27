package com.googlecode.phpreboot.model;

import java.dyn.MethodHandle;
import java.dyn.MethodType;
import java.util.List;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;

public class Function {
  private final String name;
  private final List<Parameter> parameters;
  private final Scope scope;
  private final Block block;
  private MethodHandle methodHandle;
  
  public Function(String name, List<Parameter> parameters, Scope scope, Block block) {
    this.name = name;
    this.parameters = parameters;
    this.scope = scope;
    this.block = block;
  }

  public String getName() {
    return name;
  }
  public List<Parameter> getParameters() {
    return parameters;
  }
  public Scope getScope() {
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
  
  public Object call(/*EvalEnv*/Object env, Object[] arguments) {
    return Evaluator.INSTANCE.evalFunction(this, arguments, (EvalEnv)env);
  }
  
  public MethodType asMethodType() {
    Class<?>[] parameterArray = new Class<?>[parameters.size() + 1];
    parameterArray[0] = /*EvalEnv.class*/Object.class;
    for(int i = 1; i < parameterArray.length; i++) {
      parameterArray[i] = Object.class;
    }
    return MethodType.methodType(Object.class, parameterArray);
  }
}
