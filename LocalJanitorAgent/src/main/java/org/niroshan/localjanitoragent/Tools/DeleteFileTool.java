package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

@Component
public class DeleteFileTool implements AgentTool {

    private final SafetyService safetyService;

    public DeleteFileTool(SafetyService safetyService) {
        this.safetyService = safetyService;
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

        System.out.println("---------------------------------------------------------");
        System.out.println("ALERT: Request to DELETE file: " + file.getAbsolutePath());
        System.out.println("Do you want to:");
        System.out.println("  [y] DELETE permanently");
        System.out.println("  [n] Skip / Keep");
        System.out.println("  [m] MOVE to another folder");
        System.out.print("Choice [y/n/m]: ");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim().toLowerCase();

        switch (choice) {
            case "y":
                try {
                    boolean deleted = file.delete();
                    return deleted ? "Deleted " + filename : "Failed to delete " + filename;
                } catch (Exception e) {
                    return "Error deleting: " + e.getMessage();
                }
            case "n":
                return "Skipped deletion of " + filename;
            case "m":
                System.out.print("Enter destination folder name (relative to root): ");
                String destFolder = scanner.nextLine().trim();

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
