package org.niroshan.localjanitoragent.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrainClient {

    private ChatLanguageModel model;

    public BrainClient() {
        // Default constructor, model will be configured later
    }

    public void configure(String provider, String apiKey, String url, String modelName) {
        System.out.println("Configuring AI Model: " + provider + " (" + modelName + ")");
        switch (provider.toLowerCase()) {
            case "gemini":
                this.model = GoogleAiGeminiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .temperature(0.7)
                        .build();
                break;
            case "gpt":
                this.model = dev.langchain4j.model.openai.OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .temperature(0.7)
                        .build();
                break;
            case "ollama":
                String baseUrl = (url != null && !url.isEmpty()) ? url : "http://localhost:11434";
                this.model = dev.langchain4j.model.ollama.OllamaChatModel.builder()
                        .baseUrl(baseUrl)
                        .modelName(modelName)
                        .temperature(0.7)
                        .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }

    public String ask(String prompt) {
        if (model == null) {
            System.err.println("AI Model not configured!");
            return null;
        }
        try {
            System.out.println("Generating response...");
            long start = System.currentTimeMillis();

            String response = model.generate(prompt);

            long duration = System.currentTimeMillis() - start;
            System.out.println("Generation took: " + duration + "ms");

            return response;
        } catch (Exception e) {
            System.err.println("Gemini Generation error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}
