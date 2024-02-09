package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.ServerMessage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private Map<Integer, Set<Session>> sessions = new ConcurrentHashMap<>();


    public void addSession(int gameId, Session session) {
        if(!sessions.containsKey(gameId)) sessions.put(gameId, Collections.synchronizedSet(new HashSet<>()));
        sessions.get(gameId).add(session);
    }

    public void removeSession(int gameId, Session session) {
        if(sessions.containsKey(gameId)) sessions.get(gameId).remove(session);
    }

    public void broadcast(String message, int gameId, Session exclude) throws IOException {
        for (Session ses : sessions.get(gameId)) {
            if (ses != exclude) sendMessage(ses, message);
        }
    }


    public void sendError(Session session, String message) throws IOException {
        ServerMessage error = new ServerMessage(ServerMessage.ServerMessageType.ERROR, message);
        sendMessage(session, new Gson().toJson(error));
    }


    public void sendMessage(Session session, String message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(message);
        }
    }

    public void clear() {
        sessions.clear();
    }
}
