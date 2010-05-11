package com.googlecode.phpreboot.runtime;

import java.dyn.CallSite;
import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.runtime.Array.Entry;
import com.sun.grizzly.util.ConcurrentReferenceHashMap.Option;

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
  
  
  /*public static void includeDefaultFunctions(Interpreter interpreter) {
    Reader reader = new InputStreamReader(RT.class.getResourceAsStream("functions.phpr"));
    Analyzer.analyze(reader, writer, rootScope);
  }*/
  
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
  
  public static Object mod(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return mod(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mod(l, (double)(Double)right);
      }
      throw error("invalid value for operation % "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return mod(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mod(l, (double)(Double)right);
      }
      throw error("invalid value for operation % "+right);
    }
    throw error("invalid value for operation % "+left);
  }
  public static int mod(int left, int right) {
    return left % right;
  }
  public static double mod(double left, double right) {
    return left % right;
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
  
  
  // --- conversions
  
  public static Object toBoolean(Object o) {
    /*
    if (o instanceof Boolean) {
      return o;
    }
    if (o instanceof Byte) {
      return (Byte)o == 0;
    }
    if (o instanceof Character) {
      return (Character)o == 0;
    }
    if (o instanceof Short) {
      return (Short)o == 0;
    }
    if (o instanceof Integer) {
      return (Integer)o == 0;
    }
    if (o instanceof Long) {
      return (Long)o == 0L;
    }
    if (o instanceof Float) {
      return (Float)o == 0F;
    }
    if (o instanceof Double) {
      return (Double)o == 0.0;
    }*/
    if (o instanceof Boolean) {
      return o;
    }
    if (o instanceof String) {
      return Boolean.parseBoolean((String)o);
    }
    throw RT.error("value not convertible to boolean: %s", o);
  }
  
  public static Object toInt(Object o) {
    if (o instanceof Byte) {
      return (int)(Byte)o;
    }
    if (o instanceof Character) {
      return (int)(Character)o;
    }
    if (o instanceof Short) {
      return (int)(Short)o;
    }
    if (o instanceof Integer) {
      return o;
    }
    if (o instanceof Long) {
      return (int)(long)(Long)o;
    }
    if (o instanceof Float) {
      return (int)(float)(Float)o;
    }
    if (o instanceof Double) {
      return (int)(double)(Double)o;
    }
    if (o instanceof String) {
      return Integer.parseInt((String)o);
    }
    throw RT.error("value not convertible to int: %s", o);
  }
  
  public static Object toDouble(Object o) {
    if (o instanceof Byte) {
      return (double)(Byte)o;
    }
    if (o instanceof Character) {
      return (double)(Character)o;
    }
    if (o instanceof Short) {
      return (double)(Short)o;
    }
    if (o instanceof Integer) {
      return (double)(Integer)o;
    }
    if (o instanceof Long) {
      return (double)(Long)o;
    }
    if (o instanceof Float) {
      return (double)(Float)o;
    }
    if (o instanceof Double) {
      return o;
    }
    if (o instanceof String) {
      return Double.parseDouble((String)o);
    }
    throw RT.error("value not convertible to double: %s", o);
  }
  
  public static Object toString(Object o) {
    return String.valueOf(o);
  }
  
  public static Object toArray(Object o) {
    Class<?> clazz = o.getClass();   // nullcheck
    
    //TODO String to Array (JSON parser)
    
    // sequence to array
    if (o instanceof Sequence) {
      return sequenceToArray((Sequence)o);
    }
    
    // entry to array
    if (o instanceof Array.Entry) {
      return entryToArray((Array.Entry)o);
    }
    
    //Java conversions
    if (o instanceof Object[]) {
      return objectArrayToArray((Object[])o);
    }
    if (clazz.isArray()) {
      return primitiveArrayToArray(o);
    }
    if (o instanceof Iterable<?>) {
      return iterableToArray((Iterable<?>)o);
    }
    if (o instanceof Map<?,?>) {
      return mapToArray((Map<?,?>)o);
    }
    if (o instanceof Iterator<?>) {
      return iteratorToArray((Iterator<?>)o);
    }
    
    throw RT.error("value not convertible to array: %s", o);
  }
  private static Array sequenceToArray(Sequence sequence) {
    Array array = new Array();
    for(;sequence!=null; sequence = sequence.next()) {
      array.set(sequence.getKey(), sequence.getValue());
    }
    return array;
  }
  private static Array entryToArray(Array.Entry entry) {
    Array array = new Array();
    array.set(entry.getKey(), entry.getValue());
    return array;
  }
  private static Array objectArrayToArray(Object[] objects) {
    Array array = new Array();
    for(Object o: objects) {
      array.add(o);
    }
    return array;
  }
  private static Array primitiveArrayToArray(Object object) {
    Array array = new Array();
    int length = java.lang.reflect.Array.getLength(object);
    for(int i=0; i<length; i++) {
      array.add(java.lang.reflect.Array.get(object, i));
    }
    return array;
  }
  private static Array iterableToArray(Iterable<?> iterable) {
    Array array = new Array();
    for(Object o: iterable) {
      array.add(o);
    }
    return array;
  }
  private static Array iteratorToArray(Iterator<?> iterator) {
    Array array = new Array();
    while(iterator.hasNext()) {
      array.add(iterator.next());
    }
    return array;
  }
  private static Array mapToArray(Map<?,?> map) {
    Array array = new Array();
    for(Map.Entry<?,?> entry: map.entrySet()) {
      array.set(entry.getKey(), entry.getKey());
    }
    return array;
  }
  
  public static Sequence toSequence(Object o) {
    Class<?> clazz = o.getClass(); // nullcheck
    
    if (o instanceof XML) {
      Array array= ((XML)o).elements;
      return (array == null)?null:filterValue(array.sequence(), Filter.AS_XML);
    }
    if (o instanceof Sequenceable) {
      return ((Sequenceable)o).sequence();
    }
    if (o instanceof Object[]) {
      return objectArrayToSequence((Object[])o);
    }
    if (clazz.isArray()) {
      return primitiveArrayToSequence(o);
    }
    
    //Java types
    if (o instanceof Iterable<?>) {
      return iteratorToSequence(((Iterable<?>)o).iterator());
    }
    if (o instanceof Map<?,?>) {
      return iteratorToSequence(((Map<?,?>)o).entrySet().iterator());
    }
    if (o instanceof Iterator<?>) {
      return iteratorToSequence(((Iterator<?>)o));
    }
    
    throw RT.error("value not convertible to sequence: %s", o);
  }
  private static Sequence objectArrayToSequence(final Object[] objects) {
    final int length = objects.length;
    if (length == 0)
      return null;
    return new Sequence() {
      private int index = 0;
      
      @Override
      public Sequence next() {
        if (index == length - 1)
          return null;
        index++;
        return this;
      }
      @Override
      public Object getValue() {
        return objects[index];
      }
      @Override
      public Object getKey() {
        return index;
      }
      
      @Override
      public String toString() {
        return "{"+getKey()+": "+getValue()+'}';
      }
    };
  }
  private static Sequence primitiveArrayToSequence(final Object object) {
    final int length = java.lang.reflect.Array.getLength(object);
    if (length == 0)
      return null;
    return new Sequence() {
      private int index = 0;
      
      @Override
      public Sequence next() {
        if (index == length)
          return null;
        index++;
        return this;
      }
      @Override
      public Object getValue() {
        return java.lang.reflect.Array.get(object, index);
      }
      @Override
      public Object getKey() {
        return index;
      }
      
      @Override
      public String toString() {
        return "{"+getKey()+": "+getValue()+'}';
      }
    };
  }
  private static Sequence iteratorToSequence(final Iterator<?> iterator) {
    if (!iterator.hasNext())
      return null;
    
    final Object value = iterator.next();
    return new Sequence() {
      private Object currentValue = value;
      private int index;
      
      @Override
      public Sequence next() {
        if (!iterator.hasNext())
          return null;
        currentValue = iterator.next();
        index ++;
        return this;
      }
      @Override
      public Object getValue() {
        return currentValue;
      }
      @Override
      public Object getKey() {
        return index;
      }
      
      @Override
      public String toString() {
        return "{"+getKey()+": "+getValue()+'}';
      }
    };
  }
  
  // ---
  
  static abstract class Filter {
    abstract boolean filter(Object o);
    
    static final Filter AS_XML = new Filter() {
      @Override
      boolean filter(Object o) {
        return o instanceof XML;
      }
    };
  }
  
  private static Sequence filterValue(final Sequence sequence, final Filter filter) {
    if (sequence == null)
      return null;
    
    return new Sequence() {
      private Sequence seq = sequence;
      
      @Override
      public Sequence next() {
        seq = seq.next();
        return search();
      }
      
      Sequence search() {
        do {
          Object value = seq.getValue();
          if (filter.filter(value)) {
            return this;
          }
          seq = seq.next();
        } while(seq != null);
        return null;
      }
      
      @Override
      public Object getValue() {
        return seq.getValue();
      }
      
      @Override
      public Object getKey() {
        return seq.getKey();
      }
    }.search();
  }
  
  
  // --- escape ---
  
  
  static StringBuilder append(StringBuilder builder, Object o) {
    if (o instanceof String) {
      return builder.append('\"').append(o).append('\"');
    }
    return builder.append(o);
  }
  
  static StringBuilder escapeXML(StringBuilder builder, Object o) {
    if ((o instanceof XML)) {
      return builder.append(o);
    }
    return escapeXML(builder, String.valueOf(o));
  }
  static StringBuilder escapeXML(StringBuilder builder, String text) {
    for(int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      switch(c) {
      case '<':
        builder.append("&lt;");
        break;
      case '>':
        builder.append("&gt;");
        break;
      default:
        builder.append(c);
      }
    }
    return builder;
  }
  
  
  // --- member access
  
  private final static MethodHandle array_set;
  private final static MethodHandle array_access_get;
  private final static MethodHandle test_receiver_asArray;
  private final static MethodHandle test_receiver_asArrayAccess;
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
    test_receiver_asArrayAccess = lookup.findStatic(RT.class, "test_receiver_asArrayAccess",
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
  
  public static boolean test_receiver_asArrayAccess(Object refValue) {
    return refValue instanceof ArrayAccess;
  }
  
  public static boolean test_receiver_and_key(Class<?> receiverClass, Object refValue, Object key) {
    return key instanceof String && receiverClass.isInstance(refValue);
  }
  
  public static void slowPathArraySet(CallSite callsite, boolean keyMustExist, Object refValue, Object key, Object value) {
    Class<?> refClass = refValue.getClass(); // also nullcheck
    
    String name;
    if (key instanceof String && (!(name = (String)key).isEmpty())) {
      MethodHandle mh = MethodResolver.findSetter(refClass, name);
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
        MethodHandle test = MethodHandles.insertArguments(test_receiver_and_key, 0, refClass);
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
      MethodHandle mh = MethodHandles.guardWithTest(test_receiver_asArray, array_set, callsite.getTarget());
      callsite.setTarget(mh);
      return;
    } 
    throw RT.error("member %s doesn't exist  (or is not accessible) for object: %s", key, refValue);  
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
    
    String name;
    if (key instanceof String && (!(name = (String)key).isEmpty())) {
      MethodHandle mh = MethodResolver.findGetter(refClass, name);
      if (mh == null) {
        mh = MethodResolver.findMethodHandle(refClass, name, 0);
      }
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
        MethodHandle test = MethodHandles.insertArguments(test_receiver_and_key, 0, refClass);
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
      
      MethodHandle mh = MethodHandles.guardWithTest(test_receiver_asArrayAccess, array_access_get, callsite.getTarget());
      callsite.setTarget(mh);
      return result;
    } 
    throw RT.error("member %s doesn't exist (or is not accessible) for object: %s", key, refValue);  
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
  
  public static Object interpreterMethodCall(Node node, String name, Object[] values) {
    CallSite callSite = node.getCallsiteAttribute();
    if (callSite == null) {
      MethodType type = MethodType.genericMethodType(values.length);
      callSite = new CallSite(RT.class, "", type);
      node.setCallsiteAttribute(callSite);
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
  
  
  // --- operators
  
  enum Operation {
    plus(Object.class),
    minus(Object.class),
    mult(Object.class),
    div(Object.class),
    mod(Object.class),
    
    eq(boolean.class),
    ne(boolean.class),
    
    lt(boolean.class),
    le(boolean.class),
    gt(boolean.class),
    ge(boolean.class);
    
    final MethodHandle generic;
    
    private Operation(Class<?> returnType) {
      generic = MethodHandles.lookup().findStatic(RT.class, name(),
          MethodType.methodType(returnType, Object.class, Object.class));
    }
  }
  
  public static CallSite bootstrap(Class<?> declaringClass, String name, MethodType methodType) {
    Operation operation = Operation.valueOf(name);
    CallSite callSite = new CallSite(declaringClass, name, methodType);
    MethodHandle target = RTConvertWorkaround.convertArguments(operation.generic, methodType);
    callSite.setTarget(target);
    return callSite;
  }
  
  /*
  public static CallSite bootstrap(Class<?> declaringClass, String name, MethodType methodType) {
    Operation operation = Operation.valueOf(name);
    CallSite callSite = new CallSite(declaringClass, name, methodType);
    
    Class<?> leftType = methodType.parameterType(0);
    Class<?> rightType = methodType.parameterType(1);
    
    MethodHandle target;
    if (leftType == Object.class) {
      if (rightType == Object.class) {
        target = Operation.fallback_any_any;
      } else {
        target = Operation.fallback_any_right(rightType);
      }
    } else {
      target = Operation.fallback_left_any(leftType);
    }
    
    target = MethodHandles.insertArguments(target, 0, operation, callSite);
    
    callSite.setTarget(MethodHandles.convertArguments(target, methodType));
    return callSite;
  }
  
  public enum Operation {
    plus,
    minus;
    
    private final MethodHandle op_int_int;
    private final MethodHandle op_double_double;
    
    Operation() {
      Lookup lookup = MethodHandles.lookup();
      op_int_int = lookup.findStatic(RT.class, name(),
          MethodType.methodType(int.class, int.class, int.class));
      op_double_double = lookup.findStatic(RT.class, name(),
          MethodType.methodType(double.class, double.class, double.class));
    }
    
    private MethodHandle getMethodHandle(Class<?> leftType, Class<?> rightType) {
      if (leftType == int.class || leftType == Integer.class) {
        if (rightType == int.class || rightType == Integer.class) {
          return op_int_int;
        } 
        if (rightType == double.class || rightType == Double.class) {
          return MethodHandles.convertArguments(op_double_double, op_double_double.type());
        }
      }
      if (leftType == double.class || leftType == Double.class) {
        if (rightType == int.class || rightType == Integer.class) {
          return MethodHandles.convertArguments(op_double_double, op_double_double.type());
        } 
        if (rightType == double.class || rightType == Double.class) {
          return op_double_double;
        }
      }
      throw error(leftType, rightType);
    }
    
    private RuntimeException error(Class<?> leftType, Class<?> rightType) {
      return RT.error("no available operator %s(%s,%s)", name(), leftType.getName(), rightType.getName());
    }
    
    public Object slowPath(CallSite callSite, Object left, Object right) throws Throwable {
      MethodType methodType = callSite.type();
      Class<?> leftType = methodType.parameterType(0);
      Class<?> rightType = methodType.parameterType(1);
      MethodHandle test;
      if (leftType == Object.class) {
        leftType = left.getClass();
        if (rightType == Object.class) {
          rightType = left.getClass();
          test = MethodHandles.insertArguments(isInstanceLeftRight, 0, leftType, rightType);
        } else {
          test = MethodHandles.insertArguments(isInstance, 0, leftType);
        }
      } else {
        // (rightType == Object.class) {
        rightType = right.getClass();
        test = MethodHandles.dropArguments(
            MethodHandles.insertArguments(isInstance, 0, rightType),
            0, leftType);
      }
      
      MethodHandle mh = RTConvertWorkaround.convertArguments(
          getMethodHandle(leftType, rightType),
          methodType);
      
      System.out.println("guard test "+test.type());
      System.out.println("guard target "+mh.type());
      System.out.println("guard fallback "+callSite.getTarget().type());
      
      callSite.setTarget(MethodHandles.guardWithTest(test, mh, callSite.getTarget()));
      
      return mh.invokeGeneric(left, right); 
    }
    
    public static boolean isInstance(Class<?> leftType, Class<?> rightType, Object left, Object right) {
      return leftType.isInstance(left) && rightType.isInstance(right);
    }
    
    private final static MethodHandle isInstance; 
    private final static MethodHandle isInstanceLeftRight; 
    static {
      Lookup lookup = MethodHandles.publicLookup();
      isInstance = lookup.findVirtual(Class.class, "isInstance",
          MethodType.methodType(boolean.class, Object.class));
      isInstanceLeftRight = lookup.findStatic(Operation.class, "isInstance",
          MethodType.methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));
    }
    
    
    
    / FIXME: workaround MethodHandles.convertArguments bug in JSR292 RI
    static MethodHandle fallback_left_any(Class<?> leftType) {
      if (leftType == int.class)
        return fallback_int_any;
      if (leftType == double.class)
        return fallback_double_any;
      return MethodHandles.convertArguments(fallback_any_any,
        MethodType.methodType(Object.class, leftType, Object.class));
    }
    public static Object fallback_int_any(Operation operation, CallSite callSite, int o1, Object o2) throws Throwable {
      return operation.slowPath(callSite, o1, o2);
    }
    public static Object fallback_double_any(Operation operation, CallSite callSite, double o1, Object o2) throws Throwable {
      return operation.slowPath(callSite, o1, o2);
    }
    
    static MethodHandle fallback_any_right(Class<?> rightType) {
      if (rightType == int.class)
        return fallback_any_int;
      if (rightType == double.class)
        return fallback_any_double;
      return MethodHandles.convertArguments(fallback_any_any,
        MethodType.methodType(Object.class, Object.class, rightType));
    }
    public static Object fallback_any_int(Operation operation, CallSite callSite, Object o1, int o2) throws Throwable {
      return operation.slowPath(callSite, o1, o2);
    }
    public static Object fallback_any_double(Operation operation, CallSite callSite, Object o1, double o2) throws Throwable {
      return operation.slowPath(callSite, o1, o2);
    }
    
    final static MethodHandle fallback_any_any;
    private final static MethodHandle fallback_int_any;
    private final static MethodHandle fallback_double_any;
    private final static MethodHandle fallback_any_int;
    private final static MethodHandle fallback_any_double;
    static {
      Lookup publicLookup = MethodHandles.lookup();
      fallback_any_any = publicLookup.findVirtual(Operation.class, "slowPath",
          MethodType.methodType(Object.class, CallSite.class, Object.class, Object.class));
      fallback_int_any = publicLookup.findStatic(Operation.class, "fallback_int_any",
          MethodType.methodType(Object.class, Operation.class, CallSite.class, int.class, Object.class));
      fallback_double_any = publicLookup.findStatic(Operation.class, "fallback_double_any",
          MethodType.methodType(Object.class, Operation.class, CallSite.class, double.class, Object.class));
      fallback_any_int = publicLookup.findStatic(Operation.class, "fallback_any_int",
          MethodType.methodType(Object.class, Operation.class, CallSite.class, Object.class, int.class));
      fallback_any_double = publicLookup.findStatic(Operation.class, "fallback_any_double",
          MethodType.methodType(Object.class, Operation.class, CallSite.class, Object.class, double.class));
    }
  }*/
}
