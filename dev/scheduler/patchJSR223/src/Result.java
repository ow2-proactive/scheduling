/**
 * Result is return to the GUI.
 * It will be treat in order to display proper Error or Warning message to the user.
 * 
 * @author SCHEEFER Jean-Luc (jlscheef) - ProActiveTeam
 * @version 1, Jan 8, 2008
 * @since ProActive 3.9
 */
public class Result {
	private boolean success;
	private String msg;
	private boolean force = false;
	public Result(boolean success, String msg){
		this.success = success;
		this.msg = msg;
	}
	public Result(boolean success, String msg, boolean force){
		this.success = success;
		this.msg = msg;
		this.force = force;
	}
	public String getMsg() {
		return msg;
	}
	public boolean isSuccess() {
		return success;
	}
	public boolean isForce() {
		return force;
	}
}
