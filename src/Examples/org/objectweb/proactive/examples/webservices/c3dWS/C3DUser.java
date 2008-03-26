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
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Light;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Primitive;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Sphere;


public class C3DUser implements org.objectweb.proactive.RunActive, java.io.Serializable {
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
    private MigrationStrategyManagerImpl myStrategyManager;
    private boolean onMigration;
    private String dispatcher_host;
    protected VirtualNode vnDispatcher;

    /**
     * Uses active objects if set to true
     */
    private final boolean ACTIVE = true;
    private C3DUser me;

    /**
     * The about box
     */
    private MyDialog about;

    /**
     * Status log, displays messages from other users concerning action
     * requests
     */
    TextArea ta_log;

    /**
     * SpyEvent log
     */
    TextArea ta_mess;

    /**
     * Name (number) of this user
     */
    Label l_user;

    /**
     * OS String of the C3Ddispatcher
     */
    Label l_c3ddispatcher;

    /**
     * Height of the <code>Image</code> to be rendered
     */
    final int i_height = 270;

    /**
     * Width of the <code>Image</code> to be rendered
     */
    final int i_width = 270;

    /**
     * <code>MemoryImagesource</code> as pixel source for the
     * <code>Image</code> to be rendered
     */
    transient MemoryImageSource mis;

    /**
     * Destination array for the calculated pixels of the <code>Image</code>
     */
    transient int[] pix;

    /**
     * <code>C3DDispatcher</code>, decouples the m user frames and the
     * n rendering engines. There is always exactly one C3DDispatcher object
     * in the ProActive rendering system.
     */
    C3DDispatcher c3ddispatcher;

    /**
     * Number of this user in the set of users registered at the
     * <code>C3DDispatcher</Code>, used to distinguish the action requests of
     * several users
     */

    //@@ADDON Florian 13/8/98
    /**
     * Hashtable containing the name and ID of each user<br>
     * Used for direct messaging
     */
    private Hashtable h_users = new Hashtable();

    /**
     * An AWT list allowing the User to select its locutors [??]
     */
    private transient List li_users;

    /**
     * The ID# of the selected locutor [-1=broadcast]
     */
    private int talkId = -1;

    //@@END_ADDON Florian 13/8/98

    /**
     * The number of rotations so far
     */
    public int nRotate = 0;
    public transient UserFrame userframe;
    boolean b_isApplet;
    boolean b_isBot;
    String botUrl;
    String s_username;
    int i_user;

    public C3DUser() {
    }

    public C3DUser(Boolean b_isApplet, Boolean b_isBot, String url) {
        this.b_isApplet = b_isApplet.booleanValue();
        this.b_isBot = b_isBot.booleanValue();
        this.botUrl = url;
        this.onMigration = false;
    }

    public void go(ProActiveDescriptor pad) {
        me = (C3DUser) org.objectweb.proactive.api.PAActiveObject.getStubOnThis();

        // Creates the user Frame  
        vnDispatcher = pad.getVirtualNode("Dispatcher");
        userframe = new UserFrame(me);
        userframe.createWelcomePanel();

        setOnMigration(true);
    }

    public void rebuild() {
        me = (C3DUser) org.objectweb.proactive.api.PAActiveObject.getStubOnThis();
        userframe = new UserFrame(me);
        userframe.createPanelAfterMigration(dispatcher_host, s_username);
    }

    public void clean() {
        if (userframe != null) {
            userframe.dispose();
            userframe = null;
        }
    }

    public void setOnMigration(boolean value) {
        this.onMigration = value;
    }

    public boolean getOnMigration() {
        return this.onMigration;
    }

    public void runActivity(org.objectweb.proactive.Body body) {
        logger.info("Starting custom live in C3DUser");

        //System.out.println("migration "+getOnMigration());
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        myStrategyManager = new MigrationStrategyManagerImpl(
            (org.objectweb.proactive.core.body.migration.Migratable) body);
        myStrategyManager.onDeparture("clean");

        if (getOnMigration()) {
            //System.out.println("on migration");
            //System.out.println("width "+i_width);
            //System.out.println("height "+i_height);
            rebuild();
        }

        //    while (body.isActive()) {
        //      service.blockingServeOldest();
        //    }
        service.fifoServing();

        if (getOnMigration()) {
            clean();
        }

        //if the activity is restarted it is a migration
        //to be improved 
    }

