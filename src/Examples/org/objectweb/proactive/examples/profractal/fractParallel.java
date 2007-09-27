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
package org.objectweb.proactive.examples.profractal;


/* fractParallel.java
 *
 * Created on Semptember 6 2003, 11.56
 */

/**
 * @author Daniele Di Felice
 */
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProGroup;
import org.objectweb.proactive.core.group.Group;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;


/**
 * This class execute the distributed generation of the fractal image,
 * display it in a window, save the results in a bitmap file and in an ASCII file
 */
public class fractParallel extends Frame {
    private boolean[][] results;
    private String[] address = null;
    private boolean distributed = false;
    private Boolean computing = new Boolean(false);
    private TextArea textLog = null;
    private TextField tfR1 = null;
    private TextField tfI1 = null;
    private TextField tfR2 = null;
    private TextField tfI2 = null;
    private TextField tfSteps = null;
    private TextField tfZoom = null;
    private TextField tfAONumber = null;
    private java.awt.List lst = null;
    private Checkbox bitmapCheck = null;
    private Checkbox asciiCheck = null;

    /** Constructor */
    public fractParallel() {
        super("Parallel fractal image generation with ProActive");
        setResizable(true);
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    System.exit(0);
                }
            });
    }

    /** Main */
    public static void main(String[] args) {
        fractParallel application = new fractParallel();
        application.start(args);
    }

    /** Start method
     * @param args: arguments
     * @return void
     */
    public void start(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: fractParallel resourceFile");
            System.exit(1);
        }

        // Interface construction
        Frame f = new Frame("Parallel fractal image generation with ProActive");
        f.setLayout(new GridLayout(0, 1));
        f.setResizable(true);
        Label title = new Label(
                "Parallel fractal image generation with ProActive");
        title.setFont(new Font("Arial", 1, 20));
        f.add(title);
        Panel p1 = new Panel();
        p1.setName("UL");
        p1.add(new Label("Upper-Left real part:"));
        tfR1 = new TextField("", 10);
        p1.add(tfR1);
        p1.add(new Label("Upper-Left imaginary part:"));
        tfI1 = new TextField("", 10);
        p1.add(tfI1);
        Panel p2 = new Panel();
        p2.setName("LR");
        p2.add(new Label("Lower-Right real part:"));
        tfR2 = new TextField("", 10);
        p2.add(tfR2);
        p2.add(new Label("Lower-Right imaginary part:"));
        tfI2 = new TextField("", 10);
        p2.add(tfI2);
        Panel p3 = new Panel();
        p3.setName("Parameters");
        p3.add(new Label("Steps:"));
        tfSteps = new TextField("", 10);
        p3.add(tfSteps);
        p3.add(new Label("Zoom:"));
        tfZoom = new TextField("", 10);
        p3.add(tfZoom);
        Panel p4 = new Panel();
        p4.setName("Nodes");
        p4.add(new Label("Active objects number:"));
        tfAONumber = new TextField("", 10);
        p4.add(tfAONumber);
        BufferedReader in = null;

        //ebe add an init value to textfield to avoid to re enter each time 
        tfR1.setText("-1.0");
        tfI1.setText("1.0");
        tfR2.setText("1.0");
        tfI2.setText("-1.0");
        tfSteps.setText("50");
        tfZoom.setText("0.001");
        tfAONumber.setText("4");

        try {
            in = new BufferedReader(new FileReader(args[0]));
        } catch (IOException ioe) {
            System.err.println("Error: cannot read resource file!" + args[0]);
            System.exit(1);
        }
        try {
            int dim = 0;
            while (in.readLine() != null) {
                dim++;
            }
            address = new String[dim / 2];
            in = new BufferedReader(new FileReader(args[0]));
            int z = 0;
            String nodeName = "";
            lst = new java.awt.List(2, true);
            while ((nodeName = in.readLine()) != null) {
                lst.add(nodeName);
                address[z] = in.readLine();
                z++;
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Error: error manipulating input file!");
            e.printStackTrace();
            System.exit(1);
        }
        p4.add(new Label("Free nodes:"));
        p4.add(lst);
        Panel p5 = new Panel();
        Button buttonStart = new Button("Start");
        buttonStart.addActionListener(new StartListener(f));
        p5.add(buttonStart);
        Button buttonQuit = new Button("Quit");
        buttonQuit.addActionListener(new QuitListener(f));
        p5.add(buttonQuit);
        f.add(p1);
        f.add(p2);
        f.add(p3);
        f.add(p4);
        f.add(p5);
        Panel p = new Panel();
        p.setLayout(new GridLayout(1, 0));
        p.add(new Label("Output format:"));
        bitmapCheck = new Checkbox("Bitmap", null, true);
        p.add(bitmapCheck);
        asciiCheck = new Checkbox("ASCII", null, true);
        p.add(asciiCheck);
        f.add(p);
        textLog = new TextArea("[" + (new Date()) + "] " +
                "Application started \n", 8, 40);
        textLog.setEditable(false);
        textLog.setBackground(Color.WHITE);
        f.add(textLog);
        f.pack();
        f.setVisible(true);
    }

    /** The computational method
     * @param r1: the upper-left real part
     * @param i1: the upper-left imaginary part
     * @param r2: the lower-right real part
     * @param i2: the lower-right imaginary part
     * @param steps: steps number
     * @param zoom: zoom value
     * @param nodes: nodes number
     * @return void
     */
    public void compute(double r1, double i1, double r2, double i2, long steps,
        double zoom, int nodes) {
        int w = 0;
        while ((w <= lst.getItemCount()) && (!distributed)) {
            distributed = lst.isIndexSelected(w);
            w++;
        }
        if (!distributed) {
            textLog.append("[" + (new Date()) + "] " +
                "Local parallel computation...\n");
            distributed = false;
        } else {
            textLog.append("[" + (new Date()) + "] " +
                "Distributed parallel computation...\n");
            distributed = true;
        }
        Double checkPower = new Double((double) ((double) Math.sqrt(nodes)));
        checkPower = new Double(checkPower.doubleValue() -
                checkPower.intValue());
        if (!checkPower.equals(new Double(0.0))) {
            textLog.append("[" + (new Date()) + "] " +
                "Error: nodes must be a power of two...\n");
            return;
        }
        double column = r1 - r2;
        double row = i1 - i2;
        row = row / zoom;
        column = column / zoom;
        Double nodeSquare = new Double(Math.sqrt(nodes));
        int n = nodeSquare.intValue();
        row = Math.abs(row);
        column = Math.abs(column);
        Double app = new Double((double) ((double) column) / n);
        app = new Double(app.intValue() - app.doubleValue());
        if (!app.equals(new Double(0.0))) {
            textLog.append("[" + (new Date()) + "] " +
                "Error: the differences are not divisible for the square root of nodes!\n");
            return;
        }
        app = new Double((double) ((double) row) / n);
        app = new Double(app.intValue() - app.doubleValue());
        if (!app.equals(new Double(0.0))) {
            textLog.append("[" + (new Date()) + "] " +
                "Error: the differences are not divisible for the square root of nodes!\n");
            return;
        }

        // Build the computational group
        fractComputation fractGroup = null;
        Group fg = null;
        try {
            fractGroup = (fractComputation) ProGroup.newGroup(fractComputation.class.getName());
            fg = ProGroup.getGroup(fractGroup);
        } catch (Exception e) {
            textLog.append("[" + (new Date()) + "] " +
                "Error: cannot create ProActive Group!\n");
            e.printStackTrace();
            return;
        }
        int[] selectedResources = lst.getSelectedIndexes();
        int resCount = 0;

        // Creation of the active objects and generation of the grid
        String destination = null;
        i2 = i1;
        for (int i = 0; i < n; i++) {
            i2 = i2 - ((row / n) * zoom);
            for (int j = 0; j < n; j++) {
                if (distributed) {
                    if (resCount == selectedResources.length) {
                        resCount = 0;
                    }
                    destination = address[selectedResources[resCount]];
                    System.out.println(destination);
                    resCount++;
                }
                try {
                    // If destination==null, the computation will be done locally
                    fg.add(ProActiveObject.turnActive(
                            new fractComputation(
                                new Complex(r1 + (((j * column) / n) * zoom), i1),
                                new Complex(r1 +
                                    ((((j + 1) * column) / n) * zoom), i2),
                                steps, zoom), destination));
                    textLog.append(
                        "-------------------------------------------------\n");
                    textLog.append("UL-> Real: " +
                        (r1 + (((j * column) / n) * zoom)) + " Imaginary: " +
                        i1 + "\n");
                    textLog.append("LR-> Real: " +
                        (r1 + ((((j + 1) * column) / n) * zoom)) +
                        " Imaginary: " + i2 + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                    textLog.append("[" + (new Date()) + "] " +
                        "Error: internal error!\n");
                    return;
                }
            }
            i1 = i1 - ((row / n) * zoom);
        }
        textLog.append("-------------------------------------------------\n");
        // Computation start
        textLog.append("[" + (new Date()) + "] " + "Starting computation...\n");
        Date startTime = new Date();
        fractResult resultsGroup = fractGroup.Compute();
        if (resultsGroup == null) {
            textLog.append("[" + (new Date()) + "] " +
                "Error: cannot create results group!\n");
            return;
        }
        int received = 0;
        while (received < nodes) {
            ProGroup.waitN(resultsGroup, received + 1);
            received++;
            textLog.append("[" + (new Date()) + "] " + received + "/" + nodes +
                " results received...\n");
        }
        Date endTime = new Date();

        // Build final result
        textLog.append("[" + (new Date()) + "] " +
            "Building the final result...\n");
        results = new boolean[(int) row][(int) column];
        setSize((int) column, (int) row);
        boolean[][] tmp = null;
        int ColCounter = 0;
        int RowCounter = 0;
        int index1 = 0;
        int index2 = 0;
        int appn = n;
        for (int i = 0; i < nodes; i++) {
            // Debug information
            //System.out.println(".");
            tmp = ((fractResult) ProGroup.get(resultsGroup, i)).getResults();
            if (i == appn) {
                ColCounter = 0;
                RowCounter++;
                appn = appn + n;
            }
            for (int k = 0; k < (row / n); k++) {
                for (int j = 0; j < (column / n); j++) {
                    try {
                        index1 = (k + (int) ((RowCounter * row) / n));
                        index2 = (j + (int) ((ColCounter * column) / n));
                        results[index1][index2] = tmp[k][j];
                    } catch (Exception e) {
                        System.err.println(
                            "Error: sorry... internal error... please report this bug to: Daniele.Di_Felice@sophia.inria.fr");
                        System.exit(1);
                    }
                }
            }
            tmp = null;
            ColCounter++;
        }

        // Display the image
        setVisible(true);
        // Write the ascii image
        if (asciiCheck.getState()) {
            textLog.append("[" + (new Date()) + "] " +
                "Writing the ASCII file...\n");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream("results.txt");
            } catch (Exception e) {
                textLog.append("[" + (new Date()) + "] " +
                    "Error: cannot create output file...\n");
                return;
            }
            try {
                out.write(("Upper-Left=(" + r1 + "," + i1 + "), Lower-Right=(" +
                    r2 + "," + i2 + "), step=" + steps + ", zoom=" + zoom).getBytes());
                out.write("\n\n".getBytes());
                out.write(("Start at: " + startTime.toString() + " on " +
                    nodes + " nodes").getBytes());
                out.write("\n".getBytes());
                out.write(("Finish at: " + endTime.toString()).getBytes());
                out.write("\n\n".getBytes());
                for (int i = 0; i < results.length; i++) {
                    out.write("\n".getBytes());
                    for (int j = 0; j < results[i].length; j++) {
                        if (results[i][j]) {
                            out.write("*".getBytes());
                        } else {
                            out.write("-".getBytes());
                        }
                    }
                }
                out.close();
            } catch (Exception e) {
                textLog.append("[" + (new Date()) + "] " +
                    "Error: cannot write output...\n");
                return;
            }
            try {
                Thread.sleep(1 * 1000);
            } catch (Exception e) {
            }
        }

        // Write the bitmap file
        if (bitmapCheck.getState()) {
            textLog.append("[" + (new Date()) + "] " +
                "Writing the bitmap file...\n");
            FileOutputStream os = null;
            try {
                os = new FileOutputStream("./results.bmp");
            } catch (Exception e) {
                textLog.append("[" + (new Date()) + "] " +
                    "Error: cannot create output stream!\n");
                return;
            }
            BMPEncodeParam param = new BMPEncodeParam();
            ImageEncoder enc = ImageCodec.createImageEncoder("BMP", os, param);
            byte[][] dataArray = new byte[1][(int) row * (int) column];
            DataBufferByte dbb = new DataBufferByte(dataArray,
                    (int) row * (int) column);
            Raster ras = Raster.createPackedRaster(dbb, (int) column,
                    (int) row, 8, new Point(0, 0));
            WritableRaster wras = ras.createCompatibleWritableRaster();
            for (int i = 0; i < results.length; i++) {
                for (int j = 0; j < results[i].length; j++) {
                    if (results[i][j]) {
                        wras.setPixel(j, i, new int[] { 1000 });
                    } else {
                        wras.setPixel(j, i, new int[] { 0 });
                    }
                }
            }
            int[] pam = new int[2];
            pam[0] = 8;
            pam[1] = 8;
            ComponentColorModel ccm = new ComponentColorModel(ColorSpace.getInstance(
                        ColorSpace.CS_sRGB), pam, false, false, 2, 2);
            try {
                enc.encode(wras, ccm);
                os.close();
            } catch (Exception e) {
                textLog.append("[" + (new Date()) + "] " +
                    "Error: cannot write output file!\n");
                return;
            }
        }
        textLog.append("[" + (new Date()) + "] " + "Finish!\n");
    }

    /** Paint method
     * @param g: a Graphics Object
     * @return void
     */
    @Override
    public void paint(Graphics g) {
        this.setResizable(true);
        g.setColor(Color.black);
        for (int i = 0; i < results.length; i++) {
            for (int j = 0; j < results[i].length; j++) {
                if (results[i][j]) {
                    g.drawLine(j, i, j, i);
                }
            }
        }
    }

    /**
     * This class represent the listener on the Quit button
     */
    public class QuitListener implements ActionListener {
        private Frame f;

        /** Constructor
         * @param args: the frame on which the listerner it must be activated
         */
        QuitListener(Frame f) {
            this.f = f;
        }

        /** The action to perform
         * @param e: the generator event
         */
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            f.dispose();
            System.exit(0);
        }
    }

    /**
     * This class represent the listener on the Start button
     */
    public class StartListener implements ActionListener {
        private Frame f;

        /** Constructor
         * @param args: the frame on which the listerner it must be activated
         */
        StartListener(Frame f) {
            this.f = f;
        }

        /** The action to perform
         * @param e: the generator event
         */
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            if (computing.booleanValue()) {
                textLog.append("[" + (new Date()) + "] " +
                    "Work in progress... please wait...\n");
                return;
            }

            // Control if all the inserted values are numerical correct
            textLog.append("[" + (new Date()) + "] " +
                "Checking parameters...\n");
            if ((tfR1.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the first real part...\n");
                return;
            }
            if ((tfI1.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the first imaginary part...\n");
                return;
            }
            if ((tfR2.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the second real part...\n");
                return;
            }
            if ((tfI2.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the second imaginary part...\n");
                return;
            }
            if ((tfSteps.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the steps number...\n");
                return;
            }
            if ((tfZoom.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the zoom value (ex. 0.01)...\n");
                return;
            }
            if ((tfAONumber.getText()).equals("")) {
                textLog.append("[" + (new Date()) + "] " +
                    "Please insert the active objects number...\n");
                return;
            }
            int AON;
            long Steps;
            Double Zoom = null;
            Double R1 = null;
            Double I1 = null;
            Double R2 = null;
            Double I2 = null;
            try {
                try {
                    R1 = new Double(tfR1.getText());
                    I1 = new Double(tfI1.getText());
                    R2 = new Double(tfR2.getText());
                    I2 = new Double(tfI2.getText());
                } catch (Exception e1) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid complex number...\n");
                    return;
                }
                if (R1.isNaN() || I1.isNaN() || R2.isNaN() || I2.isNaN()) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid complex number...\n");
                    return;
                }
                if (R1.compareTo(R2) >= 0) {
                    textLog.append("[" + (new Date()) + "] " +
                        "The first real part must be less to the second...\n");
                    return;
                }
                if (I1.compareTo(I2) <= 0) {
                    textLog.append("[" + (new Date()) + "] " +
                        "The first real part must be greater to the second...\n");
                    return;
                }
                Steps = (new Long(tfSteps.getText())).longValue();
                if (Steps <= 0) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid steps number...\n");
                    return;
                }
                try {
                    Zoom = new Double(tfZoom.getText());
                } catch (Exception e2) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid zoom value (try 0.001)...\n");
                    return;
                }
                if (Zoom.isNaN()) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid zoom value (try 0.001)...\n");
                    return;
                }
                AON = (new Integer(tfAONumber.getText())).intValue();
                if (AON <= 2) {
                    textLog.append("[" + (new Date()) + "] " +
                        "Invalid active objects number...\n");
                    return;
                }
            } catch (Exception pe) {
                textLog.append("[" + (new Date()) + "] " +
                    "One or more invalid parameter...\n");
                return;
            }

            // Computation start
            computing = new Boolean(true);
            compute(R1.doubleValue(), I1.doubleValue(), R2.doubleValue(),
                I2.doubleValue(), Steps, Zoom.doubleValue(), AON);
            computing = new Boolean(false);
        }
    }
}
