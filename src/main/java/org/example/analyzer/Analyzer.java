package org.example.analyzer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;

import org.example.analyzer.visitor.ABCVisitor;
import org.example.analyzer.visitor.InheritanceVisitor;
import org.example.analyzer.visitor.OverrideVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class Analyzer {
    private Map<String, ClassMetrics> classMetircsMap = new HashMap<>();

    private InheritanceVisitor inheritanceVisitor = new InheritanceVisitor();
    private OverrideVisitor overrideVisitor = new OverrideVisitor();

    public JarMetrics analyze(String jarPath) {
        try (JarFile jar = new JarFile(jarPath)) {
            jar.stream()
                    .filter(entry -> entry.getName().endsWith(".class"))
                    .forEach(entry -> {
                        try (InputStream is = jar.getInputStream(entry)) {
                            ClassReader reader = new ClassReader(is);
                            processClass(reader);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return calcMetrics();
    }

    private void processClass(ClassReader reader) {
        reader.accept(inheritanceVisitor, 0);
        reader.accept(overrideVisitor, 0);

        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        ABCVisitor abcVisitor = new ABCVisitor();
        reader.accept(abcVisitor, 0);

        ClassMetrics classMetrics = new ClassMetrics();

        classMetrics.assignments = abcVisitor.getAssignments();
        classMetrics.branches = abcVisitor.getBranches();
        classMetrics.conditionals = abcVisitor.getConditionals();
        classMetrics.fields = node.fields.size();

        classMetircsMap.put(node.name, classMetrics);
    }

    private JarMetrics calcMetrics() {
        Long totalAssigments = Long.valueOf(0);
        Long totalBranches = Long.valueOf(0);
        Long totalConditions = Long.valueOf(0);

        Integer totalFields = 0;

        for (ClassMetrics classMetrics : classMetircsMap.values()) {
            totalFields += classMetrics.fields;
            totalAssigments += classMetrics.assignments;
            totalBranches += classMetrics.branches;
            totalConditions += classMetrics.conditionals;
        }

        Double ABC = Math.sqrt(
                totalAssigments * totalAssigments + totalBranches * totalBranches + totalConditions + totalConditions);

        return new JarMetrics(
                inheritanceVisitor.getMaxInheritanceDepth(),
                inheritanceVisitor.getAvgInheritanceDepth(),
                ABC,
                overrideVisitor.getAvgMethodOverrides(),
                Double.valueOf(totalFields) / classMetircsMap.size());
    }
}
