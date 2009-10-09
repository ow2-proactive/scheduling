importClass(org.ow2.proactive.scheduler.common.util.userconsole.UserSchedulerModel);

function exMode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	UserSchedulerModel.setExceptionMode(displayStack, displayOnDemand);
}

function help(){
	UserSchedulerModel.help();
}

function submit(xmlDescriptor){
    return UserSchedulerModel.submit(""+xmlDescriptor);
}

function pausejob(jobId){
    return UserSchedulerModel.pause(""+jobId);
}

function resumejob(jobId){
    return UserSchedulerModel.resume(""+jobId);
}

function killjob(jobId){
    return UserSchedulerModel.kill(""+jobId);
}

function removejob(jobId){
    UserSchedulerModel.remove(""+jobId);
}

function result(jobId){
    return UserSchedulerModel.result(""+jobId);
}

function tresult(jobId,taskName){
    return UserSchedulerModel.tresult(""+jobId,""+taskName);
}

function output(jobId){
    UserSchedulerModel.output(""+jobId);
}

function toutput(jobId,taskName){
    UserSchedulerModel.toutput(""+jobId,""+taskName);
}

function priority(jobId, priority){
    UserSchedulerModel.priority(""+jobId,""+priority);
}

function jobstate(jobId){
	UserSchedulerModel.jobState(""+jobId);
}

function exec(commandFilePath){
	UserSchedulerModel.exec(""+commandFilePath);
}

function joblist(){
	UserSchedulerModel.schedulerState();
}

function jmxinfo(){
	UserSchedulerModel.JMXinfo();
}

function test(){
	UserSchedulerModel.test();
}

function exit(){
	UserSchedulerModel.exit();
}

var scheduler = UserSchedulerModel.getUserScheduler();
