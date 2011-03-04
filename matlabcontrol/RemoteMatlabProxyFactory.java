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
     * Specified location of MATLAB executable. If none is ever provided then
     * an OS specific value is used.
     */
    private final String _matlabLocation;


    private final String[] _startupOptions;

    /**
     * The location of this support code. This location is provided to MATLAB
     * so that it can add the location of this code to its classpath.
     */
    private final String _supportCodeLocation;

    /**
     * Default number of milliseconds to wait for a MatlabInternalProxy to be
     * received.
     */
    private static final int DEFAULT_TIMEOUT = 60000;

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
    private final ExecutorService _connectionExecutor = Executors.newSingleThreadExecutor();

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
     * @param matlabLocation
     * @throws MatlabConnectionException thrown if the initialization necessary
     *                                   for connecting to MATLAB cannot be
     *                                   properly configured
     */
    public RemoteMatlabProxyFactory(String matlabLocation) throws MatlabConnectionException {
        //Store location/alias of the MATLAB executable
        _matlabLocation = matlabLocation;

        //Location of where this code is
        _supportCodeLocation = Configuration.getSupportCodeLocation();

        _startupOptions = Configuration.getMatlabStartupOptions();

        //Initialize the registry
        initRegistry();

        //Bind the receiver to be retrieved from MATLAB
        this.bindReceiver();
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
        //If the registry hasn't been created
        if (_registry == null) {
            //Create a RMI registry
            try {
                _registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
            }
            //If we can't create one, try to retrieve an existing one
            catch (Exception e) {
                try {
                    _registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
                } catch (Exception ex) {
                    throw new MatlabConnectionException("Could not create or connect to the RMI registry", ex);
                }
            }

            //Tell the code base where it is, and just to be safe force it to use it
            //(This is necessary so that paths with spaces work properly)
            System.setProperty("java.rmi.server.codebase", Configuration.getCodebaseLocation());
            System.setProperty("java.rmi.server.useCodebaseOnly", "true");
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
            RemoteMatlabProxy proxy = new RemoteMatlabProxy(internalProxy, proxyID);
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
     * Returns the location or alias of the MATLAB program. If no location or
     * alias was assigned when constructing this factory then the value
     * returned will be the default which differs depending on the operating
     * system this code is executing on.
     *
     * @return MATLAB location
     */
    public String getMatlabLocation() {
        return _matlabLocation;
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
        String proxyID = getRandomValue();

        //Argument that MATLAB will run on start.
        //Tells MATLAB to add this code to its classpath, then to call a method which
        //will create a proxy and send it over RMI back to this JVM.
        String runArg = "javaaddpath '" + _supportCodeLocation + "'; " +
                MatlabConnector.class.getName() +
                ".connectFromMatlab('" + _receiverID + "', '" + proxyID + "');";

        //Attempt to run MATLAB
        try {
            ArrayList<String> commandList = new ArrayList();
            commandList.add(_matlabLocation);
            commandList.addAll(Arrays.asList(_startupOptions));
            commandList.add("-r");
            commandList.add(runArg);

            String command[] = (String[]) commandList.toArray(new String[commandList.size()]);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            throw new MatlabConnectionException("Could not launch MATLAB. Used location/alias: " + _matlabLocation, e);
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
            throw new MatlabConnectionException("MATLAB proxy could not be created in the specified amount " +
                    "of time: " + timeout + " milliseconds");
        }

        return _proxies.get(proxyID);
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