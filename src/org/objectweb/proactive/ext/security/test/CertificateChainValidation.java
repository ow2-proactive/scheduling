/*
 * Created on 6 avr. 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.objectweb.proactive.ext.security.test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PolicyNode;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.ext.security.CertTools;
import org.objectweb.proactive.ext.security.KeyTools;
/**
 * @author acontes
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class CertificateChainValidation {
	
	public static KeyPair keyPair() {
		KeyPair kp = null;
		// o = ProActiveSecurity.generateGenericCertificate();
		try {
			//acCert = (X509Certificate) o[0];
			//acPrivateKey = (PrivateKey) o[1];
			kp = KeyTools.genKeys(2048);
		} catch (NoSuchAlgorithmException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		} catch (NoSuchProviderException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		}
		return kp;
	}
	
	
	public static void main(String[] args) {
		//Provider myProvider = new
		// org.bouncycastle.jce.provider.BouncyCastleProvider();
		//Security.addProvider(myProvider);
		CertTools.installBCProvider();
		CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X.509", "BC");
		} catch (CertificateException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchProviderException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		X509Certificate acCert = null;
		PrivateKey acPrivateKey = null;
		// Object[] o = ProActiveSecurity.generateGenericCertificate();
		Object[] o = new Object[2];
		/*
		 * try { acCert = (X509Certificate)cf.generateCertificate(new
		 * FileInputStream("/net/home/acontes/certif/appli.cert")); } catch
		 * (CertificateException e2) { // TODO Auto-generated catch block
		 * e2.printStackTrace(); } catch (FileNotFoundException e2) { // TODO
		 * Auto-generated catch block e2.printStackTrace(); }
		 * 
		 * 
		 * 
		 * RSAPrivateKey privateKey = null; PKCS8EncodedKeySpec key_spec = null;
		 * String privateKeyFile = "/net/home/acontes/certif/appli.key"; byte[]
		 * key_bytes = null; try { FileInputStream fis = new
		 * FileInputStream(privateKeyFile); ByteArrayOutputStream key_baos = new
		 * ByteArrayOutputStream(); int aByte = 0; while ((aByte = fis.read()) !=
		 * -1) { key_baos.write(aByte); } fis.close(); key_bytes =
		 * key_baos.toByteArray(); key_baos.close(); KeyFactory key_factory =
		 * KeyFactory.getInstance("RSA", "BC"); key_spec = new
		 * PKCS8EncodedKeySpec(key_bytes); privateKey = (RSAPrivateKey)
		 * key_factory.generatePrivate(key_spec); } catch (IOException e) {
		 * System.out.println("Private Key not found : file " + privateKeyFile + "
		 * not found"); e.printStackTrace(); } catch
		 * (java.security.spec.InvalidKeySpecException e) {
		 * System.out.println("private key invalide :" + privateKeyFile);
		 * e.printStackTrace(); } catch (java.security.NoSuchAlgorithmException
		 * e) { e.printStackTrace(); } catch
		 * (java.security.NoSuchProviderException e) { e.printStackTrace(); }
		 * 
		 * acPrivateKey = (PrivateKey) privateKey;
		 */
		
		KeyPair kp = keyPair();
		
		
		try {
			acCert = CertTools.genSelfCert("CN=autorithy", 50, null, kp
					.getPrivate(), kp.getPublic(), true);
		} catch (InvalidKeyException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SignatureException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//acCert = (X509Certificate ) o[0];
		acPrivateKey = (PrivateKey) kp.getPrivate();
		//	acCert = (X509Certificate ) o[0];
		//	acPrivateKey = (PrivateKey) o[1];
		System.out.println("Generate Ca cert " + acCert.getSubjectDN()
				+ " ||  " + acCert.getIssuerDN());
		try {
			acCert.verify(acCert.getPublicKey());
			CertTools.isSelfSigned(acCert);
		} catch (InvalidKeyException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (CertificateException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchAlgorithmException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchProviderException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (SignatureException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
		/*
		o = ProActiveSecurity
				.generateCertificate("OU=nono", acCert.getSubjectDN()
						.toString(), acPrivateKey, acCert.getPublicKey());
	
		certLevel1 = (X509Certificate) o[0];
		privateKeyLevel1 = (PrivateKey) o[1];
		
		*/
		
		X509Certificate certLevel1 = null;
		PrivateKey privateKeyLevel1 = null;
		
		KeyPair kp1 = keyPair();
		
		
		try {
			certLevel1 = CertTools.genCert("CN=level1", 50, null, kp1
					.getPrivate(), kp1.getPublic(), true, acCert.getSubjectDN().toString(), acPrivateKey, acCert.getPublicKey());
		} catch (InvalidKeyException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SignatureException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		privateKeyLevel1 = kp1.getPrivate();
		
		
		System.out.println("Generate level1  cert " + certLevel1.getSubjectDN()
				+ " ||  " + certLevel1.getIssuerDN());
		try {
			certLevel1.verify(acCert.getPublicKey());
		} catch (InvalidKeyException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (CertificateException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchAlgorithmException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchProviderException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (SignatureException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
		
		
		X509Certificate certLevel2 = null;
		PrivateKey privateKeyLevel2 = null;
		
		KeyPair kp2 = keyPair();
		
		
		try {
			certLevel2 = CertTools.genCert("CN=level2", 50, null, kp2
					.getPrivate(), kp2.getPublic(), true, certLevel1.getSubjectDN().toString(), privateKeyLevel1,certLevel1.getPublicKey());
		} catch (InvalidKeyException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (SignatureException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		privateKeyLevel2 = kp2.getPrivate();
		
		
		System.out.println("Generate level2  cert " + certLevel2.getSubjectDN()
				+ " ||  " + certLevel2.getIssuerDN());
		try {
			certLevel2.verify(certLevel1.getPublicKey());
		} catch (InvalidKeyException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (CertificateException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchAlgorithmException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (NoSuchProviderException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		} catch (SignatureException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		
		/*
		 * o =
		 * ProActiveSecurity.generateCertificate("OU=nunu",certLevel2.getSubjectDN().toString(),
		 * privateKeyLevel2, certLevel2.getPublicKey());
		 * 
		 * X509Certificate certLevel3 = null; PrivateKey privateKeyLevel3 =
		 * null;
		 * 
		 * certLevel3 = (X509Certificate) o[0]; privateKeyLevel3 = (PrivateKey)
		 * o[1];
		 * 
		 * System.out.println("Generate level3 cert" + certLevel3.getSubjectDN() + " || " +
		 * certLevel3.getIssuerDN() );
		 * 
		 * try { certLevel3.verify(certLevel3.getPublicKey()); } catch
		 * (InvalidKeyException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); } catch (CertificateException e3) { // TODO
		 * Auto-generated catch block e3.printStackTrace(); } catch
		 * (NoSuchAlgorithmException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); } catch (NoSuchProviderException e3) { // TODO
		 * Auto-generated catch block e3.printStackTrace(); } catch
		 * (SignatureException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); }
		 * 
		 * 
		 * 
		 * o =
		 * ProActiveSecurity.generateCertificate("OU=nvnv",certLevel3.getSubjectDN().toString(),
		 * privateKeyLevel3, certLevel3.getPublicKey()); X509Certificate
		 * certLevel4 = null; PrivateKey privateKeyLevel4 = null;
		 * 
		 * certLevel4 = (X509Certificate) o[0]; privateKeyLevel4 = (PrivateKey)
		 * o[1];
		 * 
		 * System.out.println("Generate level4 cert" + certLevel4.getSubjectDN() + " || " +
		 * certLevel4.getIssuerDN() ); CertTools.isSelfSigned(certLevel4);
		 * 
		 * try { certLevel4.verify(certLevel4.getPublicKey()); } catch
		 * (InvalidKeyException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); } catch (CertificateException e3) { // TODO
		 * Auto-generated catch block e3.printStackTrace(); } catch
		 * (NoSuchAlgorithmException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); } catch (NoSuchProviderException e3) { // TODO
		 * Auto-generated catch block e3.printStackTrace(); } catch
		 * (SignatureException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); }
		 * 
		 * 
		 *  
		 */
		try {
			cf = CertificateFactory.getInstance("X.509");
			X509Certificate[] serverCerts = {certLevel2, certLevel1};
			// X509Certificate[] serverCerts = { acCert };
			List mylist = new ArrayList();
			for (int i = 0; i < serverCerts.length; i++) {
				mylist.add(serverCerts[i]);
			}
			CertPath cp = cf.generateCertPath(mylist);
			TrustAnchor anchor = new TrustAnchor(acCert, null);
			PKIXParameters params = new PKIXParameters(Collections
					.singleton(anchor));
			params.setRevocationEnabled(false);
			params.setSigProvider("BC");
			System.out.println("ddddddddddddd" + params.getSigProvider());
			CertPathValidator cpv = null;
			try {
				cpv = CertPathValidator.getInstance("PKIX", "BC");
			} catch (NoSuchProviderException e5) {
				// TODO Auto-generated catch block
				e5.printStackTrace();
			}
			PKIXCertPathValidatorResult result = (PKIXCertPathValidatorResult) cpv
					.validate(cp, params);
			PolicyNode policyTree = result.getPolicyTree();
			PublicKey subjectPublicKey = result.getPublicKey();
			System.out.println("Certificate validated");
			System.out.println("Policy Tree:\n" + policyTree);
			System.out.println("Subject Public key:\n" + subjectPublicKey);
		} catch (CertPathValidatorException cpve) {
			System.out.println("Validation failure, cert[" + cpve.getIndex()
					+ "] :" + cpve.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	
	
	System.out.println("storing certificate");
	
	

    //
    // store the key and the certificate chain
    //
	
    KeyStore store;
	try {
		store = KeyStore.getInstance("PKCS12", "BC");

	store.load(null, null);

    //
    // if you haven't set the friendly name and local key id above
    // the name below will be the name of the key
    //
    store.setCertificateEntry("c1", certLevel1);

    store.setCertificateEntry("c2", certLevel2);
    
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    
    store.store(bout,"ha".toCharArray());
    
    bout.close();
    
    KeyStore storin;
	
    
	storin = KeyStore.getInstance("PKCS12", "BC");
	
    storin.load(new ByteArrayInputStream(bout.toByteArray()),"ha".toCharArray());
	
	X509Certificate cc,ccc;
    
    cc = (X509Certificate) storin.getCertificate("c1");
    
    System.out.println("certificate equal ? " + certLevel1.equals(cc));
	
	} catch (KeyStoreException e5) {
		// TODO Auto-generated catch block
		e5.printStackTrace();
	} catch (NoSuchProviderException e5) {
		// TODO Auto-generated catch block
		e5.printStackTrace();
	} catch (NoSuchAlgorithmException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (CertificateException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	/*
	
	try {
		
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	ObjectOutputStream out = new ObjectOutputStream(bout);
	

	 	
        byte[] certE = null;
        
        
        
        certE =certLevel1.getEncoded();
        out.writeInt(certE.length);
        out.write(certE);
        
        certE = certLevel2.getEncoded();
        out.writeInt(certE.length);
        out.write(certE);
  
        
    out.close();
    bout.close();
	X509Certificate c1,c2;
    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
    
    
    int i = in.readInt();
    byte[] certEncoded = new byte[i];
    in.read(certEncoded,0,i);

    c1 = ProActiveSecurity.decodeCertificate(certEncoded);
    
    i = in.readInt();
    certEncoded = new byte[i];
    in.read(certEncoded,0,i);

    c2 = ProActiveSecurity.decodeCertificate(certEncoded);
    
	  } catch (CertificateEncodingException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }  
    */
    System.out.println("testing direct storing");
    
    try {
		certLevel1.verify(acCert.getPublicKey());
	} catch (InvalidKeyException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	} catch (CertificateException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	} catch (NoSuchAlgorithmException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	} catch (NoSuchProviderException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	} catch (SignatureException e4) {
		// TODO Auto-generated catch block
		e4.printStackTrace();
	}
    
    try {
        CertificateFactory cfd = CertificateFactory.getInstance("X.509");
        X509Certificate c11 = (X509Certificate) cfd.generateCertificate(new ByteArrayInputStream(
                    certLevel1.getEncoded()));
    } catch (CertificateException e) {
        e.printStackTrace();
    }
    
    
	}
	
}