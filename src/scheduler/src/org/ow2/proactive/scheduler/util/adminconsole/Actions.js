importClass(org.ow2.proactive.scheduler.util.adminconsole.AdminScheduler);

function help(){
    AdminScheduler.help();
}

function start(){
    return AdminScheduler.start();
}

function stop(){
    return AdminScheduler.stop();
}

function pause(){
    return AdminScheduler.pause();
}

function freeze(){
    return AdminScheduler.freeze();
}

function resume(){
    return AdminScheduler.resume();
}

function shutdown(){
    return AdminScheduler.shutdown();
}

function kill(){
    return AdminScheduler.kill();
}

function submit(xmlDescriptor){
    return AdminScheduler.submit(""+xmlDescriptor);
}

function pausejob(jobId){
    return AdminScheduler.pause(""+jobId);
}

function resumejob(jobId){
    return AdminScheduler.resume(""+jobId);
}

function killjob(jobId){
    return AdminScheduler.kill(""+jobId);
}

function removejob(jobId){
    AdminScheduler.remove(""+jobId);
}

function result(jobId){
    AdminScheduler.result(""+jobId);
}

function output(jobId){
    AdminScheduler.output(""+jobId);
}

function linkrm(rmURL){
	return AdminScheduler.linkRM(""+rmURL);
}

function exec(commandFilePath){
	AdminScheduler.exec(""+commandFilePath);
}

function exit(){
	AdminScheduler.exit();
}
