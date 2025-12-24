package org.niroshan.localjanitoragent.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.niroshan.localjanitoragent.Model.ToolCommand;
import org.niroshan.localjanitoragent.Service.BrainClient;
import org.niroshan.localjanitoragent.Service.PromptService;
import org.niroshan.localjanitoragent.Service.SafetyService;
import org.niroshan.localjanitoragent.Service.UserInterfaceService;
import org.niroshan.localjanitoragent.Tools.AgentTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JanitorAgent {

    private final BrainClient brainClient;
    private final Map<String, AgentTool> tools;
    private final ObjectMapper objectMapper;
    private final PromptService promptService;
    private final SafetyService safetyService;
    private final UserInterfaceService uiService;

    public JanitorAgent(BrainClient brainClient, List<AgentTool> toolList, ObjectMapper objectMapper,
            PromptService promptService, SafetyService safetyService, UserInterfaceService uiService) {
        this.brainClient = brainClient;
        this.tools = toolList.stream().collect(Collectors.toMap(AgentTool::getName, t -> t));
        this.objectMapper = objectMapper;
        this.promptService = promptService;
        this.safetyService = safetyService;
        this.uiService = uiService;
    }

    public void configure(String provider, String key, String url, String modelName) {
        brainClient.configure(provider, key, url, modelName);
    }

    public void execute(String userPath, String goal) {

        uiService.print("Starting Janitor Agent ...");

        if (userPath != null && !userPath.isEmpty()) {
            try {
                safetyService.setRootPath(userPath);
            } catch (IllegalArgumentException e) {
                uiService.print("Error: " + e.getMessage());
                uiService.print("Falling back to default sandbox.");
            }
        }

        uiService.print("Goal: " + goal);

        // Construct System prompt
        String toolDescription = tools.values().stream()
                .map(t -> "- " + t.getName() + ": " + t.getDescription())
                .collect(Collectors.joining("\n"));
        String currentFiles = tools.get("list_files").execute("");

        if (currentFiles.equals("Directory not found")) {
            uiService.print("No files found.");
            return;
        }
        // Get prompt
        String prompt = promptService.buildSystemPrompt(toolDescription, currentFiles, goal);

        uiService.print("Asking AI...");
        uiService.updateStatus("Thinking \uD83E\uDDE0");
        String responce = brainClient.ask(prompt);

        if (responce == null || responce.trim().isEmpty()) {
            uiService.print("Model return a empty response.");
            return;
        }

        // Execution Logic
        try {
            // clean up responce
            String cleanJson = responce.replace("```json", "").replace("```", "").trim();

            // Check if model return an empty list or nothing
            if (cleanJson.equals("[]")) {
                uiService.print("Model return a no work needed. ");
                return;
            }
            // Prepare as a List
            List<ToolCommand> commands = objectMapper.readValue(cleanJson, new TypeReference<List<ToolCommand>>() {
            });
            uiService.print("Received " + commands.size() + " Commands");
            uiService.updateStatus("Acting \u2699\uFE0F");
            // Execute all command in one quick loop
            for (ToolCommand cmd : commands) {
                String toolName = cmd.tool();
                AgentTool toolToRun = tools.get(toolName);

                // Fallback: Strip leading underscore if present (Model Hallucination Fix)
                if (toolToRun == null && toolName.startsWith("_")) {
                    toolName = toolName.substring(1);
                    toolToRun = tools.get(toolName);
                }

                if (toolToRun != null) {
                    uiService.print(">> Executing " + toolName + " args " + cmd.args());
                    String result = toolToRun.execute(cmd.args());
                    uiService.print("<< " + toolName + " result " + result);
                } else {
                    uiService.print("Unknown Tool " + cmd.tool());
                }
            }
            uiService.updateStatus("Done \u2705");
        } catch (Exception e) {
            uiService.print("JSON Error (Did the model return array ? ) " + e.getMessage());
            uiService.print("Raw response: " + responce);
        }
    }
}
