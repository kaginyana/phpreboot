package com.googlecode.phpreboot.runtime;

public class Functions {
  public static String join(String delimiter, Sequenceable sequenceable) {
    Sequence seq = sequenceable.sequence();
    if (seq == null)
      return "";
    seq = seq.next();
    StringBuilder builder = new StringBuilder();
    builder.append(seq.getValue());
    for(; seq != null; seq = seq.next()) {
      builder.append(delimiter).append(seq.getValue());
      seq = seq.next();
    }
    return builder.toString();
  }
}
