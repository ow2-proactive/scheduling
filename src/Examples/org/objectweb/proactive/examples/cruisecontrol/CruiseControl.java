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


/** The active object for the controller,
 * checks the speed of the car when it is on
 */
public class CruiseControl implements org.objectweb.proactive.RunActive {

    /** */
    final static int ACTIVE = 1;

    /** */
    final static int INACTIVE = 0;

    /** State of the controller */
    int state = INACTIVE;

    /** The desired speed of the controller */
    double desiredSpeed = 0;

    /** The current acceleration to applies to the car
     * when the controller is on
     */
    double acc = 0;

    /** Reference onto the dispatcher of the Applet */
    Interface father;

    /** No-arg constructor for tha ProActive */
    public CruiseControl() {
    }

    /** Constructor which intializes the father field */
    public CruiseControl(Interface m_father) {
        father = m_father;
    }

    /** Tuns on the controller, sets the new desired speed and sets the acceleration to the current acceleration of the car */
    public void controlOn(double m_desiredSpeed, double m_acc) {
        //System.out.println("control On : Cruise Control : "+ this.state);
        if (this.state == INACTIVE) {
            desiredSpeed = m_desiredSpeed;
            this.acc = m_acc;
            father.setDesiredSpeed(desiredSpeed);
            //System.out.println("control On : Cruise Control : Father "+father.acc);
            //System.out.println("control On : Cruise Control"+acc);
            this.state = ACTIVE;
        }
    }

    /** Turns off the controller,
     * sets the desired speed to null
     * and calls the dispatcher to repaint the cruisePane */
    public void controlOff() {
        if (state == ACTIVE) {
            state = INACTIVE;
            desiredSpeed = 0;
            father.setDesiredSpeed(0);
            father.brake();
        }
    }

    ////////////////////////////////////////////////////////////

    /*** Turn off the controller when the car is stopping */
    public void engineOff() {
        state = INACTIVE;
        desiredSpeed = 0;
        acc = 0;
        father.setDesiredSpeed(0);
    }

    //////////////////////////////////////////////////////////////

    /** Asks the dispatcher the current speed of the car */
    public double getDesiredSpeed() {
        return desiredSpeed;
    }

    /** Changes the desired speed to the new one */
    public void setDesiredSpeed(double m_speed) {
        if (this.state == ACTIVE) {
            this.desiredSpeed = m_speed;
        }
    }

    //////////////////////////////////////////////////////////////

    /** Increases the desired speed with one
     * and asks the dispatcher to display the new value
     */
    public void accelerate() {
        if (this.state == ACTIVE) {
            this.desiredSpeed += 1;
            father.setDesiredSpeed(this.desiredSpeed);
        }
    }

    /** Decreases the desired speed with one
     * and asks the dispatcher to display the new value
     */
    public void decelerate() {
        if (this.state == ACTIVE) {
            this.desiredSpeed -= 1;
            father.setDesiredSpeed(this.desiredSpeed);
        }
    }

    ///////////////////////////////////////////////////////////////

    /** Computes the new acceleration to apply to the car
     * when the controller is on
     */
    public void calculateAcceleration() {
        double currentSpeed;
        currentSpeed = father.getSpeed().doubleValue();
        //System.out.println("Cruise Control : calculateAcceleration >>"+ currentSpeed);
        //System.out.println("Cruise Control : calculateAcceleration >>"+ desiredSpeed);
        //System.out.println("Cruise Control : calculateAcceleration : State >>"+ this.state);
        double delta = Math.abs(currentSpeed - desiredSpeed);
        if (this.state == ACTIVE) {
            if (delta < 0.4) {
                ;
            } else if (currentSpeed < desiredSpeed) {
                if (delta < 2) {
                    this.acc += 0.5;
                } else if (delta < 8) {
                    this.acc += 1;
                } else {
                    this.acc += 2.5;
                }

                //System.out.println("CruiseControl : calculateAcceleration >> this.acc : "+ this.acc);
                father.setAcceleration(this.acc);
            } else {
                if (delta < 2) {
                    this.acc -= 0.5;
                } else if (delta < 8) {
                    this.acc -= 1;
                } else {
                    this.acc -= 1.8;
                }
                if (this.acc < 0) {
                    father.setAcceleration(0);
                    this.acc = 0;
                    this.state = INACTIVE;
                    father.deactiveControl();
                    //System.out.println("Cruise Control Off : Speed too high >>>");
                } else {
                    father.setAcceleration(this.acc);
                }
            }
        }
    }

    /** Sets the car acceleration to the new one */
    public void setAcceleration(double m_acc) {
        if (this.state == ACTIVE) {
            father.setAcceleration(m_acc);
        }
    }

    /** the routine Live which runs only the most recent call */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);

        //System.out.println ("Starts live in object ActiveCruise");
        while (body.isActive()) {
            try {
                Thread.sleep(1800);
                //System.out.println("CruiseControl : Method live >>"+desiredSpeed);
                // serve Requests before calculating Acceleration
                if (this.state == ACTIVE) {
                    this.calculateAcceleration();
                }

                //System.out.println("CruiseControl : Cruise Control Live Routine : End");
                service.flushingServeYoungest();
            } catch (InterruptedException e) {
            }
        }
    }
}
// The END
