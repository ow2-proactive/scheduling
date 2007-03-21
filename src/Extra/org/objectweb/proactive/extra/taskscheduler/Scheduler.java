/*
* ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.taskscheduler;

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.exceptions.proxy.SendRequestCommunicationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.util.wrapper.GenericTypeWrapper;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.taskscheduler.policy.GenericPolicy;
import org.objectweb.proactive.taskscheduler.resourcemanager.GenericResourceManager;


/**
 * 
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i>
 * Scheduler core this is the main active object, it commnicates with resource manager to acquire nodes and with the policy to insert and get jobs from the queue
 * @author walzouab
 *
 */
public class Scheduler implements RunActive, RequestFilter {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.TASK_SCHEDULER);
    private GenericPolicy policy; //holds the policy used 
    private GenericResourceManager resourceManager;
    private Vector<InternalTask> runningTasks;
    private Vector<InternalTask> finishedTasks;
    
    
  
    private long previousRunOfPingAll;

    private long TIME_BEFORE_TEST_ALIVE;
    private long SCHEDULER_TIMEOUT;
    
    
    
    /*
     * the following two parameters will be useful in case a nodepool was introduced again in the future
     * they are kept in the proactive configuration xml file and the will be commented out in the fct configrescheudler
    private long TIME_BEFORE_RETURNING_NODES_TO_RM;
    private double PERCENTAGE_OF_RESOURCES_TO_RETURN;*/
    private boolean shutdown;
	private boolean hardShutdown;

    /**
     * NoArg Constructor Required By ProActive
     *
     */
    public Scheduler() {
    }


    /**
     * Creates a new scheduler
     * @param policy name is a string that referes to the class's formal name and package
     * @param resourceManager
     */
    public Scheduler(String p, GenericResourceManager rm,String policyFactory)
        throws Exception 
        {
    	
    	//TODO : this point here creates the policy , changes must be accomoated for here
        try {
        	
        	//the policy factory is inovked here using reflection
            policy = (GenericPolicy) Class.forName(p).getMethod(policyFactory,GenericResourceManager.class).invoke(null, rm);
        } catch (java.lang.ClassNotFoundException e) {
            logger.error("The policy class couldn't be found " + e.toString());
            throw e;
        } catch (java.lang.IllegalAccessException e) {
            logger.error("The method cannot be acessed " +
                e.toString());
            throw e;
        } catch (java.lang.ClassCastException e) {
            logger.error(
                "The policy class doesn't implement Interface GenericPolicy " +
                e.toString());
            throw e;
        } catch (java.lang.NoClassDefFoundError e) {
            logger.error(
                "Couldnt find the class defintion, its a serious error, it might be however due to case sentivity " +
                e.toString());
            throw e;
        } catch (IllegalArgumentException e) {
        	logger.error(
                    "The argument passed isnt correct" +
                    e.toString());
                throw e;
		} catch (InvocationTargetException e) {
			logger.error(
	                "policy constructor threw an exception" +
	                e.toString());
	            throw e;
		} catch (NoSuchMethodException e) {
			logger.error(
	                "Error:Must provide a static policy factory of signature GenericPolicy foo(GenericResourceManager) " +
	                e.toString());
	            throw e;
		} 
		catch (Exception e) {
            logger.error(
                "An error occured trying to instantiate policy class, please revise it. " +
                e.toString());
            throw e;
        }
        
        
        resourceManager = rm;

        runningTasks = new Vector<InternalTask>();
        finishedTasks = new Vector<InternalTask>();
       
       
        previousRunOfPingAll = System.currentTimeMillis();
        shutdown=false;
        hardShutdown=false;
        configureScheduler();
        
        logger.info("Scheduler intialized in Stop Mode.");
    }

    /**
     * a function that configures scheduler paramters
     * @throws Exception
     */
    public void configureScheduler() throws Exception
    {
    	try{
    	SCHEDULER_TIMEOUT=Long.parseLong(System.getProperty("proactive.taskscheduler.scheduler_timeout"));
    	
    	
    	
    	
        TIME_BEFORE_TEST_ALIVE=Long.parseLong(System.getProperty("proactive.taskscheduler.time_before_test_alive"));

    	
        if(this.SCHEDULER_TIMEOUT<1)
    		throw new Exception("SCHEDULER_TIMEOUT must be a positive integer");
    	if(this.TIME_BEFORE_TEST_ALIVE<1)
    		throw new Exception("TIME_BEFORE_TEST_ALIVE must be a positive integer");
    	
    	
    		
    	}
    	catch(Exception e)
    	{
    		logger.error("An error trying to parse scheduler arguments "+e.toString());
    		throw e;
    		
    	}

    	
    }
    /**
     * This fucntion is the main thread of the scheduler, it is called automatically by proactive.
     * A high priortiy given to submission of requests ad checking if execution is finished because they
     * WARNING, very important. This filter must be used by every single call to any kind of general serve request(ie serve request without mentioned a request name) otherwise the scheduler might crash
     * @author walzouab
     */
    public void runActivity(Body body) {
        Service service = new Service(body);

        while (!shutdown) {
            //serve all available submit and isfinished requests--nonblocking
        	service.serveAll("submit");
            service.serveAll("isFinished");
           
            
            
            //if softshutdown has been initiated, it stops scheduling. if resources are returned and runiing tasks finished execution
            
            schedule();
            
            manageRunningTasks();
            pingAll();
          
           
            //serve all available submit and isfinished requests--nonblocking
            service.serveAll("submit");
            service.serveAll("isFinished");
            //serveone request
            service.blockingServeOldest(this,SCHEDULER_TIMEOUT);
        }
        
        //if it reaches this pooint thsi means sfotshutdown was intiated and must clean up
       
        
        
        
        logger.info("Shutdown Started");
        
        
        //now set all get result requests that cann never be served to killed  
         boolean onlyTerminateLeft;//this field indicates that all requests other than terminate has been served
         
         
     	
    	if(logger.isDebugEnabled())
    		{
    		onlyTerminateLeft=((service.getRequestCount()==1)&&service.hasRequestToServe("terminateScheduler"))||(service.getRequestCount()==0);
    		logger.debug("running tasks size "+runningTasks.size());
    		
    		logger.debug("service request count "+service.getRequestCount());
    		if(service.getOldest()!=null)
    		logger.debug("oldest method is "+service.getOldest().getMethodName());
    		else logger.debug("terminate not called yet!!");
    		logger.debug("only terminate left? "+onlyTerminateLeft);
    		}
    			
    
        int i=0;
         do 
        {
        	try{
        	Thread.sleep(SCHEDULER_TIMEOUT);}catch(Exception e){}
        	
        	
        
        	manageRunningTasks();
        
        	pingAll();
        
        
        	
        	
          
        	service.serveAll(this);
        	
        	
        	onlyTerminateLeft=((service.getRequestCount()==1)&&service.hasRequestToServe("terminateScheduler"))||(service.getRequestCount()==0);
        	if(logger.isDebugEnabled()&&service.getRequestCount()>0)logger.debug("Warning: must check for the case where more than one terminate was submitted"+(i++)+" "+runningTasks.size()+" "+service.getRequestCount()+service.getOldest().getMethodName() +" "+onlyTerminateLeft);
        	
        }while(!(runningTasks.isEmpty()&&onlyTerminateLeft));

        handlePolicyNFinishedTasks();
        

        service.blockingServeOldest("terminateScheduler");
       
    }

    /**
     * use this function to handle the finished tasks and the policy.
     * if hard shutdown intiated, will flush both , other wise , it needs to be implemented
     *
     */
    private void handlePolicyNFinishedTasks() {
		if(hardShutdown)
		{
			this.flushqueue();
			finishedTasks.clear();
		}
		else
		{
			//TODO:in case of soft shutdown, what do u want to do, maybe save the queue and finished task???
			logger.warn("this is a soft shutdown, what do u want to do with the queue and finished tasks??");
		}
	}


	/**
     * does clean up after shceduler shutsdown
     * @param body
     */
  
    /**
     * this function gets tasks it then schedules task on nodes if possible
     * <b>This is now used in pull method, it can be made into push be making Vector<InternalTask> tasks a parameter! </b>
     * @author walzouab
     */
    private void schedule() {
        
    	
    	//TODO:this can be passed as a parameter if policy is in push mode
    	Vector<InternalTask> tasks=policy.getReadyTasks();
    	
        
        ActiveExecuter AE; //to be used as a refernce to active executeras
        Vector<Node> troubledNodes = new Vector<Node>(); //avector of nodes to be freed if they cause trouble
        InternalTask taskRetrivedFromPolicy;
        while(!tasks.isEmpty()) 
        {
        	taskRetrivedFromPolicy=tasks.remove(0);
        	
            try {
                //get executer object
                AE = taskRetrivedFromPolicy.nodeNExecuter.executer;

                //next we will ping the active executer  to make sure it is alive
                AE.ping();
            
                //start the execution
                taskRetrivedFromPolicy.result = AE.start(taskRetrivedFromPolicy.getUserTask());

                // set the time scheudled of the task and change status to running and add it to running tasks pool 
                taskRetrivedFromPolicy.timeScheduled = System.currentTimeMillis();
                taskRetrivedFromPolicy.status = Status.RUNNNING;
               //notice we will reach here only if it was launched sucessfully
                runningTasks.add(taskRetrivedFromPolicy);
                logger.info("Task " +taskRetrivedFromPolicy.getTaskID()+"started execution.");
            } 
            catch (Exception e) 
            {
                try {
                    //try to kill the nodes, there is a high proability this will fail but it doesnt matter since its already troubled and will be returned
                	taskRetrivedFromPolicy.nodeNExecuter.node.killAllActiveObjects();
                } catch (Exception f) { /*the node is already troubled, it its pointless to handle it here */}
                
                troubledNodes.add(taskRetrivedFromPolicy.nodeNExecuter.node);
                taskRetrivedFromPolicy.status=Status.FAILED;
                taskRetrivedFromPolicy.failures++;
                policy.failed(taskRetrivedFromPolicy);
                logger.error("Node " +taskRetrivedFromPolicy.nodeNExecuter.node.getNodeInformation().getURL().toString() +" has problems, will be returned to resource manager, and task will be put at the end of the queue" + e.toString());
            }
        }
     
        if (!troubledNodes.isEmpty()) { //There are troubled nodes to be freed
        	if(logger.isDebugEnabled()){logger.debug("will free from schedule");}
        	freeDeadNodes(troubledNodes);
        }
    }
    
    /**
     * returns a healthy nodeNExecuter back to source for recycling
     * @param healthy nodeNExecuter
     */
    private void freeNodeNExecuter(NodeNExecuter executer)
    {
    	Vector<Node> nodes=new Vector<Node>();
    	nodes.add(executer.node);
    	resourceManager.freeNodes(nodes);
    	
    }
    
    
    /**
     * returns a dead node back to the source for recycling, this is only added for convineince, because RM doesnt accept one node, 
     * so it puts it in a vector and calls on the local free nodes fct
     * @param dead node
     */
    private void freeDeadNode(Node node)
    {
    	Vector<Node> nodesKilled=new Vector<Node>();
    	nodesKilled.add(node);
    	freeDeadNodes(nodesKilled);
    }
    /**
     * returns a vector of dead nodes back to the source for recycling
     * @param dead nodes vector
     */
    private void freeDeadNodes(Vector<Node>deadNodes)
    {
    	resourceManager.freeNodes(deadNodes);
    }

    /**
     * This function manages running tasks and frees unused nodes
     *
     * @author walzouab
     */
    private void manageRunningTasks() {
        //here check for the futures
        for (int i = 0; i < runningTasks.size(); i++) {
            InternalTask task = runningTasks.get(i);
            if (!ProActive.isAwaited(task.result)) {
                runningTasks.removeElementAt(i);
                task.timeFinished = System.currentTimeMillis();

                task.status = Status.FINISHED;
                //error message set to empty to indicate no errors
                task.result.setErrorMessage("");
                policy.finished(task);

                freeNodeNExecuter(task.nodeNExecuter);

                finishedTasks.add(task);
                logger.info("Task "+task.getTaskID() + " has finished");
                if (logger.isDebugEnabled()) {
                    
                    logger.debug("1 node freed after finishing execution");
                }
            }
        }
    }

    /**
     * takes a vector of iternal tasks and inserts them in the policy
     * @param tasks
     */
    public void submit(Vector<InternalTask> tasks) {
        
    	for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).status = Status.QUEUED;
            tasks.get(i).timeInsertedInQueue = System.currentTimeMillis();
            
        }

        policy.insert(tasks);

        
            logger.info(tasks.size() + " tasks added.");
        
    }

    /**
     * checks if a certain task has finished execuution
     * @param taskID
     * @return
     */
    public BooleanWrapper isFinished(String taskID) {
        for (int i = 0; i < finishedTasks.size(); i++)
            if (finishedTasks.get(i).getTaskID().equals(taskID)) {
                return new BooleanWrapper(true);
            }
        return new BooleanWrapper(false);
    }

    public Vector<Info> info_all()
    {
    	Vector<Info> info=new Vector<Info>();
    	
    	//get failed and queued from policy
    	info.addAll(policy.getInfo_all());
    	
    	
    	
    	for (int i=0;i<runningTasks.size();i++)
    	{
    		info.add(runningTasks.get(i).getTaskINFO());
    	}
    	
    	for (int i=0;i<finishedTasks.size();i++)
    	{
    		
    				info.add(finishedTasks.get(i).getTaskINFO());
    		
    			
    	}
    	
    	return info;
    }
    public GenericTypeWrapper<Info> info(String taskID) 
    {
    	InternalTask task=getTask(taskID);
    	if (task!=null)
    	return new GenericTypeWrapper<Info>(task.getTaskINFO());
    	
    	//if it couldnt be found
    	else
    	
    		
    		return new GenericTypeWrapper<Info>(new Info( Status.NEW,  taskID,  "unknown", "unknown", -1, -1,-1, -1,-1));
    		
    	
    	
    	
    	
    }
    /**
     * checks if a certain task is queued
     * @param taskID
     * @return
     */
    public BooleanWrapper isInTheQueue(String taskID)
    {
    	
    	if(policy.getTask(taskID).getObject()!=null)
    		return new BooleanWrapper(true);
    	
    	return new BooleanWrapper(false);
    }
    /**
     * gets a task as long as it exxists in the scheduler 
     * @param taskID
     * @return the task , null if not in the three main queues ie it was either already collected or its is new and wasnt inserted in the scheduler yet
     */
    private InternalTask getTask(String taskID)
    {
    	if(isInTheQueue(taskID).booleanValue())
    	return policy.getTask(taskID).getObject();
    
		 for (int i = 0; i < runningTasks.size(); i++)
	            if (runningTasks.get(i).getTaskID().equals(taskID)) {
	                return runningTasks.get(i);
	            }
		
    	
    	
    		for (int i = 0; i < finishedTasks.size(); i++)
                if (finishedTasks.get(i).getTaskID().equals(taskID)) {
                    return finishedTasks.get(i);
               
    	}
    	
    	
    	
    	
    	return null;//its not queued or runing or finished	
    }
    /**
     * checks if a certain task is running
     * @param taskID
     * @return
     */
    public BooleanWrapper isRunning(String taskID) {
        for (int i = 0; i < runningTasks.size(); i++)
            if (runningTasks.get(i).getTaskID().equals(taskID)) {
                return new BooleanWrapper(true);
            }
        return new BooleanWrapper(false);
    }

    /**
     * This is the function that gets the result.
     * </br><b>WARNING, very important. This function must be filtered by accept request to make sure the result is available before it is served otherwise the scheduler might crash</b>
     * @param taskID
     * @return
     */
    public InternalResult getResult(String taskID,String userName) {
        InternalResult result = null;
        for (int i = 0; i < finishedTasks.size(); i++)
            if (finishedTasks.get(i).getTaskID().equals(taskID)&&finishedTasks.get(i).getUserName().equals(userName)) {
                result = finishedTasks.remove(i).result;
            }

        return result;
    }

    /**
     * This the function that decides wether or not a request can be served
     * </br><b>WARNING, very important. This filter must be used by every single call to any kind of general serve request(ie serve request without mentioned a request name) otherwise the scheduler might crash</b>
     */
    public boolean acceptRequest(Request request) {
        //serves get result only if the result is available
        if (request.getMethodName() == "getResult")
        {
        	
        	String taskID=(String) request.getMethodCall().getParameter(0);
        	String userName=(String) request.getMethodCall().getParameter(1);
        	InternalTask task=this.getTask(taskID);
        	
        	//task doesnt exist while shutting down
        	if(task==null&&shutdown)
        	{
        		createError(taskID,userName,"The Task doesnt exist in the scheduler or hasn't been added to the queue yet or already collected by the user");
        		return true;
        	}
        	//if the username isnt correct 
        	else if(task!=null&&!task.getUserName().equals(userName))
        	{
        		createError(taskID,userName,"The User Name isnt correct.");
        		return true;
        		
        	}
        	//if it is finihsed, regardles of scheduler status , it must be served
        	else if (isFinished(taskID).booleanValue()) {
                return true;
            }
            
            
        	
            
        	//check if the task is queued or failed and shutdown has started
        	else if(task!=null&&task.status==Status.QUEUED&&shutdown)
        	{
        		createError(taskID,userName,"Shcheduler is shutting down, task wont be scheduled");
        		return true;
        	
        	}
            
        	else if(task!=null&&task.status==Status.FAILED&&shutdown)
        	{
        		createError(taskID,userName,"Due to an internal error, task execution failed. However, sheduler is shutting down.task wont be scheduled");
        		return true;
        	
        	}
            
            
            //all other cases dont serve it
        	else return false;
        }
//      terminate scheduler isnt allowed to sericed unless its done explicilty
        else if (request.getMethodName() == "terminateScheduler")
        {        
        	return false;
        }
        //all other functions are to be served
        else return true;
    }

    /**
     * kills all running tasks, and puts them inside the finished tasks queue after setting the internal result to show that it was killed
     * 
     * 
     *
     */
    public void killAllRunning()
    {
    	Vector<Node> nodesKilled=new Vector<Node>();
    	while(!runningTasks.isEmpty())
    	{
    		if(ProActive.isAwaited(runningTasks.get(0).result))
    		{
    			InternalTask toBeKilled=runningTasks.remove(0);
    			nodesKilled.add(toBeKilled.nodeNExecuter.node);
    			toBeKilled.status=Status.KILLED;
    			policy.finished(toBeKilled);
    			
    		
        		toBeKilled.result=new InternalResult();
        		toBeKilled.result.setErrorMessage("Task Was killed before execution finished.");
        		finishedTasks.add(toBeKilled);
    		
    			try
    			{
    		
     				toBeKilled.nodeNExecuter.executer.kill();
     	
    			}
    			//it will always throw an exception because the kill methid kills the jvm, so it is actually normal
    			catch(SendRequestCommunicationException e)
    			{
    				logger.info("Task "+toBeKilled.getTaskID()+"was killed");
    				
    			}
    			catch(Exception e)
    			{
    				e.printStackTrace();
    				logger.info("Something went wrong killing Task "+toBeKilled.getTaskID());
    			}
    			
    			
    		}
    		else//if result is available already just clean up
    		{
    			manageRunningTasks();
    		}
    		
    		
    	}
    	
    	if(!nodesKilled.isEmpty())
			
		{
    		if(logger.isDebugEnabled())
		logger.debug("will frree from kill");
	
		freeDeadNodes(nodesKilled);
		}
    	
    }
    
    /**
     * Asks the scheduler to terminate
     * <b>Warning must be filtered so that it is called only when needed
     * @return true if sucessful, false if asked to terminate while not shutdown
     */
    public BooleanWrapper terminateScheduler()
    {
    	if (shutdown==true)
    		
    	{
    		 try
    	        {
    	        	
    	      
    	        	
    	        	ProActive.getBodyOnThis().terminate();
    	        	logger.info("Scheduler terminated successfully");
    	        }
    	        catch(Exception e)
    	        {
    	        	logger.info("error terminating scheudler"+e.toString());
    	        }
    		
    	        return new BooleanWrapper(true);
    	}
    	
    	else
    		return new BooleanWrapper(false);
    }
    /**
     * if immidiate ,clears queue, kills all runing tasks, reutns nodes to resource manager and terminates
     * if not immediate, stops scheduling and waits for running tasks to finish
     * if shutdown already intiated further calls wont do anything. 
     * 
     * @param immediate-specifies wether or not to do an immediate shutdown
     */
    public void shutdown(BooleanWrapper immediate)
    {
    	if(shutdown==true)return;// shutdown already intiated
    	if(immediate.booleanValue()==true)
    	{
	    	hardShutdown=true;
			this.killAllRunning();
			
	
			
		
			
			logger.info("Immediate ShutDown Intiated");
			
    	}
    	else
    	{
    		logger.info("Soft ShutDown Intiated");
    		hardShutdown=false;
    		
    	}
    	
    	shutdown=true;
    }
    public void flushqueue()
    {
    	policy.flush();
    	logger.info("Policy has been flushed.");
    }
    /**
     * Checks if the all active executers are healthy each timeout. if not, tasks are returned to the policy and nodes are freed.
     * executes when timeout has passed and there are running tasks.
     *
     */
    private void pingAll() {
        if (!runningTasks.isEmpty()&&(System.currentTimeMillis() - previousRunOfPingAll) > TIME_BEFORE_TEST_ALIVE) {
            Vector<Node> troubledNodes = new Vector<Node>();
            int i = 0; //counter
            while (i < runningTasks.size()) {
                try {
                    //ping the executer to make sure its alive
                    runningTasks.get(i).nodeNExecuter.executer.ping();
                    //notice that if there is an error , this part will be skipped 
                    //because the removal of an item decreases the size of the running tasks vector
                    i++;
                } catch (Exception e) {
                    InternalTask failed =runningTasks.remove(i);
                   
                    
                    
                    troubledNodes.add(failed.nodeNExecuter.node);
                	failed.status=Status.FAILED;
                	failed.failures++;
                    policy.failed(failed);
                    logger.error("Task "+failed.getTaskID()+" has failed and will be returned to policy for rescheduling");
                    
                }
            }

            //return troubled nodes
            if(!troubledNodes.isEmpty())
            	{
            	if(logger.isDebugEnabled()){
            		logger.info("will freee from ping");}
            		freeDeadNodes(troubledNodes);
            		if(logger.isDebugEnabled())
                		logger.warn(troubledNodes.size()+" nodes are trobled and were returned to resource manager.");
            	}
            //set the time of this run
            previousRunOfPingAll = System.currentTimeMillis();
        }
    }

    private void createError(String taskID,String userName,String Message)
    {
    	InternalTask error=new InternalTask(null,taskID,userName);
    	error.result=new InternalResult();
    	error.status=Status.ERROR;
		error.result.setErrorMessage(Message);
		finishedTasks.add(error);
    	
    }
	
    /**
     * gets the IDS of queued tasks
     * @return
     */
    public Vector<String>getQueuedID()
    {
    	return policy.getQueuedID();
    }
    /**
     * gets the IDS of failed tasks
     * @return
     */
    public Vector<String>getFailedID()
    {
    	return policy.getFailedID();
    }
    public Vector<String>getRunningID()
    {
    	Vector<String>result=new Vector<String>();
    	for (int i=0;i<runningTasks.size();i++)
    	{
    		result.add(runningTasks.get(i).getTaskID());
    	}
    		return result;
    }
    
    /**
     * get all killed tasks
     * @return
     */
    public Vector<String>getKilledID()
    {
    	Vector<String>result=new Vector<String>();
    	for (int i=0;i<finishedTasks.size();i++)
    	{
    		if(finishedTasks.get(i).status==Status.KILLED)
    			{
    				result.add(finishedTasks.get(i).getTaskID());
    			}
    			
    	}
    		return result;
    }
    
    
    /**
     * get all finished tasks that havent been collect by the user
     * @return
     */
    public Vector<String>getFinishedID()
    {
    	Vector<String>result=new Vector<String>();
    	for (int i=0;i<finishedTasks.size();i++)
    	{
    		if(finishedTasks.get(i).status==Status.FINISHED)
    			{
    				result.add(finishedTasks.get(i).getTaskID());
    			}
    			
    	}
    		return result;
    }
    /**
     * Deletes a specfic task, only if it is either queued or running
     * @param taskID
     * @param username
     * @return true if it is deleted else false
     */
    public BooleanWrapper del(String taskID,String userName)
    {
    	InternalTask  toBeKilled=this.getTask(taskID);
    	
    	//this means it doesnt exist in the scheduler
    	if (toBeKilled==null)
    		return new BooleanWrapper(false);
    	
    	//if its not (the admin or the correct user decline it
    	if (!(toBeKilled.getUserName().equals(userName)||toBeKilled.getUserName().equals("admin")))
    		return new BooleanWrapper(false);
    		
    		
    	//notice that it wont be scheduled when running this fct becasue run activity runs schedule perdiodically after serving requests
    	if (isInTheQueue(taskID).booleanValue())
    	{
    		 toBeKilled=policy.removeTask(taskID).getObject();
    		
    		toBeKilled.status=Status.KILLED;
    		toBeKilled.result=new InternalResult();
    		toBeKilled.result.setErrorMessage("Task has been deleted from the scheduler queue");
    		finishedTasks.add(toBeKilled);
    		
    		return new BooleanWrapper(true);
    	}
    
    	if(isRunning(taskID).booleanValue())
    	{
    		
    		
        	//run trying to locate the task
    		for(int i=0;i<runningTasks.size();i++)
        	{
        		if(runningTasks.get(i).getTaskID().equals(taskID))
        		{
        	
	        		if(ProActive.isAwaited(runningTasks.get(i).result))
	        		{
	        			toBeKilled=runningTasks.remove(i);
	        			
	        			toBeKilled.status=Status.KILLED;
	        			policy.finished(toBeKilled);
	        			
	        			
	            		toBeKilled.result=new InternalResult();
	            		toBeKilled.result.setErrorMessage("Task Was killed before execution finished.");
	            		finishedTasks.add(toBeKilled);
	            		
	  
	        			try
	        			{
	         				toBeKilled.nodeNExecuter.executer.kill();
	        			}
	        			//it will always throw an exception because the kill methid kills the jvm, so it is actually normal
	        			catch(SendRequestCommunicationException e)
	        			{
	        				logger.info("Task "+toBeKilled.getTaskID()+"was killed");
	        				
	        			}
	        			catch(Exception e)
	        			{
	        				e.printStackTrace();
	        				logger.info("Something went wrong killing Task "+toBeKilled.getTaskID());
	        			}
	        		
	                		if(logger.isDebugEnabled())
	            		logger.debug("will frree from kill");
	                	
	                	
	                	
	            		freeDeadNode(toBeKilled.nodeNExecuter.node);
	            		return new BooleanWrapper(true);
	            		
	                
	        			
	        		}
	        		else//if result is available already just clean up
	        		{
	        			manageRunningTasks();
	        			
//	        			it wasnt killed because it became finished
	                	return new BooleanWrapper(false);
	        			
	        		}
        		}
        		
        	}
        	
      
        	
    	}
    	
    	
    	return new BooleanWrapper(false);
    }
    
}
