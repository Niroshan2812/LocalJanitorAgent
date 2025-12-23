package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;

@Component
public class ReadFileTool implements AgentTool {

    private final SafetyService safetyService;

    public ReadFileTool(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @Override
    public String getName() {
        return "read_file";
    }

    @Override
    public String getDescription() {
        return "Reads the first 1KB of a text file. Format args as: 'filename'";
    }

    @Override
    public String execute(String argument) {
        String filename = argument.trim();

        if (!safetyService.isSafe(filename)) {
            return "Error: Access denied to file";
        }

        File file = safetyService.getRoot().resolve(filename).toFile();
        if (!file.exists()) {
            return "Error: File does not exist";
        }
        if (!file.isFile()) {
            return "Error: Path is not a file";
        }

        try {
            // Read max 1024 chars
            String content = Files.readString(file.toPath());
            if (content.length() > 1024) {
                return content.substring(0, 1024) + "... (truncated)";
            }
            return content;
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }
}
