package matlabcontrol;

/*
 * Copyright (c) 2010, Joshua Kaplan
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of matlabcontrol nor the names of its contributors may
 *    be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Use this class to create instances of {@link RemoteMatlabProxy}. Creating a
 * proxy will launch MATLAB. Each proxy created will control the session
 * launched.
 *
 * @author <a href="mailto:jak2@cs.brown.edu">Joshua Kaplan</a>
 */
public class RemoteMatlabProxyFactory {
    /**
     * A timer that periodically checks if the proxies are still connected.
     */
    private Timer _connectionTimer;

    /**
     * Listeners for when connections are established and lost.
     *
     * @see #addConnectionListener(MatlabConnectionListener)
     * @see #removeConnectionListener(MatlabConnectionListener)
     */
    private final Vector<MatlabConnectionListener> _listeners = new Vector<MatlabConnectionListener>();

    /**
     * The instance of the {@link MatlabProcessCreator} that can spawn the MATLAB process.
     */
    private final MatlabProcessCreator _matlabCreator;

    /**
     * The location of this support code. This location is provided to MATLAB
     * so that it can add the location of this code to its classpath.
     */
    private final String _supportCodeLocation;

    /**
     * Default number of milliseconds to wait for a MatlabInternalProxy to be
     * received.
     */
    private static final int DEFAULT_TIMEOUT = 180000;

    /**
     * Map of proxyIDs to {@link RemoteMatlabProxy} instances.
     */
    private final Map<String, RemoteMatlabProxy> _proxies = new HashMap<String, RemoteMatlabProxy>();

    /**
     * The RMI registry used to communicate between JVMs. There is only ever
     * one registry actually running on a given machine, so multiple distinct
     * programs making use of matlabcontrol all share the same underlying
     * registry (although the Java object will be different).
     */
    private static Registry _registry = null;

    private static int rmiport = Registry.REGISTRY_PORT;

    /**
     * Receiver for proxies created and sent over RMI.
     */
    private final MatlabInternalProxyReceiver _receiver = new ProxyReceiver();

    /**
     * Value used to bind the {@link ProxyReceiver}, as a
     * {@link MatlabInternalProxyReceiver} so that it can be retrieved from
     * within the MATLAB JVM with this value.
     */
    private final String _receiverID = getRandomValue();

    /**
     * Used to notify listeners on a separate thread that a connection has been
     * lost or gained. This is done so that whatever code is executed by the
     * user of this API when they are notified of a connection being lost or
     * received does not interfere with the essential operations of creating
     * and receiving proxies by holding up the thread.
     */
    private ExecutorService _connectionExecutor = Executors.newSingleThreadExecutor();

    private Process theMatlabProcess;

    /**
     * Constructs this factory with a specified instance of {@link MatlabProcessCreator}.
     * Typically this will not be necessary and so using the
     * {@link #RemoteMatlabProxyFactory() the other constructor} will be the
     * preferred option. If it is invalid then an exception will be thrown
     * when creating a proxy with {@link #requestProxy()}, {@link #getProxy()},
     * or {@link #getProxy(long)}.
     *
     * @param creator the instance of {@link MatlabProcessCreator} that is in charge
     *                of creating the MATLAB process
     * @throws MatlabConnectionException thrown if the initialization necessary
     *                                   for connecting to MATLAB cannot be
     *                                   properly configured
     */
    public RemoteMatlabProxyFactory(MatlabProcessCreator creator) throws MatlabConnectionException {

        //The matlab process creator
        this._matlabCreator = creator;

        //Location of where this code is
        this._supportCodeLocation = Configuration.getSupportCodeLocation();

        //Initialize the registry
        initRegistry();

        //Bind the receiver to be retrieved from MATLAB
        this.bindReceiver();
    }

