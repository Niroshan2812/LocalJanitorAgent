package org.niroshan.localjanitoragent.Service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Scanner;

@Service
public class ConsoleUIService implements UserInterfaceService {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public String ask(String question) {
        System.out.print(question + " ");
        return scanner.nextLine().trim();
    }

    @Override
    public String askChoice(String question, List<String> options) {
        System.out.println(question);
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.print("Enter choice: ");
        return scanner.nextLine().trim();
    }

    @Override
    public void updateStatus(String status) {
        System.out.println("[STATUS] " + status);
    }

    @Override
    public void notifyFileChange(String path) {
        System.out.println("[FILE CHANGED] " + path);
    }
}
