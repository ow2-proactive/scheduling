package nonregressiontest.component.collectiveitf.multicast.classbased;

import java.util.ArrayList;
import java.util.List;

import nonregressiontest.component.collectiveitf.multicast.MulticastTestItf;
import nonregressiontest.component.collectiveitf.multicast.Test;
import nonregressiontest.component.collectiveitf.multicast.Tester;
import nonregressiontest.component.collectiveitf.multicast.WrappedInteger;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.controller.MulticastBindingController;

import testsuite.test.Assertions;


public class TesterImpl implements Tester, BindingController {
    

	MulticastTestItf clientItf;

    OneToOneMulticast oneToOneMulticastClientItf = null;
    BroadcastMulticast broadcastMulticastClientItf = null;


    public void testConnectedServerMulticastItf() throws Exception {

    }

    public void testOwnClientMulticastItf() throws Exception {

        List<WrappedInteger> listParameter = new ArrayList<WrappedInteger>();
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            listParameter.add(i, new WrappedInteger(i));
        }
        List<WrappedInteger> result;
        
        result = broadcastMulticastClientItf.dispatch(listParameter);
        Assertions.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assertions.assertTrue(result.contains(new WrappedInteger(i)));
        }
        
        result = oneToOneMulticastClientItf.dispatch(listParameter);
        Assertions.assertTrue(result.size() == Test.NB_CONNECTED_ITFS);
        for (int i = 0; i < Test.NB_CONNECTED_ITFS; i++) {
            Assertions.assertTrue(result.get(i).equals(new WrappedInteger(i)));
        } 



    }


    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if ("oneToOneMulticastClientItf".equals (clientItfName) && (serverItf instanceof OneToOneMulticast)) {
            oneToOneMulticastClientItf = (OneToOneMulticast)serverItf;
        } else  
        if ("broadcastMulticastClientItf".equals (clientItfName) && (serverItf instanceof BroadcastMulticast)) {
            broadcastMulticastClientItf = (BroadcastMulticast)serverItf;
        } else {
        throw new ProActiveRuntimeException("cannot find multicast interface " + clientItfName);
        }
		
	}

	public String[] listFc() {
		return new String[] {"oneToOneMulticastClientItf", "broadcastMulticastClientItf"};
	}

	public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("oneToOneMulticastClientItf".equals(clientItfName)) {
            return oneToOneMulticastClientItf;
        }
        if ("broadcastMulticastClientItf".equals(clientItfName)) {
            return broadcastMulticastClientItf;
        }
        throw new NoSuchInterfaceException(clientItfName);

	}

	public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if ("oneToOneMulticastClientItf".equals (clientItfName) ) {
            oneToOneMulticastClientItf = null;
        } else  
        if ("broadcastMulticastClientItf".equals (clientItfName) ) {
            broadcastMulticastClientItf = null;
        } else {
        throw new ProActiveRuntimeException("cannot find multicast interface " + clientItfName);
        }
		
	}

}
