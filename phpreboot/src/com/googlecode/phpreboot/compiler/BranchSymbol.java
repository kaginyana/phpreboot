package com.googlecode.phpreboot.compiler;

import java.util.Collection;

/** Record if branch of the an instruction 'if' (and while) must be generated or not.
 */
class BranchSymbol implements Symbol {
  boolean leftPartActivated;
  boolean rightPartActivated;
  
  LocalVar escapeFunctionVar;
  Collection<LocalVar> localVarsToRestore;
  
  BranchSymbol() {
    // activated by default, if an untaken branch break an optimistic assertion,
    // it will be deactivated at that time
    leftPartActivated = true;
    rightPartActivated = true;
  }
}
