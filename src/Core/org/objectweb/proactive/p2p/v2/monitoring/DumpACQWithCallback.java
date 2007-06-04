package org.objectweb.proactive.p2p.v2.monitoring;

import org.objectweb.proactive.p2p.v2.service.P2PService;
import org.objectweb.proactive.p2p.v2.service.messages.DumpAcquaintancesMessage;
import org.objectweb.proactive.p2p.v2.service.util.UniversalUniqueID;


public class DumpACQWithCallback extends DumpAcquaintancesMessage {
    protected Dumper d;

    public DumpACQWithCallback(int ttl, UniversalUniqueID id, P2PService sender) {
        super(ttl, id, sender);
    }

    public DumpACQWithCallback(int ttl, UniversalUniqueID id, Dumper d) {
        //this(ttl,id,sender);
        this.TTL = ttl;
        this.uuid = id;
        this.d = d;
    }

    public void execute(P2PService target) {
        //        try {
        AcquaintanceInfo info = new AcquaintanceInfo(P2PService.getHostNameAndPortFromUrl(
                    target.getAddress().toString()),
                (String[]) target.getAcquaintanceManager().getAcquaintancesURLs()
                                 .toArray(new String[] {  }),
                target.getAcquaintanceManager().getMaxNOA(),
                target.acquaintanceManager.size().intValue(),
                target.acquaintanceManager.getAwaitedRepliesUrls());

        this.d.receiveAcqInfo(info);
        //        } catch (UnknownHostException e) {
        //            e.printStackTrace();
        //        }
    }
}
