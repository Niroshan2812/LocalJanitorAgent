package org.niroshan.localjanitoragent;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalJanitorAgentApplication {

    public static void main(String[] args) {
        javafx.application.Application.launch(org.niroshan.localjanitoragent.Gui.JanitorGUI.class, args);
    }

}
