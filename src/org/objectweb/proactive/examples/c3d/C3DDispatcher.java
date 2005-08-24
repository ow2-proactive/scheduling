/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
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

import org.apache.log4j.Logger;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.timer.AverageMicroTimer;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.examples.c3d.geom.Scene;
import org.objectweb.proactive.examples.c3d.geom.Vec;
import org.objectweb.proactive.examples.c3d.gui.DispatcherGUI;
import org.objectweb.proactive.examples.c3d.gui.DispatcherGUIImpl;
import org.objectweb.proactive.examples.c3d.prim.Light;
import org.objectweb.proactive.examples.c3d.prim.Plane;
import org.objectweb.proactive.examples.c3d.prim.Primitive;
import org.objectweb.proactive.examples.c3d.prim.Sphere;
import org.objectweb.proactive.examples.c3d.prim.Surface;
import org.objectweb.proactive.examples.c3d.prim.View;

import java.io.IOException;
import java.io.RandomAccessFile;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Date;
import java.util.Vector;


/**
 * This class is the bridge between renderers and users.
 * It handles the logic of asking renderers to draw partial images. It then forwards them to the users.
 * It also allows users to hold conversations, in a chat-like way.
 */
public class C3DDispatcher implements RunActive {
    private static Logger logger = Logger.getLogger(Loggers.EXAMPLES);
    private static int IMAGE_HEIGHT = 250;
    private static int IMAGE_WIDTH = 250;

    /*
     * Stores the users in a bag containing ref to active object, name and identifier
     */
    private UserBag userBag = new UserBag();
    private int i_lastuser = 0;

    /**
     * Interval stack; each interval holds information regarding its
     * height, width and relative position within the whole image
     */
    /**
     * Hashtable of the rendering engines
     */
    /**  connects an Engine to its name, and other way round, without asking the remote object */
    private Vector engineAndStringTable = new Vector();

    /** list of engines. */
    public Vector engineVector = new Vector();

    /**
     * Scene to be rendered; set by the first user frame to register;
     * contains lights, spheres and one view
     */
    private Scene scene;

    /**
     * Pixel array to store the rendered pixels; used to initialize the
     * image on new-coming user frames, actualized in setPixels
     */
    private int[] localCopyOfImage;

    /**
     * ProactiveDescriptor object for the dispatcher
     */
    private ProActiveDescriptor proActiveDescriptor;
    private String[] rendererNodes;

    /**
     * The unique GUI reference. All GUI actions use this pointer.
     */
    private transient DispatcherGUI gui;
    private transient C3DDispatcher me;
    private BlockingRequestQueue requestQueue;

