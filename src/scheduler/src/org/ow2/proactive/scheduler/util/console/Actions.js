importClass(org.ow2.proactive.scheduler.util.console.SchedulerModel);

var s_ = SchedulerModel.getModel(false);
var scheduler = s_.getScheduler();

function filterspush(regexp){
	s_.filtersPush_(regexp);
}
function filterspop(){
	return s_.filtersPop_();
}
function filtersclear(){
	s_.filtersClear_();
}
function setpagination(state){
	if (state){
		s_.setPagination_(true);
	} else {
		s_.setPagination_(false);
	}
}

function addcandidate(str){
	if (str == undefined || str == ""){
		str = null;
	}
	s_.addCandidate_(str);
}

function exmode(displayStack, displayOnDemand){
	if (displayStack == undefined){
		displayStack = true;
	}
	if (displayOnDemand == undefined){
		displayOnDemand = true;
	}
	s_.setExceptionMode_(displayStack, displayOnDemand);
}

function cnslhelp(){
	s_.cnslhelp_();
}

function help(){
	s_.help_();
}

function submit(xmlDescriptor){
    return s_.submit_(""+xmlDescriptor);
}

function submitCmd(commandFilePath, jobName, output, selectscript) {
	if (jobName == undefined){
		jobName = null;
	}
	if (output == undefined){
		output = null;
	}
	if (selectscript == undefined){
		selectscript = null;
	}
	return s_.submitCmd_(commandFilePath, jobName, output, selectscript);
}

function pausejob(jobId){
    return s_.pause_(""+jobId);
}

function resumejob(jobId){
    return s_.resume_(""+jobId);
}

function killjob(jobId){
    return s_.kill_(""+jobId);
}

function removejob(jobId){
	return s_.remove_(""+jobId);
}

function jobresult(jobId){
    return s_.result_(""+jobId);
}

function taskresult(jobId,taskName){
    return s_.tresult_(""+jobId,""+taskName);
}

function killtask(jobId,taskName){
    return s_.killt_(""+jobId,""+taskName);
}

function restarttask(jobId,taskName,delay){
	if (delay==undefined){
		println("Default restart delay will be 5 seconds as it is not specified as 3rd argument");
		delay=5;
	}
    return s_.restartt_(""+jobId,""+taskName,""+delay);
}

function preempttask(jobId,taskName,delay){
	if (delay==undefined){
		println("Default restart delay will be 5 seconds as it is not specified as 3rd argument");
		delay=5;
	}
    return s_.preemptt_(""+jobId,""+taskName,""+delay);
}

function joboutput(jobId){
	s_.output_(""+jobId);
}

function taskoutput(jobId,taskName){
	s_.toutput_(""+jobId,""+taskName);
}

function priority(jobId, priority){
	s_.priority_(""+jobId,""+priority);
}

function jobstate(jobId){
	return s_.jobState_(""+jobId);
}

function exec(commandFilePath){
	s_.exec_(""+commandFilePath);
}

function listjobs(){
	return s_.schedulerState_();
}

function stats(){
	s_.showRuntimeData_();
}

function myaccount(){
	s_.showMyAccount_();
}

function account(username){
	s_.showAccount_(username);
}

function reloadpermissions(){
	s_.refreshPermissionPolicy_();
}

function test(){
	s_.test_();
}

function exit(){
	s_.exit_();
}

function start(){
    return s_.start_();
}

function stop(){
    return s_.stop_();
}

function pause(){
    return s_.pause_();
}

function freeze(){
    return s_.freeze_();
}

function resume(){
    return s_.resume_();
}

function shutdown(){
    return s_.shutdown_();
}

function kill(){
    return s_.kill_();
}

function linkrm(rmURL){
	return s_.linkRM_(""+rmURL);
}

function changepolicy(newPolicyName){
	s_.changePolicy_(""+newPolicyName);
}

function reconnect(schedURL){
	if (schedURL == undefined){
		schedURL = null;
	}
	s_.reconnect_();
}

