package org.objectweb.proactive.extra.p2pTest.p2p;

import java.util.Hashtable;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extra.p2pTest.messages.TracerWithCallbackMessage;
import org.objectweb.proactive.p2p.v2.monitoring.Dumper;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.node.P2PNodeLookup;
import org.objectweb.proactive.p2p.v2.service.util.P2PConstants;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


/**
 *
 * @author cvergoni
 * Main et CallBack pour TracerWithCallbackMessage
 * Main soit: -d�marre le dump de la topologie � partir d'une addresse
 *            -trace le nombre d'acquaintance pour l'ensemble des noeuds p2p
 *            -fait une requete pour obtenir un noeud ProActive
 */
public class Tracer implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    public static final boolean DEBUG = true;
    private static final String USAGE = Tracer.class.getName() +
        " address[ex://fiacre.inria.fr/] opt[dump/trace/reqnode]";
    public static final int MAX_NODE = 30;
    public static final int TIME = 4000;
    public static final int TTL = 6;
    public Hashtable<String, Integer> fileTabPosition = new Hashtable<String, Integer>();
    public int currentTabPos = 0;
    public int[] cache = new int[MAX_NODE];

    //Pour ProActive
    public Tracer() {
    }

    /**
     * Dump la topologie a l'aide de org.objectweb.proactive.p2p.v2.monitoring.Dumper pour @param addr
     * @param addr l'addresse du point d'entr� pour le dump de la topologie
     */
    public static void dumpP2PNetwork(String addr) {
        Dumper dumper = null;
        try {
            dumper = (Dumper) ProActiveObject.newActive(Dumper.class.getName(),
                    new Object[] {  });
            Dumper.requestAcquaintances(addr, dumper);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dumper.dumpAsText();
    }

    /**
     * Envoi au P2PService un message TracerWithCallbackMessage avec ProActive.getStubOnThis() pour les Callbacks
     * @param distP2PService P2PService distant
     */
    public void sendTrace(P2PService distP2PService) {
        try {
            for (int i = 0; i < MAX_NODE; i++) {
                cache[i] = 0;
            }
            distP2PService.dumpAcquaintances(new TracerWithCallbackMessage(
                    TTL, UniversalUniqueID.randomUUID(),
                    (Tracer) ProActiveObject.getStubOnThis()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printCache() {
        int i = 0;
        while (i < MAX_NODE) {
            System.out.print(cache[i++] + " ");
        }
        System.out.println();
    }

    /**
     * Permet de r�cup�rer ou de creer un index identifiant de mani�re unique tout au long de l'execution l'adresse d'un noeud
     * @param addr addresse du noeud
     * @return un index associ� de mani�re unique tout au long de l'execution � l'adresse pass�e en param�tre
     */
    public int getTabPositionOf(String addr) {
        int current;
        if (fileTabPosition.containsKey(addr)) {
            return fileTabPosition.get(addr);
        } else {
            current = currentTabPos++;
            fileTabPosition.put(addr, current);
            return current;
        }
    }

    /**
     * CallBack des Messages TracerWithCallbackMessage
     * met a jour le nombre de voisin @param nbAcquaintance pour le noeud d'adresse @param addr
     * @param addr addresse du noeud qui fait le CallBack
     * @param nbAcquaintances nombre d'acquaintance du noeud qui effectue le Callback
     */
    public void trace(String addr, int nbAcquaintances) {
        if (DEBUG) {
            System.out.println("addr [" + getTabPositionOf(addr) + "]:" + addr +
                " Acquaintances :" + nbAcquaintances);
        }
        cache[getTabPositionOf(addr)] = nbAcquaintances;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("USAGE:" + USAGE);
            return;
        }

        String addr = args[0] + P2PConstants.P2P_NODE_NAME;

        if (args[1].equalsIgnoreCase("dump")) {
            dumpP2PNetwork(addr);
        } else if (args[1].equalsIgnoreCase("trace")) {
            try {
                Tracer t = (Tracer) ProActiveObject.newActive(Tracer.class.getName(),
                        null);

                Node distNode = NodeFactory.getNode(addr);
                P2PService p2p = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];

                while (true) {
                    t.sendTrace(p2p);
                    Thread.sleep(TIME);
                    t.printCache();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[1].equalsIgnoreCase("reqnode")) {
            try {
                Node distNode = NodeFactory.getNode(addr);
                P2PService p2p = (P2PService) distNode.getActiveObjects(P2PService.class.getName())[0];
                System.out.println("DEBUT");
                P2PNodeLookup lookup = p2p.getNodes(1, "test0", "test1");
                System.out.println("FIN");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
