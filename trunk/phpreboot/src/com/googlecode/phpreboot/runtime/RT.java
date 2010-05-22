package com.googlecode.phpreboot.runtime;

import java.dyn.CallSite;
import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.dyn.MethodType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.interpreter.Profile;
import com.googlecode.phpreboot.runtime.Array.Entry;

public class RT {
  @SuppressWarnings("serial")
  public static class RTError extends Error {
    public RTError(String message) {
      super(message);
    }
    public RTError(String message, Throwable cause) {
      super(message, cause);
    }
  }
  
  public static RTError error(String format, Object arg) {
    return error(format, new Object[]{arg});
  }
  public static RTError error(String format, Object arg, Object arg2) {
    return error(format, new Object[]{arg, arg2});
  }
  public static RTError error(String format, Object... args) {
    return new RTError(String.format(format, args));
  }
  
  public static RTError error(Node node, Throwable t) {
    if (t.getCause() != null) {
      t = t.getCause();
    }
    /*if (t instanceof RTError) {
      return (RuntimeException)t;
    }*/
    String location = (node == null)? "": " at " +
        node.getLineNumberAttribute() + ',' + node.getColumnNumberAttribute();
    return new RTError(t.getMessage() + location, t);
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
        return plus(l, (String)right);
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
        return plus(l, (String)right);
      }
      throw error("invalid value for operation + "+right);
    }
    if (left instanceof String) {
      return plus((String)left, right);
    }
    if (right instanceof String) {
      return plus(left, (String)right);
    }
    
    throw error("invalid value for operation + "+left+" "+right);
  }
  public static int plus(int left, int right) {
    return left + right;
  }
  public static double plus(double left, double right) {
    return left + right;
  }
  public static String plus(String left, Object right) {
    return left + right;
  }
  public static String plus(String left, int right) {
    return left + right;
  }
  public static String plus(String left, double right) {
    return left + right;
  }
  public static String plus(Object left, String right) {
    return left + right;
  }
  public static String plus(int left, String right) {
    return left + right;
  }
  public static String plus(double left, String right) {
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
  
  @SuppressWarnings("unchecked")
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
    if (left instanceof Comparable<?> && right.getClass() == left.getClass()) {
      return lt((Comparable<? super Object>)left, right);
    }
    throw error("invalid type "+left);
  }
  public static boolean lt(int left, int right) {
    return left < right;
  }
  public static boolean lt(double left, double right) {
    return left < right;
  }
  public static boolean lt(Comparable<? super Object> left, Object right) {
    return left.compareTo(right) < 0;
  }
  
  @SuppressWarnings("unchecked")
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
    if (left instanceof Comparable<?> && right.getClass() == left.getClass()) {
      return le((Comparable<? super Object>)left, right);
    }
    throw error("invalid type "+left);
  }
  public static boolean le(int left, int right) {
    return left <= right;
  }
  public static boolean le(double left, double right) {
    return left <= right;
  }
  public static boolean le(Comparable<? super Object> left, Object right) {
    return left.compareTo(right) <= 0;
  }
  
  @SuppressWarnings("unchecked")
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
    if (left instanceof Comparable<?> && right.getClass() == left.getClass()) {
      return gt((Comparable<? super Object>)left, right);
    }
    throw error("invalid type "+left);
  }
  public static boolean gt(int left, int right) {
    return left > right;
  }
  public static boolean gt(double left, double right) {
    return left > right;
  }
  public static boolean gt(Comparable<? super Object> left, Object right) {
    return left.compareTo(right) > 0;
  }
  
  @SuppressWarnings("unchecked")
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
    if (left instanceof Comparable<?> && right.getClass() == left.getClass()) {
      return ge((Comparable<? super Object>)left, right);
    }
    throw error("invalid type "+left);
  }
  public static boolean ge(int left, int right) {
    return left >= right;
  }
  public static boolean ge(double left, double right) {
    return left >= right;
  }
  public static boolean ge(Comparable<? super Object> left, Object right) {
    return left.compareTo(right) >= 0;
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
    
    if (o instanceof Sequence) {
      return (Sequence)o;
    }
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
  
  
  static class CallSiteProfile extends CallSite implements Profile {
    public CallSiteProfile(MethodType type) {
      super(RT.class, "", type);
    }
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
        } catch(Error e) {
          throw e;
        } catch (Throwable e) {
          throw RT.error((Node)null, e);
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
    CallSiteProfile callSite = (CallSiteProfile)node.getProfileAttribute();
    MethodHandle target;
    if (callSite == null) {
      
      // cache for next call
      
      MethodType methodType = MethodType.methodType(void.class, Object.class, Object.class, Object.class);
      callSite = new CallSiteProfile(methodType);
      target = MethodHandles.insertArguments(slowPathArraySet, 0, callSite, keyMustExist);
      callSite.setTarget(target);
      
      slowPathArraySet(callSite, keyMustExist, refValue, key, value);
      
      node.setProfileAttribute(callSite);
      
    } else {
      target = callSite.getTarget();
      
      try {
        //FIXME should be invokeExact
        target.invokeGeneric(refValue, key, value);
      } catch(Error e) {
        throw e;
      } catch (Throwable e) {
        throw RT.error((Node)null, e);
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
        } catch(Error e) {
          throw e;
        } catch (Throwable e) {
          throw RT.error((Node)null, e);
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
      if (keyMustExist && result == ArrayAccess.INVALID_KEY) {
        throw RT.error("member %s doesn't exist for array: %s", key, arrayAccess);
      }
      result = (result != ArrayAccess.INVALID_KEY)? result: null;
      
      MethodHandle mh = MethodHandles.guardWithTest(test_receiver_asArrayAccess, array_access_get, callsite.getTarget());
      callsite.setTarget(mh);
      return result;
    } 
    throw RT.error("member %s doesn't exist (or is not accessible) for object: %s", key, refValue);  
  }
  
  public static Object interpreterArrayGet(Node node, Object refValue, Object key, boolean keyMustExist) {
    CallSiteProfile callSite = (CallSiteProfile)node.getProfileAttribute();
    MethodHandle target;
    if (callSite == null) {
      
      // cache for next call
      
      MethodType methodType = MethodType.methodType(Object.class, Object.class, Object.class);
      callSite = new CallSiteProfile(methodType);
      target = MethodHandles.insertArguments(slowPathArrayGet, 0, callSite, keyMustExist);
      callSite.setTarget(target);
      
      Object result = slowPathArrayGet(callSite, keyMustExist, refValue, key);
      node.setProfileAttribute(callSite);
      return result;
      
    } else {
      target = callSite.getTarget();
      
      try {
        //FIXME should be invokeExact
        return target.invokeGeneric(refValue, key);
      } catch(Error e) {
        throw e;
      } catch (Throwable e) {
        throw RT.error((Node)null, e);
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
    CallSiteProfile callSite = (CallSiteProfile)node.getProfileAttribute();
    if (callSite == null) {
      MethodType type = MethodType.genericMethodType(values.length);
      callSite = new CallSiteProfile(type);
      node.setProfileAttribute(callSite);
      MethodHandle mh = MethodHandles.insertArguments(slowPathMethodCall, 0, callSite, name);
      mh = MethodHandles.collectArguments(mh, type);
      callSite.setTarget(mh);
      
      return slowPathMethodCall(callSite, name, values);
    }
    
    MethodHandle target = callSite.getTarget();
    try {
      return target.invokeVarargs(values);
    } catch(Error e) {
      throw e;
    } catch (Throwable e) {
      throw RT.error((Node)null, e);
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
    } catch(Error e) {
      throw e;
    } catch (Throwable e) {
      throw RT.error((Node)null, e);
    } 
  }
  
  
  // --- operators
  /* raw version
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
    //MethodHandle target = RTConvertWorkaround.convertArguments(operation.generic, methodType);
    MethodHandle target = MethodHandles.convertArguments(operation.generic, methodType);
    callSite.setTarget(target);
    return callSite;
  }*/
  
  
  public static CallSite bootstrap(Class<?> declaringClass, String name, MethodType methodType) {
    OpBehavior opBehavior = OpBehavior.valueOf(name);
    CallSite callSite = new CallSite(declaringClass, name, methodType);
    
    MethodHandle target = MethodHandles.insertArguments(OpBehavior.slowPath, 0, opBehavior, callSite);
    callSite.setTarget(MethodHandles.convertArguments(target, methodType));
    return callSite;
  }
  
  public static boolean isInstance(Class<?> leftType, Class<?> rightType, Object left, Object right) {
    return leftType.isInstance(left) && rightType.isInstance(right);
  }
  
  static class OpBehavior {
    private final String operator;
    private final MethodHandle op_int_int;
    private final MethodHandle op_double_double;
    
    // constructor for sub classes
    OpBehavior(String operator, MethodHandle op_init_int, MethodHandle op_double_double) {
      this.operator = operator;
      this.op_int_int = op_init_int;
      this.op_double_double = op_double_double;
    }
    
    // constructor for binary op 
    OpBehavior(String operator, Lookup lookup, String name) {
      this(operator,
          lookup.findStatic(RT.class, name,
              MethodType.methodType(int.class, int.class, int.class)),
          lookup.findStatic(RT.class, name,
              MethodType.methodType(double.class, double.class, double.class))
            );
    }
    
    /*@Nullabble*/MethodHandle getMethodHandle(Class<?> leftType, Class<?> rightType) {
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
      return null;
    }
    
    static class Plus extends OpBehavior {
      private final MethodHandle plus_string_any;
      private final MethodHandle plus_string_int;
      private final MethodHandle plus_string_double;
      private final MethodHandle plus_any_string;
      private final MethodHandle plus_int_string;
      private final MethodHandle plus_double_string;
      
      Plus(Lookup lookup) {
        super("+", lookup, "plus");
        plus_string_any = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, String.class, Object.class));
        plus_string_int = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, String.class, int.class));
        plus_string_double = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, String.class, double.class));
        plus_any_string = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, Object.class, String.class));
        plus_int_string = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, int.class, String.class));
        plus_double_string = lookup.findStatic(RT.class, "plus",
            MethodType.methodType(String.class, double.class, String.class));
      }
      
      @Override
      /*@Nullabble*/MethodHandle getMethodHandle(Class<?> leftType, Class<?> rightType) {
        if (leftType == String.class) {
          if (rightType == int.class || rightType == Integer.class)
            return plus_string_int;
          if (rightType == double.class || rightType == Double.class)
            return plus_string_double;
          return plus_string_any;
        }
        if (rightType == String.class) {
          if (leftType == int.class || leftType == Integer.class)
            return plus_int_string;
          if (leftType == double.class || leftType == Double.class)
            return plus_double_string;
          return plus_any_string;
        }
        return super.getMethodHandle(leftType, rightType);
      }
    }
    
    static class Cmp extends OpBehavior {
      private final MethodHandle op_cmp_any;
      
      Cmp(String operator, Lookup lookup, String name) {
        super(operator,
            lookup.findStatic(RT.class, name,
                MethodType.methodType(boolean.class, int.class, int.class)),
            lookup.findStatic(RT.class, name,
                MethodType.methodType(boolean.class, double.class, double.class))
              );
        op_cmp_any = lookup.findStatic(RT.class, name,
            MethodType.methodType(boolean.class, Comparable.class, Object.class));
      }
      
      @Override
      /*@Nullabble*/MethodHandle getMethodHandle(Class<?> leftType, Class<?> rightType) {
        /*@Nullabble*/MethodHandle mh = super.getMethodHandle(leftType, rightType);
        if (mh != null)
          return mh;
        if (rightType == leftType && Comparable.class.isAssignableFrom(leftType)) {
          return op_cmp_any;
        }
        return null;
      }
    }
    
    static OpBehavior valueOf(String name) {
      OpBehavior opBehavior = BEHAVIOR_MAP.get(name);
      if (opBehavior == null) {
        throw RT.error("unknown operation %s", name);
      }
      return opBehavior;
    }
    private static final HashMap<String, OpBehavior> BEHAVIOR_MAP;
    static {
      Lookup lookup = MethodHandles.publicLookup();
      HashMap<String, OpBehavior> map = new HashMap<String, RT.OpBehavior>();
      map.put("plus", new Plus(lookup));
      
      map.put("minus", new OpBehavior("-", lookup, "minus"));
      map.put("mult", new OpBehavior("*", lookup, "mult"));
      map.put("div", new OpBehavior("/", lookup, "div"));
      map.put("mod", new OpBehavior("%", lookup, "mod"));
      
      map.put("lt", new Cmp("<", lookup, "lt"));
      map.put("le", new Cmp("<=", lookup, "le"));
      map.put("gt", new Cmp(">", lookup, "gt"));
      map.put("ge", new Cmp(">=", lookup, "ge"));
      
      BEHAVIOR_MAP = map;
    }
    
    public Object slowPath(CallSite callSite, Object left, Object right) throws Throwable {
      MethodType methodType = callSite.type();
      Class<?> leftType = methodType.parameterType(0);
      Class<?> rightType = methodType.parameterType(1);
      MethodHandle test;
      if (leftType == Object.class) {
        leftType = left.getClass();
        if (rightType == Object.class) {
          rightType = right.getClass();
          test = MethodHandles.insertArguments(isInstanceLeftRight, 0, leftType, rightType);
        } else {
          test = MethodHandles.insertArguments(isInstance, 0, leftType);
        }
      } else {
        // rightType == any
        rightType = right.getClass();
        test = MethodHandles.dropArguments(
            MethodHandles.insertArguments(isInstance, 0, rightType),
            0, leftType);
      }
      
      MethodHandle mh = getMethodHandle(leftType, rightType);
      if (mh == null)
        throw error(leftType, rightType);
      mh = MethodHandles.convertArguments(mh, methodType);
      callSite.setTarget(MethodHandles.guardWithTest(test, mh, callSite.getTarget()));
      return mh.invokeGeneric(left, right); 
    }
    
    private RTError error(Class<?> leftType, Class<?> rightType) {
      return RT.error("no available operator %s(%s,%s)", operator, leftType.getName(), rightType.getName());
    }
    
    private final static MethodHandle isInstance; 
    private final static MethodHandle isInstanceLeftRight; 
    static {
      Lookup lookup = MethodHandles.publicLookup();
      isInstance = lookup.findVirtual(Class.class, "isInstance",
          MethodType.methodType(boolean.class, Object.class));
      isInstanceLeftRight = lookup.findStatic(RT.class, "isInstance",
          MethodType.methodType(boolean.class, Class.class, Class.class, Object.class, Object.class));
    }
    
    final static MethodHandle slowPath;
    static {
      Lookup publicLookup = MethodHandles.lookup();
      slowPath = publicLookup.findVirtual(OpBehavior.class, "slowPath",
          MethodType.methodType(Object.class, CallSite.class, Object.class, Object.class));
    }
  }
}
