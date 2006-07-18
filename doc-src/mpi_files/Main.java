import org.apache.log4j.Logger;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.mpi.MPI;
import org.objectweb.proactive.mpi.MPISpmd;
import org.objectweb.proactive.mpi.control.ProActiveMPI;

import java.util.ArrayList;
import java.util.Vector;


public class Main {
    public static void main(String[] args) {
        Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

        if (args.length != 1) {
            logger.error("Usage: java " + Main.class.getName() +
                " <deployment file>");
            System.exit(0);
        }

        ProActiveConfiguration.load();

        VirtualNode jacobiOnNina;
        VirtualNode jacobiOnNef;
        ProActiveDescriptor pad = null;

        try {
            pad = ProActive.getProactiveDescriptor("file:" + args[0]);

            // gets virtual node 
            jacobiOnNef = pad.getVirtualNode("Cluster_Nef");
            jacobiOnNina = pad.getVirtualNode("Cluster_Nina");
            
            MPISpmd nefMPISpmd = MPI.newMPISpmd(jacobiOnNef);
            MPISpmd ninaMPISpmd = MPI.newMPISpmd(jacobiOnNina);

            ArrayList my_jobs = new ArrayList();
            my_jobs.add(nefMPISpmd);
            my_jobs.add(ninaMPISpmd);
            ProActiveMPI.deploy(my_jobs);

        } catch (ProActiveException e) {
            e.printStackTrace();
            logger.error("Pb when reading descriptor");
        }
    }
}
