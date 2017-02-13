/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.frontend.topology.clustering;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.Topology;
import org.ow2.proactive.topology.descriptor.BestProximityDescriptor;
import org.ow2.proactive.topology.descriptor.DistanceFunction;


/**
 * Class which holds the tests that read their input from a a visual description of a grid (a string)
 * @author Zsolt Istvan
 *
 */
public class MatrixBasedTests {

    protected static HashMap<Node, HashMap<Node, Long>> distances = new HashMap<>();

    protected static DummyNode[][][] gridMatrix;

    protected static int gridWidth;

    protected static int gridHeight;

    private class LocalTopology implements Topology {

        private static final long serialVersionUID = 32L;

        public Long getDistance(Node node, Node node2) {
            Long distance = null;
            if (distances.get(node) != null && distances.get(node).get(node2) != null) {
                distance = distances.get(node).get(node2);
            }
            if (distances.get(node2) != null && distances.get(node2).get(node) != null) {
                distance = distances.get(node2).get(node);
            }
            return distance;
        }

        public Long getDistance(InetAddress hostAddress, InetAddress hostAddress2) {
            return null;
        }

        public Long getDistance(String hostName, String hostName2) {
            return null;
        }

        public HashMap<InetAddress, Long> getHostTopology(InetAddress hostAddress) {
            return null;
        }

        public Set<InetAddress> getHosts() {
            return null;
        }

        public boolean knownHost(InetAddress hostAddress) {
            return false;
        }

        public boolean onSameHost(Node node, Node node2) {
            return false;
        }

        public List<Cluster<String>> clusterize(int numberOfClusters, DistanceFunction distanceFunction) {
            return null;
        }
    }

    /**
     * This method takes the grid description and the pivot description, populates the
     * distances map, and returns a list of the pivot nodes.
     * @param width width of the host grid
     * @param height height of the host grid
     * @param matrix string representation of this grid (has to have width*height elements). <br> '.' - empty place, 1-9 a host with X nodes
     * @param pivotMatrix same as before but this is the string describing the pivot
     */
    protected static List<Node> initDistances(int width, int height, String matrix, String pivotMatrix) {
        distances = new HashMap<>();
        gridMatrix = new DummyNode[width][height][9];
        gridWidth = width;
        gridHeight = height;
        List<Node> pivots = null;

        //create nodes in the matrix
        // the matrix has to be constructed as follows:
        //  '.' or '0' - no host in that position
        //  '1'-'9' - host exists, and this is the number of nodes on it
        if (matrix.length() == width * height) {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    if (matrix.charAt(j * width + i) != '.') {
                        int cnt = Integer.parseInt("" + matrix.charAt(j * width + i));
                        for (int c = 0; c < cnt; c++) {
                            gridMatrix[i][j][c] = new DummyNode("N_" + i + "_" + j + "_" + c);
                            distances.put(gridMatrix[i][j][c], new HashMap<Node, Long>());
                        }
                    }
                }
            }

            //pivot matrix.
            // similar to the one before, but the number shows
            // how many nodes we add to the pivot from a given host
            if (pivotMatrix != null) {
                pivots = new ArrayList<>();
                for (int j = 0; j < height; j++) {
                    for (int i = 0; i < width; i++) {
                        if (pivotMatrix.charAt(j * width + i) != '.') {
                            int cnt = Integer.parseInt("" + pivotMatrix.charAt(j * width + i));
                            pivots.addAll(Arrays.asList(gridMatrix[i][j]).subList(0, cnt));
                        }
                    }
                }
            }

