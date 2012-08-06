importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext);
importClass(org.ow2.proactive_grid_cloud_portal.cli.CLIException);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmJsHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ExitCommand);
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

var s = ApplicationContext.instance();

printWelcomeMsg();

function help() {
    execute(new RmJsHelpCommand());
}

function url(url) {
    execute(new SetUrlCommand('' + url));
}

function login(user) {
    s.setSessionId(null);
    execute(new LoginCommand('' + user));
}

function loginwithcredentials(pathname) {
    s.setSessionId(null);
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

function unlocknode(nodeUrl) {
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
    if (s.getAlias() != null) {
        loginwithcredentials(s.getCredFilePathname());
    } else if (s.getUser() != null) {
        login(s.getUser());
    } else {
        print('use either login(username) or loginwithcredentials(cred-file) function\n')

    }
}

function exit() {
	execute(new ExitCommand());
}

function printWelcomeMsg() {
    print('Type help() for interactive help \r\n');
    if (s.getUser() == null && s.getAlias() == null) {
        print('Warning: You are not currently logged in.\r\n');
    }
}

function execute(cmd) {
    try {
        cmd.execute();
    } catch (e) {
        if (e.javaException instanceof CLIException 
                && (e.javaException.reason() == CLIException.REASON_UNAUTHORIZED_ACCESS)
                && s.getProperty(AbstractLoginCommand.RETRY_LOGIN, java.lang.Boolean.TYPE, false)) {
            s.setProperty(AbstractLoginCommand.RENEW_SESSION, true);
            if (s.getCredFilePathname() != null) {
            	execute(new LoginWithCredentialsCommand(s.getCredFilePathname()));
            } else if (s.getUser() != null) {
            	execute(new LoginCommand(s.getUser()));
            } else {
                throw e;
            }
            cmd.execute();
        } else {
            throw e;
        }
    }
}

