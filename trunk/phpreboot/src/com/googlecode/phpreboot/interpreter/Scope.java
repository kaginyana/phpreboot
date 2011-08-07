package com.googlecode.phpreboot.interpreter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.googlecode.phpreboot.compiler.LocalVar;
import com.googlecode.phpreboot.model.Var;

public class Scope {
  private final HashMap<String,Var> varMap;
  private final Scope parent;
  
  private Scope(HashMap<String,Var> varMap, Scope parent) {
    this.varMap = varMap;
    this.parent = parent;
  }
  
  public Scope(Scope parent) {
    this(new HashMap<String, Var>(16), parent);
  }
  
  public Scope getParent() {
    return parent;
  }
  
  @Override
  public String toString() {
    if (parent == null)
      return varMap.toString();
    return varMap.toString() + " -> " + parent;
  }
  
  public Var lookup(String name) {
    //FIXME remove recursive tailcall
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
  
  //FIXME remove this method
  public void replace(Var var, Var newVar) {
    assert var.getName().equals(newVar.getName());
    String name = var.getName();
    
    Var value = varMap.get(name);
    if (var == value) {
      varMap.put(name, newVar);
      return;
    }
    
    if (value != null)
      throw new IllegalStateException();

    if (parent == null)
      throw new IllegalStateException();
    parent.replace(var, newVar);
  }
  
  public boolean localExists(String name) {
    return varMap.containsKey(name);
  }
  
  private static void filterReadOnlyVars(HashMap<String,Var> map, Scope scope) {
    if (scope == null)
      return;
    filterReadOnlyVars(map, scope.getParent());
    for(Var var: scope.varMap.values()) {
      if (var.isReadOnly()) {
        String name = var.getName();
        map.put(name, new Var(name, true, true, var.getType(), var.getValue()));
      }
    }
  }
  
  public static Scope filterReadOnlyVars(Scope scope) {
    HashMap<String,Var> map = new HashMap<>();
    filterReadOnlyVars(map, scope);
    
    Scope newScope = new Scope(null);
    for(Var var: map.values()) {
      newScope.register(var);
    }
    return newScope;
  }
  
  // reconstruct a scope of Scope from a scope of LocalScope
  // association between old LocalVar and new scope var is store in varMap
  // because varMap *must* have a stable order, varMap is declared
  // as a LinkedHashMap and not as a Map
  public Scope reconstructScope(LinkedHashMap<LocalVar,Var> varMap) {
    Class<?> clazz = getClass();
    if (getClass() == Scope.class || clazz == RootScope.class) {
      return this;
    }
    
    HashMap<String,Var> map = new HashMap<>();
    for(Var var: varMap.values()) {
      Var newVar = new Var(var.getName(), var.isReadOnly(), var.isReallyConstant(), var.getType(), null);
      varMap.put((LocalVar)var, newVar);
      map.put(var.getName(), newVar);
    }
    return new Scope(map, parent.reconstructScope(varMap));
  }
}
