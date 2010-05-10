package com.googlecode.phpreboot.compiler;

@SuppressWarnings("serial")
class CodeNotCompilableException extends RuntimeException {
  private CodeNotCompilableException() {
    // TODO Auto-generated constructor stub
  }
  
  static final CodeNotCompilableException INSTANCE = new CodeNotCompilableException();
}
