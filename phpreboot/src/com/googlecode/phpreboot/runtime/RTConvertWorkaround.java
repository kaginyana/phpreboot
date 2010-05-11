package com.googlecode.phpreboot.runtime;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashMap;

class RTConvertWorkaround {
  @Retention(RetentionPolicy.RUNTIME)
  @interface Bridge {
    // marker annotation
  }
  
  public static MethodHandle convertArguments(MethodHandle mh, MethodType type) {
    if (mh.type().equals(type)) { 
      return mh;
    }
    MethodHandle handle = handleMap.get(type);
    if (handle == null)
      throw new AssertionError("no handle for signature "+type);
    
    mh = MethodHandles.insertArguments(handle, 0, mh);
    
    //System.out.println("workaround mh.type() "+mh.type());
    //System.out.println("workaround type "+type);
    
    return mh;
  }
  
  @Bridge public static Object fun(MethodHandle mh, Object o1, Object o2) throws Throwable {
    return mh.invokeGeneric(o1, o2);
  }
  @Bridge public static Object fun(MethodHandle mh, int o1, Object o2) throws Throwable {
    return mh.invokeGeneric(o1, o2);
  }
  @Bridge public static Object fun(MethodHandle mh, double o1, Object o2) throws Throwable {
    return mh.invokeGeneric(o1, o2);
  }
  @Bridge public static Object fun(MethodHandle mh, Object o1, int o2) throws Throwable {
    return mh.invokeGeneric(o1, o2);
  }
  @Bridge public static Object fun(MethodHandle mh, Object o1, double o2) throws Throwable {
    return mh.invokeGeneric(o1, o2);
  }
  
  @Bridge public static boolean fun2(MethodHandle mh, Object o1, Object o2) throws Throwable {
    return (Boolean)mh.invokeGeneric(o1, o2);
  }
  @Bridge public static boolean fun2(MethodHandle mh, int o1, Object o2) throws Throwable {
    return (Boolean)mh.invokeGeneric(o1, o2);
  }
  @Bridge public static boolean fun2(MethodHandle mh, double o1, Object o2) throws Throwable {
    return (Boolean)mh.invokeGeneric(o1, o2);
  }
  @Bridge public static boolean fun2(MethodHandle mh, Object o1, int o2) throws Throwable {
    return (Boolean)mh.invokeGeneric(o1, o2);
  }
  @Bridge public static boolean fun2(MethodHandle mh, Object o1, double o2) throws Throwable {
    return (Boolean)mh.invokeGeneric(o1, o2);
  }
  
  private static final HashMap<MethodType, MethodHandle> handleMap;
  static {
    Lookup lookup = MethodHandles.lookup();
    HashMap<MethodType, MethodHandle> map =
      new HashMap<MethodType, MethodHandle>();
    for(Method method: RTConvertWorkaround.class.getMethods()) {
      if (!method.isAnnotationPresent(Bridge.class)) {
        continue;
      }
      
      MethodHandle mh = lookup.unreflect(method);
      MethodType type = mh.type().dropParameterTypes(0, 1);
      map.put(type, mh);
    }
    handleMap = map;
  }
}
