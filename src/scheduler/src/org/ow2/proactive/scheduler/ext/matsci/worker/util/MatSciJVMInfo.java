package org.ow2.proactive.scheduler.ext.matsci.worker.util;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.scheduler.ext.common.util.IOTools;
import org.ow2.proactive.scheduler.ext.matsci.worker.MatSciWorker;


/**
 * MatSciJVMInfo
 *
 * @author The ProActive Team
 */
public class MatSciJVMInfo<W extends MatSciWorker, C extends MatSciEngineConfig> {
    IOTools.LoggingThread esLogger = null;
    IOTools.LoggingThread isLogger = null;
    IOTools.RedirectionThread ioThread = null;
    Node node = null;
    Integer deployID = null;
    Process process = null;
    W worker = null;

    public C getConfig() {
        return config;
    }

    public void setConfig(C config) {
        this.config = config;
    }

    C config;

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

    public W getWorker() {
        return worker;
    }

    public void setWorker(W worker) {
        this.worker = worker;
    }
}
