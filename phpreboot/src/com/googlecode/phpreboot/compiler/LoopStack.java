package com.googlecode.phpreboot.compiler;

import java.util.ArrayDeque;
import java.util.HashMap;

import com.googlecode.phpreboot.ast.Node;

public class LoopStack {
  private final ArrayDeque<Node> stack =
    new ArrayDeque<Node>();
  private final HashMap<String,Node> labelMap =
    new HashMap<String, Node>();
  
  public void push(/*@Nullable*/String label, Node node) {
    stack.push(node);
    if (label != null) {
      labelMap.put(label, node);
    }
  }
  
  public void pop() {
    Node node = stack.pop();
    labelMap.remove(node);  // may be there is no label to remove
  }
}
