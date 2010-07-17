package com.googlecode.phpreboot.compiler;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;

public class BindMap {
  private int slotCount = 1;    // 0 == EvalEnv
  private int outputVarCount = 0;
  private final ArrayList<LocalVar> bindReferences =
    new ArrayList<LocalVar>(16);
  
  public LocalVar bind(String name, boolean isReadOnly, Object value, Type type, boolean allowOptimiticType, TypeProfileMap typeProfileMap, Node declaringNode) {
    boolean optimistic;
    if (allowOptimiticType && type == PrimitiveType.ANY) {
      type = Compiler.inferType(value);
      optimistic = !(isReadOnly || type == PrimitiveType.ANY);
      typeProfileMap.registerType(declaringNode, type);
    } else {
      optimistic = false;
      Type typeProfile = typeProfileMap.getType(declaringNode);
      if (typeProfile != null) {
        type = typeProfile;
      }
    }
    
    int slot = slotCount;
    slotCount = slot + ((type == PrimitiveType.DOUBLE)?2 :1);
    LocalVar constant = LocalVar.createConstantBound(name, isReadOnly, type, (optimistic)? declaringNode: null, value, slot);
    bindReferences.add(constant);
    
    if (!isReadOnly)
      outputVarCount++;
      
    return constant;
  }
  
  public int getSlotCount() {
    return slotCount;
  }
  
  public int getOutputVarCount() {
    return outputVarCount;
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
