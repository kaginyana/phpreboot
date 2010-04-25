package com.googlecode.phpreboot.runtime;

public interface Sequence {
  public /*maybenull*/Sequence next();
  public Object getKey();
  public Object getValue();
}
