package com.sshweb.common;

import net.schmizz.concurrent.Event;
import net.schmizz.concurrent.ExceptionChainer;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.LoggerFactory;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.*;

/**
 * Created by Administrator on 2019/2/13.
 */
public class Test
{
//    private static final Console con = System.console();

    private static String userName = "root";
    private static String userPwd = "ly123456";
    private static String ip = "10.46.101.16";

    public static void main(String... args)
            throws IOException {

        final SSHClient ssh = new SSHClient();

        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(ip);
        ssh.authPassword(userName, userPwd);
        try {

            final Session session = ssh.startSession();
            try {

                session.allocateDefaultPTY();

                final Session.Shell shell = session.startShell();

//                new StreamCopier(shell.getErrorStream(), System.err, LoggerFactory.DEFAULT)
//                        .bufSize(shell.getLocalMaxPacketSize())
//                        .spawn("stderr");

                new Thread() {
                    public void run() {
//                        try {
//                            int read = 0;
//                            byte[] buf = new byte[shell.getLocalMaxPacketSize()];
//                            int index = 0;
//                            while((read = shell.getInputStream().read(buf)) != -1) {
//                                index ++;
//                                System.out.println("循环第" + index + "遍");
//                                String a = new String(buf, 0, read);
//                                System.out.println(a);
//                            }
//                        } catch (IOException var2) {
//                            var2.printStackTrace();
//                        }

                    }
                }.start();

                byte[] buf = new byte[1024];
                int read;
//                while((read = System.in.read(buf)) != -1) {
                    read = System.in.read(buf);
                    shell.getOutputStream().write(buf, 0, read);
                    shell.getOutputStream().flush();
//                }


//                try {
////                    int read = 0;
////                    byte[] buf = new byte[shell.getLocalMaxPacketSize()];
//                    int index = 0;
//                    while((read = shell.getInputStream().read(buf)) != -1) {
//                        index ++;
//                        System.out.println("循环第" + index + "遍");
//                        String a = new String(buf, 0, read);
//                        System.out.println(a);
//                    }
//                } catch (IOException var2) {
//                    var2.printStackTrace();
//                }
            } finally {
                session.close();
            }

        } finally {
            ssh.disconnect();
            ssh.close();
        }
    }
}
