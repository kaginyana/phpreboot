package com.googlecode.phpreboot.runtime;

import java.io.Closeable;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.googlecode.phpreboot.ast.Node;

public class XMLCursor implements Sequence, ArrayAccess, Closeable {
  private static final boolean[] ALLOWED_EVENTS;
  static {
    boolean[] events = new boolean[255];
    events[XMLStreamConstants.START_ELEMENT] = true;
    events[XMLStreamConstants.END_ELEMENT] = true;
    events[XMLStreamConstants.CHARACTERS] = true;
    ALLOWED_EVENTS = events;
  }
  
  private static final String XML_KIND_NODE = "node";
  private static final String XML_KIND_TEXT = "text";
  
  private final XMLEventReader eventReader;
  private final Array stack = new Array();
  private String key;
  private Object value;
  
  public XMLCursor(XMLEventReader eventReader) {
    this.eventReader = eventReader;
    StartElement root = nextXMLEvent(eventReader).asStartElement();
    value = startElementAsXML(root);
    key = XML_KIND_NODE;
  }
  
  private static XMLEvent nextXMLEvent(XMLEventReader eventReader) {
    XMLEvent event;
    do {
      if (!eventReader.hasNext())
        return null;
      try {
        event = eventReader.nextEvent();
      } catch (XMLStreamException e) {
        throw RT.error((Node)null, e);
      }
    } while (!ALLOWED_EVENTS[event.getEventType()]);
    return event;
  }
  
  
  private static XML startElementAsXML(StartElement element) {
    XML xml = new XML(element.getName().getLocalPart());
    Array attributes = xml.attributes();
    @SuppressWarnings("unchecked") Iterator<Attribute> it = element.getAttributes();
    while(it.hasNext()) {
      Attribute attribute = it.next();
      attributes.set(attribute.getName(), attribute.getValue());
    }
    return xml;
  }
  
  @Override
  public XMLCursor next() {
    if (key == XML_KIND_NODE) {
      stack.add(value);
    }
    for(;;) {
      XMLEvent xmlEvent = nextXMLEvent(eventReader);
      if (xmlEvent == null)
        return null;
      switch(xmlEvent.getEventType()) {
      case XMLStreamConstants.START_ELEMENT:
        value = startElementAsXML(xmlEvent.asStartElement());
        key = XML_KIND_NODE;
        return this;
        
      case XMLStreamConstants.END_ELEMENT:
        stack.pop();
        if (stack.isEmpty()) {
          close();
          return null;
        }
        break;
        
      case XMLStreamConstants.CHARACTERS:
        value = xmlEvent.asCharacters().getData();
        key = XML_KIND_TEXT;
        return this;
        
      default:
        throw new AssertionError("invalid event type "+xmlEvent.getEventType());
      }
    }
  }
  
  @Override
  public Object getKey() {
    return key;
  }
  
  @Override
  public Object getValue() {
    return value;
  }
  
  @Override
  public Object get(Object key) {
    if (key != XML_KIND_NODE)
      return null;
    Array attributes = ((XML)value).attributes;
    if (attributes == null)
      return INVALID_KEY;
    return attributes.get(key);
  }
  
  public Array getStack() {
    return stack;
  }
  public XML getParent() {
    return (stack.isEmpty())?null:(XML)stack.peek().getValue();
  }
  
  @Override
  public void close() {
    try {
      eventReader.close();
    } catch (XMLStreamException e) {
      Throwable cause = e.getCause();
      if (cause == null) {
        cause = e;
      }
      throw RT.error((Node)null, cause);
    }
  }
}
