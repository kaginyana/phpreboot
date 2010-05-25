package com.googlecode.phpreboot.module;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.IntrinsicInfo;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;

//FIXME module must be loaded lazily
public abstract class Module {
  private static final Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();
  
  public void registerModule(Scope scope) {
    for(Method method: this.getClass().getMethods()) {
      if (!method.isAnnotationPresent(Export.class))
        continue;
      
      MethodHandle mh = PUBLIC_LOOKUP.unreflect(method);
      
      // bind the method handles to the current module
      if (!Modifier.isStatic(method.getModifiers())) {
        mh = MethodHandles.insertArguments(mh, 0, this);
      }
      
      // add a unused parameter for the environment
      mh = MethodHandles.dropArguments(mh, 0, Object.class/*EvalEnv.class*/);
      
      Function function = createFunction(method);
      function.setMethodHandle(mh);
      
      Var var = new Var(method.getName(), true, PrimitiveType.ANY, function);
      scope.register(var);
    }
  }
  
  private static Function createFunction(Method method) {
    ArrayList<Parameter> parameters = new ArrayList<Parameter>();
    Class<?>[] parameterTypes = method.getParameterTypes();
    for(int i=0; i< parameterTypes.length; i++) {
      Parameter parameter = new Parameter("arg"+i, asType(parameterTypes[i]));
      parameters.add(parameter);
    }
    
    // adds intrinsics link
    
    Class<?> declaringClass;
    String name;
    Intrinsic intrinsic = method.getAnnotation(Intrinsic.class);
    if (intrinsic != null) {
      declaringClass = intrinsic.declaringClass();
      name = intrinsic.name();
    } else {
      declaringClass = method.getDeclaringClass();
      name = method.getName();
    }
    IntrinsicInfo instrinsicInfo = new IntrinsicInfo(declaringClass, name);
    return new Function(method.getName(), parameters, asType(method.getReturnType()), null, instrinsicInfo, null);
  }
  
  private static Type asType(Class<?> runtimeClass) {
    //FIXME finish implementation and move the method in package runtime
    if (runtimeClass == boolean.class)
      return PrimitiveType.BOOLEAN;
    if (runtimeClass == int.class)
      return PrimitiveType.INT;
    if (runtimeClass == double.class)
      return PrimitiveType.DOUBLE;
    if (runtimeClass == String.class)
      return PrimitiveType.STRING;
    return PrimitiveType.ANY;
  }
}
