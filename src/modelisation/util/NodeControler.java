package modelisation.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class NodeControler {
    private static final int MAXTRY = 3;

    //protected static final String JAVA = "/usr/local/jdk1.2/bin/java  -Dmodelisation.ttl=100 -Dmodelisation.maxMigrations=5 -Dproactive.locationserver.rmi=//satura.inria.fr:35690/LocationServer -Xmx256m -Djava.compiler=NONE  -Dproactive.locationserver=modelisation.server.singlequeue.SelectiveServer -Dproactive.locationserver.rmi=//tuba/LocationServer ";
    protected static final String JAVA_BIN = "/usr/local/jdk1.4.0/bin/java ";
    protected static final String OPTIONS =
        "-Djava.library.path=/net/home/fhuet/workProActive/ProActive/classes/  " +
        //       "-Dproactive.communication.protocol=ibis " +
        "-Dibis.serialization=sun " +
        "-Dproactive.migrationmanager=modelisation.TimedMigrationManager" +
        "-Dmodelisation.maxMigrations=5 -Dproactive.locationserver.rmi=//satura.inria.fr:35690/LocationServer " +
        "-Xmx256m ";
    protected static String JAVA = JAVA_BIN + OPTIONS;

    // protected static final String JAVA = "/usr/local/jdk1.3.1/bin/java  -Xmx256m  -Djava.compiler=NONE ";
    private static final String NODECLASS = " org.objectweb.proactive.core.node.StartNode ";
    private static String CLASSPATH = "/u/tuba/0/oasis/fhuet/workProActive/ProActive/classes:/u/tuba/0/oasis/fhuet/workProActive/ProActive/lib/bcel.jar:.";
    protected static final String USER = "fhuet";
    protected static final String JAVA_NESSIE = " /u/dea_these/fhuet/java.sh -Dmodelisation.ttl=100 -Dmodelisation.maxMigrations=5 -Dproactive.locationserver.rmi=//satura.inria.fr:35690/LocationServer ";
    protected static final String CLASSPATH_NESSIE = "/u/dea_these/fhuet/proactive-tmp:/u/dea_these/fhuet/java/classes:/u/dea_these/fhuet/java/lib/bcel.jar";
    protected static final String USER_NESSIE = "fhuet";
    protected static final String USER_POLYA = "salouf";
    protected static final String JAVA_SATURA = "/u/satura/0/oasis/fhuet/java.sh -Dmodelisation.ttl=100 -Dmodelisation.maxMigrations=5  -Dproactive.locationserver.rmi=//satura.inria.fr:35690/LocationServer ";
    protected static final String CLASSPATH_SATURA = "/u/satura/0/oasis/fhuet/proactive-tmp:/u/satura/0/oasis/fhuet/java/classes:/u/satura/0/oasis/fhuet/java/lib/bcel.jar";
    protected static final String USER_SATURA = "fhuet";
    private Vector process;

    public NodeControler() {
        process = new Vector();
    }

    public NodeControler(String classpath) {
        NodeControler.CLASSPATH = classpath;
    }

    public void addProcess(RSHNodeProcessImpl p) {
        process.addElement(p);
    }

    public void killAllProcess() {
        System.out.println("Killing all Processes");
        for (Enumeration e = process.elements(); e.hasMoreElements();) {
            ((RSHNodeProcessImpl) e.nextElement()).stopProcess();
            try {
                Thread.sleep(1000);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    protected String getHostNameFromNodeName(String nodeName) {
        Pattern p = Pattern.compile("(.*:)?(//)?(.*)/(.*)");
        Matcher m = p.matcher(nodeName);
        return m.group(3);
    }

    public boolean startNode(String name, String redirect) {
        boolean result;
        RSHNodeProcessImpl p = null;

        String hostName = getHostNameFromNodeName(name);
        System.out.println("Hostname is " + hostName);
        p = new RSHNodeProcessImpl();
        p.setJavaPath(JAVA);
        p.setClassname(NODECLASS);
        p.setParameters(name);
        p.setHostname(hostName);

        if ("polya".equals(hostName)) {
            p.setClasspath(CLASSPATH);
            p.setJavaPath(JAVA);
            p.setUsername(USER_POLYA);
        }
        if (("tuba".equals(hostName)) || ("ornata".equals(hostName)) ||
                ("mirage".equals(hostName)) || ("solida".equals(hostName)) ||
                ("trinidad".equals(hostName))) {
            p.setClasspath(CLASSPATH);
            p.setJavaPath(JAVA);
            p.setUsername(USER);
        }
        if (hostName.indexOf("satura") >= 0) {
            p.setClasspath(CLASSPATH_SATURA);
            p.setJavaPath(JAVA_SATURA);
            p.setUsername(USER_SATURA);
        }
        if (hostName.indexOf("nessie") >= 0) {
            p.setClasspath(CLASSPATH_NESSIE);
            p.setJavaPath(JAVA_NESSIE);
            p.setUsername(USER_NESSIE);
        }
        if (hostName.indexOf("essi") >= 0) {
            p.setClasspath(CLASSPATH_NESSIE);
            p.setJavaPath(JAVA_NESSIE);
            p.setUsername(USER_NESSIE);
        }

        System.out.println(p);

        try {
            p.startProcess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // end of try-catch
        result = !p.isFinished();
        if (result) {
            //	p.initialise();
            //	t.start();
            //  p.run();
            System.out.println("Node " + name + " .............OK");
            this.addProcess(p);
        } else {
            System.out.println("WARNING: giving up");
            System.out.println("Error is " + p.getErrorMessage());
            p.stopProcess();
        }
        return result;
    }

    public boolean startAllNodes(String names, String redirect) {
        StringTokenizer st = null;
        boolean ok = true;

        //boolean tmp;
        System.out.println("NodeControler: startAllNodes() " + names);
        for (st = new StringTokenizer(names); st.hasMoreTokens();) {
            ok = ok && startNode(st.nextToken() + " -noClassServer", redirect);
        }
        return ok;
    }

    public boolean getNode(String name) {
        boolean ok = true;

        try {
            NodeFactory.getNode(name);
        } catch (NodeException e) {
            ok = false;
            e.printStackTrace();
        }

        return ok;
    }

    public boolean getAllNodes(String names) {
        System.out.println("NodeControler: getAllNodes() " + names);
        StringTokenizer st = null;
        boolean ok = true;
        for (st = new StringTokenizer(names); st.hasMoreTokens();) {
            ok = ok && getNode(st.nextToken());
        }
        return true;
    }

    public String readDestinationFile(String fileName) {
        FileReader f_in = null;
        StringBuffer total = new StringBuffer();
        System.out.println("NodeControler: using destination file " + fileName);
        try {
            f_in = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not Found");
        }
        BufferedReader _in = new BufferedReader(f_in);

        // on lit a partir de ce fichier
        // NB : a priori on ne sait pas combien de lignes on va lire !!
        try {
            // tant qu'il y a quelque chose a lire
            while (_in.ready()) {
                // on le lit
                total.append(_in.readLine());
                total.append("\n");
            }
        } // catch (IOException e) {}
        catch (Exception e) {
        }

        try {
            _in.close();
        } catch (IOException e) {
            System.out.println("Error closing the file");
        }
        System.out.println("Result of reading file is " + total.toString());

        return total.toString();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println(
                "Usage: java migration.util.NodeControler nodeName");
            System.exit(-1);
        }
        NodeControler bench = new NodeControler();
        bench.startAllNodes(args[0], "toto");
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bench.killAllProcess();
    }
}
