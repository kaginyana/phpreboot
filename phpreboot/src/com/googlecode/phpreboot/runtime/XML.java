package com.googlecode.phpreboot.runtime;

import java.util.ArrayDeque;

/* TODO add a parent
 */
public class XML implements Sequenceable {
  private String name;
  /*lazy*/ Array attributes;
  /*lazy*/ Array elements;
  
  public XML(String name) {
    this(name, null, null);
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
    name.getClass();  //nullcheck
    this.name = name;
  }
  
  /** Returns the attributs of the XML markups
   * @return the attributs of the XML markups as an array
   */
  public Array attributes() {
    if (attributes == null)
      return attributes = new Array();
    return attributes;
  }
  
  /** Returns the elements of the XML markups.
   *  Elements are texts (string) and sub-elements (XML)
   * @return the elements of the current XML markups.
   */
  public Array elements() {
    if (elements == null)
      return elements = new Array();
    return elements;
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('<').append(name);
    for(Sequence seq = (attributes == null)?null:attributes.entries(); seq != null; seq = seq.next()) {
      builder.append(' ').append(seq.getKey()).append("=\"").append(seq.getValue()).append('\"');
    }
    Sequence seq = (elements==null)?null:elements.entries();
    if (seq == null) {
      builder.append("/>");
      return builder.toString();
    }
    builder.append('>');
    for(; seq != null; seq = seq.next()) {
      RT.escapeXML(builder, seq.getValue());
    }
    builder.append("</").append(name).append(">");
    return builder.toString();
  }
  
  @Override
  public Sequence entries() {
    if (elements == null)
      return (elements = new Array()).entries();
    return elements.entries();
  }
  
  @Override
  public Sequence sequence() {
    if (elements == null || elements.isEmpty())
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
