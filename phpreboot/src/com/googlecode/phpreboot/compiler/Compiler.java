package com.googlecode.phpreboot.compiler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jsr292.weaver.opt.Optimizer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import com.googlecode.phpreboot.ast.LabeledInstrWhile;
import com.googlecode.phpreboot.ast.Node;
import com.googlecode.phpreboot.ast.Script;
import com.googlecode.phpreboot.compiler.LoopStack.Labels;
import com.googlecode.phpreboot.interpreter.EvalEnv;
import com.googlecode.phpreboot.interpreter.Evaluator;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.interpreter.Profile.LoopProfile;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.model.Var;
import com.googlecode.phpreboot.runtime.RT;
import com.googlecode.phpreboot.runtime.RTFlag;

public class Compiler {
  private static int COUNTER;

  private Compiler() {
    //enforce utility class
  }
  
  public static byte[] compileScriptAheadOfTime(String scriptName, Script script, LocalScope rootScope) {
    
    LocalScope localScope = new LocalScope(rootScope);
    //TODO add ARGS etc
    
    // typecheck
    BindMap bindMap = new BindMap();
    TypeChecker typeChecker = new TypeChecker(false, null, null, false, bindMap, new TypeProfileMap(), false);
    Type liveness;
    try {
      liveness = typecheck(typeChecker, script, PrimitiveType.VOID, localScope);
    } catch(CodeNotCompilableTypeCheckException e) {
      return null;
    }
    
    // gen
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
    ClassVisitor cv = cw;
    if (LEGACY_MODE) {
      cv = LegacyWeaver.weave(cw, false);
    }
    cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, scriptName, null, "java/lang/Object", null);
    cv.visitSource(scriptName, null);
    
