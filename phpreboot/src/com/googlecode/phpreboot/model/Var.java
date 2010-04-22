package com.googlecode.phpreboot.model;

import com.googlecode.phpreboot.runtime.RT;

public class Var {
  private final String name;
  private final boolean readOnly;
  private Object value;
  
  public Var(String name, boolean readOnly, Object value) {
    this.name = name;
    this.readOnly = readOnly;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }
  public boolean isReadOnly() {
    return readOnly;
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
