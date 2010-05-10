package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;

public class LocalScope extends Scope {
  private int slotCount;
  
  public LocalScope(Scope parent) {
    super(parent);
    slotCount = 1;
  }
  
  public LocalScope(LocalScope parent) {
    super(parent);
    slotCount = parent.slotCount;
  }
  
  public int nextSlot(Type type) {
    int slotCount = this.slotCount;
    this.slotCount= slotCount + ((type == PrimitiveType.DOUBLE)?2: 1);
    return slotCount;
  }
}
