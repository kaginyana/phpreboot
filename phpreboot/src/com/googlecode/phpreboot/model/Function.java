package com.googlecode.phpreboot.model;

import java.dyn.MethodHandle;
import java.dyn.MethodType;
import java.util.List;

import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.compiler.Compiler;
import com.googlecode.phpreboot.compiler.Type;
import com.googlecode.phpreboot.interpreter.Scope;

public class Function implements Symbol, Type {
  private final String name;
  private final Fun node;
  private final Type returnType;
  private final List<Type> parameterTypes;
  private /*lazy*/MethodHandle methodHandle;
  
  private static org.objectweb.asm.Type FUNCTION_TYPE =
    org.objectweb.asm.Type.getType(MethodHandle.class);
  
  public Function(String name, Type returnType, List<Type> parameterTypes, Fun node) {
    this.name = name;
    this.returnType = returnType;
    this.parameterTypes = parameterTypes;
    this.node = node;
  }

  public Type getReturnType() {
    return returnType;
  }
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }
  public Fun getNode() {
    return node;
  }
  
  public MethodHandle getMethodHandle(Scope scope) {
    if (methodHandle == null) {
      methodHandle = Compiler.compile(this, scope);
    }
    return methodHandle;
  }
  
  @Override
  public String getName() {
    return name;
  }
  @Override
  public Type getType() {
    return this;
  }
  
  @Override
  public org.objectweb.asm.Type asASMType() {
    return FUNCTION_TYPE;
  }
  @Override
  public Class<?> asRuntimeType() {
    return MethodHandle.class;
  }
  public MethodType asMethodType() {
    Class<?>[] parameterArray = new Class<?>[parameterTypes.size()];
    int index = 0;
    for(Type type: parameterTypes) {
      parameterArray[index++] = type.asRuntimeType();
    }
    return MethodType.methodType(returnType.asRuntimeType(), parameterArray);
  }
  @Override
  public Object checkCast(Object value) {
    if (!(value instanceof MethodHandle)) {
      throw new ClassCastException(value + "is not a function");
    }
    return value;
  }
}
