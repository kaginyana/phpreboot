package com.googlecode.phpreboot.runtime;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class JavaBridge {
  public static Iterator<Object> iterator(final Sequence sequence) {
    if (sequence == null)
      return Collections.emptyIterator();
    
    return new Iterator<Object>() {
      private Sequence seq = sequence;
      
      @Override
      public boolean hasNext() {
        return seq != null;
      }
      
      @Override
      public Object next() {
        Object value = seq.getValue();
        seq = seq.next();
        return value;
      }
      
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  public static Iterator<Array.Entry> entryIterator(final Array.Entry sequence) {
    if (sequence == null) {
      return Collections.emptyIterator();
    }
    return new Iterator<Array.Entry >() {
      private Array.Entry seq = sequence;
      
      @Override
      public boolean hasNext() {
        return seq != null;
      }
      
      @Override
      public Array.Entry next() {
        if (seq == null)
          throw new NoSuchElementException();
        
        // no defensive copy here
        Array.Entry entry = seq; 
        seq = seq.next();
        return entry;
      }
      
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
 
  /*
  public static Iterable<Object> iterable(final Sequenceable sequenceable) {
    return new Iterable<Object>() {
      @Override
      public Iterator<Object> iterator() {
        return JavaBridge.iterator(sequenceable.sequence());
      }
    };
  }
  
  public static Map<Object,Object> map(final Array array) {
    return new AbstractMap<Object,Object>() {
      @Override
      public int size() {
        return array.size();
      }
      @Override
      public Set<Map.Entry<Object, Object>> entrySet() {
        return new AbstractSet<Map.Entry<Object,Object>>() {
          @Override
          public int size() {
            return array.size();
          }
          @Override
          public Iterator<Map.Entry<Object, Object>> iterator() {
            final Sequence sequence = array.sequence();
            if (sequence == null)
              return Collections.emptyIterator();
            return new Iterator<Map.Entry<Object, Object>>() {
              private Sequence seq = sequence;
              
              @Override
              public boolean hasNext() {
                return seq != null;
              }
              @Override
              public Map.Entry<Object, Object> next() {
                seq = seq.next();
                return new AbstractMap.SimpleImmutableEntry<Object,Object>(
                    seq.getKey(),
                    seq.getValue());
              }
              @Override
              public void remove() {
                throw new UnsupportedOperationException();
              }
            };
          }
        };
      }
    };
  }
  
  public static List<Object> list(final Array array) {
    if (array.isIndexedArray()) {
      return new AbstractList<Object>() {
        @Override
        public int size() {
          return array.size();
        }
        @Override
        public Object get(int index) {
          if (index<0 || index>=array.size())
            throw new IndexOutOfBoundsException("bad index "+index);
          return array.get(index);
        }
      };
    }
    
    return new AbstractSequentialList<Object>() {
      @Override
      public int size() {
        return array.size();
      }
      @Override
      public ListIterator<Object> listIterator(final int index) {
        Sequence sequence = array.sequence();
        for(int i=index; i>0; i--) {
          sequence = sequence.next();
        }
        final Sequence sequence2 = sequence;
        return new ListIterator<Object>() {
          private Sequence seq = sequence2;
          private int i = index;
          
          @Override
          public boolean hasNext() {
            return seq != null;
          }
          @Override
          public Object next() {
            seq = seq.next();
            i++;
            return seq.getValue();
          }
          @Override
          public int nextIndex() {
            return i;
          }
          @Override
          public boolean hasPrevious() {
            throw new UnsupportedOperationException();
          }
          @Override
          public Object previous() {
            throw new UnsupportedOperationException();
          }
          @Override
          public int previousIndex() {
            throw new UnsupportedOperationException();
          }
          @Override
          public void set(Object e) {
            throw new UnsupportedOperationException();
          }
          @Override
          public void add(Object e) {
            throw new UnsupportedOperationException();
          }
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }
  
  public static Object[] array(Array array) {
    Object[] objects = new Object[array.size()];
    int index = 0;
    for(Sequence seq = array.sequence(); seq != null; seq = seq.next()) {
      objects[index++] = seq.getValue();
    }
    return objects;
  }
  
  */
}
