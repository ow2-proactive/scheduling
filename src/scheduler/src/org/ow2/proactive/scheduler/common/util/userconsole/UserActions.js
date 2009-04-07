importClass(org.ow2.proactive.scheduler.common.util.userconsole.UserController);

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
    UserController.result(""+jobId);
}

function output(jobId){
    UserController.output(""+jobId);
}

function priority(jobId, priority){
    UserController.priority(""+jobId,""+priority);
}

function jmxinfo(){
    UserController.JMXinfo();
}

function exec(commandFilePath){
	UserController.exec(""+commandFilePath);
}

function exit(){
	UserController.exit();
}
