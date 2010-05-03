package com.googlecode.phpreboot.test;

import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.XML;
import com.googlecode.phpreboot.runtime.XMLCursor;

public class XMLCursorTest {
  public static void main(String[] args) throws XMLStreamException {
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLEventReader eventReader = inputFactory.createXMLEventReader(
        new StringReader("<foo>Test<bar attr=\"3\"/><baz>Test2</baz></foo>"));
    
    XML root = null;
    
    Sequence cursor = new XMLCursor(eventReader);
    while(cursor != null) {
      System.out.println("key: "+cursor.getKey());
      Object value = cursor.getValue();
      System.out.println("value: "+value);
      
      XML parent = ((XMLCursor)cursor).getParent();
      System.out.println("parent: "+parent);
      
      if (parent == null) {
        root = (XML)value;
      } else {
        parent.elements().add(value);
      }
      
      cursor = cursor.next();
    }
    
    System.out.println("XML "+root);
  }
}
