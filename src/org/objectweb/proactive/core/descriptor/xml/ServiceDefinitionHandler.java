/*
 * Created on 23 juil. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.services.P2PLookupService;
import org.objectweb.proactive.core.descriptor.services.RMIRegistryLookupService;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;


/**
 * @author rquilici
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServiceDefinitionHandler extends PassiveCompositeUnmarshaller
    implements ProActiveDescriptorConstants {
    ProActiveDescriptor pad;
    protected String serviceId;

    public ServiceDefinitionHandler(ProActiveDescriptor pad) {
        super(false);
        this.pad = pad;
        this.addHandler(RMI_LOOKUP_TAG, new RMILookupHandler());
        this.addHandler(P2P_LOOKUP_TAG, new P2PLookupHandler());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        UniversalService service = (UniversalService) activeHandler.getResultObject();
        pad.addService(serviceId, service);

        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        this.serviceId = attributes.getValue("id");
    }

    protected class RMILookupHandler extends BasicUnmarshaller {
        public RMILookupHandler() {
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            String lookupUrl = attributes.getValue("url");
            RMIRegistryLookupService rmiService = new RMIRegistryLookupService(lookupUrl);
            setResultObject(rmiService);
        }
    } // end of inner class RMILookupHandler

    protected class P2PLookupHandler extends PassiveCompositeUnmarshaller {
        protected P2PLookupService p2pService;

        public P2PLookupHandler() {
            super(false);
            CollectionUnmarshaller ch = new CollectionUnmarshaller(String.class);
            ch.addHandler(PEER_TAG, new SingleValueUnmarshaller());
            this.addHandler(PEERS_SET_TAG, ch);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            p2pService = new P2PLookupService();
            String nodeNumberMax = attributes.getValue("nodeNumber");
            if (checkNonEmpty(nodeNumberMax)) {
                if (nodeNumberMax.equals("MAX")) {
                    p2pService.setNodeNumberToMAX();
                } else {
                    p2pService.setNodeNumber(new Integer(nodeNumberMax).intValue());
                }
            }

            //            String minNodeNumber = attributes.getValue("minNodeNumber");
            //            if(checkNonEmpty(minNodeNumber)){
            //                p2pService.setMinNodeNumber(new Integer(minNodeNumber).intValue());
            //            }
            String timeout = attributes.getValue("timeout");
            if (checkNonEmpty(timeout)) {
                if (timeout.equals("MAX")) {
                    // -1 means infinite timeout
                    p2pService.setTimeout(new Integer(-1).longValue());
                } else {
                    p2pService.setTimeout(new Integer(timeout).longValue());
                }
            }
            String lookupFrequence = attributes.getValue("lookupFrequence");
            if (checkNonEmpty(lookupFrequence)) {
                p2pService.setLookupFrequence(new Integer(lookupFrequence).intValue());
            }
            String TTL = attributes.getValue("TTL");
            if (checkNonEmpty(TTL)) {
                p2pService.setTTL(new Integer(TTL).intValue());
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            String[] peerList = (String[]) activeHandler.getResultObject();
            p2pService.setPeerList(peerList);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return p2pService;
        }

        private class SingleValueUnmarshaller extends BasicUnmarshaller {
            public void readValue(String value) throws org.xml.sax.SAXException {
                //System.out.println("SingleValueUnmarshaller.readValue() " + value);
                setResultObject(value);
            }
        }
    } // end of inner class P2PLookupHandler
}
