public class InitializedHelloClient {
    public static void main(String[] args) {
        InitializedHello myServer;
        String message;

        try {
            // checks for the server's URL
            if (args.length == 0) {
                // There is no url to the server, so create an active server within this VM
                myServer = (InitializedHello) org.objectweb.proactive.ProActive.newActive(
                        InitializedHello.class.getName(),
                        new Object[] { "local" });
            } else {
                // Lookups the server object
                System.out.println("Using server located on " + args[0]);
                myServer = (InitializedHello) org.objectweb.proactive.PAActiveObject.lookupActive(
                        InitializedHello.class.getName(),
                        args[0]);
            }

            // Invokes a remote method on this object to get the message
            message = myServer.sayHello();
            // Prints out the message
            System.out.println("The message is : " + message);
            myServer.terminate();
        } catch (Exception e) {
            System.err.println("Could not reach/create server object");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
