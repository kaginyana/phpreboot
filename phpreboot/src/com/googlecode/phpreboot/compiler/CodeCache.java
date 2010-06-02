package com.googlecode.phpreboot.compiler;

import java.util.HashMap;
import java.util.List;

import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Type;

public class CodeCache {
  private final HashMap<Function, HashMap<List<Type>, Function>> cache =
    new HashMap<Function, HashMap<List<Type>,Function>>();
  
  public void register(Function unspecializedFunction, List<Type> parameterTypes, Function specializedFunction) {
    HashMap<List<Type>, Function> functionMap = cache.get(unspecializedFunction);
    if (functionMap == null) {
      functionMap = new HashMap<List<Type>, Function>();
      cache.put(unspecializedFunction, functionMap);
    }
    functionMap.put(parameterTypes, specializedFunction);
  }
  
  public /*@Nullable*/Function lookupSpecializedFunction(Function unspecializedFunction, List<Type> parameterTypes) {
    HashMap<List<Type>, Function> functionMap = cache.get(unspecializedFunction);
    if (functionMap == null)
      return null;
    return functionMap.get(parameterTypes);
  }
}
