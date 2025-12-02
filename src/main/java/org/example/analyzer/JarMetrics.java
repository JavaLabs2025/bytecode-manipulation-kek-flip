package org.example.analyzer;

public record JarMetrics(
        Integer maxInheritanceDepth,
        Double avgInheritanceDepth,
        Double ABC,
        Double avgOverrides,
        Double avgClassFields) {
}
