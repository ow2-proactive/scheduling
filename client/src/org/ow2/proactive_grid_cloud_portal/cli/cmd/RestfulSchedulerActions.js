importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext);
importClass(org.ow2.proactive_grid_cloud_portal.cli.RestCliException);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.JSHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoginWithCredentialsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.EvalScriptCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.StartSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.StopSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PauseSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ResumeSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.FreezeSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.KillSchedulerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LinkResourceManagerCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetStatsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ListJobsCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SubmitJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobStateCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ChangeJobPriorityCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PauseJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ResumeJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.KillJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.RemoveJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobResultCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobOutputCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PreemptTaskCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.RestartTaskCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetTaskOutputCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetTaskResultCommand);

var s = ApplicationContext.instance();

function help() {
	(new JSHelpCommand()).execute();
}

function url(url) {
	(new SetUrlCommand('' + url)).execute();
}

function login(user) {
        s.deleteSession(user);
	(new LoginCommand('' + user)).execute();
}

function loginwithcredentials(pathname) {
	s.deleteSession('' + s.getAlias());
	(new LoginWithCredentialsCommand('' + pathname)).execute();
}

function submit(pathname) {
    execute(new SubmitJobCommand('' + pathname));
}

function submitarchive(pathname) {
    execute(new SubmitJobCommand('' + pathname));
}

function jobpriority(jobId,priority) {
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
    execute(new ListJobsCommand());
}

function stats() {
    execute(new GetStatsCommand());
}

function jobresult(jobId) {
    execute(new GetJobResultCommand('' + jobId));
}

function joboutput(jobId) {
    execute(new GetJobOutputCommand('' + jobId));
}

function taskresult(jobId,taskId) {
    execute(new GetTaskResultCommand('' + jobId, '' + taskId));
}

function taskoutput(jobId,taskId) {
	execute(new GetTaskOutputCommand('' + jobId, '' + taskId));
}

function preempttask(jobId,taskId) {
    execute(new PreemptTaskCommand('' + jobId, '' + taskId));
}

function restarttask(jobId,taskId) {
    execute(new RestartTaskCommand('' + jobId, '' + taskId));
}

function start() {
    execute(new StartSchedulerCommand());
}

function stop() {
    execute(new StopSchedulerCommand());
}

function pause() {
    execute(new PauseSchedulerCommand());
}

function resume() {
    execute(new ResumeSchedulerCommand());
}

function freeze() {
    execute(new FreezeSchedulerCommand());
}

function kill() {
    execute(new KillSchedulerCommand());
}

function script(path,args) {
	execute(new EvalScriptCommand('' + path, '' + args));
}

function linkrm(rmUrl) {
    execute(new LinkResourceManagerCommand('' + rmUrl));	
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
	s.setTerminated(true);	
}

function execute(cmd) {
	try {
		cmd.execute();
	} catch (e) {
		if (e.javaException instanceof RestCliException
				&& e.javaException.errorCode() == 401 && !s.isNewSession()) {
			s.clearSession();
			if (s.getAlias() != null) {
				loginwithcredentials(s.getCredFilePathname());
			} else if (s.getUser() != null) {
				login(s.getUser());
			} else {
				throw e;
			}
			cmd.execute();
		} else {
			throw e;
		}
	}
}
