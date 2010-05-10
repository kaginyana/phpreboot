package com.googlecode.phpreboot.compiler;

import java.util.ArrayList;
import java.util.IdentityHashMap;

import com.googlecode.phpreboot.model.Type;

public class BindMap {
  private int slotCount = 0;
  private final IdentityHashMap<Object, LocalVar> map =
    new IdentityHashMap<Object, LocalVar>();
  private final ArrayList<LocalVar> bindReferences =
    new ArrayList<LocalVar>();
  
  public LocalVar bind(Object value, Type type) {
    LocalVar constant = map.get(value);
    if (constant != null)
      return constant;
    
    constant = LocalVar.createConstantBound(value, type, slotCount++);
    map.put(value, constant);
    bindReferences.add(constant);
    return constant;
  }
  
  public int getSlotCount() {
    return slotCount;
  }
  
  public int getReferencesCount() {
    return bindReferences.size();
  }
  
  public Class<?> getReferenceClass(int index) {
    return bindReferences.get(index).getType().getRuntimeClass();
  }
  
  public Object[] getReferences() {
    ArrayList<LocalVar> bindReferences = this.bindReferences;
    int size = bindReferences.size();
    Object[] array = new Object[size];
    for(int i=0; i<size; i++) {
      array[i] = bindReferences.get(i).getValue(); 
    }
    return array;
  }
}
