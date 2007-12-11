import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.util.URIBuilder;

public class Hello {
    private String name;
    private String hi = "Hello world";
    private java.text.DateFormat dateFormat = new java.text.SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss");

    public Hello() {
    }

    public Hello(String name) {
        this.name = name;
    }

    public String sayHello() {
        return hi + " at " + dateFormat.format(new java.util.Date()) +
        " from node : " + ProActive.getBodyOnThis().getNodeURL();
    }

    public static void main(String[] args) {
        // Registers it with an URL
        try {
            // Creates an active instance of class HelloServer on the local node
            Hello hello = (Hello) org.objectweb.proactive.ProActive.newActive(
                    Hello.class.getName(),
                    new Object[] { "remote" });
            java.net.InetAddress localhost = ProActiveInet.getInstance().getLocal();
            org.objectweb.proactive.api.ProActiveObject.register(hello,
                "//" + localhost.getHostName() + "/Hello");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
