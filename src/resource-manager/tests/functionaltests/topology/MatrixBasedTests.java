package functionaltests.topology;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.ow2.proactive.resourcemanager.frontend.topology.BestProximityDescriptor;
import org.ow2.proactive.resourcemanager.frontend.topology.DistanceFunction;
import org.ow2.proactive.resourcemanager.selection.topology.clustering.HAC;


/**
 * Class which holds the tests that read their input from a a visual description of a grid (a string)
 * @author Zsolt Istvan
 *
 */
public class MatrixBasedTests {

    protected static HashMap<Node, HashMap<Node, Long>> distances = new HashMap<Node, HashMap<Node, Long>>();
    protected static DummyNode[][][] gridMatrix;
    protected static int gridWidth;
    protected static int gridHeight;

    protected static Long getTestDistances(Node node, Node node2) {
        Long distance = null;
        if (distances.get(node) != null && distances.get(node).get(node2) != null) {
            distance = distances.get(node).get(node2);
        }
        if (distances.get(node2) != null && distances.get(node2).get(node) != null) {
            distance = distances.get(node2).get(node);
        }
        return distance;
    }

    private static class LocalHAC extends HAC {

        public LocalHAC(List<Node> pivot, DistanceFunction distanceFunction, Long threshold) {
            super(pivot, distanceFunction, threshold);
        }

        public LocalHAC(List<Node> pivot, DistanceFunction distanceFunction) {
            super(pivot, distanceFunction, 999999);
        }

        protected Long getDistance(Node node, Node node2) {
            return getTestDistances(node, node2);
        }
    }

    /**
     * This method takes the grid description and the pivot description, populates the
     * distances map, and returns a list of the pivot nodes.
     * @param width width of the host grid
     * @param height height of the host grid
     * @param matrix string representation of this grid (has to have width*height elements). <br> '.' - empty place, 1-9 a host with X nodes
     * @param pivotMatrix same as before but this is the string describing the pivot
     * @return
     */
    protected static List<Node> initDistances(int width, int height, String matrix, String pivotMatrix) {
        distances = new HashMap<Node, HashMap<Node, Long>>();
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
            if (pivotMatrix!=null){
                pivots = new ArrayList<Node>();;
                for (int j = 0; j < height; j++) {
                    for (int i = 0; i < width; i++) {
                        if (pivotMatrix.charAt(j * width + i) != '.') {
                            int cnt = Integer.parseInt("" + pivotMatrix.charAt(j * width + i));
                            for (int c = 0; c < cnt; c++) {
                                pivots.add(gridMatrix[i][j][c]);
                            }
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
     * @param sels
     * @return
     */
    protected static String processSelected(List<Node> sels) {
        Integer[][] selMat = new Integer[gridWidth][gridHeight];
        for (Node i : sels) {
            String[] nif = i.toString().split("_");
            int x = Integer.parseInt(nif[1]);
            int y = Integer.parseInt(nif[2]);
            if (selMat[x][y] == null)
                selMat[x][y] = new Integer(0);
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

    protected static String createRandomMap(int w, int h, double density) {
        double chance;
        String map = "";
        for (int y=0; y<h; y++) {
            for (int x=0; x<w; x++) {
                chance = Math.random();
                if (chance<=density) {
                    int toAdd = (int) (Math.floor(Math.random()*10));
                    if (toAdd<1) map+='.';
                    else map+=toAdd;
                } else {
                    map+='.';
                }
            }
        }
        return map;
    }

    protected static void printMap(int w, int h, String map){
        for (int y=0; y<h; y++) {
            System.out.println("");
            for (int x=0; x<w; x++) {
                System.out.print(map.charAt(y*w+x)+"");
            }
        }
    }

    /**
     * Performs a test run
     * @param width width of the host grid
     * @param height height of the host grid
     * @param matrix string representation of this grid (has to have width*height elements). <br> '.' - empty place, 1-9 a host with X nodes
     * @param pivotMatrix same as before but this is the string describing the pivot
     * @param threshold
     * @param cliqueSize needed nodes
     * @return string of the selection's matrix
     */
    public String doHACTest(int width, int height, String matrix, String pivotMatrix, Long threshold,
            int cliqueSize) {

        List<Node> pivots = initDistances(width, height, matrix, pivotMatrix);

        HAC cfPivot = new LocalHAC(pivots, BestProximityDescriptor.AVG, threshold);
        List<Node> cliqueRes = cfPivot.select(cliqueSize, new LinkedList<Node>(distances.keySet()));

        int resultSize = cliqueRes.size();
        boolean didFail = cliqueSize != resultSize;

        if (pivots != null)
            cliqueRes.addAll(pivots);
        String out = processSelected(cliqueRes);

        return didFail ? null : out;
    }

    @org.junit.Test
    public void chooseClosestTest() {
        Long infinity = new Long(99999);
        int W = 10;
        int H = 5;
        String prob =   "2.2......." +
                        "2.2......." +
                        ".........." +
                        ".........." +
                        "..........";

        String pivot=   "0.0......." +
                        "1.0......." +
                        ".........." +
                        ".........." +
                        "..........";

        String result1= ".........." +
                        "2........." +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivot, infinity, 1).equals(result1));

        String result3= "2........." +
                        "2........." +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivot, infinity, 3).equals(result3));

        assertTrue("The algorithm did not fail when it had to!",
                doHACTest(W, H, prob, pivot, (long) 100, 4) == null);

        String probMany = "1.1.1.1.1." +
                          "1.1.9.1.1." +
                          ".........." +
                          ".........." +
                          "..........";

        String noPivot =  ".........." +
                          ".........." +
                          ".........." +
                          ".........." +
                          "..........";

        String resultMany=".........." +
                          "....9....." +
                          ".........." +
                          ".........." +
                          "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, probMany, noPivot, infinity, 9).equals(resultMany));

        String probCorners = "9........." +
                             ".........." +
                             ".........." +
                             ".........1" +
                             "9.......1.";

        String bipartPivot = "9........." +
                             ".........." +
                             ".........." +
                             ".........." +
                             "........1.";

        String resultCorners="9........." +
                             ".........." +
                             ".........." +
                             ".........." +
                             "1.......1.";

        assertTrue("The algorithm did not deal well with far away pivots!",
                doHACTest(W, H, probCorners, bipartPivot, infinity, 1).equals(resultCorners));
    }

    @org.junit.Test
    public void consequencyTest() {
        Long infinity = new Long(99999);
        int W = 10;
        int H = 5;
        String prob =   "22........" +
                        "22........" +
                        ".........." +
                        ".........." +
                        "..........";

        String pivotSW= "00........" +
                        "10........" +
                        ".........." +
                        ".........." +
                        "..........";

        String resultSW=".........." +
                        "2........." +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivotSW, infinity, 1).equals(resultSW));

        String pivotNW= "10........" +
                        "00........" +
                        ".........." +
                        ".........." +
                        "..........";

        String resultNW="2........." +
                        ".........." +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivotNW, infinity, 1).equals(resultNW));

