package org.objectweb.proactive.examples.pi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProActiveGroup;


/**
 *
 * This program evaluates the PI number using the Bailey-Borwein-Plouffe
 * algorithm.
 *
 * @author Matthieu Morel
 *
 */
public class PiBBP implements Serializable {
    private final static int SIMPLE = 1;
    private final static int PARALLEL = 2;
    private final static int PARALLEL_DISTRIBUTED = 3;
    private final static int COMPONENT = 4;
    private int run_ = SIMPLE;
    protected int nbDecimals_;
    private String deploymentDescriptorLocation_;
    private ProActiveDescriptor deploymentDescriptor_;
    private boolean ws_ = false;
    protected PiComp piComputer;

    public PiBBP() {
    }
  
    public PiBBP(String[] args) {
        parseProgramArguments(args);
    }

    public void setNbDecimals(int nbDecimals) {
        this.nbDecimals_ = nbDecimals;
    }
    
    public String runSimple() {
        System.out.println(
            "No deployment : computation will take place on the current node.");
        System.out.println("Starting computation ...");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // create a PiComputer instance
        piComputer = new PiComputer(new Integer(nbDecimals_));

        // define the interval to calculate
        Interval interval = new Interval(0, nbDecimals_);

        // compute
        Result result = piComputer.compute(interval);
        long timeAtEndOfComputation = System.currentTimeMillis();

        // display results
        System.out.println("Computation finished ...");
        System.out.println("Computed PI value is : " +
            result.getNumericalResult().toString());

        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        return result.getNumericalResult().toString();
    }

    public String  runParallel() {
        try {
            // create a group of computers on the current host
            piComputer = (PiComputer) ProActiveGroup.newGroup(PiComputer.class.getName(),
                    new Object[][] {
                        new Object[] { new Integer(nbDecimals_) },
                        new Object[] { new Integer(nbDecimals_) }
                    });

           return  computeOnGroup(piComputer);
           
        }catch (Exception e ) {e.printStackTrace();}
        return null;
    }

