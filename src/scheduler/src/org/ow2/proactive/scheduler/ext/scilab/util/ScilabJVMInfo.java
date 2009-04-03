package org.ow2.proactive.scheduler.ext.scilab.util;

import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.scilab.AOScilabWorker;
import org.objectweb.proactive.core.node.Node;


/**
 * ScilabJVMInfo
 *
 * @author The ProActive Team
 */
public class ScilabJVMInfo {
    IOTools.LoggingThread esLogger = null;

    IOTools.LoggingThread isLogger = null;

    IOTools.RedirectionThread ioThread = null;

    AOScilabWorker worker = null;

    Node node = null;

    Integer deployID = null;

    Process process = null;

    public ScilabJVMInfo() {

    }

    public ScilabJVMInfo(IOTools.LoggingThread esLogger, IOTools.LoggingThread isLogger,
            IOTools.RedirectionThread ioThread, AOScilabWorker worker, Node node, Integer deployID,
            Process process) {
        this.esLogger = esLogger;
        this.isLogger = isLogger;
        this.ioThread = ioThread;
        this.worker = worker;
        this.node = node;
        this.deployID = deployID;
        this.process = process;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public IOTools.LoggingThread getEsLogger() {
        return esLogger;
    }

    public void setEsLogger(IOTools.LoggingThread esLogger) {
        this.esLogger = esLogger;
    }

    public IOTools.LoggingThread getIsLogger() {
        return isLogger;
    }

    public void setIsLogger(IOTools.LoggingThread isLogger) {
        this.isLogger = isLogger;
    }

    public IOTools.RedirectionThread getIoThread() {
        return ioThread;
    }

    public void setIoThread(IOTools.RedirectionThread ioThread) {
        this.ioThread = ioThread;
    }

    public AOScilabWorker getWorker() {
        return worker;
    }

    public void setWorker(AOScilabWorker worker) {
        this.worker = worker;
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
}
