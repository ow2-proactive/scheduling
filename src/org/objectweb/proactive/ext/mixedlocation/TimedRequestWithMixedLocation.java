package org.objectweb.proactive.ext.mixedlocation;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.TimedRequestWithLocationServer;


public class TimedRequestWithMixedLocation
    extends TimedRequestWithLocationServer {
    private static final int MAX_TRIES = 30;
    private static int counter = 0;
    private int tries;
    transient protected LocationServer server;
    protected long startTime;

    public TimedRequestWithMixedLocation(MethodCall methodCall,
        UniversalBody sender, boolean isOneWay, long nextSequenceID,
        LocationServer server) {
        super(methodCall, sender, isOneWay, nextSequenceID, server);
    }

    protected int sendRequest(UniversalBody destinationBody)
        throws java.io.IOException {
        System.out.println("RequestWithMixedLocation: sending to universal " +
            counter);
        int ftres = FTManager.NON_FT;
        try {
            ftres = destinationBody.receiveRequest(this);
        } catch (Exception e) {
            //  e.printStackTrace();
            this.backupSolution(destinationBody);
        }
        return ftres;
    }

    /**
     * Implements the backup solution
     */
    protected void backupSolution(UniversalBody destinationBody)
        throws java.io.IOException {
        boolean ok = false;
        tries = 0;
        System.out.println(
            "RequestWithMixedLocationr: backupSolution() contacting server " +
            server);
        System.out.println(
            "RequestWithMixedLocation.backupSolution() : looking for " +
            destinationBody);
        //get the new location from the server
        while (!ok && (tries < MAX_TRIES)) {
            UniversalBody mobile = (UniversalBody) server.searchObject(destinationBody.getID());
            System.out.println(
                "RequestWithMixedLocation: backupSolution() server has sent an answer");

            //we want to bypass the stub/proxy
            UniversalBody newDestinationBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile).getProxy()).getResult();

            // !!!!
            // !!!! should put a counter here to stop calling if continuously failing
            // !!!!
            try {
                // sendRequest(newDestinationBody);
                newDestinationBody.receiveRequest(this);
                //everything went fine, we have to update the current location of the object
                //so that next requests don't go through the server
                System.out.println(
                    "RequestWithMixedLocation: backupSolution() updating location");
                if (sender != null) {
                    sender.updateLocation(newDestinationBody.getID(),
                        newDestinationBody.getRemoteAdapter());
                }
                ok = true;
            } catch (Exception e) {
                System.out.println(
                    "RequestWithMixedLocation: backupSolution() failed");
                tries++;
                try {
                    Thread.sleep(500);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
