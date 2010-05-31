package com.googlecode.phpreboot.model;

import com.googlecode.phpreboot.runtime.RT;

public class Var {
  private final String name;
  private final boolean readOnly;
  private Type type;  // are readonly for Var but read-write for LocalVar
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
  
  protected void setType(Type type) {
    this.type = type;
  }
  
  @Override
  public String toString() {
    return "var name:"+name+" readOnly:"+readOnly+" type:"+type+" value:"+value;
  }
  
  // called directly by the runtime, signature must not changed
  public void setValue(Object value) {
    if (isReadOnly())
      throw RT.error("variable %s is read only", name);
    
    this.value = value;
  }
}
