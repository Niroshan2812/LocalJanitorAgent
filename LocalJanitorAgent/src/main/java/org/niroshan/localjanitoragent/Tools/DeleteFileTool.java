package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.niroshan.localjanitoragent.Service.UserInterfaceService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class DeleteFileTool implements AgentTool {

    private final SafetyService safetyService;
    private final UserInterfaceService uiService;

    public DeleteFileTool(SafetyService safetyService, UserInterfaceService uiService) {
        this.safetyService = safetyService;
        this.uiService = uiService;
    }

    @Override
    public String getName() {
        return "delete_file";
    }

    @Override
    public String getDescription() {
        return "Deletes a file with interactive confirmation. Usage: delete_file 'filename'";
    }

    @Override
    public String execute(String argument) {
        String filename = argument.trim();
        if (!safetyService.isSafe(filename)) {
            return "Error: Access denied to " + filename;
        }

        File file = safetyService.getRoot().resolve(filename).toFile();
        if (!file.exists()) {
            return "Error: File not found: " + filename;
        }

        uiService.print("---------------------------------------------------------");
        uiService.print("ALERT: Request to DELETE file: " + file.getAbsolutePath());

        String choice = uiService.ask("Do you want to: [y] DELETE permanently, [n] Skip, [m] MOVE? Choice [y/n/m]:")
                .toLowerCase();

        switch (choice) {
            case "y":
                try {
                    boolean deleted = file.delete();
                    if (deleted) {
                        uiService.notifyFileChange(file.getParent());
                        return "Deleted " + filename;
                    } else {
                        return "Failed to delete " + filename;
                    }
                } catch (Exception e) {
                    return "Error deleting: " + e.getMessage();
                }
            case "n":
                return "Skipped deletion of " + filename;
            case "m":
                String destFolder = uiService.ask("Enter destination folder name (relative to root):").trim();

                if (!safetyService.isSafe(destFolder)) {
                    return "Error: Unsafe destination " + destFolder;
                }

                Path sourcePath = file.toPath();
                Path destPath = safetyService.getRoot().resolve(destFolder);
                File destDir = destPath.toFile();

                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                try {
                    Files.move(sourcePath, destPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                    return "Moved " + filename + " to " + destFolder;
                } catch (Exception e) {
                    return "Error moving: " + e.getMessage();
                }

            default:
                return "Action cancelled (Invalid input)";
        }
    }
}
