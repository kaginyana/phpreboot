package com.googlecode.phpreboot.runtime;

public interface ArrayAccess {
  public Object __get__(Object key);
  public Object __get__(int index);
}
