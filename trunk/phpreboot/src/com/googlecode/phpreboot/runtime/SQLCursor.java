package com.googlecode.phpreboot.runtime;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class SQLCursor implements Sequence, ArrayAccess {
  private final ResultSet resultSet;
  private int row = 1;

  public SQLCursor(ResultSet resultSet) {
    this.resultSet = resultSet;
  }
  
  @Override
  public Sequence __next__() {
    try {
      row++;
      if (resultSet.next())
        return this;
      resultSet.getStatement().close();
      return null;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public Object __key__() {
    /*
    try {
      return resultSet.getRow();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }*/
    return row;
  }
  
  @Override
  public Object __value__() {
    return this;
  }
  
  @Override
  public Object __get__(Object key) {
    try {
      if (key instanceof String) {
        return resultSet.getObject((String)key);
      }
      if (key instanceof Integer) {
        return resultSet.getObject((Integer)key);
      }
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
    throw RT.error("invalid column name/index "+key);
  }
  
  @Override
  public Object __get__(int index) {
    try {
      return resultSet.getObject(index);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append('{');
    int columnCount;
    try {
      ResultSetMetaData metaData = resultSet.getMetaData();
      columnCount = metaData.getColumnCount();
      for(int i=1; i<=columnCount; i++) {
        builder.append('"').append(metaData.getColumnName(i)).append("\": ");
        RT.append(builder, resultSet.getObject(i));
        builder.append(", ");
      }
      
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    if (columnCount != 0) {
      builder.setLength(builder.length() - 2);  
    }
    return builder.append('}').toString();
  }
}
