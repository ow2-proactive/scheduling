/*
 * RSHJVMProcessImpl.java
 *
 * Copyright 1997 - 2001 INRIA Project Oasis. All Rights Reserved.
 *
 * This software is the proprietary information of INRIA Sophia Antipolis.
 * 2004 route des lucioles, BP 93 , FR-06902 Sophia Antipolis
 * Use is subject to license terms.
 *
 * @author  ProActive Team
 * @version ProActive 0.9 (November 2001)
 *
 * ===================================================================
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL INRIA, THE OASIS PROJECT OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ===================================================================
 *
 */
package modelisation.server.fakebench;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.LocationServerFactory;
import modelisation.server.TimedLocationServerMetaObjectFactory;

public class FakeBench implements org.objectweb.proactive.RunActive {

    protected LocationServer l;
    protected long delay;

    public FakeBench() {
    }

    public FakeBench(Long delay) {
        this.delay = delay.longValue();
    }

    public void runActivity(Body b) {
        l = LocationServerFactory.getLocationServer();
        BlockingRequestQueue queue = b.getRequestQueue();
        while (b.isActive()) {
            while (!queue.isEmpty()) {
                b.serve(queue.removeOldest());
            }
            updateLocation(b.getID(), b.getRemoteAdapter());
            this.haveABreak();
            queryServer(b.getID());
        }
    }

    protected void haveABreak() {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateLocation(UniqueID bodyID, UniversalBody b) {
        System.out.println("Updating location");
        l.updateLocation(bodyID, b);
    }

    public UniversalBody queryServer(UniqueID bodyID) {
        System.out.println("Querying the server");
        UniversalBody b = l.searchObject(bodyID);
        ProActive.waitFor(b);
        return b;
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Usage: java -Dproactive.locationserver=classe -Dproactive.locationserver.rmi=name "
                               + FakeBench.class + " <numberOfBench> "
                               + "<sleepTime>");
            System.exit(-1);
        }
        int max = Integer.parseInt(args[0]);
        Object[] param = new Object[]{new Long(args[1])};
        try {
            for (int i = 0; i < max; i++) {
                ProActive.newActive("modelisation.server.fakebench.FakeBench", param, null, null, TimedLocationServerMetaObjectFactory.newInstance());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
