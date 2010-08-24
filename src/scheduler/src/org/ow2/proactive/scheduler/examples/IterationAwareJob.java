package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class IterationAwareJob extends JavaExecutable {

    private String report = "";

    @Override
    public void init(Map<String, Serializable> args) {
        for (Entry<String, Serializable> entry : args.entrySet()) {
            report += "arg " + entry.getKey() + " " + entry.getValue() + ":";
        }
    }

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        report += "prop it " + System.getProperty("pas.task.iteration") + ":";
        report += "prop dup " + System.getProperty("pas.task.duplication") + ":";

        return report;
    }

}
