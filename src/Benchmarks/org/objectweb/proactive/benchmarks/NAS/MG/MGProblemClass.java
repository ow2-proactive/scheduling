package org.objectweb.proactive.benchmarks.NAS.MG;

/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2006 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 * 
 * ################################################################
 */
import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;


/**
 * Kernel MG
 *
 * A simplified multi-grid kernel. It requires highly structured long
 * distance communication and tests both short and long distance data
 * communication.
 * It approximates a solution to the discrete Poisson problem.
 */
public class MGProblemClass extends NASProblemClass {

    public int np;
    public int maxLevel;
    public int nm2;

    public int niter;
    public int nxSz, nySz, nzSz;
    public int[] nx; // 1
    public int[] ny; // 2
    public int[] nz; // 3
    public int lt, lm;
    public int dim, ndim1, ndim2, ndim3;
    public int nm, nv, nr;
}
