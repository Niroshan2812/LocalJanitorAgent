package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

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
        System.out.println("folder: " + folder.getAbsolutePath());
        if(!folder.exists()){
            return "Directory not found";
        }
        File[] files = folder.listFiles();
        if(files==null){
            return "Directory is empty";
        }
        return Arrays.stream(files)
                .map(f -> f.getName() + (f.isDirectory()? "/": ""))
                .collect((Collectors.joining(",")));
    }
}
