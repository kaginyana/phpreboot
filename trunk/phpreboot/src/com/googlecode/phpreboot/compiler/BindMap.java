package com.googlecode.phpreboot.compiler;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.model.Type;

public class BindMap {
  private int slotCount = 1;    // 0 == EvalEnv
  private final ArrayList<LocalVar> bindReferences =
    new ArrayList<LocalVar>();
  
  public LocalVar bind(String name, boolean isReadOnly, Object value, Type type) {
    //FIXME use value type if type is any
    
    LocalVar constant = LocalVar.createConstantBound(name, isReadOnly, value, type, slotCount++);
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
  
  public List<LocalVar> getReferences() {
    return bindReferences;
  }
  
  public Object[] getReferenceValues() {
    ArrayList<LocalVar> bindReferences = this.bindReferences;
    int size = bindReferences.size();
    Object[] array = new Object[size];
    for(int i=0; i<size; i++) {
      array[i] = bindReferences.get(i).getValue(); 
    }
    return array;
  }
  
  public Object[] getReferenceValues(Object env) {
    ArrayList<LocalVar> bindReferences = this.bindReferences;
    int size = bindReferences.size();
    Object[] array = new Object[1 + size];
    array[0] = env;
    for(int i=0; i<size; i++) {
      array[i + 1] = bindReferences.get(i).getValue(); 
    }
    return array;
  }
}
