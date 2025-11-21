package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ListFilesTool  implements AgentTool{

    private final SafetyService safetyService;

    public ListFilesTool(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @Override
    public String getName() {
        return "list_files";
    }

    @Override
    public String getDescription() {
        return "Lists all files in a given directory";
    }

    @Override
    public String execute(String argument) {
        File folder = safetyService.getRoot().toFile();
        //System.out.println("folder: " + folder.getAbsolutePath());
        if (!folder.exists()) {
            return "Directory not found";
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return "Directory is empty";
        }
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            if (file.isFile()){
             long sizeInMb = file.length()/(1024*1024);
             sb.append(file.getName()).append("(").append(sizeInMb).append("MB)");
            }
        }
        return sb.toString();
    }
}
