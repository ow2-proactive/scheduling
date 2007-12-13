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
package org.objectweb.proactive.examples.nbody.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Material;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.geometry.Text2D;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;


/**
 * Main bodies displayer using Java3D
 */
public class NBody3DFrame extends JFrame implements NBodyFrame, WindowListener {

    /**
     * size of the screen
     */
    public final static int SIZE = 500;

    /**
     * Maximum of ancient position of body (aka Ghost) saved.
     * Reduces this number to increase performance
     * Example of Complexities :
     * <li> Creation of java3d scene tree is at <B>O(MAX_HISTO_SIZE*nbBodies)</B><li>
     * <li> Drawing of trace is at <B>O(MAX_HISTO_SIZE*nbBodies)</B>
     */

    //public final static int MAX_HISTO_SIZE = 10;
    public int MAX_HISTO_SIZE = 10;

    /**
     * Used to scale the diameters of the bodies
     */
    public final static double MASS_RATIO = 20000.00;

    /**
     * killsupport
     */
    private Start killsupport;

    /**
     * Numbers of bodies
     */
    private int nbBodies;

    /**
     * True when no message from any body have been received
     * (when passing false launch the display refreshing thread)
     */
    private boolean firstMovement;

    /**
     * if true "traces" are displayed
     * when false avoid translations of ghost bodies.
     */
    private boolean mustDrawTraces;

    /**
     * Display refreshing thread
     */
    private RefreshPositionsThread refreshPositionsThread;

    /**
     * Trace Toggle CheckBox
     */
    private JCheckBox traceButton;

    /**
     * Hostname Label Toggle CheckBox
     */
    private JCheckBox labelButton;

    /**
     * Array of translations of bodies in 3D space
     */
    private Transform3D[] translations;

    /**
     * Array of bodies scale (depending on the mass)
     * NOTE : USEFUL because mass issn't known at GUI Start
     */
    private Transform3D[] planetesScaling;

    /**
     * TransformGroup of bodies translation (from the origin) to apply translations on.
     */
    private TransformGroup[] translationsGroup;

    /**
     * TransformGroup of bodies scaling to apply transfromation on.
     */
    private TransformGroup[] planetesScalingGroup;

    /*
     * TransformGroup linked to a bodies location where to add hostname label.
     */

    //private TransformGroup[] labels;
    /**
     * TransformGroup linked to a bodies location where to add hostname label.
     */
    private BranchGroup[] labels;

    /**
     * translation to apply to all ghost bodies (trace) for all the bodies
     */
    private Transform3D[][] tracesTransformation;

    /**
     * TransformationGroup of translation to apply to ghost bodies (trace) for all the bodies
     */
    private TransformGroup[][] tracesGroup;

    /**
     * Relative Origin X
     */
    private double baseX;

    /**
     * Relative Origin Y
     */
    private double baseY;

    /**
     * Relative Origin Z
     */
    private double baseZ;

    /**
     * Root of the 3d scene
     */
    private BranchGroup root;

    /**
     * Root of the mouse-manipulable scene
     */
    private TransformGroup transformRoot;

    /**
     * Local Body (kind of proxy)
     */
    private LocalBody[] bodies;
    private String[] bodyname;
    private ArrayList names;
    private JComboBox protocol;
    private JComboBox listVMs;

    //
    // --- PUBLIC METHODS -------------------------------------------------------------
    //

