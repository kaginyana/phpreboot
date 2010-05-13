package com.googlecode.phpreboot.compiler;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

import com.googlecode.phpreboot.ast.Block;
import com.googlecode.phpreboot.model.Function;
import com.googlecode.phpreboot.model.Parameter;
import com.googlecode.phpreboot.model.PrimitiveType;
import com.googlecode.phpreboot.model.Type;
import com.googlecode.phpreboot.runtime.RT;

public class Compiler {
  private static int counter;
  
  public static MethodHandle compile(Function function) {
    String name = function.getName();
    
    LocalScope localScope = new LocalScope(function.getScope());
    for(Parameter parameter: function.getParameters()) {
      Type type = parameter.getType();
      localScope.register(new LocalVar(parameter.getName(), true, type, localScope.nextSlot(type)));
    }
     
    Block functionNode = function.getBlock();
    TypeChecker typeChecker = new TypeChecker();
    BindMap bindMap = new BindMap();
    TypeCheckEnv typeCheckEnv = new TypeCheckEnv(localScope, function.getReturnType(), bindMap);
    
    Type liveness;
    try {
      liveness = typeChecker.typeCheck(functionNode, typeCheckEnv);
    } catch(CodeNotCompilableException e) {
      return null;
    }
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "GenStub$"+(counter++)+'$'+name, null, "java/lang/Object", null);
    cw.visitSource("script", null);
    
    MethodType methodType = asMethodType(function, bindMap);
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, name, desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(mv);
    gen.gen(functionNode, new GenEnv(bindMap.getSlotCount(), null));
    if (liveness == LivenessType.ALIVE) {
      gen.defaultReturn(function.getReturnType());
    }
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    generateStaticInit(cw);
    
    cw.visitEnd();
    
    byte[] array = cw.toByteArray();
    
    CheckClassAdapter.verify(new ClassReader(array), true, new PrintWriter(System.err));
    
    MethodHandle mh = define(Compiler.class.getClassLoader(), name, array, methodType);
    if (bindMap.getSlotCount() != 0) {
      mh = MethodHandles.insertArguments(mh, 0, bindMap.getReferences());
    }
    
    //System.err.println("compiled method "+mh.type());
    
    return mh;
  }
  
  private static void generateStaticInit(ClassWriter cw) {
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
    mv.visitCode();
    
    mv.visitLdcInsn(org.objectweb.asm.Type.getType(RT.class));
    mv.visitLdcInsn("bootstrap");
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/dyn/Linkage", "registerBootstrapMethod", "(Ljava/lang/Class;Ljava/lang/String;)V");
    mv.visitInsn(Opcodes.RETURN);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
  }

  private static Class<?> asClass(Type type) {
    if (type instanceof PrimitiveType) {
      switch((PrimitiveType)type) {
      case BOOLEAN:
        return boolean.class;
      case INT:
        return int.class;
      case DOUBLE:
        return double.class;
      default:
        return type.getRuntimeClass();
      }
    }
    return void.class;
  }
  
  private static MethodType asMethodType(Function function, BindMap bindMap) {
    List<Parameter> parameters = function.getParameters();
    int count = bindMap.getReferencesCount();
    Class<?>[] parameterArray = new Class<?>[count + 1 + parameters.size()];
    for(int i=0; i<count; i++) {
      parameterArray[i] = bindMap.getReferenceClass(i);
    }
    parameterArray[count] = /*EvalEnv.class*/ Object.class;
    for(int i = 0; i < parameters.size(); i++) {
      parameterArray[count + 1 + i] = asClass(parameters.get(i).getType());
    }
    return MethodType.methodType(asClass(function.getReturnType()), parameterArray);
  }
  
  private static MethodHandle define(ClassLoader classLoader, String name, byte[] bytecodes, MethodType methodType) {
    Class<?> declaredClass = define(classLoader, bytecodes);
    return MethodHandles.lookup().findStatic(declaredClass, name, methodType);
  }
  
  private static Class<?> define(ClassLoader classLoader, byte[] bytecodes) {
    try {
      return (Class<?>) defineClass.invoke(classLoader, null, bytecodes, 0, bytecodes.length);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (IllegalArgumentException e) {
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e.getCause());
    }
  }
  
  private static final Method defineClass;
  static {
    Method method;
    try {
      method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
    method.setAccessible(true);
    defineClass = method;
  }
}
