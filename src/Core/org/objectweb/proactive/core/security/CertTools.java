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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.asn1.x509.X509NameTokenizer;
import org.bouncycastle.jce.X509KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Tools to handle common certificate operations.
 *
 */
public class CertTools {
    static Logger log = ProActiveLogger.getLogger(Loggers.SECURITY);
    public static final String EMAIL = "rfc822name";
    public static final String EMAIL1 = "email";
    public static final String EMAIL2 = "EmailAddress";
    public static final String EMAIL3 = "E";
    public static final String DNS = "dNSName";
    public static final String URI = "uniformResourceIdentifier";
    public static final String URI1 = "uri";

    /** Microsoft altName for windows smart card logon */
    public static final String UPN = "upn";

    /** ObjectID for upn altName for windows smart card logon */
    public static final String UPN_OBJECTID = "1.3.6.1.4.1.311.20.2.3";
    private static final String[] EMAILIDS = { EMAIL, EMAIL1, EMAIL2, EMAIL3 };

    /**
     * inhibits creation of new CertTools
     */
    private CertTools() {
    }

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

    private static final String[] dNObjectsForward = { "emailaddress", "e", "email", "uid", "cn", "sn",
            "serialnumber", "gn", "givenname", "initials", "surname", "t", "ou", "o", "l", "st", "dc", "c" };
    private static final String[] dNObjectsReverse = { "c", "dc", "st", "l", "o", "ou", "t", "surname",
            "initials", "givenname", "gn", "serialnumber", "sn", "cn", "uid", "email", "e", "emailaddress" };

    /** Change this if you want reverse order */
    private static final String[] dNObjects = dNObjectsForward;

    private static DERObjectIdentifier getOid(String o) {
        return oids.get(o.toLowerCase());
    } // getOid

