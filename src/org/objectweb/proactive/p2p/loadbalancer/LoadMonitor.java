/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *									Javier Bustos
 * ################################################################
 */

package org.objectweb.proactive.p2p.loadbalancer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Random;

import org.apache.log4j.Logger;

public class LoadMonitor extends Thread
{
	private static Logger logger = Logger.getLogger(LoadBalancer.class.getName());

    private double load,oldTotal,oldUsed;;
    private LoadBalancer lb;
    private Random r;
    private int secLoad[];
   private String name;
    private double ranking;
    private RandomAccessFile statfile;
    
    public synchronized double getRanking()
    {
    return ranking;	
    }

    public LoadMonitor(LoadBalancer lb)
    {
        this.lb = lb;
        load = 0;
        r = new Random();

		BufferedReader br;
		ranking = 1.0;
		String line=null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/cpuinfo")));
			statfile = new RandomAccessFile("/proc/stat","r");
			while ((line = br.readLine()) != null) {
				if (line.startsWith("bogomips")) {
						String splited[] = line.split(":");
						double bogomips = Double.parseDouble(splited[1]);   // obtaining the bogomips
						double bmbase10 = Math.log(bogomips)/Math.log(10)-3;  // 3 is our zero reference
						ranking = bmbase10+0.7;   // 1+atan(bmbase10)/1.3 => [0; oo)
						}
				}
		statfile.seek(5);
		line = statfile.readLine();

		long user, nice, system, idle;
        java.util.StringTokenizer st = new java.util.StringTokenizer(line," ");
        user   = Long.parseLong(st.nextToken());
        nice   = Long.parseLong(st.nextToken());
        system = Long.parseLong(st.nextToken());
        idle   = Long.parseLong(st.nextToken());
		
        oldTotal  = user + nice + system + idle;
        oldUsed   = user + nice + system;
        
        load = 1;
        calculateLoad();
		} catch (FileNotFoundException e) {
		} catch (NumberFormatException e) {
		} catch (IOException e) {
		}
		
    }
    public synchronized double  getLoad() {
            long user, nice, system, idle;
            double totalTime, realUsedTime;
            String cpuLine=null;
            try {
				// skip "cpu"
				statfile.seek(5);
				cpuLine = statfile.readLine();
			} catch (IOException e) {
				return 1;
			}
            // read "cpu x x x x"           
			
            java.util.StringTokenizer st = new java.util.StringTokenizer(cpuLine," ");
            user   = Long.parseLong(st.nextToken());
            nice   = Long.parseLong(st.nextToken());
            system = Long.parseLong(st.nextToken());
            idle   = Long.parseLong(st.nextToken());

            // compute load
            totalTime  = user + nice + system + idle - oldTotal;
            realUsedTime   = user + nice + system - oldUsed;
            if (totalTime < 1 ) totalTime = realUsedTime;
            double thisLoad = realUsedTime/(totalTime+1);
            oldTotal = user + nice + system + idle;
            oldUsed = user + nice + system;
            return thisLoad;
            
    }
    
	private synchronized void calculateLoad() {
		double newload = getLoad();
		
		newload = (0.7*newload + 0.3*load);
		load = newload;
	}

    public void run()
    {
        int i = 0;
        do
        {
        	calculateLoad();
            lb.register(Math.round(load*100));
            try
            {
            	double sl, slreal;
            	slreal=0;
            	if (load == 0) sl = 10000;
            	else {
            		sl  = 30000 + Math.abs(r.nextGaussian())*15000;
//            		slreal = sl/1000;
//            		sl = sl / Math.log(1+load*100);
            	}
                Thread.sleep(Math.round(sl));
            }
            catch(InterruptedException interruptedexception) { }
        } while(true);
    }

}
