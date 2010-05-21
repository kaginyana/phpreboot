package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;

/* 3 kinds of LocalVar:
 *   - a traditional local variable 
 *   - a constant foldable value
 *   - a constant value which is not foldable and that will be bound
 */
public class LocalVar extends Var implements Symbol {
  private final int slot;
  private final boolean bound;
  
  private final static Object LOCAL_VAR_MARKER = new Object();
  
  private LocalVar(String name, boolean readOnly, Type type, Object value, boolean bound, int slot) {
    super(name, readOnly, type, value);
    this.bound = bound;
    this.slot = slot;
  }
  
  public LocalVar(String name, boolean readOnly, Type type, int slot) {
    this(name, readOnly, type, LOCAL_VAR_MARKER, false, slot);
  }
  
  public boolean isConstant() {
    return bound;
  }
  
  public boolean isConstantFoldable() {
    return getType() == null;
  }
 
  public int getSlot(int shift) {
    return shift + slot;
  }
  
  public static LocalVar createConstantFoldable(Object value) {
    return new LocalVar(null, true, null, value, true, -1);
  }
  
  public static LocalVar createConstantBound(String name, boolean readOnly, Object value, Type type, int slot) {
    return new LocalVar(name, readOnly, type, value, true, slot);
  }
}
