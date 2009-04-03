package org.ow2.proactive.scheduler.ext.matlab.util;

import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matlab.AOMatlabWorker;
import org.objectweb.proactive.core.node.Node;


/**
 * MatlabJVMInfo
 *
 * @author The ProActive Team
 */
public class MatlabJVMInfo {

    IOTools.LoggingThread esLogger = null;

    IOTools.LoggingThread isLogger = null;

    IOTools.RedirectionThread ioThread = null;

    AOMatlabWorker worker = null;

    public MatlabJVMInfo() {
    }

    public IOTools.LoggingThread getEsLogger() {

        return esLogger;
    }

    public void setEsLogger(IOTools.LoggingThread esLogger) {
        this.esLogger = esLogger;
    }

    public IOTools.LoggingThread getLogger() {
        return isLogger;
    }

    public void setLogger(IOTools.LoggingThread logger) {
        isLogger = logger;
    }

    public IOTools.RedirectionThread getIoThread() {
        return ioThread;
    }

    public void setIoThread(IOTools.RedirectionThread ioThread) {
        this.ioThread = ioThread;
    }

    public AOMatlabWorker getWorker() {
        return worker;
    }

    public void setWorker(AOMatlabWorker worker) {
        this.worker = worker;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Integer getDeployID() {
        return deployID;
    }

    public void setDeployID(Integer deployID) {
        this.deployID = deployID;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    Node node = null;

    Integer deployID = null;

    Process process = null;
}
