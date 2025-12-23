package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class MoveFileTool implements AgentTool {

    private final SafetyService safetyService;

    public MoveFileTool(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @Override
    public String getName() {
        return "move_file";
    }

    @Override
    public String getDescription() {
        return "Moves a file to a specific folder. Creates folder if missing. IMPORTANT: Use '|' to separate filename and destination. Format: 'filename.ext|folder_name'. Example: 'image.png|Backup'";
    }

    @Override
    public String execute(String argument) {
        // Expect arguments like myphoto.png|vacation_pic
        String[] parts = argument.split("\\|");
        if (parts.length != 2) {
            return "Error: Invalid format use 'Filename | destination'";
        }
        String filename = parts[0].trim();
        String destFolderName = parts[1].trim();

        // validate source
        // 1. Try exact match
        File folder = safetyService.getRoot().toFile();
        File sourceFile = new File(folder, filename);

        // 2. Fuzzy match if exact fails
        if (!sourceFile.exists()) {
            File[] allFiles = folder.listFiles();
            if (allFiles != null) {
                File bestMatch = null;
                int matches = 0;
                String lowerName = filename.toLowerCase();

                for (File f : allFiles) {
                    if (f.getName().toLowerCase().contains(lowerName)) {
                        bestMatch = f;
                        matches++;
                    }
                }

                if (matches == 1) {
                    sourceFile = bestMatch;
                    filename = sourceFile.getName(); // Update filename for safety check
                } else if (matches > 1) {
                    return "Error: Ambiguous filename '" + filename + "'. Multiple matches found.";
                }
            }
        }

        if (!safetyService.isSafe(filename)) {
            return "Error: Access denied to source file ";
        }

        if (!sourceFile.exists()) {
            return "Error: File '" + filename + "' does not exist (and no unique partial match found)";
        }

        // validate
        if (!safetyService.isSafe(destFolderName)) {
            return "Error: Access denied to Destination ";
        }
        Path destPath = safetyService.getRoot().resolve(destFolderName);
        File destDir = destPath.toFile();

        // Create folder if not exist
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // move
        try {
            Files.move(sourceFile.toPath(), destPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "Successfully moved" + filename + " file to " + destFolderName;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }

    }
}
