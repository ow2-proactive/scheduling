package org.objectweb.proactive.core.ssh;

import com.jcraft.jsch.UserInfo;

public class UserInfoNone implements UserInfo {
	public UserInfoNone () {}
	
	public String getPassphrase() {
		return null;
	}
	public String getPassword() {
		return null;
	}
	public boolean promptPassword(String message) {
		return false;
	}
	public boolean promptPassphrase(String message) {
		return false;
	}
	public boolean promptYesNo(String message) {
		return false;
	}

	public void showMessage(String message) {
		System.out.println (message);
	}
}
