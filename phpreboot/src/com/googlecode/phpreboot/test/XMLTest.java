package com.googlecode.phpreboot.test;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.XML;

public class XMLTest {
  public static void main(String[] args) {
    XML root = new XML("root");
    Array attributes = root.__attributes__();
    attributes.__set__("foo", "bar");
    attributes.__set__("foo2", 2);
    
    XML group = new XML("group");
    root.__elements__().__add__(group);
    
    XML user1 = new XML("user");
    user1.__elements__().__add__("user1");
    group.__elements__().__add__(user1);
    
    XML user2 = new XML("user");
    user2.__elements__().__add__("user2");
    group.__elements__().__add__(user2);
    
    XML description = new XML("description");
    user2.__elements__().__add__(description);
    
    
    Sequence seq = root.__sequence__();
    for(;seq != null; seq = seq.__next__()) {
      System.out.println("key= "+seq.__key__()+" value= "+seq.__value__());
    }
  }
}
