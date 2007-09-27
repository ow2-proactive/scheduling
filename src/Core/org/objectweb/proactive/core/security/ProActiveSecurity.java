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
package org.objectweb.proactive.core.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.JDKKeyPairGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


public class ProActiveSecurity {
    public static Object[] generateGenericCertificateSelfSigned() {
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
        String dnName = "CN=Generic Certificate " + new Random().nextLong() +
            ", OU=Generic Certificate, EmailAddress=none";

        return generateCertificate(dnName, dnName, privateKey, publicKey);
    }

    public static Object[] genCert(String dn, long validity, String policyId,
        PrivateKey privKey, PublicKey pubKey, boolean isCA, String caDn,
        PrivateKey caPrivateKey, PublicKey acPubKey)
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            CertificateEncodingException, IllegalStateException {
        // Create self signed certificate
        String sigAlg = "SHA1WithRSA";
        Date firstDate = new Date();

        // Set back startdate ten minutes to avoid some problems with wrongly set clocks.
        firstDate.setTime(firstDate.getTime() - (10 * 60 * 1000));

        Date lastDate = new Date();

        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() +
            (validity * (24 * 60 * 60 * 1000)));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        // Serialnumber is random bits, where random generator is initialized with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed((long) (new Date().getTime()));
        random.nextBytes(serno);
        certgen.setSerialNumber((new java.math.BigInteger(serno)).abs());
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);
        certgen.setSubjectDN(CertTools.stringToBcX509Name(dn));
        certgen.setIssuerDN(CertTools.stringToBcX509Name(caDn));
        certgen.setPublicKey(pubKey);

        // Basic constranits is always critical and MUST be present at-least in CA-certificates.
        BasicConstraints bc = new BasicConstraints(isCA);
        certgen.addExtension(X509Extensions.BasicConstraints.getId(), true, bc);

        // Put critical KeyUsage in CA-certificates
        if (false) {
            //if (isCA == true) {
            int keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
            X509KeyUsage ku = new X509KeyUsage(keyusage);
            certgen.addExtension(X509Extensions.KeyUsage.getId(), true, ku);
        }

        // Subject and Authority key identifier is always non-critical and MUST be present for certificates to verify in Mozilla.
        try {
            if (false) {
                //if (isCA == true) {
                SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                            new ByteArrayInputStream(pubKey.getEncoded())).readObject());
                SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);

                SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                            new ByteArrayInputStream(acPubKey.getEncoded())).readObject());
                AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);

                certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(),
                    false, ski);
                certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(),
                    false, aki);
            }
        } catch (IOException e) { // do nothing
        }

        // CertificatePolicies extension if supplied policy ID, always non-critical
        if (policyId != null) {
            PolicyInformation pi = new PolicyInformation(new DERObjectIdentifier(
                        policyId));
            DERSequence seq = new DERSequence(pi);
            certgen.addExtension(X509Extensions.CertificatePolicies.getId(),
                false, seq);
        }

        X509Certificate selfcert = certgen.generate(caPrivateKey);

        return new Object[] { selfcert, privKey };
    } //genselfCert

    public static Object[] generateCertificate(String dnName,
        String issuerName, PrivateKey caPrivKey, PublicKey caPubKey) {
        KeyPair keyPair = null;

        SecureRandom rand = new SecureRandom();

        JDKKeyPairGenerator.RSA keyPairGen = new JDKKeyPairGenerator.RSA();

        keyPairGen.initialize(1024, rand);

        keyPair = keyPairGen.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        //        X509Certificate certif = null;
        Object[] o = null;
        try {
            o = genCert(dnName, 365, null, privateKey, publicKey, true,
                    issuerName, caPrivKey, caPubKey);
        } catch (InvalidKeyException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateEncodingException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        }

        /*
           X509V3CertificateGenerator certifGenerator = new X509V3CertificateGenerator();
           X509Certificate certif = null;
           DateFormat convert = DateFormat.getDateInstance();
           certifGenerator.setPublicKey(publicKey);
           String subjectCN = dnName;

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
               //         String name = s.substring(n + 1);
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
        return o;
    }

    //
    // create the subject key identifier.
    //
    public static SubjectKeyIdentifier createSubjectKeyId(PublicKey pubKey) {
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
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
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                        bIn).readObject());

            GeneralName genName = new GeneralName(name);
            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(genName);

            return new AuthorityKeyIdentifier(info,
                new GeneralNames(new DERSequence(v)),
                BigInteger.valueOf(sNumber));
        } catch (Exception e) {
            throw new RuntimeException("error creating AuthorityKeyId");
        }
    }

    public static AuthorityKeyIdentifier createAuthorityKeyId(PublicKey pubKey) {
        try {
            ByteArrayInputStream bIn = new ByteArrayInputStream(pubKey.getEncoded());
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
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

    public static X509Certificate decodeCertificate(byte[] encodedCert) {
        X509Certificate certificate = null;

        //
        // Recover Servers Certificate
        //
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                        encodedCert));
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return certificate;
    }

    /** Checks an array of X509 certicates for validity,
     * expiration, and other problems.
     *
     * @param certs  an array of X509 certificates
     *
     * @exception <code>CertificateException</code> if there is a problem with
     * any of the certificates - invalid, expired, etc.
     */

    /*
       public static void checkCertificates(java.security.cert.X509Certificate [] certs)
            throws CertificateException {
              for (int i = 0; i < certs.length; i++)
                checkCertificate( certs[i] );
       }
     */

    /**
     * Verifies a chain of X509 certificates (checks signatures)
     * Optionally, can also check for vadility, expiration of
     * individual certificates.
     *
     * @param certs          X509 certificate chain
     * @param checkVadility  if true, the vadility of each certificate
     *                       will be checked.
     *
     * @exception <code>CertificateException</code> if there is a problem with
     * any of the certificates - invalid, expired, or invalid signature etc.
     */
    public static void verifyCertificates(
        java.security.cert.X509Certificate[] certs, boolean checkVadility)
        throws GeneralSecurityException {
        java.security.cert.X509Certificate pCert = null;
        for (int i = 0; i < certs.length; i++) {
            if (checkVadility) {
                certs[i].checkValidity();
            }
            pCert = ((i + 1) >= certs.length) ? certs[i] : certs[i + 1];
            certs[i].verify(pCert.getPublicKey());
        }
    }

    /** Checks a X509 certificate for validity, expiration,
     * and other problems.
     *
     * @param acCert  certification authority X509 certificate
     * @param serverCerts chain to validate
     *
     * @exception <code>CertificateException</code> if there is a problem with
     * the certificate - invalid, expired, etc.
     */
    public static void checkCertificateChain(
        java.security.cert.X509Certificate acCert, X509Certificate[] serverCerts)
        throws CertificateException {
        try {
            CertificateFactory cf = null;
            cf = CertificateFactory.getInstance("X.509");

            // X509Certificate[] serverCerts = {certLevel2, certLevel1};
            // X509Certificate[] serverCerts = { acCert };
            List<X509Certificate> mylist = new ArrayList<X509Certificate>();
            for (int i = 0; i < serverCerts.length; i++) {
                mylist.add(serverCerts[i]);
            }
            CertPath cp = cf.generateCertPath(mylist);
            TrustAnchor anchor = new TrustAnchor(acCert, null);
            PKIXParameters params = new PKIXParameters(Collections.singleton(
                        anchor));
            params.setRevocationEnabled(false);
            params.setSigProvider("BC");
            //System.out.println("ddddddddddddd" + params.getSigProvider());
            CertPathValidator cpv = null;
            try {
                cpv = CertPathValidator.getInstance("PKIX", "BC");
            } catch (NoSuchProviderException e5) {
                e5.printStackTrace();
            }
            PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv.validate(cp,
                    params);
            PolicyNode policyTree = result.getPolicyTree();
            PublicKey subjectPublicKey = result.getPublicKey();
            System.out.println("Certificate validated");
            System.out.println("Policy Tree:\n" + policyTree);
            System.out.println("Subject Public key:\n" + subjectPublicKey);
        } catch (CertPathValidatorException cpve) {
            System.out.println("Validation failure, cert[" + cpve.getIndex() +
                "] :" + cpve.getMessage());
        } catch (InvalidAlgorithmParameterException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        } catch (CertificateException e) {
            // TODOSECURITY Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Creates a new certificate with given subject and issuer DN, public key,
     * for specified amount of time (from current time) and signs it with a given
     * private key.
     */

    /*
       public static X509Certificate createCertificate(java.security.cert.X509Certificate userCert,
                                                                 Name subject,
                                                                 PublicKey pk,
                                                                 Name issuer,
                                                                 PrivateKey sk,
                                                                 int hours)
            throws CertificateException {

              boolean extensions = false;
              X509Certificate cert = new X509Certificate();

              try {
                cert.setSerialNumber(new BigInteger(20, new Random()));
                cert.setSubjectDN(subject);
                cert.setPublicKey(pk);
                cert.setIssuerDN(issuer);

                GregorianCalendar date =
                  new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                date.add(Calendar.MINUTE, -5);
                cert.setValidNotBefore(date.getTime());
                date.add(Calendar.MINUTE, 5);
                date.add(Calendar.HOUR, hours);
                cert.setValidNotAfter(date.getTime());


                cert.sign(AlgorithmID.md5WithRSAEncryption, sk);
              } catch (Exception ex) {
                throw new GlobusProxyException("Create certificate failed.", ex);
              }

              return cert;
       }
     */
    public static void loadProvider() {
        CertTools.installBCProvider();
        //Provider myProvider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        //Security.addProvider(myProvider);
    }

    public static X509Certificate getMyCertificate() {
        ProActiveSecurityManager psm = ((AbstractBody) ProActiveObject.getBodyOnThis()).getProActiveSecurityManager();
        if (psm != null) {
            return psm.getCertificate();
        }

        return null;
    }

    public static X509Certificate[] getMyCertificateChain() {
        //TODO finish him
        //return ProActive.getBodyOnThis().getProActiveSecurityManager().getCertificate();
        return null;
    }
}
