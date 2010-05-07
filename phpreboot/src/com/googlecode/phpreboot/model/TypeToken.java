package com.googlecode.phpreboot.model;

import java.util.HashMap;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public enum TypeToken {
  ANY(Object.class, null),
  BOOLEAN(Boolean.class, false),
  INT(Integer.class, 0),
  DOUBLE(Double.class, 0.0),
  STRING(String.class, ""),
  ARRAY(Array.class, null),
  SEQUENCE(Sequence.class, null),
  XML(XML.class, null),
  URI(URI.class, null)
  ;
  
  private final String type;
  private final Class<?> runtimeClass;
  private final Object defaultValue;
  
  private TypeToken(Class<?> runtimeClass, Object defaultValue) {
    type = name().toLowerCase();
    this.runtimeClass = runtimeClass;
    this.defaultValue = defaultValue;
  }
  
  public Class<?> getRuntimeClass() {
    return runtimeClass;
  }
  public Object getDefaultValue() {
    return defaultValue;
  }
  
  @Override
  public String toString() {
    return type;
  }
  
  public static TypeToken lookup(String name) {
    TypeToken token = TypeToken.tokenMap.get(name);
    if (token == null) {
      throw RT.error("Unknown type %s", name);
    }
    return token;
  }
  
  static final HashMap<String, TypeToken> tokenMap;
  static {
    HashMap<String, TypeToken> map =
      new HashMap<String, TypeToken>();
    for(TypeToken token: TypeToken.values()) {
      map.put(token.type, token);
    }
    tokenMap = map;
  }
}