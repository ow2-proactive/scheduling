
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.objectweb.proactive.core.security.CertTools;
import org.objectweb.proactive.core.security.KeyStoreTools;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;

public class GenKeystore {
	
	public static void main(String[] args) {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		KeyPairGenerator keygen;
		try {
			keygen = KeyPairGenerator.getInstance("RSA", "BC");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return;
		} catch (NoSuchProviderException e1) {
			e1.printStackTrace();
			return;
		}
		keygen.initialize(512);

		List<X509Certificate> certs = new ArrayList<X509Certificate>();
		List<EntityType> types = new ArrayList<EntityType>();
		List<String> names = new ArrayList<String>();
		List<KeyPair> pairs = new ArrayList<KeyPair>();
		int i = 0;
		try {
			// domain fr
			names.add("CN=fr");
			types.add(EntityType.DOMAIN);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genSelfCert(names.get(i), 360, null, pairs.get(
					i).getPrivate(), pairs.get(i).getPublic(), true));
			i++;

			// domain inria
			names.add("CN=inria");
			types.add(EntityType.DOMAIN);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 1), pairs.get(i - 1).getPrivate(), pairs
					.get(i - 1).getPublic()));
			i++;

			// domain ProActive
			names.add("CN=proActive");
			types.add(EntityType.DOMAIN);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 1), pairs.get(i - 1).getPrivate(), pairs
					.get(i - 1).getPublic()));
			i++;

			// user nhouillo
			names.add("CN=nhouillo");
			types.add(EntityType.USER);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 1), pairs.get(i - 1).getPrivate(), pairs
					.get(i - 1).getPublic()));
			i++;

			// user acontes
			names.add("CN=acontes");
			types.add(EntityType.USER);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 2), pairs.get(i - 2).getPrivate(), pairs
					.get(i - 2).getPublic()));
			i++;

			// app garden1 from nhouillo
			names.add("CN=Garden1");
			types.add(EntityType.APPLICATION);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 2), pairs.get(i - 2).getPrivate(), pairs
					.get(i - 2).getPublic()));
			i++;

			// app garden2 from acontes
			names.add("CN=Garden2");
			types.add(EntityType.APPLICATION);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 2), pairs.get(i - 2).getPrivate(), pairs
					.get(i - 2).getPublic()));
			i++;

			// app garden3 from nhouillo
			names.add("CN=Garden3");
			types.add(EntityType.APPLICATION);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 4), pairs.get(i - 4).getPrivate(), pairs
					.get(i - 4).getPublic()));
			i++;

			// app garden4 from acontes
			names.add("CN=Garden4");
			types.add(EntityType.APPLICATION);
			pairs.add(keygen.genKeyPair());
			certs.add(CertTools.genCert(names.get(i), 360, null, pairs.get(i)
					.getPrivate(), pairs.get(i).getPublic(), true, names
					.get(i - 4), pairs.get(i - 4).getPrivate(), pairs
					.get(i - 4).getPublic()));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return;
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
			return;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return;
		} catch (SignatureException e) {
			e.printStackTrace();
			return;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return;
		}

		for (int k = 1; k < 5; k++) {
			try {
				KeyStore store = KeyStore.getInstance("PKCS12", "BC");
				store.load(null, null);
				for (int j = 0; j < certs.size(); j++) {
					if (j != k+4) {
						KeyStoreTools.newCertificate(store, new TypedCertificate(certs.get(j), types.get(j), null));
					}
				}

				KeyStoreTools.newApplicationPrivateKey(store, new TypedCertificate(certs.get(k + 4), types.get(k+4), pairs.get(k + 4)
						.getPrivate()));

				File file = new File("dev/security/keystores/keystore" + k + ".p12");
				store.store(new FileOutputStream(file), "ha".toCharArray());
			} catch (KeyStoreException e) {
				e.printStackTrace();
				return;
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
				return;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return;
			} catch (CertificateException e) {
				e.printStackTrace();
				return;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			}
		}

		System.out.println("==========oki==========");
		
		System.exit(0);
	}
}
