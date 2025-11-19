package org.niroshan.localjanitoragent.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.niroshan.localjanitoragent.Model.ToolCommand;
import org.niroshan.localjanitoragent.Service.BrainClient;
import org.niroshan.localjanitoragent.Tools.AgentTool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JanitorAgent implements CommandLineRunner {

    private final BrainClient brainClient;
    private final Map<String, AgentTool> tools;
    private final ObjectMapper objectMapper;



    public JanitorAgent(BrainClient brainClient, List<AgentTool> toolList, ObjectMapper objectMapper) {
        this.brainClient = brainClient;
        this.tools = toolList.stream().collect(Collectors.toMap(AgentTool::getName, t -> t));
        this.objectMapper = objectMapper;
    }


    @Override
    public void run(String... args){

        System.out.println("Starting Janitor Agent ...");

        // Construct System prompt
        String toolDescription = tools.values().stream()
                .map(t -> "_ " + t.getName()+ ": "+ t.getDescription())
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are file manager assistent
                you have access to there tools:
                %s
                
                My current files are: [Files will be listed here in real app]
                Goal: Clean up my folder.
                Respond Only in this JSON format:
                
                {"tool": "tool_name", "args": "arguments"}
                """.formatted(toolDescription);

        // ask
        System.out.println("Asking");
       String responce =  brainClient.ask(prompt);
       System.out.println("Brain row output: "+ responce);

       // Execution Logic

        try{
            // clean up responce
            String cleanJson = responce.replace("```json", "").replace("```","").trim();

            // ConverJson into Java Object

            ToolCommand command =  objectMapper.readValue(cleanJson, ToolCommand.class);

            // find the tool
            AgentTool toolToRun = tools.get(command.tool());

            if(toolToRun != null){
                System.out.println("Executing tool: "+ command.tool());

                // run the tool
                String result = toolToRun.execute(command.args());
                System.out.println("Result: "+result);
            }else{
                System.out.println("No tool to be executed: "+ command.tool());
            }



        } catch (Exception e) {
            System.out.println("Error: "+ e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
