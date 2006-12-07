package org.objectweb.proactive.ic2d.dgc.views;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.gef.EditPartFactory;
import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.dgc.data.ObjectGraph;
import org.objectweb.proactive.ic2d.dgc.editparts.DgcIC2DEditPartFactory;
import org.objectweb.proactive.ic2d.monitoring.data.AOObject;
import org.objectweb.proactive.ic2d.monitoring.views.MonitoringView;


public class DgcView extends MonitoringView implements Runnable {
    public static final String ID = "org.objectweb.proactive.ic2d.dgc.views.DgcView";

    public DgcView() {
        new Thread(this, "DgcView Updater").start();
    }

    protected EditPartFactory getEditPartFactory() {
        return new DgcIC2DEditPartFactory(this);
    }

    private void drawGraph(Map<AOObject, Collection<AOObject>> graph) {
        Set<Map.Entry<AOObject, Collection<AOObject>>> s = graph.entrySet();
        for (Map.Entry<AOObject, Collection<AOObject>> e : s) {
            AOObject ao = e.getKey();
            ao.resetCommunications();
        }
        for (Map.Entry<AOObject, Collection<AOObject>> e : s) {
            AOObject srcAO = e.getKey();
            for (AOObject destAO : e.getValue()) {
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
            Map<AOObject, Collection<AOObject>> graph = ObjectGraph.getObjectGraph(this.getWorld());
            drawGraph(graph);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
