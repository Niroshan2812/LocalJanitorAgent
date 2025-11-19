package org.niroshan.localjanitoragent.Tools;

public interface AgentTool {
    // name
    String getName();
    // what needs to be done
    String getDescription();
    // the action
    String execute(String argument);

}
