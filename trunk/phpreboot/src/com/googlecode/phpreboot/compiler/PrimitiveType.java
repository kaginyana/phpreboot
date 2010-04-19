package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;

public enum PrimitiveType implements Type {
  ANY(Object.class) {
    @Override
    public Object checkCast(Object value) {
      return value;
    }
  },
  BOOLEAN(boolean.class) {
    @Override
    public Object checkCast(Object value) {
      if (!(value instanceof Boolean)) {
        throw RT.error("%s is not a boolean", value);
      }
      return value;
    }
  },
  INT(int.class) {
    @Override
    public Object checkCast(Object value) {
      if (!(value instanceof Integer)) {
        throw RT.error("%s is not an integer", value);
      }
      return value;
    }
  },
  DOUBLE(double.class) {
    @Override
    public Object checkCast(Object value) {
      if (!(value instanceof Double)) {
        throw RT.error("%s is not a double ", value);
      }
      return value;
    }
  },
  STRING(String.class) {
    @Override
    public Object checkCast(Object value) {
      if (!(value instanceof String)) {
        throw RT.error("%s is not a string ", value);
      }
      return value;
    }
  },
  //XML(??)
  VOID(null, void.class),
  NULL(null, null)
  ;
  
  private final org.objectweb.asm.Type asmType;
  private final Class<?> runtimeType;
  
  private PrimitiveType(Class<?> runtimeType) {
    this(org.objectweb.asm.Type.getType(runtimeType), runtimeType);
  }
  
  private PrimitiveType(org.objectweb.asm.Type asmType, Class<?> runtimeType) {
    this.asmType = asmType;
    this.runtimeType = runtimeType;
  }
  
  @Override
  public org.objectweb.asm.Type asASMType() {
    return asmType;
  }
  
  @Override
  public Class<?> asRuntimeType() {
    return runtimeType;
  }
  
  @Override
  public String getName() {
    return name().toLowerCase(); 
  }
  
  @Override
  public Object checkCast(Object value) {
    throw new AssertionError();
  }
}
