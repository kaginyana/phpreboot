/**
 * 
 */
package com.googlecode.phpreboot.interpreter;

@SuppressWarnings("serial")
public class BreakError extends Error {
  String label;
  BreakError() {
    // do nothing
  }
  
  void mayRethrow(/*@Nullable*/String loopLabel) {
    String label = this.label;
    if (label != null && !label.equals(loopLabel))
      throw this;
  }
  
  // called by compiled code
  public static BreakError instance(String label) {
    BreakError instance = INSTANCE;
    instance.label = label;
    return instance;
  }
  
  private static final BreakError INSTANCE = new BreakError();
}