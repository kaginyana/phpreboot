package com.googlecode.phpreboot.module;


public class MathModule extends Module {
  @Export
  public double math_sin(double a) {
    return Math.sin(a);
  }
  
  @Export
  public double math_cos(double a) {
    return Math.cos(a);
  }
  
  @Export
  public double math_exp(double a) {
    return Math.exp(a);
  }
  
  @Export
  public double math_log(double a) {
    return Math.log(a);
  }
  
  @Export
  public double math_pow(double a, double b) {
    return Math.pow(a, b);
  }
}
