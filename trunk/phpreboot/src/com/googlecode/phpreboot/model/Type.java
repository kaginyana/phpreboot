package com.googlecode.phpreboot.model;

public interface Type {
  public String getName();
  public Class<?> getRuntimeClass();
  public Class<?> getUnboxedRuntimeClass();
}
