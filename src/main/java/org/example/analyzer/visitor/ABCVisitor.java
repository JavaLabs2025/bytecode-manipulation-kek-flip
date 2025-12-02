package org.example.analyzer.visitor;

import static org.objectweb.asm.Opcodes.*;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ABCVisitor extends ClassVisitor {
    public ABCVisitor() {
        super(ASM8);
    }

    private Long assignments = Long.valueOf(0);
    private Long branches = Long.valueOf(0);
    private Long conditionals = Long.valueOf(0);

    public Long getAssignments() {
        return assignments;
    }

    public Long getBranches() {
        return branches;
    }

    public Long getConditionals() {
        return conditionals;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return new ABCMethodVisitor();
    }

    class ABCMethodVisitor extends MethodVisitor {
        private static final Set<Integer> assignmentOpCodes = Set.of(
                IASTORE, LASTORE, FASTORE, DASTORE,
                AASTORE, BASTORE, CASTORE, SASTORE,
                PUTSTATIC, PUTFIELD);

        public ABCMethodVisitor() {
            super(ASM8);
        }

        @Override
        public void visitIincInsn(final int varIndex, final int increment) {
            assignments++;
        }

        @Override
        public void visitVarInsn(final int opcode, final int varIndex) {
            if (assignmentOpCodes.contains(opcode)) {
                assignments++;
            }
        }

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
            if (opcode == NEW) {
                branches++;
            }
        }

        @Override
        public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
            if (assignmentOpCodes.contains(opcode)) {
                assignments++;
            }
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
            if (opcode != GOTO) {
                conditionals++;
            }
        }

        @Override
        public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
            conditionals++;
        }

        @Override
        public void visitInvokeDynamicInsn(
                final String name,
                final String descriptor,
                final Handle bootstrapMethodHandle,
                final Object... bootstrapMethodArguments) {
            branches++;
        }

        @Override
        public void visitMethodInsn(
                final int opcode,
                final String owner,
                final String name,
                final String descriptor,
                final boolean isInterface) {
            branches++;
        }

        @Override
        public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
            conditionals += labels.length + 1;
        }

        @Override
        public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
            conditionals += labels.length + 1;
        }
    }
}