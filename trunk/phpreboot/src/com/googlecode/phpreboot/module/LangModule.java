package com.googlecode.phpreboot.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.runtime.XML;

public class LangModule extends Module {
  @Export
  public static XML document(URI uri) {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    InputStream input;
    try {
      input = uri.getInputStream();
    } catch (IOException e) {
      throw RT.error((Node)null, e);
    }
    
    XML root = null;
    try {
      XMLEventReader eventReader = factory.createXMLEventReader(input);
      
      ArrayDeque<XML> stack = new ArrayDeque<XML>();
      while(eventReader.hasNext()) {
        XMLEvent event = eventReader.nextEvent();
        switch (event.getEventType()) {
        case XMLStreamConstants.START_ELEMENT:
          StartElement startElement = event.asStartElement();
          XML xml = new XML(startElement.getName().getLocalPart());
          if (!stack.isEmpty()) {
            stack.peek().elements().add(xml);
          } else {
            root = xml;
          }
          stack.push(xml);
          break;
          
        case XMLStreamConstants.END_ELEMENT:
          stack.pop();
          break;
          
        case XMLStreamConstants.CHARACTERS:
          Characters characters = event.asCharacters();
          stack.peek().elements().add(characters.getData());
          break;
          
        default:
          continue;
        }
      }
      
    } catch (XMLStreamException e) {
      throw RT.error((Node)null, e);
    } finally {
      try {
        input.close();
      } catch (IOException e) {
        // do nothing
      }
    }
    
    return root;
  }
}
