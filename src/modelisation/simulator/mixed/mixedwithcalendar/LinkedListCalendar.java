package modelisation.simulator.mixed.mixedwithcalendar;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;


public class LinkedListCalendar implements Calendar {

  static DecimalFormat df ;
  static { 
  	df = (DecimalFormat) NumberFormat.getInstance(Locale.US);
  	df.applyPattern("###.00");	
  }

    protected LinkedList list;

    public LinkedListCalendar() {
        list = new LinkedList();
    }

    public void addEvent(double time, Object object) {
    }

    public void addEvent(Event e) {
        ListIterator li = list.listIterator();
        double eTime = e.getTime();
        Event tmp = null;
        while (li.hasNext()) {
            tmp = (Event)li.next();
            if (tmp.getTime() > e.getTime()) {

                int index = li.nextIndex() - 1;
                list.add(index, e);
                return;
            }
        }
        list.addLast(e);
    }

    public boolean removeEvent(Event e) {
//    	System.out.println("removing);
        return this.list.remove(e);
    }

    public Event getNextEvent() {
        return null;
    }

    public Event[] removeNextEvents() {

        //just because java is stupid
        Event[] events = new Event[1];
        ArrayList al = new ArrayList();
        Event first = (Event)list.removeFirst();
        if (first == null) {
            return null;
        }
        al.add(first);

        ListIterator li = list.listIterator();
        Event tmp = null;
        while (li.hasNext()) {
            tmp = (Event)li.next();
            if (tmp.getTime() == first.getTime()) {
            	li.remove();
                al.add(tmp);
            } else {
                return (Event[])al.toArray(events);
            }
        }
        return (Event[])al.toArray(events);
        // return (Event)list.removeFirst();
        //   return null;
    }

    public String toString() {

        StringBuffer buff = new StringBuffer();
        ListIterator li = list.listIterator();
        Event tmp = null;
        while (li.hasNext()) {
            tmp = (Event)li.next();
            buff.append("(").append(tmp.getObject().getName()).append(",").append(df.format(tmp.getTime())).append(",").append(tmp.toString()).append(")");
            //
        }
        return buff.toString();
    }

    public static void main(String[] arguments) {

        LinkedListCalendar lc = new LinkedListCalendar();
        lc.addEvent(new Event(100, new Forwarder()));
        System.out.println(lc);
        lc.addEvent(new Event(200, new Forwarder()));
        System.out.println(lc);
        lc.addEvent(new Event(20, new Forwarder()));
        System.out.println(lc);
        lc.addEvent(new Event(150, new Forwarder()));
        System.out.println(lc);
        lc.removeNextEvents();
        System.out.println(lc);
        lc.removeNextEvents();
        System.out.println(lc);
        lc.removeNextEvents();
        System.out.println(lc);
    }
}