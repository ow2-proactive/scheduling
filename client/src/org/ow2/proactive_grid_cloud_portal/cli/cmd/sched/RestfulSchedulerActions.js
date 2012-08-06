importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext);
importClass(org.ow2.proactive_grid_cloud_portal.cli.CLIException);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.AbstractLoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.EvalScriptCommand);
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
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.sched.GetTaskResultCommand);

var s = ApplicationContext.instance();

printWelcomeMsg();

function help() {
	execute(new SchedJsHelpCommand());
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

function submit(pathname) {
	execute(new SubmitJobCommand('' + pathname));
}

function submitarchive(pathname) {
	execute(new SubmitJobCommand('' + pathname));
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

function listjobs() {
	execute(new ListJobCommand());
}

function stats() {
	execute(new SchedStatsCommand());
}

function jobresult(jobId) {
	execute(new GetJobResultCommand('' + jobId));
}

function joboutput(jobId) {
	execute(new GetJobOutputCommand('' + jobId));
}

function taskresult(jobId, taskId) {
	execute(new GetTaskResultCommand('' + jobId, '' + taskId));
}

function taskoutput(jobId, taskId) {
	execute(new GetTaskOutputCommand('' + jobId, '' + taskId));
}

function preempttask(jobId, taskId) {
	execute(new PreemptTaskCommand('' + jobId, '' + taskId));
}

function restarttask(jobId, taskId) {
	execute(new RestartTaskCommand('' + jobId, '' + taskId));
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

function reconnect() {
	if (getCredFile(s) != null) {
		loginwithcredentials(getCredFile(s));
	} else if (getUser(s) != null) {
		login(getUser(s));
	} else {
		print('use either login(username) or loginwithcredentials(cred-file) function\n')

	}
}

function exit() {
	s.setTerminated(true);	
}

function getUser() {
	return s.getProperty(LoginCommand.USERNAME, java.lang.String.prototype);
}

function getCredFile() {
	return s.getProperty(LoginWithCredentialsCommand.CRED_FILE, java.lang.String.prototype);
}

function printWelcomeMsg() {
    print('Type help() for interactive help \r\n');
    if (getUser() == null && getCredFile() == null) {
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
            if (getCredFile() != null) {
            	execute(new LoginWithCredentialsCommand(getCredFile()));
            } else if (getUser() != null) {
            	execute(new LoginCommand(getUser()));
            } else {
                throw e;
            }
            cmd.execute();
        } else {
            throw e;
        }
    }
}
