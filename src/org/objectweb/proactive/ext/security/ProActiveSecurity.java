/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
* USA
*
*  Initial developer(s):               The ProActive Team
*                        http://www.inria.fr/oasis/ProActive/contacts.html
*  Contributor(s):
*
* ################################################################
*/
package org.objectweb.proactive.ext.security;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Random;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.misc.MiscObjectIdentifiers;
import org.bouncycastle.asn1.misc.NetscapeCertType;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509V3CertificateGenerator;
import org.bouncycastle.jce.provider.JDKKeyPairGenerator;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


public class ProActiveSecurity {
    public static Object[] generateGenericCertificate() {
		Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
		  Security.addProvider(myProvider);
  

        /* generation of a default certificate */
        KeyPair keyPair = null;
        SecureRandom rand = new SecureRandom();

        JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

        keyPairGen.initialize(1024, rand);

        keyPair = keyPairGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        return generateCertificate("CN=Generic Certificate " +
            new Random().nextLong() +
            ", OU=Generic Certificate, EmailAddress=none", "CN=none", privateKey,
            publicKey);
    }

    public static Object[] generateCertificate(String dnName,
        String issuerName, PrivateKey caPrivKey, PublicKey caPubKey) {
        KeyPair keyPair = null;
        SecureRandom rand = new SecureRandom();

        JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

        keyPairGen.initialize(1024, rand);

        keyPair = keyPairGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        X509V3CertificateGenerator certifGenerator = new X509V3CertificateGenerator();

        X509Certificate certif = null;

        DateFormat convert = DateFormat.getDateInstance();

        certifGenerator.setPublicKey(publicKey);

        String subjectCN = dnName;

        //    System.out.println("DefaultCertificate subjectCN " + subjectCN);
        X509Name subject = new X509Name(subjectCN);
        X509Name issuer = new X509Name(issuerName);

        certifGenerator.setSubjectDN(subject);
        certifGenerator.setIssuerDN(issuer);
        certifGenerator.setSignatureAlgorithm("MD5withRSA");

        Date start = new Date(System.currentTimeMillis() -
                (1000L * 60 * 60 * 24 * 30));
        Date stop = new Date(System.currentTimeMillis() +
                (1000L * 60 * 60 * 24 * 30));

        certifGenerator.setNotAfter(stop);
        certifGenerator.setNotBefore(start);
        certifGenerator.setPublicKey(publicKey);
        certifGenerator.setSerialNumber(new BigInteger("1"));

        certifGenerator.addExtension(X509Extensions.SubjectKeyIdentifier,
            false, createSubjectKeyId(publicKey));

        certifGenerator.addExtension(X509Extensions.AuthorityKeyIdentifier,
            false, createAuthorityKeyId(caPubKey, new X509Name(issuerName), 1));

        certifGenerator.addExtension(X509Extensions.BasicConstraints, false,
            new BasicConstraints(true));

        certifGenerator.addExtension(MiscObjectIdentifiers.netscapeCertType,
            false,
            new NetscapeCertType(NetscapeCertType.smime |
                NetscapeCertType.sslServer));

        try {
            certif = certifGenerator.generateX509Certificate(privateKey, "BC");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return new Object[] { certif, privateKey };
    }

    public static String retrieveVNName(Node node) {
        if (NodeFactory.isNodeLocal(node)) {
            //   System.out.println("Launching OA nodeURL extracted locally: " + node.getVnName());
            return node.getVnName();
        }
        String s = node.getNodeInformation().getName();

        // int n = s.lastIndexOf("/");
        //	 String name = s.substring(n + 1);
        String vn = null;
        try {
            vn = RuntimeFactory.getDefaultRuntime().getVNName(node.getNodeInformation()
                                                                  .getName());
        } catch (ProActiveException e1) {
            e1.printStackTrace();
        }

        //  System.out.println("Launching OA nodeURL : " + s);
        // System.out.println("Node url " + node.getNodeInformation().getURL());
        // System.out.println("Launching OA nodeURL extracted: " + vn);
        return vn;

        /*int i = s.lastIndexOf("//");
        String s2 = s.substring(i, s.length());
        if (i  > 0) {
        System.out.println("coupeeeeeeee " + s2);
        */

        //vn =  RuntimeFactory.getDefaultRuntime().getVNName(name);
    }

    //
    // create the subject key identifier.
    //
    public static SubjectKeyIdentifier createSubjectKeyId(PublicKey pubKey) {
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new DERInputStream(
                        bIn).readObject());

            return new SubjectKeyIdentifier(info);
        } catch (Exception e) {
            throw new RuntimeException("error creating key");
        }
    }

    //
    // create the authority key identifier.
    //
    public static AuthorityKeyIdentifier createAuthorityKeyId(
        PublicKey pubKey, X509Name name, int sNumber) {
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new DERInputStream(
                        bIn).readObject());

            GeneralName genName = new GeneralName(name);
			ASN1EncodableVector     v = new ASN1EncodableVector();

				 v.add(genName);

    
			return new AuthorityKeyIdentifier(
				info, new GeneralNames(new DERSequence(v)), BigInteger.valueOf(sNumber));
        } catch (Exception e) {
            throw new RuntimeException("error creating AuthorityKeyId");
        }
    }

    public static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey pubKey) {
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new DERInputStream(
                        bIn).readObject());

            return new AuthorityKeyIdentifier(info);
        } catch (Exception e) {
            throw new RuntimeException("error creating AuthorityKeyId");
        }
    }

    public static String retrieveVNName(String nodeName) {
        String vn = null;
        try {
            vn = RuntimeFactory.getDefaultRuntime().getVNName(nodeName);
        } catch (ProActiveException e1) {
            e1.printStackTrace();
        }

        //  System.out.println("Launching OA nodeURL : " + s);
        // System.out.println("Node url " + node.getNodeInformation().getURL());
        // System.out.println("Launching OA nodeURL extracted: " + vn);
        return vn;

        /*int i = s.lastIndexOf("//");
        String s2 = s.substring(i, s.length());
        if (i  > 0) {
        System.out.println("coupeeeeeeee " + s2);
        */

        //vn =  RuntimeFactory.getDefaultRuntime().getVNName(name);
    }

    /**
     * @param string
     */
    public static void migrateTo(PolicyServer ps, String bodyURL, Node nodeTo)
        throws SecurityMigrationException {
        PolicyServer runtimePolicyServer = null;
        PolicyServer applicationPolicyServer = null;
        String vnFrom;
        String vn;
        String vnTo;
        vn = vnFrom = vnTo = null;
        vnFrom = bodyURL;

        ProActiveRuntime pr = null;

        try {
            runtimePolicyServer = RuntimeFactory.getDefaultRuntime()
                                                .getPolicyServer();

            //applicationPolicyServer = RuntimeFactory.getDefaultRuntime().getPolicyServerFor()
            //vnFrom = ProActive.getBodyOnThis().getNodeURL();
            int n = vnFrom.lastIndexOf("/");
            String name = vnFrom.substring(n + 1);

            //System.out.println("name:" + name + " -- vnFrom :" + vnFrom);
            vn = RuntimeFactory.getDefaultRuntime().getVNName(name);

            vnTo = nodeTo.getVnName();
            if (vnTo == null) {
                vnTo = nodeTo.getNodeInformation().getURL();
                n = vnTo.lastIndexOf("/");
                name = vnTo.substring(n + 1);
                //      System.out.println("name:" + name + " -- vnTo :" + vnTo);
                pr = nodeTo.getProActiveRuntime();
                vnTo = pr.getVNName(name);
            }

            //    System.out.println("JE SUIS SUR " + vn + "ET je vais sur " + vnTo);
        } catch (ProActiveException e1) {
            e1.printStackTrace();
        }

        if (runtimePolicyServer != null) {
            if (runtimePolicyServer.canMigrateTo("VN", vn, vnTo)) {
            } else {
                throw new SecurityMigrationException("matching rule : VN[" +
                    vn + "] --> VN[" + vnTo + "]");
            }
        }

        if (ps != null) {
            Communication runtimePolicy;
            Communication VNPolicy;
            Communication distantPolicy;

            ArrayList arrayFrom = new ArrayList();
            ArrayList arrayTo = new ArrayList();

            if (vnFrom == null) {
                arrayFrom.add(new DefaultEntity());
            } else {
            //    arrayFrom.add(new EntityVirtualNode(vnFrom));
            }
            if (vnTo == null) {
                arrayTo.add(new DefaultEntity());
            } else {
            //    arrayTo.add(new EntityVirtualNode(vnTo));
            }

            SecurityContext sc = new SecurityContext(SecurityContext.MIGRATION_TO,
                    arrayFrom, arrayTo);
            try {
                sc = ps.getPolicy(sc);
            } catch (SecurityNotAvailableException e) {
            	// do nothing
            }
            if (sc.isMigration()) {
            } else {
                throw new SecurityMigrationException("matching rule : VN[" +
                    vn + "] --> VN[" + vnTo + "]");
            }
        }
    }

    public static X509Certificate decodeCertificate(byte[] encodedCert) {
        X509Certificate certificate = null;

        //
        // Recover Servers Certificate
        //
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                        encodedCert));
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        return certificate;
    }
}