    /**
     * The no-argument Constructor as commanded by ProActive;
     * otherwise unused
     */
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
     * Second Constructor, does all the creation and linking.
     * Creates the renderers, and the GUI
     */
    public void go() {
        this.me = (C3DDispatcher) ProActive.getStubOnThis();
        try {
            ProActive.register(me, "//localhost/" + "Dispatcher");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        this.gui = new DispatcherGUIImpl("C3D Dispatcher", this.me);
        try {

            /* Creates rendering engines */
            for (int i = 0; i < this.rendererNodes.length; i++) {
                String engineName = this.rendererNodes[i].toString();

                // toString() is to express the change, even though rendererNodes are Strings... 
                Object[] param = { engineName }; // engine name = engineNode name
                C3DRenderingEngine tmpEngine = (C3DRenderingEngine) ProActive.newActive("org.objectweb.proactive.examples.c3d.C3DRenderingEngine",
                        param, this.rendererNodes[i]);

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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }

        //System.out.println("String 2 engine : " + stringToEngineHash);
    }

    /**
     * Appends message to the end of the list
     */
    private void log(String s_message) {
        this.gui.log(s_message + "\n");
    }

    // used to count arrived Intervals in the received vector
    private int pointer;

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

        C3DRenderingEngine[] engine = (C3DRenderingEngine[]) engineVector.toArray(new C3DRenderingEngine[0]);

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
        Vector images = new Vector();
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
    private Image2D getReturned(Vector images, boolean[] received) {
        int index = ProActive.waitForAny(images);
        Image2D returnedImage = (Image2D) images.remove(index);
        setPixels(returnedImage);
        received[returnedImage.getInterval().number] = true;
        return returnedImage;
    }

    /**
     * Checks whether ALL intervals have spawn an Image2D.
     * This aims at having several renderers handling the same Interval ==> [fault tolerance] & speedup.
     * // TODO : real fault tolerance requires surrounding distant calls with try catch & timeouts.
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
        // TODO: this could be a group comm  : userGroup.setPixels(image);
        // Delivers the new pixels to all users
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
    public void rotateScene(int i_user, Vec angles) {
        int objects = this.scene.getNbPrimitives();

        /* on every object ... */
        for (int i = 0; i < objects; i++) {
            Primitive p = this.scene.getPrimitive(i);
            p.rotate(angles);
            this.scene.setPrimitive(p, i);
        }

        /* re-renders the image to reflect the rotation */
        render();
    }

    /**
     * ProActive queue handling
     */
    public void runActivity(Body body) {
        // Creates the rendering engines 
        go();
        requestQueue = body.getRequestQueue();

        // Loops over lifetime
        while (body.isActive()) {

            /* Waits on any method call */
            Request r = requestQueue.blockingRemoveOldest();
            String methodName = r.getMethodName();
            if (methodName.equals("rotateScene")) {
                processRotate(body, r);
            } else if (!Election.isRunning()) {
                // No election and the method isn't a rotation
                body.serve(r);
            } else if (methodName.equals("addSphere")) {
                // There is an election and addsphere comes..
                // nothing happens...
                allLog("Cannot add spheres while an election is running");
            } else {
                // There is a running election, the method is not rotate nor addshpere
                body.serve(r);
            }
        }
    }

    /**
     * check a demand for rotation is valid, and then possibly starts election
     */
    public void processRotate(Body body, Request r) {
        int i_user = 0;
        Vec rotateVec = null;
        i_user = ((Integer) r.getParameter(0)).intValue();
        rotateVec = (Vec) r.getParameter(1);
        if (Election.isRunning()) {
            int nb_votes = Election.vote(i_user, rotateVec);
            if (nb_votes == this.userBag.size()) {
                Election.finish();
            }
        } else if (this.userBag.size() == 1) {
            userLog(i_user, "Scene is being spun along " +
                rotateVec.direction());
            body.serve(r);
        } else {
            Election.newElection(i_user, rotateVec, this);
        }
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

    /**
     * Instanciates a new active C3DDispatcher on the local machine
     * @param argv Name of the descriptor file
     */
    public static void main(String[] argv) throws NodeException {
        ProActiveDescriptor proActiveDescriptor = null;
        ProActiveConfiguration.load(); // this line also registers the dispatcher in the registry!!!

        try {
            proActiveDescriptor = ProActive.getProactiveDescriptor("file:" +
                    argv[0]);
            proActiveDescriptor.activateMappings();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Pb in main, trouble loading descriptor");
        }

        String[] rendererNodes = proActiveDescriptor.getVirtualNode("Renderer")
                                                    .getNodesURL();
        Object[] param = new Object[] { rendererNodes, proActiveDescriptor };

        Node node = proActiveDescriptor.getVirtualNode("Dispatcher").getNode();
        try {
            ProActive.newActive("org.objectweb.proactive.examples.c3d.C3DDispatcher",
                param, node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Register a user, so he can join the fun */

    //SYNCHRONOUS CALL. All "c3duser." calls in this method happen AFTER the int[] is returned
    public int[] registerUser(C3DUser c3duser, String userName) {
        c3duser.log("-> Remote call-back: dispatcher found, user registered");
        log("New user " + userName + "(" + this.i_lastuser + ") has joined");

        // Informs the other users of the new user
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            this.userBag.currentUser().informNewUser(this.i_lastuser, userName);
            c3duser.informNewUser(this.userBag.currentKey(),
                this.userBag.currentName());
        }

        // Adds this User to the list 
        this.userBag.add(this.i_lastuser, c3duser, userName);
        this.gui.addUser(userName + " (" + this.i_lastuser + ")");

        //  Do some initialization, if this was the first consumer to register
        if (this.userBag.size() == 1) {

            /* Sets the scene */
            this.scene = createNewScene();

            /* Initializes local image properties */
            this.localCopyOfImage = new int[IMAGE_WIDTH * IMAGE_HEIGHT];

            /* Creates the intervals, starts the calculation */
            render();
        } else {

            /* Initializes the image of the new-coming consumer */
            Interval inter = new Interval(0, IMAGE_WIDTH, IMAGE_HEIGHT, 0,
                    IMAGE_HEIGHT);
            c3duser.setPixels(new Image2D(this.localCopyOfImage, inter, 0));
        }

        // return user_id, image_width & image_height;
        int[] result = new int[] { this.i_lastuser++, IMAGE_WIDTH, IMAGE_HEIGHT };
        return result;
    }

    public void registerMigratedUser(int userNumber) {
        String name = this.userBag.getName(userNumber);
        log("User " + name + "(" + userNumber + ") has migrated ");
        C3DUser c3duser = this.userBag.getUser(userNumber);

        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            if (this.userBag.currentKey() != userNumber) {
                c3duser.informNewUser(this.userBag.currentKey(),
                    this.userBag.currentName());
            }
        }

        /* Initializes the image of the migrated consumer */
        Interval inter = new Interval(0, IMAGE_WIDTH, IMAGE_HEIGHT, 0,
                IMAGE_HEIGHT);
        c3duser.setPixels(new Image2D(this.localCopyOfImage, inter, 0));
    }

    /** removes user from userList, so he cannot receive any more messages or images */
    public void unregisterConsumer(int number) {
        allLogExcept(number, "User " + nameOfUser(number) + " left");

        // Inform all users one left
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            if (this.userBag.currentKey() != number) {
                this.userBag.currentUser().informUserLeft(nameOfUser(number));
            }
        }

        // remove that name from the Dispatcher frame
        this.gui.removeUser(nameOfUser(number) + " (" + number + ")");

        // remove from internal list of users.
        this.userBag.remove(number);

        // if no more users, reset all numbers to zero
        if (this.userBag.size() == 0) {
            this.i_lastuser = 0;
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
    public StringWrapper getUserList() {
        StringBuffer s_list = new StringBuffer();
        for (this.userBag.newIterator(); this.userBag.hasNext();) {
            this.userBag.next();
            s_list.append("  " + this.userBag.currentName() + "\n");
        }
        return new StringWrapper(s_list.toString());
    }

    /** transforms an id in a name */
    String nameOfUser(int i_user) {
        return this.userBag.getName(i_user);
    }

    public void addSphere(Sphere s) {
        // Can only add sphere if there is no rendering going on
        this.scene.addPrimitive(s);
        allLog("A Sphere has been added\n" + s);
        render();
        log("Scene now contains " + this.scene.getNbPrimitives() + " spheres");
    }

    /** Shut down everything, send warning messages to users */
    public void exit() {
        allLog("Dispatcher closed, Exceptions may be generated...");
        try {
            proActiveDescriptor.killall(true);
            gui.trash();
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
                this.engineVector = new Vector(); // equal to "for all engines, turnOff(engine)"
                for (int k = 0; k < enginesNowUsed.length; k++)
                    turnOnEngine(enginesNowUsed[k]);

                for (int j = 0; j < 16; j++) {
                    if (requestQueue.hasRequest("doBenchmarks")) {
                        break;
                    }
                    rotateScene(0, rotation);
                    timer.start();
                    render();
                    timer.stop();
                }
                if (requestQueue.hasRequest("doBenchmarks")) {
                    log("Test aborted!");
                    plot.writeChars("Test aborted!\n");
                    requestQueue.removeOldest("doBenchmarks");
                    break;
                }

                log("End test " + i);
                // write to file + to system.out
                plot.writeChars(i + "\t" + timer.getAverage() + "\n");
                logger.info("-------------------- nb of machines " + i +
                    "------------");
                logger.info(timer);
            }
            plot.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        log("End benchmark : file written " + benchFileName);
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
    private C3DRenderingEngine nameToEngine(String name) {
        int length = engineAndStringTable.size();
        for (int i = 0; i < length; i++) {
            Object[] couple = (Object[]) engineAndStringTable.get(i);
            if (couple[1].equals(name)) {
                return (C3DRenderingEngine) couple[0];
            }
        }
        throw new ArrayIndexOutOfBoundsException("Can't find engine named " +
            name);
    }

    /**
     * Transforms an Engine into its String, WITHOUT asking the Engine (which is remote)
     * @throws ArrayIndexOutOfBoundsException if engine is not registered
     */
    private String engineToName(C3DRenderingEngine engine) {
        int length = engineAndStringTable.size();
        for (int i = 0; i < length; i++) {
            Object[] couple = (Object[]) engineAndStringTable.get(i);
            if (couple[0].equals(engine)) {
                return (String) couple[1];
            }
        }
        throw new ArrayIndexOutOfBoundsException("Can't find name of " +
            engine);
    }
}
