/*
 * Created by IntelliJ IDEA.
 * User: fhuet
 * Date: Apr 17, 2002
 * Time: 7:09:56 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package org.objectweb.proactive.ext.mixedlocation;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;

import java.io.IOException;

public class UniversalBodyWrapper implements UniversalBody, Runnable {

    protected UniversalBody wrappedBody;
    protected long time;
    protected UniqueID id;

    /**
     * Create a time-limited wrapper around a UniversalBody
     * @param body the wrapped UniversalBody
     * @param time the life expectancy of this wrapper in milliseconds
     */
    public UniversalBodyWrapper(UniversalBody body, long time) {
        this.wrappedBody = body;
        this.time = time;
        Thread t = new Thread(this);
        this.id = this.wrappedBody.getID();
        t.start();
    }

    public void receiveRequest(Request request) throws IOException {
       System.out.println("UniversalBodyWrapper.receiveRequest");
        if (this.wrappedBody == null) {
            throw new IOException();
        }
        try    {
        this.wrappedBody.receiveRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void receiveReply(Reply r) throws IOException {
        this.wrappedBody.receiveReply(r);
    }

    public String getNodeURL() {
        return this.wrappedBody.getNodeURL();
    }

    public UniqueID getID() {
        return this.id;
    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {
        this.wrappedBody.updateLocation(id, body);
    }

    public UniversalBody getRemoteAdapter() {
        return this.wrappedBody.getRemoteAdapter();
    }
    
    public void enableAC() throws java.io.IOException {
    	this.wrappedBody.enableAC();
    }
    
    public void disableAC() throws java.io.IOException {
    	this.wrappedBody.disableAC();
    }

    protected void updateServer() {
        System.out.println("UniversalBodyWrapper.updateServer");
        LocationServer server = LocationServerFactory.getLocationServer();
        try {
            server.updateLocation(id, this.wrappedBody);
        } catch (Exception e) {
            System.out.println("XXXX Error XXXX");
           // e.printStackTrace();
        }
    }

    public void run() {
        System.out.println("UniversalBodyWrapper.run life expectancy " + time);
        try {
            Thread.currentThread().sleep(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("UniversalBodyWrapper.run end of life...");
        this.updateServer();
        this.wrappedBody = null;
        System.gc();
    }
}
