package com.googlecode.phpreboot.runtime;

/** Encapsulate an object and is recognized by the runtime
 *  as a protected object that should not be escaped.
 */
public class Protect {
  private final Object value;

  public Protect(Object value) {
    this.value = value;
  }
  
  @Override
  public String toString() {
    return String.valueOf(value);
  }
}
