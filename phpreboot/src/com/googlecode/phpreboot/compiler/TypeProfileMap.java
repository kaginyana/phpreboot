package com.googlecode.phpreboot.compiler;

import java.util.HashMap;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.model.Type;

class TypeProfileMap {
  private final HashMap<Node, Type> typeMap =
    new HashMap<Node, Type>(32);
  private boolean valid = true;
  
  public void registerType(Node node, Type type) {
    assert !typeMap.containsKey(node);
    typeMap.put(node, type);
  }
  
  public /*@Nullable*/Type getType(Node node) {
    return typeMap.get(node);
  }
  
  public boolean isValid() {
    return valid;
  }
  public void validate(boolean valid) {
    this.valid = valid;
  }
  
  @Override
  public String toString() {
    return typeMap.toString();
  }
}
