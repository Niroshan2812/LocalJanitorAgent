package org.niroshan.localjanitoragent.Component;

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

    public JanitorAgent(BrainClient brainClient, List<AgentTool> toolList) {
        this.brainClient = brainClient;
        this.tools = toolList.stream().collect(Collectors.toMap(AgentTool::getName, t -> t));

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
       System.out.println(responce);


    }
}
