package com.googlecode.phpreboot.interpreter;

import java.util.HashMap;

import com.googlecode.phpreboot.model.Symbol;

public class Scope {
  private final HashMap<String,Symbol> symbolMap =
    new HashMap<String, Symbol>();
  private final Scope parent;
  
  public Scope(Scope parent) {
    this.parent = parent;
  }
  
  public Scope getParent() {
    return parent;
  }
  
  public Symbol lookup(String name) {
    Symbol symbol = symbolMap.get(name);
    if (symbol != null || parent == null)
      return symbol;
    return parent.lookup(name);
  }
  
  public void register(Symbol symbol) {
    String name = symbol.getName();
    if (symbolMap.containsKey(name)) {
      throw new IllegalStateException("scope already contains the symbol "+name);
    }
    symbolMap.put(name, symbol);
  }
  
  public boolean localExists(String name) {
    return symbolMap.containsKey(name);
  }
}
