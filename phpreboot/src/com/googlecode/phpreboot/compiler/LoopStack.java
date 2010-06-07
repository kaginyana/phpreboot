package com.googlecode.phpreboot.compiler;

import java.util.ArrayDeque;
import java.util.HashMap;

import org.objectweb.asm.Label;

class LoopStack<E> {
  static class Labels {
    final Label breakLabel;
    final Label continueLabel;
    
    Labels(Label breakLabel, Label continueLabel) {
      this.breakLabel = breakLabel;
      this.continueLabel = continueLabel;
    }
  }
  
  private final ArrayDeque<Entry<E>> stack =
    new ArrayDeque<Entry<E>>(8);
  private final HashMap<String,E> labelMap =
    new HashMap<String, E>(16);
  
  private static class Entry<E> {
    final /*@Nullable*/String label;
    final E element;
    
    Entry(/*@Nullable*/String label, E element) {
      this.label = label;
      this.element = element;
    }
  }
  
  public void push(/*@Nullable*/String label, E element) {
    stack.push(new Entry<E>(label, element));
    if (label != null) {
      labelMap.put(label, element);
    }
  }
  
  public void pop() {
    Entry<E> entry = stack.pop();
    /*@Nullable*/String label = entry.label;
    if (label != null)
      labelMap.remove(label);
  }
  
  public /*@Nullable*/E current() {
    Entry<E> entry = stack.peek();
    if (entry == null)
      return null;
    return entry.element;
  }
  public /*@Nullable*/E lookup(String label) {
    return labelMap.get(label);
  }
}
