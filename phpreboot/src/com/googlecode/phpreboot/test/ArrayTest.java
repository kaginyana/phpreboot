package com.googlecode.phpreboot.test;

import com.googlecode.phpreboot.runtime.Array;
import com.googlecode.phpreboot.runtime.Sequence;


public class ArrayTest {
  public static void main(String[] args) {
    Array array = new Array();
    array.__set__("foo", "bar");
    array.__set__("foo2", "bar2");
    array.__set__("foo3", "bar3");
    array.__set__("foo4", "bar4");
    
    System.out.println(array);
    
    array.__remove__("foo2");
    
    System.out.println(array);
    
    Array array2 = new Array();
    array2.__add__(1);
    array2.__add__(2);
    //array2.__set__(4, "foo");
    array2.__add__(3);
    array2.__add__(4);
    
    array.__set__("array2", array2);
    
    System.out.println(array.__json__());
    
    /*
    System.out.println(array2);
    
    Sequence seq = array2.__sequence__();
    for(;seq != null; seq = seq.__next__()) {
      System.out.println("key= "+seq.__key__()+" value= "+seq.__value__());
    }*/
  }
}
