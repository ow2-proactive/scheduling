package functionalTests.gcmdeployment.descriptorurl;

import functionalTests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;


public class TestGCMDescriptorURL extends FunctionalTest {
    GCMApplication gcma;
    GCMApplication gcma2;

    //    GCMApplication gcma3;

    @Test
    public void test() throws ProActiveException, FileNotFoundException, MalformedURLException {
        /**** Testing classical File url ****/
        URL descriptor1 = getClass().getResource("application/TestVirtualNodeRelative.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor1);
        /**** Testing Jar url Relative ****/
        URL jarfileurl = getClass().getResource("descriptors.jar");
        URL descriptor2 = new URL("jar:" + jarfileurl.toExternalForm() +
            "!/application/TestVirtualNodeRelative.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor2);

        /**** Testing HTTP+Jar URL !!! ***/
        //        URL jarfileurl2 = new URL("http://proactive.inria.fr/userfiles/file/apps/descriptors.jar");
        //        URL descriptor3 = new URL("jar:"+jarfileurl2.toExternalForm()+"!/application/TestVirtualNodeRelative.xml");
        //        System.out.println(descriptor3);
        gcma = PAGCMDeployment.loadApplicationDescriptor(descriptor1);
        gcma2 = PAGCMDeployment.loadApplicationDescriptor(descriptor2);
        //        gcma3 = PAGCMDeployment.loadApplicationDescriptor(descriptor3);
        Assert.assertFalse(gcma.isStarted());
        Assert.assertEquals(1, gcma.getVirtualNodes().size());

        Assert.assertFalse(gcma2.isStarted());
        Assert.assertEquals(1, gcma2.getVirtualNodes().size());

    }
}
