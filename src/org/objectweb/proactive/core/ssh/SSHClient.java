
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
 * <commandPath value="c:\...\proactive\scripts\windows\scp.bat -p password"/>
 * <processReference refid="localJVM"></processReference> 
 * </sshProcess>
 * </processDefinition>
 */
public class SSHClient
{

    /**
     * @param args
     */

    private static String buildCmdLine(String[] args, int index)
    {
        String cmd = "";

        for (int i = index; i < args.length; i++)
        {
            cmd += " " + args[i];
        }

        return cmd;
    }

    public static void main(String[] args)
    {

        if (args.length < 2)
        {
            System.err.println("not enought arguments\n" + "usage : " + SSHClient.class.getName()
                               + " username@host [-p password] cmdline");
            System.exit(1);
        }

//        for (int i = 0; i < args.length; i++)
//        {
//            System.out.println(args[i]);
//        }

        try
        {

            String host = "";
            String password = "";
            String command = "";
            String user = "";
            int index = 0;

            if ("-p".equals(args[index]))
            {
                password = args[index + 1];
                index += 2;
            }

            if ("-l".equals(args[index]))
            {
                user = args[index + 1];
                index += 2;
            }

            host = args[index];
            index++;
            command = buildCmdLine(args, index);

            JSch jsch = new JSch();

            Session session = jsch.getSession(user, host, 22);
            session.setPassword(password);

            java.util.Hashtable config = new java.util.Hashtable();
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
            while (true)
            {
                while (in.available() > 0)
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed())
                {
                    in.close();
                  //  System.out.println("JSCH: exit-status: " + channel.getExitStatus());
                    break;
                }
                try
                {
                    Thread.sleep(1000);
                }
                catch (Exception ee)
                {
                }
            }
            channel.disconnect();
            session.disconnect();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.exit(0);
    }

}
