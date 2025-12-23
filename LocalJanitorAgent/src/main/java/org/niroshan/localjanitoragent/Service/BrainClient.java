package org.niroshan.localjanitoragent.Service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BrainClient {

    private final ChatLanguageModel model;

    public BrainClient(@Value("${agent.gemini.key}") String apiKey) {
        this.model = GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .temperature(0.7)
                .build();
    }

    public String ask(String prompt) {
        try {
            System.out.println("Generating response (Gemini Flash)...");
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
