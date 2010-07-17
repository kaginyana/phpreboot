package com.googlecode.phpreboot.compiler;

@SuppressWarnings("serial")
class UntakenBranchTypeCheckException extends RuntimeException {
  private UntakenBranchTypeCheckException() {
    // enforce singleton
  }
  
  static final UntakenBranchTypeCheckException INSTANCE = new UntakenBranchTypeCheckException();
}
