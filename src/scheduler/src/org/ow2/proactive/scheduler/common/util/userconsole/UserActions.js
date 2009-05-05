importClass(org.ow2.proactive.scheduler.common.util.userconsole.UserController);

function exMode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	UserController.setExceptionMode(displayStack, displayOnDemand);
}

function help(){
	UserController.help();
}

function submit(xmlDescriptor){
    return UserController.submit(""+xmlDescriptor);
}

function pausejob(jobId){
    return UserController.pause(""+jobId);
}

function resumejob(jobId){
    return UserController.resume(""+jobId);
}

function killjob(jobId){
    return UserController.kill(""+jobId);
}

function removejob(jobId){
    UserController.remove(""+jobId);
}

function result(jobId){
    return UserController.result(""+jobId);
}

function tresult(jobId,taskName){
    return UserController.tresult(""+jobId,""+taskName);
}

function output(jobId){
    UserController.output(""+jobId);
}

function toutput(jobId,taskName){
    UserController.toutput(""+jobId,""+taskName);
}

function priority(jobId, priority){
    UserController.priority(""+jobId,""+priority);
}

function exec(commandFilePath){
	UserController.exec(""+commandFilePath);
}

function exit(){
	UserController.exit();
}

var scheduler = UserController.getUserScheduler();
