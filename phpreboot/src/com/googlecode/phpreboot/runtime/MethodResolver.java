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
  
  public static MethodHandle findSetter(Class<?> declaringClass, String name, Class<?> actualParameterClass) {
    return findMethodHandle(declaringClass, "set" + capitalize(name), actualParameterClass);
  }
  
  public static MethodHandle findGetter(Class<?> declaringClass, String name) {
    return findMethodHandle(declaringClass, "get" + capitalize(name));
  }
  
  private static MethodHandle findMethodHandle(Class<?> declaringClass, String name, Class<?>... actualParameterClasses) {
    Method method = findMethod(declaringClass, name, actualParameterClasses);
    if (method == null)
      return null;
    return PUBLIC_LOOKUP.unreflect(method);
  }
  
  private static Method findMethod(Class<?> declaringClass, String name, Class<?>[] actualParameterClasses) {
    ArrayList<Method> list = gatherMethods(declaringClass, name, actualParameterClasses);
    
    switch(list.size()) {
    case 0:
      return null;
    case 1:
      return list.get(0);
    }
    return findMostSpecific(list);
  }
  
  private static Method findMostSpecific(ArrayList<Method> list) {
    //TODO
    return list.get(0);
  }

  private static ArrayList<Method> gatherMethods(Class<?> declaringClass, String name, Class<?>[] actualParameterClasses) {
    ArrayList<Method> list = new ArrayList<Method>();
    loop: for(Method method: declaringClass.getMethods()) {
      String methodName = method.getName();
      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length != actualParameterClasses.length) {
        continue loop; 
      }
      if (!name.equals(methodName)) {
        continue loop;
      }
      
      /*
      for(int i=0; i<parameterTypes.length; i++) {
        if (!isCompatible(parameterTypes[i], actualParameterClasses[i])) {
          continue loop;
        }
      }*/
      
      list.add(method);
    }
    return list;
  }

  /*
  private static boolean isCompatible(Class<?> parameterType, Class<?> actualClass) {
    if (parameterType == actualClass)
      return true;
    //FIXME doesn't work with primitive types
    return parameterType.isAssignableFrom(actualClass);
  }*/
}
