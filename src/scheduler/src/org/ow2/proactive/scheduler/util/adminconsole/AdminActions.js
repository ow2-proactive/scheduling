importClass(org.ow2.proactive.scheduler.util.adminconsole.AdminShell);

function start(){
    return AdminShell.start();
}

function stop(){
    return AdminShell.stop();
}

function pause(){
    return AdminShell.pause();
}

function freeze(){
    return AdminShell.freeze();
}

function resume(){
    return AdminShell.resume();
}

function shutdown(){
    return AdminShell.shutdown();
}

function kill(){
    return AdminShell.kill();
}

function linkrm(rmURL){
	return AdminShell.linkRM(""+rmURL);
}
