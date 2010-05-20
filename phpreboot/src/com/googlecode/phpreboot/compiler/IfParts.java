package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.ast.Node;

class IfParts {
  final boolean inCondition;
  final /*@Nullable*/Node truePart;
  final /*@Nullable*/Node falsePart;
  
  IfParts(boolean inCondition, /*@Nullable*/Node truePart, /*@Nullable*/Node falsePart) {
    this.inCondition = inCondition;
    this.truePart = truePart;
    this.falsePart = falsePart;
  }
  
  public IfParts swap() {
    return new IfParts(inCondition, falsePart, truePart);
  }
}
