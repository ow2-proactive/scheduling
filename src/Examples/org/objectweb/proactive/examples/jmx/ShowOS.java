package org.objectweb.proactive.examples.jmx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.management.MBeanAttributeInfo;
import javax.management.ObjectName;

import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.extensions.jmx.client.ClientConnector;


/**
 * This example connects remotely a MBean Server and shows informations
 * about the operating system (such as the OS name, the OS version, ...)
 * @author ProActive Team
 */
public class ShowOS {
    private ClientConnector cc;
    private ProActiveConnection connection;
    private String url;

    public ShowOS() throws Exception {
        System.out.println("Enter the url of the JMX MBean Server :");
        this.url = read();
        connect();
        infos();
        System.out.println("Good Bye!");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        new ShowOS();
    }

    private String read() {
        String what = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            what = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return what;
    }

    private void connect() {
        System.out.println("Connecting to : " + this.url);
        this.cc = new ClientConnector(this.url);
        this.connection = cc.getConnection();
    }

    private void infos() throws Exception {
        ObjectName name = new ObjectName("java.lang:type=OperatingSystem");
        String attribute = "";
        System.out.println("Attributes :");
        help(name);
        while (true) {
            System.out.println("Enter the name of the attribute :");
            attribute = read();
            if (attribute.equals("help")) {
                help(name);
            } else if (attribute.equals("quit")) {
                return;
            } else {
                Object att = this.connection.getAttribute(name, attribute);
                System.out.println("==> " + att);
            }
        }
    }

    private void help(ObjectName name) throws Exception {
        System.out.println("List of attributes :");
        MBeanAttributeInfo[] atts = this.connection.getMBeanInfo(name)
                                                   .getAttributes();
        for (int i = 0, size = atts.length; i < size; i++)
            System.out.println("> " + atts[i].getName());
        System.out.println("> help");
        System.out.println("> quit");
    }
}
