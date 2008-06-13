package functionalTests.component.collectiveitf.dynamicdispatch;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class RunnerImpl implements RunnerItf, org.objectweb.fractal.api.control.BindingController {

    RequiredService services;
    int nbTasks = 10;

    public boolean runTest() {

        List<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < nbTasks; i++) {
            tasks.add(new Task(i));
        }
        List<Result> results = services.execute(tasks);

        Assert.assertEquals(nbTasks, results.size());

        int nbTasksForWorker0 = 0;
        int nbTasksForWorker1 = 0;
        for (int i = 0; i < nbTasks; i++) {
            if (results.get(i).getWorkerIndex() == 0) {
                nbTasksForWorker0++;
            } else if (results.get(i).getWorkerIndex() == 1) {
                nbTasksForWorker1++;
            }
        }
        System.out.println("worker 0: " + nbTasksForWorker0);
        System.out.println("worker 1: " + nbTasksForWorker1);
        Assert.assertTrue(nbTasksForWorker0 == 1);
        Assert.assertTrue(nbTasksForWorker1 == (nbTasks - 1));

        //		// run unicast test
        //		
        //		List<Integer> parameters = new ArrayList<Integer>();
        //		parameters.add(1);
        //		parameters.add(10);
        //		
        //		// first dispatch
        //		Assert.assertEquals(new IntWrapper(11), services.method1(parameters));
        //		
        //		// second dispatch
        ////		Assert.assertEquals("server 2 received parameter 2", services.method1(parameters));
        //		
        //		// has been executed
        return true;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("requiredServiceItf")) {
            services = (RequiredService) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("requiredServiceItf".equals(clientItfName)) {
            return services;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        throw new RuntimeException("not implemented");
    }

}
