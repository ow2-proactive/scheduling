importClass(org.ow2.proactive.resourcemanager.utils.adminconsole.AdminShell);

function addnode(nodeName, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
	return AdminRM.addnode(nodeName, nodeSourceName);
}

function removenode(nodeName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
    return AdminRM.removenode(nodeName,preemptively);
}

function gcmdeploy(gcmdFile, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
    return AdminRM.gcmdeploy(gcmdFile, nodeSourceName);
}

function createns(nodeSourceName){
    return AdminRM.createns(nodeSourceName);
}

function removens(nodeSourceName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminRM.removens(nodeSourceName,preemptively);
}

function listnodes(){
    return AdminRM.listnodes();
}

function listns(){
    return AdminRM.listns();
}

function shutdown(preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminRM.shutdown(preemptively);
}

function exec(commandFilePath){
	return AdminRM.exec(commandFilePath);
}

function exit(){
	return AdminRM.exit();
}
