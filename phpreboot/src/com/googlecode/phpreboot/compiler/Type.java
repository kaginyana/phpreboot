package com.googlecode.phpreboot.compiler;

public interface Type {
  public String getName();
  
  public org.objectweb.asm.Type asASMType();
  public Class<?> asRuntimeType();
  public Object checkCast(Object value);
}
