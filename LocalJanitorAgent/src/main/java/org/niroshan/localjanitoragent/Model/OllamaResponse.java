package org.niroshan.localjanitoragent.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
Json parser for the project ---


This for help map the JSON data.


 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaResponse(
    String model,
    Message message,
    boolean done
            ){
    public record Message(String role, String content){}
}
