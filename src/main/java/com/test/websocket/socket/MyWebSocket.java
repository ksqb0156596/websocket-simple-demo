package com.test.websocket.socket;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Administrator on 2017/5/1.
 */
@Component
@ServerEndpoint(value="/websocket")
public class MyWebSocket {
    private static int online = 0;
    private static CopyOnWriteArraySet<MyWebSocket> webSockets = new CopyOnWriteArraySet<>();
    private Session session;

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        webSockets.add(this);
        addCount();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count",getCount());
        jsonObject.put("command","init");
        this.sendMessage(jsonObject);
        jsonObject.put("command","add");
        this.sendMessageToAll(jsonObject);

    }

    @OnClose
    public void onClose(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command","sub");
        webSockets.remove(this);
        this.sendMessageToAll(jsonObject);
        subCount();
    }

    @OnMessage
    public void onMessage(String message,Session session){
        this.session = session;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command","message");
        jsonObject.put("content",message);
        this.sendMessageToAll(jsonObject);
    }

    public static synchronized void addCount(){
        MyWebSocket.online++;
    }
    public static synchronized void subCount(){
        MyWebSocket.online--;
    }
    public static synchronized int getCount(){
        return MyWebSocket.online;
    }

    public void sendMessage(JSONObject message){
        try {
            this.session.getBasicRemote().sendText(message.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessageToAll(JSONObject message){
        try {
            for(MyWebSocket socket : MyWebSocket.webSockets){
                socket.session.getBasicRemote().sendText(message.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