    /**
     * Entry point of the program
     */
    public static void main(String[] argv) throws NodeException {
        ProActiveDescriptor proActiveDescriptor = null;

        ProActiveConfiguration.load();

        try {
            proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" + argv[0]);
            proActiveDescriptor.activateMappings();
        } catch (Exception e) {
            e.printStackTrace();
            logger.fatal("Pb in main");
        }

        VirtualNode user = proActiveDescriptor.getVirtualNode("User");

        Node node = user.getNode();
        Object[] params = { new Boolean(false), new Boolean(false), "" };

        try {
            C3DUser c3duser = (C3DUser) org.objectweb.proactive.api.PAActiveObject.newActive(
                    "org.objectweb.proactive.examples.webservices.c3dWS.C3DUser", params, node
                            .getNodeInformation().getURL());

            c3duser.go(proActiveDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String s_message) {
        ta_log.append(s_message + "\n");
    }

    public void message(String s_message) {
        ta_mess.append(s_message + "\n");
    }

    public UserFrame getUserFrame() {
        return userframe;
    }

    //@@ADDON Florian 13/8/98

    /**
     * Informs the user that a new user has joined the party!!
     * @param  nUser The new user's ID
     * @param sNAme The new user's name
     * @version ADDON  13/8/98
     * @author The ProActive Team
     */
    public void informNewUser(int nUser, String sName) {
        li_users.add(sName);
        h_users.put(sName, new Integer(nUser));
    }

    /**
     * Informs the user that another user left
     * @param nUser The id of the old user
     * @version ADDON 16/8/98
     * @author The ProActive Team
     */
    public void informUserLeft(String sName) {
        //  remove the user from the users list
        try {
            li_users.remove(sName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Remove the user from the hash table
        h_users.remove(sName);

        if (h_users.isEmpty()) {
            talkId = -1;
            li_users.select(0);
        }
    }

    //@@END_ADDON Florian 13/8/98

    /**
     * The pinging function called by <code>C3DDispatcher</code>
     * to get the avg. pinging time
     */
    public int ping() {
        return 0;
    }

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

        /*
         * p = new Sphere(new Vec(0,0,0), 3); p.setColor(1.0,1.0,1.0); p.surf.shine = 15.0;
         * p.surf.ks = 0.5; p.surf.kt=0.5; scene.addObject(p); p = new Sphere(new Vec(-10,5.77,0),
         * 4); p.setColor(0.0,1.0,1.0); p.surf.shine = 14.0; p.surf.kd = 0.7; p.surf.ks = 0.3;
         * scene.addObject(p); p = new Sphere(new Vec(10,5.77,0), 4); p.setColor(1.0,1.0,0.0);
         * p.surf.shine = 14.0; p.surf.kd = 0.7; p.surf.ks = 0.3; scene.addObject(p); p = new
         * Sphere(new Vec(0,-11.55,0), 4); p.setColor(1.0,0.0,1.0); p.surf.shine = 14.0; p.surf.kd =
         * 0.7; p.surf.ks = 0.3; scene.addObject(p); p = new Sphere(new Vec(0,0,14), 8);
         * p.setColor(0.0,0.0,0.0); p.surf.shine = 14.0; p.surf.kd = 0.7; p.surf.ks = 0.3;
         * scene.addObject(p);
         */

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
     * Display an interval of newly calculated pixels
     * @param newpix        The pixels as int array
     * @param interval        The interval
     */
    public void setPixels(int[] newpix, Interval interval) {
        int from = interval.width * interval.yfrom;

        /* Copy new pixels among the old ones */
        System.arraycopy(newpix, 0, pix, from, newpix.length);

        /* Inform the image observers of the update */
        mis.newPixels(0, interval.yfrom, interval.width, interval.yto);

        /* Refresh the image displayed on the frame */
        userframe.repaint();
    }

    /**
     * Displays a message from the <code>C3DDispatcher</code> in the status log
     *
     * @param message message to be displayed
     */
    public void showMessage(String message) {
        ta_log.append(message + "\n");

        if (b_isBot) {
            // if all the intervals are rendered
            if (message.substring(0, 3).compareTo("All") == 0) {
                // 16 renderings 
                if (nRotate < 15) {
                    nRotate++;
                    c3ddispatcher.rotateLeft(0);
                } else {
                    c3ddispatcher.doBenchmarks();
                    exit();
                }
            }
        }
    }

    public void showUserMessage(String message) {
        ta_mess.append(message + '\n');
    }

    public void dialogMessage(String title, String content) {
        MsgDialog md = new MsgDialog(userframe, title, content);
        md.setVisible(true);
    }

    /**
     * OOP encapsulation method for 'int width'
     * @return width of the applet window, i.e. width of the rendering image
     */
    public Integer getWidth() {
        //System.out.println("Dispatcher asks for width ");
        return new Integer(i_width);
    }

    /**
     * OOP encapsulation method for 'int height'
     * @return height of the applet window, i.e. height of the rendering image
     */
    public Integer getHeight() {
        return new Integer(i_height);
    }

    /**
     * Exit the application
     * @throws IOException
     */
    void exit() {
        try {
            c3ddispatcher.unregisterConsumer(i_user);
            userframe.setVisible(false);
            userframe.dispose();
            userframe = null;
            me.terminate();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }

        //    if (!b_isApplet) {
        //    	
        //      System.exit(0);
        //    }
    }

    private void trace(String s_message) {
        logger.info("C3DUser: " + s_message);
    }

    public void terminate() {
        PAActiveObject.terminateActiveObject(true);
    }

    public class UserFrame extends Frame implements ActionListener, ItemListener {

        /**
         * Button UP
         */
        private Button b_up = new EqualButton("Up");

        /**
         * Button UP
         */
        private Button b_down = new EqualButton("Down");

        /**
         * Button to request the 'rotate left' action
         */
        private Button b_left = new EqualButton("Left");

        /**
         * Button to request the 'rotate right' action
         */
        private Button b_right = new EqualButton("Right");

        /**
         * Button to request the spin clock action
         */
        private Button b_clock = new EqualButton("Spin right");

        /**
         * Button to request the spin unclock action
         */
        private Button b_unclock = new EqualButton("Spin left");

        /**
         * Button to exit the application
         */
        private Button b_exit = new EqualButton("Exit");

        /***
         * Button to add a Sphere
         */
        private Button b_addSphere = new Button("Add random sphere");

        /**
         * Button to conenct to the dispatcher
         */
        private Button b_connect = new EqualButton("Connect");

        /**
         * Button to reset the scene
         */
        private Button b_reset = new EqualButton("Reset");

        /**
         * Button used for sending messages
         */
        private Button b_send = new Button("Send");
        private TextField tf_mess;
        private TextField tf_name;
        private TextField tf_host;
        private GridBagLayout gridbag = new GridBagLayout();
        private GridBagConstraints c = new GridBagConstraints();
        private String s_localhost;
        private C3DUser c3duser;
        private MenuItem mi_exit;
        private MenuItem mi_clear;
        private MenuItem mi_list;
        private MenuItem mi_about;
        private Font f_standard = new Font("SansSerif", Font.PLAIN, 12);
        private int i_top;
        private MenuBar mb;

        /**
         * One and only constructor for <code>UserFrame</code>. Creates and
         * displays this frame and the GUI objects, creates the image object
         * to take the rendered picture.
         */
        public UserFrame(C3DUser c3d) {
            super("Collaborative 3D Environment - User Window");
            setBackground(Color.lightGray);
            setLayout(gridbag);
            setFont(f_standard);
            addWindowListener(new MyWindowListener());
            c3duser = c3d;

            s_localhost = "";

            s_localhost = ProActiveInet.getInstance().getInetAddress().getCanonicalHostName();
        }

        public UserFrame(C3DUser c3d, boolean value) {
            super("Collaborative 3D Environment - User Window");
            setBackground(Color.lightGray);
            setLayout(gridbag);
            setFont(f_standard);
            addWindowListener(new MyWindowListener());
            c3duser = c3d;
            s_localhost = "";

            s_localhost = ProActiveInet.getInstance().getInetAddress().getCanonicalHostName();
        }

        public Button getB_up() {
            return b_up;
        }

        public Button getB_down() {
            return b_down;
        }

        public Button getB_left() {
            return b_left;
        }

        public Button getB_right() {
            return b_right;
        }

        public Button getB_clock() {
            return b_clock;
        }

        public Button getB_unclock() {
            return b_unclock;
        }

        /**
         * Creates the display for a benchmarking bot
         */
        public void createBot(String url) {
            s_username = "Benchmarking bot";

            //createFinalPanel(url, s_username);
        }

        private void createWelcomePanel() {
            Label l1 = new Label("Welcome to the Collaborative 3D Environment -", Label.CENTER);
            c.insets = new Insets(5, 5, 0, 5);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            gridbag.setConstraints(l1, c);
            add(l1);

            Label l2 = new Label("a ProActive test application", Label.CENTER);
            c.insets = new Insets(0, 5, 0, 5);
            gridbag.setConstraints(l2, c);
            add(l2);

            Label l3 = new Label("Please enter your name and the host of the C3DDispatcher.", Label.CENTER);
            c.insets = new Insets(15, 5, 10, 5);
            gridbag.setConstraints(l3, c);
            add(l3);

            Label l4 = new Label("Name:", Label.RIGHT);
            Label l5 = new Label("Host:", Label.RIGHT);
            c.insets = new Insets(5, 5, 5, 0);
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(l4, c);
            gridbag.setConstraints(l5, c);

            tf_name = new TextField(20);
            tf_name.addActionListener(this);

            tf_host = new TextField(s_localhost, 20);
            tf_host.addActionListener(this);
            c.insets = new Insets(5, 0, 5, 5);
            c.anchor = GridBagConstraints.CENTER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints(tf_name, c);
            gridbag.setConstraints(tf_host, c);

            add(l4);
            add(tf_name);
            add(l5);
            add(tf_host);

            c.gridwidth = GridBagConstraints.RELATIVE;
            c.insets = new Insets(15, 5, 10, 5);
            c.anchor = GridBagConstraints.EAST;
            gridbag.setConstraints(b_connect, c);
            b_connect.addActionListener(this);
            add(b_connect);
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.anchor = GridBagConstraints.CENTER;
            gridbag.setConstraints(b_exit, c);
            b_exit.addActionListener(this);
            add(b_exit);

            pack();
            setVisible(true);
            toFront();
        }

        public void createPanel(String s_host, String s_name) {
            setVisible(false);
            removeAll();

            mb = new MenuBar();

            Menu m_file = new Menu("File", false);
            m_file.setFont(f_standard);
            mi_exit = new MenuItem("Exit");
            mi_clear = new MenuItem("Clear log");
            mi_list = new MenuItem("List users");
            mi_clear.setFont(f_standard);
            mi_exit.setFont(f_standard);
            mi_list.setFont(f_standard);
            mi_exit.addActionListener(this);
            mi_clear.addActionListener(this);
            mi_list.addActionListener(this);
            m_file.add(mi_list);
            m_file.add(mi_clear);
            m_file.addSeparator();
            m_file.add(mi_exit);
            mb.add(m_file);

            Menu m_c3d = new Menu("C3D ProActive PDC", false);
            mi_about = new MenuItem("About ProActive PDC");
            mi_about.setFont(f_standard);
            mi_about.addActionListener(this);
            m_c3d.add(mi_about);

            mb.setHelpMenu(m_c3d);
            setMenuBar(mb);

            /* Instanciates the destination pixel array */
            pix = new int[i_width * i_height];

            /* Creates a MemoryImageSource as a pixel source for the image */
            mis = new MemoryImageSource(i_width, i_height, pix, 0, i_width);

            /* Accomodates the changing nature (rotation) of the image */
            mis.setAnimated(true);

            /*
             * Creates an image component from the image from the MemoryImageSource; avoids drawing
             * issues in the user frame and places it in a panel
             */
            Panel p_image = new Panel(new GridLayout(1, 1));

            //System.out.println("p image "+p_image.getSize().width);
            // PANEL CONSTRAINTS
            c.insets = new Insets(5, 5, 0, 5);

            // position 
            c.gridx = 0;
            c.gridy = 0;
            c.weightx = 1;
            c.weighty = 1;

            // size;
            c.gridwidth = 1;
            c.gridheight = 1;

            //fill
            c.fill = GridBagConstraints.BOTH; // components will fill their whole area

            gridbag.setConstraints(p_image, c);

            //System.out.println("p image "+p_image.getSize().width);
            p_image.add(new MyImageContainer(createImage(mis), i_width, i_height, p_image));
            add(p_image);

            /**
             * The log and buttons area
             */

            // Creates the panel and places it in the main frame
            Panel p_log = new Panel();

            // adds the panel
            c.gridx = 1;
            gridbag.setConstraints(p_log, c);
            add(p_log);

            // Creates the panel's layout manager
            GridBagLayout gb_panel = new GridBagLayout();
            p_log.setLayout(gb_panel);

            // Label displaying the username
            l_user = new Label("User " + s_name, Label.CENTER);
            l_user.setFont(new Font("SansSerif", Font.ITALIC + Font.BOLD, 18));

            GridBagConstraints pc = new GridBagConstraints();

            pc.fill = GridBagConstraints.BOTH;
            pc.insets = new Insets(5, 5, 0, 5);
            pc.gridx = 0;
            pc.gridy = 0;
            pc.weightx = 1;
            pc.weighty = 1;
            pc.gridwidth = GridBagConstraints.REMAINDER;
            gb_panel.setConstraints(l_user, pc);
            p_log.add(l_user);

            // Label displaying local machine informations 
            Label l_machine = new Label("Local host: " + s_localhost + " (" + System.getProperty("os.name") +
                " " + System.getProperty("os.version") + ")");

            // Adds the label in the panel
            pc.gridy = 1;
            gb_panel.setConstraints(l_machine, pc);
            p_log.add(l_machine);

            // Label displaying C3DDispatcher informations
            l_c3ddispatcher = new Label("C3DDispatcher host: " + s_host);
            pc.gridy = 2;
            gb_panel.setConstraints(l_c3ddispatcher, pc);
            p_log.add(l_c3ddispatcher);

            // Mesage log
            pc.gridy = 3;
            pc.weighty = 2;
            ta_log = new TextArea(5, 25);
            ta_log.setEditable(false);
            gb_panel.setConstraints(ta_log, pc);
            p_log.add(ta_log);

            //--------------------------------------------------
            //    INPUTS
            // Panel containing the buttons
            Panel p_input = new Panel();

            GridBagLayout gb_input = new GridBagLayout();
            p_input.setLayout(gb_input);

            c.gridx = 0;
            c.gridy = 1;
            c.gridwidth = 1;

            gridbag.setConstraints(p_input, c);
            add(p_input);

            //---------------- Buttons
            // unclock
            pc.gridy = 0;
            pc.gridx = 0;
            pc.gridwidth = 1;
            pc.fill = GridBagConstraints.NONE;

            gb_input.setConstraints(b_unclock, pc);
            b_unclock.addActionListener(this);
            p_input.add(b_unclock);

            // Clock
            pc.gridx = 2;
            gb_input.setConstraints(b_clock, pc);
            b_clock.addActionListener(this);
            p_input.add(b_clock);

            // Up
            pc.gridy = 0;
            pc.gridx = 1;
            gb_input.setConstraints(b_up, pc);
            b_up.addActionListener(this);
            p_input.add(b_up);

            // Down
            pc.gridx = 1;
            pc.gridy = 2;
            gb_input.setConstraints(b_down, pc);
            b_down.addActionListener(this);
            p_input.add(b_down);

            // Left
            pc.gridx = 0;
            pc.gridy = 1;
            b_left.addActionListener(this);
            gb_input.setConstraints(b_left, pc);
            p_input.add(b_left);

            // Right
            pc.gridx = 2;
            gb_input.setConstraints(b_right, pc);
            b_right.addActionListener(this);
            p_input.add(b_right);

            // Add
            // This one is added in the main frame
            //c.gridwidth = c.REMAINDER;
            c.gridheight = 1;
            c.gridx = 1;
            c.fill = GridBagConstraints.NONE;

            gridbag.setConstraints(b_addSphere, c);
            b_addSphere.addActionListener(this);
            add(b_addSphere);

            // Reset
            // This one is added in the main fram
            c.gridx = 2;
            c.gridy = 1;
            c.fill = GridBagConstraints.NONE;

            gridbag.setConstraints(b_reset, c);
            b_reset.addActionListener(this);
            add(b_reset);

            //-----------------------------------------
            // MESSAGES
            Panel p_mess = new Panel();

            c.insets = new Insets(10, 10, 10, 10);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.BOTH;
            gridbag.setConstraints(p_mess, c);
            add(p_mess);

            GridBagLayout g_mess = new GridBagLayout();
            p_mess.setLayout(g_mess);

            // Users list
            li_users = new List(5, false);
            li_users.add("~BROADCAST~");
            li_users.select(0);
            li_users.addItemListener(this);

            pc.insets = new Insets(5, 5, 5, 5);

            //      pc.anchor=pc.WEST;
            pc.gridx = 0;
            pc.gridy = 0;
            pc.fill = GridBagConstraints.BOTH;
            pc.gridwidth = GridBagConstraints.RELATIVE;
            g_mess.setConstraints(li_users, pc);
            p_mess.add(li_users);

            // Output
            ta_mess = new TextArea(5, 30);
            ta_mess.setEditable(false);
            pc.gridwidth = GridBagConstraints.REMAINDER;
            pc.gridx = 1;
            g_mess.setConstraints(ta_mess, pc);
            p_mess.add(ta_mess);

            // INPUT:::
            Panel p_msg = new Panel();
            GridBagLayout g_msg = new GridBagLayout();
            pc = new GridBagConstraints();
            p_msg.setLayout(g_msg);
            c.gridy = 3;
            gridbag.setConstraints(p_msg, c);
            add(p_msg);

            // SpyEvent
            pc.gridx = 0;
            pc.gridy = 0;
            pc.fill = GridBagConstraints.NONE;

            Label l_mess = new Label("SpyEvent:");
            g_msg.setConstraints(l_mess, pc);
            p_msg.add(l_mess);

            // Input
            tf_mess = new TextField();
            tf_mess.addActionListener(this);
            pc.weightx = 1.0;
            pc.gridwidth = GridBagConstraints.RELATIVE;
            pc.fill = GridBagConstraints.BOTH;
            pc.gridx = 1;
            g_msg.setConstraints(tf_mess, pc);
            p_msg.add(tf_mess);

            // Send button
            pc.weightx = 0.0;
            pc.gridwidth = GridBagConstraints.REMAINDER;
            b_send.addActionListener(this);
            pc.fill = GridBagConstraints.NONE;
            pc.gridx = 2;
            g_msg.setConstraints(b_send, pc);
            p_msg.add(b_send);

            pack();
            setVisible(true);
            toFront();
        }

        public void createFinalPanel(String s_host, String s_name) {
            createPanel(s_host, s_name);

            try {
                vnDispatcher.getVirtualNodeInternal().setRuntimeInformations("LOOKUP_HOST", s_host);
                c3ddispatcher = (C3DDispatcher) vnDispatcher.getUniqueAO();
                i_user = c3ddispatcher.registerConsumer(me, createScene(), s_name);
                l_c3ddispatcher.setText(l_c3ddispatcher.getText() + " (" + c3ddispatcher.getOSString() + ")");
            } catch (Exception ex) {
                log("Exception caught: " + ex);
                ex.printStackTrace();
                log("Error: C3DDispatcher not found, try to reconnect");
            }
        }

        public void createPanelAfterMigration(String s_host, String s_name) {
            createPanel(s_host, s_name);

            c3ddispatcher.registerMigratedUser(i_user);

            //System.out.println("i am here");
            l_c3ddispatcher.setText(l_c3ddispatcher.getText() + " (" + c3ddispatcher.getOSString() + ")");

            //System.out.println("i am there");
        }

        /**
         * AWT 1.1 event handling
         */
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == b_left) {

                /* Request 'rotate left' on button click */
                if (c3ddispatcher != null) {
                    //b_left.setBackground(Color.yellow);
                    c3ddispatcher.rotateLeft(i_user);
                }
            } else if (source == b_right) {

                /* Request 'rotate right' on button click */
                if (c3ddispatcher != null) {
                    //b_right.setBackground(Color.yellow);
                    c3ddispatcher.rotateRight(i_user);
                }
            } else if (source == b_up) {
                // Go up
                if (c3ddispatcher != null) {
                    //b_up.setBackground(Color.yellow);
                    c3ddispatcher.rotateUp(i_user);
                }
            } else if (source == b_down) {
                // Go down
                if (c3ddispatcher != null) {
                    //b_down.setBackground(Color.yellow);
                    c3ddispatcher.rotateDown(i_user);
                }
            } else if (source == b_clock) {
                // Go clockwise
                if (c3ddispatcher != null) {
                    //b_clock.setBackground(Color.yellow);
                    c3ddispatcher.spinClock(i_user);
                }
            } else if (source == b_unclock) {
                // Go unclockwise
                if (c3ddispatcher != null) {
                    //b_unclock.setBackground(Color.yellow);
                    c3ddispatcher.spinUnclock(i_user);
                }
            } else if ((source == mi_exit) || (source == b_exit)) {
                /* Exit the appplication */
                setVisible(false);
                exit();
            } else if ((source == tf_mess) || (source == b_send)) {
                if (c3ddispatcher != null) {
                    // The user wants to send a mesage
                    String s_mess = tf_mess.getText();

                    if (s_mess.length() > 0) {
                        if (talkId == -1) {
                            // BroadCast
                            ta_mess.append("<to all> " + s_mess + '\n');
                            c3ddispatcher
                                    .showUserMessageExcept(i_user, "[from " + s_username + "] " + s_mess);
                        } else {
                            // Private message
                            ta_mess.append("<to " + li_users.getSelectedItem() + "> " + s_mess + '\n');
                            c3ddispatcher.showUserMessage(talkId, "[Private from " + s_username + "] " +
                                s_mess);
                        }

                        tf_mess.setText("");
                    } else {
                        tf_mess.setText("Enter text to send");
                        tf_mess.selectAll();
                    }
                }
            } else if ((source == b_connect) || (source == tf_name) || (source == tf_host)) {

                /* The Welcome panel has been validated */
                String s_name = tf_name.getText();
                String s_host = tf_host.getText();

                if (s_name.length() < 1) {
                    tf_name.setText("Enter your name");
                }

                if (s_host.length() < 1) {
                    tf_host.setText("Enter dispatcher host");
                }

                if ((s_name.length() > 0) && (s_host.length() > 0)) {
                    s_username = s_name;
                    dispatcher_host = s_host;

                    createFinalPanel(s_host, s_name);
                }
            } else if (source == mi_clear) {
                ta_log.setText("");
            } else if (source == mi_list) {
                try {
                    log("List of current users:");
                    log(c3ddispatcher.getUserList());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (source == b_addSphere) {

                /* The user wants to add a sphere */

                // Computes randoms coords btw -10 and 10
                double x;

                /* The user wants to add a sphere */

                // Computes randoms coords btw -10 and 10
                double y;

                /* The user wants to add a sphere */

                // Computes randoms coords btw -10 and 10
                double z;

                /* The user wants to add a sphere */

                // Computes randoms coords btw -10 and 10
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
                sphere.surf.color = (color[Math.round((float) (Math.random() * (color.length - 1)))]);

                // Voodoo stuff...
                sphere.surf.kd = Math.random();
                sphere.surf.ks = Math.random();
                sphere.surf.shine = Math.random() * 20.0;

                //sphere.surf.kt=Math.random();
                //sphere.surf.ior=Math.random();
                c3ddispatcher.addSphere(sphere);
            } else if (source == mi_about) {
                new MyDialog(this);
            } else if (source == b_reset) {
                c3ddispatcher.resetScene(createScene());
            }
        }

        /**
         * Sets the ID of the messages' recipient
         * @param it The corresponding Item Event on li_user
         * @version ADDON Florian 17/8/98
         */
        public void itemStateChanged(ItemEvent it) {
            if (li_users.getSelectedIndex() == 0) {
                talkId = -1;
            } else {
                talkId = ((Integer) h_users.get(li_users.getSelectedItem())).intValue();
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

        /**
         * AWT 1.1 event handling for window events
         */
        class MyWindowListener extends WindowAdapter implements java.io.Serializable {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                exit();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                if (b_isBot) {
                    s_username = "Benchmarking bot";
                    createFinalPanel(botUrl, s_username);
                }
            }
        }
    }

    /**
     * The about box
     */
    class MyDialog extends Dialog implements ActionListener, MouseListener, java.io.Serializable {
        private Label d_title = new Label("ProActive PDC", Label.CENTER);
        private Label d_url = new Label("http://www.inria.fr/proactive/", Label.CENTER);

        /**
         * Button to close the about box
         */
        private Button b_ok = new Button("OK");

        public MyDialog(UserFrame parent) {
            super(parent, "About ProActive PDC", true);

            // Creates a modal about box
            GridBagLayout gb = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();

            setLayout(gb);

            // Title
            c.gridy = 0;
            c.fill = GridBagConstraints.HORIZONTAL;
            gb.setConstraints(d_title, c);
            d_title.setForeground(Color.blue);
            d_title.setFont(new Font("arial", Font.BOLD | Font.ITALIC, 16));
            add(d_title);

            // Subtitle
            c.gridy = 1;
            gb.setConstraints(d_url, c);
            d_url.addMouseListener(this);
            add(d_url);

            //Button
            c.gridy = 2;
            c.fill = GridBagConstraints.NONE;
            gb.setConstraints(b_ok, c);
            b_ok.addActionListener(this);
            add(b_ok);

            pack();

            setLocation(400, 200);
            setSize(200, 200);
            setVisible(true);
            toFront();
        }

        public void mouseClicked(MouseEvent e) {
            // TO BE REIMPLEMENTED AS SOON AS SOMEONE KNOWS HOW TO GET THE PATH WITHOUT SYSTEM.GETENV (DEPRECATED)

            /*
             * Runtime rt = Runtime.getRuntime(); System.out.println(rt); try{
             * rt.exec("/usr/local/netscape/netscape/netscape -remote
             * openURL'(http://www.inria.fr/proactive/)' &"); } catch (Exception ex){}
             */
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
        }

        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
        }
    }
}

/**
 * This class encapsulates the <code>Image</code> in an AWT
 * <code>Container</code>. It integrates easily with other AWT components
 * and hides the drawing and scaling issues inherent to image displaying
 */
class MyImageContainer extends Canvas implements java.io.Serializable {
    private Image img;
    private Image scaled_img;
    private int minwidth;
    private int minheight;
    private Insets insets;
    private Panel p;

    public MyImageContainer(Image img, int width, int height, Panel p) {
        this.img = img;
        this.scaled_img = img;
        this.minwidth = width;
        this.minheight = height;
        this.p = p;

        //System.out.println("----------"+p.getSize().width);
        insets = p.getInsets();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(scaled_img, insets.left, insets.top, this);
    }

    @Override
    public Dimension getPreferredSize() {
        insets = p.getInsets();

        return new Dimension(minwidth + insets.left + insets.right, minheight + insets.left + insets.right);
    }

    @Override
    public Dimension getMinimumSize() {
        insets = p.getInsets();

        return new Dimension(minwidth + insets.left + insets.right, minheight + insets.left + insets.right);
    }

    @Override
    public void validate() {
        insets = p.getInsets();

        //System.out.println("----------"+p.getSize().width+"-----------"+insets.left+"-----"+insets.right);
        int width = p.getSize().width - insets.left - insets.right;
        int height = p.getSize().height - insets.top - insets.bottom;

        //    int width = 270 - insets.left - insets.right;
        //    int height = 270 - insets.top - insets.bottom;
        //    System.out.println("width "+width);
        //    System.out.println("height "+height);
        int size;

        if (width > height) {
            size = height;
        } else {
            size = width;
        }

        scaled_img = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        super.validate();
        repaint();
    }
}
