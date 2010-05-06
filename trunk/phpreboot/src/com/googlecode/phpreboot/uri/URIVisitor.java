package com.googlecode.phpreboot.uri;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.googlecode.phpreboot.ast.AbsolutePathRest;
import com.googlecode.phpreboot.ast.AbsolutePathSlash;
import com.googlecode.phpreboot.ast.AbsoluteUri;
import com.googlecode.phpreboot.ast.Fragment;
import com.googlecode.phpreboot.ast.HostInfo;
import com.googlecode.phpreboot.ast.HostInfoLogin;
import com.googlecode.phpreboot.ast.HostInfoLoginPassword;
import com.googlecode.phpreboot.ast.HostnameIp;
import com.googlecode.phpreboot.ast.HostnameName;
import com.googlecode.phpreboot.ast.IdToken;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.PathAbsoluteUri;
import com.googlecode.phpreboot.ast.PathDot;
import com.googlecode.phpreboot.ast.PathDotRest;
import com.googlecode.phpreboot.ast.PathDotdot;
import com.googlecode.phpreboot.ast.PathDotdotRest;
import com.googlecode.phpreboot.ast.PathPathLiteral;
import com.googlecode.phpreboot.ast.PathPathLiteralRest;
import com.googlecode.phpreboot.ast.PathPathRest;
import com.googlecode.phpreboot.ast.PathRelativeUri;
import com.googlecode.phpreboot.ast.PathRest;
import com.googlecode.phpreboot.ast.PathRestPath;
import com.googlecode.phpreboot.ast.PathRestStep;
import com.googlecode.phpreboot.ast.PathRestTrailingSlash;
import com.googlecode.phpreboot.ast.PathRootdir;
import com.googlecode.phpreboot.ast.PathStepDot;
import com.googlecode.phpreboot.ast.PathStepDotdot;
import com.googlecode.phpreboot.ast.PathStepId;
import com.googlecode.phpreboot.ast.Port;
import com.googlecode.phpreboot.ast.SchemeFtp;
import com.googlecode.phpreboot.ast.SchemeHttp;
import com.googlecode.phpreboot.ast.UriAbsolute;
import com.googlecode.phpreboot.ast.UriPath;
import com.googlecode.phpreboot.ast.UriQuery;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.uri.PathBuilder.FilePathBuilder;
import com.googlecode.phpreboot.uri.PathBuilder.StringPathBuilder;

public class URIVisitor extends Visitor<Object, URIEnv, URISyntaxException> {
  private URIVisitor() {
    // enforce singleton
  }
  
  public static final URIVisitor INSTANCE = new URIVisitor();
  
  
  public Object eval(Node node, EvalEnv env) {
    URIEnv uriEnv = new URIEnv(env, null);
    try {
      return eval(node, uriEnv);
    } catch (URISyntaxException e) {
      throw RT.error(e);
    }
  }
  
  private Object eval(Node node, URIEnv env) throws URISyntaxException {
    return node.accept(this, env);
  }
  
  @Override
  public Object visit(UriAbsolute uri__absolute_uri, URIEnv env) throws URISyntaxException {
    return eval(uri__absolute_uri.getAbsoluteUri(), env);
  }
  @Override
  public Object visit(UriPath uri__path, URIEnv env) throws URISyntaxException {
    return eval(uri__path.getPath(), env);
  }
  
  @Override
  public Object visit(AbsoluteUri absolute_uri, URIEnv env) throws URISyntaxException {
    String scheme = (String)eval(absolute_uri.getScheme(), env);
    URI userInfoAndHost = (URI)eval(absolute_uri.getHost(), env);
    Port port = absolute_uri.getPortOptional();
    int portNumber = (port == null)?-1:port.getPortNumber().getValue();
    String path = (String)eval(absolute_uri.getAbsolutePath(), env);
    UriQuery uriQuery = absolute_uri.getUriQueryOptional();
    String query = uriQuery == null? null: uriQuery.getId().getValue();
    Fragment uriFragment = absolute_uri.getFragmentOptional();
    String fragment = (uriFragment == null)? null: uriFragment.getId().getValue();
    
    return new URI(scheme,
        userInfoAndHost.getUserInfo(),
        userInfoAndHost.getHost(),
        portNumber,
        path,
        query,
        fragment);
  }
  
  @Override
  public Object visit(SchemeFtp scheme__ftp_scheme, URIEnv env) {
    return "ftp";
  }
  @Override
  public Object visit(SchemeHttp scheme__http_scheme, URIEnv env) {
    return "http";
  }
  
  @Override
  public Object visit(HostInfo host_info, URIEnv env) throws URISyntaxException {
    String host = (String)eval(host_info.getHostname(), env);
    return new URI(null, null, host,-1, null, null, null);
  }
  @Override
  public Object visit(HostInfoLogin host_info_login, URIEnv env) throws URISyntaxException {
    String host = (String)eval(host_info_login.getHostname(), env);
    return new URI(null, host_info_login.getId().getValue(), host,-1, null, null, null);
  }
  @Override
  public Object visit(HostInfoLoginPassword host_info_login_password, URIEnv env) throws URISyntaxException {
    String host = (String)eval(host_info_login_password.getHostname(), env);
    String userInfo = host_info_login_password.getId().getValue() + ':' +
      host_info_login_password.getId2().getValue();
    return new URI(null, userInfo, host,-1, null, null, null);
  }
  
