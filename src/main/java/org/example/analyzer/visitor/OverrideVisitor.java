package org.example.analyzer.visitor;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ASM8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class OverrideVisitor extends ClassVisitor {
    public OverrideVisitor() {
        super(ASM8);
    }

    static class Method {
        final String name;
        final String argumentTypes;
        boolean overrideFound = false;

        Method(String name, String argumentTypes) {
            this.name = name;
            this.argumentTypes = argumentTypes;
        }
    }

    private final Map<String, String> classSuperClass = new HashMap<>();
    private final Map<String, String[]> classInterfaces = new HashMap<>();
    private final Map<String, List<Method>> classMethods = new HashMap<>();

    private String className;

    public Double getAvgMethodOverrides() {
        Map<String, Integer> overridenMethodCounts = getOverridenMethodCounts();
        Integer count = overridenMethodCounts.size();
        Integer sum = overridenMethodCounts.values().stream().reduce(Integer::sum).orElse(count);
        return Double.valueOf(sum) / count;
    }

    private Map<String, Integer> getOverridenMethodCounts() {
        Map<String, Integer> result = new HashMap<>();

        for (String className : classMethods.keySet()) {
            Integer inheritedOverridenCount = inheritedOverridenMethodCount(className, classSuperClass.get(className),
                    0);
            Integer implementedMethodCount = implementedMethodCount(className);

            result.put(className, inheritedOverridenCount + implementedMethodCount);
        }

        classMethods.values().stream()
                .flatMap(list -> list.stream())
                .forEach(method -> method.overrideFound = false);

        return result;
    }

    private Integer implementedMethodCount(String className) {
        String[] interfaces = classInterfaces.get(className);
        if (interfaces == null || interfaces.length == 0) {
            return 0;
        }

        Integer result = 0;
        for (Method method : classMethods.get(className)) {
            if (method.overrideFound) {
                continue;
            }

            for (String interfaceName : interfaces) {
                List<Method> interfaceMethods = classMethods.get(interfaceName);
                if (interfaceMethods == null) {
                    continue;
                }

                for (Method interfaceMethod : interfaceMethods) {
                    if (method.equals(interfaceMethod)) {
                        result++;
                        method.overrideFound = true;
                    }
                }
            }
        }

        return result;
    }

    private Integer inheritedOverridenMethodCount(String className, String superName, Integer result) {
        if (superName == null) {
            return result;
        }

        List<Method> superMethods = classMethods.get(superName);
        if (superMethods == null) {
            return result;
        }

        for (Method method : classMethods.get(className)) {
            if (method.overrideFound) {
                continue;
            }

            for (Method superMethod : superMethods) {
                if (method.equals(superMethod)) {
                    result++;
                    method.overrideFound = true;
                }
            }
        }

        return inheritedOverridenMethodCount(className, classSuperClass.get(superName), result);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        className = name;

        classMethods.put(name, new ArrayList<>());

        if ((access & ACC_INTERFACE) == ACC_INTERFACE) {
            return;
        }

        classInterfaces.put(className, interfaces);
        classSuperClass.put(className, superName);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return null;
        }

        if ((access & ACC_PRIVATE) == ACC_PRIVATE) {
            return null;
        }

        var method = new Method(name, Arrays.toString(Type.getArgumentTypes(desc)));
        classMethods.get(className).add(method);

        return null;
    }
}