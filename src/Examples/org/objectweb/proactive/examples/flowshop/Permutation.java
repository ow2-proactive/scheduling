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
package org.objectweb.proactive.examples.flowshop;

import java.io.Serializable;
import java.util.Arrays;


/**
 * Contains some tool to manipulate permutation (integer table).
 *
 * @author cdalmass
 *
 */
public class Permutation implements Serializable {

    /**
     * Return the next permutation. Warning, the parmeter are modified.
     *
     * @param perm the permutation we modifie to get the next
     * @return the next permutation if exist, null otherwise
     */
    public static int[] nextPerm(int[] perm) {
        int i = perm.length - 1;
        while ((i > 0) && (perm[i - 1] >= perm[i]))
            i--;
        if (i == 0) {
            // pas de permutation suivante
            return null;
        }
        int m = i - 1;
        int j = perm.length - 1;

        while (perm[m] >= perm[j])
            j--;
        int tmp = perm[m];
        perm[m] = perm[j];
        perm[j] = tmp;
        int k = m + 1;
        int lamb = perm.length - 1;
        while (k < lamb) {
            tmp = perm[k];
            perm[k] = perm[lamb];
            perm[lamb] = tmp;
            k++;
            lamb--;
        }
        return perm;
    }

    /**
     * Jump the n&#33;-th following permutations. That cut an entire branch of the
     * permutation's tree, for example with the jumpPerm( {1 2 3 4}, 3) we
     * return {1 4 3 2}, the first branch are skipped.
     *
     * @param perm the current permutation
     * @param n
     * @return int[]
     */
    public static int[] jumpPerm(int[] perm, int n) {
        int[] tmp = new int[n];
        System.arraycopy(perm, perm.length - n, tmp, 0, n);
        Arrays.sort(tmp); //necessary when we jump an uncomplete branch
        for (int i = 0, srcI = perm.length - 1; i < n; i++, srcI--) {
            perm[srcI] = tmp[i];
        }
        return perm;
    }

    public static int[] iemePerm(int n, int size) {
        if (n > 20) { //21! is upper than Long.MAX_VALUE
            return iemePermBig(n, size);
        }
        int[] set = new int[size];
        for (int i = 0; i < set.length; i++) {
            set[i] = i;
        }

        //
        for (int i = 0; i < set.length; i++) {
        }
        return null;
    }

    private static int[] iemePermBig(int n, int size) {
        // TODO Auto-generated method stub
        return null;
    }

    //test
    public static void main(String[] args) {
        int[] perm1 = new int[] { 3, 2, 1, 0 };
        int[] perm2 = new int[] { 0, 3, 2, 1 };
        System.out.println("inf ~ sup " + Permutation.compareTo(perm1, perm2) + "eq " +
            Permutation.compareTo(perm1, perm1) + "sup ~ inf" + Permutation.compareTo(perm2, perm1));
        if (true) {
            return;
        }

        /*        if (args.length != 1) {
           System.out.println("One args is required : number of jobs");
           return;
           }
           int nbJob = Integer.parseInt(args[0]);
         */
        int nbJob = 4;

        // compute fact(nbJob) 
        int factNbJob = 1;
        for (int i = nbJob; i > 1; i--)
            factNbJob *= i;
        System.out.println("We compute the " + factNbJob + " permutations ...");

        //init
        int[] jobs = new int[nbJob];

        for (int i = 0; i < nbJob; i++)
            jobs[i] = i + 1;

        int[] currentPerm = jobs;
        int nbPerm = 0;
        jumpPerm(currentPerm, 3);
        do {
            nbPerm++;
            System.out.print("Perm " + nbPerm + ": ");
            for (int i = 0; i < nbJob; i++)
                System.out.print(currentPerm[i]);
            System.out.println();
        } while ((nextPerm(currentPerm)) != null);
        System.out.println("We computed " + nbPerm + " permutations of the " + factNbJob + " expected!");
    }

    public static void print(int[] perm) {
        System.out.print(Permutation.string(perm));
    }

    public static String string(int[] perm) {
        String str = "";
        if (perm == null) {
            return "Perm null";
        }
        for (int i = 0; i < perm.length; i++)
            str += (perm[i] + " ");
        return str;
    }

    public static int compareTo(int[] perm1, int[] perm2) {
        if (perm1.length != perm2.length) {
            throw new IllegalArgumentException("Permutation's size not equal");
        }
        for (int i = 0; i < perm1.length; i++) {
            if (perm1[i] < perm2[i]) {
                return -1;
            } else if (perm1[i] > perm2[i]) {
                return 1;
            }
        }
        return 0;
    }
}
