package modelisation.simulator.mixed.mixedwithcalendar;

public interface Calendar {
    public void addEvent(double time, Object object);

    public void addEvent(Event e);

    public Event getNextEvent();

    public Event[] removeNextEvents();
	public boolean removeEvent(Event event);

}