    /**
     * Constructs this factory with a specified location or alias for the
     * MATLAB executable. Typically this will not be necessary and so using the
     * {@link #RemoteMatlabProxyFactory() the other constructor} will be the
     * preferred option. The location or alias specified by
     * <code>matlabLocation</code> will not be verified at construction time,
     * and so if the value is invalid it will not cause this constructor to
     * throw an exception. If it is invalid then an exception will be thrown
     * when creating a proxy with {@link #requestProxy()}, {@link #getProxy()},
     * or {@link #getProxy(long)}.
     *
     * @param matlabLocation the location of MATLAB
     * @throws MatlabConnectionException thrown if the initialization necessary
     *                                   for connecting to MATLAB cannot be
     *                                   properly configured
     */
    public RemoteMatlabProxyFactory(String matlabLocation) throws MatlabConnectionException {

        //Use the previous constructor with a default implementation of the MatlabProcessCreator
        this(new MatlabProcessCreatorImpl(matlabLocation));
    }

    /**
     * Constructs this factory with a default location or alias for the MATLAB
     * executable (on an operating system specific basis). If that default does
     * not work, then construct this class with
     * {@link #RemoteMatlabProxyFactory(String) the other constructor} so that
     * the correct location or alias can be specified.
     *
     * @throws MatlabConnectionException thrown if the initialization necessary
     *                                   for connecting to MATLAB cannot be
     *                                   properly configured
     */
    public RemoteMatlabProxyFactory() throws MatlabConnectionException {
        this(Configuration.getMatlabLocation());
    }

    /**
     * Initializes the registry if it has not already been set up. Specifies
     * the codebase so that paths with spaces in them will work properly.
     *
     * @throws MatlabConnectionException
     */
    private static void initRegistry() throws MatlabConnectionException {
        System.out.println("initRegistry()");
        System.out.flush();
        //If the registry hasn't been created

        if (_registry == null) {
            System.out.println("IS NULL _______ initRegistry()");
            System.out.flush();
            System.out.println("Create New Registry");
            System.out.flush();
            Random rnd = new Random();

            int initialPort = rnd.nextInt(65536 - 1024) + 1024;
            for (rmiport = (initialPort == 65535) ? 1024 : (initialPort + 1); rmiport != initialPort; rmiport = (rmiport == 65535) ? 1024
                    : (rmiport + 1)) {
                try {
                    _registry = LocateRegistry.createRegistry(rmiport);
                    break;
                } catch (Exception e) {
                    // Try another port
                }
            }

            System.out.println("Registry created on port :" + rmiport);
            System.out.flush();

            //Tell the code base where it is, and just to be safe force it to use it
            //(This is necessary so that paths with spaces work properly)
            System.setProperty("java.rmi.server.codebase", Configuration.getCodebaseLocation());
            System.setProperty("java.rmi.server.useCodebaseOnly", "true");
        } else {

            System.out.println("IS NOT NULL initRegistry()");
            System.out.flush();
            try {
                String[] list = _registry.list();
                System.out.println("Previous registry:");
                System.out.println(Arrays.toString(list));
                System.out.flush();
                for (String key : list) {
                    _registry.unbind(key);


                }
            } catch (Exception ex) {
                throw new MatlabConnectionException("Could not create or connect to the RMI registry", ex);
            }
        }

    }

    /**
     * Binds the receiver for RMI so it can be retrieved from the MATLAB JVM
     * as a {@link MatlabInternalProxyReceiver}.
     *
     * @throws MatlabConnectionException
     */
    private void bindReceiver() throws MatlabConnectionException {
        try {
            _registry.bind(_receiverID, UnicastRemoteObject.exportObject(_receiver, 0));
        } catch (Exception e) {
            throw new MatlabConnectionException("Could not bind proxy receiever to the RMI registry", e);
        }
    }