        String pivotNE= "01........" +
                        "00........" +
                        ".........." +
                        ".........." +
                        "..........";

        String resultNE=".2........" +
                        ".........." +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivotNE, infinity, 1).equals(resultNE));

        String pivotSE= "00........" +
                        "01........" +
                        ".........." +
                        ".........." +
                        "..........";

        String resultSE=".........." +
                        ".2........" +
                        ".........." +
                        ".........." +
                        "..........";

        assertTrue("The algorithm did not choose the closest nodes!",
                doHACTest(W, H, prob, pivotSE, infinity, 1).equals(resultSE));
    }

    @org.junit.Test(timeout=60000)
    public void reasonableTimeTestNoPivot() {
        int w = 20;
        int h = 20;
        double density = 0.30;

        //made with w=20; h=20; density=0.3
        String map =    ".2..16............8."+
                        "...7...7.963.....6.7"+
                        "....4...87.7...6..1."+
                        ".9....677.1.75...9.."+
                        "...4.....64.2.....7."+
                        "5........9....4.2..2"+
                        ".................6.."+
                        "........5....6.8...."+
                        "...6....4.........23"+
                        ".3...8.9.......9...."+
                        "...8..9..........5.3"+
                        "..6......7295....1.."+
                        "9.8..4.29..55......."+
                        "..6..7.......2......"+
                        ".4..8...74.....5...."+
                        ".2.7.6.....7.7....8."+
                        "1...637.....71..5..."+
                        ".1...9.......6....38"+
                        "9.............9.914."+
                        "....7..95.3...3..89.";

        int onePercent = (int) Math.round((w*h*density*5)/100);

        doHACTest(w, h, map, null, (long) 99999, onePercent);
        System.out.println("1");
        doHACTest(w, h, map, null, (long) 99999, onePercent*5);
        System.out.println("2");
        doHACTest(w, h, map, null, (long) 99999, onePercent*10);
        System.out.println("3");

    }

    @org.junit.Test(timeout=20000)
    public void reasonableTimeTestWithPivot() {
        int w = 20;
        int h = 20;
        double density = 0.30;

        //made with w=20; h=20; density=0.3
        String map =    ".2..16............8."+
                        "...7...7.963.....6.7"+
                        "....4...87.7...6..1."+
                        ".9....677.1.75...9.."+
                        "...4.....64.2.....7."+
                        "5........9....4.2..2"+
                        ".................6.."+
                        "........5....6.8...."+
                        "...6....4.........23"+
                        ".3...8.9.......9...."+
                        "...8..9..........5.3"+
                        "..6......7295....1.."+
                        "9.8..4.29..55......."+
                        "..6..7.......2......"+
                        ".4..8...74.....5...."+
                        ".2.7.6.....7.7....8."+
                        "1...637.....71..5..."+
                        ".1...9.......6....38"+
                        "9.............9.914."+
                        "....7..95.3...3..89.";

        //this was made by putting a pivot on every
        // 5 node host
        String pivot =
                        ".0..00............0."+
                        "...0...0.000.....0.0"+
                        "....0...00.0...0..0."+
                        ".0....000.0.01...0.."+
                        "...0.....00.0.....0."+
                        "1........0....0.0..0"+
                        ".................0.."+
                        "........1....0.0...."+
                        "...0....0.........00"+
                        ".0...0.0.......0...."+
                        "...0..0..........1.0"+
                        "..0......0001....0.."+
                        "0.0..0.00..11......."+
                        "..0..0.......0......"+
                        ".0..0...00.....1...."+
                        ".0.0.0.....0.0....0."+
                        "0...000.....00..1..."+
                        ".0...0.......0....00"+
                        "0.............0.000."+
                        "....0..01.0...0..00.";

        int onePercent = (int) Math.round((w*h*density*5)/100);

        doHACTest(w, h, map, pivot, (long) 99999, onePercent);
        doHACTest(w, h, map, pivot, (long) 99999, onePercent*5);
        doHACTest(w, h, map, pivot, (long) 99999, onePercent*10);

    }
}
