importClass(org.ow2.proactive.resourcemanager.utils.adminconsole.AdminRMModel);

function exMode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	AdminRMModel.setExceptionMode(displayStack, displayOnDemand);
}

function addnode(nodeURL, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
	return AdminRMModel.addnode(nodeURL, nodeSourceName);
}

function removenode(nodeURL,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
    return AdminRMModel.removenode(nodeURL,preemptively);
}

function gcmdeploy(gcmdFile, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
    return AdminRMModel.gcmdeploy(gcmdFile, nodeSourceName);
}

function createns(nodeSourceName){
    return AdminRMModel.createns(nodeSourceName);
}

function removens(nodeSourceName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminRMModel.removens(nodeSourceName,preemptively);
}

function listnodes(){
    return AdminRMModel.listnodes();
}

function listns(){
    return AdminRMModel.listns();
}

function shutdown(preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return AdminRMModel.shutdown(preemptively);
}

function jmxinfo(){
	AdminRMModel.JMXinfo();
}

function exec(commandFilePath){
	return AdminRMModel.exec(commandFilePath);
}

function setLogsDir(logsDir){
	if (logsDir == undefined){
		logsDir = "";
	}
	AdminRMModel.setLogsDir(""+logsDir);
}

function viewlogs(nbLines){
	AdminRMModel.viewlogs(""+nbLines);
}

function exit(){
	return AdminRMModel.exit();
}

function help(){
	AdminRMModel.help();
}

var rm = AdminRMModel.getAdminRM();
