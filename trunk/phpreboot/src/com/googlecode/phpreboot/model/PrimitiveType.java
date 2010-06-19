package com.googlecode.phpreboot.model;

import java.util.HashMap;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public enum PrimitiveType implements Type {
  ANY(null),
  VOID(null),
  BOOLEAN(false),
  INT(0),
  DOUBLE(0.0),
  STRING(""),
  ARRAY(null),
  SEQUENCE(null),
  XML(null),
  URI(null),
  FUNCTION(null)
  ;
  
  private final String type;
  private final Object defaultValue;
  
  private PrimitiveType(Object defaultValue) {
    type = name().toLowerCase();
    this.defaultValue = defaultValue;
  }
  
  @Override
  public Class<?> getRuntimeClass() {
    switch(this) {
    case ANY:
    case VOID:
      return Object.class;
    case BOOLEAN:
      return Boolean.class;
    case INT:
      return Integer.class;
    case DOUBLE:
      return Double.class;
    case STRING:
      return String.class;
    case ARRAY:
      return Array.class;
    case SEQUENCE:
      return Sequence.class;
    case XML:
      return XML.class;
    case URI:
      return URI.class;
    case FUNCTION:
      return Function.class;
    default:
      throw new AssertionError();
    }
  }
  @Override
  public Class<?> getUnboxedRuntimeClass() {
    switch(this) {
    case VOID:
      return void.class;
    case BOOLEAN:
      return boolean.class;
    case INT:
      return int.class;
    case DOUBLE:
      return double.class;
    default:
      return getRuntimeClass();
    }
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
    PrimitiveType token = PrimitiveType.tokenMap.get(name);
    if (token == null) {
      throw RT.error("Unknown type %s", name);
    }
    return token;
  }
  
  static final HashMap<String, PrimitiveType> tokenMap;
  static {
    HashMap<String, PrimitiveType> map =
      new HashMap<String, PrimitiveType>(32);
    for(PrimitiveType token: PrimitiveType.values()) {
      map.put(token.type, token);
    }
    tokenMap = map;
  }
}