package com.googlecode.phpreboot.model;

public class IntrinsicInfo {
  private final Class<?> declaringClass;
  private final String name;
  
  public IntrinsicInfo(Class<?> declaringClass, String name) {
    this.declaringClass = declaringClass;
    this.name = name;
  }
  
  public Class<?> getDeclaringClass() {
    return declaringClass;
  }
  public String getName() {
    return name;
  }
}
