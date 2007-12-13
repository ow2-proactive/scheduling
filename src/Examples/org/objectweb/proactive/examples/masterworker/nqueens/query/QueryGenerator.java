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
package org.objectweb.proactive.examples.masterworker.nqueens.query;

import java.util.Vector;

import org.apache.log4j.Logger;


public class QueryGenerator {
    protected static Logger logger = Logger.getLogger(QueryGenerator.class.getName());

    public static void main(String[] args) {
        Vector<Query> v = new Vector<Query>();
        v.add(parseArgs(args));

        Vector all = split(3, v);

        long r = 0;
        int n = all.size();
        for (int i = 0; i < n; i++) {
            r += ((Query) all.get(i)).run();
        }
        System.out.println("" + r);
    }

    public static Vector<Query> generateQueries(String[] args) {
        Vector<Query> v = new Vector<Query>();
        v.add(parseArgs(args));

        return split(3, v);
    }

    /**
     * choose what kind of computation to use according to parameters
     * @param args 0 : nb queens, 1 : first queen line position
     * @return
     */
    static Query parseArgs(String[] args) {
        int nq = Integer.parseInt(args[0]);
        int size = args.length - 1;

        // Fast version
        if (size == 0) {
            return (new FirstOutQuery(nq));
        }
        int q0 = Integer.parseInt(args[1]);

        // Diag version
        if ((size == 1) && (q0 == 0)) {
            return (new FirstDiagQuery(nq, 2));
        }

        // Generic version
        return (new BaseQuery(args));
    }

    public static Vector<Query> generateQueries(int nbQueens, int profondeur) {
        Vector<Query> v = new Vector<Query>();
        v.add(new FirstOutQuery(nbQueens));
        return split(profondeur, v);
    }

    public static Vector generateQueries(int nbQueens, int queenLine, int profondeur) {
        Vector v = new Vector();
        if (queenLine == 0) {
            v.add(new FirstDiagQuery(nbQueens, 2));
        } else {
            v.add(new BaseQuery(nbQueens, queenLine));
        }
        return split(profondeur, v);
    }

    public static Vector<Query> split(int k, Vector v) {
        if (k == 0) {
            return (v);
        }
        Vector<Query> r = new Vector<Query>();
        int n = v.size();
        for (int i = 0; i < n; i++) {
            r = ((Query) v.get(i)).split(r);
        }
        return (split(k - 1, r));
    }

    public static void getStat(Vector queryList) {
        int nbBaseQuery = 0;
        int nbDiagQuery = 0;
        int nbFirstDiagQuery = 0;
        int nbFirstOutQuery = 0;
        int nbOutQuery = 0;

        int i = 0;
        int lg = queryList.size();
        Query q;

        while (i < lg) {
            q = (Query) queryList.get(i++);
            if (q instanceof FirstOutQuery) {
                nbFirstDiagQuery++;
            } else if (q instanceof BaseQuery) {
                nbBaseQuery++;
            } else if (q instanceof DiagQuery) {
                nbDiagQuery++;
            } else if (q instanceof FirstDiagQuery) {
                nbFirstDiagQuery++;
            } else if (q instanceof OutQuery) {
                nbOutQuery++;
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("nbBaseQuery = " + nbBaseQuery);
            logger.info("nbDiagQuery = " + nbDiagQuery);
            logger.info("nbOutQuery = " + nbOutQuery);
            logger.info("nbFirstOutQuery = " + nbFirstOutQuery);
            logger.info("nbFirstDiagQuery = " + nbFirstDiagQuery);
            logger.info("Total Query = " + lg);
        }
    }

    public static Vector splitAQuery(Query nextQuery) {
        Vector res = new Vector(10);

        // check pour le taskId
        res = nextQuery.split(res);

        return res;
    }
}
