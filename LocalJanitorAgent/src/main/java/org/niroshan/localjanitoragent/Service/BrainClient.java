package org.niroshan.localjanitoragent.Service;


import org.niroshan.localjanitoragent.Model.OllamaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;


@Service
public class BrainClient {
    private final RestClient restClient;


    @Value("${agent.model}")
    private String modelName;



    public BrainClient(RestClient.Builder builder,@Value("${agent.brain.url}") String baseUrl){
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String ask (String prompt){
        // prep Json body manually or use a DTO
        var requestBody = Map.of(
                "model",  modelName,
                "message", List.of(Map.of("role", "user", "content",prompt)),
                "stream", false
        );

    // map responce directly to DTO class
        OllamaResponse response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(OllamaResponse.class);

        if(response != null && response.message() != null){
            return response.message().content();
        }
        return "Null";
    }




}
