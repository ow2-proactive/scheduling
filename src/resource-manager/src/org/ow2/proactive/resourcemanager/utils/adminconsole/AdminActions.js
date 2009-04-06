importClass(org.ow2.proactive.resourcemanager.utils.adminconsole.AdminController);

function addnode(nodeURL, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
	return AdminController.addnode(nodeURL, nodeSourceName);
}

function removenode(nodeURL,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
    return AdminController.removenode(nodeURL,preemptively);
}

function gcmdeploy(gcmdFile, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
    return AdminController.gcmdeploy(gcmdFile, nodeSourceName);
}

function createns(nodeSourceName){
    return AdminController.createns(nodeSourceName);
}

function removens(nodeSourceName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminController.removens(nodeSourceName,preemptively);
}

function listnodes(){
    return AdminController.listnodes();
}

function listns(){
    return AdminController.listns();
}

function shutdown(preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminController.shutdown(preemptively);
}

function exec(commandFilePath){
	return AdminController.exec(commandFilePath);
}

function exit(){
	return AdminController.exit();
}
