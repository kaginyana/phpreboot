package com.googlecode.phpreboot.runtime;

import java.dyn.CallSite;
import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;
import java.util.Arrays;

import com.googlecode.phpreboot.ast.Funcall;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.runtime.Array.Entry;



public class RT {
  public static RuntimeException error(String format, Object arg) {
    return error(format, new Object[]{arg});
  }
  public static RuntimeException error(String format, Object arg, Object arg2) {
    return error(format, new Object[]{arg, arg2});
  }
  public static RuntimeException error(String format, Object... args) {
    return new RuntimeException(String.format(format, args));
  }
  
  public static RuntimeException error(Throwable t) {
    if (t.getCause() != null) {
      t = t.getCause();
    }
    if (t instanceof RuntimeException)
      return (RuntimeException)t;
    return new RuntimeException(t);
  }
  
  public static Object unary_plus(Object value) {
    if (value instanceof Integer) {
      return unary_plus((int)(Integer)value);
    }
    if (value instanceof Double) {
      return unary_plus((double)(Double)value);
    }
    throw error("invalid value for operation + (unary plus) %s", value);
  }
  public static int unary_plus(int value) {
    return value;
  }
  public static double unary_plus(double value) {
    return value;
  }
  
  public static Object unary_minus(Object value) {
    if (value instanceof Integer) {
      return unary_minus((int)(Integer)value);
    }
    if (value instanceof Double) {
      return unary_minus((double)(Double)value);
    }
    throw error("invalid value for operation - (unary minus) %s",value);
  }
  public static int unary_minus(int value) {
    return -value;
  }
  public static double unary_minus(double value) {
    return -value;
  }
  
  public static boolean unary_not(Object value) {
    if (value instanceof Boolean) {
      return unary_not((boolean)(Boolean)value);
    }
    throw error("invalid value for operation ! (unary not) %s",value);
  }
  public static boolean unary_not(boolean value) {
    return !value;
  }

