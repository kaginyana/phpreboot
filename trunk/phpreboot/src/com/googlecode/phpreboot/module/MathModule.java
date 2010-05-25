package com.googlecode.phpreboot.module;


public class MathModule extends Module {
  @Export
  @Intrinsic(declaringClass=Math.class, name="sin")
  public static double math_sin(double a) {
    return Math.sin(a);
  }
  
  @Export
  @Intrinsic(declaringClass=Math.class, name="cos")
  public static double math_cos(double a) {
    return Math.cos(a);
  }
  
  @Export
  @Intrinsic(declaringClass=Math.class, name="exp")
  public static double math_exp(double a) {
    return Math.exp(a);
  }
  
  @Export
  @Intrinsic(declaringClass=Math.class, name="log")
  public static double math_log(double a) {
    return Math.log(a);
  }
  
  @Export
  @Intrinsic(declaringClass=Math.class, name="pow")
  public static double math_pow(double a, double b) {
    return Math.pow(a, b);
  }
}
