package com.googlecode.phpreboot.model;

import java.util.HashMap;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public enum PrimitiveType implements Type {
  ANY(Object.class, Object.class, null),
  VOID(Object.class, void.class, null),
  BOOLEAN(Boolean.class, boolean.class, false),
  INT(Integer.class, int.class, 0),
  DOUBLE(Double.class, double.class, 0.0),
  STRING(String.class, String.class, ""),
  ARRAY(Array.class, Array.class, null),
  SEQUENCE(Sequence.class, Sequence.class, null),
  XML(XML.class, XML.class, null),
  URI(URI.class, URI.class, null),
  FUNCTION(Function.class, Function.class, null)
  ;
  
  private final String type;
  private final Class<?> runtimeClass;
  private final Class<?> unboxedRuntimeClass;
  private final Object defaultValue;
  
  private PrimitiveType(Class<?> runtimeClass, Class<?> unboxedRuntimeClass, Object defaultValue) {
    type = name().toLowerCase();
    this.runtimeClass = runtimeClass;
    this.unboxedRuntimeClass = unboxedRuntimeClass;
    this.defaultValue = defaultValue;
  }
  
  @Override
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }
  @Override
  public Class<?> getUnboxedRuntimeClass() {
    return unboxedRuntimeClass;
  }
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public String getName() {
    return type;
  }
  @Override
  public String toString() {
    return type;
  }
  
  public static PrimitiveType lookup(String name) {
    PrimitiveType token = TOKEN_MAP.get(name);
    if (token == null) {
      throw RT.error("Unknown type %s", name);
    }
    return token;
  }
  
  static private final HashMap<String, PrimitiveType> TOKEN_MAP;
  static {
    HashMap<String, PrimitiveType> map = new HashMap<>(32);
    for(PrimitiveType token: PrimitiveType.values()) {
      map.put(token.type, token);
    }
    TOKEN_MAP = map;
  }
}