package modelisation.simulator.mixed.mixedwithcalendar;

import modelisation.simulator.common.SimulatorElement;


public class Event {
    protected double time;
    protected SimulatorElement object;
    protected String description;

    public Event(double time, SimulatorElement o) {
        this(time, o, null);
    }

    public Event(double time, SimulatorElement o, String description) {
        this.time = time;
        this.object = o;
        this.description = description;
    }

    public double getTime() {
        return this.time;
    }

    public SimulatorElement getObject() {
        return this.object;
    }

    public boolean equals(Event e) {
        return ((this.time == e.getTime()) &&
        (this.object.equals(e.getObject())));
    }

    public String toString() {
        if (description != null) {
            return this.description;
        } else {
            return super.toString();
        }
    }
}