  @Override
  public Object visit(HostnameIp hostname_ip, URIEnv env) throws URISyntaxException {
    StringBuilder builder = new StringBuilder();
    builder.append(hostname_ip.getValueLiteral().getValue());
    for(ValueLiteralToken token: hostname_ip.getValueLiteralPlus()) {
      builder.append('.').append(token.getValue());
    }
    return builder.toString();
  }
  @Override
  public Object visit(HostnameName hostname_name, URIEnv env) throws URISyntaxException {
    StringBuilder builder = new StringBuilder();
    builder.append(hostname_name.getId().getValue());
    for(IdToken token: hostname_name.getIdPlus()) {
      builder.append('.').append(token.getValue());
    }
    return builder.toString();
  }
  
  @Override
  public Object visit(AbsolutePathSlash absolute_path_slash, URIEnv env)  {
    return "/";
  }
  @Override
  public Object visit(AbsolutePathRest absolute_path_rest, URIEnv env) throws URISyntaxException {
    String step = (String)eval(absolute_path_rest.getPathStep(), env);
    
    PathRest pathRest = absolute_path_rest.getPathRestOptional();
    if (pathRest == null) {
      return "/"+step;
    }
    
    StringPathBuilder pathBuilder = new StringPathBuilder("/");
    pathBuilder.append(step);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), pathBuilder);
    eval(pathRest, newEnv);
    return pathBuilder.toPath();
  }
  
  
  @Override
  public Object visit(PathDot path_dot, URIEnv env) {
    return Paths.get(".");
  }
  @Override
  public Object visit(PathDotRest path_dot_rest, URIEnv env) throws URISyntaxException {
    FilePathBuilder pathBuilder = new FilePathBuilder(".");
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), pathBuilder);
    eval(path_dot_rest.getPathRest(), newEnv);
    return pathBuilder.toPath();
  }
  @Override
  public Object visit(PathDotdot path_dotdot, URIEnv env) {
    return Paths.get("..");
  }
  @Override
  public Object visit(PathDotdotRest path_dotdot_rest, URIEnv env) throws URISyntaxException {
    FilePathBuilder pathBuilder = new FilePathBuilder("..");
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), pathBuilder);
    eval(path_dotdot_rest.getPathRest(), newEnv);
    return pathBuilder.toPath();
  }
  @Override
  public Object visit(PathRootdir path_rootdir, URIEnv env) throws URISyntaxException {
    char rootName = path_rootdir.getRootDir().getValue();
    Path path = Paths.get(rootName+":\\");
    path = path.resolve((String)eval(path_rootdir.getPathStep(), env));
    PathRest pathRest = path_rootdir.getPathRestOptional();
    if(pathRest == null) {
      return path;
    }
    FilePathBuilder pathBuilder = new FilePathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), pathBuilder);
    eval(pathRest, newEnv);
    return pathBuilder.toPath();
  }
  @Override
  public Object visit(PathRelativeUri path_relative_uri, URIEnv env) throws URISyntaxException {
    String path = (String)eval(path_relative_uri.getPathStep(), env);
    PathRest pathRest = path_relative_uri.getPathRestOptional();
    if (pathRest == null) {
      return new URI("file:", null, path, null);   
    }
    StringPathBuilder builder = new StringPathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(pathRest, newEnv);
    return new URI("file:", null, builder.toPath(), null);
  }
  @Override
  public Object visit(PathAbsoluteUri path_absolute_uri, URIEnv env) throws URISyntaxException {
    StringPathBuilder builder = new StringPathBuilder("/");
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(path_absolute_uri.getPathRest(), newEnv);
    return new URI("file:", null, builder.toPath(), null);
  }
  @Override
  public Object visit(PathPathLiteral path_path_literal, URIEnv env) {
    return Paths.get(path_path_literal.getPathLiteral().getValue());
  }
  @Override
  public Object visit(PathPathLiteralRest path_path_literal_rest, URIEnv env) throws URISyntaxException {
    Path path = Paths.get(path_path_literal_rest.getPathLiteral().getValue());
    FilePathBuilder builder = new FilePathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(path_path_literal_rest.getPathRest(), newEnv);
    return builder.toPath();
  }
  @Override
  public Object visit(PathPathRest path_path_rest, URIEnv env) throws URISyntaxException {
    Path path = Paths.get("/");
    FilePathBuilder builder = new FilePathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(path_path_rest.getPathRest(), newEnv);
    return builder.toPath();
  }
  
  @Override
  public Object visit(PathRestStep path_rest_step, URIEnv env) throws URISyntaxException {
    String step = (String)eval(path_rest_step.getPathStep(), env);
    return env.getPathBuilder().append(step);
  }
  @Override
  public Object visit(PathRestPath path_rest_path, URIEnv env) throws URISyntaxException {
    String step = (String)eval(path_rest_path.getPathStep(), env);
    env.getPathBuilder().append(step);
    return eval(path_rest_path.getPathRest(), env);
  }
  @Override
  public Object visit(PathRestTrailingSlash path_rest_trailing_slash, URIEnv env) throws URISyntaxException {
    return env.getPathBuilder().trailingSlash();
  }
  
  @Override
  public Object visit(PathStepDot path_step_dot, URIEnv env) {
    return ".";
  }
  @Override
  public Object visit(PathStepDotdot path_step_dotdot, URIEnv env) {
    return "..";
  }
  @Override
  public Object visit(PathStepId path_step_id, URIEnv env) {
    return path_step_id.getPathId().getValue();
  }
}
