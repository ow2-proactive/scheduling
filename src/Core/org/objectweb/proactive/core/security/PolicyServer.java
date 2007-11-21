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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.security.securityentity.RuleEntities;
import org.objectweb.proactive.core.security.securityentity.RuleEntity;
import org.objectweb.proactive.core.security.securityentity.RuleEntity.Match;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The PolicyServer class contains entity's policy rules and application's
 * certificate and private key
 *
 */
public class PolicyServer implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 6881821067929081660L;
    private static final Logger log = ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER);
    private final List<PolicyRule> policyRules;
    private final RuleEntities accessAuthorizations;
    private final String policyRulesFileLocation;
    private final String applicationName;
    private final SerializableKeyStore keyStore;

    public PolicyServer() {
        this.policyRules = new ArrayList<PolicyRule>();

        this.accessAuthorizations = new RuleEntities();

        this.policyRulesFileLocation = new String();
        this.applicationName = new String();

        this.keyStore = new SerializableKeyStore(null);
    }

    public PolicyServer(KeyStore keyStore, Collection<PolicyRule> policyRules,
        String applicationName, String descriptorLocation,
        Collection<RuleEntity> accessAuthorizations) {
        if ((keyStore == null) || (policyRules == null) ||
                (applicationName == null) || (descriptorLocation == null) ||
                (accessAuthorizations == null)) {
            throw new NullPointerException();
        }

        ProActiveSecurity.loadProvider();
        this.policyRules = new ArrayList<PolicyRule>();
        this.policyRules.addAll(policyRules);

        this.accessAuthorizations = new RuleEntities();
        this.accessAuthorizations.addAll(accessAuthorizations);

        this.policyRulesFileLocation = descriptorLocation;
        this.applicationName = applicationName;

        this.keyStore = new SerializableKeyStore(keyStore);
    }

    // public PolicyServer(PolicyRule[] policyRules) {
    // this();
    // for (PolicyRule element : policyRules) {
    // this.policyRules.add(element);
    // }
    // }

    // public PolicyServer(KeyStore keyStore, Collection<PolicyRule>
    // policyRules) {
    // this();
    // this.policyRules.addAll(policyRules);
    // }

    // public PolicyServer(KeyStore keyStore, Collection<PolicyRule>
    // policyRules) {
    // this(policyRules);
    // this.keyStore = new SerializableKeyStore(keyStore);
    // }
    public SecurityContext getPolicy(Entities local, Entities distant)
        throws SecurityNotAvailableException {
        if (this.policyRules == null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                           .debug("trying to find a policy whereas none has been set" +
                this + "    " + this.policyRules);
            throw new SecurityNotAvailableException();
        }

        if (ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                               .isDebugEnabled()) {
            String s = "================================\nLocal : " +
                local.toString();
            s += ("\nDistant : " + distant.toString());
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                           .debug(s + "\n=================================\n");
        }

        // getting all rules matching the context
        List<PolicyRule> matchingRules = new ArrayList<PolicyRule>();
        for (PolicyRule policy : this.policyRules) {
            // testing if <From> tag matches <From> entities
            RuleEntities policyEntitiesFrom = policy.getEntitiesFrom();

            Match matchingFrom = policyEntitiesFrom.match(local);

            // testing if <To> tag matches <To> entities
            RuleEntities policyEntitiesTo = policy.getEntitiesTo();

            Match matchingTo = policyEntitiesTo.match(distant);

            if (ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                                   .isDebugEnabled()) {
                ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                               .debug("evaluating policy " + policy);
            }

            //
            if ((matchingFrom != Match.FAILED) && (matchingTo != Match.FAILED)) {
                matchingRules.add(policy);
            } else {
                // check if the rule apply by switching from/to
                matchingFrom = policyEntitiesFrom.match(distant);
                matchingTo = policyEntitiesTo.match(local);

                if ((matchingFrom != Match.FAILED) &&
                        (matchingTo != Match.FAILED)) {
                    matchingRules.add(new PolicyRule(policy.getEntitiesTo(),
                            policy.getEntitiesFrom(),
                            policy.getCommunicationReply(),
                            policy.getCommunicationRequest(),
                            policy.isAoCreation(), policy.isMigration()));
                }
            }
        }

        // getting the most specific rule(s)
        List<PolicyRule> applicableRules = new ArrayList<PolicyRule>();
        for (PolicyRule matchingPolicy : matchingRules) {
            if (applicableRules.isEmpty()) {
                applicableRules.add(matchingPolicy);
            } else {
                boolean add = false;

                // level represents the specificity of the target entities of a
                // rule, higher level is more specific
                int fromLevel = matchingPolicy.getEntitiesFrom().getLevel();
                int toLevel = matchingPolicy.getEntitiesTo().getLevel();
                for (Iterator<PolicyRule> applicableRulesIterator = applicableRules.iterator();
                        applicableRulesIterator.hasNext();) {
                    PolicyRule applicableRule = applicableRulesIterator.next();
                    int applicableFromLevel = applicableRule.getEntitiesFrom()
                                                            .getLevel();
                    int applicableToLevel = applicableRule.getEntitiesTo()
                                                          .getLevel();

                    if ((fromLevel >= applicableFromLevel) &&
                            (toLevel >= applicableToLevel)) {
                        // current rule is more specific than the current
                        // applicableRule
                        applicableRulesIterator.remove();
                        add = true;
                    } else if ((fromLevel > applicableFromLevel) ||
                            (toLevel > applicableToLevel)) {
                        // current rule and current applicableRule both have to
                        // be applied
                        add = true;
                    }
                }
                if (add) {
                    applicableRules.add(matchingPolicy);
                }
            }
        }

        // resolving the applicable rules
        PolicyRule matchingPolicy;
        if (applicableRules.isEmpty()) {
            // defaul policy is not defined, we create one that forbids
            // everything
            matchingPolicy = new PolicyRule();
        } else {
            matchingPolicy = PolicyRule.mergePolicies(applicableRules);
        }

        ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                       .debug("Found Policy : " + matchingPolicy);

        return new SecurityContext(local, distant,
            matchingPolicy.getCommunicationRequest(),
            matchingPolicy.getCommunicationReply(),
            matchingPolicy.isAoCreation(), matchingPolicy.isMigration());
    }

    public List<PolicyRule> getPolicies() {
        return this.policyRules;
    }

    // public void setAccessAuthorization(RuleEntities entities) {
    // this.accessAuthorizations = entities;
    // }
    protected boolean hasAccessRights(Entity user) {
        if ((user == null) || (this.accessAuthorizations == null)) {
            return false;
        }

        return this.accessAuthorizations.contains(user);
    }

    public RuleEntities getAccessAuthorizations() {
        return this.accessAuthorizations;
    }

    @Override
    public String toString() {
        String s = null;
        s = "ApplicationName : " + this.applicationName + "\nfile: " +
            this.policyRulesFileLocation + "\n";
        for (int i = 0; i < this.policyRules.size(); i++) {
            s += this.policyRules.get(i);
        }

        return s;
    }

    // /**
    // * @param policies
    // */
    // public void setPolicies(List<PolicyRule> policies) {
    // ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
    // .info("storing policies");
    // this.policyRules = policies;
    // }

    /**
     * @param uri
     */

    // public void setPolicyRulesFileLocation(String uri) {
    // // for debug only
    // // set security file path
    // this.policyRulesFileLocation = uri;
    // }
    /**
     * @return application certificate
     */
    public TypedCertificate getApplicationCertificate() {
        if (this.keyStore != null) {
            try {
                return KeyStoreTools.getApplicationCertificate(this.keyStore.getKeyStore());
            } catch (KeyStoreException e) {
                e.printStackTrace();
                PolicyServer.log.error(
                    "Application certificate cannot be found in keystore.");
            } catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @return application certificate chain
     */
    public TypedCertificateList getApplicationCertificateChain() {
        if (this.keyStore != null) {
            try {
                return KeyStoreTools.getApplicationCertificateChain(this.keyStore.getKeyStore());
            } catch (KeyStoreException e) {
                e.printStackTrace();
                PolicyServer.log.error(
                    "Application certificate chain not found in keystore.");
            } catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @return application named appName certificate
     */
    public TypedCertificate getApplicationCertificate(String appName) {
        if ((this.keyStore != null) && (appName != null)) {
            try {
                return KeyStoreTools.getCertificate(this.keyStore.getKeyStore(),
                    EntityType.APPLICATION, appName);
            } catch (KeyStoreException e) {
                e.printStackTrace();
                PolicyServer.log.error("Application : " + appName +
                    " certificate not found in keystore.");
            } catch (UnrecoverableKeyException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Set application name
     *
     * @param applicationName
     */

    // public void setApplicationName(String applicationName) {
    // this.applicationName = applicationName;
    // }
    public String getApplicationName() {
        return this.applicationName;
    }

    @Override
    public Object clone() {
        PolicyServer clone = null;

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);

            out.writeObject(this);
            out.flush();
            out.close();

            bout.close();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                        bout.toByteArray()));

            clone = (PolicyServer) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clone;
    }

    public KeyStore getKeyStore() {
        return this.keyStore.getKeyStore();
    }

    // public void setPKCS12Keystore(String pkcs12Keystore) {
    // try {
    // KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC");
    // keyStore.load(new FileInputStream(pkcs12Keystore),
    // "ha".toCharArray());
    // this.keyStore = new SerializableKeyStore(keyStore);
    // } catch (KeyStoreException e) {
    // e.printStackTrace();
    // } catch (NoSuchProviderException e) {
    // e.printStackTrace();
    // } catch (NoSuchAlgorithmException e) {
    // e.printStackTrace();
    // } catch (CertificateException e) {
    // e.printStackTrace();
    // } catch (FileNotFoundException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    public TypedCertificate getCertificate(EntityType type) {
        try {
            return KeyStoreTools.getSelfCertificate(getKeyStore(), type);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public TypedCertificateList getMyCertificateChain(EntityType type) {
        try {
            return KeyStoreTools.getSelfCertificateChain(getKeyStore(), type);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
