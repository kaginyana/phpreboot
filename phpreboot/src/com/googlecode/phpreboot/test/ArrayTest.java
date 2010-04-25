package com.googlecode.phpreboot.test;

import com.googlecode.phpreboot.runtime.Array;


public class ArrayTest {
  public static void main(String[] args) {
    Array one = new Array();
    one.set("foo", "bar");
    System.out.println(one.sequence());
    
    
    Array array = new Array();
    array.set("foo", "bar");
    array.set("foo2", "bar2");
    array.set("foo3", "bar3");
    array.set("foo4", "bar4");
    
    System.out.println(array);
    
    array.remove("foo2");
    
    System.out.println(array);
    
    Array array2 = new Array();
    array2.add(1);
    array2.add(2);
    //array2.__set__(4, "foo");
    array2.add(3);
    array2.add(4);
    
    array.set("array2", array2);
    
    System.out.println(array/*.__json__()*/);
    
    /*
    System.out.println(array2);
    
    Sequence seq = array2.__sequence__();
    for(;seq != null; seq = seq.__next__()) {
      System.out.println("key= "+seq.__key__()+" value= "+seq.__value__());
    }*/
  }
}
