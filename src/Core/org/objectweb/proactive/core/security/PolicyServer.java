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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.security.exceptions.ComputePolicyException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.DefaultEntity;
import org.objectweb.proactive.core.security.securityentity.Entity;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The PolicyServer class contains entity's policy rules and application's
 * certificate and private key
 *
 */
public class PolicyServer implements Serializable, Cloneable {
    static Logger log = ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER);
    private static int REQUIRED = 1;
    private static int DENIED = -1;
    private static int OPTIONAL = 0;
    protected ArrayList<PolicyRule> policyRules;
    protected String policyRulesFileLocation;
    protected String applicationName;
    protected transient KeyStore keyStore;
    protected byte[] encodedKeyStore;

    public PolicyServer() {
        ProActiveSecurity.loadProvider();
    }

    public PolicyServer(PolicyRule[] policyRules) {
        this.policyRules = new ArrayList<PolicyRule>();
        for (int i = 0; i < policyRules.length; i++) {
            this.policyRules.add(policyRules[i]);
        }
    }

    public PolicyServer(ArrayList<PolicyRule> policyRules) {
        this.policyRules = policyRules;
    }

    public PolicyServer(KeyStore keyStore, ArrayList<PolicyRule> policyRules) {
        this.policyRules = policyRules;
        this.keyStore = keyStore;
    }

    private int convert(String name) {
        if (name.equals("required") || name.equals("allowed") ||
                name.equals("authorized")) {
            return REQUIRED;
        } else if (name.equals("denied")) {
            return DENIED;
        } else {
            return OPTIONAL;
        }
    }

    public SecurityContext getPolicy(SecurityContext securityContext)
        throws SecurityNotAvailableException {
        ArrayList<Entity> entitiesFrom = securityContext.getEntitiesFrom();
        ArrayList<Entity> entitiesTo = securityContext.getEntitiesTo();

        if (this.policyRules == null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                           .debug("trying to find a policy whereas none has been set" +
                this + "    " + this.policyRules);
            throw new SecurityNotAvailableException();
        }

        PolicyRule policy = null;
        PolicyRule matchingPolicy = null;
        PolicyRule defaultPolicy = new PolicyRule();
        Communication communication;

        if ((entitiesFrom == null) || (entitiesFrom.size() == 0)) {
            entitiesFrom = new ArrayList<Entity>();
            entitiesFrom.add(new DefaultEntity());
        }
        if ((entitiesTo == null) || (entitiesTo.size() == 0)) {
            entitiesTo = new ArrayList<Entity>();
            entitiesTo.add(new DefaultEntity());
        }

        boolean matchingFrom;
        boolean matchingTo;
        boolean matchingFromDefault;
        boolean matchingToDefault;
        matchingFrom = matchingTo = matchingFromDefault = matchingToDefault = false;
        int length = this.policyRules.size();

        if (ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                               .isDebugEnabled()) {
            String s = "================================\nFrom :";
            for (int i = 0; i < entitiesFrom.size(); i++)
                s += ((entitiesFrom.get(i)) + " ");
            s += "\nTo :";
            for (int i = 0; i < entitiesTo.size(); i++)
                s += ((entitiesTo.get(i)) + " ");
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICYSERVER)
                           .debug(s + "\n=================================\n");
        }

        // iterate on all rules
        for (int i = 0; i < length; i++) {
            // retreiving a rule
            policy = this.policyRules.get(i);

            ArrayList policyEntitiesFrom = policy.getEntitiesFrom();

            // testing if From tag matches From entities
            for (int j = 0; !matchingFrom && (j < policyEntitiesFrom.size());
                    j++) {
                // from rules entities
                Entity policyEntityFrom = (Entity) policyEntitiesFrom.get(j);

                // System.out.println("testing from" + policyEntityFrom);
                for (int z = 0; !matchingFrom && (z < entitiesFrom.size());
                        z++) {
                    Entity entity = entitiesFrom.get(z);

                    // System.out.println("testing from -------------" +
                    // entity);
                    if (policyEntityFrom instanceof DefaultEntity) {
                        matchingFromDefault = true;
                    } else if (policyEntityFrom.equals(entity)) {
                        // System.out.println("Matching From " +
                        // policyEntityFrom);
                        matchingFrom = true;
                    }
                }
            }

            // testing To rules
            ArrayList policyEntitiesTo = policy.getEntitiesTo();

            for (int j = 0; !matchingTo && (j < policyEntitiesTo.size());
                    j++) {
                // retrieves To rule entities
                Entity policyEntityTo = (Entity) policyEntitiesTo.get(j);

                // System.out.println("testing to" + policyEntityTo );
                for (int z = 0; !matchingTo && (z < entitiesTo.size()); z++) {
                    Entity entity = entitiesTo.get(z);

                    // System.out.println("testing to -------------" + entity);
                    if (policyEntityTo instanceof DefaultEntity) {
                        matchingToDefault = true;
                    } else if (policyEntityTo.equals(entity)) {
                        // System.out.println("Matching To " + policyEntityTo);
                        matchingTo = true;
                    }
                }
            }

            // System.out.println("testing to matching :" + matchingTo +
            // " -- matchingToDefault " + matchingToDefault);
            if (matchingFrom && matchingTo) {
                matchingPolicy = policy;
                ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                               .debug("matching policy is " + policy);
                break;
            }
            if (matchingToDefault && matchingFromDefault) {
                defaultPolicy = policy;
            }

            matchingToDefault = matchingFromDefault = false;
            matchingTo = matchingFrom = false;
            // System.out.println("----------- reset matching, next rule
            // ---------");
        }

        if (matchingPolicy == null) {
            matchingPolicy = defaultPolicy;
        }

        if (matchingPolicy == null) {
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                           .warn("default Policy is null !!!!!!!!!!!!!!");
        }

        ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                       .debug("Found Policy : " + matchingPolicy);

        // TODOSECURITY split receive of a request or a reply
        if ((securityContext.getType() == SecurityContext.COMMUNICATION_RECEIVE_REQUEST_FROM) ||
                (securityContext.getType() == SecurityContext.COMMUNICATION_RECEIVE_REPLY_FROM)) {
            communication = matchingPolicy.getCommunicationReply();
            communication.setCommunication(1);
            securityContext.setReceiveReply(communication);
            securityContext.setReceiveRequest(communication);
        } else {
            communication = matchingPolicy.getCommunicationRequest();
            ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                           .debug("communication is " + communication);
            communication.setCommunication(1);
            securityContext.setSendReply(communication);
            securityContext.setSendRequest(communication);
        }

        if (securityContext.getType() == SecurityContext.MIGRATION_TO) {
            securityContext.setMigration(matchingPolicy.isMigration());
        }

        return securityContext;
    }

    public Communication getPolicyTo(String type, String virtualNodeFrom,
        String virtualNodeTo) throws SecurityNotAvailableException {
        // if (p == null) {
        // logger.debug("SEcurityNamfndjdhuidss crac r cd boium");
        // throw new SecurityNotAvailableException();
        // }
        if (true) {
            throw new RuntimeException("DEPRECATED METHOD : UPDATE !!!");
        }
        return null;
    }

    public int[] computePolicy(int[] from, int[] to)
        throws ComputePolicyException {
        // logger.info("calculating composed policy");
        if (((from[0] == REQUIRED) && (to[0] == DENIED)) ||
                ((from[1] == REQUIRED) && (to[1] == DENIED)) ||
                ((from[2] == REQUIRED) && (to[2] == DENIED)) ||
                ((from[0] == DENIED) && (to[0] == REQUIRED)) ||
                ((from[1] == DENIED) && (to[1] == REQUIRED)) ||
                ((from[2] == DENIED) && (to[2] == REQUIRED))) {
            throw new ComputePolicyException("incompatible policies");
        }

        return new int[] { from[0] + to[0], from[1] + to[1], from[2] + to[2] };
    }

    public boolean CanSendRequestTo(X509Certificate distantOA) {
        return false;
    }

    public boolean CanReceiveRequestFrom(X509Certificate distantOA) {
        return false;
    }

    public boolean CanSendReplyTo(X509Certificate distantOA) {
        return false;
    }

    public boolean CanReceiveReplyFrom(X509Certificate distantOA) {
        return false;
    }

    public boolean CanMigrateTo(X509Certificate distantOA) {
        return false;
    }

    public boolean canMigrateTo(String type, String from, String to) {
        try {
            System.out.println("Migration from " + from + "to" + to);
            ArrayList<Entity> arrayFrom = new ArrayList<Entity>();
            ArrayList<Entity> arrayTo = new ArrayList<Entity>();

            SecurityContext sc = new SecurityContext(SecurityContext.MIGRATION_TO,
                    arrayFrom, arrayTo);
            return getPolicy(sc).isMigration();
        } catch (SecurityNotAvailableException e) {
            // no security all is permitted
            return true;
        }
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

    // implements Serializable
    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        if (this.keyStore != null) {
            ByteArrayOutputStream bout = null;
            try {
                // keyStore = KeyStore.getInstance("PKCS12", "BC");
                // keyStore.load(null, null);
                //
                // if you haven't set the friendly name and local key id above
                // the name below will be the name of the key
                //

                /*
                 * if (certificate != null) {
                 * keyStore.setCertificateEntry("entityCertificate",
                 * certificate); }
                 * keyStore.setCertificateEntry("applicationCertificate",
                 * applicationCertificate);
                 */
                bout = new ByteArrayOutputStream();

                this.keyStore.store(bout, "ha".toCharArray());

                this.encodedKeyStore = bout.toByteArray();
                this.keyStore = null;
                bout.close();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                // TODOSECURITYSECURITY Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                // TODOSECURITYSECURITY Auto-generated catch block
                e.printStackTrace();
            } catch (CertificateException e) {
                // TODOSECURITYSECURITY Auto-generated catch block
                e.printStackTrace();
            }
        }
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.encodedKeyStore != null) {
            try {
                this.keyStore = KeyStore.getInstance("PKCS12", "BC");
                this.keyStore.load(new ByteArrayInputStream(
                        this.encodedKeyStore), "ha".toCharArray());
                this.encodedKeyStore = null;
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param policies
     */
    public void setPolicies(ArrayList<PolicyRule> policies) {
        ProActiveLogger.getLogger(Loggers.SECURITY_POLICY)
                       .info("storing policies");
        this.policyRules = policies;
    }

    /**
     * @param uri
     */
    public void setPolicyRulesFileLocation(String uri) {
        // for debug only
        // set security file path
        this.policyRulesFileLocation = uri;
    }

    /**
     * @return application certificate
     */
    public X509Certificate getApplicationCertificate() {
        if (this.keyStore != null) {
            try {
                return (X509Certificate) this.keyStore.getCertificate(SecurityConstants.KEYSTORE_APPLICATION_PATH);
            } catch (KeyStoreException e) {
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
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        PolicyServer clone = null;

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);

            out.writeObject(this);
            out.flush();
            bout.close();

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
        return this.keyStore;
    }

    public void setKeyStore(KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public void setPKCS12Keystore(String pkcs12Keystore) {
        try {
            this.keyStore = KeyStore.getInstance("PKCS12", "BC");
            this.keyStore.load(new FileInputStream(pkcs12Keystore),
                "ha".toCharArray());
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
