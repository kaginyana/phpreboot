package com.googlecode.phpreboot.model;

public class Parameter {
  private final String name;
  private final Type type;
  private final com.googlecode.phpreboot.ast.Parameter node;

  public Parameter(String name, Type type, com.googlecode.phpreboot.ast.Parameter node) {
    this.name = name;
    this.type = type;
    this.node = node;
  }
  
  public String getName() {
    return name;
  }
  public Type getType() {
    return type;
  }
  public com.googlecode.phpreboot.ast.Parameter getNode() {
    return node;
  }
  
  @Override
  public String toString() {
    return type+" "+name;
  }
}
