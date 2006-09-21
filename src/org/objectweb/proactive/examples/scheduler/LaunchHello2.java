package org.objectweb.proactive.examples.scheduler;

import org.objectweb.proactive.scheduler.Scheduler;


public class LaunchHello2 {
    public static void main(String[] args) throws Exception {
        String schedulerURL = args[0];
        Scheduler scheduler = Scheduler.connectTo(schedulerURL);
        String XML_LOCATION_UNIX = LaunchHello2.class.getResource(
                "/org/objectweb/proactive/examples/scheduler/job_template2.xml")
                                                     .getPath();
        scheduler.fetchJobDescription(XML_LOCATION_UNIX);
    }
}
