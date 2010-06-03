package com.googlecode.phpreboot.runtime;

import java.util.Properties;

public class RTFlag {
  private static Object getOption(String name) {
    return SYSTEM_PROPERTIES.get(name);
  }
  
  private static final Properties SYSTEM_PROPERTIES = System.getProperties();
  
  private static boolean getOption(String name, boolean defaultValue) {
    Object value = getOption(name);
    return (value != null)? true: defaultValue;
  }
  
  public static final boolean COMPILER_ENABLE = getOption("compiler.enable", true);
  public static final boolean COMPILER_OPTIMISTIC = getOption("compiler.optimistic", true);
  public static final boolean COMPILER_TRACE = getOption("compiler.trace", true);
  
  public static final boolean DEBUG = getOption("debug", false);
}
