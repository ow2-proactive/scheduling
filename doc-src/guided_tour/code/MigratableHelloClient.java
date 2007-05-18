public class MigratableHelloClient {
    /** entry point for the program
     * @param args destination nodes
     * for example :
    * rmi://localhost/node1 http://localhost/node2*/
    public static void main(String[] args) { // instanciation-based creation of the active object

        MigratableHello migratable_hello = MigratableHello.createMigratableHello("agent1");

        // check if the migratable_hello has been created
        if (migratable_hello != null) {
            // say hello
            System.out.println(migratable_hello.sayHello());

            // start moving the object around
            for (int i = 0; i < args.length; i++) {
                migratable_hello.moveTo(args[i]);
                System.out.println("received message : " +
                    migratable_hello.sayHello());
            }

            // possibly terminate the activity of the active object ...
            migratable_hello.terminate();
        } else {
            System.out.println("creation of the active object failed");
        }
    }
}
