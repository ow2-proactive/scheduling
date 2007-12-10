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
package org.objectweb.proactive.examples.webservices.c3dWS;

import java.awt.Button;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Light;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Primitive;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Sphere;
import org.objectweb.proactive.extensions.webservices.WebServices;


/**
 * This class decouples the set of user frames from the set of rendering
 */
public class C3DDispatcher implements org.objectweb.proactive.RunActive,
    Serializable {
    static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /**
     * The array of random colors
     */
    private static Vec[] color = {
            //    new Vec(0,0,0), // Black 
            new Vec(1, 1, 1), // White
            new Vec(0, 0, 0.5), // Navy
            new Vec(0, 0.5, 0), // darkgreen
            new Vec(0.5, 0, 0), // dark red
            new Vec(1, 0.6, 0), // Orange
            new Vec(1, 0.7, 0.8), // Pink
            new Vec(0.8, 0.8, 1) // Lavender
        };
    protected VirtualNode vn;

    /*
     * Stores the users in a hash table
     */
    private Hashtable h_users = new Hashtable();

    /*
     * Timestamp used to estimate rendering time
     */
    private long startTime;

    /**
     * Avg time for a rendering
     */
    private long avgRender = 0;

    /**
     * Number of renderings
     */
    private long totalRender = 0;

    /**
     * Interval stack; each interval holds information regarding its
     * height, width and relative position within the whole image
     */
    private Stack int_stack = new Stack();

    /**
     * Array of rendering engines; each C3DRenderingEngine is a possibly remote,
     * active object
     */
    private C3DRenderingEngine[] engine;

    /**
     * Hashtable of the rendering engines
     */
    private Hashtable h_engines = new Hashtable();

    /**
     * Scene to be rendered; set by the first user frame to register;
     * contains lights, spheres and one view
     */
    private Scene scene;

    /**
     * Number of rendering engines
     */
    private int engines;

    /**
     * Number of intervals, the whole picture should be divided into; this
     * value has got an impact on the performance; it should be in
     * the size of 'three times the number of rendering engines'
     */
    private int intervals;

    /**
     * Width of the rendering image
     */
    private int width;

    /**
     * Height of the rendering image
     */
    private int height;

    /**
     * Height of one rendering interval; provided for convenience only;
     * carries the value of height / intervals
     */
    private int intheight;

    /**
     * Pixel array to store the rendered pixels; used to initialize the
     * image on new-coming user frames, actualized in setPixels
     */
    private int[] pixels;

    /**
     * Number of intervals not yet rendered; used to determine the progress
     * of the asynchronous rendering process; may or may not stay in
     * future versions of live()
     */
    private int i_left = 0;
    private int i_lastuser = 0;
    private TextArea ta_log;
    private List li_users;
    private List li_enginesUsed;
    private List li_enginesAvailable;
    private String s_access_url;
    private String s_hosts;
    private Button b_addEng = new Button("Add engine");
    private Button b_rmEng = new Button("Remove engine");
    private Election election = null;

    /**
     * The average pining time for users
     */
    private long userPing = 0;

    /**
     * The number of User connected from the beginning
     */
    private long userTotal = 0;

    /**
     * The average pining time for rendering engines
     */
    private long enginePing = 0;

    /**
     * True if a rendering is going on [init = true because when 1st
     * user registers, render starts]
     */
    private boolean b_render = false;

    /**
     * ProactiveDescriptor object for the dispatcher
     */
    private ProActiveDescriptor proActiveDescriptor;
    private String[] rendererNodes;
    long previousTime = -1;
    private String url;
    private String urn;

    /**
     * The no-argument Constructor as commanded by ProActive;
     * otherwise unused
     */
    public C3DDispatcher() {
    }

    /**
     * Constructor to call when using XML Descriptor
     */
    public C3DDispatcher(String[] rendererNodes, VirtualNode vn,
        ProActiveDescriptor pad) {
        new C3DDispatcherFrame();
        this.rendererNodes = rendererNodes;
        this.vn = vn;
        this.proActiveDescriptor = pad;
    }

    /**
     * Real Constructor
     */
    public void setUrn(String urn) {
        this.urn = urn;
    }

    /**
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Continues the initialization; called when the first user registers.
     * It creates a set of rendering engines (one on each machine given by
     * the Mapping in Utils) and passes a reference to its own stub to
     * every engine.
     */
    public void init() {
        try {
            C3DDispatcher d = (C3DDispatcher) org.objectweb.proactive.api.PAActiveObject.getStubOnThis();

            /* Initializes the pixel array holding the whole image */

            //Hosts hosts = new Hosts(s_hosts);
            //engines = hosts.getMachines();
            //engines = proActiveDescriptor.getVirtualNodeMappingSize()-1;
            //System.out.println("taille du tableau d'engines "+engines);

            /* Initializes the array to hold the rendering engines */

            //engine = new C3DRenderingEngine[engines];
            Object[] param = { d };

            /* Creates rendering engines */
            //for (int n = 1; n <= engines; n++)
            //{
            //String node = hosts.getNextNode();
            //VirtualNode renderer = proActiveDescriptor.getVirtualNode("Renderer");
            //System.out.println(renderer.getName());
            //JVMNodeProcess jvmNodeProcess = (JVMNodeProcess)renderer.getVirtualMachine().getProcess();
            //System.out.println(jvmNodeProcess.getClassname());
            //renderer.activate();
            //we have to wait for the creation of the nodes
            //Node[] nodeTab = renderer.getNodes();
            engines = rendererNodes.length;
            engine = new C3DRenderingEngine[engines];

            for (int i = 0; i < rendererNodes.length; i++) {
                C3DRenderingEngine tmp = (C3DRenderingEngine) PAActiveObject.newActive("org.objectweb.proactive.examples.webservices.c3dWS.C3DRenderingEngine",
                        param, rendererNodes[i]);

                //String nodeURL = nodeTab[i].getNodeInformation().getURL();
                log("New rendering engine " + i + " created at " +
                    rendererNodes[i]);

                // always have a renderer used when launching the program
                if (i == 1) {
                    li_enginesUsed.add(rendererNodes[i].toString());
                } else {
                    li_enginesAvailable.add(rendererNodes[i].toString());
                }

                // adds the engine in the hashtable
                h_engines.put(rendererNodes[i], tmp);
            }

            //String nodeURL = node.getNodeInformation().getURL();
            //				C3DRenderingEngine tmp =
            //					(
            //						C3DRenderingEngine)ProActive
            //							.newActive(
            //						"org.objectweb.proactive.examples.c3d.C3DRenderingEngine",
            //						param,
            //						node);
            //				log("New rendering engine " + n + " created at " + nodeURL);
            //				
            //				// always have a renderer used when launching the program
            //				if (n == 1)
            //					li_enginesUsed.add(nodeURL.toString());
            //				else
            //					li_enginesAvailable.add(nodeURL.toString());
            //
            //				// adds the engine in the hashtable
            //				h_engines.put(nodeURL, tmp);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * Appends message to the end of the list
     */
    public void log(String s_message) {
        ta_log.append(s_message + "\n");
    }

    /**
     * Does the rendering; creates the interval stack, registers the current
     * scene with the engines, assigns one initial interval to each engine
     * and triggers the calculation
     */
    private void render() {
        // Checks there are some engines usde
        if (li_enginesUsed.getItems().length == 0) {
            showMessageAll("No engines ... contact the dispatcher");

            return;
        }

        // Toggles the rendering flag..
        b_render = true;

        // Benchmarking stuff...
        startTime = System.currentTimeMillis();

        Interval interval;
        String[] keys = li_enginesUsed.getItems();
        engines = keys.length;

        log("Creating " + intervals + " intervals");
        intervals = 3 * keys.length;
        intheight = height / intervals;

        /* Sets the number of intervals not yet calculated to 'all' */
        i_left = intervals;

        /* Creates the interval stack */
        int_stack = new Stack();

        for (int i = 0; i < intervals; i++) {
            //      log("Creating inter : "+i);
            Interval newint = new Interval(i, width, height, i * intheight,
                    (i + 1) * intheight, intervals);
            int_stack.push(newint);
        }

        engine = null;
        engine = new C3DRenderingEngine[keys.length];

        C3DRenderingEngine tmp;

        if (keys != null) {
            for (int e = 0; e < keys.length; e++) {

                /* Assigns one initial interval to each engine */
                if (!int_stack.empty()) {
                    interval = (Interval) int_stack.pop();

                    tmp = (C3DRenderingEngine) h_engines.get(keys[e]);

                    if (tmp != null) {
                        tmp.setScene(scene);

                        engine[e] = tmp;

                        /* Triggers the calculation of this interval on engine e */
                        tmp.render(e, interval);

                        log("Interval " + interval.number +
                            " assigned to engine " + keys[e] + "[" + e + "]");
                    } else {
                        log("Failed to assign an interval to engine " +
                            keys[e]);
                        h_engines.get(keys[e]);
                    }
                }
            }
        } else {
            b_render = false;
        }
    }

    /**
     *
     * @param from
     * @param num
     * @return
     */
    public int[] getPixels(int from, int num) {
        int[] p = new int[num];
        System.arraycopy(pixels, from, p, 0, num);

        return p;
    }

    /**
     * @param num
     * @re
     * turn
     */
    public int[] getPicture(int num) {
        int[] pix = new int[num * num];

        for (int y = 0; y < num; y++) {
            int[] tmp = getPixels(y * num, num);
            System.arraycopy(tmp, 0, pix, y * num, num);
        }

        return pix;
    }

    /**
     *
     * @return
     */
    public int getPixelMax() {
        return pixels.length;
    }

    /**
     * Forwards the newly calculated pixels from the rendering engine to the
     * redirectors (i.e. the consumers). This method is called directly by
     * the rendering engines via their reference to this C3DDispatcher.
     *
     * @param newpix the newly calculated pixels as int[]
     * @param interval the interval, the pixels belong to (width, height ...)
     * @param engine_number number of the engine, that has calculated this
     *        interval; this value is used to assign the next interval to
     *        this same engine
     */
    public void setPixels(int[] newpix, Interval interval, int engine_number) {
        //  public void setPixels(int[] newpix, Interval interval, C3DRenderingEngine currentEngine) {
        long elapsed;

        //System.out.println("SET PIXELS");

        /* Delivers the new pixels to all user frames */
        for (Enumeration e = h_users.elements(); e.hasMoreElements();) {
            User user = (User) e.nextElement();

            //System.out.println("classe user = " + user.getClass().getName());
            user.setPixels(newpix, interval);
        }

        /* Stores the newly rendered interval in <code>pixels</code>; this
         * is later used to initialize the images of newcoming consumers */
        System.arraycopy(newpix, 0, pixels, interval.width * interval.yfrom,
            newpix.length);

        /* Decreases the counter of not yet rendered intervals */
        i_left--;

        /* Has the next interval rendered by the same engine */
        Interval nextinterval;

        if (!int_stack.empty()) {
            nextinterval = (Interval) int_stack.pop();
            log("Next interval [" + nextinterval.number +
                "] assigned to engine " + engine_number);

            // new NextInterval(engine_number, engine[engine_number], nextinterval);
            engine[engine_number].render(engine_number, nextinterval);

            //      currentEngine.render(0, nextinterval);
        } else if (i_left == 0) {
            // Debugging: estimates the number of milliseconds elapsed
            elapsed = System.currentTimeMillis() - startTime;

            totalRender++;
            avgRender += ((elapsed - avgRender) / totalRender);

            showMessageAll("All intervals rendered in " + elapsed + " ms");

            if (previousTime != -1) {
                showMessageAll("Speed Up : " +
                    (java.lang.Math.rint(
                        ((double) previousTime / elapsed) * 1000) / 1000) +
                    "\n");
            } else {
                showMessageAll("");
            }

            previousTime = elapsed;

            b_render = false;
        }
    }

    /**
     * Rotates all the objects (spheres, for the moment) by <code>angle</code>
     * around their y axis; re-renders the image afterwards
     *
     * @param angle the angle to rotate the objects in radians, a positive
     *        value means a rotation to the 'right'
     */
    private void rotateSceneY(double angle) {
        int objects = scene.getObjects();
        Sphere o;

        /* on every object ... */
        for (int i = 0; i < objects; i++) {
            o = (Sphere) scene.getObject(i);

            Vec c = o.getCenter();
            double phi = Math.atan2(c.z, c.x);
            double l = Math.sqrt((c.x * c.x) + (c.z * c.z));

            /* ... perform the standard rotation math */
            c.x = l * Math.cos(phi + angle);
            c.z = l * Math.sin(phi + angle);
            o.setCenter(c);
            scene.setObject(o, i);
        }

        /* re-renders the image to reflect the rotation */
        render();
    }

    /**
     * Rotates all the objects (spheres, for the moment) by <code>angle</code>
     * around their x axis; re-renders the image afterwards
     *
     * @param angle the angle to rotate the objects in radians, a positive
     *        value means a rotation to the 'right'
     */
    private void rotateSceneX(double angle) {
        int objects = scene.getObjects();
        Sphere o;

        /* on every object ... */
        for (int i = 0; i < objects; i++) {
            o = (Sphere) scene.getObject(i);

            Vec c = o.getCenter();
            double phi = Math.atan2(c.z, c.y);
            double l = Math.sqrt((c.y * c.y) + (c.z * c.z));

            /* ... perform the standard rotation math */
            c.y = l * Math.cos(phi + angle);
            c.z = l * Math.sin(phi + angle);
            o.setCenter(c);
            scene.setObject(o, i);
        }

        /* re-renders the image to reflect the rotation */
        render();
    }

    /**
     * Rotates all the objects (spheres, for the moment) by <code>angle</code>
     * around their x axis; re-renders the image afterwards
     *
     * @param angle the angle to rotate the objects in radians, a positive
     *        value means a rotation to the 'right'
     */
    private void rotateSceneZ(double angle) {
        int objects = scene.getObjects();
        Sphere o;

        /* on every object ... */
        for (int i = 0; i < objects; i++) {
            o = (Sphere) scene.getObject(i);

            Vec c = o.getCenter();
            double phi = Math.atan2(c.x, c.y);
            double l = Math.sqrt((c.y * c.y) + (c.x * c.x));

            /* ... perform the standard rotation math */
            c.y = l * Math.cos(phi + angle);
            c.x = l * Math.sin(phi + angle);
            o.setCenter(c);
            scene.setObject(o, i);
        }

        /* re-renders the image to reflect the rotation */
        render();
    }

    /**
     * Rotates the objects in the rendering scene by pi/4 to the left
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void rotateLeft(int i) {
        rotateSceneY(-Math.PI / 4);
    }

    /**
     * Rotates the objects in the rendering scene by pi/4 to the right
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void rotateRight(int i) {
        rotateSceneY(Math.PI / 4);
    }

    /**
     * Rotates the objects in the rendering scene by pi/4 to the left
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void rotateUp(int i) {
        rotateSceneX(Math.PI / 4);
    }

    /**
     * Rotates the objects in the rendering scene by pi/4 down
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void rotateDown(int i) {
        rotateSceneX(-Math.PI / 4);
    }

    /**
     * Spins clockwise
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void spinClock(int i_user) {
        rotateSceneZ(Math.PI / 4);
    }

    /**
     * Spins the scene un-clockwise
     *
     * @param i_user number of the user requesting this rotation; this
     *        value used for the synchronization and notification of several
     *        users
     */
    public void spinUnclock(int i_user) {
        rotateSceneZ(-Math.PI / 4);
    }

    /*
       public void changeColorAll() {
           logger.info("changeColorAll()");
           for (Enumeration e = h_users.elements(); e.hasMoreElements();)
               ((User) e.nextElement()).getObject().getUserFrame().getB_left()
                .setBackground(Color.yellow);
       }
     */

    /**
     * ProActive object life routine
     * To be remdelled..
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        //registerDispatcher(body.getNodeURL());

        /* Creates the rendering engines */
        init();

        org.objectweb.proactive.core.body.request.BlockingRequestQueue requestQueue =
            body.getRequestQueue();

        /* Loops over lifetime */
        while (body.isActive()) {

            /* Waits on any method call */
            Request r = requestQueue.blockingRemoveOldest();
            String methodName = r.getMethodName();

            if (methodName.startsWith("rotate") ||
                    methodName.startsWith("spin")) {
                processRotate(body, methodName, r);
            } else {
                if (!Election.isRunning()) {
                    // No election and the method != up,down,left,right
                    body.serve(r);
                } else if (methodName.equals("addSphere")) {
                    // There is an election and addsphere comes..
                    // nothing happens...
                    showMessageAll(
                        "Cannot add spheres while an election is running");
                } else {
                    // THERE IS a running election and the method name is not left or right..
                    body.serve(r);
                }
            }
        }

        //}
    }

    /**
     * Processes the requests which are relative to rotations
     * @param body The body of the active object
     * @param methodName The methodname <font size="-1">Not reconputed in order to gain time</font>
     * @param r The request object
     */
    public void processRotate(org.objectweb.proactive.Body body,
        String methodName, Request r) {
        int i_user = 0;

        try {
            i_user = ((Integer) r.getParameter(0)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (methodName.equals("rotateLeft")) {
            // A user wants to rotate left
            if (Election.isRunning()) {
                // There is an election
                int i_votes = Election.vote(i_user, new Integer(Election.LEFT));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // There is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being rotated left");
                    body.serve(r);
                } else {
                    election = new Election(i_user, new Integer(Election.LEFT),
                            this);
                }
            }
        } else if (methodName.equals("rotateRight")) {
            // A user wants to go right
            if (Election.isRunning()) {
                // there is an election
                int i_votes = Election.vote(i_user, new Integer(Election.RIGHT));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // there is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being rotated right");
                    body.serve(r);
                } else {
                    election = new Election(i_user,
                            new Integer(Election.RIGHT), this);
                }
            }
        } else if (methodName.equals("rotateUp")) {
            // A user wants to go up
            if (Election.isRunning()) {
                // There is an election
                int i_votes = Election.vote(i_user, new Integer(Election.UP));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // There is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being rotated up");
                    body.serve(r);
                } else {
                    election = new Election(i_user, new Integer(Election.UP),
                            this);
                }
            }
        } else if (methodName.equals("rotateDown")) {
            // An user wants to go down
            if (Election.isRunning()) {
                // there is an election
                int i_votes = Election.vote(i_user, new Integer(Election.DOWN));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // there is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being rotated down");
                    body.serve(r);
                } else {
                    election = new Election(i_user, new Integer(Election.DOWN),
                            this);
                }
            }
        } else if (methodName.equals("spinClock")) {
            // A user wants to spin clockwise
            if (Election.isRunning()) {
                // There is an election
                int i_votes = Election.vote(i_user,
                        new Integer(Election.CLOCKWISE));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // There is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being spinned right");
                    body.serve(r);
                } else {
                    election = new Election(i_user,
                            new Integer(Election.CLOCKWISE), this);
                }
            }
        } else if (methodName.equals("spinUnclock")) {
            // An user wants to spin unclock
            if (Election.isRunning()) {
                // there is an election
                int i_votes = Election.vote(i_user,
                        new Integer(Election.UNCLOCKWISE));

                if (i_votes == h_users.size()) {
                    election.finish();
                }
            } else {
                // there is no election
                if (i_left > 0) {
                    showMessage(i_user, "Rendering in progress, request invalid");
                } else if (h_users.size() == 1) {
                    showMessage(i_user, "Scene is being spinned left");
                    body.serve(r);
                } else {
                    election = new Election(i_user,
                            new Integer(Election.UNCLOCKWISE), this);
                }
            }
        }
    }

    /**
     * Displays a message at a consumer; convenience method
     *
     * @param i_user number of the user frame to show the message on
     * @param message the message to display
     */
    public void showMessage(int i_user, String s_message) {
        Object user = h_users.get(new Integer(i_user));

        if (user instanceof User) {
            ((User) user).showMessage(s_message);
        }
    }

    /**
     * Displays a dialog message at a consumer; convenience method
     *
     * @param i_user number of the user frame to show the message on
     * @param message the message to display
     */
    public void showDialog(int i_user, String subject, String s_message) {
        ((User) h_users.get(new Integer(i_user))).dialogMessage(subject,
            s_message);
    }

    /**
     *   Displays a message sent by another user
     */
    public void showUserMessage(int i_user, String s_message) {
        ((User) h_users.get(new Integer(i_user))).showUserMessage(s_message);
    }

    /**
     * Sends  message to everybody
     * @param s_message the message to display
     */
    public void showMessageAll(String s_message) {
        log(s_message);

        for (Enumeration e = h_users.elements(); e.hasMoreElements();) {
            ((User) e.nextElement()).showMessage(s_message);
        }
    }

    /* public void setInitialColor() {
       for (Enumeration e = h_users.elements() ; e.hasMoreElements() ;) {
         ((User) e.nextElement()).getObject().getUserFrame().getB_clock().setBackground(Color.gray);
       }
       }*/

    /**
     * Sends a message to everybody except the user
     * @param i_user The number of the user
     * @param s_message The message
     */
    public void showMessageExcept(int i_user, String s_message) {
        log(s_message);

        int i;

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            i = ((Integer) e.nextElement()).intValue();

            if (i != i_user) {
                showMessage(i, s_message);
            }
        }
    }

    /**
     * Sends a dialog message to everybody except the user
     * @param i_user The number of the user
     * @param s_message The message
     */
    public void showDialogExcept(int i_user, String subject, String s_message) {
        int i;

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            i = ((Integer) e.nextElement()).intValue();

            if (i != i_user) {
                showDialog(i, subject, s_message);
            }
        }
    }

    /**
     * Sends a message to everybody except the user
     * @param i_user The number of the user
     * @param s_message The message
     */
    public void showUserMessageExcept(int i_user, String s_message) {
        log(s_message);

        int i;

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            i = ((Integer) e.nextElement()).intValue();

            if (i != i_user) {
                showUserMessage(i, s_message);
            }
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public int registerWSUser(String name, String url) {

        /* Adds this User to the list */
        User newUser = new WSUser(name, url);
        h_users.put(new Integer(i_lastuser), newUser);
        li_users.add(name + " (" + i_lastuser + ")");

        // Updates the remote h_users
        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            int i = ((Integer) e.nextElement()).intValue();
            User oldUser = ((User) h_users.get(new Integer(i)));

            if (i != i_lastuser) {
                // Inform the old users
                oldUser.informNewUser(i_lastuser, name);

                // Inform the new user
                newUser.informNewUser(i, oldUser.getName());
            }
        }

        if (h_users.size() == 1) {
            scene = createScene();
            width = 270;
            height = 270;
            pixels = new int[width * height];
            intervals = 3 * engines;
            intheight = height / intervals;

            int max;

            /* Creates the intervals, starts the calculation with initial  intervals */
            render();
        } else {

            /* Initializes the image of the new-coming consumer */
            Interval inter = new Interval(0, width, height, 0, height, 1);
            render();
            newUser.setPixels(pixels, inter);
        }

        return i_lastuser++;
    }

    /**
     * Instanciates a new active C3DDispatcher on the local machine
     *
     * @param argv Name of the hosts file
     */
    public static void main(String[] argv) throws NodeException {
        ProActiveDescriptor proActiveDescriptor = null;
        ProActiveConfiguration.load();

        String hostWS = "localhost:8080";

        //System.out.println("host = " + hostWS);
        if (argv.length == 2) {
            hostWS = argv[1];
        }

        Node node = null;

        try {
            proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" +
                    argv[0]);
            proActiveDescriptor.activateMappings();

            //Thread.sleep(20000);		
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Pb in main");
        }

        //Object param[] = new Object[]{ proActiveDescriptor };
        VirtualNode dispatcher = proActiveDescriptor.getVirtualNode(
                "Dispatcher");

        VirtualNode renderer = proActiveDescriptor.getVirtualNode("Renderer");
        String[] rendererNodes = renderer.getNodesURL();
        Object[] param = new Object[] {
                rendererNodes, dispatcher, proActiveDescriptor
            };

        node = dispatcher.getNode();

        try {
            C3DDispatcher c3dd = (C3DDispatcher) PAActiveObject.newActive("org.objectweb.proactive.examples.webservices.c3dWS.C3DDispatcher",
                    param, node);

            //Expose the dispatcher as a web service
            String[] methods = {
                    "rotateRight", "getPicture", "rotateLeft", "rotateUp",
                    "rotateDown", "getPixels", "getPixelMax", "waitForImage",
                    "spinClock", "spinUnclock", "addRandomSphere",
                    "resetSceneWS", "registerWSUser", "unregisterWSUser"
                };

            String urn = "C3DDispatcher";
            String url = "http://" + hostWS;
            c3dd.setUrl(url);
            c3dd.setUrn(urn);
            WebServices.exposeAsWebService(c3dd, url, urn, methods);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // System.exit(0);
    }

    public void addRandomSphere() {
        double x;
        double y;
        double z;
        double r;
        x = (Math.random() - 0.5) * 20.0;
        y = (Math.random() - 0.5) * 20.0;
        z = (Math.random() - 0.5) * 20.0;
        r = (Math.random()) * 10.0;

        Sphere sphere = new Sphere(new Vec(x, y, z), r);

        // Should maybe check that the sphere isn't in another one???
        // Color, reflection and so...
        // sets the color to a specific color chosen arbitrary in the color[] array (which btw is a Vec array..)
        // Hm.. well, I'm really sorry for the person who'll maintain this code...
        // I _DO_ apologize for the following line.
        sphere.surf.color = (color[Math.round((float) (Math.random() * (color.length -
                1)))]);

        // Voodoo stuff...
        sphere.surf.kd = Math.random();
        sphere.surf.ks = Math.random();
        sphere.surf.shine = Math.random() * 20.0;

        //sphere.surf.kt=Math.random();
        //sphere.surf.ior=Math.random();
        addSphere(sphere);
    }

    /**
     * The pinging function called by <code>C3DUser and C3DRenderingEngine</code>
     * to get the avg. pinging time
     */
    public int ping() {
        return 0;
    }

    /**
     * Registers a consumer with this C3DDispatcher; called by the user frames
     * after initialization.
     *
     * @param t the User as entry point for subsequent method
     *          calls, e.g. setPixels
     * @param scene the Scene the consumer wants to have rendered, only the
     *        first Scene is accepted
     * @return number (counter) of the consumer at this C3DDispatcher, used to
     *         distinguish consumers in the display and for subsequent
     *         method calls, e.g. rotateRight
     */
    public int registerConsumer(C3DUser c3duser, Scene newscene, String s_name) {
        //System.out.println("User wan in: " + c3duser.getWidth());
        c3duser.log("-> Remote call-back: dispatcher found, user registered");

        log("New user " + s_name + "(" + i_lastuser + ") has joined");

        User newUser = new PAUser(s_name, c3duser);

        /* Adds this User to the list */
        h_users.put(new Integer(i_lastuser), newUser);
        li_users.add(s_name + " (" + i_lastuser + ")");

        /**
         * Informs the other users of the new user
         */

        // Updates the remote h_users
        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            int i = ((Integer) e.nextElement()).intValue();
            User oldUser = ((User) h_users.get(new Integer(i)));

            if (i != i_lastuser) {
                // Inform the old users
                oldUser.informNewUser(i_lastuser, s_name);

                // Inform the new user
                newUser.informNewUser(i, oldUser.getName());
            }
        }

        /**
         * Pinging the new User
         */
        userTotal++;

        long elapsed;
        long start = System.currentTimeMillis();

        // Forces the use of a synchroneous call by using future
        if (c3duser.ping() == 0) {
            // avg = avg + ((new-avg)/times)
            elapsed = System.currentTimeMillis() - start;
            userPing += ((elapsed - userPing) / userTotal);
            log("Pinging user " + s_name + " in " + elapsed + " ms. Average: " +
                userPing);
        }

        /**
         * Does some initialization when the first consumer registers
         */
        if (h_users.size() == 1) {
            /* Sets the scene to the consumers wish */
            scene = newscene;

            /* Initializes image properties */
            width = c3duser.getWidth().intValue();
            height = c3duser.getHeight().intValue();
            pixels = new int[width * height];
            intervals = 3 * engines;
            intheight = height / intervals;

            int max;

            if (s_name.compareTo("Benchmarking bot") == 0) {
                max = 50;
            } else {
                max = 1;
            }

            // Performs pings on the remote engines
            int engPingTotal = 0;

            for (int n = 0; n < engines; n++) {
                for (int i = 1; i < max; i++) {
                    engPingTotal++;
                    start = System.currentTimeMillis();

                    if (engine[n].ping() == 1) {
                        elapsed = System.currentTimeMillis() - start;
                        enginePing += ((elapsed - enginePing) / engPingTotal);
                    }
                }
            }

            /* Creates the intervals, starts the calculation with initial  intervals */
            render();
        } else {

            /* Initializes the image of the new-coming consumer */
            Interval inter = new Interval(0, width, height, 0, height, 1);
            c3duser.setPixels(pixels, inter);
        }

        return i_lastuser++;
    }

    public void registerMigratedUser(int userNumber) {
        User user = (User) h_users.get(new Integer(userNumber));
        log("User " + user.getName() + "(" + userNumber + ") has migrated ");

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            int i = ((Integer) e.nextElement()).intValue();
            User oldUser = ((User) h_users.get(new Integer(i)));

            if (i != userNumber) {
                // Inform users
                user.informNewUser(i, oldUser.getName());
            }
        }

        /* Initializes the image of the migrated consumer */
        Interval inter = new Interval(0, width, height, 0, height, 1);
        user.setPixels(pixels, inter);
    }

    /**
     * Reset the scene
     */
    public void resetScene(Scene s) {
        if (!int_stack.isEmpty()) {
            showMessageAll("Cannot reset scene while rendering");

            return;
        } else {
            scene = s;
            showMessageAll("The scene has been reseted. Rendering...");
            render();
        }
    }

    //Method for WS
    public void resetSceneWS() {
        this.scene = createScene();
        render();
    }

    //WS convenience

    /**
     * Create and initialize the scene for the rendering picture.
     * @return The scene just created
     */
    private Scene createScene() {
        int x = 0;
        int y = 0;

        Scene scene = new Scene();

        /* Creates three objects (spheres) */
        Primitive p = new Sphere(new Vec(10, -5.77, 0), 7);
        p.setColor(1.0, 0.0, 0.0);
        p.surf.shine = 14.0;
        p.surf.kd = 0.7;
        p.surf.ks = 0.3;
        p.surf.ior = 0.3;
        scene.addObject(p);

        p = new Sphere(new Vec(0, 11.55, 0), 7);
        p.setColor(0.0, 1.0, 0.0);
        p.surf.shine = 14.0;
        p.surf.kd = 0.7;
        p.surf.ks = 0.3;
        scene.addObject(p);

        p = new Sphere(new Vec(-10, -5.77, 0), 7);
        p.setColor(0.0, 0.0, 1.0);
        p.surf.shine = 14.0;
        p.surf.kd = 0.7;
        p.surf.ks = 0.3;
        scene.addObject(p);

        /* Creates five lights for the scene */
        scene.addLight(new Light(100, 100, -50, 1.0));
        scene.addLight(new Light(-100, 100, -50, 1.0));
        scene.addLight(new Light(100, -100, -50, 1.0));

        //    scene.addLight(new Light(-100,-100,-50, 1.0));
        //    scene.addLight(new Light(200, 200, 0, 1.0));
        //    scene.addLight(new Light(0,0, 0, 1.0));

        /* Creates a View (viewing point) for the rendering scene */
        View v = new View();

        //    v.from = new Vec(x, y, -40);
        v.from = new Vec(x, y, -30);

        //    v.from = new Vec(x, y, -5);
        v.at = new Vec(x, y, -15);
        v.up = new Vec(0, 1, 0);
        v.angle = (35.0 * 3.14159265) / 180.0;
        v.aspect = 1.0; /* 4.0/3.0; */
        v.dist = 1.0;
        scene.addView(v);

        return scene;
    }

    /**
     * Removes a consumer from the list of registered consumers
     *
     * @param number number of the consumer to be removed
     */
    public void unregisterConsumer(int number) {
        showMessageExcept(number, "User " + nameOfUser(number) + " left");

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            int i = ((Integer) e.nextElement()).intValue();

            if (i != number) {
                User user = ((User) h_users.get(new Integer(i)));

                // Inform all user except the user that left
                user.informUserLeft(nameOfUser(number));
            }
        }

        try {
            li_users.remove(nameOfUser(number) + " (" + number + ")");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        h_users.remove(new Integer(number));

        if (h_users.isEmpty()) {
            //h_engines.clear();
            //li_enginesUsed.removeAll();
            //li_enginesAvailable.removeAll();
            int_stack.removeAllElements();
            i_lastuser = 0;
        }
    }

    public void unregisterWSUser(String name, String urlUser) {

        /* search the user */
        User removedUser;
        int number = 0;

        for (Enumeration e = h_users.keys(); e.hasMoreElements();) {
            number = ((Integer) e.nextElement()).intValue();

            User user = ((User) h_users.get(new Integer(number)));

            if (user.getName().equals(name)) {
                if (user instanceof WSUser &&
                        ((WSUser) user).getUrl().equals(urlUser)) {
                    removedUser = user;
                }

                // System.out.println("Unregister WSUser : " + name + "--" +
                //    urlUser);
                //System.out.println("User = " + number);
                unregisterConsumer(number);

                break;
            }
        }
    }

    /**
     * Return OS of the machine, this C3DDispatcher is running on
     *
     * @return name and version of the OS
     */
    public String getOSString() {
        return System.getProperty("os.name") + " " +
        System.getProperty("os.version");
    }

    public int getIntervals() {
        return intervals;
    }

    public String getUserList() {
        StringBuffer s_list = new StringBuffer();

        for (Enumeration e = h_users.elements(); e.hasMoreElements();) {
            s_list.append("  " + ((User) e.nextElement()).getName() + "\n");
        }

        return s_list.toString();
    }

    String nameOfUser(int i_user) {
        return ((User) h_users.get(new Integer(i_user))).getName();
    }

    private void trace(String s_message) {
        logger.info("C3DDispatcher: " + s_message);
    }

    public void addSphere(Sphere s) {
        // Can only add sphere if there is no rendering going on
        if (!int_stack.isEmpty()) {
            showMessageAll("Cannot add spheres while rendering");

            return;
        } else {
            scene.addObject((Primitive) s);
            showMessageAll("A Sphere has been added\nX:" +
                Math.floor(s.getCenter().x) + " Y:" +
                Math.floor(s.getCenter().y) + " Z:" +
                Math.floor(s.getCenter().y));
            render();
            log("Scene now contains " + scene.getObjects() + " spheres");
        }
    }

    /**
     * @@ experimental benchmark function...
     * Should maybe dump the results into a file???
     * Ascii / cvs format ???
     */
    public void doBenchmarks() {
        FileWriter bench;
        long start;
        long elapsed;

        for (int n = 0; n < engines; n++) {
            // Pings each engine
            start = System.currentTimeMillis();

            if (engine[n].ping() == 0) {
                elapsed = System.currentTimeMillis() - start;
                enginePing += ((elapsed - enginePing) / (n + 1));
                log("Pinging engine " + n + "in " + elapsed + " ms. Average: " +
                    enginePing);
            }
        }

        try {
            log("Creating file");

            bench = new FileWriter("bench" + engines + ".txt");

            bench.write("#Test file for bench generation\n");
            bench.write(new Date().toString());
            bench.write("#Total number of users:\n" + userTotal);
            bench.write("\n#Avg. rd-trip time:\n" + userPing);
            bench.write("\n#Number of engines:\n" + engine.length);
            bench.write("\n#Avg rd-trip time:\n" + enginePing);
            bench.write("\n#Number of renderings:\n" + totalRender);
            bench.write("\n#Avg rendering time:\n" + avgRender);
            bench.write('\n');
            bench.close();
            log("Closing");
        } catch (IOException e) {
            log(e.getMessage());
        }

        // Popular demand : plot file
        try {
            RandomAccessFile plot = new RandomAccessFile("papdc.plot", "rw");

            // Goes to EOF
            plot.seek(plot.length());
            plot.writeChars("#" + new Date().toString() + "\n");
            plot.writeChars("" + engines + "\t" + avgRender + "\n");
            plot.close();
        } catch (Exception ex) {
            return;
        }
    }

    class C3DDispatcherFrame extends Frame implements ActionListener {
        private MenuItem mi_exit;
        private MenuItem mi_clear;
        private MenuItem mi_benchmark;
        private Font f_standard = new Font("SansSerif", Font.PLAIN, 12);
        private int i_top;

        public C3DDispatcherFrame() {
            super("Collaborative 3D Environment - Dispatcher");

            setBackground(Color.lightGray);
            addWindowListener(new MyWindowListener());
            setFont(f_standard);

            MenuBar mb = new MenuBar();
            Menu m_file = new Menu("File", false);
            m_file.setFont(f_standard);
            mi_exit = new MenuItem("Exit");
            mi_clear = new MenuItem("Clear log");
            mi_benchmark = new MenuItem("Benchmarks results");

            // Font
            mi_clear.setFont(f_standard);
            mi_exit.setFont(f_standard);
            mi_benchmark.setFont(f_standard);

            // Actions
            mi_exit.addActionListener(this);
            mi_clear.addActionListener(this);
            mi_benchmark.addActionListener(this);

            m_file.add(mi_clear);
            m_file.addSeparator();
            m_file.add(mi_exit);
            m_file.addSeparator();
            m_file.add(mi_benchmark);
            mb.add(m_file);
            setMenuBar(mb);

            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            setLayout(gridbag);

            Label l_header = new Label("C3D Dispatcher", Label.CENTER);
            l_header.setFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 18));
            c.insets = new Insets(12, 10, 0, 10);
            c.weightx = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.weighty = 0.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(l_header, c);
            add(l_header);

            String s_localhost = "";

            try {
                s_localhost = URIBuilder.getLocalAddress().getCanonicalHostName();
            } catch (UnknownHostException e) {
                s_localhost = "unknown!";
            }

            Label l_machine = new Label("on machine: " + s_localhost + " (" +
                    System.getProperty("os.name") + " " +
                    System.getProperty("os.version") + ")");
            c.insets = new Insets(0, 10, 5, 10);
            c.gridy = 1;
            gridbag.setConstraints(l_machine, c);
            add(l_machine);

            ta_log = new TextArea(10, 35);
            ta_log.setEditable(false);

            c.anchor = GridBagConstraints.CENTER;
            c.weighty = 1.0;
            c.gridy++;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(ta_log, c);
            add(ta_log);

            Label l_users = new Label("List of current users:", Label.LEFT);
            c.gridy++;
            c.insets = new Insets(5, 10, 0, 10);
            c.anchor = GridBagConstraints.WEST;
            gridbag.setConstraints(l_users, c);
            add(l_users);

            li_users = new List(5);
            c.gridy++;
            gridbag.setConstraints(li_users, c);
            add(li_users);

            //-----------------------------
            // Panel for engines
            Panel p_eng = new Panel();
            GridBagLayout p_gl = new GridBagLayout();
            GridBagConstraints pc = new GridBagConstraints();

            p_eng.setLayout(p_gl);
            c.gridy++;
            c.fill = GridBagConstraints.BOTH;
            c.gridwidth = GridBagConstraints.REMAINDER;

            //c.weightx=1.0;
            //      p_eng.setBackground(Color.blue);
            gridbag.setConstraints(p_eng, c);
            add(p_eng);

            Label l_engines = new Label("Available engines:", Label.CENTER);

            //      pc.anchor=pc.CENTER;
            //      pc.anchor=pc.center;
            pc.weightx = 1.0;
            pc.weighty = 1.0;
            pc.gridx = 0;
            pc.gridy = 0;
            pc.insets = new Insets(5, 5, 5, 5);
            pc.gridwidth = GridBagConstraints.RELATIVE;
            p_gl.setConstraints(l_engines, pc);
            p_eng.add(l_engines);

            Label l_used = new Label("Engines used:", Label.CENTER);
            pc.gridx = 1;
            pc.gridwidth = GridBagConstraints.REMAINDER;
            p_gl.setConstraints(l_used, pc);
            p_eng.add(l_used);

            li_enginesAvailable = new List(5);
            li_enginesAvailable.setMultipleMode(true);
            pc.fill = GridBagConstraints.BOTH;
            pc.gridy = 1;
            pc.gridx = 0;
            pc.gridwidth = GridBagConstraints.RELATIVE;
            p_gl.setConstraints(li_enginesAvailable, pc);
            p_eng.add(li_enginesAvailable);

            li_enginesUsed = new List(5);
            li_enginesUsed.setMultipleMode(true);
            pc.gridx = 1;
            pc.gridwidth = GridBagConstraints.REMAINDER;
            p_gl.setConstraints(li_enginesUsed, pc);
            p_eng.add(li_enginesUsed);

            b_addEng.addActionListener(this);
            pc.fill = GridBagConstraints.NONE;
            pc.gridx = 0;
            pc.gridy = 2;
            pc.gridwidth = GridBagConstraints.RELATIVE;
            p_gl.setConstraints(b_addEng, pc);
            p_eng.add(b_addEng);

            b_rmEng.addActionListener(this);
            pc.gridwidth = GridBagConstraints.REMAINDER;
            pc.gridx = 1;
            p_gl.setConstraints(b_rmEng, pc);
            p_eng.add(b_rmEng);

            pack();
            setVisible(true);
            toFront();
        }

        private void exit() {
            try {
                //				org.objectweb.proactive.ProActive.unregisterVirtualNode(
                //					vn);
                // System.out.println("undeploy : " + urn + " at : " + url);
                WebServices.unExposeAsWebService(urn, url);
                proActiveDescriptor.killall(false);
            } catch (Exception e) {
                trace("WARNING occurs when killing the application!");

                //e.printStackTrace();
            }

            try {
                setVisible(false);
                dispose();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void paint(Graphics g) {
            update(g);
        }

        @Override
        public void update(Graphics g) {
            i_top = this.getInsets().top;
            g.setColor(Color.gray);
            g.drawLine(0, i_top - 1, 2000, i_top - 1);
            g.setColor(Color.white);
            g.drawLine(0, i_top, 2000, i_top);
        }

        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == mi_exit) {
                exit();
            } else if (source == mi_clear) {
                ta_log.setText("");
            } else if (source == mi_benchmark) {
                doBenchmarks();
            } else if (source == b_addEng) {
                String[] sel = li_enginesAvailable.getSelectedItems();

                if (sel != null) {
                    for (int i = 0; i < sel.length; i++) {
                        li_enginesUsed.add(sel[i]);

                        try {
                            li_enginesAvailable.remove(sel[i]);
                        } catch (Exception l_ex) {
                            l_ex.printStackTrace();
                        }
                    }
                }
            } else if (source == b_rmEng) {
                String[] sel = li_enginesUsed.getSelectedItems();

                if (sel != null) {
                    for (int i = 0; i < sel.length; i++) {
                        li_enginesAvailable.add(sel[i]);

                        try {
                            li_enginesUsed.remove(sel[i]);
                        } catch (Exception l_ex) {
                            l_ex.printStackTrace();
                        }
                    }
                }
            }
        }

        /**
         * AWT 1.1 event handling for window events
         */
        class MyWindowListener extends WindowAdapter {
            @Override
            public void windowClosing(WindowEvent e) {
                exit();
            }
        }
    }
}


