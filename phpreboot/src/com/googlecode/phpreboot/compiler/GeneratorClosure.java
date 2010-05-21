package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;

abstract class GeneratorClosure {
  abstract Type gen(GenEnv env);
  abstract Type liveness();
}