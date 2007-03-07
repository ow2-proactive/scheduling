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
package org.objectweb.proactive.core.ssh;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


/**
 * @author arnaud contes this class simulates an ssh client in the use case of a
 * remote execution of a command. Useful under a windows system without ssh a
 * process using this client should be written according the following pattern
 * <processDefinition id="ssh_crusoe">
 * <sshProcess class="org.objectweb.proactive.core.process.ssh.SSHProcess" hostname="host" username="username">
 * <commandPath value="c:\...\proactive\scripts\windows\ssh.bat -p password"/>
 * <processReference refid="localJVM"></processReference>
 * </sshProcess>
 * </processDefinition>
 */
public class SSHClient {

    /**
     * @param args
     */
    private static String buildCmdLine(String[] args, int index) {
        String cmd = "";

        for (int i = index; i < args.length; i++) {
            cmd += (" " + args[i]);
        }

        return cmd;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("not enought arguments\n" + "usage : " +
                SSHClient.class.getName() +
                " username@host [-p password] cmdline");
            System.exit(1);
        }

        //        for (int i = 0; i < args.length; i++)
        //        {
        //            System.out.println(args[i]);
        //        }
        try {
            String host = "";
            String password = "";
            String command = "";
            String user = "";
            int index = 0;

            if ("-p".equals(args[index])) {
                password = args[index + 1];
                index += 2;
            }

            if ("-l".equals(args[index])) {
                user = args[index + 1];
                index += 2;
            }

            host = args[index];
            index++;
            command = buildCmdLine(args, index);

            JSch jsch = new JSch();

            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            java.util.Hashtable<String, String> config = new java.util.Hashtable<String, String>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            // System.out.println("---" + command + "---");
            session.connect(3000); // making connection with timeout.

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setXForwarding(true);

            channel.setInputStream(System.in);
            // channel.setOutputStream(System.out);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    in.close();
                    //  System.out.println("JSCH: exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
