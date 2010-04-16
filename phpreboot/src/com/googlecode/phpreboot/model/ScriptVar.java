package com.googlecode.phpreboot.model;

import com.googlecode.phpreboot.compiler.Type;

public class ScriptVar implements Symbol {
  private final String name;
  private final Type type;
  private Object value;
  
  public ScriptVar(String name, Type type, Object value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }
  
  @Override
  public String getName() {
    return name;
  }
  @Override
  public Type getType() {
    return type;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    this.value = type.checkCast(value);
  }
}
