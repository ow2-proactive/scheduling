/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Set;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.State;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.AOListener;


public class AOEditPart extends AbstractMonitoringEditPart {
    private Integer length;

    /*private Set<IFigure> figuresToUpdate;
    private Set<GraphicalCommunication> communicationsToDraw;

    private boolean shouldRepaint = true;*/
    private AOFigure castedFigure;
    private ActiveObject castedModel;
    private NodeEditPart castedParentEditPart;
    private NodeFigure castedParentFigure;
    private IFigure globalPanel;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public AOEditPart(ActiveObject model) {
        super(model);

        /*this.figuresToUpdate = Collections.synchronizedSet(new HashSet());
        this.communicationsToDraw = Collections.synchronizedSet(new HashSet());

        new Thread(){
                public void run(){
                        while(shouldRepaint){
                                try {
                                        Thread.sleep(100);
                                } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                }

                                Display.getDefault().asyncExec(new Runnable() {
                                        public void run () {
                                                for (GraphicalCommunication communication : communicationsToDraw) {
                                                        communication.draw();
                                                }
                                                communicationsToDraw.clear();

                                                for (IFigure figure : figuresToUpdate) {
                                                        figure.repaint();
                                                }
                                                figuresToUpdate.clear();
                                        }
                                });
                        }
                }
        }.start();*/
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * This method is called whenever the observed object is changed. It calls
     * the method <code>refresh()</code>.
     *
     * @param o
     *            the observable object (instance of AbstractDataObject).
     * @param arg
     *            an argument passed to the notifyObservers method.
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            // State updated
            if (arg instanceof State) {
                final State state = (State) arg;

                //final IFigure panel = getWorldEditPart().getFigure().getParent();
                if (state == State.NOT_MONITORED) {
                    getViewer().getControl().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                getCastedFigure()
                                    .removeConnections(getGlobalPanel());
                            }
                        });
                    /*this.figuresToUpdate.add(getGlobalPanel());*/ addFigureToUpdtate(getGlobalPanel());
                } else {
                    getViewer().getControl().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                getCastedFigure().setState(state);
                                getCastedFigure().refresh();
                            }
                        });
                    /*this.figuresToUpdate.add(getCastedFigure());*/ addFigureToUpdtate(getCastedFigure());
                }

                /*Display.getDefault().asyncExec(new Runnable() {
                        public void run () {
                                if(state == State.NOT_MONITORED) {
                                        getCastedFigure().removeConnections(panel);
                                }
                                else{
                                        getCastedFigure().setState(state);
                                }
                        }
                });*/
            }
            // Add communication
            else if (arg instanceof HashSet) {
                final Set<ActiveObject> communications = (HashSet<ActiveObject>) arg;

                final AOFigure destination = getCastedFigure();

                final IFigure panel = getGlobalPanel();

                if (communications.isEmpty()) {
                    getViewer().getControl().getDisplay().syncExec(new Runnable() {
                            public void run() {
                                destination.removeConnections(panel);
                            }
                        });
                }

                for (ActiveObject aoSource : communications) {
                    AbstractMonitoringEditPart editPart = AbstractMonitoringEditPart.registry.get(aoSource);
                    if (editPart != null) {
                        AOFigure source = (AOFigure) editPart.getFigure();
                        if (source != null) {
                            /*communicationsToDraw.add(*/ addGraphicalCommunication(new GraphicalCommunication(
                                    source, destination, panel, getArrowColor()));
                        } else {
                            System.out.println(
                                "[Error] Unable to find the source");
                        }
                    }
                }

                /*this.figuresToUpdate.add(panel);*/ addFigureToUpdtate(panel);

                /*
                Display.getDefault().asyncExec(new Runnable() {
                        public void run () {
                                if (communications.isEmpty())
                                        destination.removeConnections(panel);

                                for (Iterator<ActiveObject> it = communications.iterator(); it.hasNext(); )
                                {
                                        ActiveObject  aoSource = it.next();
                                        AbstractMonitoringEditPart editPart = AbstractMonitoringEditPart.registry.get(aoSource);

                                        if(editPart!=null){
                                                AOFigure source = (AOFigure) editPart.getFigure();
                                                if(source!=null){
                                                        source.addConnection(destination, panel, getArrowColor());
                                                }
                                                else
                                                        System.out.println("[Error] Unable to find the source");
                                        }
                                        else{
                                                System.out.println("[Error] Unable to draw the arrow : "+aoSource.getName()+" -->  "+getCastedModel().getName());
                                        }
                                }
                        }});*/
            }
            // Request queue length has changed
            else if (arg instanceof Integer) {
                length = (Integer) arg;

                getCastedFigure().setRequestQueueLength(length);
                /*this.figuresToUpdate.add(getCastedFigure());*/ addFigureToUpdtate(getCastedFigure());

                /*Display.getDefault().asyncExec(new Runnable() {
                        public void run () {
                                getCastedFigure().setRequestQueueLength(length);
                        }
                });*/
            }
        } else {
            super.update(o, arg);
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated with the type of model object the EditPart
     * is associated with. So here, it returns a new NodeFigure.
     * @return a new NodeFigure view associated with the NodeObject model.
     */
    protected IFigure createFigure() {
        AOFigure figure = new AOFigure(getCastedModel().getName() /*getFullName()*/);
        AOListener listener = new AOListener(getCastedModel(), figure,
                getMonitoringView(), getCastedParentFigure());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    protected Color getArrowColor() {
        return new Color(Display.getCurrent(), 108, 108, 116);
    }

    /**
     * Returns a List containing the children model objects.
     * @return the List of children
     */
    protected List<AbstractData> getModelChildren() {
        return getCastedModel().getMonitoredChildrenAsList();
    }

    @Override
    protected void createEditPolicies() { /* Do nothing */
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Convert the result of EditPart.getModel()
     * to AOObject (the real type of the model).
     * @return the casted model
     */
    @SuppressWarnings("unchecked")
    @Override
    public ActiveObject getCastedModel() {
        if (castedModel == null) {
            castedModel = (ActiveObject) getModel();
        }
        return castedModel;
    }

    /**
     * Convert the result of EditPart.getFigure()
     * to AOFigure (the real type of the figure).
     * @return the casted figure
     */
    @SuppressWarnings("unchecked")
    @Override
    public AOFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (AOFigure) getFigure();
        }
        return castedFigure;
    }

    private NodeEditPart getCastedParentEditPart() {
        if (castedParentEditPart == null) {
            castedParentEditPart = (NodeEditPart) getParent();
        }
        return castedParentEditPart;
    }

    private NodeFigure getCastedParentFigure() {
        if (castedParentFigure == null) {
            castedParentFigure = (NodeFigure) getCastedParentEditPart()
                                                  .getFigure();
        }
        return castedParentFigure;
    }

    private IFigure getGlobalPanel() {
        if (globalPanel == null) {
            globalPanel = getWorldEditPart().getFigure().getParent();
        }
        return globalPanel;
    }

    @Override
    public void deactivate() {
        getCastedModel().resetCommunications();
        super.deactivate();
    }
}
