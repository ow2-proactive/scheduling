
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ReadKeystore {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
		try {
			KeyStore store = KeyStore.getInstance("PKCS12", "BC");
			store.load(new FileInputStream(new File("dev/security/keystores/keystore1.p12")), "ha".toCharArray());
			
			Enumeration<String> e = store.aliases();
			while (e.hasMoreElements()) {
				String alias = e.nextElement();
				System.out.println(alias);
//				System.out.println(store.getCertificate(alias).toString());
				System.out.println("===============================");
			}
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
