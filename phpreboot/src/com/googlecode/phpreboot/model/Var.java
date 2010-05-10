package com.googlecode.phpreboot.model;

import com.googlecode.phpreboot.runtime.RT;

public class Var {
  private final String name;
  private final boolean readOnly;
  private final Type type;
  private Object value;
  
  public Var(String name, boolean readOnly, Type type, Object value) {
    this.name = name;
    this.readOnly = readOnly;
    this.type = type;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }
  public boolean isReadOnly() {
    return readOnly;
  }
  public Type getType() {
    return type;
  }
  public Object getValue() {
    return value;
  }
  public void setValue(Object value) {
    if (isReadOnly())
      throw RT.error("variable %s is read only", name);
    
    this.value = value;
  }
}
