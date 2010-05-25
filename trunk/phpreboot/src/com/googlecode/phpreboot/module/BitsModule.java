package com.googlecode.phpreboot.module;


public class BitsModule extends Module {
  @Export
  public int bits_shl(int a, int b) {
    return a << b;
  }
  
  @Export
  public int bits_or(int a, int b) {
    return a | b;
  }
  
  @Export
  public void echo_char(int a) {
    System.out.print((char)a);
  }
}
