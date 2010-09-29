package org.ow2.proactive_grid_cloud_portal;

import javax.xml.bind.annotation.XmlRootElement;

import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobState;

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
    
    

}


