package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.Polyline;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.RelativeBendpoint;
import org.eclipse.draw2d.ConnectionRouter.NullConnectionRouter;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.Anchor;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.CommunicationFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.Position;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


public class CommunicationEditPart extends AbstractConnectionEditPart implements Observer {
    private boolean active = true;
    private static final Color DEFAULT_ARROW_COLOR = new Color(Display.getCurrent(), 108, 108, 116);

    public static final DrawingStyle DEFAULT_STYLE = DrawingStyle.FIXED;
    private static final int MAX_STROKE_WIDTH_RATIO = 12;
    private static final int MAX_STROKE_WIDTH_PROPORTIONAL = 80;
    private float communicationCounter = 1;
    private static float maxCommunicationCounter = 1;
    public static DrawingStyle drawingStyle = DEFAULT_STYLE;;

    public CommunicationEditPart(Communication model) {

        this.setModel(model);
        model.addObserver(this);
    }

    public void update(Observable o, Object arg) {
        //   	 System.out.println("CommunicationEditPart.update()");

        if ((arg != null) && (arg instanceof MVCNotification)) {
            final MVCNotificationTag mvcNotif = ((MVCNotification) arg).getMVCNotification();
            final Object notificationdata = ((MVCNotification) arg).getData();

            // State updated
            switch (mvcNotif) {

                //This message is sent by Communication Object when a communication has been received.
                //Right now the feature has been disabled
                //See Communication class
                case ACTIVE_OBJECT_ADD_COMMUNICATION: {
                    this.communicationCounter = ((Communication) this.getModel()).getnumberOfCalls();
                    ((CommunicationFigure) this.getFigure()).setLineWidth(drawingStyleSize());
                    System.out.println("CommunicationEditPart.update()->add com " + communicationCounter);
                }
            }
        }
    }

    protected IFigure createFigure() {

        //PolylineConnection connection = (PolylineConnection) super.createFigure();

        PolylineConnection connection = new CommunicationFigure();

        connection.setTargetDecoration(new PolygonDecoration());
        connection.setForegroundColor(DEFAULT_ARROW_COLOR);

        //Polyline connection = (Polyline) new RoundedLineConnection(DEFAULT_ARROW_COLOR);
        return connection;
    }

    @Override
    protected void createEditPolicies() {

        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
    }

    @Override
    protected ConnectionAnchor getTargetConnectionAnchor() {
        return super.getTargetConnectionAnchor();
    }

    @Override
    protected ConnectionAnchor getSourceConnectionAnchor() {
        return super.getSourceConnectionAnchor();
    }

    public void refresh() {
        //this.communicationCounter=((Communication)this.getModel()).getnumberOfCalls();

        //System.out.println("CommunicationEditPart.refresh()"+communicationCounter);

        //((CommunicationFigure)this.getFigure()).setLineWidth(drawingStyleSize());

        //	if ((getSourceConnectionAnchor().getOwner()==null)|| (getTargetConnectionAnchor().getOwner())==null)
        //	 {
        //		// System.out.println("...................CommunicationEditPart: bug here .........");
        //	     this.deactivateFigure();
        //	     active=false;
        //	     return;     	 
        //	 }
        //	
        //	
        //	if ((!active) && (getSourceConnectionAnchor().getOwner()!=null) && (getTargetConnectionAnchor().getOwner())!=null)
        //	 {
        //		// System.out.println("...................CommunicationEditPart: bug here .........");
        //	     this.activateFigure();
        //	     active=true;
        //	     return;     	 
        //	 }

        if (active && ((this.getSource() == null) || (this.getTarget() == null))) {
            active = false;
            this.deactivateFigure();

        } else if (!active && (this.getSource() != null) && (this.getTarget() != null)) {
            active = true;
            this.activateFigure();
        }

        //	 RoundedLineConnection connection = (RoundedLineConnection)this.getFigure();
        //	 if ((connection.getConnectionRouter() instanceof NullConnectionRouter) &&((this.getSource()!=null) && (this.getTarget()!=null)))
        //	 {
        //		   AOFigure source = ((AOEditPart)this.getSource()).getCastedFigure();
        //		   AOFigure target = ((AOEditPart)this.getTarget()).getCastedFigure();
        //		   RoundedLineConnection c= 		   ((RoundedLineConnection)this.getFigure());
        //		//   AOConnection.addRouterToConnection(c, source, target);
        //
        //		   
        //		   
        //	 
        //	 }
        //	 
        super.refresh();
    }

    protected int drawingStyleSize() {
        switch (drawingStyle) {
            case FIXED:
                return 1;
            case PROPORTIONAL:
                if (maxCommunicationCounter > MAX_STROKE_WIDTH_PROPORTIONAL) {
                    return (int) (((MAX_STROKE_WIDTH_PROPORTIONAL / maxCommunicationCounter) * communicationCounter) + 1);
                } else {
                    return (int) (communicationCounter + 1);
                }
            case RATIO:
                return (int) (((MAX_STROKE_WIDTH_RATIO / maxCommunicationCounter) * communicationCounter) + 1);
        }
        return 1;
    }

    public enum DrawingStyle {
        PROPORTIONAL, RATIO, FIXED;
    }

    public static void setDrawingStyle(DrawingStyle newDrawingStyle) {
        drawingStyle = newDrawingStyle;
    }

}
