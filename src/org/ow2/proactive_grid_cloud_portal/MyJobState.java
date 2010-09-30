package org.ow2.proactive_grid_cloud_portal;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.api.PAFuture;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;

@XmlRootElement
public class MyJobState {

    private JobState js;
    
    public MyJobState(){}
    
    public MyJobState(JobState js) {
           this.js = js;
           
    }
    
    public long getFinishedTime(){
        return js.getFinishedTime();
    }
    
    public String getDescription()
    {
       return  js.getDescription();
    }
    
    
    public JobId getId()
    {
        return js.getId();
    }
    
    public Map<String,String> getGenericInformations() {
        Map<String,String> gen = js.getGenericInformations();
        if (gen != null) {
           gen = new HashMap<String,String>(js.getGenericInformations());
        }
        
        return gen;
    }
    
    public int getNumberOfFinishedTasks() 
    {
       return  js.getJobInfo().getNumberOfFinishedTasks();
    }
    
    public int getNumberOfPendingTasks()
    {
       return  js.getJobInfo().getNumberOfPendingTasks();
    }
    
    public int getNumberOfRunningTasks()
    {
       return  js.getJobInfo().getNumberOfRunningTasks();
    }
    
    public JobStatus getStatus()
    {
        return PAFuture.getFutureValue(js.getJobInfo().getStatus());
    }
    
    public long getStartTime() {
        return js.getStartTime();
    }
    
    public int getTotalNumberOfTasks()
    {
        return js.getJobInfo().getTotalNumberOfTasks();
    }
    
    public long getSubmittedTime()
    {
        return js.getSubmittedTime();
    }
    
    public JobPriority getPriority()
    {
        return js.getPriority();
    }
}