    public String runParallelDistributed() {
        
        try {
            //*************************************************************
            // * creation of remote nodes
            // *************************************************************/
            System.out.println("\nStarting deployment of virtual nodes");
            // parse the descriptor file
            deploymentDescriptor_ = ProActive.getProactiveDescriptor(
                    "../descriptors/" + deploymentDescriptorLocation_);
            deploymentDescriptor_.activateMappings();
            VirtualNode computersVN = deploymentDescriptor_.getVirtualNode(
                    "computers-vn");
            
            //            // create the remote nodes for the virtual node computersVN
            //           computersVN.activate();
            //*************************************************************
            // * creation of active objects on the remote nodes
            // *************************************************************/
            System.out.println(
                "\nCreating a group of computers on the given virtual node ...");
            
            // create a group of computers on the virtual node computersVN
            piComputer = (PiComputer) ProActiveGroup.newGroupInParallel(PiComputer.class.getName(),
                    new Object[] { new Integer(nbDecimals_) },
                    computersVN.getNodes());
            
            return computeOnGroup(piComputer);
            
            
        } catch (Exception e) {
            e.printStackTrace();
                   } finally {
                       try {
                        deploymentDescriptor_.killall(true);
                    } catch (ProActiveException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                   }
                   return "";
    }

    public void runComponent() {
        try {
        String arg0 = "-fractal"; // using the fractal component model
        String arg1 = "org.objectweb.proactive.examples.pi.fractal.PiBBPWrapper";
//        String arg1 = "org.objectweb.proactive.examples.pi.fractal.bindings-local";
        String arg2 = "r";
        
        String arg3 ="../descriptors/" + deploymentDescriptorLocation_; // the deployment descriptor
        
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        ProActiveDescriptor deploymentDescriptor = ProActive.getProactiveDescriptor(arg3);
        context.put("deployment-descriptor",deploymentDescriptor);
        deploymentDescriptor.activateMappings();
        int nbNodes=deploymentDescriptor.getVirtualNode("computers-vn").getNumberOfCreatedNodesAfterDeployment();
        List<Interval> intervals= PiUtil.dividePIList(nbNodes, nbDecimals_);
        Component master = (Component) f.newComponent("org.objectweb.proactive.examples.pi.fractal.PiBBPWrapper",null);
       
        
        Component worker;
        List<Component> workers=new ArrayList<Component>();
        for(int i=0;i<nbNodes;i++){
        	worker=(Component) f.newComponent("org.objectweb.proactive.examples.pi.fractal.PiComputer",context);
        	Fractal.getBindingController(master).bindFc("multicastDispatcher", worker.getFcInterface("computation"));
        	workers.add(worker);
        }
        
        Component w;
        //Starting all the workers
        PiComp picomp;
        for (int j=0;j<workers.size();j++){
        	w=(Component)workers.get(j);
        	Fractal.getLifeCycleController(w).startFc();
        	 picomp=(PiComp)w.getFcInterface("computation");
           	 picomp.setScale(nbDecimals_);/*Normally, this is made when instanciating PiComputers, but with ADL instanciation, we have to make an explicit call to setScale */

        }
   	
        Fractal.getLifeCycleController(master).startFc();
        MasterComputation m=(MasterComputation)master.getFcInterface("s");
        m.startComputation(intervals);
        
        
        //deploymentDescriptor.killall(false);
        
//        Fractal.getLifeCycleController(root).startFc();
//        ((Runnable)root.getFcInterface("r")).run();

        
        // FIXME : how do I elegantly reference the tutorial/descriptors directory ?
//            org.objectweb.proactive.core.component.adl.Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String computeOnGroup(PiComp piComputers) { 
        int nbNodes = ProActiveGroup.getGroup(piComputers).size();
        System.out.println("\nUsing " + nbNodes +
                " PiComputers for the computation\n");

            // distribution of the intervals to the computers is handled in
            // a private method
            Interval intervals = null;
            try {
                intervals = PiUtil.dividePI(nbNodes, nbDecimals_);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }

        // scatter group data, so that independent intervals are sent as
        // parameters to each PiComputer instance
        ProActiveGroup.setScatterGroup(intervals);

        //*************************************************************
        // * computation
        // *************************************************************/
        System.out.println("Starting computation ...\n");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // invocation on group, parameters are scattered, result is a
        // group
        Result results = piComputers.compute(intervals);
        Group resultsGroup = ProActiveGroup.getGroup(results);
        
        // the following is displayed because the "compute" operation is
        // asynchronous (non-blocking)
        System.out.println("Intervals sent to the computers...\n");

        Result total = PiUtil.conquerPI(results);

        long timeAtEndOfComputation = System.currentTimeMillis();

        //*************************************************************
        // * results
        // *************************************************************/
        System.out.println("\nComputation finished ...");
        System.out.println("Computed PI value is : " +
            total.getNumericalResult().toString());
        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        System.out.println("Cumulated time from all computers is : " +
            total.getComputationTime() + " ms");
        System.out.println("Ratio for " + resultsGroup.size() +
            " processors is : " +
            (((double) total.getComputationTime() / ((double) (timeAtEndOfComputation -
            timeAtBeginningOfComputation))) * 100) + " %");
        return total.getNumericalResult().toString();
    }

    public void start() {
        System.out.println("Evaluation of Pi will be performed with " +
            nbDecimals_ + " decimals");

        switch (run_) {
        case (SIMPLE):
            runSimple();
            break;
        case (PARALLEL):
            runParallel();
            break;
        case (PARALLEL_DISTRIBUTED):
            runParallelDistributed();
        
        try{
        	deploymentDescriptor_.killall(false);
        }
        catch(Exception e){
        	e.printStackTrace();
        }
            break;
        case (COMPONENT):
            runComponent();
            break;
        default:
            runSimple();
        }
    }

    public static void main(String[] args) {
        try {
            //            PiBBP piApplication = new PiBBP(args);
            //            piApplication.start();
            PiBBP piApplication = (PiBBP) ProActive.newActive(PiBBP.class.getName(),
                    new Object[] { args });

            if (piApplication.isWebService()) {
                ProActive.exposeAsWebService(piApplication,
                    "http://localhost:8080/", "piComputation",
                    new String[] {
                        "runSimple", "runParallel", "runParallelDistributed",
                        "setNbDecimals"
                    });
            } else {
                piApplication.start();
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    
    // distributes the computation data to the available computers
    /*
    private static Interval distributeIntervals(int length, int scale)
        throws Exception {
        int intervalSize = scale / length;
        Interval intervals = (Interval) ProActiveGroup.newGroup(Interval.class.getName());
        Group intervals_group = ProActiveGroup.getGroup(intervals);
        for (int i = 0; i < length; i++) {
            int beginning = i * intervalSize;
            int end = ((i == (length - 1)) ? scale
                                           : ((beginning + intervalSize) - 1));
            intervals_group.add(new Interval(beginning, end));
        }
        return intervals;
    }
     */
    public boolean isWebService() {
        return ws_;
    }

    private void parseProgramArguments(String[] args) {
        if (args.length == 0) {
            ws_ = true;
            deploymentDescriptorLocation_ = "LAN.xml";
        } else {
            nbDecimals_ = new Integer(args[0]).intValue();
            run_ = new Integer(args[1]).intValue();
            int deployment = new Integer(args[2]).intValue();
            switch (deployment) {
            case 1:
                deploymentDescriptorLocation_ = "localhost.xml";
                break;
            case 2:
                deploymentDescriptorLocation_ = "LAN.xml";
                break;
            case 3:
                deploymentDescriptorLocation_ = "sophia-infra-p2p.xml";
                break;
            case 4:
                deploymentDescriptorLocation_ = "sophia-cluster.xml";
                break;
            case 5:
                deploymentDescriptorLocation_ = "custom-descriptor.xml";
                break;
            default:
                deploymentDescriptorLocation_ = "localhost.xml";
            }
        }
    }
}
