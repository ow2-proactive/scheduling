/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.examples.cruisecontrol;


/** This class is the main dispatcher for the active objects.
 * it acts as link between the distributed active objects
 * and the Applet.
 * The interface is turn active by the ProActive primitives turnActive.
 * All the communications between active objects, between an active object and the Applet
 * pass in transit through it
 *
 */
public class Interface {
    // Fields 

    /**
     * the active state of the car when the controller is off
     */
    final static int ACTIVE = 1;

    /** the off state of the car when the engine is off */
    final static int INACTIVE = 0;

    /**
     * the active state of the car when the Cruise Control is on
     */
    final static int CRUISING = 2;

    /** the state of the car */
    int state = INACTIVE;

    /** the desired speed of the controller */
    double desiredSpeed = 0;

    /** the acceleration to apply to the car when the cruise control is off */
    double acc = 0;

    /** Used to brake */
    double brake = 0;

    /** the incline of the road */
    double alpha = 0;

    /** the active object Car */
    CarModel activeSpeed = null;

    /** the active object cruise control */
    CruiseControl activeCruise = null;

    ///** The active object road */
    //public RoadModel road = null;

    /** The applet of the interface which is necessary for painting purposes */
    CruiseControlApplet applet;

    /** A no-arg construtor for ProActive pruposes */
    public Interface() {
    }

    /** constructor wich initializes the applet field */
    public Interface(CruiseControlApplet m_applet) {
        this.applet = m_applet;
    }

