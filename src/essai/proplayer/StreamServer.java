package essai.proplayer;

import org.objectweb.proactive.Body;


/**
 * a ProStream server
 * @see ProStream
 */
public abstract class StreamServer implements org.objectweb.proactive.RunActive {

    /**reference to the client*/
    protected StreamClient client;

    /**identification*/
    protected int id;

    /**file generating the stream*/
    protected String filename;

    /**
     * empty no arg constructor -specific to ProActive
     */
    public StreamServer() {
    }

    /**
     * creates a reference to this server's client
     * @param sc the StreamClient for the ProStream
     * @param index the id of this StreamServer for the StreamClient
     */
    public void registerClient(StreamClient sc, int index) {
        client = sc;
        id = index;
        try {
            System.out.println("Client registered on " +
                java.net.InetAddress.getLocalHost().getHostName());
        } catch (Exception e) {
            System.out.println("Exception :" + e);
        }
    }

    /**starts serving*/
    public void startStream() {

        /**meant to be overridden*/
        System.err.println("Please override the startStream method.");
    }

    /**serves <code>n</code> times*/
    public void startStream(int n) {
        System.err.println("Please override the startStream method.");
    }

    /**ends serving*/
    public void stopStream() {
        System.err.println("Please override the stopStream method.");
    }

    /**adds a chunk of the served file to the StreamClient's buffer*/
    public void getChunk() {
        System.err.println("Please override the getChunk method");
    }

    /**the live method @param body this Active objects body*/
    public void runActivity(Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.blockingServeOldest("registerClient");
        System.out.println("Blocking the RequestQueue");

        /**blocking*/
        service.blockingServeOldest("startStream");
    }
}