            //calculate distances in a monster loop
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++)
                    for (int x = 0; x < 9; x++)
                        if (gridMatrix[i][j][x] != null) {
                            for (int l = 0; l < height; l++) {
                                for (int k = 0; k < width; k++) {

                                    // distance function is a taxi-cab function multiplied by 100
                                    long dist = (((long) (Math.abs(i - k) + Math.abs((j - l)))) * 100);

                                    for (int q = 0; q < 9; q++)
                                        if (gridMatrix[k][l][q] != null && (i != k || j != l || q != x)) {
                                            distances.get(gridMatrix[i][j][x]).put(gridMatrix[k][l][q], dist);
                                            distances.get(gridMatrix[k][l][q]).put(gridMatrix[i][j][x], dist);
                                        }
                                }
                            }
                        }
            }
        }
        return pivots;
    }

    /**
     * Takes the list of selected nodes, and outputs a string describing the result matrix.
     */
    protected static String processSelected(List<Node> sels) {
        Integer[][] selMat = new Integer[gridWidth][gridHeight];
        for (Node i : sels) {
            String[] nif = i.toString().split("_");
            int x = Integer.parseInt(nif[1]);
            int y = Integer.parseInt(nif[2]);
            if (selMat[x][y] == null)
                selMat[x][y] = 0;
            selMat[x][y]++;
        }
        String out = "";
        for (int j = 0; j < gridHeight; j++) {
            for (int i = 0; i < gridWidth; i++) {
                if (gridMatrix[i][j] != null) {
                    if (selMat[i][j] != null && selMat[i][j] > 0) {
                        out += (selMat[i][j]);
                    } else {
                        out += (".");
                    }
                } else {
                    out += ("?");
                }
            }
        }
        return out;
    }

    /**
     * Performs a test run
     * @param width width of the host grid
     * @param height height of the host grid
     * @param matrix string representation of this grid (has to have width*height elements). <br> '.' - empty place, 1-9 a host with X nodes
     * @param pivotMatrix same as before but this is the string describing the pivot
     * @param cliqueSize needed nodes
     * @return string of the selection's matrix
     */
    public String doHACTest(int width, int height, String matrix, String pivotMatrix, Long threshold, int cliqueSize) {

        List<Node> pivots = initDistances(width, height, matrix, pivotMatrix);

        HAC cfPivot = new HAC(new LocalTopology(), pivots, BestProximityDescriptor.AVG, threshold);
        List<Node> cliqueRes = cfPivot.select(cliqueSize, new LinkedList<>(distances.keySet()));

        int resultSize = cliqueRes.size();
        boolean didFail = cliqueSize != resultSize;

        if (pivots != null)
            cliqueRes.addAll(pivots);
        String out = processSelected(cliqueRes);

        return didFail ? null : out;
    }

    @org.junit.Test
    public void chooseClosestTest() {
        Long infinity = (long) 99999;
        int W = 10;
        int H = 5;
        String prob = "2.2......." + "2.2......." + ".........." + ".........." + "..........";

        String pivot = "0.0......." + "1.0......." + ".........." + ".........." + "..........";

        String result1 = ".........." + "2........." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivot, infinity, 1).equals(result1));

        String result3 = "2........." + "2........." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivot, infinity, 3).equals(result3));

        assertTrue("The algorithm did not fail when it had to!", doHACTest(W, H, prob, pivot, (long) 100, 4) == null);

        String probMany = "1.1.1.1.1." + "1.1.9.1.1." + ".........." + ".........." + "..........";

        String noPivot = ".........." + ".........." + ".........." + ".........." + "..........";

        String resultMany = ".........." + "....9....." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, probMany, noPivot, infinity, 9).equals(resultMany));

        String probCorners = "9........." + ".........." + ".........." + ".........1" + "9.......1.";

        String bipartPivot = "9........." + ".........." + ".........." + ".........." + "........1.";

        String resultCorners = "9........." + ".........." + ".........." + ".........." + "1.......1.";

        assertTrue("The algorithm did not deal well with far away pivots!",
                   doHACTest(W, H, probCorners, bipartPivot, infinity, 1).equals(resultCorners));
    }

    @org.junit.Test
    public void consequencyTest() {
        Long infinity = (long) 99999;
        int W = 10;
        int H = 5;
        String prob = "22........" + "22........" + ".........." + ".........." + "..........";

        String pivotSW = "00........" + "10........" + ".........." + ".........." + "..........";

        String resultSW = ".........." + "2........." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivotSW, infinity, 1).equals(resultSW));

        String pivotNW = "10........" + "00........" + ".........." + ".........." + "..........";

        String resultNW = "2........." + ".........." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivotNW, infinity, 1).equals(resultNW));

        String pivotNE = "01........" + "00........" + ".........." + ".........." + "..........";

        String resultNE = ".2........" + ".........." + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivotNE, infinity, 1).equals(resultNE));

        String pivotSE = "00........" + "01........" + ".........." + ".........." + "..........";

        String resultSE = ".........." + ".2........" + ".........." + ".........." + "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                   doHACTest(W, H, prob, pivotSE, infinity, 1).equals(resultSE));
    }

    @org.junit.Test(timeout = 60000)
    public void reasonableTimeTestNoPivot() {
        int w = 20;
        int h = 20;
        double density = 0.30;

        //made with w=20; h=20; density=0.3
        String map = ".2..16............8." + "...7...7.963.....6.7" + "....4...87.7...6..1." + ".9....677.1.75...9.." +
                     "...4.....64.2.....7." + "5........9....4.2..2" + ".................6.." + "........5....6.8...." +
                     "...6....4.........23" + ".3...8.9.......9...." + "...8..9..........5.3" + "..6......7295....1.." +
                     "9.8..4.29..55......." + "..6..7.......2......" + ".4..8...74.....5...." + ".2.7.6.....7.7....8." +
                     "1...637.....71..5..." + ".1...9.......6....38" + "9.............9.914." + "....7..95.3...3..89.";

        int onePercent = (int) Math.round((w * h * density * 5) / 100);

        doHACTest(w, h, map, null, (long) 99999, onePercent);
        System.out.println("1");
        doHACTest(w, h, map, null, (long) 99999, onePercent * 5);
        System.out.println("2");
        doHACTest(w, h, map, null, (long) 99999, onePercent * 10);
        System.out.println("3");

    }

    @org.junit.Test(timeout = 20000)
    public void reasonableTimeTestWithPivot() {
        int w = 20;
        int h = 20;
        double density = 0.30;

        //made with w=20; h=20; density=0.3
        String map = ".2..16............8." + "...7...7.963.....6.7" + "....4...87.7...6..1." + ".9....677.1.75...9.." +
                     "...4.....64.2.....7." + "5........9....4.2..2" + ".................6.." + "........5....6.8...." +
                     "...6....4.........23" + ".3...8.9.......9...." + "...8..9..........5.3" + "..6......7295....1.." +
                     "9.8..4.29..55......." + "..6..7.......2......" + ".4..8...74.....5...." + ".2.7.6.....7.7....8." +
                     "1...637.....71..5..." + ".1...9.......6....38" + "9.............9.914." + "....7..95.3...3..89.";

        //this was made by putting a pivot on every
        // 5 node host
        String pivot = ".0..00............0." + "...0...0.000.....0.0" + "....0...00.0...0..0." +
                       ".0....000.0.01...0.." + "...0.....00.0.....0." + "1........0....0.0..0" +
                       ".................0.." + "........1....0.0...." + "...0....0.........00" +
                       ".0...0.0.......0...." + "...0..0..........1.0" + "..0......0001....0.." +
                       "0.0..0.00..11......." + "..0..0.......0......" + ".0..0...00.....1...." +
                       ".0.0.0.....0.0....0." + "0...000.....00..1..." + ".0...0.......0....00" +
                       "0.............0.000." + "....0..01.0...0..00.";

        int onePercent = (int) Math.round((w * h * density * 5) / 100);

        doHACTest(w, h, map, pivot, (long) 99999, onePercent);
        doHACTest(w, h, map, pivot, (long) 99999, onePercent * 5);
        doHACTest(w, h, map, pivot, (long) 99999, onePercent * 10);

    }
}
