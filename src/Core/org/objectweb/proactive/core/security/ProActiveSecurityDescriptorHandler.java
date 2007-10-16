/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.security;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.core.security.securityentity.DefaultEntity;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.security.securityentity.EntityVirtualNode;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.SingleValueUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ProActiveSecurityDescriptorHandler
    extends AbstractUnmarshallerDecorator {
    protected PolicyServer policyServer;
    protected X509Certificate applicationCertificate;
    protected String pkcs12Keystore = null;
    protected String applicationName = null;
    protected String applicationPrivateKeyPath = null;
    protected String applicationCertificatePath = null;
    protected ArrayList<PolicyRule> policyRules = null;
    static Logger logger = ProActiveLogger.getLogger(Loggers.SECURITY);
    protected static String PROACTIVE_SECURITY_TAG = "Policy";
    protected String RULE_TAG = "Rule";
    protected String ENTITY_TAG = "Entity";
    protected String RULES_TAG = "Rules";
    protected String PRIVATE_KEY_TAG = "PrivateKey";
    protected String CERTIFICATE_TAG = "Certificate";
    protected String TRUSTED_CERTIFICATION_AUTHORITY_TAG = "TrustedCertificationAuthority";
    protected String ENTITY_FROM_TAG = "From";
    protected String ENTITY_TO_TAG = "To";
    protected String RULE_COMMUNICATION_TAG = "Communication";
    protected String RULE_COMMUNICATION_TO_TAG = "Request";
    protected String RULE_COMMUNICATION_FROM_TAG = "Reply";
    protected String RULE_COMMUNICATION_MIGRATION_TAG = "Migration";
    protected String RULE_COMMUNICATION_AOCREATION_TAG = "OACreation";
    protected String RULE_COMMUNICATION_ATTRIBUTES_TAG = "Attributes";
    protected String RULE_MIGRATION_AUTHORIZED = "authorized";
    protected String RULE_MIGRATION_DENIED = "denied";
    protected String RULE_AOCREATION_AUTHORIZED = "authorized";
    protected String APPLICATION_NAME_TAG = "ApplicationName";
    protected String PKCS12_CERTIFICATE = "PKCS12KeyStore";

    static {
        ProActiveSecurity.loadProvider();
    }

    /**
     *
     */
    public ProActiveSecurityDescriptorHandler() {
        super();
        Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        Security.addProvider(myProvider);
        policyServer = new PolicyServer();
        addHandler(APPLICATION_NAME_TAG, new SingleValueUnmarshaller());
        addHandler(PRIVATE_KEY_TAG, new SingleValueUnmarshaller());
        addHandler(CERTIFICATE_TAG, new SingleValueUnmarshaller());
        addHandler(PKCS12_CERTIFICATE, new SingleValueUnmarshaller());

        addHandler(RULES_TAG, new RulesHandler());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    @Override
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        //        if (name.equals(PRIVATE_KEY_TAG)) {
        //           applicationPrivateKeyPath = (String) activeHandler.getResultObject();
        //           policyServer.setApplicationPrivateKey(applicationPrivateKeyPath);
        //        } else if (name.equals(CERTIFICATE_TAG)) {
        //            applicationCertificatePath = (String) activeHandler.getResultObject();
        //            policyServer.setApplicationCertificate(applicationCertificatePath);
        //        } else 
        if (name.equals(RULES_TAG)) {
            policyRules = (ArrayList) activeHandler.getResultObject();
            policyServer.setPolicies(policyRules);
        } else if (name.equals(APPLICATION_NAME_TAG)) {
            applicationName = (String) activeHandler.getResultObject();
            policyServer.setApplicationName(applicationName);
        } else if (name.equals(PKCS12_CERTIFICATE)) {
            pkcs12Keystore = (String) activeHandler.getResultObject();
            policyServer.setPKCS12Keystore(pkcs12Keystore);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        //        if (pkcs12Keystore != null) {
        //            KeyStore keyStore = null;
        //        
        //        try {
        //             keyStore = KeyStore.getInstance("PKCS12", "BC");
        //        keyStore.load(new FileInputStream(pkcs12Keystore), "ha".toCharArray());
        //        } catch (Exception e ) {
        //            e.printStackTrace();
        //        }
        //        return new PolicyServer(keyStore, policyRules);
        //    } else {
        //        policyServer = new PolicyServer();
        //        policyServer.setApplicationPrivateKey(applicationPrivateKeyPath);
        //        policyServer.setApplicationCertificate(applicationCertificatePath);
        //        policyServer.setPolicies(policyRules);
        //        policyServer.setApplicationName(applicationName);
        return policyServer;
        //    }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
    }

    /**
     * This class receives Security events
     */
    private class RulesHandler extends AbstractUnmarshallerDecorator {
        RuleHandler ruleHandler = null;
        private ArrayList<PolicyRule> policies;

        public RulesHandler() {
            super();
            policies = new ArrayList<PolicyRule>();
            ruleHandler = new RuleHandler();
            addHandler(RULE_TAG, ruleHandler);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            if (name.equals(RULE_TAG)) {
                //policies.add(activeHandler.getResultObject());
                // ruleHandler = new RuleHandler();
            }
        }

        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            // new handler otherwise all policies reference the same object, maybe there is another thing to do
            // addHandler(RULE_TAG, new RuleHandler());
            // ruleHandler = new RuleHandler();
            if (name.equals(RULE_TAG)) {
                policies.add((PolicyRule) activeHandler.getResultObject());
                //	  ruleHandler = new RuleHandler();
            }
            addHandler(RULE_TAG, new RuleHandler());
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return policies;
        }
    }

    // end inner class RulesHandler

    /**
     * Receives deployment events
     */
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        // line added to return a ProactiveDescriptorHandler object
        private ProActiveSecurityDescriptorHandler proActiveSecurityDescriptorHandler;
        protected PolicyServer ps;

        private InitialHandler() {
            super();
            proActiveSecurityDescriptorHandler = new ProActiveSecurityDescriptorHandler();
            this.addHandler(PROACTIVE_SECURITY_TAG,
                proActiveSecurityDescriptorHandler);
        }

        public Object getResultObject() throws org.xml.sax.SAXException {
            return ps; //(PolicyServer) proActiveSecurityDescriptorHandler.getResultObject();
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            if (name.equals(PROACTIVE_SECURITY_TAG)) {
                ps = (PolicyServer) activeHandler.getResultObject();
            }
        }
    }

    /**
     * This class receives Security events
     */
    private class RuleHandler extends AbstractUnmarshallerDecorator {
        private PolicyRule policy;

        public RuleHandler() {
            super();
            policy = new PolicyRule();
            addHandler(ENTITY_FROM_TAG, new EntityCollector());
            addHandler(ENTITY_TO_TAG, new EntityCollector());
            addHandler(RULE_COMMUNICATION_TAG,
                new CommunicationCollectionHandler());
            addHandler(RULE_COMMUNICATION_AOCREATION_TAG,
                new SingleValueUnmarshaller());
            addHandler(RULE_COMMUNICATION_MIGRATION_TAG,
                new SingleValueUnmarshaller());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            policy = new PolicyRule();
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(ENTITY_FROM_TAG)) {
                policy.setEntitiesFrom((ArrayList) activeHandler.getResultObject());
            } else if (name.equals(ENTITY_TO_TAG)) {
                policy.setEntitiesTo((ArrayList) activeHandler.getResultObject());
            } else if (name.equals(RULE_COMMUNICATION_TAG)) {
                policy.setCommunicationRules((Communication[]) activeHandler.getResultObject());
            } else if (name.equals(RULE_COMMUNICATION_AOCREATION_TAG)) {
                String value = (String) activeHandler.getResultObject();
                boolean b;
                if (value.equals(RULE_AOCREATION_AUTHORIZED)) {
                    b = true;
                } else {
                    b = false;
                }
                policy.setAocreation(b);
            } else if (name.equals(RULE_COMMUNICATION_MIGRATION_TAG)) {
                String value = (String) activeHandler.getResultObject();
                boolean b;
                if (value.equals(RULE_MIGRATION_AUTHORIZED)) {
                    b = true;
                } else {
                    b = false;
                }
                policy.setMigration(b);
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return policy;
        }
    }

    // end inner class RulesHandler
    private class EntityCollector extends AbstractUnmarshallerDecorator {
        private ArrayList entities;

        public EntityCollector() {
            entities = new ArrayList();
            addHandler(ENTITY_TAG, new EntityHandler());
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            entities.add(activeHandler.getResultObject());
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return entities;
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
         */
        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }
    }

    /**
     * This class receives Security events
     */
    private class EntityHandler extends BasicUnmarshaller {
        private Entity entity;

        public EntityHandler() {
            super();
        }

        @Override
        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            if (attributes.getValue("type").equals("VN")) {
                entity = new EntityVirtualNode(attributes.getValue("name"),
                        policyServer.getApplicationCertificate(), null);
            } else if (attributes.getValue("type").equals("DefaultVirtualNode")) {
                entity = new DefaultEntity();
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        @Override
        public Object getResultObject() throws SAXException {
            return entity;
        }
    }

    // end inner class EntityHandler
    private class CommunicationCollectionHandler
        extends AbstractUnmarshallerDecorator {
        private Communication[] communication;

        public CommunicationCollectionHandler() {
            super();
            communication = new Communication[2];
            addHandler(RULE_COMMUNICATION_FROM_TAG, new CommunicationHandler());
            addHandler(RULE_COMMUNICATION_TO_TAG, new CommunicationHandler());
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(RULE_COMMUNICATION_FROM_TAG)) {
                communication[0] = (Communication) activeHandler.getResultObject();
                //                System.out.println("TAG FROM !!!!" + communication[0]);
            } else if (name.equals(RULE_COMMUNICATION_TO_TAG)) {
                communication[1] = (Communication) activeHandler.getResultObject();
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            return communication;
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String, org.objectweb.proactive.core.xml.io.Attributes)
         */
        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
        }
    }

    /**
     * This class receives Security events
     */
    private class CommunicationHandler extends AbstractUnmarshallerDecorator {
        private Communication communication;

        public CommunicationHandler() {
            super();

            addHandler(RULE_COMMUNICATION_ATTRIBUTES_TAG,
                new CommunicationAttributesHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(RULE_COMMUNICATION_ATTRIBUTES_TAG)) {
                communication = (Communication) activeHandler.getResultObject();
            }
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() throws SAXException {
            //logger.info(" communication : "+ communication);
            return communication;
        }
    }

    /**
     * This class receives Security events
     */
    private class CommunicationAttributesHandler extends BasicUnmarshaller {
        private Communication communication;

        public CommunicationAttributesHandler() {
            super();
        }

        @Override
        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            communication = new Communication(convert(attributes.getValue(
                            "authentication")),
                    convert(attributes.getValue("integrity")),
                    convert(attributes.getValue("confidentiality")));
        }

        /* (non-Javadoc)
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        @Override
        public Object getResultObject() throws SAXException {
            return communication;
        }
    }

    // end inner class CommunicationHandler

    /**
     * Creates ProActiveDescriptor object from XML Descriptor
     * @param xmlDescriptorUrl the URL of XML Descriptor
     */
    public static PolicyServer createPolicyServer(String xmlDescriptorUrl)
        throws InvalidPolicyFile {
        //static method added to replace main method
        String uri = null;
        try {
            InitialHandler h = new InitialHandler();

            // ProActiveSecurityDescriptorHandler h = new ProActiveSecurityDescriptorHandler();
            uri = xmlDescriptorUrl;
            org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(
                        uri), h);
            sr.read();
            ((PolicyServer) h.getResultObject()).setPolicyRulesFileLocation(uri);

            return (PolicyServer) h.getResultObject();
        } catch (Exception e) {
            e.printStackTrace();
            ProActiveLogger.getLogger(Loggers.SECURITY)
                           .warn("a problem occurs when getting the security part of the ProActiveDescriptorHandler at location \"" +
                uri + "\".");
            throw new InvalidPolicyFile(e);
        }
    }

    private int convert(String name) {
        if (name == null) {
            return Communication.OPTIONAL;
        }
        if (name.equals("required") || name.equals("allowed") ||
                name.equals("authorized")) {
            return Communication.REQUIRED;
        } else if (name.equals("denied")) {
            return Communication.DENIED;
        } else {
            return Communication.OPTIONAL;
        }
    }

    public static void main(String[] args)
        throws IOException, org.xml.sax.SAXException {
        InitialHandler h = new InitialHandler();

        // ProActiveSecurityDescriptorHandler h = new ProActiveSecurityDescriptorHandler();
        String uri = "/net/home/acontes/dev/ProActive/descriptors/scurrav2.xml";
        org.objectweb.proactive.core.xml.io.StreamReader sr = new org.objectweb.proactive.core.xml.io.StreamReader(new org.xml.sax.InputSource(
                    args[0]), h);
        sr.read();
    }
}
