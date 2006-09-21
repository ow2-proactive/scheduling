package org.objectweb.proactive.examples.scheduler;

import org.objectweb.proactive.scheduler.Scheduler;


public class LaunchHello {
    public static void main(String[] args) throws Exception {
        String schedulerURL = args[0];
        Scheduler scheduler = Scheduler.connectTo(schedulerURL);
        String XML_LOCATION_UNIX = LaunchHello.class.getResource(
                "/org/objectweb/proactive/examples/scheduler/job_template.xml")
                                                    .getPath();
        scheduler.fetchJobDescription(XML_LOCATION_UNIX);
    }
}
