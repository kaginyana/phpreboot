package com.googlecode.phpreboot.runtime;



public class RT {
  public static RuntimeException error(String format, Object arg) {
    return error(format, new Object[]{arg});
  }
  public static RuntimeException error(String format, Object arg, Object arg2) {
    return error(format, new Object[]{arg, arg2});
  }
  public static RuntimeException error(String format, Object... args) {
    return new RuntimeException(String.format(format, args));
  }
  
  public static Object unary_plus(Object value) {
    if (value instanceof Integer) {
      return unary_plus((int)(Integer)value);
    }
    if (value instanceof Double) {
      return unary_plus((double)(Double)value);
    }
    throw error("invalid value for operation + (unary plus) %s", value);
  }
  public static int unary_plus(int value) {
    return value;
  }
  public static double unary_plus(double value) {
    return value;
  }
  
  public static Object unary_minus(Object value) {
    if (value instanceof Integer) {
      return unary_minus((int)(Integer)value);
    }
    if (value instanceof Double) {
      return unary_minus((double)(Double)value);
    }
    throw error("invalid value for operation - (unary minus) %s",value);
  }
  public static int unary_minus(int value) {
    return -value;
  }
  public static double unary_minus(double value) {
    return -value;
  }
  
  public static boolean unary_not(Object value) {
    if (value instanceof Boolean) {
      return unary_not((boolean)(Boolean)value);
    }
    throw error("invalid value for operation ! (unary not) %s",value);
  }
  public static boolean unary_not(boolean value) {
    return !value;
  }

  public static Object plus(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return plus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return plus(l, (double)(Double)right);
      }
      if (right instanceof String) {
        return l + (String)right;
      }
      throw error("invalid value for operation + "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return plus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return plus(l, (double)(Double)right);
      }
      if (right instanceof String) {
        return l + (String)right;
      }
      throw error("invalid value for operation + "+right);
    }
    if (left instanceof String) {
      return ((String)left) + right;
    }
    if (right instanceof String) {
      return left + (String)right;
    }
    
    throw error("invalid value for operation + "+left+" "+right);
  }
  public static int plus(int left, int right) {
    return left + right;
  }
  public static double plus(double left, double right) {
    return left + right;
  }
  
  public static Object minus(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return minus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return minus(l, (double)(Double)right);
      }
      throw error("invalid value for operation - "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return minus(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return minus(l, (double)(Double)right);
      }
      throw error("invalid value for operation - "+right);
    }
    throw error("invalid value for operation - "+left);
  }
  public static int minus(int left, int right) {
    return left - right;
  }
  public static double minus(double left, double right) {
    return left - right;
  }
  
  public static Object mult(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return mult(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mult(l, (double)(Double)right);
      }
      throw error("invalid value for operation * "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return mult(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return mult(l, (double)(Double)right);
      }
      throw error("invalid value for operation * "+right);
    }
    throw error("invalid value for operation * "+left);
  }
  public static int mult(int left, int right) {
    return left * right;
  }
  public static double mult(double left, double right) {
    return left * right;
  }
  
  public static Object div(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return div(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return div(l, (double)(Double)right);
      }
      throw error("invalid value for operation / "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return div(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return div(l, (double)(Double)right);
      }
      throw error("invalid value for operation / "+right);
    }
    throw error("invalid value for operation / "+left);
  }
  public static int div(int left, int right) {
    return left / right;
  }
  public static double div(double left, double right) {
    return left / right;
  }
  
  public static boolean eq(Object left, Object right) {
    return (left == null)? right == null: left.equals(right);
  }
  
  public static boolean ne(Object left, Object right) {
    return (left == null)? right != null: !left.equals(right);
  }
  
  public static boolean lt(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return lt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return lt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return lt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return lt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean lt(int left, int right) {
    return left < right;
  }
  public static boolean lt(double left, double right) {
    return left < right;
  }
  
  public static boolean le(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return le(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return le(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return le(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return le(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean le(int left, int right) {
    return left <= right;
  }
  public static boolean le(double left, double right) {
    return left <= right;
  }
  
  public static boolean gt(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return gt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return gt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return gt(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return gt(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean gt(int left, int right) {
    return left > right;
  }
  public static boolean gt(double left, double right) {
    return left > right;
  }
  
  public static boolean ge(Object left, Object right) {
    if (left instanceof Integer) {
      int l = (Integer)left;
      if (right instanceof Integer) {
        return ge(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return ge(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    if (left instanceof Double) {
      double l = (Double)left;
      if (right instanceof Integer) {
        return ge(l, (int)(Integer)right);
      }
      if (right instanceof Double) {
        return ge(l, (double)(Double)right);
      }
      throw error("invalid type "+right);
    }
    throw error("invalid type "+left);
  }
  public static boolean ge(int left, int right) {
    return left >= right;
  }
  public static boolean ge(double left, double right) {
    return left >= right;
  }
  
  static StringBuilder append(StringBuilder builder, Object o) {
    if (o instanceof String) {
      return builder.append('\"').append(o).append('\"');
    }
    return builder.append(o);
  }
}
