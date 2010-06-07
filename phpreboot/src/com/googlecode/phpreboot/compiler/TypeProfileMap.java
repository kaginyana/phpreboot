package com.googlecode.phpreboot.compiler;

import java.util.HashMap;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.model.Type;

class TypeProfileMap {
  private final HashMap<Node, Type> map =
    new HashMap<Node, Type>(32);
  private boolean valid = true;
  
  public void register(Node node, Type type) {
    assert !map.containsKey(node);
    map.put(node, type);
  }
  
  public /*@Nullable*/Type get(Node node) {
    return map.get(node);
  }
  
  public boolean isValid() {
    return valid;
  }
  public void validate(boolean valid) {
    this.valid = valid;
  }
  
  @Override
  public String toString() {
    return map.toString();
  }
}
