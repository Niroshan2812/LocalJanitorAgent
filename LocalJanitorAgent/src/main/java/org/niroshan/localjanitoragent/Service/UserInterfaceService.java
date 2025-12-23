package org.niroshan.localjanitoragent.Service;

import java.util.List;

public interface UserInterfaceService {
    void print(String message);

    String ask(String question);

    String askChoice(String question, List<String> options);
}
