package modelisation.server;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

import util.timer.MicroTimer;


public class TimedRequestWithLocationServer extends RequestImpl
    implements java.io.Serializable {
    private transient boolean shouldMesureTime = false;
    private static final int MAX_TRIES = 30;

    /**
     * the number of time we try before reporting a failure
     */

    // private long startTimeServer;
    //private long startTimeAgent;
    protected long startTime;
    private int tries;
    private transient LocationServer server;

    public TimedRequestWithLocationServer(MethodCall methodCall,
        UniversalBody sender, boolean isOneWay, long nextSequenceID,
        LocationServer server) {
        super(methodCall, sender, isOneWay, nextSequenceID);
        this.server = server;
        //we only want to mesure time on the first send
        this.shouldMesureTime = true;
    }

    public Reply serve(Body targetBody) throws ServeException {
        MicroTimer timer = new MicroTimer();
        timer.start();

        Reply r = super.serve(targetBody);
        timer.stop();
        System.out.println("TimedRequestWithLocationServer: " +
            timer.getCumulatedTime() + " for method " + methodName);
        return r;
    }

    protected void sendRequest(UniversalBody destinationBody)
        throws java.io.IOException {
        if (shouldMesureTime) {
            System.out.println(
                "TimedRequestWithLocationServer: sending to remote " +
                methodName);
        }
        try {
            startTime = System.currentTimeMillis();
            destinationBody.receiveRequest(this);

            long endTime = System.currentTimeMillis();
            if (this.shouldMesureTime) {
                System.out.println(
                    "TimedRequestWithLocationServer:  .............. 1/gamma = " +
                    (endTime - startTime) + " for method " + methodName);
                System.out.println(
                    "TimedRequestWithLocationServer:  .............. done  = " +
                    (endTime - startTime) + " for method " + methodName);
            }
        } catch (Exception e) {
            // endTime = System.currentTimeMillis();
            //There can only be a problem when trying to contact the Agent
            System.out.println(
                "TimedRequestWithLocationServer:  .............. FAILED = " +
                (System.currentTimeMillis() - startTime) + " for method " +
                methodName);
            //e.printStackTrace();
            System.out.println(">>>>>>>>>>>> Exception " + e);
            this.backupSolution(destinationBody);
        }
    }

    /**
     * Implements the backup solution
     */
    protected void backupSolution(UniversalBody destinationBody)
        throws java.io.IOException {
        //   long startTimeGamma1=0;
        //   long endTimeGamma1=0;
        boolean ok = false;
        tries = 0;

        //   System.out.println("TimedRequestWithLocationServer: backupSolution() contacting server  at time " + System.currentTimeMillis());
        //get the new location from the server
        UniqueID bodyID = destinationBody.getID();
        while (!ok && (tries < MAX_TRIES)) {
            UniversalBody remoteBody = null;
            System.out.println(" ==== Query server ==== time " +
                System.currentTimeMillis());

            UniversalBody mobile = queryServer(bodyID);
            System.out.println("=========================== time " +
                System.currentTimeMillis());
            //we want to bypass the stub/proxy
            remoteBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile).getProxy()).getResult();

            long startTimeGamma = System.currentTimeMillis();
            try {
                remoteBody.receiveRequest(this);

                long endTime = System.currentTimeMillis();
                if (this.shouldMesureTime) {
                    System.out.println(
                        "TimedRequestWithLocationServer:  .............. 1/gamma = " +
                        (endTime - startTimeGamma) + " for method " +
                        methodName);
                    System.out.println(
                        "TimedRequestWithLocationServer:  .............. done = " +
                        (endTime - startTime) + " for method " + methodName);
                }

                //everything went fine, we have to update the current location of the object
                //so that next requests don't go through the server
                if (sender != null) {
                    sender.updateLocation(bodyID, remoteBody);
                } else {
                    LocalBodyStore.getInstance().getLocalBody(getSourceBodyID())
                                  .updateLocation(bodyID, remoteBody);
                }
                ok = true;
            } catch (Exception e) {
                System.out.println(
                    "TimedRequestWithLocationServer:  .............. FAILED = " +
                    (System.currentTimeMillis() - startTimeGamma) +
                    " for method " + methodName);
                tries++;
            }
        }
    }

    protected UniversalBody queryServer(UniqueID bodyID) {
        long startTimeBackupSolution = System.currentTimeMillis();
        if (server == null) {
            server = LocationServerFactory.getLocationServer();
        }

        UniversalBody mobile = (UniversalBody) server.searchObject(bodyID);
        long endTimeBackupSolution = System.currentTimeMillis();
        System.out.println(
            "TimedRequestWithLocationServer: backupSolution() server has sent an answer after " +
            (endTimeBackupSolution - startTimeBackupSolution));
        ProActive.waitFor(mobile);
        return mobile;
    }
}
