/*
 * Created on Sep 1, 2004
 *
 */
package org.objectweb.proactive.core.ssh;

import java.util.Random;
import java.io.IOException;
import java.net.InetAddress;

import org.objectweb.proactive.core.ssh.SshParameters;

import org.apache.log4j.Logger;

import com.jcraft.jsch.*;

/**
 * @author mlacage
 *
 * This class implements a very simple wrapper on top of the jsch
 * library. Most notably, it implements thread-safe and exception-safe
 * creation and destruction of SSH tunnels.

 * Thread-safety is achieved by using a single global lock whenever 
 * we need to access the non-thread-safe jsch library for tunnel
 * creation or destruction operations. This global lock is stored in
 * JSchSingleton and is accessed with the acquireLock and releaseLock
 * methods
 */
public class SshTunnel {
	private static Logger logger = Logger.getLogger(SshTunnel.class.getName());
	private static Random _random = new Random();
	private int _localPort;
	private Session _session;
	private String _username;
	private String _distantHost;
	private String _sshPort;
	private int _distantPort;
		
	/**
	 * Authenticate with the ssh server on the distantHost if needed (authenticated
	 * connections are cached). The ssh server is expected to be listening on port 
	 * proactive.ssh.port. If this property is not set, the server is assumed to be
	 * listening on distantHost:22. The Authentication is performed with the username
	 * proactive.ssh.username if this property is set. It is performed with the value of
	 * user.name otherwise (this is supposed to be the username of the user running this
	 * JVM). The SSH Authentication is performed with the standard public/private key
	 * authentication. The private key used by this client is either any of the private
	 * keys located in proactive.ssh.key_directory if it is set or any of the private 
	 * keys found in user.home/.ssh/.
	 * The public key of the server to which we connect is verified with the file
	 * proactive.ssh.known_hosts if this property is set of user.home/.ssh/known_hosts.
	 * 
	 * Once authentication is performed, a tunnel is established from 
	 * localhost:randomPort to distantHost:distantPort. The randomPort can be otained from
	 * this Tunnel with the getPort method after this constructor has completed 
	 * successfully.
	 * 
	 * @param distantHost the name of the machine to which a tunnel must be established. 
	 * @param distantPort the port number on the distant machine to which a tunnel must
	 *        be established
	 * @throws IOException an exception is thrown if either the authentication or the 
	 *         tunnel establishment fails.
	 */
	public SshTunnel (String distantHost, int distantPort) throws IOException {
		JSchSingleton.acquireLock ();
		try {
			String username = SshParameters.getSshUsername ();
			String sshPort = SshParameters.getSshPort ();		
			Session session;
			try {
				session = JSchSingleton.getSession(username, distantHost, sshPort);
			} catch (IOException e) {
				throw new IOException ("SSH tunnel failed: 127.0.0.1 -->"
						+ distantHost + ":" + distantPort);
			}
			int lport = 0;
			int i = 0;
			while (i < 5) {
				lport = _random.nextInt(65000) + 1024;
				try {
					session.setPortForwardingL ("127.0.0.1", lport, distantHost, distantPort);
					_session = session;
					_localPort = lport;
					_username = username;
					_distantHost = distantHost;
					_distantPort = distantPort;
					_sshPort = sshPort;
					break;
				} catch (JSchException e) {
					i++;
				}
			}
			if (i == 5) {
				throw new IOException ("SSH tunnel failed (could not allocate port number): 127.0.0.1 -->" +
						distantHost + ":" + distantPort);
			}
		} catch (IOException e) {
			throw e;
		} finally {
			JSchSingleton.releaseLock ();
		}
	}
	
	public void realClose () throws Exception {
		JSchSingleton.acquireLock ();
		try {
			logger.debug ("close Tunnel for port " + _localPort);
			JSchSingleton.flushMaybe (_username, _distantHost, _sshPort, _localPort);
			_username = null;
			_distantHost = null;
			_distantPort = 0;
			_sshPort = null;
			_session = null;
			_localPort = 0;
		} catch (Exception e) {
			throw e;
		} finally {
			JSchSingleton.releaseLock ();
		}
	}
	
	/**
	 * This method returns the local port number which can be used
	 * to access this tunnel.
	 * This method cannot fail. 
	 */
	public int getPort () {
		return _localPort;
	}
	
	public InetAddress getInetAddress () throws java.net.UnknownHostException {
		return InetAddress.getByName (_distantHost);
	}
	
	public String getDistantHost () {
		return _distantHost;
	}
	public int getDistantPort () {
		return _distantPort;
	}
}
