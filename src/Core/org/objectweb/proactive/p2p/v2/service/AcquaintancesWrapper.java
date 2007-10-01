package org.objectweb.proactive.p2p.v2.service;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class AcquaintancesWrapper implements Serializable {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_ACQUAINTANCES);
    private P2PService acquaintances_active = null;
    private Group groupOfAcquaintances = null;
    private ArrayList<String> urlList = new ArrayList<String>();

    public AcquaintancesWrapper() {
        try {
            acquaintances_active = (P2PService) ProGroup.newGroup(P2PService.class.getName());
            this.groupOfAcquaintances = ProGroup.getGroup(acquaintances_active);
        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(P2PService p) {
        return this.groupOfAcquaintances.contains(p);
    }

    public boolean add(P2PService p, String peerUrl) {
        boolean result = this.groupOfAcquaintances.add(p);

        //this.groupOfAcquaintances.indexOf(p);
        if (result) {
            //            try {
            logger.info("----- Adding " + peerUrl);
            urlList.add(P2PService.getHostNameAndPortFromUrl(peerUrl));
            //            } catch (UnknownHostException e) {
            //                e.printStackTrace();
            //            }
        }

        return result;
    }

    public boolean remove(P2PService p, String peerUrl) {
        logger.info("------ Removing " + p);
        //        try {
        urlList.remove(P2PService.getHostNameAndPortFromUrl(peerUrl));
        //		} catch (UnknownHostException e) {
        //			e.printStackTrace();
        //		}
        return this.groupOfAcquaintances.remove(p);
    }

    public P2PService get(int i) {
        return (P2PService) this.groupOfAcquaintances.get(i);
    }

    public P2PService getAcquaintances() {
        return this.acquaintances_active;
    }

    public Group getAcquaintancesAsGroup() {
        return groupOfAcquaintances;
    }

    public int size() {
        return this.groupOfAcquaintances.size();
    }

    //    public void dumpAcquaintances() {
    //        Iterator it = urlList.iterator();
    //        logger.info("***********************");
    //        while (it.hasNext()) {
    //            logger.info(it.next());
    //        }
    //        logger.info("***********************");
    //    }
    public String[] getAcquaintancesURLs() {
        return (String[]) urlList.toArray(new String[] {  });
    }

    /*
     * TODO: handle exceptions with urlList.remove(index); for each
     * member in the ExceptionListException
     */
}
