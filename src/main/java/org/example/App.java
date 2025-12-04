package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.analyzer.Analyzer;
import org.example.analyzer.JarMetrics;

public class App {
    private static final String defaultJar = "src/main/resources/sample.jar";
    private static final String defaultOutput = "./output.json";

    public static void main(String[] args) throws IOException {
        String jar = defaultJar;
        String outFile = defaultOutput;

        if (args.length > 0) {
            jar = args[0];
        }

        if (args.length > 1) {
            outFile = args[1];
        }

        JarMetrics metrics = new Analyzer().analyze(jar);
        ObjectMapper mapper = new ObjectMapper();
        byte[] stringifiedMetrics = mapper.writeValueAsBytes(metrics);
        System.out.write(stringifiedMetrics);
        System.out.println();

        if (outFile != null) {
            try (OutputStream outputStream = new FileOutputStream(outFile)) {
                outputStream.write(stringifiedMetrics);
            }
        }
    }
}
