package org.niroshan.localjanitoragent.Gui;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.DirectoryChooser;
import java.time.LocalTime;
import java.util.Optional;
import org.niroshan.localjanitoragent.Component.JanitorAgent;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MainWindowController {

    @FXML
    private Label greetingLabel;
    @FXML
    private TreeView<String> fileTree;
    @FXML
    private TextField pathField;
    @FXML
    private TextField goalField;
    @FXML
    private TextArea logArea;
    @FXML
    private Button runButton;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<String> quickActionCombo;

    private final JanitorAgent agent;
    private final GuiUIService guiUIService;

    public MainWindowController(JanitorAgent agent, GuiUIService guiUIService) {
        this.agent = agent;
        this.guiUIService = guiUIService;
    }

    @FXML
    public void initialize() {
        // Connect the logger
        guiUIService.setLogCallback(msg -> logArea.appendText(msg));

        // Configure Model on Startup
        javafx.application.Platform.runLater(this::showModelConfigDialog);

        // Set Greeting
        String userName = System.getProperty("user.name");
        LocalTime now = LocalTime.now();
        String timeGreeting;
        if (now.getHour() < 12) {
            timeGreeting = "Good Morning";
        } else if (now.getHour() < 18) {
            timeGreeting = "Good Afternoon";
        } else {
            timeGreeting = "Good Evening";
        }
        greetingLabel.setText(String.format("%s, %s", timeGreeting, userName));

        // Quick Actions
        quickActionCombo.getItems().addAll(
                "Delete all .tmp files",
                "Organize files by extension",
                "Move all images to 'Images' folder",
                "List all files larger than 10MB");
        quickActionCombo.setOnAction(e -> {
            String selected = quickActionCombo.getValue();
            if (selected != null) {
                goalField.setText(selected);
            }
        });

        // Callbacks
        guiUIService.setStatusCallback(status -> statusLabel.setText("Status: " + status));
        guiUIService.setFileChangeCallback(path -> {
            // Refresh tree on file change
            // For simplicity, we refresh the root or the modified path's parent
            // Since we don't track TreeItems easily, refreshing the whole view is safest
            // for now
            // or better, if we have the root path.
            if (pathField.getText() != null) {
                refreshTree(new File(pathField.getText()));
            }
        });

        // Set default path safely
        String userHome = System.getProperty("user.home");
        File defaultSandbox = new File(userHome, "Janitor_Sandbox");

        // Ensure default directory exists so TreeView shows up
        if (!defaultSandbox.exists()) {
            defaultSandbox.mkdirs();
        }

        pathField.setText(defaultSandbox.getAbsolutePath());

        refreshTree(defaultSandbox); // Initial Load

        // Also redirect System.out roughly? No, just rely on service.
        logArea.appendText("Welcome only agent logs will appear here.\n");
    }

    @FXML
    public void handleBrowse() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Working Directory");
        File selected = dc.showDialog(runButton.getScene().getWindow());
        if (selected != null) {
            pathField.setText(selected.getAbsolutePath());
            refreshTree(selected);
        }
    }

    private void refreshTree(File rootDir) {
        if (!rootDir.exists()) {
            fileTree.setRoot(null);
            return;
        }
        TreeItem<String> rootItem = new TreeItem<>(rootDir.getName());
        rootItem.setExpanded(true);
        populateTree(rootItem, rootDir);
        fileTree.setRoot(rootItem);
    }

    private void populateTree(TreeItem<String> parentItem, File parentFile) {
        File[] files = parentFile.listFiles();
        if (files != null) {
            for (File file : files) {
                TreeItem<String> item = new TreeItem<>(file.getName());
                parentItem.getChildren().add(item);
                if (file.isDirectory()) {
                    populateTree(item, file);
                }
            }
        }
    }

    @FXML
    public void handleRun() {
        String path = pathField.getText();
        String goal = goalField.getText();

        if (goal.isEmpty()) {
            logArea.appendText("Error: Please specify a goal.\n");
            return;
        }

        runButton.setDisable(true);
        logArea.appendText("Starting Agent...\n");

        // Run in background thread to not freeze UI
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // We need to refactor Agent to accept arguments directly
                // For now, we simulate args
                try {
                    // Set manual path input logic for the agent via a hack or refactor?
                    // Best is to Refactor JanitorAgent to explicitly take path and goal.

                    // We will assume JanitorAgent has a new method execute(path, goal)
                    agent.execute(path, goal);
                } catch (Exception e) {
                    e.printStackTrace(); // print to console
                    guiUIService.print("Error: " + e.getMessage());
                }
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            runButton.setDisable(false);
            logArea.appendText("Agent Finished.\n");
            // refreshTree(new File(pathField.getText())); // handled by callback now, but
            // good to keep as final sync
        });

        task.setOnFailed(e -> {
            runButton.setDisable(false);
            logArea.appendText("Agent Failed: " + task.getException().getMessage() + "\n");
        });

        new Thread(task).start();
    }

    private void showModelConfigDialog() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("AI Model Configuration");
        dialog.setHeaderText("Select and Configure AI Model");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> modelCombo = new ComboBox<>();
        modelCombo.getItems().addAll("Gemini", "GPT", "Ollama");
        modelCombo.setValue("Gemini");

        TextField modelNameField = new TextField();
        modelNameField.setPromptText("Model Name (e.g. gemini-1.5-flash)");
        modelNameField.setText("gemini-1.5-flash");

        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("API Key");

        TextField urlField = new TextField();
        urlField.setPromptText("URL (e.g., http://localhost:11434)");
        urlField.setText("http://localhost:11434");
        urlField.setDisable(true); // Default disabled for Gemini/GPT

        modelCombo.setOnAction(e -> {
            String selected = modelCombo.getValue();
            if ("Ollama".equals(selected)) {
                apiKeyField.setDisable(true);
                urlField.setDisable(false);
                modelNameField.setText("Qwen2.5-Coder-0.5B-Instruct");
            } else if ("GPT".equals(selected)) {
                apiKeyField.setDisable(false);
                urlField.setDisable(true);
                modelNameField.setText("gpt-4o");
            } else { // Gemini
                apiKeyField.setDisable(false);
                urlField.setDisable(true);
                modelNameField.setText("gemini-1.5-flash");
            }
        });

        grid.add(new Label("Model Provider:"), 0, 0);
        grid.add(modelCombo, 1, 0);
        grid.add(new Label("Model Name:"), 0, 1);
        grid.add(modelNameField, 1, 1);
        grid.add(new Label("API Key:"), 0, 2);
        grid.add(apiKeyField, 1, 2);
        grid.add(new Label("URL (Ollama):"), 0, 3);
        grid.add(urlField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    agent.configure(modelCombo.getValue(), apiKeyField.getText(), urlField.getText(),
                            modelNameField.getText());
                    logArea.appendText(
                            "Model Configured: " + modelCombo.getValue() + " (" + modelNameField.getText() + ")\n");
                    return true;
                } catch (Exception e) {
                    logArea.appendText("Configuration Error: " + e.getMessage() + "\n");
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait();
    }
}