    /**
     * Receives the inner proxy from MATLAB. This inner class exists to hide
     * the {@link MatlabInternalProxyReceiver#registerControl(String, MatlabInternalProxy)}
     * method which must be public because it is implementing an interface;
     * however, this method should not be visible to users of the API so
     * instead it is hidden inside of this private class.
     */
    private class ProxyReceiver implements MatlabInternalProxyReceiver {
        /**
         * This method is to be called by {@link MatlabConnector} running inside
         * of the MATLAB JVM.
         *
         * @param proxyID       the identifier for this proxy
         * @param internalProxy the proxy used internally
         */
        public void registerControl(String proxyID, MatlabInternalProxy internalProxy) {
            //Create proxy, store it
            RemoteMatlabProxy proxy = new RemoteMatlabProxy(internalProxy, proxyID, RemoteMatlabProxyFactory.this);
            _proxies.put(proxyID, proxy);

            //Wake up the thread potentially waiting for the proxy
            synchronized (RemoteMatlabProxyFactory.this) {
                RemoteMatlabProxyFactory.this.notifyAll();
            }
            RemoteMatlabProxyFactory.this.connectionEstablished(proxy);

            //Create the timer, if necessary, which checks if proxies are still connected
            RemoteMatlabProxyFactory.this.initConnnectionTimer();
        }
    }

    /**
     * Generates a random value to be used in binding and proxy IDs.
     *
     * @return random value
     */
    private static String getRandomValue() {
        return UUID.randomUUID().toString();
    }

    /**
     * Requests a {@link RemoteMatlabProxy}. When the proxy has been made
     * (there is a possibility it will not be if errors occur), all
     * listeners will be notified. The identifier of the proxy that will be
     * created is returned. A proxy's identifier can be accessed by calling
     * {@link RemoteMatlabProxy#getIdentifier()}.
     *
     * @return proxy's unique identifier
     * @throws MatlabConnectionException
     * @see #addConnectionListener(MatlabConnectionListener)
     * @see RemoteMatlabProxy#getIdentifier()
     * @see #getProxy()
     * @see #getProxy(long)
     */
    public String requestProxy() throws MatlabConnectionException {
        //Unique ID for proxy
        final String proxyID = getRandomValue();

        //Argument that MATLAB will run on start.
        //Tells MATLAB to add this code to its classpath, then to call a method which
        //will create a proxy and send it over RMI back to this JVM.
        final String runArg = "javaaddpath '" + _supportCodeLocation + "'; " +
                MatlabConnector.class.getName() +
                ".connectFromMatlab('" + _receiverID + "', '" + proxyID + "', " + rmiport + ");";

        //Create the MATLAB process that will run the argument
        try {
            this.theMatlabProcess = this._matlabCreator.createMatlabProcess(runArg);
        } catch (MatlabConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new MatlabConnectionException("Unable to create the matlab process", e);
        }

        return proxyID;
    }

    /**
     * Returns a {@link RemoteMatlabProxy}. This will take some time as it
     * involves launching MATLAB. If a connection cannot be established within
     * 60 seconds then this method will end execution and an exception will be
     * thrown.
     *
     * @return proxy
     * @throws MatlabConnectionException
     * @see #requestProxy()
     * @see #getProxy(long)
     */
    public RemoteMatlabProxy getProxy() throws MatlabConnectionException {
        return this.getProxy(DEFAULT_TIMEOUT);
    }

    /**
     * Returns a {@link RemoteMatlabProxy}. This will take some time as it
     * involves launching MATLAB. If a connection cannot be established within
     * the specified number of milliseconds specified by <code>timeout</code>
     * then this method will end execution and an exception will be thrown.
     *
     * @param timeout time to wait in milliseconds for a proxy to be created
     * @return proxy
     * @throws MatlabConnectionException
     * @see #requestProxy()
     * @see #getProxy()
     */
    public RemoteMatlabProxy getProxy(long timeout) throws MatlabConnectionException {

        String proxyID = this.requestProxy();

        //Wait until the controller is received or until timeout
        synchronized (this) {
            try {
                this.wait(timeout);
            } catch (InterruptedException e) {
                throw new MatlabConnectionException("Thread was interrupted while waiting for MATLAB proxy", e);
            }
        }

        //If the proxy has not be received before the timeout
        if (!_proxies.containsKey(proxyID)) {
            theMatlabProcess.destroy();
            // Read the log file
            String output = getLogFileOutput();

            throw new MatlabConnectionException("MATLAB proxy could not be created in the specified amount " +
                    "of time: " + timeout + " milliseconds. The matlab output :" + "\n" + output);
        }


        return _proxies.get(proxyID);
    }

