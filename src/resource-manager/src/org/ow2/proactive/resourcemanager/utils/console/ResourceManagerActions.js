importClass(org.ow2.proactive.resourcemanager.utils.console.ResourceManagerModel);

function exMode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	ResourceManagerModel.setExceptionMode(displayStack, displayOnDemand);
}

function addnode(nodeURL, nodeSourceName){
	if (nodeSourceName==undefined){
		nodeSourceName = null;
		println("Node Source will be the default one as it is not set");
	}
	return ResourceManagerModel.addnode(nodeURL, nodeSourceName);
}

function removenode(nodeURL,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
    return ResourceManagerModel.removenode(nodeURL,preemptively);
}

function createns(nsName,infrastructure,policy){
	return ResourceManagerModel.createns(nsName,infrastructure,policy);
}

function removens(nodeSourceName,preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return ResourceManagerModel.removens(nodeSourceName,preemptively);
}

function listnodes(){
    return ResourceManagerModel.listnodes();
}

function listns(){
    return ResourceManagerModel.listns();
}

function listInfrastructures(){
	return ResourceManagerModel.listInfrastructures();
}

function listPolicies(){
	return ResourceManagerModel.listPolicies();
}

function shutdown(preemptively){
	if (preemptively == undefined){
		preemptively = false;
		println("Preemptive mode will be false as it is not set");
	}
	return ResourceManagerModel.shutdown(preemptively);
}

function jmxinfo(){
	ResourceManagerModel.JMXinfo();
}

function exec(commandFilePath){
	return ResourceManagerModel.exec(commandFilePath);
}

function setLogsDir(logsDir){
	if (logsDir == undefined){
		logsDir = "";
	}
	ResourceManagerModel.setLogsDir(""+logsDir);
}

function viewlogs(nbLines){
	ResourceManagerModel.viewlogs(""+nbLines);
}

function exit(){
	return ResourceManagerModel.exit();
}

function help(){
	ResourceManagerModel.help();
}

var rm = ResourceManagerModel.getResourceManager();
