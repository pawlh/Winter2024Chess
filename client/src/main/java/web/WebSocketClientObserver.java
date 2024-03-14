package web;

import webSocketMessages.serverMessages.ServerMessage;

public interface WebSocketClientObserver {
    void receiveMessage(ServerMessage message);
}
