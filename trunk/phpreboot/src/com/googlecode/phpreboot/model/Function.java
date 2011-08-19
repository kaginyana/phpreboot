package com.googlecode.phpreboot.model;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MutableCallSite;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.ast.ParameterAny;
import com.googlecode.phpreboot.ast.ParameterTyped;
import com.googlecode.phpreboot.ast.Parameters;
import com.googlecode.phpreboot.ast.ReturnType;
import com.googlecode.phpreboot.interpreter.Scope;

public class Function {
  private final String name;
  private final List<Parameter> parameters;
  private final List<Type> parameterTypes; //FIXME should be a view of parameters 
  private Type returnType;                 // can't be final because of inference of recursive function return type
  private final /*@Nullable*/Scope scope;
  private final Block block;
  private final /*@Nullable*/IntrinsicInfo intrinsicInfo;
  
  private MethodHandle methodHandle;
  private boolean optimized;
  private FunctionCallSite callSites;
  private final Map<List<Type>, Function> signatureCache;
  
  
  public Function(String name, List<Parameter> parameters, Type returnType, /*@Nullable*/Scope scope, /*@Nullable*/IntrinsicInfo intrinsicInfo, Map<List<Type>, /*@Nullable*/Function> signatureCache, Block block) {
    this.name = name;
    this.parameters = parameters;
    this.parameterTypes = gatherParameterTypes(parameters);
    this.returnType = returnType;
    this.scope = scope;
    this.intrinsicInfo = intrinsicInfo;
    this.signatureCache = signatureCache;
    this.block = block;
  }
  
  private static List<Type> gatherParameterTypes(List<Parameter> parameters) {
    ArrayList<Type> parameterTypes = new ArrayList<>(parameters.size());
    for(Parameter parameter: parameters) {
      parameterTypes.add(parameter.getType());
    }
    return parameterTypes;
  }

  public String getName() {
    return name;
  }
  public List<Parameter> getParameters() {
    return parameters;
  }
  public List<Type> getParameterTypes() {
    return parameterTypes;
  }
  public Type getReturnType() {
    if (returnType == null) {    // null during inference of recursive function 
      return PrimitiveType.ANY;  // in that case return type is any
    }
    return returnType;
  }
  public /*@Nullable*/Scope getScope() {
    return scope;
  }
  public /*@Nullable*/IntrinsicInfo getIntrinsicInfo() {
    return intrinsicInfo;
  }
  public Block getBlock() {
    return block;
  }
  
  //should be called only by the Typechecker 
  public void __setReturnType__(Type returnType) {
    this.returnType = returnType;
  }
  public /*@Nullable*/Map<List<Type>, Function> getSignatureCache() {
    return signatureCache;
  }
  // A call to this method is only valid if intrinsicInfo is null
  public Function lookupSignature(List<Type> signature) {
    assert intrinsicInfo == null;
    return signatureCache.get(signature);
  }
  // A call to this method is only valid if intrinsicInfo is null
  public void registerSignature(Function function) {
    assert intrinsicInfo == null;
    signatureCache.put(function.parameterTypes, function);
  }
  
  
  public static class FunctionCallSite extends MutableCallSite {
    FunctionCallSite next;

    public FunctionCallSite(MethodType methodType) {
        super(methodType);
    }
  }
  
  public boolean isOptimized() {
    return optimized;
  }
  public MethodHandle getMethodHandle() {
    return methodHandle;
  }
  public void setMethodHandle(MethodHandle methodHandle, boolean optimized) {
    this.methodHandle = methodHandle;
    if (optimized && callSites != null) {
      for(FunctionCallSite callSite = callSites; callSite != null; callSite = callSite.next) {
        callSite.setTarget(methodHandle); 
      }
      callSites = null;
      this.optimized = true;
    }
  }
  public void linkCallSite(FunctionCallSite functionCallSite) {
    functionCallSite.next = callSites;
    callSites = functionCallSite;
  }
  
  @Override
  public String toString() {
    return name+parameters+':'+returnType;
  }
  
  public static Function createFunction(String name, Parameters parametersNode, IntrinsicInfo intrinsicInfo, Scope scope, Block block) {
    List<com.googlecode.phpreboot.ast.Parameter> parameterStar = parametersNode.getParameterStar();
    int size = parameterStar.size();
    ArrayList<Parameter> parameters = new ArrayList<>(size);
    
    for(int i=0; i<size; i++) {
      String parameterName;
      Type type;
      com.googlecode.phpreboot.ast.Parameter parameter = parameterStar.get(i);
      if (parameter instanceof ParameterAny) {
        parameterName = ((ParameterAny)parameter).getId().getValue();
        type = PrimitiveType.ANY;
      } else {
        ParameterTyped parameterType = (ParameterTyped)parameter;
        parameterName = parameterType.getId().getValue();
        type = PrimitiveType.lookup(parameterType.getType().getId().getValue());
      }
      parameters.add(new Parameter(parameterName, type, parameter));
    }
 
    ReturnType returnTypeNode = parametersNode.getReturnTypeOptional();
    Type returnType = (returnTypeNode == null)? PrimitiveType.ANY: PrimitiveType.lookup(returnTypeNode.getType().getId().getValue());
    
    Function function = new Function(name,
        parameters,
        returnType,
        Scope.filterReadOnlyVars(scope),
        intrinsicInfo,
        new HashMap<List<Type>, Function>(),
        block);
    function.registerSignature(function);
    return function;
  }
}