    /**
     * Creates a (Bouncycastle) X509Name object from a string with a DN. Known OID (with order)
     * are: <code> EmailAddress, UID, CN, SN (SerialNumber), GivenName, Initials, SurName, T, OU,
     * O, L, ST, DC, C </code>
     * To change order edit 'dnObjects' in this source file.
     *
     * @param dn String containing DN that will be transformed into X509Name, The DN string has the
     *        format "CN=zz,OU=yy,O=foo,C=SE". Unknown OIDs in the string will be silently
     *        dropped.
     *
     * @return X509Name
     */
    public static X509Name stringToBcX509Name(String dn) {
        //log.debug(">stringToBcX509Name: " + dn);
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
                //log.debug("Found 1 "+object+" at index " + index);
                DERObjectIdentifier oid = getOid(object);

                if (oid != null) {
                    //log.debug("Added "+object+", "+oldvalues.elementAt(index));
                    ordering.add(oid);

                    // remove from the old vectors, so we start clean the next round
                    values.add(oldvalues.remove(index));
                    oldordering.remove(index);
                    index = -1;
                }
            }
        }

        /*
           if (log.isDebugEnabled()) {
               Iterator i1 = ordering.iterator();
               Iterator i2 = values.iterator();
               log.debug("Order: ");
               while (i1.hasNext()) {
                   log.debug(((DERObjectIdentifier)i1.next()).getId());
               }
               log.debug("Values: ");
               while (i2.hasNext()) {
                   log.debug((String)i2.next());
               }
           } */

        //log.debug("<stringToBcX509Name");
        return new X509Name(ordering, values);
    } // stringToBcX509Name

    /**
     * Every DN-string should look the same. Creates a name string ordered and looking like we want
     * it...
     *
     * @param dn String containing DN
     *
     * @return String containing DN
     */
    public static String stringToBCDNString(String dn) {
        //log.debug(">stringToBcDNString: "+dn);
        String ret = stringToBcX509Name(dn).toString();

        //log.debug("<stringToBcDNString: "+ret);
        return ret;
    }

    //    * Convenience method for getting an email address from a DN. Uses {@link
    //    * getPartFromDN(String,String)} internally, and searches for {@link EMAIL}, {@link EMAIL1},
    //    * {@link EMAIL2}, {@link EMAIL3} and returns the first one found.

    /**
     * Convenience method for getting an email address from a DN.
     * @param dn the DN
     * @return the found email address, or <code>null</code> if none is found
     */
    public static String getEmailFromDN(String dn) {
        log.debug(">getEmailFromDN(" + dn + ")");

        String email = null;

        for (int i = 0; (i < EMAILIDS.length) && (email == null); i++) {
            email = getPartFromDN(dn, EMAILIDS[i]);
        }

        log.debug("<getEmailFromDN(" + dn + "): " + email);

        return email;
    }

    /**
     * Gets a specified part of a DN. Specifically the first occurrence it the DN contains several
     * instances of a part (i.e. cn=x, cn=y returns x).
     *
     * @param dn String containing DN, The DN string has the format "C=SE, O=xx, OU=yy, CN=zz".
     * @param dnpart String specifying which part of the DN to get, should be "CN" or "OU" etc.
     *
     * @return String containing dnpart or null if dnpart is not present
     */
    public static String getPartFromDN(String dn, String dnpart) {
        log.debug(">getPartFromDN: dn:'" + dn + "', dnpart=" + dnpart);

        String part = null;

        if ((dn != null) && (dnpart != null)) {
            String o;
            dnpart += "="; // we search for 'CN=' etc.

            X509NameTokenizer xt = new X509NameTokenizer(dn);

            while (xt.hasMoreTokens()) {
                o = xt.nextToken();

                //log.debug("checking: "+o.substring(0,dnpart.length()));
                if ((o.length() > dnpart.length()) &&
                    o.substring(0, dnpart.length()).equalsIgnoreCase(dnpart)) {
                    part = o.substring(dnpart.length());

                    break;
                }
            }
        }

        log.debug("<getpartFromDN: resulting DN part=" + part);

        return part;
    } //getPartFromDN

    /**
     * Gets subject DN in the format we are sure about (BouncyCastle),supporting UTF8.
     *
     * @param cert X509Certificate
     *
     * @return String containing the subjects DN.
     */
    public static String getSubjectDN(X509Certificate cert) {
        return getDN(cert, 1);
    }

    /**
     * Gets issuer DN in the format we are sure about (BouncyCastle),supporting UTF8.
     *
     * @param cert X509Certificate
     *
     * @return String containing the issuers DN.
     */
    public static String getIssuerDN(X509Certificate cert) {
        return getDN(cert, 2);
    }

    /**
     * Gets subject or issuer DN in the format we are sure about (BouncyCastle),supporting UTF8.
     *
     * @param cert X509Certificate
     * @param which DOCUMENT ME!
     *
     * @return String containing the DN.
     */
    private static String getDN(X509Certificate cert, int which) {
        //log.debug(">getDN("+which+")");
        String dn = null;
        if (cert == null) {
            return dn;
        }
        try {
            CertificateFactory cf = CertTools.getCertificateFactory();
            X509Certificate x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert
                    .getEncoded()));

            //log.debug("Created certificate of class: " + x509cert.getClass().getName());
            if (which == 1) {
                dn = x509cert.getSubjectDN().toString();
            } else {
                dn = x509cert.getIssuerDN().toString();
            }
        } catch (CertificateException ce) {
            log.error("CertificateException: ", ce);
            return null;
        }

        //log.debug("<getDN("+which+"):"+dn);
        return stringToBCDNString(dn);
    } // getDN

    /**
     * Gets issuer DN for CRL in the format we are sure about (BouncyCastle),supporting UTF8.
     *
     * @param crl X509RL
     *
     * @return String containing the DN.
     */
    public static String getIssuerDN(X509CRL crl) {
        //log.debug(">getIssuerDN(crl)");
        String dn = null;
        try {
            CertificateFactory cf = CertTools.getCertificateFactory();
            X509CRL x509crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crl.getEncoded()));

            //log.debug("Created certificate of class: " + x509crl.getClass().getName());
            dn = x509crl.getIssuerDN().toString();
        } catch (CRLException ce) {
            log.error("CRLException: ", ce);

            return null;
        }

        //log.debug("<getIssuerDN(crl):"+dn);
        return stringToBCDNString(dn);
    } // getIssuerDN

    public static CertificateFactory getCertificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509", "BC");
        } catch (NoSuchProviderException nspe) {
            log.error("NoSuchProvider: ", nspe);
        } catch (CertificateException ce) {
            log.error("CertificateException: ", ce);
        }
        return null;
    }

    public static void installBCProvider() {
        // we need to check if the BouncyCastle provider is already installed
        // before installing it
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * Reads a certificate in PEM-format from a file. The file may contain other things,
     * the first certificate in the file is read.
     *
     * @param certFile the file containing the certificate in PEM-format
     * @return Ordered Collection of X509Certificate, first certificate first, or empty Collection
     * @exception IOException if the filen cannot be read.
     * @exception CertificateException if the filen does not contain a correct certificate.
     */
    public static Collection getCertsFromPEM(String certFile) throws IOException, CertificateException {
        log.debug(">getCertfromPEM: certFile=" + certFile);
        InputStream inStrm = new FileInputStream(certFile);
        Collection certs = getCertsFromPEM(inStrm);
        log.debug("<getCertfromPEM: certFile=" + certFile);
        return certs;
    }

    /**
     * Reads a certificate in PEM-format from an InputStream. The stream may contain other things,
     * the first certificate in the stream is read.
     *
     * @param certstream the input stream containing the certificate in PEM-format
     * @return Ordered Collection of X509Certificate, first certificate first, or empty Collection
     * @exception IOException if the stream cannot be read.
     * @exception CertificateException if the stream does not contain a correct certificate.
     */
    public static Collection getCertsFromPEM(InputStream certstream) throws IOException, CertificateException {
        log.debug(">getCertfromPEM:");
        ArrayList<X509Certificate> ret = new ArrayList<X509Certificate>();
        String beginKey = "-----BEGIN CERTIFICATE-----";
        String endKey = "-----END CERTIFICATE-----";
        BufferedReader bufRdr = new BufferedReader(new InputStreamReader(certstream));
        while (bufRdr.ready()) {
            ByteArrayOutputStream ostr = new ByteArrayOutputStream();
            PrintStream opstr = new PrintStream(ostr);
            String temp;
            while (((temp = bufRdr.readLine()) != null) && !temp.equals(beginKey)) {
                continue;
            }
            if (temp == null) {
                throw new IOException("Error in " + certstream.toString() + ", missing " + beginKey +
                    " boundary");
            }
            while (((temp = bufRdr.readLine()) != null) && !temp.equals(endKey)) {
                opstr.print(temp);
            }
            if (temp == null) {
                throw new IOException("Error in " + certstream.toString() + ", missing " + endKey +
                    " boundary");
            }
            opstr.close();

            byte[] certbuf = Base64.decode(ostr.toByteArray());
            ostr.close();
            // Phweeew, were done, now decode the cert from file back to X509Certificate object
            CertificateFactory cf = CertTools.getCertificateFactory();
            X509Certificate x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(
                certbuf));
            //            String dn = x509cert.getSubjectDN().toString();
            ret.add(x509cert);
        }

        log.debug("<getcertfromPEM:" + ret.size());
        return ret;
    } // getCertsFromPEM

    /**
     * Returns a certificate in PEM-format.
     *
     * @param certs the certificate to convert to PEM
     * @return byte array containing PEM certificate
     * @exception IOException if the stream cannot be read.
     * @exception CertificateException if the stream does not contain a correct certificate.
     */
    public static byte[] getPEMFromCerts(Collection certs) throws CertificateException {
        String beginKey = "-----BEGIN CERTIFICATE-----";
        String endKey = "-----END CERTIFICATE-----";
        ByteArrayOutputStream ostr = new ByteArrayOutputStream();
        PrintStream opstr = new PrintStream(ostr);
        Iterator iter = certs.iterator();
        while (iter.hasNext()) {
            X509Certificate cert = (X509Certificate) iter.next();
            byte[] certbuf = Base64.encode(cert.getEncoded());
            opstr.println("Subject: " + cert.getSubjectDN());
            opstr.println("Issuer: " + cert.getIssuerDN());
            opstr.println(beginKey);
            opstr.println(new String(certbuf));
            opstr.println(endKey);
        }
        opstr.close();
        byte[] ret = ostr.toByteArray();
        return ret;
    }

    /**
     * Creates X509Certificate from byte[].
     *
     * @param cert byte array containing certificate in DER-format
     *
     * @return X509Certificate
     *
     * @throws CertificateException if the byte array does not contain a proper certificate.
     * @throws IOException if the byte array cannot be read.
     */
    public static X509Certificate getCertfromByteArray(byte[] cert) throws IOException, CertificateException {
        log.debug(">getCertfromByteArray:");

        CertificateFactory cf = CertTools.getCertificateFactory();
        X509Certificate x509cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(cert));
        log.debug("<getCertfromByteArray:");

        return x509cert;
    } // getCertfromByteArray

    /**
     * Creates X509CRL from byte[].
     *
     * @param crl byte array containing CRL in DER-format
     *
     * @return X509CRL
     *
     * @throws IOException if the byte array can not be read.
     * @throws CertificateException if the byte arrayen does not contani a correct CRL.
     * @throws CRLException if the byte arrayen does not contani a correct CRL.
     */
    public static X509CRL getCRLfromByteArray(byte[] crl) throws IOException, CertificateException,
            CRLException {
        log.debug(">getCRLfromByteArray:");

        if (crl == null) {
            throw new IOException("Cannot read byte[] that is 'null'!");
        }

        CertificateFactory cf = CertTools.getCertificateFactory();
        X509CRL x509crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(crl));
        log.debug("<getCRLfromByteArray:");

        return x509crl;
    } // getCRLfromByteArray

    /**
     * Checks if a certificate is self signed by verifying if subject and issuer are the same.
     *
     * @param cert the certificate that skall be checked.
     *
     * @return boolean true if the certificate has the same issuer and subject, false otherwise.
     */
    public static boolean isSelfSigned(X509Certificate cert) {
        log
                .debug(">isSelfSigned: cert: " + CertTools.getIssuerDN(cert) + "\n" +
                    CertTools.getSubjectDN(cert));

        boolean ret = CertTools.getSubjectDN(cert).equals(CertTools.getIssuerDN(cert));
        log.debug("<isSelfSigned:" + ret);

        return ret;
    } // isSelfSigned

    /**
     * DOCUMENT ME!
     *
     * @param dn DOCUMENT ME!
     * @param validity DOCUMENT ME!
     * @param policyId DOCUMENT ME!
     * @param privKey DOCUMENT ME!
     * @param pubKey DOCUMENT ME!
     * @param isCA DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws NoSuchAlgorithmException DOCUMENT ME!
     * @throws SignatureException DOCUMENT ME!
     * @throws InvalidKeyException DOCUMENT ME!
     * @throws IllegalStateException
     * @throws CertificateEncodingException
     */
    public static X509Certificate genSelfCert(String dn, long validity, String policyId, PrivateKey privKey,
            PublicKey pubKey, boolean isCA) throws NoSuchAlgorithmException, SignatureException,
            InvalidKeyException, CertificateEncodingException, IllegalStateException {
        // Create self signed certificate
        String sigAlg = "SHA1WithRSA";
        Date firstDate = new Date();

        // Set back startdate ten minutes to avoid some problems with wrongly set clocks.
        firstDate.setTime(firstDate.getTime() - (10 * 60 * 1000));

        Date lastDate = new Date();

        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() + (validity * (24 * 60 * 60 * 1000)));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        // Serialnumber is random bits, where random generator is initialized with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed((new Date().getTime()));
        random.nextBytes(serno);
        certgen.setSerialNumber((new java.math.BigInteger(serno)).abs());
        certgen.setNotBefore(firstDate);
        certgen.setNotAfter(lastDate);
        certgen.setSignatureAlgorithm(sigAlg);
        certgen.setSubjectDN(CertTools.stringToBcX509Name(dn));
        certgen.setIssuerDN(CertTools.stringToBcX509Name(dn));
        certgen.setPublicKey(pubKey);

        // Basic constranits is always critical and MUST be present at-least in CA-certificates.
        BasicConstraints bc = new BasicConstraints(isCA);
        certgen.addExtension(X509Extensions.BasicConstraints.getId(), true, bc);

        // Put critical KeyUsage in CA-certificates
        if (isCA == true) {
            int keyusage = X509KeyUsage.keyCertSign + X509KeyUsage.cRLSign;
            X509KeyUsage ku = new X509KeyUsage(keyusage);
            certgen.addExtension(X509Extensions.KeyUsage.getId(), true, ku);
        }

        // Subject and Authority key identifier is always non-critical and MUST be present for certificates to verify in Mozilla.
        try {
            if (isCA == true) {
                SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                    new ByteArrayInputStream(pubKey.getEncoded())).readObject());
                SubjectKeyIdentifier ski = new SubjectKeyIdentifier(spki);

                SubjectPublicKeyInfo apki = new SubjectPublicKeyInfo((ASN1Sequence) new ASN1InputStream(
                    new ByteArrayInputStream(pubKey.getEncoded())).readObject());
                AuthorityKeyIdentifier aki = new AuthorityKeyIdentifier(apki);

                certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(), false, ski);
                certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), false, aki);
            }
        } catch (IOException e) { // do nothing
        }

        // CertificatePolicies extension if supplied policy ID, always non-critical
        if (policyId != null) {
            PolicyInformation pi = new PolicyInformation(new DERObjectIdentifier(policyId));
            DERSequence seq = new DERSequence(pi);
            certgen.addExtension(X509Extensions.CertificatePolicies.getId(), false, seq);
        }

        X509Certificate selfcert = certgen.generate(privKey);

        return selfcert;
    } //genselfCert

    public static X509Certificate genCert(String dn, long validity, String policyId, PrivateKey privKey,
            PublicKey pubKey, boolean isCA, String caDn, PrivateKey caPrivateKey, PublicKey acPubKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            CertificateEncodingException, IllegalStateException {
        // Create self signed certificate
        String sigAlg = "SHA1WithRSA";
        Date firstDate = new Date();

        // Set back startdate ten minutes to avoid some problems with wrongly set clocks.
        firstDate.setTime(firstDate.getTime() - (10 * 60 * 1000));

        Date lastDate = new Date();

        // validity in days = validity*24*60*60*1000 milliseconds
        lastDate.setTime(lastDate.getTime() + (validity * (24 * 60 * 60 * 1000)));

        X509V3CertificateGenerator certgen = new X509V3CertificateGenerator();

        // Serialnumber is random bits, where random generator is initialized with Date.getTime() when this
        // bean is created.
        byte[] serno = new byte[8];
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed((new Date().getTime()));
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

                certgen.addExtension(X509Extensions.SubjectKeyIdentifier.getId(), false, ski);
                certgen.addExtension(X509Extensions.AuthorityKeyIdentifier.getId(), false, aki);
            }
        } catch (IOException e) { // do nothing
        }

        // CertificatePolicies extension if supplied policy ID, always non-critical
        if (policyId != null) {
            PolicyInformation pi = new PolicyInformation(new DERObjectIdentifier(policyId));
            DERSequence seq = new DERSequence(pi);
            certgen.addExtension(X509Extensions.CertificatePolicies.getId(), false, seq);
        }

        X509Certificate cert = certgen.generate(caPrivateKey);

        return cert;
    } //genCert

    /**
     * Get the authority key identifier from a certificate extensions
     *
     * @param cert certificate containing the extension
     * @return byte[] containing the authority key identifier
     * @throws IOException if extension can not be parsed
     */
    public static byte[] getAuthorityKeyId(X509Certificate cert) throws IOException {
        byte[] extvalue = cert.getExtensionValue("2.5.29.35");
        if (extvalue == null) {
            return null;
        }
        DEROctetString oct = (DEROctetString) (new ASN1InputStream(new ByteArrayInputStream(extvalue))
                .readObject());
        AuthorityKeyIdentifier keyId = new AuthorityKeyIdentifier((ASN1Sequence) new ASN1InputStream(
            new ByteArrayInputStream(oct.getOctets())).readObject());
        return keyId.getKeyIdentifier();
    } // getAuthorityKeyId

    /**
     * Get the subject key identifier from a certificate extensions
     *
     * @param cert certificate containing the extension
     * @return byte[] containing the subject key identifier
     * @throws IOException if extension can not be parsed
     */
    public static byte[] getSubjectKeyId(X509Certificate cert) throws IOException {
        byte[] extvalue = cert.getExtensionValue("2.5.29.14");
        if (extvalue == null) {
            return null;
        }
        ASN1OctetString str = ASN1OctetString.getInstance(new ASN1InputStream(new ByteArrayInputStream(
            extvalue)).readObject());
        SubjectKeyIdentifier keyId = SubjectKeyIdentifier.getInstance(new ASN1InputStream(
            new ByteArrayInputStream(str.getOctets())).readObject());
        return keyId.getKeyIdentifier();
    } // getSubjectKeyId

    /**
     * Get a certificate policy ID from a certificate policies extension
     *
     * @param cert certificate containing the extension
     * @param pos position of the policy id, if several exist, the first is as pos 0
     * @return String with the certificate policy OID
     * @throws IOException if extension can not be parsed
     */
    public static String getCertificatePolicyId(X509Certificate cert, int pos) throws IOException {
        byte[] extvalue = cert.getExtensionValue(X509Extensions.CertificatePolicies.getId());
        if (extvalue == null) {
            return null;
        }
        DEROctetString oct = (DEROctetString) (new ASN1InputStream(new ByteArrayInputStream(extvalue))
                .readObject());
        ASN1Sequence seq = (ASN1Sequence) new ASN1InputStream(new ByteArrayInputStream(oct.getOctets()))
                .readObject();

        // Check the size so we don't ArrayIndexOutOfBounds
        if (seq.size() < (pos + 1)) {
            return null;
        }
        PolicyInformation pol = new PolicyInformation((ASN1Sequence) seq.getObjectAt(pos));
        String id = pol.getPolicyIdentifier().getId();
        return id;
    } // getCertificatePolicyId

    /**
     * Gets the Microsoft specific UPN altName.
     *
     * @param cert certificate containing the extension
     * @return String with the UPN name
     */
    public static String getUPNAltName(X509Certificate cert) throws IOException, CertificateParsingException {
        Collection altNames = cert.getSubjectAlternativeNames();
        if (altNames != null) {
            Iterator i = altNames.iterator();
            while (i.hasNext()) {
                List listitem = (List) i.next();
                Integer no = (Integer) listitem.get(0);
                if (no.intValue() == 0) {
                    byte[] altName = (byte[]) listitem.get(1);
                    DERObject oct = (new ASN1InputStream(new ByteArrayInputStream(altName)).readObject());
                    ASN1Sequence seq = ASN1Sequence.getInstance(oct);
                    ASN1TaggedObject obj = (ASN1TaggedObject) seq.getObjectAt(1);
                    DERUTF8String str = DERUTF8String.getInstance(obj.getObject());
                    return str.getString();
                }
            }
        }
        return null;
    } // getUPNAltName

    /**
     * Return the CRL distribution point URL form a certificate.
     */
    public static URL getCrlDistributionPoint(X509Certificate certificate) throws CertificateParsingException {
        try {
            DERObject obj = getExtensionValue(certificate, X509Extensions.CRLDistributionPoints.getId());
            if (obj == null) {
                return null;
            }
            ASN1Sequence distributionPoints = (ASN1Sequence) obj;
            for (int i = 0; i < distributionPoints.size(); i++) {
                ASN1Sequence distrPoint = (ASN1Sequence) distributionPoints.getObjectAt(i);
                for (int j = 0; j < distrPoint.size(); j++) {
                    ASN1TaggedObject tagged = (ASN1TaggedObject) distrPoint.getObjectAt(j);
                    if (tagged.getTagNo() == 0) {
                        String url = getStringFromGeneralNames(tagged.getObject());
                        if (url != null) {
                            return new URL(url);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CertificateParsingException(e.toString());
        }
        return null;
    }

    /**
     * Return an Extension DERObject from a certificate
     */
    private static DERObject getExtensionValue(X509Certificate cert, String oid) throws IOException {
        byte[] bytes = cert.getExtensionValue(oid);
        if (bytes == null) {
            return null;
        }
        ASN1InputStream aIn = new ASN1InputStream(new ByteArrayInputStream(bytes));
        ASN1OctetString octs = (ASN1OctetString) aIn.readObject();
        aIn = new ASN1InputStream(new ByteArrayInputStream(octs.getOctets()));
        return aIn.readObject();
    } //getExtensionValue

    private static String getStringFromGeneralNames(DERObject names) {
        ASN1Sequence namesSequence = ASN1Sequence.getInstance((ASN1TaggedObject) names, false);
        if (namesSequence.size() == 0) {
            return null;
        }
        DERTaggedObject taggedObject = (DERTaggedObject) namesSequence.getObjectAt(0);
        return new String(ASN1OctetString.getInstance(taggedObject, false).getOctets());
    } //getStringFromGeneralNames

    /**
     * Generate SHA1 fingerprint in string representation.
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     *
     * @return String containing hex format of SHA1 fingerprint.
     */
    public static String getCertFingerprintAsString(byte[] ba) {
        try {
            X509Certificate cert = getCertfromByteArray(ba);
            byte[] res = generateSHA1Fingerprint(cert.getEncoded());

            return Hex.encode(res).toString();
        } catch (CertificateEncodingException cee) {
            log.error("Error encoding X509 certificate.", cee);
        } catch (CertificateException cee) {
            log.error("Error decoding X509 certificate.", cee);
        } catch (IOException ioe) {
            log.error("Error reading byte array for X509 certificate.", ioe);
        }

        return null;
    }

    /**
     * Generate SHA1 fingerprint of certificate in string representation.
     *
     * @param cert X509Certificate.
     *
     * @return String containing hex format of SHA1 fingerprint.
     */
    public static String getFingerprintAsString(X509Certificate cert) {
        try {
            byte[] res = generateSHA1Fingerprint(cert.getEncoded());

            return Hex.encode(res).toString();
        } catch (CertificateEncodingException cee) {
            log.error("Error encoding X509 certificate.", cee);
        }

        return null;
    }

    /**
     * Generate SHA1 fingerprint of CRL in string representation.
     *
     * @param crl X509CRL.
     *
     * @return String containing hex format of SHA1 fingerprint.
     */
    public static String getFingerprintAsString(X509CRL crl) {
        try {
            byte[] res = generateSHA1Fingerprint(crl.getEncoded());

            return Hex.encode(res).toString();
        } catch (CRLException ce) {
            log.error("Error encoding X509 CRL.", ce);
        }

        return null;
    }

    /**
     * Generate a SHA1 fingerprint from a byte array containing a X.509 certificate
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     *
     * @return Byte array containing SHA1 hash of DER encoded certificate.
     */
    public static byte[] generateSHA1Fingerprint(byte[] ba) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");

            return md.digest(ba);
        } catch (NoSuchAlgorithmException nsae) {
            log.error("SHA1 algorithm not supported", nsae);
        }

        return null;
    } // generateSHA1Fingerprint

    /**
     * Generate a MD5 fingerprint from a byte array containing a X.509 certificate
     *
     * @param ba Byte array containing DER encoded X509Certificate.
     *
     * @return Byte array containing MD5 hash of DER encoded certificate.
     */
    public static byte[] generateMD5Fingerprint(byte[] ba) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            return md.digest(ba);
        } catch (NoSuchAlgorithmException nsae) {
            log.error("MD5 algorithm not supported", nsae);
        }

        return null;
    } // generateMD5Fingerprint

    public static KeyPair keyPair(int size) {
        KeyPair kp = null;

        // o = ProActiveSecurity.generateGenericCertificate();
        try {
            //acCert = (X509Certificate) o[0];
            //acPrivateKey = (PrivateKey) o[1];
            kp = KeyTools.genKeys(size);
        } catch (NoSuchAlgorithmException e4) {
            e4.printStackTrace();
        } catch (NoSuchProviderException e4) {
            e4.printStackTrace();
        }
        return kp;
    }
} // CertTools
