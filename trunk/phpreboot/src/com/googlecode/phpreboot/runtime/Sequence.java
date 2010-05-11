package com.googlecode.phpreboot.runtime;

public interface Sequence {
  public /*@Nullable*/Sequence next();
  public Object getKey();
  public Object getValue();
}
