package org.niroshan.localjanitoragent.Service;


import org.niroshan.localjanitoragent.Model.OllamaResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;


@Service
public class BrainClient {
    private final RestClient restClient;


    @Value("${agent.model}")
    private String modelName;



    public BrainClient(RestClient.Builder builder,@Value("${agent.brain.url}") String baseUrl){

        // -- Error - GoAway ----fix 7.23 pm
        // config inner Http CLient
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        // Create factory with a long read Timeout
        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        // wait 5 min for AI
        requestFactory.setReadTimeout(Duration.ofMinutes(5));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("ngrok-skip-browser-warning", "true")
                .defaultHeader("User-Agent", "Janitor-Agent-v1")
                .build();
    }

    public String ask (String prompt){
        // prep Json body manually or use a DTO
        var requestBody = Map.of(
                "model",  modelName,
                "messages", List.of(Map.of("role", "user", "content",prompt)),
                "stream", false
        );
        try{
            OllamaResponse response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("ngrok-skip-browser-warning", "true")
                    .body(requestBody)
                    .retrieve()
                    .body(OllamaResponse.class);

            if (response != null && response.message() != null) {

                return response.message().content();

            }
        }catch(Exception e){
            System.out.println("Connection error: "+ e.getMessage());
        }
        return null;
    }




}
