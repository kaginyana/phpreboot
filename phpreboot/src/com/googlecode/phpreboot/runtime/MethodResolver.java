package com.googlecode.phpreboot.runtime;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.ArrayList;

class MethodResolver {
  private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

  private static String capitalize(String s) {
    assert !s.isEmpty();
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
  
  public static MethodHandle findSetter(Class<?> declaringClass, String name) {
    return findMethodHandle(declaringClass, "set" + capitalize(name), 1);
  }
  
  public static MethodHandle findGetter(Class<?> declaringClass, String name) {
    return findMethodHandle(declaringClass, "get" + capitalize(name), 0);
  }
  
  public static MethodHandle findMethodHandle(Class<?> declaringClass, String name, int parameterCount) {
    Method method = findMethod(declaringClass, name, parameterCount);
    if (method == null)
      return null;
    
    //FIXME, should try to find if the method doesn't override a public method of an
    // accessible class
    try {
      method.setAccessible(true);
    } catch(SecurityException e) {
      return null;
    }
    
    return PUBLIC_LOOKUP.unreflect(method);
  }
  
  private static Method findMethod(Class<?> declaringClass, String name, int parameterCount) {
    ArrayList<Method> list = gatherMethods(declaringClass, name, parameterCount);
    if (list.size() == 1) {
      return list.get(0);
    }
    return null; // zaro method or too much methods
  }

  private static ArrayList<Method> gatherMethods(Class<?> declaringClass, String name, int parameterCount) {
    ArrayList<Method> list = new ArrayList<Method>();
    loop: for(Method method: declaringClass.getMethods()) {
      String methodName = method.getName();
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length != parameterCount) {
        continue loop; 
      }
      if (!name.equals(methodName)) {
        continue loop;
      }
      
      list.add(method);
    }
    return list;
  }
}
