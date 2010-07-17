package com.googlecode.phpreboot.compiler;

@SuppressWarnings("serial")
class CodeNotCompilableTypeCheckException extends RuntimeException {
  private CodeNotCompilableTypeCheckException() {
    // enforce singleton
  }
  
  static final CodeNotCompilableTypeCheckException INSTANCE = new CodeNotCompilableTypeCheckException();
}
