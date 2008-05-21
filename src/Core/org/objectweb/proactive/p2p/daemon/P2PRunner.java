package org.objectweb.proactive.p2p.daemon;

import java.io.IOException;

import org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger;


public class P2PRunner implements Runner {

    private P2PInformation info;

    private DaemonJVMProcess workerJVM;

    public P2PRunner(P2PInformation info) {
        this.info = info;
    }

    public void run() {
        if (workerJVM == null) {
            workerJVM = new DaemonJVMProcess(new StandardOutputMessageLogger());
            workerJVM.setClassname("org.objectweb.proactive.p2p.service.StartP2PService");

            workerJVM.setParameters(createParameters());

            try {
                workerJVM.startProcess();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String createParameters() {
        StringBuffer buf = new StringBuffer("-s");
        for (String host : info.getPeerList()) {
            buf.append(" ");
            buf.append(host);
        }
        return buf.toString();
    }

    public void stop() {
        System.out.println("================= STOPPING P2P");
        if (workerJVM != null) {
            workerJVM.stopProcess();
            workerJVM = null;
        }
    }

}
