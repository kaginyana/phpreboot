package com.googlecode.phpreboot.uri;

import java.nio.file.Path;
import java.nio.file.Paths;

abstract class PathBuilder {
  public abstract PathBuilder append(String pathStep);
  
  public abstract Object toPath();
  
  public abstract PathBuilder trailingSlash();
  
  /*
  public PathBuilder appendPathLiteral(String pathLiteral) {
    String[] path = pathLiteral.split("/");
    append(path[0]);
    String path_1 = path[1];
    if (!path_1.isEmpty())
      append(path_1);
    return this;
  }*/
  
  static class FilePathBuilder extends PathBuilder {
    private Path path;
    
    FilePathBuilder(String firstStep) {
      this(Paths.get(firstStep));
    }
    
    FilePathBuilder(Path path) {
      this.path = path;
    }
    
    @Override
    public PathBuilder append(String pathStep) {
      path = path.resolve(pathStep);
      return this;
    }
    
    @Override
    public PathBuilder trailingSlash() {
      return this;
    }
    
    @Override
    public Path toPath() {
      return path;
    }
  }
  
  static class StringPathBuilder extends PathBuilder {
    private final StringBuilder path;
    
    StringPathBuilder(String firstStep) {
      StringBuilder path = new StringBuilder();
      this.path = path.append(firstStep);
    }
    
    @Override
    public PathBuilder append(String pathStep) {
      path.append('/').append(pathStep);
      return this;
    }
    
    @Override
    public PathBuilder trailingSlash() {
      path.append('/');
      return this;
    }
    
    @Override
    public String toPath() {
      return path.toString();
    }
  }
}