    /**
     * Initialisation of the frame
     * @param title Title of the GUI
     * @param nb number of body
     * @param ft adds fault-tolerance interface.
     * @param killsupport (unknown)
     */
    public NBody3DFrame(String title, Integer nb, Boolean ft, Start killsupport) {
        super(title);
        this.killsupport = killsupport;
        this.firstMovement = true;
        this.mustDrawTraces = true;
        this.refreshPositionsThread = new RefreshPositionsThread();
        boolean displayft = ft.booleanValue();
        bodyname = new String[nb.intValue()];

        names = new ArrayList(nb.intValue());
        for (int i = 0; i < nb.intValue(); i++) {
            names.add(i, " ");
            bodyname[i] = "";
        }

        //historique
        this.MAX_HISTO_SIZE = 200 / nb.intValue();

        // Planetes locales
        this.nbBodies = nb.intValue();
        bodies = new LocalBody[nbBodies];
        for (int i = 0; i < nbBodies; i++) {
            bodies[i] = new LocalBody();
        }

        // Referenciel
        this.baseX = this.baseY = this.baseZ = 0.0;

        // Transformations
        this.translations = new Transform3D[nbBodies];
        this.planetesScaling = new Transform3D[nbBodies];
        this.translationsGroup = new TransformGroup[nbBodies];
        this.planetesScalingGroup = new TransformGroup[nbBodies];

        //Labels
        this.labels = new BranchGroup[nbBodies];

        //Traces
        this.tracesTransformation = new Transform3D[this.nbBodies][this.MAX_HISTO_SIZE];
        this.tracesGroup = new TransformGroup[this.nbBodies][this.MAX_HISTO_SIZE];

        // GUI
        this.setSize(SIZE, SIZE);
        this.addWindowListener(this);

        // 3D parts
        Canvas3D canvas3D = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        SimpleUniverse simpleU = new SimpleUniverse(canvas3D);
        simpleU.getViewingPlatform().setNominalViewingTransform();

        /*simpleU.getViewingPlatform().setCapability(ViewingPlatform.ALLOW_BOUNDS_WRITE);
        simpleU.getViewingPlatform().setBounds(new BoundingSphere(new Point3d(0,0,0), 999999999));
         */
        BranchGroup scene = this.createScene(simpleU);
        scene.compile();
        simpleU.addBranchGraph(scene);

        // Assembling it all
        JPanel main = new JPanel(new BorderLayout());
        main.add(canvas3D, BorderLayout.CENTER);

        JPanel south = new JPanel();
        main.add(south, BorderLayout.SOUTH);

        if (displayft) {
            JPanel killingPanel = new JPanel(new GridLayout(1, 4));
            protocol = new JComboBox(new Object[] { "rsh", "ssh" });
            listVMs = new JComboBox();
            //listVMs.addActionListener(this);
            JLabel cmd = new JLabel(" killall java  ");
            JButton kill = new JButton("Execute");
            kill.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    try {
                        Runtime.getRuntime().exec(
                                "" + protocol.getSelectedItem() + " " + listVMs.getSelectedItem() +
                                    " killall -KILL java");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            });
            killingPanel.add(protocol);
            killingPanel.add(listVMs);
            killingPanel.add(cmd);
            killingPanel.add(kill);
            killingPanel.setBorder(BorderFactory.createTitledBorder("Execution control"));

            south.add(killingPanel);
        }

