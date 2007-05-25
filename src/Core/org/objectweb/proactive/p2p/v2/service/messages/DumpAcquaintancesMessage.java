package org.objectweb.proactive.p2p.v2.service.messages;

import java.io.Serializable;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.util.UrlBuilder;
import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class DumpAcquaintancesMessage extends BreadthFirstMessage implements Serializable {
	
	public DumpAcquaintancesMessage() {}
	
    public DumpAcquaintancesMessage(int ttl, UniversalUniqueID id,
        P2PService sender) {
        super(ttl, id, sender);
    }

    public void execute(P2PService target) {
//        try {
            System.out.println("***** " +
                P2PService.getHostNameAndPortFromUrl(target.getAddress()
                                                           .toString()) +
                " *****");
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        }

        String[] v =  (String[]) target.getAcquaintanceManager().getAcquaintancesURLs().toArray(new String[] {});

        for (int i = 0; i < v.length; i++) {
            System.out.println(v[i]);
        }

        System.out.println("*****  *****");
        System.out.println("DumpAcquaintancesMessage.execute() " + TTL);
    }
}
