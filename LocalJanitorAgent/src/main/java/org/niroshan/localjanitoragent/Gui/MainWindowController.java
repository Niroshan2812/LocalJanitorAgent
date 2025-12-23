package org.niroshan.localjanitoragent.Gui;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;
import org.niroshan.localjanitoragent.Component.JanitorAgent;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MainWindowController {

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
            refreshTree(new File(pathField.getText()));
        });

        task.setOnFailed(e -> {
            runButton.setDisable(false);
            logArea.appendText("Agent Failed: " + task.getException().getMessage() + "\n");
        });

        new Thread(task).start();
    }
}
