/*
 * Created on Oct 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package modelisation;

import modelisation.mixed.Bench;

import modelisation.util.NodeControler;

import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import java.util.Vector;


/**
 * @author fabrice
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ModelisationBench {
    protected static Node StartNode;
    protected static NodeControler auto;
    protected static ProActiveDescriptor pad;

    public ModelisationBench() {
    }

    public static void initialise(NodeControler a) {
        Bench.auto = a;
    }

    public static void stop() {
        System.out.println("Bench: stoping......");
        // Bench.auto.killAllProcess();
        ModelisationBench.killAll();
		System.out.println("Bench: stoping...... done");
    }

    public static Node[] readDestinationFile(String fileName, String extension) {
        //we check if we should create the nodes
        //of if they have been launched by the user
        NodeControler auto = new NodeControler();
        if ("true".equals(System.getProperty("nodecontroler.startnode"))) {
            if (!auto.startAllNodes(auto.readDestinationFile(fileName),
                        extension)) {
                auto.killAllProcess();
                System.err.println("Error creating nodes, aborting");
                System.exit(-1);
            }
        } else {
            if (!auto.getAllNodes(auto.readDestinationFile(fileName))) {
                auto.killAllProcess();
                System.err.println(
                    "Error getting already created nodes, aborting");
                System.exit(-1);
            }
        }

        ModelisationBench.initialise(auto);

        //Reading the destination file
        FileReader f_in = null;
        Vector v = new Vector();
        String s;
        try {
            f_in = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File not Found");
        }

        // on ouvre un "lecteur" sur ce fichier
        BufferedReader _in = new BufferedReader(f_in);

        // on lit a partir de ce fichier
        // NB : a priori on ne sait pas combien de lignes on va lire !!
        try {
            // tant qu'il y a quelque chose a lire
            while (_in.ready()) {
                // on le lit
                s = _in.readLine();
                //   StringTokenizer tokens = new StringTokenizer(s, " ");
                System.out.println("Adding " + s + " to destinationFile");
                v.addElement(NodeFactory.getNode(s));
                //   this.add(new NodeDestination(new String (tokens.nextToken()),tokens.nextToken()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Node[] result = new Node[v.size()];
        v.copyInto(result);
        return result;
    }

    public static Node[] readMapingFile(String fileName) {
        Node[] result = null;
        try {
            pad = ProActive.getProactiveDescriptor(fileName);
            pad.activateMappings(); //activate all VNs
            //we first read the StartNode
            VirtualNode startNode = pad.getVirtualNode("StartNode");
            Bench.StartNode = startNode.getNode();
            //then the destination nodes
            VirtualNode vn = pad.getVirtualNode("Nodes");
            result = vn.getNodes();
            //	  return result;
        } catch (ProActiveException e) {
            // TO DO : Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static void killAll() {
        try {
            pad.killall();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}
