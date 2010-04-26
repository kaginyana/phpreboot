package com.googlecode.phpreboot.runtime;

public class Array implements Sequenceable, ArrayAccess {  
  public static class Entry implements Sequence {
    /*final*/ Object key;
    Object value;
    Entry next;
    Entry before;
    Entry after;
    Entry header;

    public Entry(Object key, Object value) {
      key.getClass();
      value.getClass();
      this.key = key;
      this.value = value;
    }
    
    Entry(Object key, Object value, Entry next, Entry header) {
      this.key = key;
      this.value = value;
      this.next = next;
      this.header = header;
    }

    @Override
    public Sequence next() {
      Entry after = this.after;
      return (after == header) ? null: after;
    }

    @Override
    public Object getKey() {
      return key;
    }

    @Override
    public Object getValue() {
      return value;
    }
  }

  private Entry[] table;
  private Entry header;
  private int threshold;
  private int size;
  private int nextIndex;

  public Array() {
    table = new Entry[16];
    threshold = 12;
    header = new Entry(null, null, null, null);
    header.before = header.after = header;
  }

  public int size() {
    return size;
  }
  
  public boolean isEmpty() {
    return size == 0;
  }

  private static int hashIndex(Object key, int length) {
    return key.hashCode() & (length-1);
  }
  
  @Override
  public Object get(Object key) {
    Entry[] table = this.table;
    for (Entry e = table[hashIndex(key, table.length)];
         e != null;
         e = e.next) {
      if (e.key.equals(key)) {
        return e.value;
      }
    }
    return null;
  }

  @Override
  public Object get(int key) {
    return get((Integer)key);
  }
  
  public void set(Object key, Object value) {
    value.getClass();
    Entry[] table = this.table;
    int i = hashIndex(key, table.length);
    for (Entry e = table[i]; e != null; e = e.next) {
      if (e.key.equals(key)) {
        e.value = value;
        return;
      }
    }
    
    Entry e = new Entry(key, value, table[i], header);
    e.after  = header;
    e.before = header.before;
    e.before.after = e; 
    e.after.before = e;
    table[i] = e;
    
    if (key instanceof Integer) {
      nextIndex = 1 + (Integer)key;
    }
    
    if (size++ >= threshold)
      resize(table);
  }
  
  Entry getEntry(Object key) {
    Entry[] table = this.table;
    int i = hashIndex(key, table.length);
    for (Entry e = table[i]; e != null; e = e.next) {
      if (e.key.equals(key)) {
        return e;
      }
    }
    
    Entry e = new Entry(key, null, table[i], header);
    e.after  = header;
    e.before = header.before;
    e.before.after = e; 
    e.after.before = e;
    table[i] = e;
    
    if (key instanceof Integer) {
      nextIndex = 1 + (Integer)key;
    }
    
    if (size++ >= threshold)
      resize(table);
    
    return e;
  }
  
  public void __set__(Entry entry) {
    if (entry.next != null)
      throw entryAlreadyStoredInAnotherArray();
      
    Object key = entry.key;
    Entry[] table = this.table;
    int i = hashIndex(key, table.length);
    for (Entry e = table[i]; e != null; e = e.next) {
      if (e.key.equals(key)) {
        e.value = entry.value;
        return;
      }
    }
    
    entry.next = table[i];
    entry.after = entry.header = header;
    entry.before = header.before;
    entry.before.after = entry; 
    entry.after.before = entry;
    table[i] = entry;
    
    if (key instanceof Integer) {
      nextIndex = 1 + (Integer)key;
    }
    
    if (size++ >= threshold)
      resize(table);
  }
  
  private static IllegalArgumentException entryAlreadyStoredInAnotherArray() {
    return new IllegalArgumentException("entry already stored in another array");
  }
  
  /*
  public void set(int key, Object value) {
    set((Object)key, value);
  }*/
  
  public void add(Object value) {
    set(nextIndex, value);
  }

  private void resize(Entry[] table) {
    int newCapacity = table.length << 1;
    Entry[] newTable = new Entry[newCapacity];
    
    Entry header = this.header;
    for (Entry e = header.after; e != header; e = e.after) {
        int i = hashIndex(e.key, newCapacity);
        e.next = newTable[i];
        newTable[i] = e;
    }
    table = newTable;
    threshold = newCapacity + newCapacity >> 1;
  }

  public void remove(Object key) {
    Entry[] table = this.table;
    int i = hashIndex(key, table.length);
    Entry prev = table[i];
    Entry e = prev;

    while (e != null) {
      Entry next = e.next;
      if (e.key.equals(key)) {
        size--;
        if (prev == e)
          table[i] = next;
        else
          prev.next = next;
        
        e.before.after = e.after;
        e.after.before = e.before;
        return;
      }
      prev = e;
      e = next;
    }

    return ;
  }
  
  /*
  public void remove(int key) {
    remove((Integer)key);
  }*/

  /*public void clear() {
    table = new Entry[16];
    size = 0;
    threshold = 12;
    header.before = header.after = header;
  }*/

  /*@Override
  public String toString() {
    if (size == 0)
      return "[]";
    
    Entry header = this.header;
    StringBuilder builder = new StringBuilder().append('[');
    
    // optimistic, try to print as an array, we may revert back
    if (size == nextIndex) {
      int index = 0;
      for (Entry e = header.after; e != header; e = e.after) {
        Object key = e.key;
        if (!(key instanceof Integer) || ((Integer)key != index++)) {
          builder.setLength(1);
          break; // rollback
        }
        RT.append(builder, e.value).append(", ");
      }
    }
    
    // print as a map
    if (builder.length() == 1) {
      for (Entry e = header.after; e != header; e = e.after) {
        RT.append(RT.append(builder, e.key).append(": "), e.value).append(", ");
      }
    }
    
    builder.setLength(builder.length() - 2);
    return builder.append(']').toString();
  }*/
  
  /*public String __json__() {*/
  @Override
  public String toString() {
    if (size == 0)
      return "[]";
    
    Entry header = this.header;
    StringBuilder builder = new StringBuilder(); 
    
    // optimistic, try to print as an array, we may revert back
    if (size == nextIndex) {
      builder.append('[');
      int index = 0;
      for (Entry e = header.after; e != header; e = e.after) {
        Object key = e.key;
        if (!(key instanceof Integer) || ((Integer)key != index++)) {
          builder.setLength(0);
          break; // rollback
        }
        Object value = e.value;
        if (value instanceof Array) {
          builder.append((/*(Array)*/value)/*.__json__()*/);
        } else {
          RT.append(builder, value);
        }
        builder.append(", ");
      }
      
      if (builder.length() != 0) {
        builder.setLength(builder.length() - 2);
        return builder.append(']').toString();
      }
    }
    
    // print as a map
    builder.append('{');
    for (Entry e = header.after; e != header; e = e.after) {
      Object value = e.value;
      builder.append('\"').append(e.key).append("\": ");
      if (value instanceof Array) {
        builder.append((/*(Array)*/value)/*.__json__()*/);
      } else {
        RT.append(builder, value);    
      }
      builder.append(", ");
    }
    
    builder.setLength(builder.length() - 2);
    return builder.append('}').toString();
  }
  
  @Override
  public Sequence entries() {
    return sequence();
  }
  
  @Override
  public Sequence sequence() {
    if (size == 0)
      return null;
    return header.after;
  }
}
