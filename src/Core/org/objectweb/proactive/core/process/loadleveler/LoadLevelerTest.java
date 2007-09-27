package org.objectweb.proactive.core.process.loadleveler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class LoadLevelerTest {
    public LoadLevelerTest() {
    }

    public StringWrapper ping() {
        try {
            return new StringWrapper("Hi from " +
                InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new StringWrapper("RESOLUTION FAILED");
        }
    }

    public static void main(String[] args) {
        try {
            //		String path = "/users/icmcb/cave/proactive.loadleveler/descriptors/examples";
            ProActiveDescriptor pad =  //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_SimpleExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTasksPerNodeExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTotalTasksExample.xml");
                                       //			ProActive.getProactiveDescriptor(path+"/LoadLeveler_AdvancedTaskGeometryExample.xml");
                ProDeployment.getProactiveDescriptor(args[0]);

            //			ProActive.getProactiveDescriptor(path+"/SSH_LSF_Example.xml");
            pad.activateMappings();
            VirtualNode vn = pad.getVirtualNode("levelerVn");

            LoadLevelerTest group = (LoadLevelerTest) ProGroup.newActiveAsGroup(LoadLevelerTest.class.getName(),
                    null, vn);

            StringWrapper p = group.ping();

            ProGroup.waitAll(p);

            StringWrapper sw1 = (StringWrapper) ProGroup.get(p, 0);
            StringWrapper sw2 = (StringWrapper) ProGroup.get(p, 1);

            System.out.println(sw1);
            System.out.println(sw2);
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