  public static Object plus(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return plus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return plus(l, (double)(Double)right);
      }
      if (right instanceof String) {
        return l + (String)right;
      }
      throw error("invalid value for operation + "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return plus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return plus(l, (double)(Double)right);
      }
      if (right instanceof String) {
        return l + (String)right;
      }
      throw error("invalid value for operation + "+right);
    }
    if (left instanceof String) {
      return ((String)left) + right;
    }
    if (right instanceof String) {
      return left + (String)right;
    }
    
    throw error("invalid value for operation + "+left+" "+right);
  }
  public static int plus(int left, int right) {
    return left + right;
  }
  public static double plus(double left, double right) {
    return left + right;
  }
  
  public static Object minus(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return minus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return minus(l, (double)(Double)right);
      }
      throw error("invalid value for operation - "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return minus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return minus(l, (double)(Double)right);
      }
      throw error("invalid value for operation - "+right);
    }
    throw error("invalid value for operation - "+left);
  }
  public static int minus(int left, int right) {
    return left - right;
  }
  public static double minus(double left, double right) {
    return left - right;
  }
  
  public static Object mult(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return mult(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mult(l, (double)(Double)right);
      }
      throw error("invalid value for operation * "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return mult(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mult(l, (double)(Double)right);
      }
      throw error("invalid value for operation * "+right);
    }
    throw error("invalid value for operation * "+left);
  }
  public static int mult(int left, int right) {
    return left * right;
  }
  public static double mult(double left, double right) {
    return left * right;
  }
  
  public static Object div(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return div(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return div(l, (double)(Double)right);
      }
      throw error("invalid value for operation / "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return div(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return div(l, (double)(Double)right);
      }
      throw error("invalid value for operation / "+right);
    }
    throw error("invalid value for operation / "+left);
  }
  public static int div(int left, int right) {
    return left / right;
  }
  public static double div(double left, double right) {
    return left / right;
  }
  
  public static boolean eq(Object left, Object right) {
    return (left == null)? right == null: left.equals(right);
  }
  
  public static boolean ne(Object left, Object right) {
    return (left == null)? right != null: !left.equals(right);
  }
  
  public static boolean lt(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return lt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return lt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return lt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return lt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean lt(int left, int right) {
    return left < right;
  }
  public static boolean lt(double left, double right) {
    return left < right;
  }
  
  public static boolean le(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return le(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return le(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return le(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return le(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean le(int left, int right) {
    return left <= right;
  }
  public static boolean le(double left, double right) {
    return left <= right;
  }
  
  public static boolean gt(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return gt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return gt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return gt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return gt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean gt(int left, int right) {
    return left > right;
  }
  public static boolean gt(double left, double right) {
    return left > right;
  }
  
  public static boolean ge(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return ge(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return ge(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return ge(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return ge(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean ge(int left, int right) {
    return left >= right;
  }
  public static boolean ge(double left, double right) {
    return left >= right;
  }
  
  public static Sequence foreach_expression(Object value) {
    if (value instanceof Array) {
      return ((Array)value).sequence();
    }
    if (value instanceof Sequence) {
      return (Sequence)value; 
    }
    throw RT.error("foreach needs a sequence or an array: %s", value);
  }
  
  
  
  static StringBuilder append(StringBuilder builder, Object o) {
    if (o instanceof String) {
      return builder.append('\"').append(o).append('\"');
    }
    return builder.append(o);
  }
  
  
  // --- member access
  
  private final static MethodHandle array_set;
  private final static MethodHandle array_access_get;
  private final static MethodHandle test_receiver_asArray;
  private final static MethodHandle test_receiver_and_key;
  private final static MethodHandle slowPathArraySet;
  private final static MethodHandle slowPathArrayGet;
  
  static {
    Lookup lookup = MethodHandles.publicLookup();
    array_set = MethodHandles.convertArguments(
        lookup.findVirtual(Array.class, "set",
          MethodType.methodType(void.class, Object.class, Object.class)),
        MethodType.methodType(void.class, Object.class, Object.class, Object.class));
    array_access_get = MethodHandles.convertArguments(
        lookup.findVirtual(ArrayAccess.class, "get",
          MethodType.methodType(Object.class, Object.class)),
        MethodType.methodType(Object.class, Object.class, Object.class));
    
    test_receiver_asArray = lookup.findStatic(RT.class, "test_receiver_asArray",
        MethodType.methodType(boolean.class, Object.class));
    test_receiver_and_key = lookup.findStatic(RT.class, "test_receiver_and_key",
        MethodType.methodType(boolean.class, Class.class, Object.class, Object.class));
    
    
    slowPathArraySet = lookup.findStatic(RT.class, "slowPathArraySet",
        MethodType.methodType(void.class, CallSite.class, boolean.class, Object.class, Object.class, Object.class));
    slowPathArrayGet = lookup.findStatic(RT.class, "slowPathArrayGet",
        MethodType.methodType(Object.class, CallSite.class, boolean.class, Object.class, Object.class));
  }
  
  public static boolean test_receiver_asArray(Object refValue) {
    return refValue instanceof Array;
  }
  
  public static boolean test_receiver_and_key(Class<?> receiverClass, Object refValue, Object key) {
    return key instanceof String && receiverClass.isInstance(refValue);
  }
  
  public static void slowPathArraySet(CallSite callsite, boolean keyMustExist, Object refValue, Object key, Object value) {
    Class<?> refClass = refValue.getClass(); // also nullcheck
    
    MethodHandle mh;
    MethodHandle test;
    String name;
    if (key instanceof String && (!(name = (String)key).isEmpty())) {
      mh = MethodResolver.findSetter(refClass, name);
      if (mh != null) {
        mh = MethodHandles.convertArguments(mh,  
            MethodType.methodType(void.class, Object.class, Object.class));
        try {
          //FIXME should be invokeExact
          mh.invokeGeneric(refValue, value);
        } catch (Throwable e) {
          throw RT.error(e);
        }
        mh = MethodHandles.dropArguments(mh, 1, Object.class);
        test = MethodHandles.insertArguments(test_receiver_and_key, 0, refClass);
        mh = MethodHandles.guardWithTest(test, mh, callsite.getTarget());
        callsite.setTarget(mh);
        return;
      }
    }
      
    if (refValue instanceof Array) {
      Array array = (Array)refValue;
      Entry entry = array.getEntry(key);
      
      if (keyMustExist && entry.value == null) {
        array.remove(key);
        throw RT.error("member %s doesn't exist for array: %s", key, array);
      }
      entry.value = value; 

      mh = array_set;
      test = test_receiver_asArray;

      mh = MethodHandles.guardWithTest(test, mh, callsite.getTarget());
      callsite.setTarget(mh);
      return;
    } 
    throw RT.error("member %s doesn't exist for object: %s", key, refValue);  
  }
  
  public static void interpreterArraySet(Node node, Object refValue, Object key, Object value, boolean keyMustExist) {
    CallSite callSite = node.getCallsiteAttribute();
    MethodHandle target;
    if (callSite == null) {
      
      // cache for next call
      
      MethodType methodType = MethodType.methodType(void.class, Object.class, Object.class, Object.class);
      callSite = new CallSite(RT.class, "", methodType);
      target = MethodHandles.insertArguments(slowPathArraySet, 0, callSite, keyMustExist);
      callSite.setTarget(target);
      
      slowPathArraySet(callSite, keyMustExist, refValue, key, value);
      
      node.setCallsiteAttribute(callSite);
      
    } else {
      target = callSite.getTarget();
      
      try {
        //FIXME should be invokeExact
        target.invokeGeneric(refValue, key, value);
      } catch (Throwable e) {
        throw RT.error(e);
      } 
    }
  }
  
  
  public static Object slowPathArrayGet(CallSite callsite, boolean keyMustExist, Object refValue, Object key) {
    Class<?> refClass = refValue.getClass(); // also nullcheck
    
    MethodHandle mh;
    MethodHandle test;
    String name;
    if (key instanceof String && (!(name = (String)key).isEmpty())) {
      mh = MethodResolver.findGetter(refClass, name);
      if (mh != null) {
        mh = MethodHandles.convertArguments(mh,  
            MethodType.methodType(Object.class, Object.class));
        Object result;
        try {
          //FIXME should be invokeExact
          result = mh.invokeGeneric(refValue);
        } catch (Throwable e) {
          throw RT.error(e);
        }
        mh = MethodHandles.dropArguments(mh, 1, Object.class);
        test = MethodHandles.insertArguments(test_receiver_and_key, 0, refClass);
        mh = MethodHandles.guardWithTest(test, mh, callsite.getTarget());
        callsite.setTarget(mh);
        return result;
      }
    }
      
    if (refValue instanceof ArrayAccess) {
      ArrayAccess arrayAccess = (ArrayAccess)refValue;
      Object result = arrayAccess.get(key);
      if (keyMustExist && result == null) {
        throw RT.error("member %s doesn't exist for array: %s", key, arrayAccess);
      }
      
      mh = array_access_get;
      test = test_receiver_asArray;

      mh = MethodHandles.guardWithTest(test, mh, callsite.getTarget());
      callsite.setTarget(mh);
      return result;
    } 
    throw RT.error("member %s doesn't exist for object: %s", key, refValue);  
  }
  
  public static Object interpreterArrayGet(Node node, Object refValue, Object key, boolean keyMustExist) {
    CallSite callSite = node.getCallsiteAttribute();
    MethodHandle target;
    if (callSite == null) {
      
      // cache for next call
      
      MethodType methodType = MethodType.methodType(Object.class, Object.class, Object.class);
      callSite = new CallSite(RT.class, "", methodType);
      target = MethodHandles.insertArguments(slowPathArrayGet, 0, callSite, keyMustExist);
      callSite.setTarget(target);
      
      Object result = slowPathArrayGet(callSite, keyMustExist, refValue, key);
      node.setCallsiteAttribute(callSite);
      return result;
      
    } else {
      target = callSite.getTarget();
      
      try {
        //FIXME should be invokeExact
        return target.invokeGeneric(refValue, key);
      } catch (Throwable e) {
        throw RT.error(e);
      } 
    }
  }
  
  // --- function call
  
  private static final MethodHandle test_receiver_class;
  private static final MethodHandle slowPathMethodCall;
  static {
    Lookup lookup = MethodHandles.publicLookup();
    test_receiver_class = lookup.findVirtual(Class.class, "isInstance",
        MethodType.methodType(boolean.class, Object.class));
    slowPathMethodCall = lookup.findStatic(RT.class, "slowPathMethodCall",
        MethodType.methodType(Object.class, CallSite.class, String.class, Object[].class));
  }
  
  public static Object interpreterMethodCall(Funcall funcall, String name, Object[] values) {
    CallSite callSite = funcall.getCallsiteAttribute();
    if (callSite == null) {
      MethodType type = MethodType.genericMethodType(values.length);
      callSite = new CallSite(RT.class, "", type);
      funcall.setCallsiteAttribute(callSite);
      MethodHandle mh = MethodHandles.insertArguments(slowPathMethodCall, 0, callSite, name);
      mh = MethodHandles.collectArguments(mh, type);
      callSite.setTarget(mh);
      
      return slowPathMethodCall(callSite, name, values);
    }
    
    MethodHandle target = callSite.getTarget();
    try {
      return target.invokeVarargs(values);
    } catch (Throwable e) {
      throw RT.error(e);
    } 
  }
  
  public static Object slowPathMethodCall(CallSite callSite, String name, Object[] values) {
    Class<?> receiverClass = values[0].getClass(); // nullcheck
    MethodHandle target = MethodResolver.findMethodHandle(receiverClass, name, values.length - 1);
    if (target == null) {
      throw RT.error("no function %s with values %s", name, Arrays.toString(values));
    }
    
    target = MethodHandles.convertArguments(target, callSite.type());
    MethodHandle test = MethodHandles.insertArguments(test_receiver_class, 0, receiverClass);
    MethodHandle guard = MethodHandles.guardWithTest(test, target, callSite.getTarget());
    callSite.setTarget(guard);
    
    try {
      return target.invokeVarargs(values);
    } catch (Throwable e) {
      throw RT.error(e);
    } 
  }
}
