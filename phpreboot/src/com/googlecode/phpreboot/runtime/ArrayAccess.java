package com.googlecode.phpreboot.runtime;

public interface ArrayAccess {
  public static final Object INVALID_KEY = new Object() {
    @Override
    public String toString() {
      return "!INVALID_KEY!";
    }
  };
  
  public Object get(Object key);
}
