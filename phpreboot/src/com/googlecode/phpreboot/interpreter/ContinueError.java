/**
 * 
 */
package com.googlecode.phpreboot.interpreter;

@SuppressWarnings("serial")
public class ContinueError extends Error {
  String label;
  ContinueError() {
    // do nothing
  }
  
  void mayRethrow(/*@Nullable*/String loopLabel) {
    String label = this.label;
    if (label != null && !label.equals(loopLabel))
      throw this;
  }
  
  // called by compiled code
  public static ContinueError instance(String label) {
    ContinueError instance = INSTANCE;
    instance.label = label;
    return instance;
  }
  
  private static final ContinueError INSTANCE = new ContinueError();
}