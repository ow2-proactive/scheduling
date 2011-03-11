package org.ow2.proactive_grid_cloud_portal;

import org.ow2.proactive.resourcemanager.common.event.RMEvent;
import org.ow2.proactive.resourcemanager.common.event.RMEventType;
import org.ow2.proactive.resourcemanager.common.event.RMInitialState;
import org.ow2.proactive.resourcemanager.common.event.RMNodeEvent;
import org.ow2.proactive.resourcemanager.common.event.RMNodeSourceEvent;
import org.ow2.proactive.resourcemanager.frontend.RMEventListener;
import org.ow2.proactive.resourcemanager.frontend.RMMonitoringImpl;

public class CachingRMEventListener implements RMEventListener {

    protected RMMonitoringImpl rmMonitoring;
    protected RMInitialState rmInitialState;
    protected RMEventType RMstate;


    public void rmEvent(RMEvent event) {
        switch (event.getEventType()) {
            case ALIVE:
                RMstate = RMEventType.ALIVE;
                break;
            case SHUTTING_DOWN:
                RMstate = RMEventType.SHUTTING_DOWN;
                break;
            case SHUTDOWN:
                RMstate = RMEventType.SHUTDOWN;
                break;
        }

    }

    public void nodeSourceEvent(RMNodeSourceEvent event) {
       switch (event.getEventType()) {
           case NODESOURCE_CREATED:
               rmInitialState.getNodeSource().add(event);
               break;
           case NODESOURCE_REMOVED:
               for (int i = 0 ; i < rmInitialState.getNodeSource().size(); i++) {
                   if (rmInitialState.getNodeSource().get(i).getSourceName().equals(event.getSourceName())) {
                       rmInitialState.getNodeSource().remove(i);
                       break;
                   }
               }
               break;
       }

    }

    public void nodeEvent(RMNodeEvent event) {
        switch (event.getEventType()) {
            case NODE_REMOVED:
                for (int i = 0; i < rmInitialState.getNodesEvents().size(); i++) {
                    if (event.getNodeUrl().equals(rmInitialState.getNodesEvents().get(i))) {
                        rmInitialState.getNodesEvents().remove(i);
                        break;
                    }
                }
                break;
            case NODE_ADDED:
                rmInitialState.getNodesEvents().add(event);
                break;
            case NODE_STATE_CHANGED:
                for (int i = 0; i < rmInitialState.getNodesEvents().size(); i++) {
                    if (event.getNodeUrl().equals(rmInitialState.getNodesEvents().get(i))) {
                        rmInitialState.getNodesEvents().set(i,event);
                        break;
                    }
                }
                break;


        }

      }



}
