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
package org.objectweb.proactive.examples.matrix;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class Matrix implements java.io.Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private int width;
    private int height;
    private double[][] tab;
    boolean migration = true;

    // -----------------------------//
    //        CONSTRUCTORS          //
    //------------------------------//
    public Matrix() {
    }

    public Matrix(int w, int h) {
        width = w;
        height = h;
        tab = new double[w][h];
    }

    public Matrix(double[][] table) {
        width = table.length;
        height = table[0].length;
        tab = new double[width][];
        for (int i = 0; i < width; i++)
            tab[i] = table[i];
    }

    //Udab: ERREUR ICI, LE WIDTH NE DEVRAIT PAS ETRE MODIFIE
    // EN FAIT IL Y A CONFUSION ENTRE LE WIDTH ATTRIBUT DE L'OBJET ET LE WIDTH VARIABLE LOCALE
    //C'EST UN PEU LE BORDEL EN FAIT ICI
    public Matrix(Matrix mr, int w) {
        int index = 0;
        int width = 0;
        Matrix result = null;

        int size = ProGroup.size(mr);

        /*        for (int i = 0 ; i < size ; i++) {
           System.out.println("MATRIX : INIT ::  " + ((FutureProxy)ProActiveGroup.get(mr,i)));
           ((FutureProxy)ProActiveGroup.get(mr,i)).waitFor();
           }
               System.out.println("nombre de matrices dans le groupe : " + size);
         */
        this.width = w;
        this.height = w; // ((Matrix)((FutureProxy)ProActiveGroup.get(mr,0)).getResult()).getHeight();

        /*
           startTime = System.currentTimeMillis();
           FutureProxy fp = (FutureProxy)ProActiveGroup.get(mr,0);
           endTime = System.currentTimeMillis() - startTime;
           System.out.println("         Initialisation de reconstruction 1 : " + endTime + " millisecondes\n");
           startTime = System.currentTimeMillis();
           Matrix m = (Matrix)fp.getResult();
           endTime = System.currentTimeMillis() - startTime;
           System.out.println("         Initialisation de reconstruction 2 : " + endTime + " millisecondes\n");
           startTime = System.currentTimeMillis();
           this.height = m.getHeight();
           endTime = System.currentTimeMillis() - startTime;
           System.out.println("         Initialisation de reconstruction 3 : " + endTime + " millisecondes\n");
         */
        tab = new double[this.width][];

        for (int i = 0; i < size; i++) {
            result = ((Matrix) ProGroup.get(mr, i));

            // 	    result = ((Matrix)
            // 		      ((FutureProxy)ProActiveGroup.get(mr,i))
            // 		      .getResult());
            int widthTmp = result.getWidth();
            for (int j = 0; j < widthTmp; j++) {
                // Recopie de la colonne "index"
                tab[index] = result.getColumn(j);
                index++;
            }
        }
    }

    public Matrix(Matrix mr) {
        int index = 0;
        int width = 0;
        Matrix result = null;

        int size = ProGroup.size(mr);

        for (int i = 0; i < size; i++) {
            width += ((Matrix) ((FutureProxy) ProGroup.get(mr, i)).getResult()).getWidth();
        }

        height = ((Matrix) ((FutureProxy) ProGroup.get(mr, 0)).getResult()).getHeight();
        tab = new double[width][];
        for (int i = 0; i < size; i++) {
            result = ((Matrix) ((FutureProxy) ProGroup.get(mr, i)).getResult());
            width = result.getWidth();
            for (int j = 0; j < width; j++) {
                tab[index] = result.getColumn(j);
                index++;
            }
        }
    }

    //     public Matrix (Matrix mr) {
    // 	int w = 0;
    // // 	System.out.println(mr.getClass());
    // // 	org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject)mr).getProxy();
    // // 	System.out.println(theProxy.getClass());
    // // 	System.out.println(((FutureProxy)theProxy).getResult().getClass());
    // // 	if (!(theProxy instanceof org.objectweb.proactive.core.group.ProxyForGroup))
    // // 	    System.out.println("m2group est bien un group !!!!!!!");
    // 	for (int i=0 ; i < ProActiveGroup.size(mr) ; i++) {
    // //  	    System.out.println(" taille width : " + ((Matrix)((FutureProxy)ProActiveGroup.get(mr,i)).getResult()).getWidth() +
    // //  			       " taille height : " + ((Matrix)((FutureProxy)ProActiveGroup.get(mr,i)).getResult()).getHeight() );
    // 	    w += ((Matrix)((FutureProxy)ProActiveGroup.get(mr,i)).getResult()).getWidth();
    // 	}
    // 	width = w;
    // 	height = ((Matrix)((FutureProxy)ProActiveGroup.get(mr,0)).getResult()).getHeight();
    // //        	System.out.println("Largeur de la matrix resultat : "+ width);
    // 	tab = new double[width][];
    // 	int index = 0;
    // 	for (int i=0 ; i < ProActiveGroup.size(mr) ; i++) {
    // // 	    System.out.println("Matrix resultat : " + i);
    // 	    for (int j=0 ; j < ((Matrix)((FutureProxy)ProActiveGroup.get(mr,i)).getResult()).getWidth() ; j++) {
    // // 		System.out.println("Je m'occupe de la colonne : " + index);
    // 		tab[index] = ((Matrix)((FutureProxy)ProActiveGroup.get(mr,i)).getResult()).getColumn(j);
    // 		index++;
    // 	    }
    // 	}
    //     }
    public Matrix(Matrix[] mr) {
        int w = 0;
        for (int i = 0; i < mr.length; i++)
            w += mr[i].getWidth();

        width = w;
        height = mr[0].getHeight();

        tab = new double[width][];

        int index = 0;
        for (int i = 0; i < mr.length; i++) {
            for (int j = 0; j < mr[i].getWidth(); j++) {
                tab[index] = mr[i].getColumn(j);
                index++;
            }
        }
    }

    //----------------------------//
    //         ACCESSORS          //
    //----------------------------//
    public int getWidth() {
        return width;
    }

    public void setWidth(int w) {
        width = w;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int h) {
        height = h;
    }

    public double getWH(int w, int h) {
        return tab[w][h];
    }

    public void setWH(int w, int h, double val) {
        tab[w][h] = val;
    }

    public double[][] getTab() {
        return tab;
    }

    public double[] getColumn(int w) {
        return tab[w];
    }

    //-----------------------------------//
    //             METHODS               //
    //-----------------------------------//
    static int k = 0;

    public void initializeWithRandomValues() {
        //int k= 0;
        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++)
                setWH(i, j, k++);

        /*        for (int i = 0 ; i < tab.length ; i++)
           for (int j = 0 ; j < tab[i].length ; j++)
               tab[i][j] = Math.round(Math.random()*10);
         */
    }

    //Udab: Modifie pour tests
    @Override
    public String toString() {
        //System.out.println("Methode Matrix::toString");
        String s = new String("");
        int height = this.getHeight();
        int width = this.getWidth();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // System.out.println(" i = " + i + ";   j = " + j + ";");
                s += Double.toString(getWH(j, i));
                s += "   ";
            }
            s += "\n";
        }
        return s;
    }

    public Matrix getVerticalSubMatrix(int widthStart, int widthStop) {
        double[][] d = new double[widthStop - widthStart][];
        for (int i = 0; i < (widthStop - widthStart); i++)
            d[i] = tab[widthStart + i];
        return new Matrix(d);
    }

    public Matrix getActiveVerticalSubMatrix(int widthStart, int widthStop,
        Node node) {
        Matrix vsm = null;

        double[][] d = new double[widthStop - widthStart][];
        for (int i = 0; i < (widthStop - widthStart); i++)
            d[i] = tab[widthStart + i];

        Object[] params = new Object[1];
        params[0] = d;

        try {
            vsm = (Matrix) ProActiveObject.newActive("org.objectweb.proactive.examples.matrix.Matrix",
                    params, node);
        } catch (ActiveObjectCreationException e) {
            logger.error(
                "Error create Active Vertical Sub Matrix : ActiveObjectCreationException\n");
        } catch (NodeException e) {
            logger.error(
                "Error create Active Vertical Sub Matrix : NodeException\n");
        }
        return vsm;
    }

    public Matrix transformIntoActiveVerticalSubMatrixGroup(Node[] nodeList) {
        // if (getWidth() >= nodeList.length)
        Matrix result = null;
        int widthSubMatrix;
        int more;
        boolean pile;
        if ((getWidth() % nodeList.length) == 0) {
            widthSubMatrix = getWidth() / nodeList.length;
            more = 0;
            pile = true;
        } else {
            widthSubMatrix = (getWidth() / nodeList.length) + 1;
            more = getWidth() % widthSubMatrix;
            pile = false;
        }

        Object[][] params = new Object[nodeList.length][];

        for (int i = 0; i < nodeList.length; i++) {
            Object[] po = new Object[1];
            double[][] d;
            if ((!pile) && (i == (nodeList.length - 1))) {
                d = new double[more][];
            } else {
                d = new double[widthSubMatrix][];
            }

            for (int j = 0; j < d.length; j++)
                d[j] = tab[(i * widthSubMatrix) + j];

            po[0] = d;

            params[i] = po;

            /* Debugging
               System.out.println("SubMatrix " + i + "  d.length = " + d.length);
               String s = new String("");
               for (int h=0 ; h < d[0].length ; h++) {
                   for (int l=0 ; l < d.length ; l++) {
                       s += d[l][h];
                       s += "   ";
                   }
                   s += "\n";
               }
               System.out.println(s);

             */
        }

        try {
            result = (Matrix) ProGroup.newGroup("org.objectweb.proactive.examples.matrix.Matrix",
                    params, nodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public Matrix localMultiplyForGroup(Matrix m) {
        //long startTime;
        //long endTime;
        //startTime= System.currentTimeMillis();
        Matrix res = new Matrix(getWidth(), m.getHeight());
        int height = res.getHeight();
        int width = res.getWidth();
        for (int line = 0; line < height; line++) {
            for (int column = 0; column < width; column++) {
                double val = 0;
                for (int index = 0; index < height; index++) {
                    //System.out.println("(" + m.getWH(index,line) + " * " + getWH(column,index) + ") + ");
                    val += (m.getWH(index, line) * getWH(column, index));
                }

                //System.out.println(" == " + val + "\n");
                res.setWH(column, line, val);
            }
        }

        //endTime = System.currentTimeMillis() - startTime;
        //System.out.println(" Local multiply for group : " + endTime + " millisecondes\n\n");
        return res;
    }

    public Matrix distributedMultiply(Matrix m, Node[] nodeList) {
        if (getWidth() != m.getHeight()) {
            logger.error("Error : no compatible Matrix");
            return null;
        } else {
            Matrix verticalSubMatrixGroup = null;

            verticalSubMatrixGroup = m.transformIntoActiveVerticalSubMatrixGroup(nodeList);

            Matrix mr = verticalSubMatrixGroup.localMultiplyForGroup(this);
            return new Matrix(mr);
        }
    }

    public Matrix distributedMultiply(Matrix m) {
        Node[] nodeList = new Node[1];
        nodeList[0] = null;
        return distributedMultiply(m, nodeList);
    }

    public Matrix localMultiply(Matrix m) {
        return m.localMultiplyForGroup(this);
    }

    public Matrix[] transformIntoActiveMatrixTable(Node[] nodeList) {
        // if (getWidth() >= nodeList.length)
        Matrix[] result = new Matrix[nodeList.length];
        int widthSubMatrix;
        int more;
        boolean pile;
        if ((getWidth() % nodeList.length) == 0) {
            widthSubMatrix = getWidth() / nodeList.length;
            more = 0;
            pile = true;
        } else {
            widthSubMatrix = (getWidth() / nodeList.length) + 1;
            more = getWidth() % widthSubMatrix;
            pile = false;
        }

        for (int i = 0; i < nodeList.length; i++) {
            Object[] po = new Object[1];
            double[][] d;
            if ((!pile) && (i == (nodeList.length - 1))) {
                d = new double[more][];
            } else {
                d = new double[widthSubMatrix][];
            }

            for (int j = 0; j < d.length; j++)
                d[j] = tab[(i * widthSubMatrix) + j];

            po[0] = d;

            try {
                result[i] = (Matrix) ProActiveObject.newActive("org.objectweb.proactive.examples.matrix.Matrix",
                        po, nodeList[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public Matrix distributedMultiplyWithOutGroup(Matrix m, Node[] nodeList) {
        if (getWidth() != m.getHeight()) {
            logger.error("Error : no compatible Matrix");
            return null;
        } else {
            Matrix[] verticalSubMatrixTable;

            verticalSubMatrixTable = m.transformIntoActiveMatrixTable(nodeList);

            Matrix[] mr = new Matrix[verticalSubMatrixTable.length];

            for (int i = 0; i < verticalSubMatrixTable.length; i++)
                mr[i] = verticalSubMatrixTable[i].localMultiplyForGroup(this);

            return new Matrix(mr);
        }
    }

    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    private void writeObject(java.io.ObjectOutputStream out)
        throws java.io.IOException {
        //long startTime;
        //long endTime;
        //startTime = System.currentTimeMillis();
        out.defaultWriteObject();

        //endTime = System.currentTimeMillis() - startTime;
        //	System.out.println("     Serialization : " + endTime + " millisecondes\n");
    }

    //     private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    // 	in.defaultReadObject();
    //     }
}
