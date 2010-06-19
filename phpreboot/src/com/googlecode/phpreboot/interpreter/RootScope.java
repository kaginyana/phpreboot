package com.googlecode.phpreboot.interpreter;

import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.module.BitsModule;
import com.googlecode.phpreboot.module.LangModule;
import com.googlecode.phpreboot.module.MathModule;

public class RootScope extends Scope {
  private boolean modulesInitialized;
  
  public RootScope() {
    super(null);
  }
  
  @Override
  public Var lookup(String name) {
    Var var = super.lookup(name);
    if (var != null)
      return var;
    
    if (modulesInitialized)
      return null;
    
    // register modules
    new LangModule().registerModule(this);
    new MathModule().registerModule(this);
    new BitsModule().registerModule(this);
    modulesInitialized = true;
    
    return super.lookup(name);
  }
}
