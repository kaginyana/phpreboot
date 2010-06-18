package com.googlecode.phpreboot.model;

public class IntrinsicInfo {
  private final Class<?> declaringClass;
  private final String name;
  private final int opcode;
  
  /**
   * @param declaringClass declaring class or null if it's the current class.
   * @param name method name
   * @param opcode bytecode opcode or -1
   */
  public IntrinsicInfo(/*@Nullable*/Class<?> declaringClass, String name, int opcode) {
    this.declaringClass = declaringClass;
    this.name = name;
    this.opcode = opcode;
  }
  
  // null means current class.
  public /*@Nullable*/Class<?> getDeclaringClass() {
    return declaringClass;
  }
  public String getName() {
    return name;
  }
  // return the butecode opcode or -1
  public int getOpcode() {
    return opcode;
  }
}
