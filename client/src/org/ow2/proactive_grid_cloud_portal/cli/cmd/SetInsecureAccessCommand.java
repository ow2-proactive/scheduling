package org.ow2.proactive_grid_cloud_portal.cli.cmd;

public class SetInsecureAccessCommand extends AbstractCommand implements
		Command {
	
	public SetInsecureAccessCommand() {
	}

	@Override
	public void execute() throws Exception {
		applicationContext().allowInsecureAccess(true);
	}

}
