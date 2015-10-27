try {
    load("nashorn:mozilla_compat.js");
    this.println = print
    stringClass = Java.type("java.lang.String")['class']
} catch (e) {
    stringClass = java.lang.String.prototype
}

importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContextImpl);

importClass(org.ow2.proactive_grid_cloud_portal.cli.CLIException);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);

importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractIModeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PrintSessionCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetSilentCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.EvalScriptCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.JsHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetDebugModeCommand);

importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.rm.RmJsHelpCommand);
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

importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.LoginSchedCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PutThirdPartyCredentialCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.RemoveThirdPartyCredentialCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ThirdPartyCredentialKeySetCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedJsHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.StartCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.StopCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PauseCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ResumeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.FreezeCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.KillCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.LinkRmCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SchedStatsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListJobTasksCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.SubmitJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobStateCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ChangeJobPriorityCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PauseJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ResumeJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.KillJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.RemoveJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobResultCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetJobOutputCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.PreemptTaskCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.RestartTaskCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetTaskOutputCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.ListTaskStatesCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetTaskResultCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.UploadFileCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.DownloadFileCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.LiveLogCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.JsVersionCommand);

importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractCommand);
importClass(com.google.common.collect.ObjectArrays);


var currentContext = ApplicationContextImpl.currentContext();

function url(url) {
    execute(new SetUrlCommand('' + url));
}

function silent() {
    execute(new SetSilentCommand());
}

function exit() {
    currentContext.setProperty(AbstractIModeCommand.TERMINATE, true);
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

function printError(error, currentContext) {
    print("An error occurred while executing the command:\r\n" + error.message + "\r\n");
    if (AbstractCommand.isDebugModeEnabled(currentContext)) {
        if (error.javaException != null) {
            error.javaException.printStackTrace();
        } else {
            error.printStackTrace(); // if executed with JDK8
        }
    }
}

function reconnect() {
    if (getCredFile(currentContext) != null) {
        loginwithcredentials(getCredFile(currentContext));
    } else if (getUser(currentContext) != null) {
        login(getUser(currentContext));
    } else {
        print('use either login(username) or loginwithcredentials(cred-file) function\r\n')

    }
}

function debug(flag) {
    execute(new SetDebugModeCommand(string(flag)));
}

function string(obj) {
    return '' + obj;
}

function getUser(context) {
    return context.getProperty(LoginCommand.USERNAME, stringClass);
}

function getCredFile(context) {
    return context.getProperty(LoginWithCredentialsCommand.CRED_FILE, stringClass);
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
            printError(e, currentContext);
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
            printError(e, currentContext);
        }
    }
}

function schedulerlogin(user) {
    currentContext.setSessionId('');
    execute(new LoginSchedCommand('' + user));
}

function login(user) {
    currentContext.setProperty(AbstractLoginCommand.PROP_RENEW_SESSION, true);
    execute(new LoginCommand('' + user));
}

function loginwithcredentials(pathname) {
    currentContext.setProperty(AbstractLoginCommand.PROP_RENEW_SESSION, true);
    execute(new LoginWithCredentialsCommand('' + pathname));
}

// scheduler

function schedulerhelp() {
    execute(new SchedJsHelpCommand());
}

function putcredential(key, value) {
    execute(new PutThirdPartyCredentialCommand(string(key), string(value)));
}

function removecredential(key) {
    execute(new RemoveThirdPartyCredentialCommand(string(key)));
}

function listcredentials() {
    execute(new ThirdPartyCredentialKeySetCommand());
}

function submit(pathname, variables) {
    if (typeof variables == 'undefined') {
        execute(new SubmitJobCommand([string(pathname)]));
    } else {
        execute(new SubmitJobCommand(new Array([string(pathname)], variables)));
    }
}

function submitarchive(pathname, variables) {
     if (typeof variables == 'undefined') {
        execute(new SubmitJobCommand([string(pathname)]));
    } else {
        execute(new SubmitJobCommand(ObjectArrays.concat([string(pathname)], variables, java.lang.String)));
    }
}

function jobpriority(jobId, priority) {
    execute(new ChangeJobPriorityCommand('' + jobId, '' + priority));
}

function pausejob(jobId) {
    execute(new PauseJobCommand('' + jobId));
}

function resumejob(jobId) {
    execute(new ResumeJobCommand('' + jobId));
}

function killjob(jobId) {
    execute(new KillJobCommand('' + jobId));
}

function removejob(jobId) {
    execute(new RemoveJobCommand('' + jobId));
}

function jobstate(jobId) {
    execute(new GetJobStateCommand('' + jobId));
}

function listjobs(x, y) {
    if (typeof x == 'undefined') {
        execute(new ListJobCommand());
    } else if (typeof y == 'undefined') {
        execute(new ListJobCommand('latest=' + x));
    } else {
        execute(new ListJobCommand(['from=' + x, 'limit=' + y]));
    }
}

/*
 * The following parameters have different meaning whether
 * listtasks is called with 1,2,3 or 4 parameters.
 * 1 parameter : arg1 is the jobId
 * 2 parameters : arg1 is the jobId
 * 				  arg2 is the tag
 * 3 parameters : arg1 is the jobId
 * 				  arg2 is the offset
 * 				  arg3 is the limit
 * 4 parameters : arg1 is the jobId
 * 				  arg2 is the tag
 * 				  arg3 is the offset
 * 				  arg4 is the limit
 * 
 */
