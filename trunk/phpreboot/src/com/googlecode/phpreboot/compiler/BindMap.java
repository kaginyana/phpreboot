package com.googlecode.phpreboot.compiler;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;

public class BindMap {
  private int slotCount = 1;    // 0 == EvalEnv
  private final ArrayList<LocalVar> bindReferences =
    new ArrayList<LocalVar>();
  
  public LocalVar bind(String name, boolean isReadOnly, Object value, Type type, boolean allowOptimiticType) {
    boolean optimistic;
    if (allowOptimiticType && type == PrimitiveType.ANY) {
      type = Compiler.inferType(value);
      optimistic = !(isReadOnly || type == PrimitiveType.ANY);
    } else {
      optimistic = false;
    }
    
    int slot = slotCount;
    slotCount = slot + ((type == PrimitiveType.DOUBLE)?2 :1);
    LocalVar constant = LocalVar.createConstantBound(name, isReadOnly, type, optimistic, value, slot);
    bindReferences.add(constant);
    return constant;
  }
  
  public int getSlotCount() {
    return slotCount;
  }
  
  public int getReferencesCount() {
    return bindReferences.size();
  }
  
  public Type getReferenceType(int index) {
    return bindReferences.get(index).getType();
  }
  
  public List<LocalVar> getReferences() {
    return bindReferences;
  }
  
  public void dump() {
    System.err.println("-- bindmap:");
    for(LocalVar localVar: bindReferences) {
      System.err.println(localVar);
    }
  }
}
