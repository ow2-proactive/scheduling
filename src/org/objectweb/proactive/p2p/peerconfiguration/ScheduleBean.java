package org.objectweb.proactive.p2p.peerconfiguration;
import java.util.*;

/*
 * Created on Jan 21, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author jbustos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ScheduleBean {
private double maxLoad;
private int days;
private int startTime;
private int workTime;
private GregorianCalendar begin;
private GregorianCalendar end;

public ScheduleBean() {
	maxLoad=0;
	days = 0;
	startTime = 0;
	workTime=0;
	begin = null;
	end = null;
}

/******************** SETTERS  ***************************/

public void setMaxLoad(double maxLoad) {
	this.maxLoad = maxLoad;
}

public void setDays (int days) {
	this.days = days;
}

public void setStartTime (int startTime) {
	this.startTime = startTime;
}

public void setWorkTime (int workTime) {
	this.workTime = workTime;
}

public void setBegin(GregorianCalendar begin) {
	this.begin = begin;
}

public void setEnd(GregorianCalendar end) {
	this.end=end;
}
/******************** GETTERS *****************************/


public double getMaxLoad() {
	return maxLoad;
}

public int getDays() {
	return days;
}

public int getStartTime() {
	return startTime;
}

public int getWorkTime() {
	return workTime;
}

public GregorianCalendar getBegin() {
	return begin;
}

public GregorianCalendar getEnd() {
	return end;
}

/********************* other methods ************************/

/* The days are scheduled in a long string "Monday, Fryday, etc"
 * This method traduce that string to an internal representation (bit = 1 => valid day)
 */
 
public int Days2Byte(String strDays) {
	strDays = strDays.trim();
	
	int internal = 0;
	String days[] = strDays.split(" ");
	
	String[] week = {"Sunday","Monday", "Tuesday","Wednesday", "Thursday","Friday", "Saturday"};
	int j=days.length-1;
	for (int i=week.length-1; i >=0 ; i--) {
		internal = internal << 1;
		if  (j >=0  && week[i].equals(days[j])) {
			internal++;
			j--;
		}
	}
	
	return internal;
}

/* 
 * This method return true if, by calendar, the load balancer has to work today
 */
 
public boolean WorkToday() {
	Calendar today= Calendar.getInstance();
	
	if (begin==null && end == null) return true;
	 
	if (today.after(begin) && today.before(end)) { 
		today.setFirstDayOfWeek(Calendar.SUNDAY);
		int weekday = (int) Math.round(Math.pow(2,today.get(Calendar.DAY_OF_WEEK)-1));
		return (days & weekday) != 0;
	}
	return false;
}

}
