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
package org.objectweb.proactive.examples.c3d;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.api.ProDeployment;
import org.objectweb.proactive.api.ProFuture;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.migration.MigrationStrategyManagerImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.examples.c3d.geom.Scene;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.gui.DispatcherGUI;
import org.objectweb.proactive.examples.c3d.prim.Light;
import org.objectweb.proactive.examples.c3d.prim.Plane;
import org.objectweb.proactive.examples.c3d.prim.Primitive;
import org.objectweb.proactive.examples.c3d.prim.Sphere;
import org.objectweb.proactive.examples.c3d.prim.Surface;
import org.objectweb.proactive.examples.c3d.prim.View;

import timer.AverageMicroTimer;


/**
 * This class is the bridge between renderers and users.
 * It handles the logic of asking renderers to draw partial images. It then forwards them to the users.
 * It also allows users to hold conversations, in a chat-like way.
 */
public class C3DDispatcher implements InitActive, RunActive, Serializable,
    Dispatcher, DispatcherLogic {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    private static int IMAGE_HEIGHT = 500;
    private static int IMAGE_WIDTH = 500;

    /**  Stores the users in a bag containing ref to active object, name and identifier */
    private UserBag userBag = new UserBag();
    protected int lastUserID = 0;

    /**  connects an Engine to its name, and other way round, without asking the remote object */
    private Vector<Object[]> engineAndStringTable = new Vector<Object[]>();

    /** list of engines. */
    private Vector<RenderingEngine> engineVector = new Vector<RenderingEngine>();

    /**
     * Scene to be rendered; set by the first user frame to register;
     * contains lights, spheres and one view
     */
    private Scene scene;

    /**
     * Pixel array to store the rendered pixels; used to initialize the
     * image on new-coming user frames, actualized in setPixels
     */
    private int[] localCopyOfImage = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

    /** ProactiveDescriptor for the dispatcher, used to kill nodes at the end */
    private ProActiveDescriptor proActiveDescriptor;
    private String[] rendererNodes;

    /** The unique GUI reference. All GUI actions use this pointer. */
    private transient DispatcherGUI gui;
    private transient Dispatcher me;
    private Election election;

    /** The object serving requests   */
    private transient Service service;

    // used to count arrived Intervals in the received vector
    private int pointer;

    /** The no-argument Constructor as commanded by ProActive; otherwise unused */
    public C3DDispatcher() {
    }

    /**
     * Constructor to call when using XML Descriptor
     */
    public C3DDispatcher(String[] rendererNodes, ProActiveDescriptor pad) {
        this.rendererNodes = rendererNodes;
        this.proActiveDescriptor = pad;
    }

    /**
     * Second Constructor, does all the creation and linking. Called in RunActivity.
     * Creates the renderers, and the GUI. When some users are already connected
     * (ie after migration), warn them of new machine settings.
     */
    public void go() {
        this.me = (Dispatcher) ProActiveObject.getStubOnThis();

        this.gui = new DispatcherGUI("C3D Dispatcher", (DispatcherLogic) this.me);

        if (engineAndStringTable.size() == 0) { // ==0 when starting, not when migrating

            /* Creates rendering engines */
            for (int i = 0; i < this.rendererNodes.length; i++) {
                String engineName = this.rendererNodes[i].toString();

                // toString() is to express the change, even though rendererNodes are Strings... 
                Object[] param = { engineName }; // engine name = engineNode name
                RenderingEngine tmpEngine;

                try {
                    tmpEngine = (RenderingEngine) ProActiveObject.newActive(C3DRenderingEngine.class.getName(),
                            param, this.rendererNodes[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.toString());
                }

                log("New rendering engine " + i + " created at " +
                    this.rendererNodes[i]);

                // always put all renderers as in use, when launching the program
                // put this <renderer string> in the "used list" of the <GUI>
                this.gui.addUsedEngine(engineName);
                // put <renderer> in the "used list" of the <Dispatcher logic> for computation
                this.engineVector.add(tmpEngine);

                // adds all the engine in the hashtable
                this.engineAndStringTable.add(new Object[] { tmpEngine, engineName });
            }
        } else {
            // the GUI shows the engines being used.  
            for (int i = 0; i < this.engineAndStringTable.size(); i++) {
                Object[] engineAndString = this.engineAndStringTable.get(i);

                if (engineVector.contains(engineAndString[0])) {
                    this.gui.addUsedEngine((String) engineAndString[1]);
                } else {
                    this.gui.addAvailableEngine((String) engineAndString[1]);
                }
            }

            // show users in the GUI, and give these users the new dispatcher host information
            for (userBag.newIterator(); userBag.hasNext();) {
                userBag.next();
                this.gui.addUser(userBag.currentName() + " (" +
                    userBag.currentKey() + ")");
                userBag.currentUser()
                       .setDispatcherMachine(getMachineName(), getOSString());
            }
        }
    }

    /**
     * Appends message to the end of the list
     */
    private void log(String s_message) {
        this.gui.log(s_message + "\n");
    }

    /**
     * Does the rendering; creates the interval stack, registers the current
     * scene with the engines, assigns one initial interval to each engine
     * and triggers the calculation
     */
    private void render() {
        // Checks there are some engines used
        if (this.engineVector.size() == 0) {
            allLog("No engines ... contact the dispatcher");

            return;
        }

        // Benchmarking stuff...
        long startTime = System.currentTimeMillis();

        RenderingEngine[] engine = engineVector.toArray(new RenderingEngine[0]);

        int nbTasks = 3 * engine.length;
        log("Creating " + nbTasks + " intervals");

        int intheight = IMAGE_HEIGHT / nbTasks;

        // Create the interval stack 
        Interval[] intervalsToDraw = new Interval[nbTasks];
        boolean[] received = new boolean[nbTasks];

        // create all intervals except last one
        for (int i = 0; i < (nbTasks - 1); i++) {
            Interval newint = new Interval(i, IMAGE_WIDTH, IMAGE_HEIGHT,
                    i * intheight, (i + 1) * intheight);
            intervalsToDraw[i] = newint;
        }

        // last Interval goes to end of picture, ie IMAGE_HEIGHT
        Interval newint = new Interval(nbTasks - 1, IMAGE_WIDTH, IMAGE_HEIGHT,
                (nbTasks - 1) * intheight, IMAGE_HEIGHT);
        intervalsToDraw[intervalsToDraw.length - 1] = newint;

        // To store future ImagePart values
        Vector<Image2D> images = new Vector<Image2D>();
        int counter = 0; // says which interval is to assign next 

        for (int i = 0; i < nbTasks; i++) // no value has come back yet    

            received[i] = false;

        // assign one task to each renderer
        for (int i = 0; i < engine.length; i++) {
            try {
                engine[i].setScene(this.scene);

                Interval interval = intervalsToDraw[counter++];
                images.add(engine[i].render(i, interval));
                log("Interval " + interval.number + " assigned to engine " +
                    engineToName(engine[i]) + " [" + i + "]");
            } catch (Exception e) {
                log("OUPS , " + engineToName(engine[i]) + " is  dead");
            }
        }

        // assign another task to each renderer, so they are busy, even after returning the future
        for (int i = 0; i < engine.length; i++) {
            try {
                Interval interval = intervalsToDraw[counter++];
                images.add(engine[i].render(i, interval));
                log("Interval " + interval.number + " assigned to engine " +
                    engineToName(engine[i]) + " [" + i + "]");
            } catch (Exception e) {
                log("OUPS , " + engineToName(engine[i]) + "  dead");
            }
        }

        // when a renderer has finished its task, assign it a new one, until there are no more tasks
        while (counter < intervalsToDraw.length) {
            Image2D returnedImage = getReturned(images, received);
            int engineFree = returnedImage.getEngineNb();

            // log("RECEIVED interval " + returnedImage.getInterval().number + " from " + engineFree);
            Interval newInterval = intervalsToDraw[counter++];
            images.add(engine[engineFree].render(engineFree, newInterval));
            log("Interval " + newInterval.number + " assigned to engine " +
                engineToName(engine[engineFree]) + "[" + engineFree + "]");
        }

        this.pointer = 0;

        Image2D returnedImage = getReturned(images, received);
        int engineFree = returnedImage.getEngineNb();

        //log("RECEIVED interval " + returnedImage.getInterval().number + " from " + engineFree);
        // when all tasks have been assigned, finish treating arriving results
        // and also redraw first intervals, hoping to get faster rendering...
        int intervalToRecompute;

        while (-1 != (intervalToRecompute = allArrived(received))) { // we're sure not all have yet arrived
                                                                     // intervalToRecompute is the next not-yet-returned interval
                                                                     //assert !received[stillComputing] : "Oups, recomputing one already received! " + stillComputing;
                                                                     // assign to newly freed engine an interval not yet received
            Interval redrawInterval = intervalsToDraw[intervalToRecompute];
            images.add(engine[engineFree].render(engineFree, redrawInterval));
            log("Interval " + redrawInterval.number +
                " re-assigned to engine " + engineToName(engine[engineFree]) +
                "[" + engineFree + "]");

            returnedImage = getReturned(images, received);
            engineFree = returnedImage.getEngineNb();

            //log("RECEIVED interval " + returnedImage.getInterval().number + " from " + engineFree);
        }

        long elapsed = System.currentTimeMillis() - startTime;

        allLog("Image rendered in " + elapsed + " ms");
    }

    /**
     * Find the returned image in the images Vector, and sets received to true for this image.
     * @param images the Vector which contains futures, on which to wait for arrival
     * @param received the array of booleans which says if vector is arrived, aka !toBeComputedAgain
     * @return an image which is no longer a future
     */
    private Image2D getReturned(Vector<Image2D> images, boolean[] received) {
        int index = ProFuture.waitForAny(images);
        Image2D returnedImage = images.remove(index);
        setPixels(returnedImage);
        received[returnedImage.getInterval().number] = true;

        return returnedImage;
    }

    /**
     * Checks whether ALL intervals have spawn an Image2D.
     * This aims at having several renderers handling the same Interval ==> fault tolerance & speedup.
     * Real fault tolerance requires surrounding distant calls with try catch & timeouts.
     * Note we could be having an Interval still being computed, and also already
     * received, but we only care about receiving all values. The future is discarded.
     * @return -1 if all Intervals have returned an Image2D, index of interval missing elsewise.
     */
    private int allArrived(boolean[] received) {
        // needed as stop value in the second for loop
        int oldvalue = this.pointer;

        // check all values from oldvalue to array.length
        for (; this.pointer < received.length; this.pointer++)
            if (!received[this.pointer]) {
                return this.pointer++;
            }

        // check all values from 0 to oldvalue
        for (this.pointer = 0; this.pointer < oldvalue; this.pointer++)
            if (!received[this.pointer]) {
                return this.pointer++;
            }

        // if all booleans in received[] are true, then it means all results have arrived.
        return -1;
    }

    /** Delivers newly computed pixels to users, and stores for future use. */
    private void setPixels(Image2D image) {
        // Delivers the new pixels to all users
        // this could be a group comm  : userGroup.setPixels(image);
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            this.userBag.currentUser().setPixels(image);
        }

        // Stores the newly rendered interval in this.localCopyOfImage. 
        // this.localCopyOfImage is later used to initialize the images of newcoming consumers 
        System.arraycopy(image.getPixels(), 0, this.localCopyOfImage,
            image.getInterval().totalImageWidth * image.getInterval().yfrom,
            image.getPixels().length);
    }

    /**
     * Rotate every object by the given angle
     */
    public void rotateScene(int i_user, Vec angle) {
        // If there is more than one user, proceed with a vote. 
        // i_user < 0 means the election trigerred the rotation  
        if ((i_user >= 0) & (this.userBag.size() > 1)) {
            this.election.vote(i_user, this.userBag.getName(i_user), angle);

            // election cannot be null, it should be created by registerUser & by migration rebuild 
            return;
        }

        allLog("Scene is being spun along " + angle.direction());

        /* rotate every object ... */
        int objects = this.scene.getNbPrimitives();

        for (int i = 0; i < objects; i++) {
            Primitive p = this.scene.getPrimitive(i);
            p.rotate(angle);
            this.scene.setPrimitive(p, i);
        }

        /* render the image to reflect the rotation */
        render();
    }

    /** Tells what are the operations to perform before starting the activity of the AO.
     * Here, we state that if migration asked, procedure  is : leaveHost, migrate */
    public void initActivity(Body body) {
        MigrationStrategyManagerImpl myStrategyManager = new MigrationStrategyManagerImpl((org.objectweb.proactive.core.body.migration.Migratable) body);
        myStrategyManager.onDeparture("leaveHost");

        try {
            ProActiveObject.register(ProActiveObject.getStubOnThis(),
                "//" + InetAddress.getLocalHost().getHostName() + "/" +
                "Dispatcher");
        } catch (IOException ioe) {
            logger.error("Coudn't register the Dispatcher! " +
                ioe.getMessage());
        }
    }

    /** ProActive queue handling */
    public void runActivity(Body body) {
        // Creates the rendering engines 
        go();
        service = new Service(body);
        service.fifoServing();
    }

    /** Method called when leaving the current host, for example when migrating.
     * Made public because put in the request queue */
    public void leaveHost() {
        this.gui.trash();

        // should we call a ProActive.unregister("//localhost/Dispatcher"); ? 
    }

    /** Sends a [log] message to given user */
    public void userLog(int i_user, String s_message) {
        this.userBag.getUser(i_user).log(s_message);
    }

    /** Shows a message to a user*/
    public void userWriteMessage(int i_user, String s_message) {
        this.userBag.getUser(i_user).message(s_message);
    }

    /** Ask users & dispatcher log s_message, except one  */
    public void allLogExcept(int i_user, String s_message) {
        log(s_message);

        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();

            if (this.userBag.currentKey() != i_user) {
                this.userBag.currentUser().log(s_message);
            }
        }
    }

    /** send message to all users except one */
    public void userWriteMessageExcept(int i_user, String s_message) {
        log(s_message);

        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();

            if (this.userBag.currentKey() != i_user) {
                this.userBag.currentUser().message(s_message);
            }
        }
    }

    /** Ask all users & dispatcher to log s_message */
    public void allLog(String s_message) {
        log(s_message);

        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            this.userBag.currentUser().log(s_message);
        }
    }

    /** Register a user, so he can join the fun */

    //SYNCHRONOUS CALL. All "c3duser." calls in this method happen AFTER the int[] is returned
    public int registerUser(User c3duser, String userName) {
        c3duser.log("-> Remote call-back: dispatcher found, user registered");
        log("New user " + userName + "(" + this.lastUserID + ") has joined");

        // Informs the other users of the new user
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            this.userBag.currentUser().informNewUser(this.lastUserID, userName);
            c3duser.informNewUser(this.userBag.currentKey(),
                this.userBag.currentName());
        }

        // Adds this User to the list 
        this.userBag.add(this.lastUserID, c3duser, userName);
        this.gui.addUser(userName + " (" + this.lastUserID + ")");

        //  Do some initialization, if this was the first consumer to register
        if (this.userBag.size() == 1) {
            /* Sets the scene */
            this.scene = createNewScene();

            /* Creates the intervals, starts the calculation */
            render();
        } else {

            /* Initializes the image of the new-coming consumer */
            Interval inter = new Interval(0, IMAGE_WIDTH, IMAGE_HEIGHT, 0,
                    IMAGE_HEIGHT);
            c3duser.setPixels(new Image2D(this.localCopyOfImage, inter, 0));
        }

        // Tell the user where the dispatcher is running
        c3duser.setDispatcherMachine(getMachineName(), getOSString());

        // CREATE the election mechanism when more than one user registered
        int nbUsers = this.userBag.size();

        if ((nbUsers >= 2) && (this.election == null)) {
            try {
                this.election = (Election) ProActiveObject.newActive(Election.class.getName(),
                        new Object[] { (C3DDispatcher) me });
            } catch (Exception e) {
                e.printStackTrace();
            }

            election.setNbUsers(nbUsers);
        }

        // return user_id
        return this.lastUserID++;
    }

    public void registerMigratedUser(int userNumber) {
        String name = this.userBag.getName(userNumber);
        log("User " + name + "(" + userNumber + ") has migrated ");

        User c3duser = this.userBag.getUser(userNumber);

        /* Initializes the image of the migrated consumer
         * (user's copy of image is destroyed before user's migration) */
        Interval inter = new Interval(0, IMAGE_WIDTH, IMAGE_HEIGHT, 0,
                IMAGE_HEIGHT);
        c3duser.setPixels(new Image2D(this.localCopyOfImage, inter, 0));
    }

    /** removes user from userList, so he cannot receive any more messages or images */
    public void unregisterConsumer(int number) {
        String nameOfUser = this.userBag.getName(number);
        allLogExcept(number, "User " + nameOfUser + " has left");

        // Inform all users one left
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();

            if (this.userBag.currentKey() != number) {
                this.userBag.currentUser().informUserLeft(nameOfUser);
            }
        }

        // remove that name from the Dispatcher frame
        this.gui.removeUser(nameOfUser + " (" + number + ")");

        // remove from internal list of users.
        this.userBag.remove(number);

        int nbUsers = this.userBag.size();

        // depending on nb users left, reset fields  
        switch (nbUsers) {
        case 0:
            this.lastUserID = 0;
            break;
        case 1:
            this.election.terminate();
            this.election = null; // when only one user in simulation, election should not be used, 

            break;
        default:
            this.election.setNbUsers(nbUsers);
            break;
        }
    }

    public void resetScene() {
        this.scene = createNewScene();
        allLog("The scene has been reset. Rendering...");
        render();
    }

    /**
     * Create and initialize the scene for the rendering picture.
     * @return The scene just created
     */
    private Scene createNewScene() {
        Scene scene = new Scene();

        /* Creates three spheres */
        Primitive p = new Sphere(new Vec(10, -5.77, 0), 7);
        p.setSurface(new Surface(new Vec(1, 0, 0), 0.7, 0.3, 14, 0, 1));
        scene.addPrimitive(p);

        p = new Sphere(new Vec(0, 11.55, 0), 7);
        p.setSurface(new Surface(new Vec(0, 1, 0), 0.7, 0.3, 14, 0, 1));
        scene.addPrimitive(p);

        p = new Sphere(new Vec(-10, -5.77, 0), 7);
        p.setSurface(new Surface(new Vec(0, 0, 1), 0.7, 0.3, 14, 0, 1));
        scene.addPrimitive(p);

        /* How about a Plane ? */
        p = new Plane(new Vec(0, 0, 1), 10);

        Vec cornSilk = new Vec(1, 248. / 255., 220. / 255.);

        //Vec nightBlue = new Vec(0.098, 0.098, 0.439);
        p.setSurface(new Surface(cornSilk, 0.7, 0.3, 14, 0, 1));
        scene.addPrimitive(p);

        /* Creates lights for the scene */
        scene.addLight(new Light(100, 100, -50, 1.0));
        scene.addLight(new Light(-100, 100, -50, 1.0));
        scene.addLight(new Light(100, -100, -50, 1.0));

        /* Creates a View (viewing point) for the rendering scene */
        View v = new View();
        scene.setView(v);

        return scene;
    }

    /** Find the name of the machine this Dispatcher is running on */
    public String getMachineName() {
        String hostName = "unknown";

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return hostName;
    }

    /** Find the name of the OS the Dispatcher is running on */
    public String getOSString() {
        return System.getProperty("os.name") + " " +
        System.getProperty("os.version");
    }

    /** Get the list of users in an asynchronous call, entries being separated by \n */
    public StringMutableWrapper getUserList() {
        StringBuffer s_list = new StringBuffer();

        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            s_list.append("  " + this.userBag.currentName() + "\n");
        }

        return new StringMutableWrapper(s_list.toString());
    }

    public void addSphere(Sphere s) {
        if ((this.election != null) && this.election.isRunning()) {
            allLog("A Sphere Cannot be added while election is running!");

            return;
        }

        this.scene.addPrimitive(s);
        allLog("A Sphere has been added\n" + s);
        render();
        log("Scene now contains " + this.scene.getNbPrimitives() + " spheres");
    }

    /** Shut down everything, send warning messages to users */
    public void exit() {
        allLog("Dispatcher closed, Exceptions may be generated...");

        try {
            gui.trash();
            proActiveDescriptor.killall(true);
            System.exit(0);
        } catch (Exception e) {
            logger.info(
                "C3DDispatcher: WARNING occurs when killing the application!");
        }
    }

    /** See how well the simulation improves with more renderers */
    public void doBenchmarks() {
        String benchFileName = "c3d_benchmark.plot";

        try {
            // open plot file
            RandomAccessFile plot = new RandomAccessFile(benchFileName, "rw");
            plot.seek(plot.length()); // Goes to EOF
            plot.writeChars("#" + new Date().toString() + "\n");
            plot.writeChars("# fields : <nb machines> <average time>\n");

            this.scene = createNewScene(); // make the scene
            rotateScene(0, new Vec(0, Math.PI / 4, 0)); // just a little turn, so it's not flat.

            Vec rotation = new Vec(0, 0, Math.PI / 4); // the rotation vector used
            int max = this.engineAndStringTable.size(); // nb of engines available

            for (int i = 1; i <= max; i++) {
                log("##########   Testing with " + i + " engine(s)");

                AverageMicroTimer timer = new AverageMicroTimer("Engines : " +
                        i);

                // A too long way to say "use only i engines"
                String[] enginesNowUsed = this.gui.setEngines(i);
                this.engineVector = new Vector<RenderingEngine>(); // equal to "for all engines, turnOff(engine)"

                for (int k = 0; k < enginesNowUsed.length; k++)
                    turnOnEngine(enginesNowUsed[k]);

                for (int j = 0; j < 16; j++) {
                    if (service.hasRequestToServe("doBenchmarks")) {
                        break;
                    }

                    rotateScene(-1, rotation);
                    timer.start();
                    render();
                    timer.stop();
                }

                if (service.hasRequestToServe("doBenchmarks")) {
                    // this is non blocking as we have just checked there was such a method
                    service.blockingRemoveOldest("doBenchmarks");
                    log("Test aborted!");
                    plot.writeChars("Test aborted!\n");

                    break;
                }

                log("End test " + i);
                // write to file + to system.out
                plot.writeChars(i + "\t" + timer.getAverage() + "\n");
                logger.info("-------------------- nb of renderers" + i +
                    "------------");
                logger.info(timer);
            }

            plot.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        log("End benchmark : file written " + benchFileName);
    }

    /** given an engine, adds it up to the engines available to the Dispatcher */
    public void addEngine(RenderingEngine engine, String name) {
        engineAndStringTable.add(new Object[] { engine, name });
        updateGUI();
    }

    /** given an engine, removes it from the engines used or left available by the Dispatcher */
    public void removeEngine(RenderingEngine engine) {
        int length = engineAndStringTable.size();
        String name = null;

        for (int i = 0; i < length; i++) {
            Object[] couple = engineAndStringTable.get(i);

            if (couple[0].equals(engine)) {
                name = (String) couple[1];
                engineAndStringTable.remove(i);

                if (engineVector.remove(engine)) {
                    logger.debug("Found engine in vector, removed!");
                } else {
                    logger.debug("Engine not found in vector!");
                }

                break;
            }
        }

        updateGUI();

        if (name == null) {
            throw new ArrayIndexOutOfBoundsException("Can't remove engine " +
                engine);
        }
    }

    /** Add to the GUI all the engines that where bound to the Dispatcher.
    * This also sets all engines to "available". */
    protected void updateGUI() {
        if (this.gui != null) {
            this.engineVector.removeAllElements();
            this.gui.noEngines();

            int length = engineAndStringTable.size();

            for (int i = 0; i < length; i++) {
                Object[] couple = engineAndStringTable.get(i);
                gui.addUsedEngine((String) couple[1]);
                turnOnEngine((String) couple[1]);
            }
        }
    }

    /** Makes the engine participate in the computation of images */
    public void turnOnEngine(String engineName) {
        this.engineVector.add(nameToEngine(engineName));
    }

    /** Stops the engine from participating in the computation of images*/
    public void turnOffEngine(String engineName) {
        engineVector.remove(nameToEngine(engineName));
    }

    /**
     * Transforms a String representing an Engine to the C3DRenderingEngine associated.
     * @throws ArrayIndexOutOfBoundsException if no engine with such name
     */
    private RenderingEngine nameToEngine(String name) {
        int length = engineAndStringTable.size();

        for (int i = 0; i < length; i++) {
            Object[] couple = engineAndStringTable.get(i);

            if (couple[1].equals(name)) {
                return (RenderingEngine) couple[0];
            }
        }

        throw new ArrayIndexOutOfBoundsException("Can't find engine named " +
            name);
    }

    /**
     * Transforms an Engine into its String, WITHOUT asking the Engine (which is remote)
     * @throws ArrayIndexOutOfBoundsException if engine is not registered
     */
    private String engineToName(RenderingEngine engine) {
        int length = engineAndStringTable.size();

        for (int i = 0; i < length; i++) {
            Object[] couple = engineAndStringTable.get(i);

            if (couple[0].equals(engine)) {
                return (String) couple[1];
            }
        }

        throw new ArrayIndexOutOfBoundsException("Can't find name of " +
            engine);
    }

    /**
     * Instanciates a new active C3DDispatcher on the local machine
     * @param argv Name of the descriptor file
     */
    public static void main(String[] argv) throws NodeException {
        ProActiveDescriptor proActiveDescriptor = null;
        ProActiveConfiguration.load();

        try {
            if (argv.length == 0) {
                proActiveDescriptor = ProDeployment.getProactiveDescriptor();
            } else {
                proActiveDescriptor = ProDeployment.getProactiveDescriptor(
                        "file:" + argv[0]);
            }

            proActiveDescriptor.activateMappings();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Pb in main, trouble loading descriptor");
        }

        String[] rendererNodes = proActiveDescriptor.getVirtualNode("Renderer")
                                                    .getNodesURL();
        Object[] param = new Object[] { rendererNodes, proActiveDescriptor };

        Node dispatcherNode = proActiveDescriptor.getVirtualNode("Dispatcher")
                                                 .getNode();

        try {
            ProActiveObject.newActive(C3DDispatcher.class.getName(), param,
                dispatcherNode);
        } catch (Exception e) {
            logger.error("Problemn with C3DDispatcher Active Object creation:");
            e.printStackTrace();
        }
    }
}
