package com.googlecode.phpreboot.test;

import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;

import com.googlecode.phpreboot.runtime.XML;
import com.googlecode.phpreboot.runtime.XPathNavigator;

public class XMLPathTest {
  public static void main(String[] args) throws JaxenException {
    XML root = new XML("root");
    XML foo = new XML("foo");
    root.elements().add(foo);
    XML bar1 = new XML("bar");
    foo.elements().add(bar1);
    XML bar2 = new XML("bar");
    foo.elements().add(bar2);
    XML barz = new XML("baz");
    bar1.elements().add(barz);
    
    System.out.println(root);
    BaseXPath xpath = new BaseXPath("*/bar", new XPathNavigator());
    System.out.println(xpath.selectNodes(root));
  }
}
