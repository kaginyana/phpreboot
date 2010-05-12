package com.googlecode.phpreboot.uri;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
import com.googlecode.phpreboot.ast.PathRest;
import com.googlecode.phpreboot.ast.PathRestPath;
import com.googlecode.phpreboot.ast.PathRestStep;
import com.googlecode.phpreboot.ast.PathRestTrailingSlash;
import com.googlecode.phpreboot.ast.PathStepDollarAccess;
import com.googlecode.phpreboot.ast.PathStepDot;
import com.googlecode.phpreboot.ast.PathStepDotdot;
import com.googlecode.phpreboot.ast.PathStepId;
import com.googlecode.phpreboot.ast.Port;
import com.googlecode.phpreboot.ast.RelativeUriRest;
import com.googlecode.phpreboot.ast.RelativeUriRootdir;
import com.googlecode.phpreboot.ast.RelativeUriStepRest;
import com.googlecode.phpreboot.ast.SchemeFtp;
import com.googlecode.phpreboot.ast.SchemeHttp;
import com.googlecode.phpreboot.ast.UriAbsolute;
import com.googlecode.phpreboot.ast.UriQuery;
import com.googlecode.phpreboot.ast.UriQueryPair;
import com.googlecode.phpreboot.ast.UriRelative;
import com.googlecode.phpreboot.ast.ValueLiteralToken;
import com.googlecode.phpreboot.ast.Visitor;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.URI;
import com.googlecode.phpreboot.uri.PathBuilder.FilePathBuilder;
import com.googlecode.phpreboot.uri.PathBuilder.StringPathBuilder;

public class URIVisitor extends Visitor<Object, URIEnv, URISyntaxException> {
  private URIVisitor() {
    // enforce singleton
  }
  
  public static final URIVisitor INSTANCE = new URIVisitor();
  
  
  public URI eval(Node node, EvalEnv env) {
    URIEnv uriEnv = new URIEnv(env, null);
    try {
      return (URI)eval(node, uriEnv);
    } catch (URISyntaxException e) {
      throw RT.error(node, e);
    }
  }
  
  private Object eval(Node node, URIEnv env) throws URISyntaxException {
    return node.accept(this, env);
  }
  
  @Override
  public Object visit(UriAbsolute uri__absolute_uri, URIEnv env) throws URISyntaxException {
    return new URI((java.net.URI)eval(uri__absolute_uri.getAbsoluteUri(), env));
  }
  @Override
  public Object visit(UriRelative uri_relative, URIEnv env) throws URISyntaxException {
    return new URI((Path)eval(uri_relative.getRelativeUri(), env));
  }
  
  
  @Override
  public Object visit(AbsoluteUri absolute_uri, URIEnv env) throws URISyntaxException {
    String scheme = (String)eval(absolute_uri.getScheme(), env);
    java.net.URI userInfoAndHost = (java.net.URI)eval(absolute_uri.getHost(), env);
    Port port = absolute_uri.getPortOptional();
    int portNumber = (port == null)?-1:port.getPortNumber().getValue();
    String path = (String)eval(absolute_uri.getAbsolutePath(), env);
    UriQuery uriQuery = absolute_uri.getUriQueryOptional();
    String query = uriQuery == null? null: (String)eval(uriQuery, env);
    Fragment uriFragment = absolute_uri.getFragmentOptional();
    String fragment = (uriFragment == null)? null: uriFragment.getId().getValue();
    
    return new java.net.URI(scheme,
        userInfoAndHost.getRawUserInfo(),
        userInfoAndHost.getHost(),
        portNumber,
        path,
        query,
        fragment);
  }
  
  @Override
  public Object visit(UriQuery uriQuery, URIEnv env) {
    StringBuilder builder = new StringBuilder();
    List<UriQueryPair> uriQueryPairPlus = uriQuery.getUriQueryPairPlus();
    for(UriQueryPair uriQueryPair: uriQueryPairPlus) {
      builder.append(uriQueryPair.getId().getValue()).
        append('=').append(uriQueryPair.getId2().getValue()).append('&');
    }
    builder.setLength(builder.length() - 1);
    return builder.toString();
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
    return new java.net.URI(null, null, host,-1, null, null, null);
  }
  @Override
  public Object visit(HostInfoLogin host_info_login, URIEnv env) throws URISyntaxException {
    String host = (String)eval(host_info_login.getHostname(), env);
    return new java.net.URI(null, host_info_login.getId().getValue(), host,-1, null, null, null);
  }
  @Override
  public Object visit(HostInfoLoginPassword host_info_login_password, URIEnv env) throws URISyntaxException {
    String host = (String)eval(host_info_login_password.getHostname(), env);
    String userInfo = host_info_login_password.getId().getValue() + ':' +
      host_info_login_password.getId2().getValue();
    return new java.net.URI(null, userInfo, host,-1, null, null, null);
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
  public Object visit(RelativeUriStepRest relative_uri_step_rest, URIEnv env) throws URISyntaxException {
    String path = (String)eval(relative_uri_step_rest.getPathStep(), env);
    PathRest pathRest = relative_uri_step_rest.getPathRestOptional();
    if (pathRest == null) {
      return Paths.get(path);
    }
    FilePathBuilder builder = new FilePathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(pathRest, newEnv);
    return builder.toPath();
  }
  @Override
  public Object visit(RelativeUriRest relative_uri_rest, URIEnv env) throws URISyntaxException {
    FilePathBuilder builder = new FilePathBuilder("/");
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), builder);
    eval(relative_uri_rest.getPathRest(), newEnv);
    return builder.toPath();
  }
  
  @Override
  public Object visit(RelativeUriRootdir relative_uri_rootdir, URIEnv env) throws URISyntaxException {
    char rootName = relative_uri_rootdir.getRootDir().getValue();
    Path path = Paths.get(rootName+":\\");
    path = path.resolve((String)eval(relative_uri_rootdir.getPathStep(), env));
    PathRest pathRest = relative_uri_rootdir.getPathRestOptional();
    if(pathRest == null) {
      return path;
    }
    FilePathBuilder pathBuilder = new FilePathBuilder(path);
    URIEnv newEnv = new URIEnv(env.getEvalEnv(), pathBuilder);
    eval(pathRest, newEnv);
    return pathBuilder.toPath();
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
  @Override
  public Object visit(PathStepDollarAccess path_step_dollar_access, URIEnv env) throws URISyntaxException {
    return Evaluator.INSTANCE.eval(path_step_dollar_access.getDollarAccess(), env.getEvalEnv()).toString(); //nullcheck
  }
}
