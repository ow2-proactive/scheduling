importClass(org.ow2.proactive.scheduler.util.adminconsole.AdminController);

function start(){
    return AdminController.start();
}

function stop(){
    return AdminController.stop();
}

function pause(){
    return AdminController.pause();
}

function freeze(){
    return AdminController.freeze();
}

function resume(){
    return AdminController.resume();
}

function shutdown(){
    return AdminController.shutdown();
}

function kill(){
    return AdminController.kill();
}

function linkrm(rmURL){
	return AdminController.linkRM(""+rmURL);
}

var scheduler = AdminController.getAdminScheduler();
