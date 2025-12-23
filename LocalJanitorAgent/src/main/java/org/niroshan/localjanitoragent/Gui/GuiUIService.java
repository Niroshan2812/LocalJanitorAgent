package org.niroshan.localjanitoragent.Gui;

import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import org.niroshan.localjanitoragent.Service.UserInterfaceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@org.springframework.context.annotation.Primary
public class GuiUIService implements UserInterfaceService {

    // Simple callback to log to the text area
    private java.util.function.Consumer<String> logCallback;

    public void setLogCallback(java.util.function.Consumer<String> callback) {
        this.logCallback = callback;
    }

    @Override
    public void print(String message) {
        if (logCallback != null) {
            Platform.runLater(() -> logCallback.accept(message + "\n"));
        } else {
            System.out.println(message);
        }
    }

    @Override
    public String ask(String question) {
        // Must run on FX thread and wait for result
        CompletableFuture<String> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Required");
            dialog.setHeaderText(question);
            dialog.setContentText("Value:");
            Optional<String> result = dialog.showAndWait();
            future.complete(result.orElse(""));
        });

        try {
            return future.get(); // Blocking wait
        } catch (InterruptedException | ExecutionException e) {
            return "";
        }
    }

    @Override
    public String askChoice(String question, List<String> options) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(options.get(0), options);
            dialog.setTitle("Choice Required");
            dialog.setHeaderText(question);
            dialog.setContentText("Choose:");
            Optional<String> result = dialog.showAndWait();
            future.complete(result.orElse(""));
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "";
        }
    }
}
