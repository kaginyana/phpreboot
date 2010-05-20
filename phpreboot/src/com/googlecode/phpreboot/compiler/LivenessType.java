package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

enum LivenessType implements Type {
  ALIVE, DEAD;

  @Override
  public String getName() {
    return name();
  }

  @Override
  public Class<?> getRuntimeClass() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Class<?> getUnboxedRuntimeClass() {
    throw new UnsupportedOperationException();
  }
}