        traceButton = new JCheckBox();
        traceButton.setText("Plots");
        traceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceButtonActionPerformed(evt);
            }
        });
        south.add(traceButton);

        labelButton = new JCheckBox();
        labelButton.setText("Host Names");
        labelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelButtonActionPerformed(evt);
            }
        });
        labelButton.setSelected(true);
        south.add(labelButton);

        JButton centreButton = new JButton();
        centreButton.setText("Center");
        centreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                centrer();
            }
        });
        south.add(centreButton);

        //main.add(controlPanel, BorderLayout.SOUTH);
        setContentPane(main);
        setVisible(true);
    }

    /**
     * Method Invoked by remote bodies
     * @param x new x of the body
     * @param y new y of the body
     * @param z new z of the body
     * @param vx new vx of the body
     * @param vy new vy of the body
     * @param vz new vz of the body
     * @param mass mass of the body (INCOHERENT !)
     * @param diameter diameter of the body (DOUBLON D INFO, et INCOHERENT)
     * @param identification id of the body who call the method
     * @param hostName where the body is hosted
     */
    public void drawBody(double x, double y, double z, double vx, double vy, double vz, int mass,
            int diameter, int identification, String hostName) {
        // Deplacement
        //this.movePlanete(identification, x, y,z);
        synchronized (this.bodies[identification]) {
            this.bodies[identification].move(x, y, z);
        }

        // Diametre des planetesScaling
        if (this.bodies[identification].getDiameter() != mass) {
            this.bodies[identification].setDiameter(mass);
            this.setPlaneteDiameter(identification, mass);
        }

        // Labelling
        if (this.bodies[identification].getHostName().compareTo(hostName) != 0) {
            this.bodies[identification].setHostName(hostName);
            if (this.labelButton.isSelected()) {
                this.writeLabel(identification, hostName, diameter);
            }
        }

        // Initialize movements
        if (firstMovement) {
            this.refreshPositionsThread.start();
            this.setTraceVisible(this.traceButton.isSelected());
            this.firstMovement = false;
        }

        bodyname[identification] = hostName;
        if (!names.contains(hostName)) {
            this.names.remove(identification);
            this.names.add(identification, hostName);
            if (this.listVMs != null) {
                this.listVMs.addItem(hostName);
            }
        }
    }

    /**
     * Called by the refreshing thread, replaces all the 3D shapes
     * (eq to repaint)
     */
    public void refreshPositions() {
        double x;
        double y;
        double z;
        for (int i = 0; i < this.nbBodies; i++) {
            synchronized (this.bodies[i]) {
                x = this.bodies[i].getX();
                y = this.bodies[i].getY();
                z = this.bodies[i].getZ();
            }
            this.movePlanete(i, x - this.baseX, y - this.baseY, z - this.baseZ);
        }
    }

    //
    // -- implements WindowListener --------------------------
    //
    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.killsupport.quit();
        System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
        windowClosing(e);
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    //
    // -- PRIVATE METHODS ----------------------------------
    //

    /**
     * Method Invoked when Trace CheckBox is toggled
     * @param evt Event
     */
    private void traceButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setTraceVisible(this.traceButton.isSelected());
    }

    private void labelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setLabelVisible(this.labelButton.isSelected());
    }

    /**
     * Initialize java3D Space
     * Construct the globel java3D Tree
     * @param simpleU (unknown)
     * @return Return the root Branch Group of the scene (after this, compile it).
     */
    private BranchGroup createScene(SimpleUniverse simpleU) {
        BoundingSphere bounds = new BoundingSphere(new Point3d(), 10000.0);
        TransformGroup vpTrans = simpleU.getViewingPlatform().getViewPlatformTransform();

        // RAW ROOT
        BranchGroup objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

        // ENABLE MANIPULATION
        transformRoot = this.enableMouseNavigation(objRoot, bounds);

        // MANIPULABLE ROOT
        this.root = new BranchGroup();
        this.root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        transformRoot.addChild(this.root);

        // planets Creation
        for (int i = 0; i < nbBodies; i++) {
            Appearance a = createPlaneteAppareance(i);
            Appearance t = new Appearance();
            t.setMaterial(a.getMaterial());

            this.createPlanete(root, i, a);
            this.createPlaneteTraces(root, i, t);
        }

        // Add the jpg to the Background
        this.applyBackgroundImage(objRoot, bounds);

        // Add a ambient light
        this.addAmbientLight(objRoot, bounds);

        return objRoot;
    }

    /**
     * Paste the default background image
     * @param root root of the 3d scene
     * @param schedulingBounds bounds
     */
    private void applyBackgroundImage(BranchGroup root, BoundingSphere schedulingBounds) {
        ClassLoader cl = this.getClass().getClassLoader();
        java.net.URL u = cl.getResource("org/objectweb/proactive/examples/nbody/common/fondnbody3d.jpg");
        final Image backGround = getToolkit().getImage(u);
        TextureLoader starsTexture = new TextureLoader(backGround, this);

        Dimension tailleEcran = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        double hauteur = tailleEcran.getHeight();
        double largeur = tailleEcran.getWidth();
        Background stars = new Background(starsTexture.getScaledImage((int) largeur, (int) hauteur));
        //Background stars = new Background (starsTexture.getScaledImage(10000,10000));
        stars.setApplicationBounds(schedulingBounds);
        root.addChild(stars);
    }

    /**
     * Create a new random appearance
     * (INCOMPLETE: missing texture)
     * @return return a random appearance for a 3d shape
     */
    private Appearance createPlaneteAppareance(int index) {
        Appearance planeteAppareance = new Appearance();

        float red = 0.0f;
        float green = 0.0f;
        float blue = 0.0f;

        switch (index % 8) {
            case 0: //red
                red = 1.0f;
                green = 0.0f;
                blue = 0.0f;
                break;
            case 1: //green
                red = 0.0f;
                green = 1.0f;
                blue = 0.0f;
                break;
            case 3: //blue
                red = 0.0f;
                green = 0.0f;
                blue = 1.0f;
                break;
            case 2: //yellow
                red = 1.0f;
                green = 1.0f;
                blue = 0.0f;
                break;
            case 4: //violet
                red = 1.0f;
                green = 0.0f;
                blue = 1.0f;
                break;
            case 5: //light blue
                red = 0.5f;
                green = 0.5f;
                blue = 1.0f;
                break;
            case 6: //pink
                red = 1.0f;
                green = 0.5f;
                blue = 0.5f;
                break;
            case 7: //light green
                red = 0.5f;
                green = 1.0f;
                blue = 0.5f;
                break;
        }

        planeteAppareance.setMaterial(new Material(new Color3f(0, 0, 0), new Color3f(0.2f, 0.2f, 0.2f),
            new Color3f(red, green, blue), new Color3f(0f, 0f, 0f), 128));
        return planeteAppareance;
    }

    /**
     * Create a group componsed of a sphere and different transform group + a hostname label
     * @param root where adding the "sphere group" in 3D scene
     * @param i body to represent
     * @param a appearance of the sphere which represent the body
     */
    private void createPlanete(BranchGroup root, int i, Appearance a) {
        // Creation of the Root Animation Node of the planet
        TransformGroup planeteAnimationGroup = new TransformGroup();
        planeteAnimationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        translationsGroup[i] = planeteAnimationGroup;

        // Allow to Translate the Node of the planet
        Transform3D translationPlanete = new Transform3D();
        translations[i] = translationPlanete;

        // Creating floating hostname label near the planet
        BranchGroup lGroup = new BranchGroup();
        lGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        lGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        lGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        labels[i] = lGroup;

        // Rotation of the planet on itself
        TransformGroup planeteRotationGroup = new TransformGroup();
        planeteRotationGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        Alpha planeteRotationAlpha = new Alpha(-1, 4000);
        RotationInterpolator rotator = new RotationInterpolator(planeteRotationAlpha, planeteRotationGroup);
        rotator.setSchedulingBounds(new BoundingSphere(new Point3d(0, 0, 0), 999999999));
        planetesScalingGroup[i] = planeteRotationGroup;

        // Allow to change the Scale of the sphere representing the planet
        Transform3D planeteScaling = new Transform3D();
        planeteRotationGroup.setTransform(planeteScaling);
        planetesScaling[i] = planeteScaling;

        // Creation of the sphere representing the planete
        Sphere planete = new Sphere(0.01f, Sphere.GENERATE_NORMALS, 80, a);

        // Assembling java3D nodes
        planeteRotationGroup.addChild(planete);
        planeteAnimationGroup.addChild(lGroup);
        planeteAnimationGroup.addChild(planeteRotationGroup);
        root.addChild(planeteAnimationGroup);
    }

    /**
     * Add 3 directional lights (a primary and 2 secondary) to increase 3D sensation
     * @param root where to add the lights
     * @param bounds bounds
     */
    private void addAmbientLight(BranchGroup root, BoundingSphere bounds) {
        // Lumiere principale
        Color3f lightColor1 = new Color3f(1.0f, 1.0f, 1.0f);
        Vector3f lightDirection1 = new Vector3f(2.0f, -3.0f, -6.0f);
        DirectionalLight light1 = new DirectionalLight(lightColor1, lightDirection1);
        light1.setInfluencingBounds(bounds);
        root.addChild(light1);

        // Lumiere de support 1
        Color3f lightColor2 = new Color3f(0.25f, 0.25f, 0.25f);
        Vector3f lightDirection2 = new Vector3f(-2.0f, 3.0f, 6.0f);
        DirectionalLight light2 = new DirectionalLight(lightColor2, lightDirection2);
        light2.setInfluencingBounds(bounds);
        root.addChild(light2);

        // Lumiere de support 2
        Color3f lightColor3 = new Color3f(0.3f, 0.3f, 0.3f);
        Vector3f lightDirection3 = new Vector3f(2.0f, -3.0f, 6.0f);
        DirectionalLight light3 = new DirectionalLight(lightColor3, lightDirection3);
        light3.setInfluencingBounds(bounds);
        root.addChild(light3);
    }

    /**
     * Change the origin
     */
    private void centrer() {
        // Find the new origin
        this.baseX = 0;
        this.baseY = 0;
        this.baseZ = 0;

        for (int i = 0; i < this.nbBodies; i++) {
            this.baseX += this.bodies[i].getX();
            this.baseY += this.bodies[i].getY();
            this.baseZ += this.bodies[i].getZ();
        }

        this.baseX /= this.nbBodies;
        this.baseY /= this.nbBodies;
        this.baseZ /= this.nbBodies;

        //Place the origin in front of the camera sight
        this.resetCamera();
    }

    /**
     * Called to move the group of 3d object representing the planet at (x,y,z)
     * @param i body identification number
     * @param x new x
     * @param y new y
     * @param z new z
     */
    private void movePlanete(int i, double x, double y, double z) {
        translations[i].setTranslation(new Vector3d(x, y, z));
        translationsGroup[i].setTransform(translations[i]);

        if (this.mustDrawTraces) {
            this.drawTrace();
        }
    }

    /**
     * Used to write a text next to the sphere representing a body
     * @param i the body identification
     * @param label the text to write
     */
    private void writeLabel(int i, String label, double diameter) {
        labels[i].removeAllChildren();

        if (label.compareTo("") != 0) {
            // Creating
            BranchGroup bg = new BranchGroup();
            bg.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
            bg.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
            bg.setCapability(BranchGroup.ALLOW_DETACH);

            // Translating & scaling
            TransformGroup labelGroup = new TransformGroup();
            Transform3D scaling = new Transform3D();
            scaling.setTranslation(new Vector3d(0.01 + ((0.005 * diameter) / MASS_RATIO),
                0.01 + ((0.005 * diameter) / MASS_RATIO), 0.0));
            //scaling.setTranslation(new Vector3d(0.15,0.15,0.0));
            scaling.setScale(0.5);
            labelGroup.setTransform(scaling);

            // Assembling and creating the text2D
            labelGroup.addChild(new Text2D(label, new Color3f(1.0f, 1.0f, 1.0f), "Arial", 14, Font.PLAIN));
            bg.addChild(labelGroup);
            bg.compile();

            labels[i].addChild(bg);
        }
    }

    /**
     * Change the diameter of a sphere representing a body
     * @param i the body identification number
     * @param diameter the new diameter
     */
    private void setPlaneteDiameter(int i, int diameter) {
        planetesScaling[i].setScale(((double) diameter) / MASS_RATIO);
        planetesScalingGroup[i].setTransform(planetesScaling[i]);
    }

    /**
     * Called to place ghost bodies
     * (uses a lot of ressources)
     */
    private void drawTrace() {
        for (int i = 0; i < this.nbBodies; i++) {
            for (int j = 0; j < (this.MAX_HISTO_SIZE - 1); j++) {
                double x = this.bodies[i].getX(j + 1) - this.baseX;
                double y = this.bodies[i].getY(j + 1) - this.baseY;
                double z = this.bodies[i].getZ(j + 1) - this.baseZ;

                //this.tracesTransformation[i][j].setTranslation(new Vector3d(x,y,z));
                //this.tracesGroup[i][j].setTransform(tracesTransformation[i][j]);
                Transform3D t = new Transform3D();
                t.setTranslation(new Vector3d(x, y, z));
                this.tracesGroup[i][j].setTransform(t);
                //System.out.println("["+i+"]["+j+"]: ("+x+","+y+","+z+")");
            }
        }
    }

    /**
     * Called to create ghosts of the body
     * Ghost are the ancien position of a body
     * (slows down the java3D tree creation)
     * @param root where to add these ghost
     * @param i body to be ghosted
     * @param a appearance of the ghosts
     */
    private void createPlaneteTraces(BranchGroup root, int i, Appearance a) {
        for (int j = 0; j < (this.MAX_HISTO_SIZE - 1); j++) {
            // Trace Transformation Group
            TransformGroup traceGroup = new TransformGroup();
            traceGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            this.tracesGroup[i][j] = traceGroup;

            // Trace Transformation (place and direction)
            Transform3D traceTransformation = new Transform3D();
            this.tracesTransformation[i][j] = traceTransformation;

            // Cylinder representing a segment
            //Cylinder(float radius, float height, Appearance ap)
            Sphere ghost = new Sphere(0.005f, a);

            //Assembling group
            traceGroup.addChild(ghost);
            //traceGroup.setTransform(traceTransformation);
            root.addChild(traceGroup);
        }
    }

    /**
     * Show or hide the ghosts
     * @param b true: show the ghosts
     * false: hide the ghosts
     */
    private void setTraceVisible(boolean b) {
        if (!b) {
            // Hide Trace
            Transform3D hide = new Transform3D();
            hide.setScale(0.0);
            for (int i = 0; i < this.nbBodies; i++) {
                for (int j = 0; j < (this.MAX_HISTO_SIZE - 1); j++) {
                    this.tracesGroup[i][j].setTransform(hide);
                }
            }
        }
        this.mustDrawTraces = b;
    }

    /**
     * Show or hide the hostname label
     *
     **/
    private void setLabelVisible(boolean b) {
        if (!b) {
            // Hide Label
            for (int i = 0; i < this.nbBodies; i++) {
                this.writeLabel(i, "", this.bodies[i].getDiameter());
            }
        } else {
            // Show Label
            for (int i = 0; i < this.nbBodies; i++) {
                this.writeLabel(i, this.bodies[i].getHostName(), this.bodies[i].getDiameter());
            }
        }
    }

    /**
     * Enable keyboard navigation in the application
     * Uses arrows to
     * <li> Rotate following the X axis</li>
     * <li> Translate following the Z axis</li>
     * Uses PAGEDOWN and PAGEUP to
     * <li> Rotate following the Y axis </li>
     * Use '=' to center view
     * @param root root of the 3D scene
     * @param vpTrans 3D scene first transformation group
     */
    private void enableKeyBoardNavigation(BranchGroup root, TransformGroup vpTrans) {
        // Navigation au clavier
        KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(vpTrans);
        keyNavBeh.setSchedulingBounds(root.getBounds());
        root.addChild(keyNavBeh);
    }

    /**
     * Create a TransformGroup where it is possible to navigate through using the mouse.
     * Move
     * <li>+ Left Click : Rotate</li>
     * <li>+ Right Click: Translate</li>
     * <li>+ Left Click + Right Click: Zoom In/Out</li>
     *
     * MouseWheel:
     * <li>Zoom In/out</li>
     * @param root First Branch Group of the 3D scene
     * @param bounds bounds of the scene
     * @return return a Transform Group when navigation with mouse is possible
     */
    private TransformGroup enableMouseNavigation(BranchGroup root, BoundingSphere bounds) {
        TransformGroup manipulator = new TransformGroup();
        manipulator.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        // Rotation a la souris
        MouseRotate rotateBehavior = new MouseRotate();
        rotateBehavior.setTransformGroup(manipulator);
        rotateBehavior.setSchedulingBounds(bounds);
        manipulator.addChild(rotateBehavior);

        // Translation
        MouseTranslate translateBehavior = new MouseTranslate();
        translateBehavior.setTransformGroup(manipulator);
        translateBehavior.setSchedulingBounds(bounds);
        manipulator.addChild(translateBehavior);

        // Zoom Molette
        MouseWheelZoom wheelZoomBehavior = new MouseWheelZoom();
        wheelZoomBehavior.setTransformGroup(manipulator);
        wheelZoomBehavior.setSchedulingBounds(bounds);
        manipulator.addChild(wheelZoomBehavior);

        // Zoom Souris
        MouseZoom zoomBehavior = new MouseZoom();
        zoomBehavior.setTransformGroup(manipulator);
        zoomBehavior.setSchedulingBounds(bounds);
        manipulator.addChild(zoomBehavior);

        root.addChild(manipulator);
        return manipulator;
    }

    private void resetCamera() {
        Transform3D t = new Transform3D();
        this.transformRoot.setTransform(t);
    }

    //
    // --- INNER CLASSES -------------------------------------------------------
    //
    /**
     * Local Body (kind of proxy)
     */
    private class LocalBody {

        /**
         * Name of machine who host the remote body
         */
        private String hostName;

        /**
         * Array of X positions of the body
         * <li>0 is now</li>
         * <li>1 and more is in past</li>
         */
        private double[] x;

        /**
         * Array of Y positions of the body
         * <li>0 is now</li>
         * <li>1 and more is in past</li>
         */
        private double[] y;

        /**
         * Array of Z positions of the body
         * <li>0 is now</li>
         * <li>1 and more is in past</li>
         */
        private double[] z;

        /**
         * Diameter of the body
         */
        private int diameter;

        /**
         * Number of movements received of the body since creation
         */
        private int movement;

        /**
         * Interval between two position saves
         */
        private int interval;

        // Constructor
        /**
         * Create a new LocalBody
         * default position is (0,0,0);
         */
        public LocalBody() {
            this.interval = 12;
            this.movement = 0;
            this.x = new double[MAX_HISTO_SIZE + 1];
            this.y = new double[MAX_HISTO_SIZE + 1];
            this.z = new double[MAX_HISTO_SIZE + 1];
            for (int i = 0; i < MAX_HISTO_SIZE; i++) {
                this.x[i] = 0.0;
                this.y[i] = 0.0;
                this.z[i] = 0.0;
            }
            this.diameter = 0;
            this.hostName = "";
        }

        /**
         * Create a new LocalBody with specified diameter
         * @param diameter the diameter
         */
        public LocalBody(int diameter) {
            this();
            this.diameter = diameter;
        }

        /**
         * Create a new LocalBody with specified hostName
         * @param hostName the name of the machine which host this body
         */
        public LocalBody(String hostName) {
            this();
            this.hostName = hostName;
        }

        /**
         * Create a new LocalBody with specified diameter and hostName
         * @param hostName the name of the machine which host this body
         * @param diameter The diameter of the new body
         */
        public LocalBody(String hostName, int diameter) {
            this(hostName);
            this.diameter = diameter;
        }

        // Setters
        /**
         * Change the body diameter
         * @param diameter new diameter
         */
        public void setDiameter(int diameter) {
            this.diameter = diameter;
        }

        /**
         * Change the host name of the body
         * @param host the name of the new hosting machine
         */
        public void setHostName(String host) {
            this.hostName = host;
        }

        // getters
        /**
         * get current X position
         * @return the actual x position
         */
        public double getX() {
            return this.x[0];
        }

        /**
         * get current Y position
         * @return the actual z position
         */
        public double getY() {
            return this.y[0];
        }

        /**
         * get current Z position
         * @return the actual z position
         */
        public double getZ() {
            return this.z[0];
        }

        /**
         * get a past (or current) X position
         * @param past when
         * @return the X or 0 if out of history bounds
         */
        public double getX(int past) {
            if (past > MAX_HISTO_SIZE) {
                return 0.0;
            }
            return this.x[past];
        }

        /**
         * get a past (or current) Y position
         * @param past when
         * @return the Y or 0 if out of history bounds
         */
        public double getY(int past) {
            if (past > MAX_HISTO_SIZE) {
                return 0.0;
            }
            return this.y[past];
        }

        /**
         * get a past (or current) Z position
         * @param past when
         * @return the Z or 0 if out of history bounds
         */
        public double getZ(int past) {
            if (past > MAX_HISTO_SIZE) {
                return 0.0;
            }
            return this.z[past];
        }

        /**
         * Return the hostName of the body
         * @return the name of the machine which host the remote body
         */
        public String getHostName() {
            return this.hostName;
        }

        /**
         * Return the diameter of the body
         * @return the diameter
         */
        public int getDiameter() {
            return this.diameter;
        }

        // PUBLIC
        /**
         * Move a body (updating history: one time on 3 in order to speed up)
         * @param x the new X
         * @param y the new Y
         * @param z the new Z
         */
        public void move(double x, double y, double z) {
            if ((movement % this.interval) == 0) {
                for (int i = MAX_HISTO_SIZE - 1; i > 0; i--) {
                    this.x[i] = this.x[i - 1];
                    this.y[i] = this.y[i - 1];
                    this.z[i] = this.z[i - 1];
                }
            }
            this.x[0] = x;
            this.y[0] = y;
            this.z[0] = z;
            this.movement++;
        }

        public void setTraceInterval(int i) {
            if (i > 0) {
                this.interval = i;
            }
        }
    } // LocalBody class

    /**
     * Display Refreshing Thread
     */
    private class RefreshPositionsThread implements Runnable {
        //Constructors
        /**
         * Construct a new display refreshing thread
         */
        RefreshPositionsThread() {
        }

        //
        // implements runnable
        //
        /**
         * Refresh the display every 20ms (50Hz);
         */
        public void run() {
            while (true) {
                try {
                    refreshPositions();
                    //core.refresh();
                    Thread.sleep(20);
                } catch (java.lang.InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Launch the thread
         */
        public void start() {
            new Thread(this).start();
        }
    } // RefreshThread class
}