class Election extends Thread {
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int CLOCKWISE = 4;
    public static final int UNCLOCKWISE = 5;

    /**
     * Duration of one election round in seconds
     */
    private static final int WAITSECS = 4;
    private static boolean running = false;
    private static Hashtable wishes;
    private static C3DDispatcher c3ddispatcher;

    public Election(int i_user, Integer wish, C3DDispatcher c3ddispatcher) {
        Election.c3ddispatcher = c3ddispatcher;
        Election.running = true;
        wishes = new Hashtable();
        vote(i_user, wish);
        c3ddispatcher.showMessage(i_user,
            "Request 'rotate " + voteString(wish) + "' submitted, \nnew " +
            WAITSECS + " second election started ...");
        c3ddispatcher.showMessageExcept(i_user,
            "New " + WAITSECS + " second election started:");
        c3ddispatcher.showMessageExcept(i_user,
            "   User " + c3ddispatcher.nameOfUser(i_user) +
            " wants to rotate " + voteString(wish));

        //c3ddispatcher.showDialogExcept(i_user,"Election","An election has been started !");
        // Launches the Election thread
        this.start();
    }

    @Override
    public synchronized void run() {
        try {
            wait(WAITSECS * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int[] score = { 0, 0, 0, 0, 0, 0 };

        int i_user;

        c3ddispatcher.showMessageAll("Election finished");

        for (Enumeration e = wishes.keys(); e.hasMoreElements();) {
            i_user = ((Integer) e.nextElement()).intValue();

            Integer wish = (Integer) wishes.get(new Integer(i_user));
            c3ddispatcher.showMessageAll("   User " +
                c3ddispatcher.nameOfUser(i_user) + " voted '" +
                voteString(wish) + "'");

            // update the scores
            score[wish.intValue()]++;
        }

        c3ddispatcher.showMessageAll("   Result:\n       " + score[RIGHT] +
            " right, " + score[LEFT] + " left [rotate]\n      " + score[UP] +
            " up ," + score[DOWN] + " down [vertical]\n      " +
            score[CLOCKWISE] + " right," + score[UNCLOCKWISE] + " left [spin]");

        // Computes the winner
        int winner = -1;

        for (int i = 0; i < score.length; i++) {
            // If a candidate has got all the votes
            if (score[i] == wishes.size()) {
                winner = i;
            }
        }

        switch (winner) {
        case UP:
            c3ddispatcher.showMessageAll("   The scene will be rotated up.");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.rotateUp(0);

            break;
        case DOWN:
            c3ddispatcher.showMessageAll("   The scene will be rotated down.");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.rotateDown(0);

            break;
        case LEFT:
            c3ddispatcher.showMessageAll("   The scene will be rotated left.");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.rotateLeft(0);

            break;
        case RIGHT:
            c3ddispatcher.showMessageAll("   The scene will be rotated right.");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.rotateRight(0);

            break;
        case CLOCKWISE:
            c3ddispatcher.showMessageAll("  The scene will be spinned right");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.spinClock(0);

            break;
        case UNCLOCKWISE:
            c3ddispatcher.showMessageAll("  The scene will be spinned left");
            Election.running = false;
            Election.wishes.clear();
            c3ddispatcher.spinUnclock(0);

            break;
        default:
            c3ddispatcher.showMessageAll(
                "   No consensus found, vote again please!");
        }

        Election.running = false;
        Election.wishes.clear();
    }

    public synchronized static int vote(int i_user, Integer wish) {
        if (wishes.containsKey(new Integer(i_user))) {
            c3ddispatcher.showMessage(i_user,
                "You have already voted in this round");
        } else {
            wishes.put(new Integer(i_user), wish);
        }

        return wishes.size();
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public String voteString(Integer wish) {
        String ret;

        switch (wish.intValue()) {
        case UP:
            ret = "up";
            break;
        case DOWN:
            ret = "down";
            break;
        case LEFT:
            ret = "left";
            break;
        case RIGHT:
            ret = "right";
            break;
        case CLOCKWISE:
            ret = "clockwise";
            break;
        case UNCLOCKWISE:
            ret = "unclockwise";
            break;
        default:
            ret = "error";
            break;
        }

        return ret;
    }

    public synchronized void finish() {
        c3ddispatcher.showMessageAll("Everybody voted");
        this.notify();
    }
}
