package com.googlecode.phpreboot.compiler;

import java.dyn.MethodHandle;
import java.dyn.MethodHandles;
import java.dyn.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.googlecode.phpreboot.ast.Fun;
import com.googlecode.phpreboot.interpreter.Scope;
import com.googlecode.phpreboot.model.Function;

public class Compiler {
  /*
  private static int counter;
  
  public static MethodHandle compile(Function function, Scope scope) {
    
    Fun functionNode = function.getNode();
    TypeChecker typeChecker = new TypeChecker();
    typeChecker.typeCheck(functionNode, scope);
    
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "GenStub$"+counter++, null, "java/lang/Object", null);
    
    MethodType methodType = function.asMethodType();
    String desc = methodType.toMethodDescriptorString();
    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC|Opcodes.ACC_STATIC, "gen", desc, null, null);
    mv.visitCode();
    
    Gen gen = new Gen(mv);
    gen.gen(functionNode);
    
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    
    cw.visitEnd();
    
    byte[] array = cw.toByteArray();
    return define(Compiler.class.getClassLoader(), array, methodType);
  }
  
  private static MethodHandle define(ClassLoader classLoader, byte[] bytecodes, MethodType methodType) {
    Class<?> declaredClass = define(classLoader, bytecodes);
    return MethodHandles.lookup().findStatic(declaredClass, "gen", methodType);
  }
  
  private static Class<?> define(ClassLoader classLoader, byte[] bytecodes) {
    try {
      return (Class<?>) define.invoke(classLoader, null, bytecodes, 0, bytecodes.length);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    } catch (IllegalArgumentException e) {
      throw new AssertionError(e);
    } catch (InvocationTargetException e) {
      throw new AssertionError(e.getCause());
    }
  }
  
  private static final Method define;
  static {
    Method method;
    try {
      method = ClassLoader.class.getDeclaredMethod("define", String.class, byte[].class, int.class, int.class);
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
    method.setAccessible(true);
    define = method;
  }*/
}
