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
  
  /** Gets the name of the XML markup
   * @return the name of the current XML markup
   */
  public String getName() {
    return name;
  }
  
  /** Change the name of the XML markup
   * @param name the new name of the current XML markup
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /** Returns the attributs of the XML markups
   * @return the attributs of the XML markups as an array
   */
  public Array attributes() {
    return attributes;
  }
  
  /** Returns the elements of the XML markups.
   *  Elements are texts (string) and sub-elements (XML)
   * @return the elements of the current XML markups.
   */
  public Array elements() {
    return elements;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('<').append(name);
    for(Sequence seq = attributes.entries(); seq != null; seq = seq.next()) {
      builder.append(' ').append(seq.getKey()).append("=\"").append(seq.getValue()).append('\"');
    }
    Sequence seq = elements.entries();
    if (seq == null) {
      builder.append("/>");
      return builder.toString();
    }
    builder.append('>');
    for(; seq != null; seq = seq.next()) {
      builder.append(seq.getValue());
    }
    builder.append("</").append(name).append(">");
    return builder.toString();
  }
  
  @Override
  public Sequence entries() {
    return elements.entries();
  }
  
  @Override
  public Sequence sequence() {
    if (elements.size() == 0)
      return null;
    
    return new Sequence() {
      private final ArrayDeque<Sequence> stack = new ArrayDeque<Sequence>(); 
      private Sequence current = elements.sequence();
      
      @Override
      public Object getValue() {
        return current.getValue();
      }
      
      @Override
      public Object getKey() {
        return current.getKey();
      }
      
      @Override
      public Sequence next() {
        Sequence current = this.current;
        Object value = current.getValue();
        if (value instanceof Sequenceable) {
          Sequence seq = ((Sequenceable)value).entries();
          if (seq != null) {
            System.out.println("push "+current);
            
            stack.push(current);
            this.current = seq;
            return this;
          }
        }
        
        while ((current = current.next()) == null) {
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
