importClass(org.ow2.proactive_grid_cloud_portal.cli.ApplicationContext);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.JSHelpCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.SetUrlCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoggingCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.LoggingWithCredentialsCommand);
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
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ChangeJobPriorityCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.PauseJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.ResumeJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.KillJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.RemoveJobCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobResultCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobOutputCommand);
importClass(org.ow2.proactive_grid_cloud_portal.cli.cmd.GetJobStateCommand);
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
	(new LoggingCommand('' + user)).execute();
}

function loginwithcredentials(pathname) {
	s.deleteSession('' + s.getAlias());
	(new LoggingWithCredentialsCommand('' + pathname)).execute();
}

function submit(pathname) {
	(new SubmitJobCommand('' + pathname)).execute();
}

function submitarchive(pathname) {
	(new SubmitJobCommand('' + pathname)).execute();
}

function jobpriority(jobId,priority) {
	(new ChangeJobPriorityCommand('' + jobId, '' + priority)).execute();
}

function pausejob(jobId) {
	(new PauseJobCommand('' + jobId)).execute();
}

function resumejob(jobId) {
	(new ResumeJobCommand('' + jobId)).execute();
}

function killjob(jobId) {
	(new KillJobCommand('' + jobId)).execute();
}

function removejob(jobId) {
	(new RemoveJobCommand('' + jobId)).execute();
}

function jobstate(jobId) {
	(new GetJobStateCommand('' + jobId)).execute();
}

function listjobs() {
	(new ListJobsCommand()).execute();
}

function stats() {
	(new GetStatsCommand()).execute();
}

function jobresult(jobId) {
	(new GetJobResultCommand('' + jobId)).execute();
}

function joboutput(jobId) {
	(new GetJobOutputCommand('' + jobId)).execute();
}

function taskresult(jobId,taskId) {
	(new GetTaskResultCommand('' + jobId, '' + taskId)).execute();
}

function taskoutput(jobId,taskId) {
	(new GetTaskOutputCommand('' + jobId, '' + taskId)).execute();
}

function preempttask(jobId,taskId) {
	(new PreemptTaskCommand('' + jobId, '' + taskId)).execute();
}

function restarttask(jobId,taskId) {
	(new RestartTaskCommand('' + jobId, '' + taskId)).execute();
}

function start() {
	(new StartSchedulerCommand()).execute();
}

function stop() {
	(new StopSchedulerCommand()).execute();
}

function pause() {
	(new PauseSchedulerCommand()).execute();
}

function resume() {
	(new ResumeSchedulerCommand()).execute();
}

function freeze() {
	(new FreezeSchedulerCommand()).execute();
}

function kill() {
	(new KillSchedulerCommand()).execute();
}

function script(path,args) {
	(new EvalScriptCommand('' + path, '' + args)).execute();
}

function linkrm(rmUrl) {
	(new LinkResourceManagerCommand('' + rmUrl)).execute();	
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
