package com.googlecode.phpreboot.compiler;

class IfParts {
  final boolean inCondition;
  final /*@Nullable*/GeneratorClosure truePart;
  final /*@Nullable*/GeneratorClosure falsePart;
  
  IfParts(boolean inCondition, /*@Nullable*/GeneratorClosure truePart, /*@Nullable*/GeneratorClosure falsePart) {
    this.inCondition = inCondition;
    this.truePart = truePart;
    this.falsePart = falsePart;
  }
  
  public IfParts swap() {
    return new IfParts(inCondition, falsePart, truePart);
  }
}
