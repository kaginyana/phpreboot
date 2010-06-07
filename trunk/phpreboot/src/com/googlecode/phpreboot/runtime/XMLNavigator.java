package com.googlecode.phpreboot.runtime;

import java.util.Collections;
import java.util.Iterator;

import org.jaxen.BaseXPath;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenException;
import org.jaxen.XPath;


@SuppressWarnings("serial")
public class XMLNavigator extends DefaultNavigator {
  @Override
  public boolean isDocument(Object node) {
    return false;
  }
  @Override
  public boolean isComment(Object node) {
    return false;
  }
  @Override
  public boolean isNamespace(Object node) {
    return false;
  }
  @Override
  public boolean isProcessingInstruction(Object node) {
    return false;
  }
  
  @Override
  public boolean isAttribute(Object node) {
    return node instanceof Array.Entry;
  }
  @Override
  public boolean isElement(Object node) {
    return node instanceof XML;
  }
  @Override
  public boolean isText(Object node) {
    return node instanceof String;
  }
  
  @Override
  public String getAttributeName(Object node) {
    return ((Array.Entry)node).key.toString();  //nullcheck
  }
  @Override
  public String getAttributeNamespaceUri(Object node) {
    return "";
  }
  @Override
  public String getAttributeQName(Object node) {
    return getAttributeName(node);
  }
  @Override
  public String getAttributeStringValue(Object node) {
    return ((Array.Entry)node).value.toString();   //nullcheck
  }

  @Override
  public String getCommentStringValue(Object node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getElementName(Object node) {
    return ((XML)node).getName();
  }
  @Override
  public String getElementNamespaceUri(Object node) {
    return "";
  }
  @Override
  public String getElementQName(Object node) {
    return getElementName(node);
  }
  
  @Override
  public String getElementStringValue(Object node) {
    return getElementName(node);
  }

  @Override
  public String getNamespacePrefix(Object node) {
    throw new UnsupportedOperationException();
  }
  @Override
  public String getNamespaceStringValue(Object node) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTextStringValue(Object node) {
    return node.toString();
  }

  @Override
  public XPath parseXPath(String xpathExpr) throws JaxenException {
    //System.err.println("xpath expression "+xpathExpr);
    return new BaseXPath(xpathExpr, this);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Iterator getChildAxisIterator(Object node) {
    if (node instanceof XML) {
      Array elements = ((XML)node).elements;
      if (elements == null)
        return Collections.emptyIterator();
      return JavaBridge.iterator(elements.sequence());  
    }
    return Collections.emptyIterator();
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Iterator getParentAxisIterator(Object node) {
    //FIXME currently XML node doesn't store parent
    return Collections.emptyIterator();
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Iterator getAttributeAxisIterator(Object node) {
    if (node instanceof XML) {
      Array attributes = ((XML)node).attributes;
      if (attributes == null)
        return Collections.emptyIterator();
      return JavaBridge.entryIterator(attributes.sequence());  
    }
    return Collections.emptyIterator();
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Iterator getNamespaceAxisIterator(Object node) {
    return Collections.emptyIterator();
  }
}
