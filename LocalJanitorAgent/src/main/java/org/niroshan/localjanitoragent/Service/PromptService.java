package org.niroshan.localjanitoragent.Service;

import org.springframework.stereotype.Service;

@Service
public class PromptService {

  public String buildSystemPrompt(String toolDescription, String fileList, String goal) {
    // Ask for Jason Array
    return """
        You are a Janitor Agent.

         Tools Available:
         %s

         Current Files:
         %s

         Goal: %s

         CRITICAL INSTRUCTION:
         - You must respond with a JSON ARRAY of commands.
         - Do not move the same file twice.
         - If no files need moving, or the goal is already met, use 'report_reason' to explain why. Do NOT return an empty array.

         Example Response:
         [
           { "tool": "move_file", "args": "a.png|Images" },
           { "tool": "move_file", "args": "b.png|Images" }
         ]
         """
        .formatted(toolDescription, fileList, goal);
  }
}
