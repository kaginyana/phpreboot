/**
 * 
 */
package com.googlecode.phpreboot.interpreter;

@SuppressWarnings("serial")
public class ReturnError extends Error {
  public Object value;
  ReturnError() {
    // do nothing
  }

  // called by compiled code
  public static ReturnError instance(Object value) {
    ReturnError instance = INSTANCE;
    instance.value = value;
    return instance;
  }
  
  private static final ReturnError INSTANCE = new ReturnError();
}