package org.niroshan.localjanitoragent.Tools;

import org.springframework.stereotype.Component;

@Component
public class ReportTool implements AgentTool {

    @Override
    public String getName() {
        return "report_reason";
    }

    @Override
    public String getDescription() {
        return "Use this to explain why no actions are needed or if the goal cannot be fulfilled. Usage: report_reason 'No .png files found'";
    }

    @Override
    public String execute(String argument) {
        return "Report: " + argument;
    }
}
