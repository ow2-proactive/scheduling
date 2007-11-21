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
package org.objectweb.proactive.ic2d.security.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.X509NameTokenizer;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.x509.X509V3CertificateGenerator;


/**
 * Tools to handle common certificate operations.
 *
 */
public abstract class CertTools {

    /** BC X509Name contains some lookup tables that could maybe be used here. */
    private static final HashMap<String, DERObjectIdentifier> oids = new HashMap<String, DERObjectIdentifier>();

    static {
        oids.put("c", X509Name.C);
        oids.put("dc", X509Name.DC);
        oids.put("st", X509Name.ST);
        oids.put("l", X509Name.L);
        oids.put("o", X509Name.O);
        oids.put("ou", X509Name.OU);
        oids.put("t", X509Name.T);
        oids.put("surname", X509Name.SURNAME);
        oids.put("initials", X509Name.INITIALS);
        oids.put("givenname", X509Name.GIVENNAME);
        oids.put("gn", X509Name.GIVENNAME);
        oids.put("sn", X509Name.SN);
        oids.put("serialnumber", X509Name.SN);
        oids.put("cn", X509Name.CN);
        oids.put("uid", X509Name.UID);
        oids.put("emailaddress", X509Name.EmailAddress);
        oids.put("e", X509Name.EmailAddress);
        oids.put("email", X509Name.EmailAddress);
    }

    private static final String[] dNObjectsForward = {
            "emailaddress", "e", "email", "uid", "cn", "sn", "serialnumber",
            "gn", "givenname", "initials", "surname", "t", "ou", "o", "l", "st",
            "dc", "c"
        };

    /** Change this if you want reverse order */
    private static final String[] dNObjects = dNObjectsForward;

    private static DERObjectIdentifier getOid(String o) {
        return oids.get(o.toLowerCase());
    } // getOid

    /**
     * Creates a (Bouncycastle) X509Name object from a string with a DN. Known
     * OID (with order) are:
     * <code> EmailAddress, UID, CN, SN (SerialNumber), GivenName, Initials, SurName, T, OU,
     * O, L, ST, DC, C </code>
     * To change order edit 'dnObjects' in this source file.
     *
     * @param dn
     *            String containing DN that will be transformed into X509Name,
     *            The DN string has the format "CN=zz,OU=yy,O=foo,C=SE". Unknown
     *            OIDs in the string will be silently dropped.
     *
     * @return X509Name
     */
    public static X509Name stringToBcX509Name(String dn) {
        // log.debug(">stringToBcX509Name: " + dn);
        // first make two vectors, one with all the C, O, OU etc specifying
        // the order and one holding the actual values
        ArrayList<String> oldordering = new ArrayList<String>();
        ArrayList<String> oldvalues = new ArrayList<String>();
        X509NameTokenizer xt = new X509NameTokenizer(dn);

        while (xt.hasMoreTokens()) {
            // This is a pair (CN=xx)
            String pair = xt.nextToken();
            int ix = pair.indexOf("=");

            if (ix != -1) {
                // make lower case so we can easily compare later
                oldordering.add(pair.substring(0, ix).toLowerCase());
                oldvalues.add(pair.substring(ix + 1));
            } else {
                // Huh, what's this?
            }
        }

        // Now in the specified order, move from oldordering to newordering,
        // reshuffling as we go along
        Vector<DERObjectIdentifier> ordering = new Vector<DERObjectIdentifier>();
        Vector<String> values = new Vector<String>();
        int index = -1;

        for (String object : dNObjects) {
            while ((index = oldordering.indexOf(object)) != -1) {
                // log.debug("Found 1 "+object+" at index " + index);
                DERObjectIdentifier oid = getOid(object);

                if (oid != null) {
                    // log.debug("Added "+object+",
                    // "+oldvalues.elementAt(index));
                    ordering.add(oid);

                    // remove from the old vectors, so we start clean the next
                    // round
                    values.add(oldvalues.remove(index));
                    oldordering.remove(index);
                    index = -1;
                }
            }
        }

        /*
         * if (log.isDebugEnabled()) { Iterator i1 = ordering.iterator();
         * Iterator i2 = values.iterator(); log.debug("Order: "); while
         * (i1.hasNext()) { log.debug(((DERObjectIdentifier)i1.next()).getId()); }
         * log.debug("Values: "); while (i2.hasNext()) {
         * log.debug((String)i2.next()); } }
         */

        // log.debug("<stringToBcX509Name");
        return new X509Name(ordering, values);
    } // stringToBcX509Name

