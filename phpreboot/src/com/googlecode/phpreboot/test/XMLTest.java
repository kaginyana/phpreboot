package com.googlecode.phpreboot.test;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.Sequence;
import com.googlecode.phpreboot.runtime.XML;

public class XMLTest {
  public static void main(String[] args) {
    XML root = new XML("root");
    Array attributes = root.attributes();
    attributes.set("foo", "bar");
    attributes.set("foo2", 2);
    
    XML group = new XML("group");
    root.elements().add(group);
    
    XML user1 = new XML("user");
    user1.elements().add("user1");
    group.elements().add(user1);
    
    XML user2 = new XML("user");
    user2.elements().add("user2");
    group.elements().add(user2);
    
    XML description = new XML("description");
    user2.elements().add(description);
    
    
    Sequence seq = RT.toSequence(root);
    for(;seq != null; seq = seq.next()) {
      System.out.println("key= "+seq.getKey()+" value= "+seq.getValue());
    }
  }
}
