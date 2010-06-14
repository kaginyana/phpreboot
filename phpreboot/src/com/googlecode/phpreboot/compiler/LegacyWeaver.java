package com.googlecode.phpreboot.compiler;

import jsr292.weaver.OnlineWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

class LegacyWeaver extends OnlineWeaver {
  private LegacyWeaver(ClassVisitor cv, boolean retransform) {
    super(cv, retransform);
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(Opcodes.V1_5, access, name, signature, superName, interfaces);
  }

  public static ClassVisitor weave(ClassVisitor cv, boolean optimizer) {
    return new LegacyWeaver(cv, optimizer);
  }
}