    /** Method which creates the differents active objects */
    public void initialize() {
        Object[] arg;
        arg = new Object[1];
        arg[0] = org.objectweb.proactive.api.ProActiveObject.getStubOnThis();

        //System.out.println("initialize Method");
        try {
            activeSpeed = (CarModel) org.objectweb.proactive.api.ProActiveObject.newActive(CarModel.class.getName(),
                    arg);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        arg[0] = org.objectweb.proactive.api.ProActiveObject.getStubOnThis();

        try {
            activeCruise = (CruiseControl) org.objectweb.proactive.api.ProActiveObject.newActive(CruiseControl.class.getName(),
                    arg);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        /**        try
         * {
         *road = (RoadModel)org.objectweb.proactive.ProActive.newActiveObject ("org.objectweb.cruisecontrol.RoadModel", arg, null);
         *
         *  }
         *catch (org.objectweb.proactive.ActiveObjectCreationException e)
         * {
         *e.printStackTrace();
         *System.exit(0);
         *   }
         */
    }

    ///////////////////////////////////////////////////////////////////
    public void displayMessage(String msg) {
        applet.receiveMessage(msg);
    }

    /** Turns on the active objects activeSpeed and the road */
    public void engineOn() {
        //	//System.out.println("Speed Changed");
        //System.out.println("EngineOn : Interface : STATE >> "+ state);
        if (this.state == INACTIVE) {
            this.state = ACTIVE;
            //System.out.println("Interface : EngineOn : STATE after >> "+ state);
            activeSpeed.engineOn();
            applet.engineOn();
        }
    }

    /** Turns off the active objects activeSpeed, activeCruise, calls the applet method engineOff */
    public void engineOff() {
        activeSpeed.engineOff();
        activeCruise.engineOff();

        this.state = INACTIVE;
        this.acc = 0;
        this.desiredSpeed = 0;
        setAcceleration(0);
        applet.engineOff();

        //System.out.println("Speed Null");
    }

    ///////////////////////////////////////////////////////////////////

    /** Actives the controller,
     * get the current speed from the object activeSpeed
     */
    public void controlOn() {
        if (this.state == ACTIVE) {
            this.state = CRUISING;
            desiredSpeed = getSpeed().doubleValue();
            //System.out.println("control On : Interface");
            // Acceleration Null from the driver
            //System.out.println("control On : Interface : Interface Speed"+acc);
            activeCruise.controlOn(desiredSpeed, this.acc);
            //System.out.println("Cruise Acc : "+activeCruise.acc);
            //System.out.println("state :"+activeCruise.state);
        }
    }

    /** Deactives the controller, set the current acceleration to null,
     * asks the applet to repaint the controller
     */
    public void controlOff() {
        if (this.state == CRUISING) {
            this.state = ACTIVE;
            acc = 0;
            setAcceleration(0);

            activeCruise.controlOff();
        }
    }

    ///////////////////////////////////////////////////////////////

    /** Deactives the controller when the speed is too low to be held
     * asks the applet to repaint the controller
     */
    public void deactiveControl() {
        if (this.state == CRUISING) {
            this.state = ACTIVE;
            acc = 0;
            activeSpeed.brake();
            applet.controlPaneOff();
        }
    }

    ///////////////////////////////////////////////////////////////////    

    /** When the user pressed the "+" button, increases the current acceleration with one,
     * changes the current acceleration of the car
     * and asks the applet to repaint the controller
     */
    public void accelerate() {
        if (this.state == ACTIVE) {
            if (this.acc < 50) {
                this.acc += 1;
                //System.out.println("Interface : Accelerate >> THIS.ACC :  "+this.acc);
                activeSpeed.setAcceleration(this.acc);
                applet.setAcceleration(this.acc);
            }
        }
    }

    /** When the user pressed the "-" button, decreases the current acceleration with one,
     * changes the current acceleration of the car
     * and asks the applet to repaint the controller
     */
    public void decelerate() {
        if (this.state == ACTIVE) {
            if (this.acc > 0) {
                this.acc -= 1;
                activeSpeed.setAcceleration(this.acc);
                applet.setAcceleration(this.acc);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /** When the user pressed the "++" button, increases the current desired speed with one,
     * changes the current desired speed of the controller
     * changes the desired speed of the applet
     */
    public void accelerateCruise() {
        if (this.state == CRUISING) {
            desiredSpeed += 1;
            activeCruise.setDesiredSpeed(desiredSpeed);
            applet.setDesiredSpeed(desiredSpeed);
            //System.out.println("desiredSpeed Interface");
        }
    }

    /** When the user pressed the "--" button, decreases the current desired speed with one,
     * changes the current desired speed of the controller
     * changes the desired speed of the applet
     */
    public void decelerateCruise() {
        if (this.state == CRUISING) {
            desiredSpeed -= 1;
            activeCruise.setDesiredSpeed(desiredSpeed);
            applet.setDesiredSpeed(desiredSpeed);
            //System.out.println("desiredSpeed Interface");
        }
    }

    ///////////////////////////////////////////////////////////////////////

    /**
     * Increases the current value of the incline road
     */
    public void incAlpha() {
        //System.out.println("incAlpha : Interface");
        //System.out.println("incAlpha : Interface : state >> "+ this.state);
        if ((this.state == ACTIVE) || (this.state == CRUISING)) {
            //System.out.println("incAlpha : Interface");
            if (alpha < 0.6) {
                alpha += 0.09;
            } else {
                alpha = 0.6;
            }
            setAlpha(alpha);
            //System.out.println("Interface : IncAlpha : Alpha >> "+ alpha);
        }
    }

    /**
     * Decreases the current value of the incline road
     */
    public void decAlpha() {
        if ((this.state == ACTIVE) || (this.state == CRUISING)) {
            if (alpha > -0.6) {
                alpha -= 0.09;
            } else {
                alpha = -0.6;
            }
            setAlpha(alpha);
            //System.out.println("Interface : IncAlpha : Alpha >> "+ alpha);
        }
    }

    ////////////////////////////////////////////////////////////////////////

    /** Sets the current acceleration to null
     * sets the controlller off if it is active
     * calls the activeSpeed method brake
     * asks the applet to repaint it
     */
    public void brake() {
        //System.out.println("Interface : Brake : state >> "+ this.state);
        if ((this.state == ACTIVE) || (this.state == CRUISING)) {
            activeCruise.controlOff();
            activeSpeed.brake();
            applet.brake();
            acc = 0;
            this.state = ACTIVE;
        }
    }

    /** Changes the speed that the applet displays */
    public void setSpeed(double speed) {
        applet.setSpeed(speed);
    }

    /** Changes the distance that the applet displays */
    public void setDistance(double distance) {
        applet.setDistance(distance);
    }

    /** Changes the applet desired Speed */
    public void setDesiredSpeed(double m_speed) {
        applet.setDesiredSpeed(m_speed);
        //System.out.println("desiredSpeed Interface");
    }

    /** Gets the current speed of the active object activeSpeed */
    public Double getSpeed() {
        //System.out.println("Interface : getSpeed Interface");
        return activeSpeed.getSpeed();
    }

    /** Changes  the acceleration of the active object activeSpeed
     * and asks the applet to repaint it
     */
    public void setAcceleration(double m_acc) {
        //System.out.println("Interface : setAcceleration : M_ACC >> "+ m_acc);
        this.acc = m_acc;
        activeSpeed.setAcceleration(m_acc);
        applet.setAcceleration(m_acc);
    }

    /** Returns the current acceleration of the user */
    public Double getAcceleration() {
        //System.out.println("Interface : getAcceleration : THIS.ACC >> "+ acc);
        return new Double(this.acc);
    }

    ///////////////////////////////////////////////////////////////

    /** Changes the incline of the car : active Speed
     * and asks the applet to display the new value
     */
    public void setAlpha(double m_alpha) {
        if ((this.state == ACTIVE) || (this.state == CRUISING)) {
            activeSpeed.setAlpha(m_alpha);
            applet.setAlpha(m_alpha);
            //System.out.println("Interface : setAlpha >> "+ m_alpha);
        }
    }
}