    MethodType methodType = MethodType.methodType(void.class, String[].class);
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "main", desc, null, null);
    mv.visitCode();
    
    // init env
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, Gen.EVAL_ENV_INTERNAL_NAME, "defaultEvalEnv", "()L"+Gen.EVAL_ENV_INTERNAL_NAME+';');
    mv.visitVarInsn(Opcodes.ASTORE, 0);
    
    Gen gen = new Gen(false, scriptName, cv, typeChecker.getTypeAttributeMap(), typeChecker.getSymbolAttributeMap());
    gen.gen(script, new GenEnv(mv, 2 /*ARGS + env*/, PrimitiveType.VOID, null, new LoopStack<Labels>(), null));
    if (liveness == LivenessType.ALIVE) {
      Gen.defaultReturn(mv, PrimitiveType.VOID);
    }
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    //generateStaticInit(cv);
    
    cv.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    if (RTFlag.DEBUG) {
      bindMap.dump();
      CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    }
    
    return array;
  }
  
  public static MethodHandle compileFunction(Function function) {
    String name = function.getName();
    LocalScope localScope = new LocalScope(function.getScope());
    localScope.register(new Var(name, true, true, PrimitiveType.ANY, function));
    List<Parameter> parameters = function.getParameters();
    int size = parameters.size();
    for(int i=0; i<size; i++) {
      Parameter parameter = parameters.get(i);
      Type type = parameter.getType();
      localScope.register(LocalVar.createLocalVar(parameter.getName(), true, type, null, localScope.nextSlot(type)));
    }
    
    BindMap bindMap = new BindMap();
    TypeChecker typeChecker = new TypeChecker(false, null, null, false, bindMap, new TypeProfileMap(), false);
    Type liveness;
    try {
      liveness = typecheck(typeChecker, function.getBlock(), function.getReturnType(), localScope);
    } catch(CodeNotCompilableTypeCheckException ignored) {
      return null;
    }
    
    return gen(function, bindMap, liveness, typeChecker.getTypeAttributeMap(), typeChecker.getSymbolAttributeMap());
  }
  
  public static Function traceTypecheckFunction(Function function, List<Type> types) {
    String name = function.getName();
    LocalScope localScope = new LocalScope(function.getScope());
    localScope.register(new Var(name, true, true, PrimitiveType.ANY, function));
    List<Parameter> parameters = function.getParameters();
    int size = parameters.size();
    Var[] vars = new Var[size];
    for(int i=0; i<size; i++) {
      Parameter parameter = parameters.get(i);
      Type type = parameter.getType();
      Node node;
      if (type == PrimitiveType.ANY) {
        type = types.get(i);
        node = parameter.getNode();
      } else {
        node = null;
      }
      LocalVar localVar = LocalVar.createLocalVar(parameter.getName(), true, type, node, localScope.nextSlot(type));
      vars[i] = localVar;
      localScope.register(localVar);
    }
    
    Function specializedFunction = createSpecializedFunction(function, vars);
    Type functionReturnType = function.getReturnType();
    boolean inferReturnType = functionReturnType == PrimitiveType.ANY;
    if (!inferReturnType) {
      specializedFunction.__setReturnType__(functionReturnType);
    }
    
    function.getSignatureCache().put(types, specializedFunction);   //fix point, avoid infinite recursive typechecking
    
    BindMap bindMap = new BindMap();
    TypeChecker typeChecker = new TypeChecker(false, null, null, inferReturnType, bindMap, new TypeProfileMap(), RTFlag.COMPILER_OPTIMISTIC);
    
    Type liveness;
    try {
      liveness = typecheck(typeChecker, function.getBlock(), functionReturnType, localScope);
    } catch(CodeNotCompilableTypeCheckException ignored) {
      function.getSignatureCache().remove(types);
      return null;
    }
    
    if (inferReturnType) {
      specializedFunction.__setReturnType__(typeChecker.getInferedReturnType());
    }
    
    MethodHandle mh = SpecializedFunctionStub.specializedStub(specializedFunction,
        bindMap,
        liveness,
        typeChecker.getTypeAttributeMap(),
        typeChecker.getSymbolAttributeMap(),
        asMethodType(specializedFunction));
    specializedFunction.setMethodHandle(mh, false);
    return specializedFunction;
  }
  
  private static Function createSpecializedFunction(Function unspecializedFunction, Var[] vars) {
    assert unspecializedFunction.getIntrinsicInfo() == null;
    
    int length = vars.length;
    ArrayList<Parameter> parameters = new ArrayList<>(length);
    List<Parameter> unspecializedParameters = unspecializedFunction.getParameters();
    for(int i=0; i<length; i++) {
      Parameter unspecializedParameter = unspecializedParameters.get(i);
      Parameter parameter = new Parameter(unspecializedParameter.getName(), vars[i].getType(), unspecializedParameter.getNode());
      parameters.add(parameter);
    }
    
    Map<List<Type>, Function> signatureCache = unspecializedFunction.getSignatureCache();
    Function function = new Function(unspecializedFunction.getName(),
        parameters,
        null,           // return type will be set later
        unspecializedFunction.getScope(),
        null,
        signatureCache,
        unspecializedFunction.getBlock());
    function.registerSignature(function);
    return function;
  }
  
  public static class CompileFunctionStub {
    private int counter;
    
    public static MethodHandle compileStub() {
      CompileFunctionStub compileFunctionStub = new CompileFunctionStub();
      return STUB.bindTo(compileFunctionStub);
    }
    
    public static Object stub(CompileFunctionStub stub, Function function, EvalEnv env, Object[] args) throws Throwable {
      int counter = stub.counter;
      if (counter >= RTFlag.COMPILER_FUNCTION_THRESHOLD) {
        MethodHandle compileMH = Compiler.compileFunction(function);
        if (compileMH != null) {
          function.setMethodHandle(compileMH, true);
        } else { // fallback, always use the evaluator
          MethodType functionType = function.getMethodHandle().type();
          MethodHandle evaluator = MethodHandles.insertArguments(Evaluator.EVAL_FUNCTION, 0, function);
          evaluator = evaluator.asCollector(Object[].class, functionType.parameterCount() - 1);
          evaluator = evaluator.asType(functionType);
          function.setMethodHandle(evaluator, true);
        }
      } 
      stub.counter = counter + 1;
      return Evaluator.INSTANCE.evalFunction(function, env, args);
    }
    
    static final MethodHandle STUB;
    static {
      try {
        STUB = MethodHandles.publicLookup().findStatic(CompileFunctionStub.class, "stub",
            MethodType.methodType(Object.class, CompileFunctionStub.class, Function.class, EvalEnv.class, Object[].class));
      } catch (IllegalAccessException e) {
        throw (AssertionError)new AssertionError().initCause(e);
      } catch (NoSuchMethodException e) {
        throw (AssertionError)new AssertionError().initCause(e);
      }
    }
  }
  
  public static class SpecializedFunctionStub {
    public static MethodHandle specializedStub(Function specializedFunction,
        BindMap bindMap,
        Type liveness,
        Map<Node, Type> typeAttributeMap,
        Map<Node, Symbol> symbolAttributeMap,
        MethodType methodType) {
      
      MethodHandle stub = MethodHandles.insertArguments(STUB, 0, specializedFunction, bindMap, liveness, typeAttributeMap, symbolAttributeMap);
      return stub.asCollector(Object[].class, methodType.parameterCount()).asType(methodType);
    }
    
    public static Object stub(Function specializedFunction,
        BindMap bindMap,
        Type liveness,
        Map<Node, Type> typeAttributeMap,
        Map<Node, Symbol> symbolAttributeMap,
        Object[] args) throws Throwable {

       MethodHandle mh = Compiler.gen(specializedFunction, bindMap, liveness, typeAttributeMap, symbolAttributeMap);
       specializedFunction.setMethodHandle(mh, true);
       return mh.invokeWithArguments(args);
    }
    
    static final MethodHandle STUB;
    static {
      try {
        STUB = MethodHandles.publicLookup().findStatic(SpecializedFunctionStub.class, "stub",
            MethodType.methodType(Object.class, Function.class, BindMap.class, Type.class, Map.class, Map.class, Object[].class));
      } catch (IllegalAccessException e) {
        throw (AssertionError)new AssertionError().initCause(e);
      } catch (NoSuchMethodException e) {
        throw (AssertionError)new AssertionError().initCause(e);
      }
    }
  }
  
  static MethodHandle gen(Function function, BindMap bindMap, Type liveness, Map<Node, Type> typeAttributeMap, Map<Node, Symbol> symbolAttributeMap) {
    String name = function.getName();
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ((ANONYMOUS_CLASS_DEFINE == null)?ClassWriter.COMPUTE_FRAMES:0));
    ClassVisitor cv = cw;
    if (LEGACY_MODE) {
      cv = LegacyWeaver.weave(cw, true);
    }
    String className = "GenStub$"+(COUNTER++)+'$'+name;
    cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, className, null, "java/lang/Object", null);
    cv.visitSource("script", null);
    
    MethodType methodType = asMethodType(function, bindMap);
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, name, desc, null, null);
    mv.visitCode();
    
    Type returnType = function.getReturnType();
    Gen gen = new Gen(false, className, cv, typeAttributeMap, symbolAttributeMap);
    gen.gen(function.getBlock(), new GenEnv(mv, bindMap.getSlotCount(), returnType, null, new LoopStack<Labels>(), null));
    if (liveness == LivenessType.ALIVE) {
      Gen.defaultReturn(mv, returnType);
    }
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    //generateStaticInit(cv);
    
    cv.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    if (RTFlag.DEBUG) {
      bindMap.dump();
      CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    }
    
    MethodHandle mh = define(className, name, array, methodType);
    
    List<LocalVar> bindReferences = bindMap.getReferences();
    if (!bindReferences.isEmpty()) {
      int size = bindReferences.size();
      Object[] boundArray = new Object[size];
      for(int i=0; i<size; i++) {
        boundArray[i] = bindReferences.get(i).getValue(); 
      }
      
      mh = MethodHandles.insertArguments(mh, 1, boundArray);
    }
    
    //System.err.println("compiled method "+mh.type());
    
    return mh;
  }
  
  // returns the liveness of the block 
  private static Type typecheck(TypeChecker typeChecker, Node node, Type returnType, LocalScope localScope) throws CodeNotCompilableTypeCheckException {
    LoopStack<Boolean> loopStack = new LoopStack<>();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, loopStack, false, returnType);
    return typeChecker.typeCheck(node, typeCheckEnv);
  }
  
  // @return null -> never compile, true -> trace ends, false -> trace escape
  public static Boolean traceCompileAndExec(LabeledInstrWhile labeledInstrWhile, LoopProfile profile, boolean optimisticTrace, EvalEnv env) {
    Scope rootScope = env.getScope();
    LocalScope localScope = new LocalScope(rootScope);
    
    TypeProfileMap typeProfileMap = new TypeProfileMap();
    BindMap bindMap = new BindMap();
    TypeChecker typeChecker = new TypeChecker(true, labeledInstrWhile, rootScope, false, bindMap, typeProfileMap, RTFlag.COMPILER_OPTIMISTIC && optimisticTrace);
    LoopStack<Boolean> loopStack = new LoopStack<>();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, loopStack, false, PrimitiveType.ANY);
    
    try {
      typeChecker.typeCheck(labeledInstrWhile, typeCheckEnv);
    } catch(CodeNotCompilableTypeCheckException e) {
      return null;
    }
    
    
    if (!typeProfileMap.isValid()) {
      //System.err.println("optimistic typecheck failed");
      typeProfileMap.validate(true);
      bindMap = new BindMap();
      typeChecker = new TypeChecker(true, labeledInstrWhile, rootScope, false, bindMap, typeProfileMap, false);
      
      //System.err.println("typeProfileMap "+typeProfileMap);
      
      // typecheck again but use the typeProfileMap instead
      
      localScope = new LocalScope(rootScope);
      typeCheckEnv = new TypeCheckEnv(localScope, loopStack, false, PrimitiveType.ANY);
      try {
        typeChecker.typeCheck(labeledInstrWhile, typeCheckEnv);
      } catch(CodeNotCompilableTypeCheckException e) {
        return null;
      }
      
      if (!typeProfileMap.isValid())
        throw new AssertionError("second typecheck invalidated !");
    }
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS| ((ANONYMOUS_CLASS_DEFINE == null)?ClassWriter.COMPUTE_FRAMES:0));
    ClassVisitor cv = cw;
    if (LEGACY_MODE) {
      cv = LegacyWeaver.weave(cw, true);
    }
    String className = "GenStub$"+(COUNTER++)+"$trace";
    cv.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC|Opcodes.ACC_FINAL, className, null, "java/lang/Object", null);
    cv.visitSource("script", null);
    
    MethodType methodType = asTraceMethodType(bindMap);
    
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "trace", desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(true, className, cv, typeChecker.getTypeAttributeMap(), typeChecker.getSymbolAttributeMap());
    gen.gen(labeledInstrWhile,
        new GenEnv(mv,
            bindMap.getSlotCount() + bindMap.getReferencesCount(),
            PrimitiveType.ANY,
            null,   //ifparts
            new LoopStack<Labels>(),
            null));
    
    // restore env vars
    List<LocalVar> references = bindMap.getReferences();
    int size = references.size();
    int outputVarCount = bindMap.getOutputVarCount();
    Object[] args = new Object[size + outputVarCount + 1];
    args[0] = env;
    if (size != 0) {
      Gen.restoreEnv(mv, references, bindMap.getSlotCount() -1 /*XXX substract env slot */, rootScope, args);
    }
    
    mv.visitInsn(Opcodes.ICONST_1);
    mv.visitInsn(Opcodes.IRETURN);   // return true, trace ends
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    //generateStaticInit(cv);
    
    cv.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    if (RTFlag.DEBUG) {
      bindMap.dump();
      //new ClassReader(array).accept(new TraceClassVisitor(new PrintWriter(System.err)), 0);
      CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    }
    
    MethodHandle mh = define(className, "trace", array, methodType);
    
    // record for reuse
    profile.recordTrace(bindMap, mh);
    
    // debug
    //System.err.println("calls "+java.util.Arrays.toString(args));
    
    try {
      return (Boolean)mh.invokeWithArguments(args);
    } catch(Error e) {
      throw e;
    } catch (Throwable e) {
      throw RT.error((Node)null, e);
    }
  }

  static Class<?> asClass(Type type) {
    return type.getUnboxedRuntimeClass();         // use primitive type if possible
  }
  
  static MethodType asMethodType(Function function) {
    List<Parameter> parameters = function.getParameters();
    int parameterSize = parameters.size();
    Class<?>[] parameterArray = new Class<?>[1 + parameterSize];
    parameterArray[0] = EvalEnv.class;
    for(int i = 0; i < parameterSize; i++) {
      parameterArray[i + 1] = asClass(parameters.get(i).getType());
    }
    return MethodType.methodType(asClass(function.getReturnType()), parameterArray);
  }
  
  private static MethodType asMethodType(Function function, BindMap bindMap) {
    int count = bindMap.getReferencesCount();
    List<Parameter> parameters = function.getParameters();
    Class<?>[] parameterArray = new Class<?>[count + 1 + parameters.size()];
    parameterArray[0] = EvalEnv.class;
    for(int i=0; i<count; i++) {
      parameterArray[i + 1] = asClass(bindMap.getReferenceType(i));
    }
    int parameterSize = parameters.size();
    for(int i = 0; i < parameterSize; i++) {
      parameterArray[i + count + 1] = asClass(parameters.get(i).getType());
    }
    return MethodType.methodType(asClass(function.getReturnType()), parameterArray);
  }
  
  private static MethodType asTraceMethodType(BindMap bindMap) {
    int count = bindMap.getReferencesCount();
    int outputVarsCount = bindMap.getOutputVarCount();
    Class<?>[] parameterArray = new Class<?>[count + outputVarsCount + 1];
    parameterArray[0] = EvalEnv.class;
    for(int i=0; i<count; i++) {
      parameterArray[i + 1] = asClass(bindMap.getReferenceType(i));
    }
    for(int i=0; i<outputVarsCount; i++) {
      parameterArray[i + count + 1] = Var.class;
    }
    return MethodType.methodType(boolean.class, parameterArray);
  }
  
  
  // --- profile
  
  public static Type inferType(Object value) {
    if (value instanceof Boolean) {
      return PrimitiveType.BOOLEAN;
    }
    if (value instanceof Integer) {
      return PrimitiveType.INT;
    }
    if (value instanceof Double) {
      return PrimitiveType.DOUBLE;
    }
    if (value instanceof String) {
      return PrimitiveType.STRING;
    }
    return PrimitiveType.ANY;
  }
  
  public static boolean enableVarProfile(Object value) {
    return inferType(value) != PrimitiveType.ANY;
  }
  
  static Type eraseAsProfile(Type type) {
    switch((PrimitiveType)type) {
    case BOOLEAN:
    case INT:
    case DOUBLE:
    case STRING:
      return type;
    default:
      return PrimitiveType.ANY;   
    }
  }
  
  
  // --- define
  
  private static MethodHandle define(String className, String name, byte[] bytecodes, MethodType methodType) {
    Class<?> declaredClass;
    if (ANONYMOUS_CLASS_DEFINE != null) {
      declaredClass = AnonymousLoader.define(bytecodes);
    } else {
      declaredClass = StandardLoader.define(className.replace('.', '/'), bytecodes);
    }
    try {
      return MethodHandles.lookup().findStatic(declaredClass, name, methodType);
    } catch (IllegalAccessException e) {
      throw (AssertionError)new AssertionError().initCause(e);
    } catch (NoSuchMethodException e) {
      throw (AssertionError)new AssertionError().initCause(e);
    }
  }
  
  static final MethodHandle ANONYMOUS_CLASS_DEFINE;
  static final boolean LEGACY_MODE;
  static {
    boolean legacyMode;
    MethodHandle define;
    
    try {
      try {
        Class.forName("jsr292.weaver.Agent");
      } catch(Error e) {
        // weaver agent exists but java.lang.instrumentation doesn't exist
        // android platform => go in legacy mode
      }
      legacyMode = true;
      define = null;
      
    } catch(ClassNotFoundException e2) {
      legacyMode = false;
      try {
        Class<?> anonymousClassLoaderClass = Class.forName("sun.invoke.anon.AnonymousClassLoader");
        define = MethodHandles.publicLookup().findVirtual(anonymousClassLoaderClass, "loadClass",
            MethodType.methodType(Class.class, byte[].class));
      } catch(ClassNotFoundException e) {
        define = null;
      } catch (IllegalAccessException e) {
        System.err.println(e.getMessage());
        define = null;
      } catch (NoSuchMethodException e) {
        System.err.println(e.getMessage());
        define = null;
      }
    }
    LEGACY_MODE = legacyMode;
    ANONYMOUS_CLASS_DEFINE = define;
  }
  
  static class AnonymousLoader {
    private static final Object ANONYMOUS_CLASS_LOADER;
    static {
      try {
        Class<?> anonymousClassLoaderClass = Class.forName("sun.invoke.anon.AnonymousClassLoader");
        Constructor<?> constructor = anonymousClassLoaderClass.getConstructor(Class.class);
        ANONYMOUS_CLASS_LOADER = constructor.newInstance((Object)null);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        if (cause instanceof RuntimeException) {
          throw (RuntimeException)cause;
        }
        if (cause instanceof Error) {
          throw (Error)cause;
        }
        throw new UndeclaredThrowableException(cause);
      } catch(ReflectiveOperationException e) {
        throw new AssertionError(e);
      }
    }
    static Class<?> define(byte[] bytecodes) {
      try {
        //XXX workaround bug in jdk7b94, should use invokeGeneric instead
        return (Class<?>)ANONYMOUS_CLASS_DEFINE.invokeWithArguments(ANONYMOUS_CLASS_LOADER, bytecodes);
      } catch(Throwable t) {
        t.printStackTrace();
        throw new AssertionError(t);
      }
    }
  }
  
  static class StandardLoader extends ClassLoader {
    private static final StandardLoader STANDARD_LOADER;
    static {
      if (!LEGACY_MODE) {
        registerAsParallelCapable();
      }
      STANDARD_LOADER = new StandardLoader();
    }
    
    static Class<?> define(String className, byte[] bytecodes) {
      if (LEGACY_MODE) {
        Optimizer.registerClassDefinition(className, bytecodes);
      }
      
      return STANDARD_LOADER.defineClass(className, bytecodes, 0, bytecodes.length);
    }
  }
}
