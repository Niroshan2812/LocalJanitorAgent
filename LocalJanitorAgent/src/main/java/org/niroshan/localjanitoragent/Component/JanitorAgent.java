package org.niroshan.localjanitoragent.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.niroshan.localjanitoragent.Model.ToolCommand;
import org.niroshan.localjanitoragent.Service.BrainClient;
import org.niroshan.localjanitoragent.Service.PromptService;
import org.niroshan.localjanitoragent.Service.SafetyService;
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
    private final PromptService promptService;
    private final SafetyService safetyService;

    public JanitorAgent(BrainClient brainClient, List<AgentTool> toolList, ObjectMapper objectMapper,
            PromptService promptService, SafetyService safetyService) {
        this.brainClient = brainClient;
        this.tools = toolList.stream().collect(Collectors.toMap(AgentTool::getName, t -> t));
        this.objectMapper = objectMapper;
        this.promptService = promptService;
        this.safetyService = safetyService;
    }

    @Override
    public void run(String... args) {

        System.out.println("Starting Janitor Agent ...");

        // Ask for working directory
        System.out.print("Enter target directory path (Press Enter for default): ");
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        String userPath = scanner.nextLine().trim();

        if (!userPath.isEmpty()) {
            safetyService.setRootPath(userPath);
        }

        // Wait, I missed that SafetyService is NOT injected into JanitorAgent.
        // It's in the constructor but not stored as a field? Checking constructor...
        // Constructor has PromptService, BrainClient, Tools.
        // I need to add SafetyService to JanitorAgent first.

        String goal = (args.length > 0) ? args[0] : "Move ALL files ending in .png to a folder named 'Images'.";
        System.out.println("Goal: " + goal);

        // Construct System prompt
        String toolDescription = tools.values().stream()
                .map(t -> "_ " + t.getName() + ": " + t.getDescription())
                .collect(Collectors.joining("\n"));
        String currentFiles = tools.get("list_files").execute("");

        if (currentFiles.equals("Directory not found")) {
            System.out.println("No files found.");
            return;
        }
        // Get prompt
        String prompt = promptService.buildSystemPrompt(toolDescription, currentFiles, goal);
        // ask
        // System.out.println("Loaded Tools: " + tools.keySet());

        // -----------------------------------------------------------------------------------------------------------------

        System.out.println("Asking");
        String responce = brainClient.ask(prompt);

        if (responce == null || responce.trim().isEmpty()) {
            System.out.println("Model return a empty response.");
            return;
        }

        // Execution Logic
        try {
            // clean up responce
            String cleanJson = responce.replace("```json", "").replace("```", "").trim();

            // Check if model return an empty list or nothing
            if (cleanJson.equals("[]")) {
                System.out.println("Model return a no work needed. ");
                return;
            }
            // Prepare as a List
            List<ToolCommand> commands = objectMapper.readValue(cleanJson, new TypeReference<List<ToolCommand>>() {
            });
            System.out.println("Commands: " + commands);
            System.out.println("Received " + commands.size() + "Commands");
            // Execute all command in one quick loop
            for (ToolCommand cmd : commands) {
                AgentTool toolToRun = tools.get(cmd.tool());
                if (toolToRun != null) {
                    System.out.println(">> Executing " + cmd.tool() + " args " + cmd.args());
                    String result = toolToRun.execute(cmd.args());
                    System.out.println("<< " + cmd.tool() + " result " + result);
                } else {
                    System.out.println("Unknown Tool " + cmd.tool());
                }
            }
        } catch (Exception e) {
            System.out.println("JSON Error (Did the model return array ? ) " + e.getMessage());
            System.out.println("Row response: " + responce);
        }
    }
}
