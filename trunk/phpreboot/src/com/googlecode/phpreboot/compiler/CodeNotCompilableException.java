package com.googlecode.phpreboot.compiler;

@SuppressWarnings("serial")
class CodeNotCompilableException extends RuntimeException {
  private CodeNotCompilableException() {
    // enforce singleton
  }
  
  static final CodeNotCompilableException INSTANCE = new CodeNotCompilableException();
}
