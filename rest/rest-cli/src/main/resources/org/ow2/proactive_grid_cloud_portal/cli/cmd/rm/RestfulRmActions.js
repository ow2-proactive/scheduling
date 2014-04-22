try {
    load("nashorn:mozilla_compat.js");
    this.println = print
    stringClass = Java.type("java.lang.String")['class']
} catch (e) {
    stringClass = java.lang.String.prototype
}

importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl);
importClass(org.ow2.proactive_grid_cloud_portal.cli.CLIException);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmJsHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PrintSessionCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetSilentCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.AddNodeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RemoveNodeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetInfrastructureCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.SetPolicyCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.CreateNodeSourceCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RemoveNodeSourceCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.LockNodeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.UnlockNodeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListNodeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListNodeSourceCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListInfrastructureCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ListPolicyCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.GetNodeInfoCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmStatsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.GetTopologyCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.ShutdownCommand);

var currentContext = ApplicationContextImpl.currentContext();

printWelcomeMsg();

function help() {
    execute(new RmJsHelpCommand());
}

function url(url) {
    execute(new SetUrlCommand('' + url));
}

function login(user) {
    currentContext.setProperty('org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.renewSession', true);
    execute(new LoginCommand('' + user));
}

function loginwithcredentials(pathname) {
    currentContext.setProperty('org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand.renewSession', true);
    execute(new LoginWithCredentialsCommand('' + pathname));
}

function addnode(nodeUrl, nodeSource) {
    if (nodeSource) {
        execute(new AddNodeCommand('' + nodeUrl, '' + nodeSource));
    } else {
        execute(new AddNodeCommand('' + nodeUrl));
    }
}

function removenode(nodeUrl, preempt) {
    execute(new RemoveNodeCommand('' + nodeUrl, preempt));
}

function createns(nodeSource, infrastructure, policy) {
    execute(new SetInfrastructureCommand(infrastructure));
    execute(new SetPolicyCommand(policy));
    execute(new CreateNodeSourceCommand('' + nodeSource));
}

function removens(nodeSource, preempt) {
    execute(new RemoveNodeSourceCommand('' + nodeSource, preempt));
}

function locknodes(nodeUrl) {
    execute(new LockNodeCommand(nodeUrl));
}

function unlocknodes(nodeUrl) {
    execute(new UnlockNodeCommand(nodeUrl));
}

function listnodes(nodeSource) {
    if (nodeSource) {
        execute(new ListNodeCommand('' + nodeSource));
    } else {
        execute(new ListNodeCommand());
    }
}

function listns() {
    execute(new ListNodeSourceCommand());
}

function nodeinfo(nodeUrl) {
    execute(new GetNodeInfoCommand('' + nodeUrl));
}

function listinfrastructures() {
    execute(new ListInfrastructureCommand());
}

function listpolicies() {
    execute(new ListPolicyCommand());
}

function topology() {
    execute(new GetTopologyCommand());
}

function stats() {
    execute(new RmStatsCommand());
}

function shutdown() {
    execute(new ShutdownCommand());
}

function reconnect() {
	if (getCredFile(currentContext) != null) {
		loginwithcredentials(getCredFile(currentContext));
	} else if (getUser(currentContext) != null) {
		login(getUser(currentContext));
	} else {
		print('use either login(username) or loginwithcredentials(cred-file) function\n')

	}
}

function silent() {
    execute(new SetSilentCommand());
}

function exit() {
	currentContext.setProperty(AbstractIModeCommand.TERMINATE, true);	
}

function getUser(context) {
    return context.getProperty(LoginCommand.USERNAME, stringClass);
}

function getCredFile(context) {
    return context.getProperty(LoginWithCredentialsCommand.CRED_FILE, stringClass);
}

function printWelcomeMsg() {
    print('Type help() for interactive help \r\n');
     if (getUser(currentContext) == null && getCredFile(currentContext) == null) {
        print('Warning: You are not currently logged in.\r\n');
    }
}

function prints() {
    execute(new PrintSessionCommand());
}

function execute(cmd) {
    var tryAfterReLogin = false;
    try {
        cmd.execute(currentContext);
    } catch (e) {
        if (e.javaException instanceof CLIException 
                && (e.javaException.reason() == CLIException.REASON_UNAUTHORIZED_ACCESS)
                && currentContext.getProperty(AbstractLoginCommand.PROP_PERSISTED_SESSION, java.lang.Boolean.TYPE, false)) {
            tryAfterReLogin = true;
        } else {
            printError(e);
        }
    }
    if (tryAfterReLogin) {
	currentContext.setProperty(AbstractLoginCommand.PROP_RENEW_SESSION, java.lang.Boolean.TRUE);
        try {
            if (getCredFile(currentContext) != null) {
                execute(new LoginWithCredentialsCommand(getCredFile(currentContext)));
            } else if (getUser(currentContext) != null) {
                execute(new LoginCommand(getUser(currentContext)));
            }
            cmd.execute(currentContext);
        } catch (e) {
            printError(e);
        }
    }
}

function printError(error) {
    print("An error occurred while executing the command:\r\n");
    if (error.javaException != null) {
        error.javaException.printStackTrace();
    } else {
        error.printStackTrace(); // if executed with JDK8
    }
}


