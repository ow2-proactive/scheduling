package org.ow2.proactive_grid_cloud_portal.cli.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

public class HttpClientUtil {

	private HttpClientUtil() {
	}

	public static void setInsecureAccess(HttpClient client) throws Exception {
		SSLSocketFactory socketFactory = new SSLSocketFactory(
				new RelaxedTrustStrategy(),
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme https = new Scheme("https", 443, socketFactory);
		client.getConnectionManager().getSchemeRegistry().register(https);
	}

	private static class RelaxedTrustStrategy implements TrustStrategy {
		@Override
		public boolean isTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			return true;
		}
	}
}
