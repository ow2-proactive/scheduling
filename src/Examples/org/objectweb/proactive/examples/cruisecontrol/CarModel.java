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

/**
 * The class representing the current road
 */
public class CarModel implements org.objectweb.proactive.RunActive {

    /** constants representing the ACTIVE state of the car */
    final static int ACTIVE = 1;

    /** constants representing the INACTIVE state of the car */
    final static int INACTIVE = 0;

    /** the range of time for the physics equation of the car */
    final static double deltaT = 1;

    /** the G acceleration */
    final static double g = 9.8;

    /** the weight of the car */
    final static int m = 1000;

    /** the state of the car depending of the user interaction */
    public int state = INACTIVE;

    /** the current acceleration needed for the speed */
    double acc = 0;

    /** the  current speed of the car */
    double speed = 0;

    /** the current distance */
    double distance = 0;

    /** constants for the */
    private static double coeff = 0.31;

    /** the current incline of the road */
    private double alpha = 0;

    /** Reference onto the dispatcher */
    Interface father;

    /** No arg-constructor */
    public CarModel() {
    }

    /** Initializes the car with a reference to the Interface Dispatcher : father */
    public CarModel(Interface m_father) {
        father = m_father;
    }

    ////////////////////////////////////////////////////////////

    /** Changes the state of the car to ACTIVE */
    public void engineOn() {
        //father.displayMessage("CarModel : EngineOn");
        if (state == INACTIVE) {
            state = ACTIVE;
            //father.displayMessage("Speed Changed "+speed);
        }
    }

    /** Changes the state of the car to INACTIVE
     * and notifies to the Interface to set the father speed to Null
     */
    public void engineOff() {
        state = INACTIVE;
        father.setSpeed(0);
        speed = 0;
        acc = 0;
    }

    ////////////////////////////////////////////////////////////

    /** Sets the new speed of the car */
    public void setSpeed(double m_speed) {
        this.speed = m_speed;
    }

    /** Returns the current speed of the car */
    public Double getSpeed() {
        return new Double(this.speed);
    }

    /////////////////////////////////////////////////////////////

    /**
     * Computes the new speed and the new distance of the car
     */
    public void calculateSpeed(double m_newAcc) {
        if (this.state == ACTIVE) {
            //		this.acc = father.getAcceleration().doubleValue();
            if ((m_newAcc <= 50) && (m_newAcc >= 0)) {
                speed = (speed + (m_newAcc * deltaT)) - (coeff * speed * deltaT) -
                    (Math.sin(alpha) * 4 * g * deltaT);
                if (this.speed < 0.005) {
                    this.speed = 0;
                }
            }
            distance += ((speed * deltaT) / 3600);
            father.setSpeed(speed);
            father.setDistance(distance);
            //father.displayMessage("Speed : "+speed);
            //father.displayMessage("Distance : "+ this.distance);
        }

        //father.displayMessage("CarModel : calculateSpeed : Speed >> "+speed);
        //father.displayMessage("CarModel : calculateSpeed : CURRENT ACCELERATION >> "+ this.acc);
    }

    /////////////////////////////////////////////////////////////////

    /** Increase the acceleration of the car */
    public void incAcceleration(double m_acc) {
        if (this.state == ACTIVE) {
            if (((this.acc < 50) && (m_acc > 0)) || ((this.acc > 0) && (m_acc < 0))) {
                this.acc += m_acc;
            }

            //father.displayMessage("Acceleration : "+this.acc);
        }

        //	else 
        //  father.displayMessage("Engine Off, can not accelerate more");
    }

    /** Applies the new acceleration */
    public void setAcceleration(double m_acc) {
        //father.displayMessage("CarModel : setAcceleration >> M_ACC : "+ m_acc);
        this.acc = m_acc;
        //father.displayMessage("CarModel : setAcceleration >> THIS.ACC : "+ this.acc);
        //father.displayMessage("Engine Accelerating or Decelerating");
    }

    /** Puts the acceleration to null */
    public void brake() {
        acc = 0;
        if (speed > 0.01) {
        } else {
            speed = 0;
        }
    }

    ////////////////////////////////////////////////////////////

    /** sets the new value of the incline */
    public void setAlpha(double m_alpha) {
        alpha = m_alpha;
    }

    /** the current policy of the active object */
    public void runActivity(org.objectweb.proactive.Body body) {
        //father.displayMessage ("Starts live in object ActiveSpeed");
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (body.isActive()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
            if (this.state == ACTIVE) {
                this.calculateSpeed(acc);
            }
            service.flushingServeYoungest("brake");
            service.flushingServeYoungest();
        }
    }
}
