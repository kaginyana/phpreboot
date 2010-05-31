package com.googlecode.phpreboot.compiler;

import com.googlecode.phpreboot.ast.Node;
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
  private final /*@Nullable*/Node declaringNode;
  
  private final static Object LOCAL_VAR_MARKER = new Object();
  
  private LocalVar(String name, boolean readOnly, Type type, Node declaringNode, Object value, boolean bound, int slot) {
    super(name, readOnly, type, value);
    this.declaringNode = declaringNode;
    this.bound = bound;
    this.slot = slot;
  }
  
  public boolean isConstant() {
    return bound;
  }
  
  public boolean isConstantFoldable() {
    return getType() == null;
  }
 
  public boolean isOptimistic() {
    return declaringNode != null;
  }
  
  public /*@Nullable*/Node getDeclaringNode() {
    return declaringNode;
  }
  
  @Override
  public void setType(Type type) {
    super.setType(type);
  }
  
  @Override
  public String toString() {
    return super.toString() + " declaringNode:"+declaringNode+" bound:"+bound+" slot:"+slot;
  }
  
  public int getSlot(int shift) {
    return shift + slot;
  }
  
  public static LocalVar createConstantFoldable(Object value) {
    return new LocalVar(null, true, null, /*not optimistic*/null, value, true, -1);
  }
  
  public static LocalVar createConstantBound(String name, boolean readOnly, Type type, Node declaringNode, Object value, int slot) {
    return new LocalVar(name, readOnly, type, declaringNode, value, true, slot);
  }
  
  public static LocalVar createLocalVar(String name, boolean readOnly, Type type, Node declaringNode, int slot) {
    return new LocalVar(name, readOnly, type, declaringNode, LOCAL_VAR_MARKER, false, slot);
  }
}
