importClass(org.ow2.proactive.resourcemanager.utils.adminconsole.AdminShell);

function addnode(nodeName, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
	return AdminShell.addnode(nodeName, nodeSourceName);
}

function removenode(nodeName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
    return AdminShell.removenode(nodeName,preemptively);
}

function gcmdeploy(gcmdFile, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
    return AdminShell.gcmdeploy(gcmdFile, nodeSourceName);
}

function createns(nodeSourceName){
    return AdminShell.createns(nodeSourceName);
}

function removens(nodeSourceName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminShell.removens(nodeSourceName,preemptively);
}

function listnodes(){
    return AdminShell.listnodes();
}

function listns(){
    return AdminShell.listns();
}

function shutdown(preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminShell.shutdown(preemptively);
}

function exec(commandFilePath){
	return AdminShell.exec(commandFilePath);
}

function exit(){
	return AdminShell.exit();
}
