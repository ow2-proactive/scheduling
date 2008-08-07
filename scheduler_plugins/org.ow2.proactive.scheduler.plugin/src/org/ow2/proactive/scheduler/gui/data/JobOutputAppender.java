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
package org.ow2.proactive.scheduler.gui.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.ow2.proactive.scheduler.gui.views.JobOutput;


/**
 * A job output appender
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 0.9
 */
public class JobOutputAppender extends AppenderSkeleton {

    /**
     * Period in ms of the event buffer flushing.
     */
    public static final long FLUSH_PERIOD = 1000;

    /**
     * Size of the event buffer.
     */
    public static final int BUFFER_SIZE = 128;

    // event buffer
    private List<LoggingEvent> eventBuffer = new ArrayList<LoggingEvent>(BUFFER_SIZE);
    // Thread for periodic flushing of the event buffer
    private Timer flusher = new Timer("Job output event flusher");
    // indicate if a flush is scheduled in the flusher
    private boolean flushScheduled = false;

    private JobOutput jobOutput = null;

    // -------------------------------------------------------------------- //
    // --------------------------- constructor ---------------------------- //
    // -------------------------------------------------------------------- //
    /**
     * The default constructor
     *
     * @param jobOutput the job output
     */
    public JobOutputAppender(JobOutput jobOutput) {
        this.jobOutput = jobOutput;
    }

    // -------------------------------------------------------------------- //
    // ------------------------------ public ------------------------------ //
    // -------------------------------------------------------------------- //
    /**
     * To obtains the job output
     *
     * @return the job output
     */
    public JobOutput getJobOutput() {
        return jobOutput;
    }

    // -------------------------------------------------------------------- //
    // -------------------- extends AppenderSkeleton ---------------------- //
    // -------------------------------------------------------------------- //
    /**
     * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void append(LoggingEvent event) {
        this.eventBuffer.add(event);
        if (this.eventBuffer.size() > BUFFER_SIZE) {
            // additionnal flush to avoid buffer overflow
            this.flusher.schedule(new FlusherTask(), 0);
            this.flushScheduled = true;
            this.flushBuffer();
        } else if (!flushScheduled) {
            this.flusher.schedule(new FlusherTask(), FLUSH_PERIOD);
            this.flushScheduled = true;
        }
    }

    /**
     * Flush the event buffer onto the console
     */
    private synchronized void flushBuffer() {
        StringBuffer msg = new StringBuffer();
        for (LoggingEvent e : this.eventBuffer) {
            //    		if (e.getLevel().equals(Level.INFO)){
            msg.append(this.layout != null ? this.layout.format(e) : e.getRenderedMessage());
            //	  		}else{
            //	  			msg.append(ErrBegin);
            //	  			msg.append(this.layout!=null?this.layout.format(e):e.getRenderedMessage());
            //	  			msg.append(ErrEnd);
            //	  		}
        }
        //    	printWithLevel(msg);
        jobOutput.info(msg.toString()); // only INFO for now...
        this.eventBuffer.clear();
        this.flushScheduled = false;
    }

    /**
     * Define a simple task that flush the event buffer 
     * @see flushBuffer()
     */
    private class FlusherTask extends TimerTask {
        @Override
        public void run() {
            JobOutputAppender.this.flushBuffer();
        }
    }

    //    public static final String ErrBegin = "^$B|";
    //    public static final String ErrEnd = "^$E|";
    //    public static final int TagLength = 4;    
    //    private void printWithLevel(StringBuffer msg){
    //    	int parseIndex = 0;
    //    	int tagIndex;
    //    	while ((tagIndex=msg.indexOf(ErrBegin,parseIndex))>=0){
    //    		// parse for sdterr
    //    		jobOutput.info(msg.substring(parseIndex,tagIndex));
    //    		int end = msg.indexOf(ErrEnd,tagIndex+TagLength);
    //    		jobOutput.error(msg.substring(tagIndex+TagLength,end));
    //    		parseIndex = end+TagLength;
    //    	}
    //    	// last stdout if any
    //    	jobOutput.info(msg.substring(parseIndex,msg.length()));
    //    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#close()
     */
    @Override
    public void close() {
        this.flusher.cancel();
    }

    /**
     * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}
