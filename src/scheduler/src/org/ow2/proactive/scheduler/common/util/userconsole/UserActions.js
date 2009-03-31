importClass(org.ow2.proactive.scheduler.common.util.userconsole.UserShell);

function help(){
	UserShell.help();
}

function submit(xmlDescriptor){
    return UserShell.submit(""+xmlDescriptor);
}

function pausejob(jobId){
    return UserShell.pause(""+jobId);
}

function resumejob(jobId){
    return UserShell.resume(""+jobId);
}

function killjob(jobId){
    return UserShell.kill(""+jobId);
}

function removejob(jobId){
    UserShell.remove(""+jobId);
}

function result(jobId){
    UserShell.result(""+jobId);
}

function output(jobId){
    UserShell.output(""+jobId);
}

function priority(jobId, priority){
    UserShell.priority(""+jobId,""+priority);
}

function exec(commandFilePath){
	UserShell.exec(""+commandFilePath);
}

function exit(){
	UserShell.exit();
}
