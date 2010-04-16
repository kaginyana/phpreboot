package com.googlecode.phpreboot.model;

import com.googlecode.phpreboot.compiler.Type;

public class LocalVar implements Symbol {
  private final String name;
  private final Type type;
  private int slot = -1;
  
  public LocalVar(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Type getType() {
    return type;
  }
  
  public int getSlot() {
    return slot;
  }
  void setSlot(int slot) {
    this.slot = slot;
  }
}
