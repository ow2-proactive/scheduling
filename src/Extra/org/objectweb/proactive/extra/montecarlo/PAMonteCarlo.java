package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.core.EngineTaskAdapter;
import org.objectweb.proactive.extra.montecarlo.core.MCMemoryFactory;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;


/**
 * PAMonteCarlo
 *
 * @author The ProActive Team
 */
public class PAMonteCarlo {

    ProActiveMaster master = null;

    public PAMonteCarlo(URL descriptorURL, String masterVNName, String workersVNName)
            throws ProActiveException {
        if (masterVNName != null) {
            // Remote master
            master = new ProActiveMaster<EngineTaskAdapter, Serializable>(descriptorURL, masterVNName,
                new MCMemoryFactory());
        } else {
            // Local master
            master = new ProActiveMaster<EngineTaskAdapter, Serializable>(new MCMemoryFactory());
        }
        master.addResources(descriptorURL, workersVNName);
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
    }

    public PAMonteCarlo(String schedulerLocation, String user, String password) throws ProActiveException {

        master = new ProActiveMaster<EngineTaskAdapter, Serializable>(new MCMemoryFactory());
        master.setInitialTaskFlooding(Integer.MAX_VALUE);
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
        master.addResources(schedulerLocation, user, password);

    }

    public Serializable run(EngineTask toplevelTask) throws TaskException {
        ArrayList<EngineTaskAdapter> singletask = new ArrayList<EngineTaskAdapter>(1);
        singletask.add(new EngineTaskAdapter(toplevelTask));
        master.solve(singletask);
        return master.waitOneResult();
    }

}
