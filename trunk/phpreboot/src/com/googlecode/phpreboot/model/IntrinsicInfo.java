package com.googlecode.phpreboot.model;

public class IntrinsicInfo {
  private final Class<?> declaringClass;
  private final String name;
  
  /**
   * @param declaringClass declaring class or null if it's the current class.
   * @param name method name
   */
  public IntrinsicInfo(/*@Nullable*/Class<?> declaringClass, String name) {
    this.declaringClass = declaringClass;
    this.name = name;
  }
  
  // null means current class.
  public /*@Nullable*/Class<?> getDeclaringClass() {
    return declaringClass;
  }
  public String getName() {
    return name;
  }
}
