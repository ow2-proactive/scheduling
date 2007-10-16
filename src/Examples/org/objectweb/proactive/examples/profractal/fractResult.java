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
package org.objectweb.proactive.examples.profractal;


/* fractResult.java
 *
 * Created on Semptember 6 2003, 11.56
 */

/**
 * @author Daniele Di Felice
 */
import java.io.Serializable;


/**
 * This class represent the computation's results and it's needed by the
 * ProActive model, because te boolean[][] is final
 */
public class fractResult implements Serializable {
    private boolean[][] result;

    /** Empty constructor */
    public fractResult() {
    }

    /** Constructor
     * @param results: a bi-dimensional array of boolean values
     * @return an instance of the fractResult class
     */
    public fractResult(boolean[][] result) {
        this.result = result;
    }

    /** Sets the results array
     * @param results: a bi-dimensional array of boolean values
     * @return void
     */
    public void setResult(boolean[][] result) {
        this.result = result;
    }

    /** Gets the results matrix
     * @param void
     * @return results: the results matrix
     */
    public boolean[][] getResults() {
        return (this.result);
    }

    /** Gets the result at the row i and column j
     * @param void
     * @return result[i][j]: the result at the row i and column j
     */
    public boolean getResult(int i, int j) {
        return (result[i][j]);
    }
}
