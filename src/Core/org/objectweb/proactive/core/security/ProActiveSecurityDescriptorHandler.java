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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.exceptions.InvalidPolicyFile;
import org.objectweb.proactive.core.security.securityentity.CertificatedRuleEntity;
import org.objectweb.proactive.core.security.securityentity.NamedRuleEntity;
import org.objectweb.proactive.core.security.securityentity.RuleEntities;
import org.objectweb.proactive.core.security.securityentity.RuleEntity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.SingleValueUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.StreamReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * @author acontes
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ProActiveSecurityDescriptorHandler
    extends AbstractUnmarshallerDecorator {
    //	protected PolicyServer policyServer;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.SECURITY);
    protected static final String PROACTIVE_SECURITY_TAG = "Policy";
    protected static final String RULE_TAG = "Rule";
    protected static final String ENTITY_TAG = "Entity";
    protected static final String RULES_TAG = "Rules";
    protected static final String ACCESS_TAG = "AccessRights";
    protected static final String TRUSTED_CERTIFICATION_AUTHORITY_TAG = "TrustedCertificationAuthority";
    protected static final String ENTITY_FROM_TAG = "From";
    protected static final String ENTITY_TO_TAG = "To";
    protected static final String RULE_COMMUNICATION_TAG = "Communication";
    protected static final String RULE_COMMUNICATION_REQUEST_TAG = "Request";
    protected static final String RULE_COMMUNICATION_REPLY_TAG = "Reply";
    protected static final String RULE_COMMUNICATION_MIGRATION_TAG = "Migration";
    protected static final String RULE_COMMUNICATION_AOCREATION_TAG = "OACreation";
    protected static final String RULE_COMMUNICATION_ATTRIBUTES_TAG = "Attributes";
    protected static final String RULE_MIGRATION_AUTHORIZED = "authorized";
    protected static final String RULE_MIGRATION_DENIED = "denied";
    protected static final String RULE_AOCREATION_AUTHORIZED = "authorized";
    protected static final String APPLICATION_NAME_TAG = "ApplicationName";
    protected static final String PKCS12_KEYSTORE = "PKCS12KeyStore";
    private String descriptorUrl;
    private List<PolicyRule> policyRules;
    private String applicationName;
    private List<RuleEntity> accessAuthorizations;
    protected KeyStore keystore;

    static {
        ProActiveSecurity.loadProvider();
    }

    /**
     *
     */
    public ProActiveSecurityDescriptorHandler(String descriptorUrl) {
        super();
        Provider myProvider = new BouncyCastleProvider();
        Security.addProvider(myProvider);
        this.descriptorUrl = descriptorUrl;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String,
     *      org.objectweb.proactive.core.xml.io.Attributes)
     */
    public void startContextElement(String name, Attributes attributes) {
        addHandler(APPLICATION_NAME_TAG, new SingleValueUnmarshaller());
        addHandler(PKCS12_KEYSTORE, new SingleValueUnmarshaller());
        addHandler(RULES_TAG, new RulesHandler());
        addHandler(ACCESS_TAG, new AccessHandler());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String,
     *      org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    @Override
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
        if (name.equals(RULES_TAG)) {
            List<PolicyRule> policyRules = (List<PolicyRule>) activeHandler.getResultObject();
            this.policyRules = policyRules;
        } else if (name.equals(APPLICATION_NAME_TAG)) {
            String applicationName = (String) activeHandler.getResultObject();
            this.applicationName = applicationName;
        } else if (name.equals(PKCS12_KEYSTORE)) {
            String pkcs12Keystore = (String) activeHandler.getResultObject();
            try {
                File keyStoreFile = new File(pkcs12Keystore);
                if (!keyStoreFile.exists()) {
                    // the url does not exist as a complete path
                    // try it as a relative path from the current descriptor location
                    String parentDirectory = new File(this.descriptorUrl).getParent();
                    keyStoreFile = new File(parentDirectory + File.separator +
                            pkcs12Keystore);
                }
                this.keystore = KeyStore.getInstance("PKCS12", "BC");
                this.keystore.load(new FileInputStream(keyStoreFile),
                    "ha".toCharArray());
            } catch (KeyStoreException e) {
                e.printStackTrace();
                this.keystore = null;
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
                this.keystore = null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                this.keystore = null;
            } catch (CertificateException e) {
                e.printStackTrace();
                this.keystore = null;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                this.keystore = null;
            } catch (IOException e) {
                e.printStackTrace();
                this.keystore = null;
            }
        } else if (name.equals(ACCESS_TAG)) {
            RuleEntities entities = (RuleEntities) activeHandler.getResultObject();
            this.accessAuthorizations = entities;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() {
        try {
            if (this.policyRules == null) {
                this.policyRules = new ArrayList<PolicyRule>();
            }
            if (this.accessAuthorizations == null) {
                this.accessAuthorizations = new ArrayList<RuleEntity>();
            }
            return new PolicyServer(this.keystore, this.policyRules,
                this.applicationName, this.descriptorUrl,
                this.accessAuthorizations);
        } catch (NullPointerException npe) {
            return null;
        }
    }

    /**
     * This class receives Security events
     */
    private class RulesHandler extends AbstractUnmarshallerDecorator {
        private List<PolicyRule> policies;

        public RulesHandler() {
            super();
        }

        public void startContextElement(String name, Attributes attributes) {
            this.policies = new ArrayList<PolicyRule>();
            addHandler(RULE_TAG, new RuleHandler());
        }

        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(RULE_TAG)) {
                Object resultObject = activeHandler.getResultObject();
                if (resultObject != null) {
                    this.policies.add((PolicyRule) resultObject);
                }
            }

            //			addHandler(RULE_TAG, new RuleHandler());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            return this.policies;
        }
    }

    // end inner class RulesHandler

    /**
     * Receives deployment events
     */
    private static class InitialHandler extends AbstractUnmarshallerDecorator {
        private PolicyServer ps;

        protected InitialHandler(String xmlDescriptorUrl) {
            super();
            this.addHandler(PROACTIVE_SECURITY_TAG,
                new ProActiveSecurityDescriptorHandler(xmlDescriptorUrl));
        }

        public Object getResultObject() {
            return this.ps;
        }

        public void startContextElement(String name, Attributes attributes) {
            // nothing
        }

        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(PROACTIVE_SECURITY_TAG)) {
                this.ps = (PolicyServer) activeHandler.getResultObject();
            }
        }
    }

    /**
     * This class receives Security events
     */
    private class RuleHandler extends AbstractUnmarshallerDecorator {
        private RuleEntities from;
        private RuleEntities to;
        private Communication request;
        private Communication reply;
        private boolean aoCreation;
        private boolean migration;

        public RuleHandler() {
            super();
        }

        public void startContextElement(String name, Attributes attributes) {
            addHandler(ENTITY_FROM_TAG, new EntityCollector());
            addHandler(ENTITY_TO_TAG, new EntityCollector());
            addHandler(RULE_COMMUNICATION_TAG,
                new CommunicationCollectionHandler());
            addHandler(RULE_COMMUNICATION_AOCREATION_TAG,
                new SingleValueUnmarshaller());
            addHandler(RULE_COMMUNICATION_MIGRATION_TAG,
                new SingleValueUnmarshaller());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String,
         *      org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            Object resultObject = activeHandler.getResultObject();
            if (resultObject != null) {
                if (name.equals(ENTITY_FROM_TAG)) {
                    this.from = (RuleEntities) resultObject;
                } else if (name.equals(ENTITY_TO_TAG)) {
                    this.to = (RuleEntities) resultObject;
                } else if (name.equals(RULE_COMMUNICATION_TAG)) {
                    Communications comms = (Communications) resultObject;
                    this.request = comms.getRequest();
                    this.reply = comms.getReply();
                } else if (name.equals(RULE_COMMUNICATION_AOCREATION_TAG)) {
                    String value = (String) resultObject;
                    this.aoCreation = value.equals(RULE_AOCREATION_AUTHORIZED);
                } else if (name.equals(RULE_COMMUNICATION_MIGRATION_TAG)) {
                    String value = (String) resultObject;
                    this.migration = value.equals(RULE_MIGRATION_AUTHORIZED);
                }
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            try {
                return new PolicyRule(this.from, this.to, this.request,
                    this.reply, this.aoCreation, this.migration);
            } catch (NullPointerException npe) {
                return null;
            }
        }
    }

    // end inner class RulesHandler
    private class EntityCollector extends AbstractUnmarshallerDecorator {
        private RuleEntities entities;

        public EntityCollector() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String,
         *      org.objectweb.proactive.core.xml.io.Attributes)
         */
        public void startContextElement(String name, Attributes attributes) {
            this.entities = new RuleEntities();
            addHandler(ENTITY_TAG, new EntityHandler());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String,
         *      org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            RuleEntity re = (RuleEntity) activeHandler.getResultObject();
            if (re == null) {
                this.entities = null;
            }
            if (this.entities != null) {
                this.entities.add(re);
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            return this.entities;
        }
    }

    /**
     * This class receives Security events
     */
    private class EntityHandler extends BasicUnmarshaller {
        private RuleEntity entity;

        public EntityHandler() {
            super();
        }

        @Override
        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            EntityType type = EntityType.fromString(attributes.getValue("type"));
            String value = attributes.getValue("name");
            KeyStore keystore = ProActiveSecurityDescriptorHandler.this.keystore;

            switch (type) {
            case OBJECT:
            case ENTITY:
            case NODE:
            case RUNTIME:
                try {
                    this.entity = new NamedRuleEntity(type, keystore, value);
                } catch (KeyStoreException e1) {
                    e1.printStackTrace();
                }
                break;
            case APPLICATION:
            case USER:
            case DOMAIN:
                try {
                    this.entity = new CertificatedRuleEntity(type, keystore,
                            value);
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                break;
            default:

                // nothing
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        @Override
        public Object getResultObject() {
            return this.entity;
        }
    }

    // end inner class EntityHandler
    private class CommunicationCollectionHandler
        extends AbstractUnmarshallerDecorator {
        private Communication request;
        private Communication reply;

        public CommunicationCollectionHandler() {
            super();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.String,
         *      org.objectweb.proactive.core.xml.io.Attributes)
         */
        public void startContextElement(String name, Attributes attributes) {
            addHandler(RULE_COMMUNICATION_REPLY_TAG, new CommunicationHandler());
            addHandler(RULE_COMMUNICATION_REQUEST_TAG,
                new CommunicationHandler());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String,
         *      org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(RULE_COMMUNICATION_REPLY_TAG)) {
                this.reply = (Communication) activeHandler.getResultObject();
                // System.out.println("TAG FROM !!!!" + communication[0]);
            } else if (name.equals(RULE_COMMUNICATION_REQUEST_TAG)) {
                this.request = (Communication) activeHandler.getResultObject();
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            return new Communications(this.request, this.reply);
        }
    }

    /**
     * This class receives Security events
     */
    private class CommunicationHandler extends AbstractUnmarshallerDecorator {
        private Communication comm;
        private boolean allowed;

        public CommunicationHandler() {
            super();
        }

        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            addHandler(RULE_COMMUNICATION_ATTRIBUTES_TAG,
                new CommunicationAttributesHandler());

            this.allowed = attributes.getValue("value")
                                     .equalsIgnoreCase("authorized");
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(java.lang.String,
         *      org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
         */
        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            if (name.equals(RULE_COMMUNICATION_ATTRIBUTES_TAG)) {
                Authorizations attr = (Authorizations) activeHandler.getResultObject();
                this.comm = new Communication(this.allowed,
                        attr.getAuthentication(), attr.getConfidentiality(),
                        attr.getIntegrity());
            }
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            // logger.info(" communication : "+ communication);
            return this.comm;
        }
    }

    /**
     * This class receives Security events
     */
    private class CommunicationAttributesHandler extends BasicUnmarshaller {
        private Authorizations attributes;

        public CommunicationAttributesHandler() {
            super();
        }

        @Override
        public void startContextElement(String name, Attributes attributes)
            throws SAXException {
            this.attributes = new Authorizations(Authorization.fromString(
                        attributes.getValue("authentication")),
                    Authorization.fromString(attributes.getValue(
                            "confidentiality")),
                    Authorization.fromString(attributes.getValue("integrity")));
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        @Override
        public Object getResultObject() {
            return this.attributes;
        }
    }

    // end inner class CommunicationHandler
    private class AccessHandler extends AbstractUnmarshallerDecorator {
        private RuleEntities entities;

        public AccessHandler() {
            super();
        }

        public void startContextElement(String name, Attributes attributes) {
            this.entities = new RuleEntities();
            addHandler(ENTITY_TAG, new EntityHandler());
        }

        @Override
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws SAXException {
            // new handler otherwise all policies reference the same object,
            // maybe there is another thing to do
            // addHandler(RULE_TAG, new RuleHandler());
            // ruleHandler = new RuleHandler();
            if (name.equals(ENTITY_TAG)) {
                RuleEntity entity = (RuleEntity) activeHandler.getResultObject();
                if ((entity != null) && (entity.getType() == EntityType.USER)) {
                    this.entities.add(entity);
                }
            }
            addHandler(RULE_TAG, new RuleHandler());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
         */
        public Object getResultObject() {
            return this.entities;
        }
    }

    /**
     * Creates ProActiveDescriptor object from XML Descriptor
     *
     * @param xmlDescriptorUrl
     *            the URL of XML Descriptor
     */
    public static PolicyServer createPolicyServer(String xmlDescriptorUrl)
        throws InvalidPolicyFile {
        // static method added to replace main method
        try {
            InitialHandler h = new InitialHandler(xmlDescriptorUrl);

            StreamReader sr = new StreamReader(new InputSource(xmlDescriptorUrl),
                    h);
            sr.read();

            return (PolicyServer) h.getResultObject();
        } catch (Exception e) {
            e.printStackTrace();
            ProActiveLogger.getLogger(Loggers.SECURITY)
                           .warn("a problem occurs when getting the security part of the ProActiveDescriptorHandler at location \"" +
                xmlDescriptorUrl + "\".");
            throw new InvalidPolicyFile(e);
        }
    }

    private class Communications {
        private Communication request;
        private Communication reply;

        public Communications(Communication request, Communication reply) {
            this.request = request;
            this.reply = reply;
        }

        public Communication getReply() {
            return this.reply;
        }

        public Communication getRequest() {
            return this.request;
        }
    }

    private class Authorizations {
        private Authorization authentication;
        private Authorization confidentiality;
        private Authorization integrity;

        public Authorizations(Authorization authentication,
            Authorization confidentiality, Authorization integrity) {
            this.authentication = authentication;
            this.confidentiality = confidentiality;
            this.integrity = integrity;
        }

        public Authorization getAuthentication() {
            return this.authentication;
        }

        public Authorization getConfidentiality() {
            return this.confidentiality;
        }

        public Authorization getIntegrity() {
            return this.integrity;
        }

        @Override
        public String toString() {
            String s = "";
            s += ("\n\nAuth : " + this.authentication);
            s += ("\nConf : " + this.confidentiality);
            s += ("\nInt : " + this.integrity);
            return s;
        }
    }
}
