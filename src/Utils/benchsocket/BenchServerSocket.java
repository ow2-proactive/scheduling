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
package benchsocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class BenchServerSocket extends ServerSocket {
    private BenchFactoryInterface parent;

    public BenchServerSocket() throws IOException {
        super();
    }

    public BenchServerSocket(int port, BenchFactoryInterface parent) throws IOException {
        super(port);
        this.parent = parent;
    }

    public BenchServerSocket(int port, InetAddress localAddress, BenchFactoryInterface parent)
            throws IOException {
        super(port, -1, localAddress);
        this.parent = parent;
    }

    @Override
    public Socket accept() throws IOException {
        Socket s = new Socket();
        implAccept(s);
        return new BenchClientSocket(s, parent);
    }
}
