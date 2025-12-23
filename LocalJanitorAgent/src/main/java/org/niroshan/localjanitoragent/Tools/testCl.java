package org.niroshan.localjanitoragent.Tools;

import org.niroshan.localjanitoragent.Service.SafetyService;

public class testCl {
    public static void main(String[] args) {

        ListFilesTool tool = new ListFilesTool(new SafetyService("C:/Test"));
        System.out.println(tool.execute("listFiles"));
    }
}