    public static X509Certificate genSelfCert(String dn, long validity,
        String policyId, PrivateKey privKey, PublicKey pubKey, boolean isCA)
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            CertificateEncodingException, IllegalStateException {
        // Create self signed certificate
        String sigAlg = "SHA1WithRSA";
        Date firstDate = new Date();

        // Set back startdate ten minutes to avoid some problems with wrongly
        // set clocks.
        firstDate.setTime(firstDate.getTime() - (10 * 60 * 1000));

        Date lastDate = new Date();

        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() + (validity * 24 * 60 * 60 * 1000));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        // Serialnumber is random bits, where random generator is initialized
        // with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(new Date().getTime());
        random.nextBytes(serno);
        certgen.setSerialNumber((new java.math.BigInteger(serno)).abs());
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);
        certgen.setSubjectDN(CertTools.stringToBcX509Name(dn));
        certgen.setIssuerDN(CertTools.stringToBcX509Name(dn));
        certgen.setPublicKey(pubKey);

        // Basic constranits is always critical and MUST be present at-least in
        // CA-certificates.
        BasicConstraints bc = new BasicConstraints(isCA);
        certgen.addExtension(X509Extensions.BasicConstraints.getId(), true, bc);

        // Put critical KeyUsage in CA-certificates
        if (isCA == true) {
            int keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
            X509KeyUsage ku = new X509KeyUsage(keyusage);
            certgen.addExtension(X509Extensions.KeyUsage.getId(), true, ku);
        }

        // Subject and Authority key identifier is always non-critical and MUST
        // be present for certificates to verify in Mozilla.
        try {
            if (isCA == true) {
                SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                            new ByteArrayInputStream(pubKey.getEncoded())).readObject());
                SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);

                SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                            new ByteArrayInputStream(pubKey.getEncoded())).readObject());
                AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);

                certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(),
                    false, ski);
                certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(),
                    false, aki);
            }
        } catch (IOException e) { // do nothing
        }

        // CertificatePolicies extension if supplied policy ID, always
        // non-critical
        if (policyId != null) {
            PolicyInformation pi = new PolicyInformation(new DERObjectIdentifier(
                        policyId));
            DERSequence seq = new DERSequence(pi);
            certgen.addExtension(X509Extensions.CertificatePolicies.getId(),
                false, seq);
        }

        X509Certificate selfcert = certgen.generate(privKey);

        return selfcert;
    } // genselfCert

    public static X509Certificate genCert(String dn, long validity,
        String policyId, PublicKey pubKey, boolean isCA, String caDn,
        PrivateKey caPrivateKey, PublicKey acPubKey)
        throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            CertificateEncodingException, IllegalStateException {
        // Create self signed certificate
        String sigAlg = "SHA1WithRSA";
        Date firstDate = new Date();

        // Set back startdate ten minutes to avoid some problems with wrongly
        // set clocks.
        firstDate.setTime(firstDate.getTime() - (10 * 60 * 1000));

        Date lastDate = new Date();

        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() + (validity * 24 * 60 * 60 * 1000));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        // Serialnumber is random bits, where random generator is initialized
        // with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(new Date().getTime());
        random.nextBytes(serno);
        certgen.setSerialNumber((new java.math.BigInteger(serno)).abs());
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);
        certgen.setSubjectDN(CertTools.stringToBcX509Name(dn));
        certgen.setIssuerDN(CertTools.stringToBcX509Name(caDn));
        certgen.setPublicKey(pubKey);

        // Basic constranits is always critical and MUST be present at-least in
        // CA-certificates.
        BasicConstraints bc = new BasicConstraints(isCA);
        certgen.addExtension(X509Extensions.BasicConstraints.getId(), true, bc);

        // Put critical KeyUsage in CA-certificates
        if (false) {
            // if (isCA == true) {
            int keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
            X509KeyUsage ku = new X509KeyUsage(keyusage);
            certgen.addExtension(X509Extensions.KeyUsage.getId(), true, ku);
        }

        // Subject and Authority key identifier is always non-critical and MUST
        // be present for certificates to verify in Mozilla.
        try {
            if (false) {
                // if (isCA == true) {
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

        // CertificatePolicies extension if supplied policy ID, always
        // non-critical
        if (policyId != null) {
            PolicyInformation pi = new PolicyInformation(new DERObjectIdentifier(
                        policyId));
            DERSequence seq = new DERSequence(pi);
            certgen.addExtension(X509Extensions.CertificatePolicies.getId(),
                false, seq);
        }

        X509Certificate cert = certgen.generate(caPrivateKey);

        return cert;
    } // genCert

    public static X509Certificate getCertfromByteArray(byte[] cert) {
        CertificateFactory cf = CertTools.getCertificateFactory();
        X509Certificate x509cert = null;
        try {
            x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                        cert));
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return x509cert;
    } // getCertfromByteArray

    public static CertificateFactory getCertificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509", "BC");
        } catch (NoSuchProviderException nspe) {
            nspe.printStackTrace();
        } catch (CertificateException ce) {
            ce.printStackTrace();
        }
        return null;
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
} // CertTools
