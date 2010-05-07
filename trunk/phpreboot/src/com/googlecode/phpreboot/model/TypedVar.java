package com.googlecode.phpreboot.model;

public class TypedVar extends Var {
  private final Class<?> type;
  
  public TypedVar(String name, boolean readOnly, Class<?> type, Object value) {
    super(name, readOnly, type.cast(value));
    this.type = type;
  }

  @Override
  public void setValue(Object value) {
    type.cast(value);
    super.setValue(value);
  }
}