    public String getLogFileOutput() {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(this._matlabCreator.getLogFile()));
            String str;
            while ((str = in.readLine()) != null) {
                builder.append(str + System.getProperty("line.separator"));
            }
            in.close();

        } catch (IOException e) {
        }
        return builder.toString();
    }

    /**
     * Adds a listener to be notified when MATLAB connections are established
     * and lost.
     *
     * @param listener
     */
    public void addConnectionListener(MatlabConnectionListener listener) {
        _listeners.add(listener);
    }

    /**
     * Removes a listener so that it is no longer notified.
     *
     * @param listener
     */
    public void removeConnectionListener(MatlabConnectionListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Called when a connection has been established.
     * <br><br>
     * Notify the listeners that the connection has been established
     * in a separate thread so that it whatever users of this API
     * are doing it does not interfere.
     *
     * @param proxy
     */
    private void connectionEstablished(final RemoteMatlabProxy proxy) {
        _connectionExecutor.submit(new Runnable() {
            public void run() {
                for (MatlabConnectionListener listener : _listeners) {
                    listener.connectionEstablished(proxy);
                }
            }
        });
    }

    /**
     * Called when a connection has been lost.
     * <br><br>
     * Notify the listeners that the connection has been lost
     * in a separate thread so that it whatever users of this API
     * are doing it does not interfere with checking the proxies.
     */
    private void connectionLost(final RemoteMatlabProxy proxy) {
        _connectionExecutor.submit(new Runnable() {
            public void run() {
                for (MatlabConnectionListener listener : _listeners) {
                    listener.connectionLost(proxy);
                }
            }
        });
    }

    /**
     * Creates a timer, if it does not already exist, to check for lost proxy
     * connections.
     */
    private void initConnnectionTimer() {
        //If there is no timer yet, create a timer to monitor the connections
        if (_connectionTimer == null) {
            _connectionTimer = new Timer();
            _connectionTimer.schedule(new TimerTask() {
                public void run() {
                    RemoteMatlabProxyFactory.this.checkConnections();
                }
            }, 1000, 1000);
        }
    }

    private void cancelConnnectionTimer() {
        if (_connectionTimer != null) {
            _connectionTimer.cancel();
        }
    }

    public void clean() {
        try {
            _registry.unbind(_receiverID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            UnicastRemoteObject.unexportObject(this._receiver, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        cancelConnnectionTimer();
        _connectionExecutor.shutdownNow();
        _connectionExecutor = null;
        _proxies.clear();


        threadsCleaning();
    }


    private static void threadsCleaning() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup().getParent();
        Thread[] threads = new Thread[200];
        int len = tg.enumerate(threads, true);
        int nbKilled = 0;

        for (int i = 0; i < len; i++) {
            Thread ct = threads[i];

            if ((ct.getName().indexOf("RMI RenewClean") >= 0) ||
                    (ct.getName().indexOf("ThreadInThePool") >= 0)) {

                try {
                    ct.interrupt();
                } catch (Exception e) {
                }


                nbKilled++;
                if (ct.isAlive()) {
                    try {
                        ct.stop();
                    } catch (Exception e) {
                    }
                }

            }
        }

        System.err.println(nbKilled + " thread(s) stopped on " + len);
    }


    /**
     * Checks the connections to MATLAB. If a connection has died, the
     * listeners are informed and all references to it by this class
     * are removed.
     */
    private void checkConnections() {
        //Proxies that have become disconnected
        final Vector<RemoteMatlabProxy> disconnectedProxies = new Vector<RemoteMatlabProxy>();

        //Check each proxy's connection, if it has died add to disconnectedProxies
        synchronized (_proxies) {
            Vector<String> proxyKeys = new Vector<String>(_proxies.keySet());
            for (String proxyKey : proxyKeys) {
                RemoteMatlabProxy proxy = _proxies.get(proxyKey);
                if (!proxy.isConnected()) {
                    _proxies.remove(proxyKey);
                    disconnectedProxies.add(proxy);
                }
            }
        }

        //Notify the listeners of the disconnected proxies
        for (RemoteMatlabProxy proxy : disconnectedProxies) {
            this.connectionLost(proxy);
        }
    }
}