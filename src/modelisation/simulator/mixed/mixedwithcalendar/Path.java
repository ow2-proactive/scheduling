package modelisation.simulator.mixed.mixedwithcalendar;

import java.util.ArrayList;
import java.util.Iterator;

import modelisation.simulator.common.Averagator;

import org.apache.log4j.Logger;


public class Path {
    
    static Logger logger = Logger.getLogger(Path.class.getName());
    
    protected ArrayList path;
    protected Averagator averagator;

    public Path() {
        this.path = new ArrayList();
        this.averagator = new Averagator();
    }

    public void add(State s) {
        this.path.add(s);
    }

    public void addTime(double time) {
        this.averagator.add(time);
    }

    public State get(int i) {
        return (State)path.get(i);
    }

    public State getLast() {
        return (State)path.get(path.size() - 1);
    }

    public void clear() {
        this.path.clear();
    }

    public void end() {
        if (logger.isInfoEnabled()) {
            logger.info(this +  " time = " +this.averagator.average()
            + " count = " + this.averagator.getCount());            
        }
    }

    public String toString() {
        StringBuffer tmp = new StringBuffer();
        for (Iterator iter = path.iterator(); iter.hasNext();) {
            tmp.append("(").append(((State)iter.next()).toString()).append(")");
        }
        return tmp.toString();
    }

    public Object[] toArray() {
        return path.toArray();
    }

    public boolean equals(Object o) {
        Object[] first = this.toArray();
        Object[] second = ((Path)o).toArray();
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) {
            if (!((State)first[i]).equals(second[i])) {
                return false;
            }
        }
        return true;
    }
}