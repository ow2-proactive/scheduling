package org.objectweb.proactive.core.ssh;

import java.io.IOException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.util.Hashtable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;

import org.objectweb.proactive.core.ssh.SshParameters;

import org.apache.log4j.Logger;


public class JSchSingleton {
	private static Logger logger = Logger.getLogger(JSchSingleton.class.getName());
	
	private JSchSingleton () {}
	
	static private Semaphore _sem = null;
	static private Semaphore getSem () {
		if (_sem == null) {
			_sem = new Semaphore (1);
		}
		return _sem;
	}
	
	/**
	 * Everytime you want to access one of the methods exported
	 * by the JSch library, you need to acquire this global lock.
	 */
	static public void acquireLock () {
		Semaphore sem = getSem ();
		sem.down ();
	}
	static public void releaseLock () {
		Semaphore sem = getSem ();
		sem.up ();
	}
	
	static private JSch getJSch () {
		if (_jsch == null) {
			Hashtable config = new Hashtable ();
			config.put ("StrictHostKeyChecking", "no");
			JSch.setConfig (config);
			_jsch = new JSch ();
			try {
			    String hostfile = SshParameters.getSshKnownHostsFile ();
				InputStream is = new FileInputStream (hostfile);
				_jsch.setKnownHosts (is);
			} catch (Exception e) {
				e.printStackTrace();
				}
			try {
				String keydir = SshParameters.getSshKeyDirectory ();
				logger.debug ("read key dir: " + keydir);
				File parent = new File (keydir);
		            String[] children = parent.list();
		            for (int i = 0; i < children.length; i++) {
		            	String filename = children[i];
		            	if (filename.endsWith (".pub")) {
		            		String privKeyFilename = filename.substring(0, filename.length () - 4);
		            		File privKeyFile = new File (parent, privKeyFilename);
		            		if (privKeyFile.canRead ()) {
		            			try {
		            				logger.debug ("adding identity " + privKeyFile.getPath ());
		            				_jsch.addIdentity (privKeyFile.getPath (), null);
		            			} catch (Exception e) {
		            				e.printStackTrace();
		            				}
		            		}
		            	}
		            }
			} catch (Exception e) {
			    e.printStackTrace();
			    }
		}
		return _jsch;
	}
	static private JSch _jsch = null;
	
	static public Session getSession (String username, String hostname, String sshPort) 
			throws IOException {
		if (_hash == null) {
			_hash = new Hashtable ();
		}
		String key = sshPort + username + hostname;
		Object val = _hash.get (key);
		Session session;
		if (val == null) {
			int port = Integer.parseInt (sshPort);
			try {
				session = getJSch ().getSession (username, hostname, port);
				session.setUserInfo (new UserInfoNone ());
			} catch (JSchException e) {
				throw new IOException (e.getMessage ());
			}
			_hash.put (key, session);
		} else {
			session = (Session) val;
		}
		if (!session.isConnected ()) {
			try {
				session.connect ();
			} catch (JSchException e) {
				e.printStackTrace();
				throw new IOException (e.getMessage ());
			}
		}
		return session;
	}
	static private Hashtable _hash = null; 
	
	static public void flushMaybe (String username, String hostname, String sshPort, int localPort) {
		String key = sshPort + username + hostname;
		Object val = _hash.get (key);
		if (val == null) {
			return;
		}
		Session session = (Session) val;
		try {
			session.delPortForwardingL (localPort);
			int nForward = session.getPortForwardingL ().length;
			if (nForward == 0) {
				_hash.remove (key);
				session.disconnect ();
			}
		} catch (JSchException e) {
		    e.printStackTrace();
		    }
	}
}
