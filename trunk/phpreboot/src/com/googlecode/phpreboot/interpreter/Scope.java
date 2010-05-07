package com.googlecode.phpreboot.interpreter;

import java.util.Collection;
import java.util.HashMap;

import com.googlecode.phpreboot.model.Var;

public final class Scope {
  private final HashMap<String,Var> varMap;
  private final Scope parent;
  
  private Scope(HashMap<String,Var> varMap, Scope parent) {
    this.varMap = varMap;
    this.parent = parent;
  }
  
  public Scope(Scope parent) {
    this(new HashMap<String, Var>(), parent);
  }
  
  public Scope getParent() {
    return parent;
  }
  
  public Var lookup(String name) {
    Var symbol = varMap.get(name);
    if (symbol != null || parent == null)
      return symbol;
    return parent.lookup(name);
  }
  
  public void register(Var var) {
    String name = var.getName();
    if (varMap.containsKey(name)) {
      throw new IllegalStateException("scope already contains variable "+name);
    }
    varMap.put(name, var);
  }
  
  public boolean localExists(String name) {
    return varMap.containsKey(name);
  }
  
  public Collection<Var> varMap() {
    return varMap.values();
  }
}
