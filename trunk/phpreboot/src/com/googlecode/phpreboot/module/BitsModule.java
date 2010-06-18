package com.googlecode.phpreboot.module;

import static org.objectweb.asm.Opcodes.*;


public class BitsModule extends Module {
  @Export
  @IntrinsicOpcode(ISHL)
  public static int bits_shl(int a, int b) {
    return a << b;
  }
  
  @Export
  @IntrinsicOpcode(ISHR)
  public static int bits_shr(int a, int b) {
    return a >> b;
  }
  
  @Export
  @IntrinsicOpcode(IUSHR)
  public static int bits_ushr(int a, int b) {
    return a >>> b;
  }
  
  @Export
  @IntrinsicOpcode(IAND)
  public static int bits_and(int a, int b) {
    return a & b;
  }
  
  @Export
  @IntrinsicOpcode(IOR)
  public static int bits_or(int a, int b) {
    return a | b;
  }
  
  @Export
  @IntrinsicOpcode(IXOR)
  public static int bits_xor(int a, int b) {
    return a ^ b;
  }
  
  @Export
  public static void echo_char(int a) {
    System.out.print((char)a);
  }
}
