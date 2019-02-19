package com.sshweb.common;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.ChannelInputStream;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Administrator on 2019/2/12.
 */
public class SSHClientUtils{

    private static Logger log = LoggerFactory.getLogger(SSHClientUtils.class);

    private static String  DEFAULTCHART="UTF-8";

    private static String ip = "10.46.101.16";
    private static String userName = "root";
    private static String userPwd = "ly123456";

    private static SSHClient ssh;
    private static Session session;
    private static Session.Shell shell;

    public static void init(){
        ssh = new SSHClient();
        try {
            ssh.addHostKeyVerifier(new PromiscuousVerifier());
            ssh.connect(ip);
            ssh.authPassword(userName, userPwd);

            session = ssh.startSession();
            session.allocateDefaultPTY();
            shell = session.startShell();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        init();

        new Thread() {
            public void run() {
                result(null);
            }
        }.start();

//            SSHClientUtils.shell("ls\n");
//                while (true){
//                    InputStream in = System.in;
//
//                    byte[] b = new byte[4];
//                    try {
//                        in.read(b);
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                    String s = new String(b);
//                    SSHClientUtils.shell(s);

//                }

    }

    public static void result(javax.websocket.Session session){

        try {
            int read;
            byte[] buf = new byte[shell.getLocalMaxPacketSize()];
            while((read = shell.getInputStream().read(buf)) != -1) {
                String a = new String(buf, 0, read);
//                WebSocket.sendMessage(session, a);
                session.getBasicRemote().sendBinary(str2ByteBuffer(a));
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }
    }

    public static ByteBuffer str2ByteBuffer(String message){
        return ByteBuffer.wrap(message.getBytes());
    }

    public static void shell(String cmd){
        try {
            shell.getOutputStream().write(cmd.getBytes());
            shell.getOutputStream().flush();
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void close(){
        try {
            if(shell != null)
                shell.close();
            if(session != null)
                session.close();
            if(ssh != null)
                ssh.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
