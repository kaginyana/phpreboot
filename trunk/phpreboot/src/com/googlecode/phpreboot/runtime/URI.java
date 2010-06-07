package com.googlecode.phpreboot.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.googlecode.phpreboot.ast.Node;

public class URI {
  // dual representation
  private final java.net.URI uri;
  private final Path path;
  
  private URI(java.net.URI uri, Path path) {
    this.uri = uri;
    this.path = path;
  }
  
  public URI(java.net.URI uri) {
    this(uri, null);
  }
  
  public URI(Path path) {
    this(null, path);
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof URI))
      return false;
    URI otherURI = (URI)o;
    if (uri != null)
      return uri.equals(otherURI.uri);
    return path.equals(otherURI.path);
  }
  
  @Override
  public int hashCode() {
    if (uri != null)
      return uri.hashCode();
    return path.hashCode();
  }
  
  @Override
  public String toString() {
    if (uri != null)
      return uri.toString();
    return path.toString();
  }
  
  public String getScheme() {
    if (uri != null)
      return uri.getScheme();
    return "file";
  }

  public String getFragment() {
    if (uri != null)
      return uri.getFragment();
    return null;
  }
  
  public String getQuery() {
    if (uri != null)
      return uri.getQuery();
    return null;
  }
  
  public String getUserInfo() {
    if (uri != null)
      return uri.getUserInfo();
    return null;
  }
  
  public String getHost() {
    if (uri != null)
      return uri.getHost();
    return null;
  }
  
  public int getPort() {
    if (uri != null)
      return uri.getPort();
    return -1;
  }
  
  public String getName() {
    if (uri != null)
      return getLastPart(uri.getPath());
    return path.getName().toString();
  }
  
  private static String getLastPart(String path) {
    int index = path.lastIndexOf('/');
    if (index == -1)
      return "";
    return path.substring(index + 1);
  }
  
  public String getExtension() {
    return getExtension(getName());
  }
  
  private static String getExtension(String name) {
    int index = name.lastIndexOf('.');
    if (index == -1)
      return "";
    return name.substring(index + 1);
  }
  
  public URI getParent() {
    if (uri != null) {
      return getParent(this);
    }
    return new URI(path.getParent());
  }
  
  public static URI getParent(URI uri) {
    java.net.URI innerURI = uri.uri;
    String path = innerURI.getPath();
    if (path.equals("/")) {  // if it's '/'
      return uri;
    }
    
    String newPath;
    int length = path.length();
    if (length > 0 && path.charAt(length - 1) == '/') {  // if there is a trailing slash
      if (length <= 2) {
        newPath="/"; 
      } else {
        int index = path.lastIndexOf('/', length - 2);
        newPath = path.substring(0, index);
      }
    } else {
      int index = path.lastIndexOf('/');
      newPath = path.substring(0, index);
    }
    
    try {
      return new URI(new java.net.URI(innerURI.getScheme(),
          innerURI.getRawAuthority(),
          newPath,
          innerURI.getRawQuery(),
          innerURI.getRawFragment()));
    } catch (URISyntaxException e) {
      throw new AssertionError(e);
    }
  }
  
  public URI getPath() {
    if (uri != null) {
      return new URI(Paths.get(uri.getPath()));
    }
    return this;
  }
  
  public URI resolve(URI uri) {
    if (this.uri != null) {
      if (uri.uri != null) {
        return new URI(this.uri.resolve(uri.uri));
      }
      return new URI(this.uri.resolve(path.toString()));
    }
    if (uri.uri != null) {
      return uri;
    }
    return new URI(path.resolve(uri.path));
  }
  
  /* FIXME, need to escape characters
  public URI append(String pathToResolve) {
    if (uri != null)
      return new URI(uri.resolve(pathToResolve));
    return new URI(path.resolve(pathToResolve));
  }*/
  
  public URI relativize(URI uri) {
    if (this.uri != null) {
      if (uri.uri != null) {
        return new URI(this.uri.relativize(uri.uri));
      }
      return new URI(this.uri.relativize(java.net.URI.create(path.toString())));
    }
    if (uri.uri != null) {
      return uri;
    }
    return new URI(path.relativize(uri.path));
  }
  
  public URI normalize() {
    if (uri != null) {
      return new URI(uri.normalize());
    }
    return new URI(path.normalize());
  }
  
  public boolean isAbsolute() {
    if (uri != null)
      return uri.isAbsolute();
    return path.isAbsolute();
  }
  
  public InputStream getInputStream() throws IOException {
    if (uri != null) {
      try {
        return uri.toURL().openStream();
      } catch (MalformedURLException e) {
        throw RT.error((Node)null, e);
      }
    }
    return path.newInputStream();
  }
}
