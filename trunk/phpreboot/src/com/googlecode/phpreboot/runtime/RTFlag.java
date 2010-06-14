package com.googlecode.phpreboot.runtime;

import java.util.Properties;

public class RTFlag {
  private static Object getOption(String name) {
    return SYSTEM_PROPERTIES.get(name);
  }
  
  private static final Properties SYSTEM_PROPERTIES = System.getProperties();
  
  private static boolean getOption(String name, boolean defaultValue) {
    Object value = getOption(name);
    return (value != null)? Boolean.parseBoolean(value.toString()): defaultValue;
  }
  private static int getOption(String name, int defaultValue) {
    Object value = getOption(name);
    return (value != null)? Integer.parseInt(value.toString()): defaultValue;
  }
  
  public static final boolean DEBUG = getOption("debug", false);
  
  public static final boolean COMPILER_ENABLE = getOption("compiler.enable", true);
  public static final boolean COMPILER_OPTIMISTIC = COMPILER_ENABLE && getOption("compiler.optimistic", true);
  public static final boolean COMPILER_TRACE = COMPILER_ENABLE && getOption("compiler.trace", true);
  
  public static final int COMPILER_FUNCTION_THRESHOLD = getOption("compiler.function.threshold", 203);
  public static final int COMPILER_TRACE_THRESHOLD = getOption("compiler.trace.threshold", 157);
}
