package org.niroshan.localjanitoragent.Service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class SafetyService {
    // provide the test folder

   private final Path ROOT_DIR = Paths.get(System.getProperty("user.home"), "Downloads", "Janitor_Sandbox");


    public boolean isSafe(String pathString){
        try{
            Path reqPath = ROOT_DIR.resolve(pathString).normalize();
            System.out.println(reqPath);
            // check is the requested path is start with the Root_Dir
            return reqPath.startsWith(ROOT_DIR);
        }catch(Exception e){
            System.out.println("This come from root pat : "+ e.getMessage());
            return false;
        }
    }

    public Path getRoot(){
        return ROOT_DIR;
    }

}
