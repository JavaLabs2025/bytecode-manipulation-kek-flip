package org.example.analyzer.visitor;

import static org.objectweb.asm.Opcodes.ASM8;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;

public class InheritanceVisitor extends ClassVisitor {
    private class ClassInfo {
        String name;
        String superName;

        Integer inharitanceDepth;

        ClassInfo(String name, String superName) {
            this.name = name;
            this.superName = superName;

            inharitanceDepth = -1;
        }
    }

    private final Map<String, ClassInfo> classesInfoMap = new HashMap<>();

    public InheritanceVisitor() {
        super(ASM8);
    }

    private Integer calcInharitanceDepth(String name) {
        String currentClass = name;
        Integer depth = 0;

        while (true) {
            if (currentClass.equals("java/lang/Object")) {
                break;
            }

            ClassInfo classInfo = classesInfoMap.get(currentClass);
            if (classInfo.inharitanceDepth > -1) {
                depth += classInfo.inharitanceDepth;
                break;
            }

            depth++;
            currentClass = classInfo.superName;
        }

        return depth;
    }

    public Integer getMaxInheritanceDepth() {
        Integer maxDepth = -1;
        for (ClassInfo classInfo : classesInfoMap.values()) {
            if (classInfo.inharitanceDepth == -1) {
                classInfo.inharitanceDepth = calcInharitanceDepth(classInfo.name);
            }

            maxDepth = Math.max(maxDepth, classInfo.inharitanceDepth);
        }
        return maxDepth;
    }

    public Double getAvgInheritanceDepth() {
        Integer depthSum = 0;
        for (ClassInfo classInfo : classesInfoMap.values()) {
            if (classInfo.inharitanceDepth == -1) {
                classInfo.inharitanceDepth = calcInharitanceDepth(classInfo.name);
            }

            depthSum += classInfo.inharitanceDepth;
        }
        return Double.valueOf(depthSum) / classesInfoMap.size();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (superName == null) {
            return;
        }

        classesInfoMap.putIfAbsent(name, new ClassInfo(name, superName));
    }
}
