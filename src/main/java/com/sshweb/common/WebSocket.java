package com.sshweb.common;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2019/1/23.
 */
@ServerEndpoint("/login/ws/{id}")
@Component
public class WebSocket  {

    private static Logger log = LoggerFactory.getLogger(WebSocket.class);
    private static int onlineCount = 0;
    private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<WebSocket>();

    private Session session;

    private static String cmd = "";

    private static int index = 0;

    //接收sid
    private String sid="";

    /**
     * 连接建立成功调用的方法*/
    @OnOpen
    public void onOpen(Session session, @PathParam("id") String sid) {

        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount());
        this.sid=sid;
        new Thread() {
            public void run() {
                SSHClientUtils.result(session);
            }
        }.start();
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        SSHClientUtils.close();
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
        cmd = "";
        index = 0;
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        //((?=[\x21-\x7e]+)[^A-Za-z0-9])
        //[ 0-9A-Za-z]+
        String pattern = "[ 0-9A-Za-z]+";
        Pattern r = Pattern.compile(pattern);
        String code = encode(message);
        String z = "\\u7b\\u22\\u64\\u61\\u74\\u61\\u22\\u3a\\u22\\u5c\\u75\\u30\\u30\\u31\\u62\\u5b\\u44\\u22\\u7d";
        String y = "\\u7b\\u22\\u64\\u61\\u74\\u61\\u22\\u3a\\u22\\u5c\\u75\\u30\\u30\\u31\\u62\\u5b\\u43\\u22\\u7d";
        Data data = JSON.parseObject(message, Data.class);
        if(data.getData() == null){
            return;
        }
        if(z.equals(code)){
            index ++;
        }
        if(index > 0 && y.equals(code)){
            index --;
        }

        String qian = "";
        String hou = "";
        if(cmd.length() > 0){
            qian = cmd.substring(0, cmd.length() - index);
            hou = cmd.substring(cmd.length() - index, cmd.length());
        }

        Matcher matcher = r.matcher(data.getData());
        boolean b = matcher.matches();
        boolean b1 = "\\u7b\\u22\\u64\\u61\\u74\\u61\\u22\\u3a\\u22\\u5c\\u72\\u22\\u7d".equals(code); //回车
        boolean b2 = "\\u7b\\u22\\u64\\u61\\u74\\u61\\u22\\u3a\\u22\\u7f\\u22\\u7d".equals(code);//回退符

        String resultdata = "";

        if(!b && !b1 && !b2){
            resultdata = data.getData();
        } else if(b1){
            System.out.println("执行的命令 : " + cmd);
            SSHClientUtils.shell(cmd + "\n");
            cmd = "";
        } else if(b2){
            if("".equals(cmd))
                resultdata = "";
            else{
                String ht = "\b\u001B[K";
                if(index > 0){
                    ht = "\b\u001B[1P";
                    String a = cmd.substring(cmd.length() - index, cmd.length());
                    ht += a;
                    for(int i = 0; i < index; i ++){
                        ht += "\b";
                    }
                }
                qian = qian.substring(0, qian.length() - 1);
                cmd = qian + hou;
                resultdata = ht;
            }
        } else{
            String c = data.getData();
            cmd = qian + c + hou;
            c += hou;
            for(int i = 0; i < hou.length(); i ++){
                c += "\b";
            }
            resultdata = c;
        }
        //群发消息
        for (WebSocket item : webSocketSet) {
            try {
                if(!"".equals(resultdata) && resultdata != null)
                    item.sendMessage(resultdata);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendBinary(str2ByteBuffer(message));
    }

    public static ByteBuffer str2ByteBuffer(String message){
        return ByteBuffer.wrap(message.getBytes());
    }

    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message,@PathParam("id") String sid) throws IOException {
        log.info("推送消息到窗口"+sid+"，推送内容:"+message);

        for (WebSocket item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(sid==null) {
                    item.sendMessage(message);
                }else if(item.sid.equals(sid)){
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                continue;
            }
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    public static String encode(String str) {
        String prefix = "\\u";
        StringBuffer sb = new StringBuffer();
        char[] chars = str.toCharArray();
        if (chars == null || chars.length == 0) {
            return null;
        }
        for (char c : chars) {
            sb.append(prefix);
            sb.append(Integer.toHexString(c));
        }
        return sb.toString();
    }
}
