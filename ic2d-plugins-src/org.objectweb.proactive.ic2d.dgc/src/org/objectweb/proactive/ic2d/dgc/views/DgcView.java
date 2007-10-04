package org.objectweb.proactive.ic2d.dgc.views;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.EditPartFactory;
import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.dgc.data.ObjectGraph;
import org.objectweb.proactive.ic2d.dgc.editparts.DgcIC2DEditPartFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class DgcView extends MonitoringView implements Runnable {
    public static final String ID = "org.objectweb.proactive.ic2d.dgc.views.DgcView";

    public DgcView() {
        new Thread(this, "DgcView Updater").start();
    }

    protected EditPartFactory getEditPartFactory() {
        return new DgcIC2DEditPartFactory(this);
    }

    private void drawGraph(Map<ActiveObject, Collection<ActiveObject>> graph) {
        Set<Map.Entry<ActiveObject, Collection<ActiveObject>>> s = graph.entrySet();
        for (Map.Entry<ActiveObject, Collection<ActiveObject>> e : s) {
            ActiveObject ao = e.getKey();
            ao.resetCommunications();
        }
        for (Map.Entry<ActiveObject, Collection<ActiveObject>> e : s) {
            ActiveObject srcAO = e.getKey();
            for (ActiveObject destAO : e.getValue()) {
                srcAO.addCommunication(destAO);
            }
        }
    }

    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        this.setPartName("DGC View");
    }

    public void run() {
        for (;;) {
            Map<ActiveObject, Collection<ActiveObject>> graph = ObjectGraph.getObjectGraph(this.getWorld());
            drawGraph(graph);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
