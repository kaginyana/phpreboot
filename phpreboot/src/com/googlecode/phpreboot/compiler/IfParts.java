package com.googlecode.phpreboot.compiler;

class IfParts {
  final boolean inCondition;
  final /*@Nullable*/Generator truePart;
  final /*@Nullable*/Generator falsePart;
  
  IfParts(boolean inCondition, /*@Nullable*/Generator truePart, /*@Nullable*/Generator falsePart) {
    this.inCondition = inCondition;
    this.truePart = truePart;
    this.falsePart = falsePart;
  }
  
  public IfParts swap() {
    return new IfParts(inCondition, falsePart, truePart);
  }
}
