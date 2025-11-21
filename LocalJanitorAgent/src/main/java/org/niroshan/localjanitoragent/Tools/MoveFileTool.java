package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class MoveFileTool implements AgentTool{

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
        return "Moves a file. Format args as: 'source_filename|destination_folder_name'. Example: 'image.png|Pictures'";
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
        if(!safetyService.isSafe(filename)){
            return "Error: Access denied to source file ";
        }
        File sourceFile = safetyService.getRoot().resolve(filename).toFile();
        if(!sourceFile.exists()){
            return "Error: File does not exist";
        }

        // validate
      if(!safetyService.isSafe(destFolderName)){
          return "Error: Access denied to Destination ";
      }
        Path destPath = safetyService.getRoot().resolve(destFolderName);
        File destDir = destPath.toFile();

        // Create folder if not exist
        if(!destDir.exists()){
            destDir.mkdirs();
        }

        // move
        try{
            Files.move(sourceFile.toPath(), destPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "Successfully moved"+ filename+ " file to "+ destFolderName ;
        }catch(Exception e){
            return "Error: "+e.getMessage();
        }

    }
}
