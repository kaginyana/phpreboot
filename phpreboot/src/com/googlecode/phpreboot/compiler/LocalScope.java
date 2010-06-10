package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;

class LocalScope extends Scope {
  private int slotCount;
  
  LocalScope(Scope parent) {
    super(parent);
    slotCount = 0;
  }
  
  LocalScope(LocalScope parent) {
    super(parent);
    slotCount = (parent == null)? 0: parent.slotCount;
  }
  
  public int nextSlot(Type type) {
    int slot = this.slotCount;
    this.slotCount= slot + ((type == PrimitiveType.DOUBLE)?2: 1);
    return slot;
  }
}
