package functionalTests.component.conform.components;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;


public class MasterImpl implements BindingController, Master {
    public static String ITF_CLIENTE_MULTICAST = "client-multicast";
    private SlaveMulticast slaves = null;

    public MasterImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public void bindFc(String clientItfName, Object serverItf)
        throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            slaves = (SlaveMulticast) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String[] listFc() {
        return new String[] { ITF_CLIENTE_MULTICAST };
    }

    /**
     * {@inheritDoc}
     */
    public Object lookupFc(String clientItfName)
        throws NoSuchInterfaceException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            return slaves;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unbindFc(String clientItfName)
        throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        if (ITF_CLIENTE_MULTICAST.equals(clientItfName)) {
            slaves = null;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    public void run() {
        List<List<String>> multicastArgsList = new ArrayList<List<String>>();
        for (int i = 0; i < 6; i++) {
            multicastArgsList.add(i, new ArrayList<String>());

            for (int j = 0; j < i; j++) {
                multicastArgsList.get(i).add("arg " + j);
            }
        }

        //        System.err.println("Async call with non-reifiable type");
        //        for (List<String> list : multicastArgsList) {
        //            System.err.println("Avec " + list.size() + " arguments.");
        //            Object[] sw = ((List<String>) slaves.computeSync(list,
        //                    "Sync")).toArray();
        //            for (Object object : sw) {
        //                System.err.println("Object result: " + object);
        //            }
        //            System.err.println();
        //        }
        System.err.println();

        for (List<String> list : multicastArgsList) {
            System.err.println("Async calls with " + list.size() +
                " arguments.");
            Object[] sw = ((List<StringWrapper>) slaves.computeAsync(list,
                    "Async")).toArray();
            for (Object object : sw) {
                System.err.println("Object result: " + object);
            }
            System.err.println();
        }
        System.err.println();

        for (List<String> list : multicastArgsList) {
            System.err.println("OneWay calls with " + list.size() +
                " arguments.");
            slaves.computeOneWay(list, "OneWay");
        }
        System.err.println();
    }

    public void computeOneWay(List<String> args, String other) {
    	slaves.computeOneWay(args, other);
    }
    
	public List<StringWrapper> computeAsync(List<String> args, String other) {
		return slaves.computeAsync(args, other);
	}
	
    public List<GenericTypeWrapper<String>> computeAsyncGenerics(List<String> args,
        String other) {
        //            System.err.println("Async calls with " + list.size() + " arguments.");
        //            Object[] sw = ((List<StringWrapper>) slaves.computeAsync(list,
        //                    "Async")).toArray();
        return slaves.computeAsyncGenerics(args, other);
        //            for (Object object : sw) {
        //                System.err.println("Object result: " + object);
        //            }
    }

    public List<String> computeSync(List<String> args, String other) {
        // TODO Auto-generated method stub
        return null;
    }


}
