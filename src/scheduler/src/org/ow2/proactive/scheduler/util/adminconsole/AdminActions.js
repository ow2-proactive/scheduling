importClass(org.ow2.proactive.scheduler.util.adminconsole.AdminSchedulerModel);

function start(){
    return AdminSchedulerModel.start();
}

function stop(){
    return AdminSchedulerModel.stop();
}

function pause(){
    return AdminSchedulerModel.pause();
}

function freeze(){
    return AdminSchedulerModel.freeze();
}

function resume(){
    return AdminSchedulerModel.resume();
}

function shutdown(){
    return AdminSchedulerModel.shutdown();
}

function kill(){
    return AdminSchedulerModel.kill();
}

function linkrm(rmURL){
	return AdminSchedulerModel.linkRM(""+rmURL);
}

function setLogsDir(logsDir){
	if (logsDir == undefined){
		logsDir = "";
	}
	AdminSchedulerModel.setLogsDir(""+logsDir);
}

function viewlogs(nbLines){
	AdminSchedulerModel.viewlogs(""+nbLines);
}

function viewDevlogs(nbLines){
	AdminSchedulerModel.viewDevlogs(""+nbLines);
}

var scheduler = AdminSchedulerModel.getAdminScheduler();
