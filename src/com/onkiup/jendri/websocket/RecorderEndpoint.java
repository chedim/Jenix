package com.onkiup.jendri.websocket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.io.IOUtils;

@ServerEndpoint("/ws/recorder")
public class RecorderEndpoint {

    private static class SessionInfo {
        private OutputStream out;
        private RemoteEndpoint.Basic remoteEndpoint;
    }

    private HashMap<String, SessionInfo> sessions = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        try {
            UUID uuid = UUID.randomUUID();
            File dir = new File(System.getProperty("java.io.tmpdir") + "/recorder/");
            dir.mkdirs();
            File outFile = new File(dir, uuid.toString() + ".opus");
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.remoteEndpoint = session.getBasicRemote();
            sessionInfo.out = new FileOutputStream(outFile);
            sessions.put(session.getId(), sessionInfo);
            System.out.println(session.getId()+" | Writing into " + outFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        SessionInfo info = sessions.get(session.getId());
        try {
            info.out.close();
            sessions.remove(session.getId());
            System.out.println(session.getId() + " | closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @OnMessage
    public void onMessage(Session session, ByteBuffer bb, boolean last) {
        try {
            SessionInfo info = sessions.get(session.getId());
            info.out.write(bb.array());
            System.out.println(session.getId()+" | chunk");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