function listtasks(arg1, arg2, arg3, arg4) {
	// There is only one parameter, the jobId
	if (arguments.length == 1 && typeof arg1 != 'undefined') {
		execute(ListJobTasksCommand.LJTCommandBuilder.newInstance().jobId(arg1).instance());
	}
	// Two arguments: jobId and tag
	if (arguments.length == 2 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined') {
		execute(ListJobTasksCommand.LJTCommandBuilder.newInstance().jobId(arg1).tag(arg2).instance());
	}
	// Three parameters: jobId, offset and limit
	if (arguments.length == 3 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined' && typeof arg3 != 'undefined') {
		execute(ListJobTasksCommand.LJTCommandBuilder.newInstance().jobId(arg1).offset(arg2).limit(arg3).instance());
	}
	// Four parameters: jobId, tag, offset and limit
	// We don't parse any parameters after the fourth one
	if (arguments.length >= 4 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined' && typeof arg3 != 'undefined'
			&& typeof arg4 != 'undefined') {
		execute(ListJobTasksCommand.LJTCommandBuilder.newInstance().jobId(arg1).tag(arg2).offset(arg3).limit(arg4).instance());
	}
}

function schedulerstats() {
    execute(new SchedStatsCommand());
}

function jobresult(jobId, tag) {
    if(typeof tag == 'undefined'){
        execute(new GetJobResultCommand('' + jobId));
    }
    else{
        execute(new GetJobResultCommand('' + jobId, tag));
    }

}

function joboutput(jobId, tag) {
    if(typeof tag == 'undefined'){
        execute(new GetJobOutputCommand('' + jobId));
    }
    else{
        execute(new GetJobOutputCommand('' + jobId, tag));
    }

}

function taskresult(jobId, taskId) {
    execute(new GetTaskResultCommand('' + jobId, '' + taskId));
}

function taskoutput(jobId, taskId) {
    execute(new GetTaskOutputCommand('' + jobId, '' + taskId));
}

/*
 * The following parameters have different meaning whether
 * taskstates is called with 1,2,3 or 4 parameters.
 * 1 parameter : arg1 is the jobId
 * 2 parameters : arg1 is the jobId
 * 				  arg2 is the tag
 * 3 parameters : arg1 is the jobId
 * 				  arg2 is the offset
 * 				  arg3 is the limit
 * 4 parameters : arg1 is the jobId
 * 				  arg2 is the tag
 * 				  arg3 is the offset
 * 				  arg4 is the limit
 * 
 */
function taskstates(arg1, arg2, arg3, arg4){
	// There is only one parameter, the jobId
	if (arguments.length == 1 && typeof arg1 != 'undefined') {
		execute(new ListTaskStatesCommand(arg1));
	}
	// Two arguments: jobId and tag
	if (arguments.length == 2 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined') {
		execute(new ListTaskStatesCommand(arg1, arg2));
	}
	// Three parameters: jobId, offset and limit
	if (arguments.length == 3 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined' && typeof arg3 != 'undefined') {
		execute(new ListTaskStatesCommand(arg1, arg2, arg3));
	}
	// Four parameters: jobId, tag, offset and limit
	// We don't parse any parameters after the fourth one
	if (arguments.length >= 4 && typeof arg1 != 'undefined'
		&& typeof arg2 != 'undefined' && typeof arg3 != 'undefined'
			&& typeof arg4 != 'undefined') {
		execute(new ListTaskStatesCommand(arg1, arg2, arg3, arg4));
	}
}

function preempttask(jobId, taskId) {
    execute(new PreemptTaskCommand('' + jobId, '' + taskId));
}

function restarttask(jobId, taskId) {
    execute(new RestartTaskCommand('' + jobId, '' + taskId));
}

function uploadfile(spaceName, filePath, fileName, localFile) {
    execute(new UploadFileCommand(string(spaceName), string(filePath),string(fileName), string(localFile)));
}

function downloadfile(spaceName, pathName, localFile) {
    execute(new DownloadFileCommand(string(spaceName), string(pathName), string(localFile))) ;
}

function livelog(jobId) {
    execute(new LiveLogCommand(string(jobId)));
}

function start() {
    execute(new StartCommand());
}

function stop() {
    execute(new StopCommand());
}

function pause() {
    execute(new PauseCommand());
}

function resume() {
    execute(new ResumeCommand());
}

function freeze() {
    execute(new FreezeCommand());
}

function kill() {
    execute(new KillCommand());
}

function script(path, args) {
    execute(new EvalScriptCommand('' + path, '' + args));
}

function linkrm(rmUrl) {
    execute(new LinkRmCommand('' + rmUrl));
}

// rm

function rmhelp() {
    execute(new RmJsHelpCommand());
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
	var result = [];
	for (var i = 0; i < arguments.length; i++) {
	    result[i] = arguments[i];
	}
    execute(new LockNodeCommand(result));
}

function unlocknodes(nodeUrl) {
	var result = [];
	for (var i = 0; i < arguments.length; i++) {
	    result[i] = arguments[i];
	}
    execute(new UnlockNodeCommand(result));
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

function rmstats() {
    execute(new RmStatsCommand());
}

function shutdown() {
    execute(new ShutdownCommand());
}

function help() {
    execute(new JsHelpCommand());
}

function version() {
    execute(new JsVersionCommand())
}

printWelcomeMsg();

