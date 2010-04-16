package com.googlecode.phpreboot.runtime;

import java.util.ArrayDeque;

public class XML implements Sequenceable {
  private String name;
  private final Array attributes;
  final Array elements;
  
  public XML(String name) {
    this(name, new Array(), new Array());
  }
  
  public XML(String name, Array attributes, Array elements) {
    this.name = name;
    this.attributes = attributes;
    this.elements = elements;
  }
  
  public XML() {
    this("");
  }
  
  public String _name__() {
    return name;
  }
  public void __name__(String name) {
    this.name = name;
  }
  public Array __attributes__() {
    return attributes;
  }
  public Array __elements__() {
    return elements;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('<').append(name);
    for(Sequence seq = attributes.__entries__(); seq != null; seq = seq.__next__()) {
      builder.append(' ').append(seq.__key__()).append("=\"").append(seq.__value__()).append('\"');
    }
    Sequence seq = elements.__entries__();
    if (seq == null) {
      builder.append("\\>");
      return builder.toString();
    }
    builder.append('>');
    for(; seq != null; seq = seq.__next__()) {
      builder.append(seq.__value__());
    }
    builder.append("</").append(name).append(">");
    return builder.toString();
  }
  
  @Override
  public Sequence __entries__() {
    return elements.__entries__();
  }
  
  @Override
  public Sequence __sequence__() {
    if (elements.__size__() == 0)
      return null;
    
    return new Sequence() {
      private final ArrayDeque<Sequence> stack = new ArrayDeque<Sequence>(); 
      private Sequence current = elements.__sequence__();
      
      @Override
      public Object __value__() {
        return current.__value__();
      }
      
      @Override
      public Object __key__() {
        return current.__key__();
      }
      
      @Override
      public Sequence __next__() {
        Sequence current = this.current;
        Object value = current.__value__();
        if (value instanceof Sequenceable) {
          Sequence seq = ((Sequenceable)value).__entries__();
          if (seq != null) {
            System.out.println("push "+current);
            
            stack.push(current);
            this.current = seq;
            return this;
          }
        }
        
        while ((current = current.__next__()) == null) {
          if (stack.isEmpty())
            return null;
          current = stack.pop();
        }
        
        this.current = current;
        return this;
      }
    };
  }
}
