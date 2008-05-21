package org.objectweb.proactive.p2p.daemon;

import org.jvnet.winp.WinProcess;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.util.RemoteProcessMessageLogger;


public class DaemonJVMProcess extends JVMProcessImpl {

    protected WinProcess wp;

    public DaemonJVMProcess(RemoteProcessMessageLogger messageLogger) {
        super(messageLogger);
    }

    protected void internalStartProcess(String commandToExecute) throws java.io.IOException {
        super.internalStartProcess(commandToExecute);
        if (externalProcess != null)
            this.wp = new WinProcess(externalProcess);
    }

    protected void internalStopProcess() {
        if (externalProcess != null && wp != null) {
            wp.killRecursively();
        }

        super.internalStopProcess();
    }

}
