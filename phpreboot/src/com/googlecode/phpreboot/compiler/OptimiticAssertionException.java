package com.googlecode.phpreboot.compiler;

@SuppressWarnings("serial")
public class OptimiticAssertionException extends RuntimeException {
  private OptimiticAssertionException() {
    // enforce singleton
  }
  
  public static final OptimiticAssertionException INSTANCE = new OptimiticAssertionException();
}
