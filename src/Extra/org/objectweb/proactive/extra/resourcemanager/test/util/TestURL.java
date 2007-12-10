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
package org.objectweb.proactive.extra.resourcemanager.test.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


public class TestURL {
    public static void main(String[] args) {
        try {
            //URL url1 = new URL("http://localhost/HelloLocal.xml");
            //URL url1 = new URL("http://localhost.titi/truc/machin/HelloLocal.xml");
            URL url1 = new URL("http://localhost.titi/");
            URL url2 = new URL("http", "localhost", "HelloLocal.xml");
            URL url3 = new URL("http", "localhost", 2010, "HelloLocal.xml");

            System.out.println("URL 1 : " + url1);
            System.out.println("URL 2 : " + url2);
            System.out.println("URL 3 : " + url3);

            System.out.println(
                "-------------------------------------------------");
            System.out.println("URL 1 :");
            System.out.println(url1);
            System.out.println("+--> protocol : " + url1.getProtocol());
            System.out.println("+--> host     : " + url1.getHost());
            System.out.println("+--> file     : " + url1.getFile());
            System.out.println("+--> query    : " + url1.getQuery());
            System.out.println("+--> ref      : " + url1.getRef());
            System.out.println("+--> path     : " + url1.getPath());
            System.out.println(
                "-------------------------------------------------");

            //URI uri = url1.toURI();
            URI uri = new URI("rmi://localhost/titi/truc/machin/HelloLocal.xml");

            System.out.println("URI :");
            System.out.println(uri);
            System.out.println("+--> path     : " + uri.getPath());
            System.out.println("+--> host     : " + uri.getHost());
            System.out.println("+--> fragment : " + uri.getFragment());
            System.out.println("+--> query    : " + uri.getQuery());
            System.out.println("+--> scheme   : " + uri.getScheme());
            System.out.println(
                "-------------------------------------------------");

            System.out.println("Slitons un peu le path de l'uri : ");

            String path = uri.getPath();
            String[] chemin = path.split("/");
            String file = chemin[chemin.length - 1];
            System.out.println(file);

            URI uri1 = new URI("/localhost/titi/truc/machin/HelloLocal.xml");

            String path1 = uri1.getPath();
            String[] chemin1 = path1.split("/");
            String file1 = chemin1[chemin1.length - 1];
            System.out.println(file1);

            System.out.println("URI : HelloLocal.xml");

            URI uri2 = new URI("HelloLocal.xml");

            String path2 = uri2.getPath();
            String[] chemin2 = path2.split("/");
            String file2 = chemin2[chemin2.length - 1];
            System.out.println(file2);

            URL uri2url = uri.toURL();
            System.out.println(uri2url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
