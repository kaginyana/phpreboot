package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

abstract class Generator {
  abstract void gen(GenEnv env);
  abstract Type liveness();
}