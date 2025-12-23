package org.niroshan.localjanitoragent.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class SafetyService {
    private Path rootDir;

    public SafetyService(@Value("${agent.fs.root-dir}") String configRootDir) {
        setRootPath(configRootDir);
    }

    public void setRootPath(String path) {
        Path candidatePath = Paths.get(path).toAbsolutePath().normalize();

        // Check against protected system paths
        List<String> protectedEnvVars = Arrays.asList("SystemRoot", "ProgramFiles", "ProgramFiles(x86)");
        for (String envVar : protectedEnvVars) {
            String envValue = System.getenv(envVar);
            if (envValue != null) {
                Path protectedPath = Paths.get(envValue).toAbsolutePath().normalize();
                if (candidatePath.startsWith(protectedPath)) {
                    throw new IllegalArgumentException(
                            "Blocked for safety: " + candidatePath + " is a system directory (" + envVar + ")");
                }
            }
        }

        this.rootDir = candidatePath;
        File folder = this.rootDir.toFile();
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                System.out.println("Created directory: " + folder.getAbsolutePath());
            } else {
                System.err.println("Failed to create directory: " + folder.getAbsolutePath());
            }
        } else {
            System.out.println("Using directory: " + folder.getAbsolutePath());
        }
    }

    public boolean isSafe(String pathString) {
        try {
            Path reqPath = rootDir.resolve(pathString).normalize();
            // check is the requested path is start with the Root_Dir
            return reqPath.startsWith(rootDir);
        } catch (Exception e) {
            System.out.println("Security Check Error: " + e.getMessage());
            return false;
        }
    }

    public Path getRoot() {
        return rootDir;
    }

}
