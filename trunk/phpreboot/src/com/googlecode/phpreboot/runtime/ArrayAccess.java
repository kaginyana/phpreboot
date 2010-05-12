package com.googlecode.phpreboot.runtime;

public interface ArrayAccess {
  public static final Object INVALID_KEY = new Object();
  
  public Object get(Object key);
}
