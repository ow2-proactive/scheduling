package org.objectweb.proactive.core.ssh;


/**
 * This class contains all the parameters used by the ssh code.
 * This code documents what default values are used for each
 * of these parameters.
 */
public class SshParameters {
    static private int _connectTimeout = -1;
    static private String _tryNormalFirst = null;


    static public boolean getTryNormalFirst () {
    	if (_tryNormalFirst == null) {
    		_tryNormalFirst = System.getProperty ("proactive.tunneling.try_normal_first");
    	}
    	if (_tryNormalFirst != null && _tryNormalFirst.equals ("yes")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    static public int getConnectTimeout () {
    	if (_connectTimeout == -1) {
    		String timeout = System.getProperty ("proactive.tunneling.connect_timeout");
    		if (timeout != null) {
    			_connectTimeout = Integer.parseInt (timeout);
    		} else {
    			_connectTimeout = 2000;
    		}
    	}
    	return _connectTimeout;
    }

    static public boolean getUseTunnelGC () {
    	String useTunnelGC = System.getProperty ("proactive.tunneling.use_gc");
    	if (useTunnelGC != null && useTunnelGC.equals ("yes")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    static public int getTunnelGCPeriod () {
    	String gcPeriod = System.getProperty ("proactive.tunneling.gc_period");
    	if (gcPeriod != null) {
    		return Integer.parseInt (gcPeriod);
    	} else {
    		// 10s
    		return 10000;
    	}
    }
    
    static public boolean getSshTunneling () {
    	String tunneling = System.getProperty ("proactive.communication.protocol");
    	if (tunneling != null && tunneling.equals ("rmissh")) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    static public String getSshUsername () {
    	String username = System.getProperty ("proactive.ssh.username");
	    if (username == null) {
		    username = System.getProperty ("user.name");
	    }
	    return username;
    }

    static public String getSshPort () {
    	String sshPort = System.getProperty ("proactive.ssh.port");
    	if (sshPort == null) {
    		sshPort = "22";
    	}
    	return sshPort;
    }

    static public String getSshKnownHostsFile () {
    	String hostfile = System.getProperty ("proactive.ssh.known_hosts");
    	if (hostfile == null) {
    		hostfile = System.getProperty ("user.home") + "/.ssh/known_hosts";
    	}
    	return hostfile;
    }

    static public String getSshKeyDirectory () {
    	String keydir = System.getProperty ("proactive.ssh.key_directory");
    	if (keydir == null) {
    		keydir = System.getProperty ("user.home") + "/.ssh/";
    	}
    	return keydir;
    }
}
